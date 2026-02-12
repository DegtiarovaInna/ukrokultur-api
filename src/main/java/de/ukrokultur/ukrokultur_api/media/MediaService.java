package de.ukrokultur.ukrokultur_api.media;

import de.ukrokultur.ukrokultur_api.common.dto.UploadResponseDto;
import de.ukrokultur.ukrokultur_api.common.exception.ApiException;
import de.ukrokultur.ukrokultur_api.common.error.ErrorCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Service
public class MediaService {

    private final SupabaseProperties props;
    private final RestClient restClient;

    public MediaService(SupabaseProperties props, RestClient restClient) {
        this.props = props;
        this.restClient = restClient;
    }

    public UploadResponseDto upload(MultipartFile file) {
        return upload(file, "news");
    }

    public UploadResponseDto upload(MultipartFile file, String folder) {
        ensureConfigured();

        if (file == null || file.isEmpty()) {
            throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "File is required");
        }

        String safeFolder = normalizeFolder(folder);
        String objectPath = buildObjectPath(file.getOriginalFilename(), safeFolder);

        putToStorage(objectPath, file);

        String publicUrl = buildPublicUrl(objectPath);
        return new UploadResponseDto(objectPath, publicUrl);
    }

    public void delete(String objectPath) {
        ensureConfigured();

        if (!StringUtils.hasText(objectPath)) {
            throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "objectPath is required");
        }

        String deleteUrl = props.url() + "/storage/v1/object/" + props.bucket() + "/" + encodePath(objectPath);

        try {
            restClient.delete()
                    .uri(deleteUrl)
                    .header("Authorization", "Bearer " + props.serviceRoleKey())
                    .header("apikey", props.serviceRoleKey())
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            throw new ApiException(502, ErrorCode.INTERNAL_ERROR, "Failed to delete file from storage");
        }
    }

    private void putToStorage(String objectPath, MultipartFile file) {
        String uploadUrl = props.url() + "/storage/v1/object/" + props.bucket() + "/" + encodePath(objectPath);

        try {
            byte[] bytes = file.getBytes();
            String contentType = StringUtils.hasText(file.getContentType())
                    ? file.getContentType()
                    : MediaType.APPLICATION_OCTET_STREAM_VALUE;

            restClient.put()
                    .uri(uploadUrl)
                    .header("Authorization", "Bearer " + props.serviceRoleKey())
                    .header("apikey", props.serviceRoleKey())
                    .header("x-upsert", "true")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(bytes)
                    .retrieve()
                    .toBodilessEntity();

        } catch (IOException e) {
            throw new ApiException(500, ErrorCode.INTERNAL_ERROR, "Failed to read uploaded file");
        } catch (Exception e) {
            throw new ApiException(502, ErrorCode.INTERNAL_ERROR, "Failed to upload file to storage");
        }
    }

    private String normalizeFolder(String folder) {
        String f = (folder == null ? "" : folder.trim().toLowerCase());
        return switch (f) {
            case "news", "projects", "about", "home", "pages" -> f;
            default -> throw new ApiException(
                    400,
                    ErrorCode.VALIDATION_ERROR,
                    "Unsupported folder. Allowed: news, projects, about, home, pages"
            );
        };
    }

    private void ensureConfigured() {
        if (!StringUtils.hasText(props.url())
                || !StringUtils.hasText(props.serviceRoleKey())
                || !StringUtils.hasText(props.bucket())) {
            throw new ApiException(
                    500,
                    ErrorCode.INTERNAL_ERROR,
                    "Storage is not configured (SUPABASE_URL / SUPABASE_SERVICE_ROLE_KEY / SUPABASE_BUCKET)"
            );
        }
    }

    public String buildObjectPath(String originalFileName, String folder) {
        String safeName = sanitizeFilename(originalFileName);
        String ts = String.valueOf(Instant.now().toEpochMilli());
        return folder + "/" + ts + "_" + safeName;
    }

    private String sanitizeFilename(String originalFileName) {
        String name = (originalFileName == null ? "file" : originalFileName).trim();
        name = name.replaceAll("\\s+", "_");
        name = name.replace("\\", "_").replace("/", "_");
        name = name.replace("..", "_");
        if (!StringUtils.hasText(name)) return "file";
        return name;
    }

    public String buildPublicUrl(String objectPath) {
        if (StringUtils.hasText(props.publicBaseUrl())) {
            return props.publicBaseUrl() + "/" + encodePath(objectPath);
        }
        return props.url() + "/storage/v1/object/public/" + props.bucket() + "/" + encodePath(objectPath);
    }

    private String encodePath(String path) {
        String[] parts = path.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append("/");
            sb.append(URLEncoder.encode(parts[i], StandardCharsets.UTF_8));
        }
        return sb.toString();
    }
}
