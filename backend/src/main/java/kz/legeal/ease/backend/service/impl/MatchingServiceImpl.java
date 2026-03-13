package kz.legeal.ease.backend.service.impl;

import kz.legeal.ease.backend.request.MatchingRequest;
import kz.legeal.ease.backend.service.MatchingService;
import kz.legeal.ease.backend.service.rule.context.RuleContext;
import kz.legeal.ease.backend.service.rule.engine.RuleEngine;
import kz.legeal.ease.backend.service.rule.result.RuleEngineResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MatchingServiceImpl implements MatchingService {

    private final RuleEngine ruleEngine;

    @Override
    @Transactional
    public RuleEngineResult match(MatchingRequest request) {
        final var context = RuleContext.builder()
                .inputText(request.getInputText())
                .categoryId(request.getCategoryId())
                .build();
        return ruleEngine.match(context);
    }
}
