package de.ukrokultur.ukrokultur_api.common.dto.about;

import de.ukrokultur.ukrokultur_api.common.dto.I18nText;

public record AboutIntroDto(
        String image,
        I18nText title,
        I18nText text,
        boolean published
) {}
