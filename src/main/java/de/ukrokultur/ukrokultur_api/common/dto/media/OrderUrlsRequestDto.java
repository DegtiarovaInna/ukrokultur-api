package de.ukrokultur.ukrokultur_api.common.dto.media;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record OrderUrlsRequestDto(
        @NotNull List<@NotNull String> urls
) {}