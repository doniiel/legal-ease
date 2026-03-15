package kz.legeal.ease.backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine-based in-process cache configuration.
 *
 * <p>Cache names:
 * <ul>
 *   <li>{@code validationRules}  — ValidationRule lists keyed by templateId; TTL 5 min</li>
 *   <li>{@code riskRules}        — RiskRule lists keyed by templateId; TTL 5 min</li>
 *   <li>{@code matchingRules}    — MatchingRule lists keyed by templateId; TTL 5 min</li>
 *   <li>{@code conditionalRules} — ConditionRule lists keyed by templateId; TTL 5 min</li>
 *   <li>{@code requiredDocRules} — RequiredDocRule lists keyed by templateId; TTL 5 min</li>
 *   <li>{@code templates}        — Published Template by ID; TTL 10 min</li>
 * </ul>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String VALIDATION_RULES  = "validationRules";
    public static final String RISK_RULES        = "riskRules";
    public static final String MATCHING_RULES    = "matchingRules";
    public static final String CONDITIONAL_RULES = "conditionalRules";
    public static final String REQUIRED_DOC_RULES = "requiredDocRules";
    public static final String TEMPLATES         = "templates";

    @Bean
    public CacheManager cacheManager() {
        final var manager = new CaffeineCacheManager(
                VALIDATION_RULES, RISK_RULES, MATCHING_RULES,
                CONDITIONAL_RULES, REQUIRED_DOC_RULES, TEMPLATES
        );
        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats());
        return manager;
    }
}
