package io.dataease.regression;

import io.dataease.api.ds.vo.BusiDsRequest;
import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.api.sync.datasource.api.SyncDatasourceImportProvider;
import io.dataease.api.sync.datasource.dto.SyncDatasourceImport;
import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.repository.CoreDatasourceRepository;
import io.dataease.datasource.manage.DataSourceManage;
import io.dataease.datasource.server.DatasourceServer;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.api.PluginManageApi;
import io.dataease.extensions.datasource.dto.DatasourceDTO;
import io.dataease.i18n.Translator;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.model.BusiNodeVO;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.CommonThreadPool;
import io.dataease.utils.IDUtils;
import org.springframework.context.support.StaticMessageSource;

import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;
import static org.mockito.Mockito.*;

public class Regression {
    private static int passed;
    private static final String CONFIG = "{\"host\":\"fixture.invalid\",\"port\":9030,\"dataBase\":\"fixture\",\"username\":\"fixture-user\",\"password\":\"synthetic-secret\",\"initialPoolSize\":5,\"minPoolSize\":5,\"maxPoolSize\":10,\"queryTimeout\":30}";
    private static void check(boolean condition) {
        if (!condition) throw new AssertionError("Scenario " + passed);
        passed++;
    }
    private static void denied(Runnable action) {
        try { action.run(); } catch (DEException expected) { passed++; return; }
        throw new AssertionError("Expected rejection");
    }
    private static void inject(Object target, String field, Object value) throws Exception {
        var f = target.getClass().getDeclaredField(field);
        f.setAccessible(true); f.set(target, value);
    }
    private static BusiDsRequest request() {
        var r = new BusiDsRequest(); r.setSyncSourceId("source"); r.setPid(0L); r.setName("imported"); return r;
    }
    private static BusiNodeVO node(long id, int weight, boolean leaf) {
        var n = new BusiNodeVO(); n.setId(id); n.setWeight(weight); n.setLeaf(leaf); return n;
    }
    public static void main(String[] args) throws Exception {
        new Translator().setMessageSource(new StaticMessageSource());
        new io.dataease.license.utils.CommonBeanFactory().setApplicationContext(new org.springframework.context.support.StaticApplicationContext());
        new io.dataease.utils.LocalModelUtils().setModelValue("standalone");
        var server = new DatasourceServer();
        var provider = mock(SyncDatasourceImportProvider.class);
        var auth = mock(InteractiveAuthApi.class);
        var manage = mock(DataSourceManage.class);
        var repository = mock(CoreDatasourceRepository.class);
        var plugins = mock(PluginManageApi.class);
        var pool = mock(CommonThreadPool.class);
        inject(server, "syncDatasourceImportProvider", provider);
        inject(server, "dataSourceManage", manage);
        inject(server, "coreDatasourceRepository", repository);
        inject(server, "pluginManage", plugins);
        inject(server, "commonThreadPool", pool);
        var source = new SyncDatasourceImport(); source.setType("doris"); source.setConfiguration(CONFIG);
        source.setDescription("server description");
        doReturn(source).when(provider).loadAuthorized("source");
        when(auth.resource(any())).thenReturn(List.of(node(0, 7, false)));
        when(manage.tree(any())).thenReturn(List.of());
        when(plugins.queryPluginDs()).thenReturn(List.of());
        try (var users = mockStatic(V3UserUtil.class); var license = mockStatic(LicenseUtil.class);
             var beans = mockStatic(CommonBeanFactory.class); var ids = mockStatic(IDUtils.class)) {
            users.when(V3UserUtil::getUid).thenReturn(11L);
            license.when(LicenseUtil::licenseValid).thenReturn(true);
            beans.when(() -> CommonBeanFactory.getBean(InteractiveAuthApi.class)).thenReturn(auth);
            ids.when(IDUtils::snowID).thenReturn(123L);
            check(!server.checkRepeat(request()));
            var saved = server.save(request());
            check(saved.getId() == 123L && saved.getType().equals("doris") && saved.getConfiguration().equals(CONFIG));
            check(saved.getName().equals("imported") && saved.getDescription().equals("server description"));
            verify(provider, times(2)).loadAuthorized("source"); passed++;
            verify(manage).innerSave(any(DatasourceDTO.class)); passed++;
            // Duplicate detection uses the server connection, not client configuration.
            var existingNode = node(44, 7, true); existingNode.setType("doris");
            var existing = new CoreDatasource(); existing.setConfiguration(CONFIG); existing.setType("doris");
            when(manage.tree(any())).thenReturn(List.of(existingNode));
            when(repository.findAllById(any())).thenReturn(List.of(existing));
            check(server.checkRepeat(request()));
            for (Consumer<BusiDsRequest> tamper : List.<Consumer<BusiDsRequest>>of(
                    r -> r.setId(44L), r -> r.setCopySourceId(44L), r -> r.setConfiguration(CONFIG),
                    r -> r.setType("mysql"), r -> r.setApiConfigurationStr("[]"), r -> r.setParamsStr("[]"),
                    r -> r.setPid(null), r -> r.setPid(-1L), r -> r.setSyncSourceId(""))) {
                var r = request(); tamper.accept(r); denied(() -> server.checkRepeat(r)); denied(() -> server.save(r));
            }
            for (var n : List.of(node(0, 1, false), node(0, 7, true), node(77, 7, false))) {
                when(auth.resource(any())).thenReturn(List.of(n));
                denied(() -> server.checkRepeat(request())); denied(() -> server.save(request()));
            }
            var root = node(0, 1, false); root.setChildren(List.of(node(42, 7, false)));
            when(auth.resource(any())).thenReturn(List.of(root));
            var nested = request(); nested.setPid(42L); check(server.checkRepeat(nested));
            denied(() -> server.checkRepeat(request()));
            when(auth.resource(any())).thenReturn(List.of(node(0, 7, false)));
            when(provider.loadAuthorized("source")).thenThrow(new DEException(400, "synthetic denial"));
            denied(() -> server.checkRepeat(request())); denied(() -> server.save(request()));
            doReturn(source).when(provider).loadAuthorized("source");
            users.when(V3UserUtil::getUid).thenReturn(null);
            denied(() -> server.save(request()));
            users.when(V3UserUtil::getUid).thenReturn(11L);
            license.when(LicenseUtil::licenseValid).thenReturn(false);
            denied(() -> server.save(request()));
            license.when(LicenseUtil::licenseValid).thenReturn(true);
            // Ordinary create/copy still reject masks. A fresh password still works.
            var ordinary = new BusiDsRequest(); ordinary.setPid(0L); ordinary.setName("ordinary"); ordinary.setType("doris");
            ordinary.setConfiguration(Base64.getEncoder().encodeToString(CONFIG.replace("synthetic-secret", "******").getBytes()));
            denied(() -> server.checkRepeat(ordinary)); denied(() -> server.save(ordinary));
            ordinary.setCopySourceId(44L); denied(() -> server.save(ordinary));
            ordinary.setConfiguration(Base64.getEncoder().encodeToString(CONFIG.getBytes()));
            check(server.save(ordinary).getConfiguration().equals(CONFIG));
            var restore = DatasourceServer.class.getDeclaredMethod("restorePlainConfiguration", String.class, Long.class, String.class);
            restore.setAccessible(true);
            var stored = new DatasourceDTO(); stored.setType("doris"); stored.setConfiguration(CONFIG);
            when(manage.getDs(44L)).thenReturn(stored);
            var permission = mock(io.dataease.system.manage.CorePermissionManage.class);
            when(permission.checkAuth(any())).thenReturn(true); inject(server, "corePermissionManage", permission);
            check(((String) restore.invoke(server, CONFIG.replace("synthetic-secret", "******"), 44L, "doris")).contains("synthetic-secret"));
            try { restore.invoke(server, CONFIG.replace("synthetic-secret", "******").replace("fixture.invalid", "changed.invalid"), 44L, "doris"); throw new AssertionError(); }
            catch (java.lang.reflect.InvocationTargetException e) { check(e.getCause() instanceof DEException); }
            // Save must recheck permissions after a successful duplicate check.
            when(manage.tree(any())).thenReturn(List.of());
            check(!server.checkRepeat(request()));
            int writes = mockingDetails(manage).getInvocations().size();
            when(auth.resource(any())).thenReturn(List.of(node(0, 1, false)));
            denied(() -> server.save(request()));
            check(mockingDetails(manage).getInvocations().size() == writes);
            when(auth.resource(any())).thenReturn(List.of(node(0, 7, false)));
            source.setConfiguration(CONFIG.replace("fixture.invalid", "updated.invalid"));
            check(server.save(request()).getConfiguration().contains("updated.invalid"));
            source.setConfiguration(CONFIG);
            inject(server, "syncDatasourceImportProvider", null);
            denied(() -> server.checkRepeat(request())); denied(() -> server.save(request()));
            check(server.save(ordinary).getConfiguration().contains("synthetic-secret"));
            inject(server, "syncDatasourceImportProvider", provider);
            beans.when(() -> CommonBeanFactory.getBean(InteractiveAuthApi.class)).thenReturn(null);
            denied(() -> server.save(request()));
            beans.when(() -> CommonBeanFactory.getBean(InteractiveAuthApi.class)).thenReturn(auth);
            users.when(V3UserUtil::getLink).thenReturn(mock(io.dataease.permission.model.LinkIdentity.class));
            denied(() -> server.checkRepeat(request())); denied(() -> server.save(request()));
            users.when(V3UserUtil::getLink).thenReturn(null);
            // Exercise actual ordinary save/update entry points for all affected built-in contracts.
            var json = new com.fasterxml.jackson.databind.ObjectMapper();
            var editable = DatasourceServer.class.getDeclaredMethod("configurationForEdit", String.class, String.class);
            editable.setAccessible(true);
            for (String type : List.of("doris", "mysql", "oracle", "sqlServer", "pg", "db2", "h2")) {
                new io.dataease.utils.LocalModelUtils().setModelValue("h2".equals(type) ? "desktop" : "standalone");
                String storedConfig = "h2".equals(type) ? CONFIG.substring(0, CONFIG.length() - 1) + ",\"jdbcUrl\":\"jdbc:h2:mem:sync_import_regression\"}" : CONFIG;
                stored.setType(type); stored.setId(44L); stored.setPid(42L); stored.setConfiguration(storedConfig);
                var config = (String) editable.invoke(server, type, storedConfig);
                var masked = io.dataease.utils.ConfigurationCredentials.mask(json.readTree(config)).toString();
                var edit = new BusiDsRequest(); edit.setId(44L); edit.setName("renamed"); edit.setType(type); edit.setPid(999L);
                edit.setConfiguration(Base64.getEncoder().encodeToString(masked.getBytes()));
                var updated = server.update(edit);
                check(json.readTree(updated.getConfiguration()).path("password").asText().equals("synthetic-secret"));
                check(updated.getId() == 44L && updated.getPid() == 42L && updated.getName().equals("renamed"));
                // A save request carrying an existing id still follows the ordinary update route.
                check(json.readTree(server.save(edit).getConfiguration()).path("password").asText().equals("synthetic-secret"));
                for (String changed : List.of(masked.replace("fixture.invalid", "changed.invalid"),
                        masked.replace("9030", "9031"), masked.replace("fixture-user", "another-user"))) {
                    edit.setConfiguration(Base64.getEncoder().encodeToString(changed.getBytes()));
                    denied(() -> server.update(edit));
                }
                edit.setConfiguration(Base64.getEncoder().encodeToString(masked.replace("fixture.invalid", "changed.invalid").replace("******", "new-synthetic-secret").getBytes()));
                check(json.readTree(server.update(edit).getConfiguration()).path("password").asText().equals("new-synthetic-secret"));
                edit.setConfiguration(Base64.getEncoder().encodeToString(masked.replace("******", "").getBytes()));
                check(json.readTree(server.update(edit).getConfiguration()).path("password").asText().isEmpty());
                var copy = new BusiDsRequest(); copy.setName("copy"); copy.setType(type); copy.setPid(0L); copy.setCopySourceId(44L);
                copy.setConfiguration(Base64.getEncoder().encodeToString(masked.getBytes()));
                denied(() -> server.save(copy));
                copy.setConfiguration(Base64.getEncoder().encodeToString(masked.replace("******", "new-copy-secret").getBytes()));
                check(json.readTree(server.save(copy).getConfiguration()).path("password").asText().equals("new-copy-secret"));
            }
            new io.dataease.utils.LocalModelUtils().setModelValue("standalone");
            stored.setType("doris");
            when(permission.checkAuth(any())).thenReturn(false);
            var unauthorized = new BusiDsRequest(); unauthorized.setId(44L); unauthorized.setType("doris");
            unauthorized.setConfiguration(Base64.getEncoder().encodeToString(CONFIG.replace("synthetic-secret", "******").getBytes()));
            denied(() -> server.update(unauthorized));
            when(permission.checkAuth(any())).thenReturn(true);
            HttpRegression.run(server, CONFIG);
        }
        System.out.println("PASS " + passed + " core import, authorization and ordinary credential checks (no database/network)");
    }
}
