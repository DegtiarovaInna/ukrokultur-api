package de.ukrokultur.ukrokultur_api.about;

import de.ukrokultur.ukrokultur_api.common.dto.about.*;
import de.ukrokultur.ukrokultur_api.common.dto.I18nText;
import de.ukrokultur.ukrokultur_api.common.error.ErrorCode;
import de.ukrokultur.ukrokultur_api.common.exception.ApiException;
import de.ukrokultur.ukrokultur_api.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.util.List;

@Service
@Transactional
public class AboutService {

    private final AboutIntroRepository introRepository;
    private final AboutMemberRepository memberRepository;

    public AboutService(AboutIntroRepository introRepository, AboutMemberRepository memberRepository) {
        this.introRepository = introRepository;
        this.memberRepository = memberRepository;
    }


    public AboutResponseDto getPublic() {
        AboutIntro intro = getOrCreateIntro();
        List<AboutMember> members = memberRepository.findAllOrdered();

        AboutIntroDto introDto = toIntroDto(intro);
        List<AboutMemberDto> memberDtos = members.stream()
                .filter(AboutMember::isPublished)
                .map(this::toMemberDto)
                .toList();

        return new AboutResponseDto(introDto, memberDtos, intro.getUpdatedAt());
    }


    public AboutIntroDto getIntroAdmin() {
        return toIntroDto(getOrCreateIntro());
    }

    public AboutIntroDto updateIntro(AboutIntroUpsertRequestDto req) {
        AboutIntro intro = getOrCreateIntro();

        intro.setImage(req.image());
        intro.setTitle(toEmb(req.title()));
        intro.setText(toEmb(req.text()));
        intro.setPublished(Boolean.TRUE.equals(req.published()));

        introRepository.save(intro);
        return toIntroDto(intro);
    }

    @Transactional(readOnly = true)
    public List<AboutMemberDto> getMembersAdmin() {
        return memberRepository.findAllOrdered().stream().map(this::toMemberDto).toList();
    }

    public AboutMemberDto createMember(AboutMemberUpsertRequestDto req) {
        String slug = req.slug().trim();

        if (memberRepository.existsBySlug(slug)) {
            throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "Member slug already exists: " + slug);
        }

        AboutMember m = new AboutMember();
        apply(m, req, slug);
        return toMemberDto(memberRepository.save(m));
    }

    public AboutMemberDto updateMember(UUID id, AboutMemberUpsertRequestDto req) {
        AboutMember m = memberRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("AboutMember", id));

        String newSlug = req.slug().trim();

        if (!m.getSlug().equals(newSlug) && memberRepository.existsBySlug(newSlug)) {
            throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "Member slug already exists: " + newSlug);
        }

        apply(m, req, newSlug);
        return toMemberDto(memberRepository.save(m));
    }

    public void deleteMember(UUID id) {
        AboutMember m = memberRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("AboutMember", id));
        memberRepository.delete(m);
    }

    private AboutIntro getOrCreateIntro() {
        return introRepository.findAll().stream().findFirst().orElseGet(() -> {
            AboutIntro i = new AboutIntro();
            i.setImage(null);
            i.setTitle(new I18nEmbeddable("", "", ""));
            i.setText(new I18nEmbeddable("", "", ""));
            i.setPublished(true);
            introRepository.save(i);
            return i;
        });
    }

    private void apply(AboutMember m, AboutMemberUpsertRequestDto req, String slug) {
        m.setSlug(slug);
        m.setName(req.name().trim());
        m.setImage(req.image());
        m.setOrder(req.order());
        m.setPublished(Boolean.TRUE.equals(req.published()));
        m.setInstagramUrl(req.instagramUrl());
        m.setRole(toEmb(req.role()));
        m.setBiography(toEmb(req.biography()));
    }

    private AboutIntroDto toIntroDto(AboutIntro i) {
        return new AboutIntroDto(
                i.getImage(),
                toDto(i.getTitle()),
                toDto(i.getText()),
                i.isPublished()
        );
    }

    private AboutMemberDto toMemberDto(AboutMember m) {
        return new AboutMemberDto(
                m.getId() == null ? null : m.getId().toString(),
                m.getSlug(),
                m.getName(),
                m.getImage(),
                m.getOrder(),
                m.isPublished(),
                toDto(m.getRole()),
                toDto(m.getBiography()),
                m.getInstagramUrl()
        );
    }

    private I18nEmbeddable toEmb(I18nText t) {
        return new I18nEmbeddable(t.en(), t.de(), t.uk());
    }

    private I18nText toDto(I18nEmbeddable e) {
        return new I18nText(e.getEn(), e.getDe(), e.getUk());
    }
}
