package io.dataease.share.manage;

import io.dataease.share.server.ShareTicketServer;
import io.dataease.share.server.XpackShareServer;
import io.dataease.utils.ModelUtils;
import io.dataease.visualization.manage.CoreVisualizationManage;
import io.dataease.visualization.dao.auto.entity.DataVisualizationInfo;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoMapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Run main directly: no database, application startup or secret file writes; Surefire skips tests. */
public class DesktopShareProfileRegression {
    private static final Class<?>[] SHARE_COMPONENTS = {
            ShareSecretManage.class, XpackShareManage.class, ShareTicketManage.class,
            XpackShareServer.class, ShareTicketServer.class
    };

    public static void main(String[] args) throws Exception {
        verifyProfiles("desktop", false);
        verifyProfiles("standalone", true);
        verifyProfiles("distributed", true);
        verifyProfiles("desktop,standalone", false);
        verifyDeletion("desktop");
        verifyDeletion("standalone");
        verifyDeletion("distributed");
        System.out.println("Desktop sharing regression passed: 4 profile and 3 deletion scenarios");
    }

    private static void verifyProfiles(String profiles, boolean enabled) {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().setActiveProfiles(profiles.split(","));
            context.register(SHARE_COMPONENTS);
            for (Class<?> component : SHARE_COMPONENTS) {
                boolean registered = Arrays.stream(context.getBeanDefinitionNames())
                        .map(context::getBeanDefinition)
                        .anyMatch(definition -> component.getName().equals(definition.getBeanClassName()));
                if (registered != enabled) {
                    throw new AssertionError(profiles + ": unexpected registration of " + component.getName());
                }
            }
            if (!enabled) context.refresh();
        }
    }

    private static void verifyDeletion(String profile) throws Exception {
        new ModelUtils().setModelValue(profile);
        try (var context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().setActiveProfiles(profile);
            context.register(CoreVisualizationManage.class);
            // Mock persistence and audit collaborators; only the actual injection and delete orchestration run.
            for (var field : CoreVisualizationManage.class.getDeclaredFields()) {
                if (field.isAnnotationPresent(Resource.class)) {
                    context.getBeanFactory().registerSingleton(field.getName(), mock(field.getType()));
                }
            }
            XpackShareManage sharing = null;
            if (!"desktop".equals(profile)) {
                sharing = mock(XpackShareManage.class);
                context.getBeanFactory().registerSingleton("xpackShareManage", sharing);
            }
            context.refresh();
            when(context.getBean(DataVisualizationInfoMapper.class).selectById(42L))
                    .thenReturn(new DataVisualizationInfo());
            context.getBean(CoreVisualizationManage.class).delete(42L);
            if (sharing != null) verify(sharing).deleteByResource(42L);
            if ("desktop".equals(profile) && !context.getBeansOfType(ShareSecretManage.class).isEmpty()) {
                throw new AssertionError("Desktop must not initialize sharing secrets");
            }
        }
    }
}
