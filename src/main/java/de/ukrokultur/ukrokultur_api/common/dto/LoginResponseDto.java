package de.ukrokultur.ukrokultur_api.common.dto;



public record LoginResponseDto(
        String tokenType,
        String accessToken
) {
    public static LoginResponseDto bearer(String token) {
        return new LoginResponseDto("Bearer", token);
    }
}
