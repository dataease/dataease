package io.dataease.regression;

import io.dataease.api.ds.vo.BusiDsRequest;
import io.dataease.api.sync.datasource.api.SyncDatasourceImportProvider;
import io.dataease.datasource.manage.DataSourceManage;
import io.dataease.datasource.server.DatasourceServer;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.DatasourceDTO;
import io.dataease.i18n.Translator;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.system.manage.CorePermissionManage;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.IDUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.context.support.StaticMessageSource;
import java.util.Base64;
import java.util.List;
import static org.mockito.Mockito.*;

/** Actual optional injection without any XPack implementation; other business dependencies are mocked. */
public class CommunityImportRegression {
    static int passed;
    static void check(boolean condition) { if (!condition) throw new AssertionError("Community scenario " + passed); passed++; }
    static void denied(Runnable action) { try { action.run(); } catch (DEException expected) { passed++; return; } throw new AssertionError("Expected rejection"); }
    public static void main(String[] args) throws Exception {
        check(CommunityImportRegression.class.getClassLoader().getResource("io/dataease/xpack/sync/datasource/manage/SyncDatasourceManage.class") == null);
        new Translator().setMessageSource(new StaticMessageSource());
        new io.dataease.utils.LocalModelUtils().setModelValue("standalone");
        new io.dataease.license.utils.CommonBeanFactory().setApplicationContext(new StaticApplicationContext());
        try (var context = new AnnotationConfigApplicationContext(); var license = mockStatic(LicenseUtil.class);
             var identity = mockStatic(V3UserUtil.class); var ids = mockStatic(IDUtils.class)) {
            for (var field : DatasourceServer.class.getDeclaredFields()) {
                var resource = field.getAnnotation(jakarta.annotation.Resource.class);
                var autowired = field.getAnnotation(org.springframework.beans.factory.annotation.Autowired.class);
                if (resource != null || autowired != null && autowired.required()) {
                    String name = resource != null && !resource.name().isEmpty() ? resource.name() : field.getName();
                    context.getBeanFactory().registerSingleton(name, mock(field.getType()));
                }
            }
            context.register(CommonBeanFactory.class, DatasourceServer.class);
            context.refresh();
            check(context.getBeansOfType(SyncDatasourceImportProvider.class).isEmpty());
            var server = context.getBean(DatasourceServer.class);
            var manage = context.getBean(DataSourceManage.class);
            when(manage.tree(any())).thenReturn(List.of());
            when(context.getBean(CorePermissionManage.class).checkAuth(any())).thenReturn(true);
            identity.when(V3UserUtil::getUid).thenReturn(1L);
            ids.when(IDUtils::snowID).thenReturn(123L);
            String config = "{\"host\":\"fixture.invalid\",\"port\":9030,\"dataBase\":\"fixture\",\"username\":\"user\",\"password\":\"synthetic-secret\"}";
            var ordinary = new BusiDsRequest(); ordinary.setType("doris"); ordinary.setName("ordinary"); ordinary.setPid(0L);
            ordinary.setConfiguration(Base64.getEncoder().encodeToString(config.getBytes()));
            check(!server.checkRepeat(ordinary));
            check(server.save(ordinary).getConfiguration().contains("synthetic-secret"));
            var stored = new DatasourceDTO(); stored.setId(44L); stored.setType("doris"); stored.setPid(0L); stored.setConfiguration(config);
            when(manage.getDs(44L)).thenReturn(stored);
            ordinary.setId(44L); ordinary.setConfiguration(Base64.getEncoder().encodeToString(config.replace("synthetic-secret", "******").getBytes()));
            check(server.update(ordinary).getConfiguration().contains("synthetic-secret"));
            ordinary.setId(null); ordinary.setCopySourceId(44L); denied(() -> server.save(ordinary));
            ordinary.setConfiguration(Base64.getEncoder().encodeToString(config.replace("synthetic-secret", "copy-secret").getBytes()));
            check(server.save(ordinary).getConfiguration().contains("copy-secret"));
            var imported = new BusiDsRequest(); imported.setSyncSourceId("source"); imported.setName("import"); imported.setPid(0L);
            denied(() -> server.checkRepeat(imported)); denied(() -> server.save(imported));
        }
        System.out.println("PASS " + passed + " actual Spring optional injection and ordinary datasource paths without XPack implementations");
    }
}
