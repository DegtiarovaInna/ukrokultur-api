package de.ukrokultur.ukrokultur_api.common.dto.projects;


import de.ukrokultur.ukrokultur_api.common.dto.I18nText;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProjectPartnerDto(
        @NotNull @Valid I18nText country,
        @NotBlank String organization
) {}