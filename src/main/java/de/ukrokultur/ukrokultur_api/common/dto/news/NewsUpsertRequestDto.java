package de.ukrokultur.ukrokultur_api.common.dto.news;

import de.ukrokultur.ukrokultur_api.common.dto.I18nText;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record NewsUpsertRequestDto(
        String slug,
        @NotNull LocalDate newsDate,
        LocalDate eventDate,

        @NotNull  @Valid I18nText title,
        @NotNull @Valid I18nText content,

        List<String> images,
        List<NewsVideoDto> videos,

        @NotNull Boolean published
) {}
