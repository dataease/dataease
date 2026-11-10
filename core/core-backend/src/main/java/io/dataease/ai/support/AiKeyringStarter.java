package io.dataease.ai.support;

import io.dataease.ai.manage.AiKeyringManage;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class AiKeyringStarter implements ApplicationRunner {
    private final AiKeyringManage keyring;

    public AiKeyringStarter(AiKeyringManage keyring) { this.keyring = keyring; }

    @Override
    public void run(ApplicationArguments args) {
        try {
            keyring.initialize();
        } catch (RuntimeException failure) {
            // AI stays unavailable; do not expose SQL/configuration or block unrelated product features.
            LoggerFactory.getLogger(AiKeyringStarter.class).warn("AI key ring initialization failed; AI key operations remain unavailable");
        }
    }
}
