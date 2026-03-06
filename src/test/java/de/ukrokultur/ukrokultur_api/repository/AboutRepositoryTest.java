package de.ukrokultur.ukrokultur_api.repository;

import de.ukrokultur.ukrokultur_api.about.AboutMember;
import de.ukrokultur.ukrokultur_api.about.AboutMemberRepository;
import de.ukrokultur.ukrokultur_api.about.I18nEmbeddable;
import de.ukrokultur.ukrokultur_api.support.AbstractJpaTcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;


class AboutRepositoryTest extends AbstractJpaTcTest{

    @Autowired
    AboutMemberRepository repo;

    @Test
    void findMaxSortOrder_defaultsToMinus1() {
        assertThat(repo.findMaxSortOrder()).isEqualTo(-1);
    }

    @Test
    void existsBySlug_works() {
        AboutMember m = new AboutMember();
        m.setSlug("s1");
        m.setName("Name");
        m.setSortOrder(0);
        m.setPublished(true);
        m.setInstagramUrl(null);
        m.setRole(new I18nEmbeddable("r", "r", "r"));
        m.setBiography(new I18nEmbeddable("b", "b", "b"));

        repo.saveAndFlush(m);

        assertThat(repo.existsBySlug("s1")).isTrue();
        assertThat(repo.existsBySlug("s2")).isFalse();
    }
}