package kz.legeal.ease.backend.service;

import kz.legeal.ease.backend.request.MatchingRequest;
import kz.legeal.ease.backend.service.rule.result.RuleEngineResult;

public interface MatchingService {

    RuleEngineResult match(MatchingRequest request);
}
