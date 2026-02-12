package de.ukrokultur.ukrokultur_api.common.dto.news;


import java.util.List;

public record NewsPageResultDto(
        List<NewsItemDto> items,
        int page,
        int pageSize,
        long totalItems,
        int totalPages
) {}
