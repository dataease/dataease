package io.dataease.ai;

import io.dataease.ai.manage.*;
import io.dataease.ai.server.AiConversationServer;
import io.dataease.ai.support.AiErrors;
import io.dataease.api.ds.DatasourceApi;
import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.api.permissions.auth.dto.BusiPerCheckDTO;
import io.dataease.constant.AuthEnum;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.extensions.datasource.dto.DatasourceDTO;
import io.dataease.extensions.datasource.dto.DatasetTableDTO;
import io.dataease.license.bo.F2CLicResult;
import io.dataease.model.BusiNodeVO;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.system.manage.CorePermissionManage;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** Actual SQL scene/dialogue HTTP; synthetic current deployment, DE API and authorization SPI. */
public final class ConversationPermissionRegression {
    private static final String OPEN = "{\"scene\":\"sql\",\"resourceId\":\"11\",\"editorId\":\"parity\",\"sql\":\"select 1\"}";
    private static int open(MockMvc mvc) throws Exception {
        try {
            return mvc.perform(post("/ai/conversations").contentType(MediaType.APPLICATION_JSON).content(OPEN))
                    .andReturn().getResponse().getStatus();
        } catch (jakarta.servlet.ServletException failure) {
            if (failure.getCause() instanceof AiErrors.Failure) return 403;
            throw failure;
        }
    }
    public static void run() throws Exception {
        var fixture = new ModeRegression.Fixture();
        fixture.status.set(F2CLicResult.Status.expired);
        var allowed = new AtomicBoolean(true);
        var identityAllowed = new AtomicBoolean(true);
        var api = (InteractiveAuthApi) Proxy.newProxyInstance(ConversationPermissionRegression.class.getClassLoader(), new Class<?>[]{InteractiveAuthApi.class}, (proxy, method, args) -> {
            switch (method.getName()) {
                case "checkAiCommunityAllowed":
                case "checkAiAllowed":
                    return identityAllowed.get() && Objects.equals(args[0], 1L);
                case "checkAiCommunityAccess":
                case "checkAiAccess":
                    if (!identityAllowed.get() || !Objects.equals(args[0], 1L)) throw AiErrors.error("forbidden");
                    return null;
                case "checkAuth":
                    // An installed enterprise endpoint can be unavailable in verified community mode.
                    if (fixture.status.get() != F2CLicResult.Status.valid) throw AiErrors.error("unsupported");
                    var request = (BusiPerCheckDTO) args[0];
                    if (!allowed.get() || !Objects.equals(request.getId(), 11L) || request.getBusiEnum() != BusiResourceEnum.DATASOURCE || request.getAuthEnum() != AuthEnum.READ)
                        throw AiErrors.error("forbidden");
                    return null;
                default: throw new UnsupportedOperationException(method.getName());
            }
        });
        var datasource = (DatasourceApi) Proxy.newProxyInstance(ConversationPermissionRegression.class.getClassLoader(), new Class<?>[]{DatasourceApi.class}, (proxy, method, args) -> {
            if (method.getName().equals("tree")) {
                var node = new BusiNodeVO(); node.setId(11L); node.setName("Community resource"); node.setLeaf(true); return List.of(node);
            }
            if (method.getName().equals("getSimpleDs")) {
                var value = new DatasourceDTO(); value.setId(11L); value.setName("Community resource"); value.setType("MySQL"); value.setUpdateTime(1L); return value;
            }
            if (method.getName().equals("getTables")) {
                if (!allowed.get() || !identityAllowed.get()) throw new AssertionError("denied metadata operation executed");
                var table = new DatasetTableDTO(); table.setTableName("AI_PROBE"); return List.of(table);
            }
            throw new UnsupportedOperationException(method.getName());
        });
        var permissions = new CorePermissionManage();
        if (!permissions.checkAuth(new BusiPerCheckDTO(11L, BusiResourceEnum.DATASOURCE, AuthEnum.READ))) throw new AssertionError("existing community operation denied");
        var beans = new StaticListableBeanFactory(new HashMap<>(Map.of("authorization", api)));
        var access = new AiAccess(beans.getBeanProvider(InteractiveAuthApi.class), fixture.mode);
        var scene = new AiSqlConversationScene(datasource, beans.getBeanProvider(InteractiveAuthApi.class), access, permissions);
        beans.addBean("scene", scene);
        var usageConfiguration = new AiConfigurationManage(null) {
            public io.dataease.ai.dao.entity.CoreAiSetting setting() { var setting = new io.dataease.ai.dao.entity.CoreAiSetting(); setting.setEnabled(true); return setting; }
        };
        beans.addBean("runtime", new AiRuntimeManage(beans.getBeanProvider(io.dataease.api.ai.runtime.AiFeature.class), usageConfiguration, null, null, null, new com.fasterxml.jackson.databind.ObjectMapper(), access));
        var manage = new AiConversationManage(beans.getBeanProvider(AiConversationScene.class), beans.getBeanProvider(AiRuntimeManage.class), access);
        var mvc = MockMvcBuilders.standaloneSetup(new AiConversationServer(manage)).setControllerAdvice(new io.dataease.exception.GlobalExceptionHandler()).build();
        V3UserUtil.clear(); V3UserUtil.setUid(1L);
        try {
            if (open(mvc) != 200) throw new AssertionError("SQL assistant must preserve existing expired-license community operation access");
            var context = scene.resolve("11", "select 1");
            if (!scene.execute(context, "list_tables", Map.of()).contains("AI_PROBE")) throw new AssertionError("installed expired-license community metadata must match existing business permission path");
            identityAllowed.set(false);
            if (open(mvc) != 403) throw new AssertionError("community identity denial must retain HTTP403 through real global error advice");
            identityAllowed.set(true);
            fixture.status.set(F2CLicResult.Status.valid);
            if (open(mvc) != 200) throw new AssertionError("licensed authorized operation denied");
            if (!scene.execute(context, "list_tables", Map.of()).contains("AI_PROBE")) throw new AssertionError("licensed authorized metadata denied");
            allowed.set(false);
            try { scene.execute(context, "list_tables", Map.of()); throw new AssertionError("licensed metadata READ refusal bypassed"); }
            catch (AiErrors.Failure expected) { }
            if (open(mvc) < 400) throw new AssertionError("licensed READ denial used community fallback");
            allowed.set(true);
            var empty = new StaticListableBeanFactory();
            var missingAccess = new AiAccess(empty.getBeanProvider(InteractiveAuthApi.class), fixture.mode);
            var missingScene = new AiSqlConversationScene(datasource, empty.getBeanProvider(InteractiveAuthApi.class), missingAccess, permissions);
            empty.addBean("scene", missingScene);
            var missing = new AiConversationManage(empty.getBeanProvider(AiConversationScene.class), empty.getBeanProvider(AiRuntimeManage.class), missingAccess);
            try {
                if (open(MockMvcBuilders.standaloneSetup(new AiConversationServer(missing)).build()) < 400)
                    throw new AssertionError("licensed missing provider used community fallback");
            } finally { missing.destroy(); }
            System.out.println("PASS SQL dialogue and real scene metadata operation parity for installed expired-license community, current identity denial, licensed READ allow/deny and missing-provider rejection");
        } finally { manage.destroy(); V3UserUtil.clear(); V3UserUtil.setUid(1L); }
    }
}
