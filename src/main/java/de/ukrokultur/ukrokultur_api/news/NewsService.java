package de.ukrokultur.ukrokultur_api.news;

import de.ukrokultur.ukrokultur_api.common.dto.I18nText;
import de.ukrokultur.ukrokultur_api.common.dto.news.NewsItemDto;
import de.ukrokultur.ukrokultur_api.common.dto.news.NewsPageResultDto;
import de.ukrokultur.ukrokultur_api.common.dto.news.NewsUpsertRequestDto;
import de.ukrokultur.ukrokultur_api.common.dto.news.NewsVideoDto;
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

import java.time.ZoneOffset;
import java.util.*;
import java.util.UUID;
import java.util.function.Predicate;

@Service
@Transactional
public class NewsService {

    private final NewsRepository newsRepository;
    private final MediaService mediaService;

    public NewsService(NewsRepository newsRepository, MediaService mediaService) {
        this.newsRepository = newsRepository;
        this.mediaService = mediaService;
    }

    @Transactional(readOnly = true)
    public NewsPageResultDto getPage(int page1Based, int pageSize, boolean publishedOnly) {
        int safeSize = Math.min(Math.max(pageSize, 1), 50);
        int page0 = Math.max(page1Based - 1, 0);

        Pageable pageable = PageRequest.of(page0, safeSize);
        Page<News> p = newsRepository.findPageOrdered(publishedOnly, pageable);

        List<NewsItemDto> items = p.getContent().stream().map(this::toItemDto).toList();

        return new NewsPageResultDto(
                items,
                page1Based,
                safeSize,
                p.getTotalElements(),
                p.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public NewsItemDto getByIdAdmin(UUID publicId) {
        News n = newsRepository.findByPublicId(publicId)
                .orElseThrow(() -> NotFoundException.of("News", publicId));
        return toItemDto(n);
    }

    public NewsItemDto createMultipart(NewsUpsertRequestDto data, List<MultipartFile> images, MultipartFile video) {
        List<String> uploadedImages = List.of();
        String uploadedVideo = null;

        try {
            List<String> urls = data.images();

            if (images != null && !images.isEmpty()) {
                uploadedImages = mediaService.uploadMany(images, "news")
                        .stream().map(x -> x.publicUrl()).toList();
                urls = uploadedImages;
            }

            List<NewsVideoDto> videos = data.videos();
            if (video != null && !video.isEmpty()) {
                uploadedVideo = mediaService.upload(video, "news").publicUrl();

                String label = null;
                if (data.videos() != null && !data.videos().isEmpty()) {
                    label = data.videos().get(0).label();
                }

                videos = List.of(new NewsVideoDto("mp4", uploadedVideo, label));
            }

            NewsUpsertRequestDto req = new NewsUpsertRequestDto(
                    data.slug(),
                    data.newsDate(),
                    data.eventDate(),
                    data.title(),
                    data.content(),
                    urls,
                    videos,
                    data.published()
            );

            return create(req);

        } catch (RuntimeException ex) {
            mediaService.deleteManyByPublicUrlsQuietly(uploadedImages);
            mediaService.deleteByPublicUrlQuietly(uploadedVideo);
            throw ex;
        }
    }

    public NewsItemDto updateMultipart(UUID publicId, NewsUpsertRequestDto data, List<MultipartFile> images, MultipartFile video) {
        News existing = newsRepository.findByPublicId(publicId)
                .orElseThrow(() -> NotFoundException.of("News", publicId));

        List<String> oldUrls = extractImageUrls(existing);
        String oldVideoUrl = existing.getVideoUrl();

        List<String> uploadedImages = List.of();
        String uploadedVideo = null;

        try {
            List<String> urls = (data.images() != null ? data.images() : oldUrls);

            if (images != null && !images.isEmpty()) {
                uploadedImages = mediaService.uploadMany(images, "news")
                        .stream().map(x -> x.publicUrl()).toList();
                urls = uploadedImages;
            }


            List<NewsVideoDto> videos;
            if (video != null && !video.isEmpty()) {
                uploadedVideo = mediaService.upload(video, "news").publicUrl();

                String label = null;
                if (data.videos() != null && !data.videos().isEmpty()) {
                    label = data.videos().get(0).label();
                }

                videos = List.of(new NewsVideoDto("mp4", uploadedVideo, label));
            } else if (data.videos() == null) {
                if (StringUtils.hasText(oldVideoUrl)) {
                    videos = List.of(new NewsVideoDto(
                            existing.getVideoType() == null ? "external" : existing.getVideoType(),
                            oldVideoUrl,
                            existing.getVideoLabel()
                    ));
                } else {
                    videos = null;
                }
            } else {
                videos = data.videos();
            }

            NewsUpsertRequestDto req = new NewsUpsertRequestDto(
                    data.slug(),
                    data.newsDate(),
                    data.eventDate(),
                    data.title(),
                    data.content(),
                    urls,
                    videos,
                    data.published()
            );

            NewsItemDto out = update(publicId, req);

            if (!uploadedImages.isEmpty()) {
                mediaService.deleteManyByPublicUrlsQuietly(oldUrls);
            }


            String newVideoUrl = extractFirstVideoUrl(videos);
            if (shouldDeleteManagedVideo(oldVideoUrl, newVideoUrl)) {
                mediaService.deleteByPublicUrlQuietly(oldVideoUrl);
            }

            return out;

        } catch (RuntimeException ex) {
            mediaService.deleteManyByPublicUrlsQuietly(uploadedImages);
            mediaService.deleteByPublicUrlQuietly(uploadedVideo);
            throw ex;
        }
    }

    public NewsItemDto create(NewsUpsertRequestDto req) {
        News n = new News();

        applyUpsert(n, req);

        String requested = safeTrim(req.slug());
        if (StringUtils.hasText(requested)) {
            String normalized = SlugGenerator.slugify(requested);
            if (!StringUtils.hasText(normalized)) {
                throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "Slug is invalid");
            }
            n.setSlug(normalized);
        } else {
            n.setSlug(null);
        }

        ensureSlug(n, req);

        if (StringUtils.hasText(n.getSlug()) && newsRepository.existsBySlug(n.getSlug())) {
            throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "Slug already exists: " + n.getSlug());
        }

        return toItemDto(newsRepository.save(n));
    }

    public NewsItemDto update(UUID publicId, NewsUpsertRequestDto req) {
        News n = newsRepository.findByPublicId(publicId)
                .orElseThrow(() -> NotFoundException.of("News", publicId));

        String requested = safeTrim(req.slug());
        if (StringUtils.hasText(requested) && !Objects.equals(requested, n.getSlug())) {
            String normalized = SlugGenerator.slugify(requested);
            if (!StringUtils.hasText(normalized)) {
                throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "Slug is invalid");
            }
            if (newsRepository.existsBySlug(normalized)) {
                throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "Slug already exists: " + normalized);
            }
            n.setSlug(normalized);
        }

        applyUpsert(n, req);

        if (!StringUtils.hasText(n.getSlug())) {
            ensureSlug(n, req);
        }

        return toItemDto(n);
    }

    public void delete(UUID publicId) {
        News n = newsRepository.findByPublicId(publicId)
                .orElseThrow(() -> NotFoundException.of("News", publicId));

        List<String> urls = extractImageUrls(n);
        String videoUrl = n.getVideoUrl();

        newsRepository.delete(n);

        mediaService.deleteManyByPublicUrlsQuietly(urls);
        if (mediaService.isManagedPublicUrl(videoUrl)) {
            mediaService.deleteByPublicUrlQuietly(videoUrl);
        }
    }

    public NewsItemDto addImages(UUID publicId, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "images are required");
        }

        List<String> uploaded = List.of();

        try {
            News n = newsRepository.findByPublicId(publicId)
                    .orElseThrow(() -> NotFoundException.of("News", publicId));

            uploaded = mediaService.uploadMany(images, "news")
                    .stream().map(x -> x.publicUrl()).toList();

            int startOrder = n.getImages().size();

            for (String url : uploaded) {
                NewsImage img = new NewsImage();
                img.setUrl(url);
                img.setSortOrder(startOrder++);
                img.setCover(false);
                n.addImage(img);
            }

            normalizeNewsImages(n);
            return toItemDto(n);

        } catch (RuntimeException ex) {
            mediaService.deleteManyByPublicUrlsQuietly(uploaded);
            throw ex;
        }
    }

    public NewsItemDto deleteOneImage(UUID publicId, String url) {
        if (!StringUtils.hasText(url)) {
            throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "url is required");
        }

        News n = newsRepository.findByPublicId(publicId)
                .orElseThrow(() -> NotFoundException.of("News", publicId));

        boolean removed = n.getImages().removeIf(img -> url.equals(img.getUrl()));
        if (!removed) {
            throw new ApiException(404, ErrorCode.NOT_FOUND, "Image not found in news gallery");
        }

        normalizeNewsImages(n);

        mediaService.deleteByPublicUrlQuietly(url);

        return toItemDto(n);
    }

    public NewsItemDto reorderImages(UUID publicId, List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "urls are required");
        }

        News n = newsRepository.findByPublicId(publicId)
                .orElseThrow(() -> NotFoundException.of("News", publicId));

        Map<String, NewsImage> byUrl = new LinkedHashMap<>();
        for (NewsImage img : n.getImages()) {
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

        List<NewsImage> ordered = new ArrayList<>();
        for (int i = 0; i < urls.size(); i++) {
            NewsImage img = byUrl.get(urls.get(i));
            img.setSortOrder(i);
            img.setCover(i == 0);
            ordered.add(img);
        }

        n.getImages().clear();
        for (NewsImage img : ordered) {
            n.addImage(img);
        }

        return toItemDto(n);
    }

    private void normalizeNewsImages(News n) {
        n.getImages().sort(Comparator.comparingInt(NewsImage::getSortOrder));
        for (int i = 0; i < n.getImages().size(); i++) {
            NewsImage img = n.getImages().get(i);
            img.setSortOrder(i);
            img.setCover(i == 0);
        }
    }

    private List<String> extractImageUrls(News n) {
        List<String> urls = new ArrayList<>();
        for (NewsImage img : n.getImages()) {
            if (img != null && StringUtils.hasText(img.getUrl())) {
                urls.add(img.getUrl());
            }
        }
        return urls;
    }


    private String extractFirstVideoUrl(List<NewsVideoDto> videos) {
        if (videos == null || videos.isEmpty()) return null;
        NewsVideoDto first = videos.get(0);
        return first == null ? null : first.url();
    }


    private boolean shouldDeleteManagedVideo(String oldVideoUrl, String newVideoUrl) {
        if (!StringUtils.hasText(oldVideoUrl)) return false;
        if (!mediaService.isManagedPublicUrl(oldVideoUrl)) return false;
        return !Objects.equals(oldVideoUrl, newVideoUrl);
    }

    private static String safeTrim(String s) {
        if (!StringUtils.hasText(s)) return null;
        return s.trim();
    }

    private void ensureSlug(News n, NewsUpsertRequestDto req) {
        if (StringUtils.hasText(n.getSlug())) return;

        String base = firstNonBlank(req.title().de(), req.title().en(), req.title().uk());
        Predicate<String> exists = newsRepository::existsBySlug;

        n.setSlug(SlugGenerator.generateUnique(base, exists));
    }

    private static String firstNonBlank(String... values) {
        if (values == null) return "item";
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return "item";
    }

    private void applyUpsert(News n, NewsUpsertRequestDto req) {
        n.setPublished(Boolean.TRUE.equals(req.published()));
        n.setEventDate(req.eventDate());

        n.setPublishedAt(req.newsDate().atStartOfDay().atOffset(ZoneOffset.UTC));

        if (req.videos() != null && !req.videos().isEmpty()) {
            NewsVideoDto v = req.videos().get(0);
            n.setVideoType(v.type());
            n.setVideoUrl(v.url());
            n.setVideoLabel(v.label());
        } else {
            n.setVideoType(null);
            n.setVideoUrl(null);
            n.setVideoLabel(null);
        }

        upsertTranslation(n, "en", req.title().en(), req.content().en());
        upsertTranslation(n, "de", req.title().de(), req.content().de());
        upsertTranslation(n, "uk", req.title().uk(), req.content().uk());

        n.clearImages();
        if (req.images() != null && !req.images().isEmpty()) {
            int order = 0;
            for (String url : req.images()) {
                if (!StringUtils.hasText(url)) continue;
                NewsImage img = new NewsImage();
                img.setUrl(url);
                img.setSortOrder(order);
                img.setCover(order == 0);
                n.addImage(img);
                order++;
            }
        }
    }

    private void upsertTranslation(News n, String lang, String title, String text) {
        NewsTranslation existing = null;
        for (NewsTranslation t : n.getTranslations()) {
            if (lang.equals(t.getLang())) {
                existing = t;
                break;
            }
        }

        if (existing == null) {
            NewsTranslation nt = new NewsTranslation();
            nt.setLang(lang);
            nt.setTitle(title);
            nt.setText(text);
            n.addTranslation(nt);
        } else {
            existing.setTitle(title);
            existing.setText(text);
        }
    }

    private NewsItemDto toItemDto(News n) {
        String enTitle = null, deTitle = null, ukTitle = null;
        String enText = null, deText = null, ukText = null;

        for (NewsTranslation t : n.getTranslations()) {
            switch (t.getLang()) {
                case "en" -> { enTitle = t.getTitle(); enText = t.getText(); }
                case "de" -> { deTitle = t.getTitle(); deText = t.getText(); }
                case "uk" -> { ukTitle = t.getTitle(); ukText = t.getText(); }
            }
        }

        I18nText title = new I18nText(enTitle, deTitle, ukTitle);
        I18nText content = new I18nText(enText, deText, ukText);

        List<String> images = new ArrayList<>();
        for (NewsImage img : n.getImages()) {
            images.add(img.getUrl());
        }

        List<NewsVideoDto> videos = new ArrayList<>();
        if (n.getVideoUrl() != null && !n.getVideoUrl().isBlank()) {
            videos.add(new NewsVideoDto(
                    n.getVideoType() == null ? "external" : n.getVideoType(),
                    n.getVideoUrl(),
                    n.getVideoLabel()
            ));
        }

        return new NewsItemDto(
                n.getPublicId() == null ? null : n.getPublicId().toString(),
                n.getSlug(),
                n.getPublishedAt() == null ? null : n.getPublishedAt().toLocalDate(),
                n.getEventDate(),
                title,
                content,
                images,
                videos,
                n.isPublished(),
                n.getCreatedAt(),
                n.getUpdatedAt()
        );
    }
}