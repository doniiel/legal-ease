package kz.legeal.ease.backend.service.rule;

import kz.legeal.ease.backend.service.rule.chain.RuleChainContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class RuleHandler {

    @Getter
    private RuleHandler next;

    public final RuleHandler setNext(RuleHandler next) {
        this.next = next;
        return next;
    }

    public final void process(RuleChainContext context) {
        if (context.isAborted()) {
            log.debug("[{}] Skipped — chain aborted: {}", name(), context.getAbortReason());
            return;
        }

        log.debug("[{}] Processing...", name());
        handle(context);

        if (!context.isAborted() && next != null) {
            next.process(context);
        }
    }

    protected abstract void handle(RuleChainContext context);

    public abstract String name();
}
