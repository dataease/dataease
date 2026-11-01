package io.dataease.share.manage;

import io.dataease.share.server.ShareTicketServer;
import io.dataease.share.server.XpackShareServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/** Profile registration regression; no application startup, database or secret file writes. */
public class DesktopShareProfileRegression {
    private static final Class<?>[] SHARE_COMPONENTS = {
            ShareSecretManage.class, LinkTokenValidationManage.class,
            XpackShareManage.class, ShareTicketManage.class,
            XpackShareServer.class, ShareTicketServer.class
    };

    public static void main(String[] args) {
        verify("desktop", false);
        verify("standalone", true);
        verify("distributed", true);
        verify("desktop,standalone", false);
        System.out.println("Desktop sharing profile regression passed (4 profile combinations)");
    }

    private static void verify(String profiles, boolean enabled) {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().setActiveProfiles(profiles.split(","));
            context.register(SHARE_COMPONENTS);
            for (Class<?> component : SHARE_COMPONENTS) {
                boolean registered = java.util.Arrays.stream(context.getBeanDefinitionNames())
                        .map(context::getBeanDefinition)
                        .anyMatch(definition -> component.getName().equals(definition.getBeanClassName()));
                if (registered != enabled) {
                    throw new AssertionError(profiles + ": unexpected registration of " + component.getName());
                }
            }
            // Refresh desktop to ensure that excluding the dependency chain leaves no injection failures.
            // Other profiles only check registration: their real dependencies require a full environment.
            if (!enabled) {
                context.refresh();
                if (!context.getBeansOfType(ShareSecretManage.class).isEmpty()) {
                    throw new AssertionError("Desktop must not initialize a sharing secret");
                }
            }
        }
    }
}
