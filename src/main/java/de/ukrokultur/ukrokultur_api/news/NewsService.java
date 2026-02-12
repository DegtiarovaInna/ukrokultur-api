package de.ukrokultur.ukrokultur_api.news;

import de.ukrokultur.ukrokultur_api.common.dto.I18nText;
import de.ukrokultur.ukrokultur_api.common.dto.news.NewsItemDto;
import de.ukrokultur.ukrokultur_api.common.dto.news.NewsPageResultDto;
import de.ukrokultur.ukrokultur_api.common.dto.news.NewsUpsertRequestDto;
import de.ukrokultur.ukrokultur_api.common.dto.news.NewsVideoDto;
import de.ukrokultur.ukrokultur_api.common.error.ErrorCode;
import de.ukrokultur.ukrokultur_api.common.exception.ApiException;
import de.ukrokultur.ukrokultur_api.common.exception.NotFoundException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.*;

@Service
@Transactional
public class NewsService {

    private final NewsRepository newsRepository;

    public NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
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

    public NewsItemDto create(NewsUpsertRequestDto req) {
        String slug = req.id().trim();
        if (newsRepository.existsBySlug(slug)) {
            throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "News id already exists: " + slug);
        }

        News n = new News();
        applyUpsert(n, req);
        return toItemDto(newsRepository.save(n));
    }

    public NewsItemDto update(String slug, NewsUpsertRequestDto req) {
        News n = newsRepository.findBySlug(slug)
                .orElseThrow(() -> NotFoundException.of("News", slug));

        if (!slug.equals(req.id().trim())) {
            throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "id cannot be changed");
        }


        applyUpsert(n, req);

        return toItemDto(n);
    }

    public void delete(String slug) {
        News n = newsRepository.findBySlug(slug)
                .orElseThrow(() -> NotFoundException.of("News", slug));
        newsRepository.delete(n);
    }

    private void applyUpsert(News n, NewsUpsertRequestDto req) {
        n.setSlug(req.id().trim());
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
