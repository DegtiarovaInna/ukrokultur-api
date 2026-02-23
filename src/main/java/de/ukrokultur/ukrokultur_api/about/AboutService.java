package de.ukrokultur.ukrokultur_api.about;

import de.ukrokultur.ukrokultur_api.common.dto.about.*;
import de.ukrokultur.ukrokultur_api.common.dto.I18nText;
import de.ukrokultur.ukrokultur_api.common.error.ErrorCode;
import de.ukrokultur.ukrokultur_api.common.exception.ApiException;
import de.ukrokultur.ukrokultur_api.common.exception.NotFoundException;
import de.ukrokultur.ukrokultur_api.common.slug.SlugGenerator;
import de.ukrokultur.ukrokultur_api.media.MediaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;
import java.util.List;

@Service
@Transactional
public class AboutService {

    private final AboutIntroRepository introRepository;
    private final AboutMemberRepository memberRepository;
    private final MediaService mediaService;

    public AboutService(
            AboutIntroRepository introRepository,
            AboutMemberRepository memberRepository,
            MediaService mediaService
    ) {
        this.introRepository = introRepository;
        this.memberRepository = memberRepository;
        this.mediaService = mediaService;
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

    public AboutIntroDto updateIntroMultipart(AboutIntroUpsertRequestDto data, MultipartFile image) {
        String imageUrl = data.image();
        if (image != null && !image.isEmpty()) {
            imageUrl = mediaService.upload(image, "about").publicUrl();
        }

        AboutIntroUpsertRequestDto req = new AboutIntroUpsertRequestDto(
                imageUrl,
                data.title(),
                data.text(),
                data.published()
        );

        return updateIntro(req);
    }

    @Transactional(readOnly = true)
    public List<AboutMemberDto> getMembersAdmin() {
        return memberRepository.findAllOrdered().stream().map(this::toMemberDto).toList();
    }

    public AboutMemberDto createMember(AboutMemberUpsertRequestDto req) {
        AboutMember m = new AboutMember();

        int max = memberRepository.findMaxSortOrder();
        m.setSortOrder(max + 1);

        String slug = resolveUniqueSlug(req.slug(), req.name());
        m.setSlug(slug);

        applyFields(m, req);

        AboutMember saved = memberRepository.save(m);
        return toMemberDto(saved);
    }


    public AboutMemberDto createMemberMultipart(AboutMemberUpsertRequestDto data, MultipartFile image) {
        String imageUrl = data.image();
        if (image != null && !image.isEmpty()) {
            imageUrl = mediaService.upload(image, "about").publicUrl();
        }

        AboutMemberUpsertRequestDto req = new AboutMemberUpsertRequestDto(
                data.slug(),
                data.name(),
                imageUrl,
                data.order(),
                data.published(),
                data.instagramUrl(),
                data.role(),
                data.biography()
        );

        return createMember(req);
    }

    public AboutMemberDto updateMember(UUID id, AboutMemberUpsertRequestDto req) {
        AboutMember m = memberRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("AboutMember", id));

        if (req.slug() != null && !req.slug().isBlank()) {
            String normalized = SlugGenerator.slugify(req.slug());
            if (normalized == null) {
                throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "Slug is invalid");
            }
            if (!normalized.equals(m.getSlug()) && memberRepository.existsBySlug(normalized)) {
                throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "Member slug already exists: " + normalized);
            }
            m.setSlug(normalized);
        } else if (m.getSlug() == null || m.getSlug().isBlank()) {
            m.setSlug(resolveUniqueSlug(null, req.name()));
        }

        Integer requestedOrder = req.order();
        if (requestedOrder != null) {
            moveMember(m, requestedOrder);
        }

        applyFields(m, req);

        return toMemberDto(memberRepository.save(m));
    }

    public AboutMemberDto updateMemberMultipart(UUID id, AboutMemberUpsertRequestDto data, MultipartFile image) {
        String imageUrl = data.image();
        if (image != null && !image.isEmpty()) {
            imageUrl = mediaService.upload(image, "about").publicUrl();
        }

        AboutMemberUpsertRequestDto req = new AboutMemberUpsertRequestDto(
                data.slug(),
                data.name(),
                imageUrl,
                data.order(),
                data.published(),
                data.instagramUrl(),
                data.role(),
                data.biography()
        );

        return updateMember(id, req);
    }

    public void deleteMember(UUID id) {
        AboutMember m = memberRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("AboutMember", id));

        memberRepository.delete(m);
        compressMemberOrder();
    }

    private String resolveUniqueSlug(String requested, String nameBase) {
        if (requested != null && !requested.isBlank()) {
            String normalized = SlugGenerator.slugify(requested);
            if (normalized == null) {
                throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "Slug is invalid");
            }
            if (memberRepository.existsBySlug(normalized)) {
                throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "Member slug already exists: " + normalized);
            }
            return normalized;
        }
        return SlugGenerator.generateUnique(nameBase, memberRepository::existsBySlug);
    }

    private void moveMember(AboutMember target, int newIndex) {
        List<AboutMember> ordered = memberRepository.findAllOrdered();

        ordered.removeIf(x -> x.getId().equals(target.getId()));

        int idx = Math.max(0, Math.min(newIndex, ordered.size()));
        ordered.add(idx, target);

        for (int i = 0; i < ordered.size(); i++) {
            ordered.get(i).setSortOrder(i);
        }

        memberRepository.saveAll(ordered);
    }

    private void compressMemberOrder() {
        List<AboutMember> ordered = memberRepository.findAllOrdered();
        for (int i = 0; i < ordered.size(); i++) {
            ordered.get(i).setSortOrder(i);
        }
        memberRepository.saveAll(ordered);
    }

    private void applyFields(AboutMember m, AboutMemberUpsertRequestDto req) {
        m.setName(req.name().trim());
        m.setImage(req.image());
        m.setPublished(Boolean.TRUE.equals(req.published()));
        m.setInstagramUrl(req.instagramUrl());
        m.setRole(toEmb(req.role()));
        m.setBiography(toEmb(req.biography()));
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
                m.getSortOrder(),
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