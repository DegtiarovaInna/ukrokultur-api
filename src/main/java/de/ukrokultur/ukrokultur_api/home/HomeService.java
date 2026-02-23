package de.ukrokultur.ukrokultur_api.home;

import de.ukrokultur.ukrokultur_api.common.dto.home.HomeResponseDto;
import de.ukrokultur.ukrokultur_api.common.dto.home.HomeUpsertRequestDto;
import de.ukrokultur.ukrokultur_api.common.dto.I18nText;
import de.ukrokultur.ukrokultur_api.common.error.ErrorCode;
import de.ukrokultur.ukrokultur_api.common.exception.ApiException;
import de.ukrokultur.ukrokultur_api.media.MediaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.UUID;
import de.ukrokultur.ukrokultur_api.common.slug.SlugGenerator;

@Service
@Transactional
public class HomeService {

    private static final long HOME_ID = 1L;

    private final HomePageRepository pageRepo;
    private final HomeWorkFieldRepository workRepo;
    private final MediaService mediaService;

    public HomeService(HomePageRepository pageRepo, HomeWorkFieldRepository workRepo, MediaService mediaService) {
        this.pageRepo = pageRepo;
        this.workRepo = workRepo;
        this.mediaService = mediaService;
    }

    public HomeResponseDto getPublic() {
        HomePage page = getOrCreate();
        List<HomeWorkFieldItem> items = workRepo.findAllByOrderBySortOrderAsc();

        List<HomeResponseDto.HomeWorkFieldItemDto> itemDtos = items.stream()
                .filter(HomeWorkFieldItem::isPublished)
                .map(this::toItemDto)
                .toList();

        return toResponse(page, itemDtos);
    }

    public HomeResponseDto getAdmin() {
        HomePage page = getOrCreate();
        List<HomeWorkFieldItem> items = workRepo.findAllByOrderBySortOrderAsc();

        List<HomeResponseDto.HomeWorkFieldItemDto> itemDtos = items.stream()
                .map(this::toItemDto)
                .toList();

        return toResponse(page, itemDtos);
    }

    public HomeResponseDto upsert(HomeUpsertRequestDto req) {
        HomePage page = getOrCreate();
        applyPage(page, req);

        List<HomeWorkFieldItem> existing = workRepo.findAllByOrderBySortOrderAsc();
        Map<UUID, HomeWorkFieldItem> byId = new HashMap<>();
        for (HomeWorkFieldItem e : existing) {
            byId.put(e.getPublicId(), e);
        }

        Set<UUID> incomingIds = new HashSet<>();

        List<HomeUpsertRequestDto.HomeWorkFieldItemUpsertDto> incoming = req.workFields().items();
        if (incoming == null) {
            throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "workFields.items is required");
        }

        int sort = 0;
        for (HomeUpsertRequestDto.HomeWorkFieldItemUpsertDto it : incoming) {
            UUID id = parseUuidOrNull(it.id());
            HomeWorkFieldItem e;
            if (id != null) {
                e = byId.get(id);
                if (e == null) {
                    throw new ApiException(404, ErrorCode.NOT_FOUND, "HomeWorkFieldItem not found: " + id);
                }
                incomingIds.add(id);
            } else {
                e = new HomeWorkFieldItem();
            }

            applyWorkField(e, it, sort);

            workRepo.save(e);
            incomingIds.add(e.getPublicId());

            sort++;
        }

        for (HomeWorkFieldItem old : existing) {
            if (!incomingIds.contains(old.getPublicId())) {
                workRepo.delete(old);
            }
        }

        pageRepo.save(page);
        return getAdmin();
    }


    public HomeResponseDto upsertMultipart(HomeUpsertRequestDto data, MultipartFile heroImage, MultipartFile missionImage) {
        HomeUpsertRequestDto.HomeHeroUpsertDto hero = data.hero();
        HomeUpsertRequestDto.HomeMissionUpsertDto mission = data.mission();

        String heroUrl = hero.image();
        if (heroImage != null && !heroImage.isEmpty()) {
            heroUrl = mediaService.upload(heroImage, "home").publicUrl();
        }

        String missionUrl = mission.image();
        if (missionImage != null && !missionImage.isEmpty()) {
            missionUrl = mediaService.upload(missionImage, "home").publicUrl();
        }

        HomeUpsertRequestDto req = new HomeUpsertRequestDto(
                new HomeUpsertRequestDto.HomeHeroUpsertDto(
                        heroUrl,
                        hero.title(),
                        hero.subtitle(),
                        hero.published()
                ),
                new HomeUpsertRequestDto.HomeMissionUpsertDto(
                        missionUrl,
                        mission.title(),
                        mission.text(),
                        mission.published()
                ),
                data.workFields()
        );

        return upsert(req);
    }

    private void applyWorkField(HomeWorkFieldItem e, HomeUpsertRequestDto.HomeWorkFieldItemUpsertDto it, int sortOrder) {
        e.setSortOrder(sortOrder);
        e.setPublished(Boolean.TRUE.equals(it.published()));

        I18nText t = it.title();
        I18nText d = it.description();
        e.setTitleEn(t.en());
        e.setTitleDe(t.de());
        e.setTitleUk(t.uk());

        e.setDescriptionEn(d.en());
        e.setDescriptionDe(d.de());
        e.setDescriptionUk(d.uk());

        String requestedSlug = safeTrim(it.slug());
        if (StringUtils.hasText(requestedSlug)) {
            String normalized = SlugGenerator.slugify(requestedSlug);
            if (!StringUtils.hasText(normalized)) {
                throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "workFields.items.slug is invalid");
            }
            if ((e.getSlug() == null || !e.getSlug().equals(normalized)) && workRepo.existsBySlug(normalized)) {
                throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "Slug already exists: " + normalized);
            }
            e.setSlug(normalized);
        } else if (!StringUtils.hasText(e.getSlug())) {
            String base = firstNonBlank(t.de(), t.en(), t.uk());
            e.setSlug(SlugGenerator.generateUnique(base, workRepo::existsBySlug));
        }
    }

    private static UUID parseUuidOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return UUID.fromString(s.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private static String firstNonBlank(String... values) {
        if (values == null) return "item";
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return "item";
    }

    private HomePage getOrCreate() {
        return pageRepo.findById(HOME_ID).orElseGet(() -> {
            HomePage p = new HomePage();

            p.setHeroTitleEn(" ");
            p.setHeroTitleDe(" ");
            p.setHeroTitleUk(" ");
            p.setHeroSubtitleEn(" ");
            p.setHeroSubtitleDe(" ");
            p.setHeroSubtitleUk(" ");

            p.setMissionTitleEn(" ");
            p.setMissionTitleDe(" ");
            p.setMissionTitleUk(" ");
            p.setMissionTextEn(" ");
            p.setMissionTextDe(" ");
            p.setMissionTextUk(" ");

            p.setHeroPublished(true);
            p.setMissionPublished(true);
            p.setWorkFieldsPublished(true);

            return pageRepo.save(p);
        });
    }

    private void applyPage(HomePage page, HomeUpsertRequestDto req) {
        if (req.hero() == null || req.mission() == null || req.workFields() == null) {
            throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "hero/mission/workFields are required");
        }

        page.setHeroImage(req.hero().image());

        page.setHeroTitleEn(req.hero().title().en());
        page.setHeroTitleDe(req.hero().title().de());
        page.setHeroTitleUk(req.hero().title().uk());

        page.setHeroSubtitleEn(req.hero().subtitle().en());
        page.setHeroSubtitleDe(req.hero().subtitle().de());
        page.setHeroSubtitleUk(req.hero().subtitle().uk());

        page.setHeroPublished(Boolean.TRUE.equals(req.hero().published()));

        page.setMissionImage(req.mission().image());

        page.setMissionTitleEn(req.mission().title().en());
        page.setMissionTitleDe(req.mission().title().de());
        page.setMissionTitleUk(req.mission().title().uk());

        page.setMissionTextEn(req.mission().text().en());
        page.setMissionTextDe(req.mission().text().de());
        page.setMissionTextUk(req.mission().text().uk());

        page.setMissionPublished(Boolean.TRUE.equals(req.mission().published()));

        page.setWorkFieldsPublished(Boolean.TRUE.equals(req.workFields().published()));
    }

    private HomeResponseDto.HomeWorkFieldItemDto toItemDto(HomeWorkFieldItem e) {
        return new HomeResponseDto.HomeWorkFieldItemDto(
                e.getPublicId() == null ? null : e.getPublicId().toString(),
                e.getSlug(),
                e.getSortOrder(),
                e.isPublished(),
                new I18nText(e.getTitleEn(), e.getTitleDe(), e.getTitleUk()),
                new I18nText(e.getDescriptionEn(), e.getDescriptionDe(), e.getDescriptionUk())
        );
    }

    private HomeResponseDto toResponse(HomePage page, List<HomeResponseDto.HomeWorkFieldItemDto> items) {
        HomeResponseDto.HomeHeroDto hero = new HomeResponseDto.HomeHeroDto(
                page.getHeroImage(),
                new I18nText(page.getHeroTitleEn(), page.getHeroTitleDe(), page.getHeroTitleUk()),
                new I18nText(page.getHeroSubtitleEn(), page.getHeroSubtitleDe(), page.getHeroSubtitleUk()),
                page.isHeroPublished()
        );

        HomeResponseDto.HomeMissionDto mission = new HomeResponseDto.HomeMissionDto(
                page.getMissionImage(),
                new I18nText(page.getMissionTitleEn(), page.getMissionTitleDe(), page.getMissionTitleUk()),
                new I18nText(page.getMissionTextEn(), page.getMissionTextDe(), page.getMissionTextUk()),
                page.isMissionPublished()
        );

        HomeResponseDto.HomeWorkFieldsDto workFields = new HomeResponseDto.HomeWorkFieldsDto(
                page.isWorkFieldsPublished(),
                items
        );

        return new HomeResponseDto(hero, mission, workFields, page.getUpdatedAt());
    }

    private static String safeTrim(String s) {
        if (!StringUtils.hasText(s)) return null;
        return s.trim();
    }
}