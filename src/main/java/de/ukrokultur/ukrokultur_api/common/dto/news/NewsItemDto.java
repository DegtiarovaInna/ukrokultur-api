package de.ukrokultur.ukrokultur_api.common.dto.news;


import de.ukrokultur.ukrokultur_api.common.dto.I18nText;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record NewsItemDto(
        String id,
        String slug,
        LocalDate newsDate,
        LocalDate eventDate,
        I18nText title,
        I18nText content,
        List<String> images,
        List<NewsVideoDto> videos,
        boolean published,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
