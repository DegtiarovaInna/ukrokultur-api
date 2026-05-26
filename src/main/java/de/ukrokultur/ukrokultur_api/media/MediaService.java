package de.ukrokultur.ukrokultur_api.media;

import de.ukrokultur.ukrokultur_api.common.dto.media.UploadResponseDto;
import de.ukrokultur.ukrokultur_api.common.error.ErrorCode;
import de.ukrokultur.ukrokultur_api.common.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
public class MediaService {

    private static final Logger log = LoggerFactory.getLogger(MediaService.class);

    private static final long MAX_IMAGE_SIZE_BYTES = 10L * 1024 * 1024; // 10 MB
    private static final long MAX_VIDEO_SIZE_BYTES = 25L * 1024 * 1024; // 25 MB

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

        validateFile(file);

        String objectPath = buildObjectPath(file.getOriginalFilename(), safeFolder);

        putToStorage(objectPath, file);

        String publicUrl = buildPublicUrl(objectPath);

        return new UploadResponseDto(objectPath, publicUrl);
    }

    public List<UploadResponseDto> uploadMany(List<MultipartFile> files, String folder) {
        ensureConfigured();

        if (files == null || files.isEmpty()) {
            throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "files are required");
        }

        List<UploadResponseDto> out = new ArrayList<>();

        for (MultipartFile f : files) {
            if (f == null || f.isEmpty()) {
                continue;
            }

            out.add(upload(f, folder));
        }

        if (out.isEmpty()) {
            throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "files are required");
        }

        return out;
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
        } catch (Exception ex) {
            log.error("Failed to delete file from Supabase Storage. objectPath={}", objectPath, ex);

            throw new ApiException(502, ErrorCode.INTERNAL_ERROR, "Failed to delete file from storage");
        }
    }

    public void deleteQuietly(String objectPath) {
        if (!StringUtils.hasText(objectPath)) {
            return;
        }

        try {
            delete(objectPath);
        } catch (Exception ex) {
            log.warn("Storage delete failed (ignored). objectPath={}", objectPath, ex);
        }
    }

    public void deleteByPublicUrlQuietly(String publicUrl) {
        String objectPath = extractObjectPathFromPublicUrl(publicUrl);

        if (!StringUtils.hasText(objectPath)) {
            return;
        }

        deleteQuietly(objectPath);
    }

    public void deleteManyByPublicUrlsQuietly(Collection<String> publicUrls) {
        if (publicUrls == null || publicUrls.isEmpty()) {
            return;
        }

        for (String url : publicUrls) {
            deleteByPublicUrlQuietly(url);
        }
    }

    public boolean isManagedPublicUrl(String publicUrl) {
        return StringUtils.hasText(extractObjectPathFromPublicUrl(publicUrl));
    }

    public String extractObjectPathFromPublicUrl(String publicUrl) {
        if (!StringUtils.hasText(publicUrl)) {
            return null;
        }

        ensureConfigured();

        String url = publicUrl.trim();

        if (StringUtils.hasText(props.publicBaseUrl())) {
            String base = props.publicBaseUrl().trim();

            if (url.startsWith(base)) {
                String tail = url.substring(base.length());

                if (tail.startsWith("/")) {
                    tail = tail.substring(1);
                }

                tail = stripQuery(tail);
                tail = tail.isBlank() ? null : decodePath(tail);

                return tail;
            }
        }

        String prefix = props.url().trim() + "/storage/v1/object/public/" + props.bucket() + "/";

        if (url.startsWith(prefix)) {
            String tail = url.substring(prefix.length());

            tail = stripQuery(tail);
            tail = tail.isBlank() ? null : decodePath(tail);

            return tail;
        }

        try {
            URI uri = URI.create(url);
            String noQuery = uri.toString().split("\\?")[0];

            if (StringUtils.hasText(props.publicBaseUrl())) {
                String base = props.publicBaseUrl().trim();

                if (noQuery.startsWith(base)) {
                    String tail = noQuery.substring(base.length());

                    if (tail.startsWith("/")) {
                        tail = tail.substring(1);
                    }

                    tail = tail.isBlank() ? null : decodePath(tail);

                    return tail;
                }
            }

            if (noQuery.startsWith(prefix)) {
                String tail = noQuery.substring(prefix.length());

                tail = tail.isBlank() ? null : decodePath(tail);

                return tail;
            }
        } catch (Exception ignore) {
        }

        return null;
    }

    private static String stripQuery(String s) {
        if (s == null) {
            return null;
        }

        int idx = s.indexOf('?');

        return idx >= 0 ? s.substring(0, idx) : s;
    }

    private static String decodePath(String encodedPath) {
        if (!StringUtils.hasText(encodedPath)) {
            return null;
        }

        String[] parts = encodedPath.split("/");
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                sb.append("/");
            }

            sb.append(URLDecoder.decode(parts[i], StandardCharsets.UTF_8));
        }

        return sb.toString();
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
        } catch (IOException ex) {
            log.error(
                    "Failed to read uploaded file. objectPath={}, originalFilename={}",
                    objectPath,
                    file == null ? null : file.getOriginalFilename(),
                    ex
            );

            throw new ApiException(500, ErrorCode.INTERNAL_ERROR, "Failed to read uploaded file");
        } catch (Exception ex) {
            log.error(
                    "Failed to upload file to Supabase Storage. objectPath={}, originalFilename={}",
                    objectPath,
                    file == null ? null : file.getOriginalFilename(),
                    ex
            );

            throw new ApiException(502, ErrorCode.INTERNAL_ERROR, "Failed to upload file to storage");
        }
    }

    private void validateFile(MultipartFile file) {
        String contentType = file.getContentType();

        if (!StringUtils.hasText(contentType)) {
            throw new ApiException(
                    400,
                    ErrorCode.VALIDATION_ERROR,
                    "File content type is required"
            );
        }

        String normalizedContentType = contentType.toLowerCase();

        if (isImage(normalizedContentType)) {
            validateSize(file, MAX_IMAGE_SIZE_BYTES, "Image must be <= 10 MB");
            return;
        }

        if (isVideo(normalizedContentType)) {
            validateSize(file, MAX_VIDEO_SIZE_BYTES, "Video must be <= 25 MB");
            return;
        }

        throw new ApiException(
                400,
                ErrorCode.VALIDATION_ERROR,
                "Unsupported file type. Allowed images: JPEG, PNG, WEBP, GIF. Allowed videos: MP4, WEBM, MOV"
        );
    }

    private void validateSize(MultipartFile file, long maxSizeBytes, String message) {
        if (file.getSize() > maxSizeBytes) {
            throw new ApiException(400, ErrorCode.VALIDATION_ERROR, message);
        }
    }

    private boolean isImage(String contentType) {
        return contentType.equals("image/jpeg")
                || contentType.equals("image/png")
                || contentType.equals("image/webp")
                || contentType.equals("image/gif");
    }

    private boolean isVideo(String contentType) {
        return contentType.equals("video/mp4")
                || contentType.equals("video/webm")
                || contentType.equals("video/quicktime");
    }

    private String normalizeFolder(String folder) {
        String f = folder == null ? "" : folder.trim().toLowerCase();

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
        String safeName = buildSafeStorageFilename(originalFileName);
        String ts = String.valueOf(Instant.now().toEpochMilli());

        return folder + "/" + ts + "_" + safeName;
    }

    private String buildSafeStorageFilename(String originalFileName) {
        String extension = extractSafeExtension(originalFileName);
        String randomName = UUID.randomUUID().toString().replace("-", "");

        return randomName + extension;
    }

    private String extractSafeExtension(String originalFileName) {
        if (!StringUtils.hasText(originalFileName)) {
            return "";
        }

        String name = originalFileName.trim();
        name = name.replace("\\", "/");

        int slashIndex = name.lastIndexOf("/");
        if (slashIndex >= 0) {
            name = name.substring(slashIndex + 1);
        }

        int dotIndex = name.lastIndexOf(".");
        if (dotIndex < 0 || dotIndex == name.length() - 1) {
            return "";
        }

        String extension = name.substring(dotIndex + 1).toLowerCase();

        if (!extension.matches("[a-z0-9]{1,10}")) {
            return "";
        }

        return "." + extension;
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
            if (i > 0) {
                sb.append("/");
            }

            sb.append(URLEncoder.encode(parts[i], StandardCharsets.UTF_8));
        }

        return sb.toString();
    }
}