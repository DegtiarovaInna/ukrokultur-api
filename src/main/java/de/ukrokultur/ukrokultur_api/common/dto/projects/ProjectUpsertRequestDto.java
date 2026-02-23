package de.ukrokultur.ukrokultur_api.common.dto.projects;

import de.ukrokultur.ukrokultur_api.common.dto.I18nText;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;


import java.util.List;

public record ProjectUpsertRequestDto(
        String slug,

        @NotNull @Valid I18nText title,
        @Valid I18nText subtitle,

        String coverImage,
        List<String> galleryImages,

        LocalDate startDate,
        LocalDate endDate,

        @NotNull @Valid I18nText description,

        List<@Valid I18nText> goals,
        List<@Valid I18nText> activities,
        List<@Valid ProjectPartnerDto> partners,

        @NotNull Boolean published,
        Integer order
) {}
