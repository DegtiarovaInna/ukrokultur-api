package de.ukrokultur.ukrokultur_api.media;

import de.ukrokultur.ukrokultur_api.common.dto.UploadResponseDto;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Tag(name = "Media (Admin)", description = "Admin media upload/delete (Supabase Storage).")
@SecurityRequirement(name = de.ukrokultur.ukrokultur_api.config.OpenApiConfig.BEARER_SCHEME_NAME)
@RestController
@RequestMapping("/admin/media")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }
    @Operation(summary = "Upload file", description = "Uploads a file to Supabase Storage and returns objectPath + publicUrl.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = de.ukrokultur.ukrokultur_api.common.error.ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = de.ukrokultur.ukrokultur_api.common.error.ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = de.ukrokultur.ukrokultur_api.common.error.ApiError.class))),
            @ApiResponse(responseCode = "502", description = "Storage error", content = @Content(schema = @Schema(implementation = de.ukrokultur.ukrokultur_api.common.error.ApiError.class)))
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponseDto upload(@RequestPart("file") MultipartFile file) {
        return mediaService.upload(file);
    }
    @Operation(
            summary = "Upload file to folder",
            description = "Uploads a file to Supabase Storage into '{folder}/' and returns objectPath + publicUrl. " +
                    "Allowed folders: news, projects, about, home, pages."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = de.ukrokultur.ukrokultur_api.common.error.ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = de.ukrokultur.ukrokultur_api.common.error.ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = de.ukrokultur.ukrokultur_api.common.error.ApiError.class))),
            @ApiResponse(responseCode = "502", description = "Storage error",
                    content = @Content(schema = @Schema(implementation = de.ukrokultur.ukrokultur_api.common.error.ApiError.class)))
    })
    @PostMapping(value = "/upload/{folder}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponseDto uploadToFolder(
            @PathVariable("folder") String folder,
            @RequestPart("file") MultipartFile file
    ) {
        return mediaService.upload(file, folder);
    }

    @Operation(
            summary = "Upload many files to folder",
            description = "Uploads multiple files in one multipart request to '{folder}/'. Part name: files"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = de.ukrokultur.ukrokultur_api.common.error.ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = de.ukrokultur.ukrokultur_api.common.error.ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = de.ukrokultur.ukrokultur_api.common.error.ApiError.class))),
            @ApiResponse(responseCode = "502", description = "Storage error",
                    content = @Content(schema = @Schema(implementation = de.ukrokultur.ukrokultur_api.common.error.ApiError.class)))
    })
    @PostMapping(value = "/upload/batch/{folder}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<UploadResponseDto> uploadBatch(
            @PathVariable("folder") String folder,
            @RequestPart("files") List<MultipartFile> files
    ) {
        return mediaService.uploadMany(files, folder);
    }

    @Operation(summary = "Delete file", description = "Deletes a file from Supabase Storage by objectPath.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = de.ukrokultur.ukrokultur_api.common.error.ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = de.ukrokultur.ukrokultur_api.common.error.ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = de.ukrokultur.ukrokultur_api.common.error.ApiError.class)))
    })
    @DeleteMapping
    public void delete(@RequestParam("objectPath") String objectPath) {
        mediaService.delete(objectPath);
    }
}
