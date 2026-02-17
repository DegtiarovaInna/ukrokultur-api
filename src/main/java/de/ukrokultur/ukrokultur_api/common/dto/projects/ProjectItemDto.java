package de.ukrokultur.ukrokultur_api.common.dto.projects;

import de.ukrokultur.ukrokultur_api.common.dto.I18nText;

import java.time.OffsetDateTime;
import java.util.List;

public record ProjectItemDto(
        String id,
        String slug,
        I18nText title,
        I18nText content,
        List<String> images,
        boolean published,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
