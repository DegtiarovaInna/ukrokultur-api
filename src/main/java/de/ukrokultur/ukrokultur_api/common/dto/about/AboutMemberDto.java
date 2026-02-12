package de.ukrokultur.ukrokultur_api.common.dto.about;

import de.ukrokultur.ukrokultur_api.common.dto.I18nText;

public record AboutMemberDto(
        String id,
        String name,
        String image,
        int order,
        boolean published,
        I18nText role,
        I18nText biography,
        String instagramUrl
) {}
