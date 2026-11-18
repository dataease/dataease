package io.dataease.regression;

import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.exception.DEException;
import io.dataease.exception.ErrorCategory;
import io.dataease.i18n.Translator;
import io.dataease.permission.model.V3AdminProxy;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.resource.RootFolderCreatePermissionManage;
import io.dataease.substitute.permissions.login.SubstituleLoginServer;
import io.dataease.utils.CommonBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.StaticMessageSource;

import static org.mockito.Mockito.*;

public class CommunityRegression {
    private static int passed;

    private static void check(boolean value, String name) {
        if (!value) throw new AssertionError(name);
        passed++;
    }

    private static void denied(Runnable action, String name) {
        try {
            action.run();
        } catch (DEException failure) {
            check(failure.getCategory() == ErrorCategory.FORBIDDEN, name);
            return;
        }
        throw new AssertionError(name);
    }

    public static void main(String[] args) {
        new Translator().setMessageSource(new StaticMessageSource());
        V3AdminProxy proxy = mock(V3AdminProxy.class);
        try (var users = mockStatic(V3UserUtil.class)) {
            users.when(V3UserUtil::getUid).thenReturn(1L);
            users.when(V3UserUtil::getProxy).thenReturn(proxy);
            try (var context = new AnnotationConfigApplicationContext()) {
                context.register(CommonBeanFactory.class, SubstituleLoginServer.class,
                        RootFolderCreatePermissionManage.class);
                context.refresh();
                check(context.containsBean("substituleLoginServer"), "actual substitute bean name");
                check(!context.containsBean("loginServer"), "enterprise login absent");
                check(context.getBeansOfType(InteractiveAuthApi.class).isEmpty(), "no enterprise permission implementation");
                var guard = context.getBean(RootFolderCreatePermissionManage.class);
                for (BusiResourceEnum type : new BusiResourceEnum[]{BusiResourceEnum.DATASET, BusiResourceEnum.DATASOURCE}) {
                    guard.check(0L, type);
                    guard.check(null, type);
                    check(true, type + " root allowed without XPack");
                    users.when(V3UserUtil::getUid).thenReturn(11L);
                    denied(() -> guard.check(0L, type), type + " substitute mode not open to arbitrary users");
                    users.when(V3UserUtil::getUid).thenReturn(1L);
                    when(proxy.isProxy()).thenReturn(true);
                    denied(() -> guard.check(0L, type), type + " substitute proxy denied");
                    when(proxy.isProxy()).thenReturn(false);
                }
            }
            try (var context = new AnnotationConfigApplicationContext()) {
                context.getBeanFactory().registerSingleton("loginServer", new Object());
                context.register(CommonBeanFactory.class, SubstituleLoginServer.class,
                        RootFolderCreatePermissionManage.class);
                context.refresh();
                check(!context.containsBean("substituleLoginServer"), "actual conditional disables substitute login");
                var guard = context.getBean(RootFolderCreatePermissionManage.class);
                denied(() -> guard.check(0L, BusiResourceEnum.DATASET), "enterprise missing auth fails closed");
            }
        }
        System.out.println("PASS " + passed + " real Spring community-context assertions (no XPack implementation)");
    }
}
