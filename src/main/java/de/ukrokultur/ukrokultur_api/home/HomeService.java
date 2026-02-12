package de.ukrokultur.ukrokultur_api.home;

import de.ukrokultur.ukrokultur_api.common.dto.home.HomeResponseDto;
import de.ukrokultur.ukrokultur_api.common.dto.home.HomeUpsertRequestDto;
import de.ukrokultur.ukrokultur_api.common.dto.I18nText;
import de.ukrokultur.ukrokultur_api.common.error.ErrorCode;
import de.ukrokultur.ukrokultur_api.common.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class HomeService {

    private static final long HOME_ID = 1L;

    private final HomePageRepository pageRepo;
    private final HomeWorkFieldRepository workRepo;

    public HomeService(HomePageRepository pageRepo, HomeWorkFieldRepository workRepo) {
        this.pageRepo = pageRepo;
        this.workRepo = workRepo;
    }

    @Transactional(readOnly = true)
    public HomeResponseDto getPublic() {
        HomePage page = getOrCreate();
        List<HomeWorkFieldItem> items = workRepo.findAllByOrderBySortOrderAsc();


        List<HomeResponseDto.HomeWorkFieldItemDto> itemDtos = items.stream()
                .filter(HomeWorkFieldItem::isPublished)
                .map(this::toItemDto)
                .toList();

        return toResponse(page, itemDtos);
    }

    @Transactional(readOnly = true)
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


        workRepo.deleteAllInBatch();

        if (req.workFields() != null && req.workFields().items() != null) {
            for (HomeUpsertRequestDto.HomeWorkFieldItemUpsertDto it : req.workFields().items()) {
                HomeWorkFieldItem e = new HomeWorkFieldItem();
                e.setId(it.id().trim());
                e.setSortOrder(it.order());
                e.setPublished(Boolean.TRUE.equals(it.published()));

                I18nText t = it.title();
                e.setTitleEn(t.en());
                e.setTitleDe(t.de());
                e.setTitleUk(t.uk());

                I18nText d = it.description();
                e.setDescriptionEn(d.en());
                e.setDescriptionDe(d.de());
                e.setDescriptionUk(d.uk());

                workRepo.save(e);
            }
        }

        pageRepo.save(page);

        return getAdmin();
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
                e.getId(),
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
}
