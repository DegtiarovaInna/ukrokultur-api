package de.ukrokultur.ukrokultur_api.projects;

import de.ukrokultur.ukrokultur_api.common.dto.I18nText;
import de.ukrokultur.ukrokultur_api.common.dto.projects.ProjectItemDto;
import de.ukrokultur.ukrokultur_api.common.dto.projects.ProjectUpsertRequestDto;
import de.ukrokultur.ukrokultur_api.common.error.ErrorCode;
import de.ukrokultur.ukrokultur_api.common.exception.ApiException;
import de.ukrokultur.ukrokultur_api.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Objects;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository repo;

    public ProjectService(ProjectRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<ProjectItemDto> getAll(boolean publishedOnly) {
        return repo.findAllOrdered(publishedOnly).stream().map(this::toDto).toList();
    }

    public ProjectItemDto create(ProjectUpsertRequestDto req) {
        Project p = new Project();

        applyUpsert(p, req);

        if (StringUtils.hasText(p.getSlug()) && repo.existsBySlug(p.getSlug())) {
            throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "Slug already exists: " + p.getSlug()); // CHANGED
        }

        return toDto(repo.save(p));
    }

    public ProjectItemDto update(UUID publicId, ProjectUpsertRequestDto req) {
        Project p = repo.findByPublicId(publicId).orElseThrow(() -> NotFoundException.of("Project", publicId));

        String newSlug = safeTrim(req.slug());
        if (StringUtils.hasText(newSlug) && !Objects.equals(newSlug, p.getSlug())) {
            if (repo.existsBySlug(newSlug)) {
                throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "Slug already exists: " + newSlug); // CHANGED
            }
        }

        applyUpsert(p, req);

        return toDto(p);
    }

    public void delete(UUID publicId) {
        Project p = repo.findByPublicId(publicId)
                .orElseThrow(() -> NotFoundException.of("Project", publicId));
        repo.delete(p);
    }

    private void applyUpsert(Project p, ProjectUpsertRequestDto req) {
        p.setSlug(safeTrim(req.slug()));
        p.setPublished(Boolean.TRUE.equals(req.published()));

        upsertTranslation(p, "en", req.title().en(), req.content().en());
        upsertTranslation(p, "de", req.title().de(), req.content().de());
        upsertTranslation(p, "uk", req.title().uk(), req.content().uk());

        p.clearImages();
        if (req.images() != null && !req.images().isEmpty()) {
            int order = 0;
            for (String url : req.images()) {
                ProjectImage img = new ProjectImage();
                img.setUrl(url);
                img.setSortOrder(order);
                img.setCover(order == 0);
                p.addImage(img);
                order++;
            }
        }
    }

    private void upsertTranslation(Project p, String lang, String title, String text) {
        ProjectTranslation existing = null;
        for (ProjectTranslation t : p.getTranslations()) {
            if (lang.equals(t.getLang())) {
                existing = t;
                break;
            }
        }

        if (existing == null) {
            ProjectTranslation nt = new ProjectTranslation();
            nt.setLang(lang);
            nt.setTitle(title);
            nt.setText(text);
            p.addTranslation(nt);
        } else {
            existing.setTitle(title);
            existing.setText(text);
        }
    }

    private ProjectItemDto toDto(Project p) {
        String enTitle = null, deTitle = null, ukTitle = null;
        String enText = null, deText = null, ukText = null;

        for (ProjectTranslation t : p.getTranslations()) {
            switch (t.getLang()) {
                case "en" -> { enTitle = t.getTitle(); enText = t.getText(); }
                case "de" -> { deTitle = t.getTitle(); deText = t.getText(); }
                case "uk" -> { ukTitle = t.getTitle(); ukText = t.getText(); }
            }
        }

        I18nText title = new I18nText(enTitle, deTitle, ukTitle);
        I18nText content = new I18nText(enText, deText, ukText);

        List<String> images = new ArrayList<>();
        for (ProjectImage img : p.getImages()) {
            images.add(img.getUrl());
        }

        return new ProjectItemDto(
                p.getPublicId() == null ? null : p.getPublicId().toString(),
                p.getSlug(),
                title,
                content,
                images,
                p.isPublished(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

    private static String safeTrim(String s) {
        if (!StringUtils.hasText(s)) return null;
        return s.trim();
    }
}
