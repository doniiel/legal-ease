package kz.legeal.ease.backend.controller.lawyer;

import kz.legeal.ease.backend.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lawyer/templates")
public class LawyerTemplateController {

    private final TemplateService templateService;

    @PostMapping
    public
}
