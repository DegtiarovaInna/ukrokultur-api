package de.ukrokultur.ukrokultur_api.media;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "supabase")
public record SupabaseProperties(
        String url,
        String serviceRoleKey,
        String bucket,
        String publicBaseUrl
) {}
