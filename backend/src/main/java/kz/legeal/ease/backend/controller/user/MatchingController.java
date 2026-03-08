package kz.legeal.ease.backend.controller.user;

import jakarta.validation.Valid;
import kz.legeal.ease.backend.request.MatchingRequest;
import kz.legeal.ease.backend.service.rule.context.RuleContext;
import kz.legeal.ease.backend.service.rule.engine.RuleEngine;
import kz.legeal.ease.backend.service.rule.result.RuleEngineResult;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/matching")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class MatchingController {

    private final RuleEngine ruleEngine;

    // USER вводит текст или выбирает категорию → получает список подходящих шаблонов
    @PostMapping
    public RuleEngineResult match(@Valid @RequestBody MatchingRequest request) {
        final var context = RuleContext.builder()
                .inputText(request.getInputText())
                .categoryId(request.getCategoryId())
                .build();
        return ruleEngine.match(context);
    }
}
