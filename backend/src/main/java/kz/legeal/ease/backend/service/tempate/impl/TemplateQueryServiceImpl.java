package kz.legeal.ease.backend.service.tempate.impl;

import kz.legeal.ease.backend.dto.template.TemplateDto;
import kz.legeal.ease.backend.exception.template.TemplateForbiddenException;
import kz.legeal.ease.backend.exception.template.TemplateNotFoundException;
import kz.legeal.ease.backend.mapper.TemplateMapper;
import kz.legeal.ease.backend.repository.TemplateRepository;
import kz.legeal.ease.backend.service.tempate.TemplateQueryService;
import kz.legeal.ease.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TemplateQueryServiceImpl implements TemplateQueryService {

    private final TemplateRepository templateRepository;
    private final TemplateMapper templateMapper;


    @Override
    @Transactional(readOnly = true)
    public Page<TemplateDto> getMyTemplates(Pageable pageable) {
        final var currentLawyer = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return templateRepository.findAllByLawyerId(currentLawyer.getId(), pageable)
                .map(templateMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public TemplateDto getMyTemplateById(Long templateId) {
        final var currentLawyer = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        final var template = templateRepository.findByIdAndActive(templateId)
                .orElseThrow(() -> new TemplateNotFoundException(templateId));

        if (!template.isOwnedBy(currentLawyer.getId())) {
            throw new TemplateForbiddenException();
        }

        return templateMapper.toDto(template);
    }
}
