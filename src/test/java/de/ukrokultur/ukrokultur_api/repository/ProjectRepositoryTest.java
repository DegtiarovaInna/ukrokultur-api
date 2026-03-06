package de.ukrokultur.ukrokultur_api.repository;

import de.ukrokultur.ukrokultur_api.projects.Project;
import de.ukrokultur.ukrokultur_api.projects.ProjectRepository;
import de.ukrokultur.ukrokultur_api.support.AbstractJpaTcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.PageRequest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


class ProjectRepositoryTest extends AbstractJpaTcTest{

    @Autowired
    private ProjectRepository repo;

    @Test
    void findMaxSortOrder_defaultsToMinus1() {
        assertThat(repo.findMaxSortOrder()).isEqualTo(-1);
    }

    @Test
    void findPageOrdered_sortsBySortOrderAsc_thenCreatedAtDesc() {
        Project p1 = new Project();
        p1.setPublicId(UUID.randomUUID());
        p1.setSlug("p1");
        p1.setPublished(true);
        p1.setSortOrder(5);
        p1.setTitleEn("a"); p1.setTitleDe("a"); p1.setTitleUk("a");
        p1.setDescriptionEn("d"); p1.setDescriptionDe("d"); p1.setDescriptionUk("d");
        repo.save(p1);

        Project p2 = new Project();
        p2.setPublicId(UUID.randomUUID());
        p2.setSlug("p2");
        p2.setPublished(true);
        p2.setSortOrder(1);
        p2.setTitleEn("a"); p2.setTitleDe("a"); p2.setTitleUk("a");
        p2.setDescriptionEn("d"); p2.setDescriptionDe("d"); p2.setDescriptionUk("d");
        repo.saveAndFlush(p2);

        var page = repo.findPageOrdered(true, PageRequest.of(0, 10));
        assertThat(page.getContent()).extracting(Project::getSlug).containsExactly("p2", "p1");
    }
}