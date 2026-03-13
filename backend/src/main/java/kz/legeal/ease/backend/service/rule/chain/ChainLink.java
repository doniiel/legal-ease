package kz.legeal.ease.backend.service.rule.chain;

import kz.legeal.ease.backend.service.rule.RuleHandler;

public class ChainLink extends RuleHandler {

    private final RuleHandler delegate;

    public ChainLink(RuleHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public void handle(RuleChainContext context) {
        delegate.handle(context);
    }

    @Override
    public String name() {
        return delegate.name();
    }
}