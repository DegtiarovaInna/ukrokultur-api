package de.ukrokultur.ukrokultur_api.common.dto.news;


public record NewsVideoDto(
        String type,   // instagram | facebook | youtube | mp4 | external
        String url,
        String label
) {}
