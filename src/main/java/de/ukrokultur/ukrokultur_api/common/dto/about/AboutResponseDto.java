package de.ukrokultur.ukrokultur_api.common.dto.about;


import java.time.OffsetDateTime;
import java.util.List;

public record AboutResponseDto(
        AboutIntroDto intro,
        List<AboutMemberDto> members,
        OffsetDateTime updatedAt
) {}
