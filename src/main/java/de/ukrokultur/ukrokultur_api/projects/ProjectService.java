package de.ukrokultur.ukrokultur_api.projects;

import de.ukrokultur.ukrokultur_api.common.dto.I18nText;
import de.ukrokultur.ukrokultur_api.common.dto.projects.ProjectItemDto;
import de.ukrokultur.ukrokultur_api.common.dto.projects.ProjectUpsertRequestDto;
import de.ukrokultur.ukrokultur_api.common.error.ErrorCode;
import de.ukrokultur.ukrokultur_api.common.exception.ApiException;
import de.ukrokultur.ukrokultur_api.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
        String slug = req.id().trim();
        if (repo.existsBySlug(slug)) {
            throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "Project id already exists: " + slug);
        }

        Project p = new Project();
        applyUpsert(p, req);
        return toDto(repo.save(p));
    }

    public ProjectItemDto update(String slug, ProjectUpsertRequestDto req) {
        Project p = repo.findBySlug(slug).orElseThrow(() -> NotFoundException.of("Project", slug));

        if (!slug.equals(req.id().trim())) {
            throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "id cannot be changed");
        }

        applyUpsert(p, req);

        return toDto(p);
    }

    public void delete(String slug) {
        Project p = repo.findBySlug(slug).orElseThrow(() -> NotFoundException.of("Project", slug));
        repo.delete(p);
    }

    private void applyUpsert(Project p, ProjectUpsertRequestDto req) {
        p.setSlug(req.id().trim());
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
                p.getSlug(),
                title,
                content,
                images,
                p.isPublished(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
