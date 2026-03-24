package de.ukrokultur.ukrokultur_api.projects;

import de.ukrokultur.ukrokultur_api.common.dto.I18nText;
import de.ukrokultur.ukrokultur_api.common.dto.projects.*;
import de.ukrokultur.ukrokultur_api.common.error.ErrorCode;
import de.ukrokultur.ukrokultur_api.common.exception.ApiException;
import de.ukrokultur.ukrokultur_api.common.exception.NotFoundException;
import de.ukrokultur.ukrokultur_api.common.slug.SlugGenerator;
import de.ukrokultur.ukrokultur_api.media.MediaService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.UUID;
import java.util.function.Predicate;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository repo;
    private final MediaService mediaService;

    public ProjectService(ProjectRepository repo, MediaService mediaService) {
        this.repo = repo;
        this.mediaService = mediaService;
    }

    @Transactional(readOnly = true)
    public ProjectPageResultDto getPage(int page1Based, int pageSize, boolean publishedOnly) {
        int safeSize = Math.min(Math.max(pageSize, 1), 50);
        int page0 = Math.max(page1Based - 1, 0);

        Pageable pageable = PageRequest.of(page0, safeSize);
        Page<Project> p = repo.findPageOrdered(publishedOnly, pageable);

        List<ProjectItemDto> items = p.getContent().stream().map(this::toDto).toList();

        return new ProjectPageResultDto(
                items,
                page1Based,
                safeSize,
                p.getTotalElements(),
                p.getTotalPages()
        );
    }
    @Transactional(readOnly = true)
    public ProjectItemDto getByIdPublic(UUID publicId) {
        Project p = repo.findByPublicId(publicId)
                .orElseThrow(() -> NotFoundException.of("Project", publicId));

        if (!p.isPublished()) {
            throw NotFoundException.of("Project", publicId);
        }

        return toDto(p);
    }
    @Transactional(readOnly = true)
    public ProjectItemDto getByIdAdmin(UUID publicId) {
        Project p = repo.findByPublicId(publicId)
                .orElseThrow(() -> NotFoundException.of("Project", publicId));
        return toDto(p);
    }
    public ProjectItemDto createMultipart(ProjectUpsertRequestDto data, MultipartFile coverImage, List<MultipartFile> galleryImages) {
        String uploadedCover = null;
        List<String> uploadedGallery = List.of();

        try {
            String coverUrl = data.coverImage();
            if (coverImage != null && !coverImage.isEmpty()) {
                uploadedCover = mediaService.upload(coverImage, "projects").publicUrl();
                coverUrl = uploadedCover;
            }

            List<String> galleryUrls = data.galleryImages();
            if (galleryImages != null && !galleryImages.isEmpty()) {
                uploadedGallery = mediaService.uploadMany(galleryImages, "projects")
                        .stream().map(x -> x.publicUrl()).toList();
                galleryUrls = uploadedGallery;
            }

            ProjectUpsertRequestDto req = new ProjectUpsertRequestDto(
                    data.slug(),
                    data.title(),
                    data.subtitle(),
                    coverUrl,
                    galleryUrls,
                    data.startDate(),
                    data.endDate(),
                    data.description(),
                    data.goals(),
                    data.activities(),
                    data.partners(),
                    data.published(),
                    data.order()
            );

            return create(req);

        } catch (RuntimeException ex) {
            mediaService.deleteByPublicUrlQuietly(uploadedCover);
            mediaService.deleteManyByPublicUrlsQuietly(uploadedGallery);
            throw ex;
        }
    }

    public ProjectItemDto updateMultipart(UUID id, ProjectUpsertRequestDto data, MultipartFile coverImage, List<MultipartFile> galleryImages) {
        Project existing = repo.findByPublicId(id).orElseThrow(() -> NotFoundException.of("Project", id));

        String oldCover = existing.getCoverImage();
        List<String> oldGallery = extractGalleryUrls(existing);

        String uploadedCover = null;
        List<String> uploadedGallery = List.of();

        try {
            String coverUrl = (data.coverImage() != null ? data.coverImage() : oldCover);
            if (coverImage != null && !coverImage.isEmpty()) {
                uploadedCover = mediaService.upload(coverImage, "projects").publicUrl();
                coverUrl = uploadedCover;
            }

            List<String> galleryUrls = (data.galleryImages() != null ? data.galleryImages() : oldGallery);
            if (galleryImages != null && !galleryImages.isEmpty()) {
                uploadedGallery = mediaService.uploadMany(galleryImages, "projects")
                        .stream().map(x -> x.publicUrl()).toList();
                galleryUrls = uploadedGallery;
            }

            ProjectUpsertRequestDto req = new ProjectUpsertRequestDto(
                    data.slug(),
                    data.title(),
                    data.subtitle(),
                    coverUrl,
                    galleryUrls,
                    data.startDate(),
                    data.endDate(),
                    data.description(),
                    data.goals(),
                    data.activities(),
                    data.partners(),
                    data.published(),
                    data.order()
            );

            ProjectItemDto out = update(id, req);

            if (uploadedCover != null) {
                mediaService.deleteByPublicUrlQuietly(oldCover);
            }
            if (uploadedGallery != null && !uploadedGallery.isEmpty()) {
                mediaService.deleteManyByPublicUrlsQuietly(oldGallery);
            }

            return out;

        } catch (RuntimeException ex) {
            mediaService.deleteByPublicUrlQuietly(uploadedCover);
            mediaService.deleteManyByPublicUrlsQuietly(uploadedGallery);
            throw ex;
        }
    }

    public ProjectItemDto create(ProjectUpsertRequestDto req) {
        Project p = new Project();

        int max = repo.findMaxSortOrder();
        p.setSortOrder(max + 1);

        String requestedSlug = safeTrim(req.slug());
        if (StringUtils.hasText(requestedSlug)) {
            String normalized = SlugGenerator.slugify(requestedSlug);
            if (!StringUtils.hasText(normalized)) {
                throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "Slug is invalid");
            }
            p.setSlug(normalized);
        } else {
            p.setSlug(null);
        }

        applyUpsert(p, req);

        ensureSlug(p, req);

        if (StringUtils.hasText(p.getSlug()) && repo.existsBySlug(p.getSlug())) {
            throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "Slug already exists: " + p.getSlug());
        }

        return toDto(repo.save(p));
    }

    public ProjectItemDto update(UUID publicId, ProjectUpsertRequestDto req) {
        Project p = repo.findByPublicId(publicId).orElseThrow(() -> NotFoundException.of("Project", publicId));

        String requested = safeTrim(req.slug());
        if (StringUtils.hasText(requested) && !Objects.equals(requested, p.getSlug())) {
            String normalized = SlugGenerator.slugify(requested);
            if (!StringUtils.hasText(normalized)) {
                throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "Slug is invalid");
            }
            if (repo.existsBySlug(normalized)) {
                throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "Slug already exists: " + normalized);
            }
            p.setSlug(normalized);
        }

        if (req.order() != null) {
            moveProject(p, req.order());
        }

        applyUpsert(p, req);

        if (!StringUtils.hasText(p.getSlug())) {
            ensureSlug(p, req);
        }

        return toDto(p);
    }

    public void delete(UUID publicId) {
        Project p = repo.findByPublicId(publicId)
                .orElseThrow(() -> NotFoundException.of("Project", publicId));

        String cover = p.getCoverImage();
        List<String> gallery = extractGalleryUrls(p);

        repo.delete(p);
        compressOrder();

        mediaService.deleteByPublicUrlQuietly(cover);
        mediaService.deleteManyByPublicUrlsQuietly(gallery);
    }

    public ProjectItemDto addGalleryImages(UUID publicId, List<MultipartFile> galleryImages) {
        if (galleryImages == null || galleryImages.isEmpty()) {
            throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "galleryImages are required");
        }

        List<String> uploaded = List.of();

        try {
            Project p = repo.findByPublicId(publicId)
                    .orElseThrow(() -> NotFoundException.of("Project", publicId));

            uploaded = mediaService.uploadMany(galleryImages, "projects")
                    .stream().map(x -> x.publicUrl()).toList();

            int startOrder = p.getImages().size();
            for (String url : uploaded) {
                ProjectImage img = new ProjectImage();
                img.setUrl(url);
                img.setSortOrder(startOrder++);
                p.addImage(img);
            }

            normalizeProjectGallery(p);
            return toDto(p);

        } catch (RuntimeException ex) {
            mediaService.deleteManyByPublicUrlsQuietly(uploaded);
            throw ex;
        }
    }

    public ProjectItemDto deleteOneGalleryImage(UUID publicId, String url) {
        if (!StringUtils.hasText(url)) {
            throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "url is required");
        }

        Project p = repo.findByPublicId(publicId)
                .orElseThrow(() -> NotFoundException.of("Project", publicId));

        boolean removed = p.getImages().removeIf(img -> url.equals(img.getUrl()));
        if (!removed) {
            throw new ApiException(404, ErrorCode.NOT_FOUND, "Image not found in project gallery");
        }

        normalizeProjectGallery(p);

        mediaService.deleteByPublicUrlQuietly(url);

        return toDto(p);
    }

    public ProjectItemDto reorderGallery(UUID publicId, List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "urls are required");
        }

        Project p = repo.findByPublicId(publicId)
                .orElseThrow(() -> NotFoundException.of("Project", publicId));

        Map<String, ProjectImage> byUrl = new LinkedHashMap<>();
        for (ProjectImage img : p.getImages()) {
            if (img != null && StringUtils.hasText(img.getUrl())) {
                byUrl.put(img.getUrl(), img);
            }
        }

        for (String u : urls) {
            if (!byUrl.containsKey(u)) {
                throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "Unknown url in order list: " + u);
            }
        }
        if (urls.size() != byUrl.size()) {
            throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "Order list must contain all existing image urls");
        }

        List<ProjectImage> ordered = new ArrayList<>();
        for (int i = 0; i < urls.size(); i++) {
            ProjectImage img = byUrl.get(urls.get(i));
            img.setSortOrder(i);
            ordered.add(img);
        }

        p.getImages().clear();
        for (ProjectImage img : ordered) {
            p.addImage(img);
        }

        return toDto(p);
    }

    private List<String> extractGalleryUrls(Project p) {
        return p.getImages().stream()
                .map(ProjectImage::getUrl)
                .filter(StringUtils::hasText)
                .toList();
    }

    private void normalizeProjectGallery(Project p) {
        p.getImages().sort(Comparator.comparingInt(ProjectImage::getSortOrder));
        for (int i = 0; i < p.getImages().size(); i++) {
            p.getImages().get(i).setSortOrder(i);
        }
    }

    private void moveProject(Project target, int newIndex) {
        List<Project> all = repo.findAll(Sort.by(Sort.Direction.ASC, "sortOrder", "createdAt"));
        all.removeIf(x -> x.getPublicId().equals(target.getPublicId()));

        int idx = Math.max(0, Math.min(newIndex, all.size()));
        all.add(idx, target);

        for (int i = 0; i < all.size(); i++) {
            all.get(i).setSortOrder(i);
        }
        repo.saveAll(all);
    }

    private void compressOrder() {
        List<Project> all = repo.findAll(Sort.by(Sort.Direction.ASC, "sortOrder", "createdAt"));
        for (int i = 0; i < all.size(); i++) {
            all.get(i).setSortOrder(i);
        }
        repo.saveAll(all);
    }

    private void applyUpsert(Project p, ProjectUpsertRequestDto req) {
        p.setPublished(Boolean.TRUE.equals(req.published()));
        p.setStartDate(req.startDate());
        p.setEndDate(req.endDate());

        p.setCoverImage(req.coverImage());

        p.setTitleEn(req.title().en());
        p.setTitleDe(req.title().de());
        p.setTitleUk(req.title().uk());

        if (req.subtitle() != null) {
            p.setSubtitleEn(req.subtitle().en());
            p.setSubtitleDe(req.subtitle().de());
            p.setSubtitleUk(req.subtitle().uk());
        } else {
            p.setSubtitleEn(null);
            p.setSubtitleDe(null);
            p.setSubtitleUk(null);
        }

        p.setDescriptionEn(req.description().en());
        p.setDescriptionDe(req.description().de());
        p.setDescriptionUk(req.description().uk());

        p.clearImages();
        if (req.galleryImages() != null && !req.galleryImages().isEmpty()) {
            int order = 0;
            for (String url : req.galleryImages()) {
                if (!StringUtils.hasText(url)) continue;
                ProjectImage img = new ProjectImage();
                img.setUrl(url);
                img.setSortOrder(order);
                p.addImage(img);
                order++;
            }
        }

        p.clearGoals();
        if (req.goals() != null && !req.goals().isEmpty()) {
            int i = 0;
            for (I18nText g : req.goals()) {
                ProjectGoal goal = new ProjectGoal();
                goal.setSortOrder(i);
                goal.setEn(g.en());
                goal.setDe(g.de());
                goal.setUk(g.uk());
                p.addGoal(goal);
                i++;
            }
        }

        p.clearActivities();
        if (req.activities() != null && !req.activities().isEmpty()) {
            int i = 0;
            for (I18nText a : req.activities()) {
                ProjectActivity act = new ProjectActivity();
                act.setSortOrder(i);
                act.setEn(a.en());
                act.setDe(a.de());
                act.setUk(a.uk());
                p.addActivity(act);
                i++;
            }
        }

        p.clearPartners();
        if (req.partners() != null && !req.partners().isEmpty()) {
            int i = 0;
            for (ProjectPartnerDto dto : req.partners()) {
                ProjectPartner partner = new ProjectPartner();
                partner.setSortOrder(i);
                partner.setCountryEn(dto.country().en());
                partner.setCountryDe(dto.country().de());
                partner.setCountryUk(dto.country().uk());
                partner.setOrganization(dto.organization());
                p.addPartner(partner);
                i++;
            }
        }
    }

    private void ensureSlug(Project p, ProjectUpsertRequestDto req) {
        if (StringUtils.hasText(p.getSlug())) return;

        String base = firstNonBlank(req.title().de(), req.title().en(), req.title().uk());
        Predicate<String> exists = repo::existsBySlug;

        p.setSlug(SlugGenerator.generateUnique(base, exists));
    }

    private static String firstNonBlank(String... values) {
        if (values == null) return "item";
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return "item";
    }

    private ProjectItemDto toDto(Project p) {
        I18nText title = new I18nText(p.getTitleEn(), p.getTitleDe(), p.getTitleUk());

        I18nText subtitle = null;
        if (StringUtils.hasText(p.getSubtitleEn()) || StringUtils.hasText(p.getSubtitleDe()) || StringUtils.hasText(p.getSubtitleUk())) {
            subtitle = new I18nText(
                    safeText(p.getSubtitleEn()),
                    safeText(p.getSubtitleDe()),
                    safeText(p.getSubtitleUk())
            );
        }

        I18nText description = new I18nText(p.getDescriptionEn(), p.getDescriptionDe(), p.getDescriptionUk());

        List<String> gallery = new ArrayList<>();
        for (ProjectImage img : p.getImages()) {
            gallery.add(img.getUrl());
        }

        List<I18nText> goals = new ArrayList<>();
        for (ProjectGoal g : p.getGoals()) {
            goals.add(new I18nText(g.getEn(), g.getDe(), g.getUk()));
        }

        List<I18nText> activities = new ArrayList<>();
        for (ProjectActivity a : p.getActivities()) {
            activities.add(new I18nText(a.getEn(), a.getDe(), a.getUk()));
        }

        List<ProjectPartnerDto> partners = new ArrayList<>();
        for (ProjectPartner pp : p.getPartners()) {
            partners.add(new ProjectPartnerDto(
                    new I18nText(pp.getCountryEn(), pp.getCountryDe(), pp.getCountryUk()),
                    pp.getOrganization()
            ));
        }

        return new ProjectItemDto(
                p.getPublicId() == null ? null : p.getPublicId().toString(),
                p.getSlug(),
                title,
                subtitle,
                p.getCoverImage(),
                gallery,
                p.getStartDate(),
                p.getEndDate(),
                description,
                goals,
                activities,
                partners,
                p.isPublished(),
                p.getSortOrder(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

    private static String safeTrim(String s) {
        if (!StringUtils.hasText(s)) return null;
        return s.trim();
    }

    private static String safeText(String s) {
        return s == null ? "" : s;
    }
}