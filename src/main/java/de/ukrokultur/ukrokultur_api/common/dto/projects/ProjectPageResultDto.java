package de.ukrokultur.ukrokultur_api.common.dto.projects;

import java.util.List;

public record ProjectPageResultDto(
        List<ProjectItemDto> items,
        int page,
        int pageSize,
        long totalItems,
        int totalPages
) {}