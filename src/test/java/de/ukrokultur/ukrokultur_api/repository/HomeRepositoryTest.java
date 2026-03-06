package de.ukrokultur.ukrokultur_api.repository;


import de.ukrokultur.ukrokultur_api.home.HomeWorkFieldItem;
import de.ukrokultur.ukrokultur_api.home.HomeWorkFieldRepository;
import de.ukrokultur.ukrokultur_api.support.AbstractJpaTcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;


class HomeRepositoryTest extends AbstractJpaTcTest{

    @Autowired
    private HomeWorkFieldRepository repo;

    @Test
    void findAllByOrderBySortOrderAsc_orders() {
        HomeWorkFieldItem a = new HomeWorkFieldItem();
        a.setSlug("a");
        a.setSortOrder(2);
        a.setPublished(true);
        a.setTitleEn("t"); a.setTitleDe("t"); a.setTitleUk("t");
        a.setDescriptionEn("d"); a.setDescriptionDe("d"); a.setDescriptionUk("d");
        repo.save(a);

        HomeWorkFieldItem b = new HomeWorkFieldItem();
        b.setSlug("b");
        b.setSortOrder(1);
        b.setPublished(true);
        b.setTitleEn("t"); b.setTitleDe("t"); b.setTitleUk("t");
        b.setDescriptionEn("d"); b.setDescriptionDe("d"); b.setDescriptionUk("d");
        repo.saveAndFlush(b);

        var list = repo.findAllByOrderBySortOrderAsc();
        assertThat(list).extracting(HomeWorkFieldItem::getSlug).containsExactly("b", "a");
    }
}