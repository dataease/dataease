import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.api.permissions.auth.dto.BusiPerCheckDTO;
import io.dataease.api.visualization.DataVisualizationApi;
import io.dataease.api.visualization.dto.VisualizationViewTableDTO;
import io.dataease.api.visualization.request.DataVisualizationBaseRequest;
import io.dataease.auth.DePermit;
import io.dataease.constant.AuthEnum;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.dao.auto.entity.DataVisualizationInfo;
import io.dataease.exception.DEException;
import io.dataease.permission.model.LinkIdentity;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoRepository;
import io.dataease.visualization.server.DataVisualizationServer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.*;
import static org.mockito.Mockito.*;

public class Regression {
    static int passed;
    static void check(boolean value, String name) {
        if (!value) throw new AssertionError(name);
        passed++;
        System.out.println("PASS " + name);
    }
    static void denied(Runnable work, String name) {
        try { work.run(); } catch (DEException expected) { check(true, name); return; }
        throw new AssertionError(name);
    }
    static void set(Object target, String name, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
    static GenericApplicationContext context(Object auth, boolean community, boolean enterprise) {
        var context = new GenericApplicationContext();
        if (auth != null) context.getBeanFactory().registerSingleton("auth", auth);
        if (community) context.getBeanFactory().registerSingleton("substituleLoginServer", new Object());
        if (enterprise) context.getBeanFactory().registerSingleton("loginServer", new Object());
        context.refresh();
        new CommonBeanFactory().setApplicationContext(context);
        return context;
    }
    public static void main(String[] args) throws Exception {
        var server = new DataVisualizationServer();
        var repository = mock(DataVisualizationInfoRepository.class);
        set(server, "dataVisualizationInfoRepository", repository);
        set(server, "snapshotDataVisualizationInfoRepository", mock(io.dataease.visualization.dao.auto.mapper.SnapshotDataVisualizationInfoRepository.class));
        var resource = new DataVisualizationInfo();
        resource.setId(123L); resource.setName("published name"); resource.setType("dashboard");
        resource.setComponentData("secret draft/layout must never be serialized");
        when(repository.findById(123L)).thenReturn(Optional.of(resource));
        when(repository.findById(999L)).thenReturn(Optional.empty());
        var auth = mock(InteractiveAuthApi.class);
        try (var context = context(auth, false, true)) {
            V3UserUtil.setUid(10L);
            var request = new DataVisualizationBaseRequest(123L, "dataV");
            request.setResourceTable("snapshot");
            var name = server.findResourceName(request);
            var json = new ObjectMapper().readTree(new ObjectMapper().writeValueAsString(name));
            check(json.size() == 2 && json.get("id").asText().equals("123")
                    && json.get("name").asText().equals("published name"), "name response contains only id and published name");
            verify(auth).checkAuth(new BusiPerCheckDTO(123L, BusiResourceEnum.PANEL, AuthEnum.READ));
            check(true, "stored dashboard type overrides forged screen category");
            resource.setType("dataV"); server.findResourceName(request);
            verify(auth).checkAuth(new BusiPerCheckDTO(123L, BusiResourceEnum.SCREEN, AuthEnum.READ));
            check(true, "screen uses screen read authorization");
            doAnswer(call -> { DEException.throwException("denied"); return null; }).when(auth).checkAuth(any());
            denied(() -> server.findResourceName(request), "unauthorized name read rejected");
            denied(() -> server.detailList(123L), "unauthorized metadata rejected before query");
            reset(auth);
            doAnswer(call -> { if (((BusiPerCheckDTO) call.getArgument(0)).getAuthEnum() == AuthEnum.MANAGE)
                DEException.throwException("read only"); return null; }).when(auth).checkAuth(any());
            server.findResourceName(request);
            denied(() -> server.detailList(123L), "read-only user cannot read draft field metadata");
            reset(auth);
            var factory = mock(JPAQueryFactory.class);
            @SuppressWarnings("unchecked") JPAQuery<VisualizationViewTableDTO> query = mock(JPAQuery.class, RETURNS_SELF);
            when(factory.select(org.mockito.ArgumentMatchers.<Expression<VisualizationViewTableDTO>>any())).thenReturn(query);
            when(query.fetch()).thenReturn(List.of()); set(server, "queryFactory", factory);
            check(server.detailList(123L).isEmpty(), "authorized editor reaches metadata query");
            verify(auth).checkAuth(new BusiPerCheckDTO(123L, BusiResourceEnum.SCREEN, AuthEnum.MANAGE));
            check(true, "editor metadata requires actual resource manage permission");
            clearInvocations(auth, factory);
            resource.setDeleteFlag(true);
            denied(() -> server.findResourceName(request), "deleted visualization denied");
            resource.setDeleteFlag(false);
            denied(() -> server.detailList(999L), "missing visualization denied");
            denied(() -> server.detailList(0L), "zero id cannot bypass permission provider");
            denied(() -> server.detailList(null), "null id denied");
            V3UserUtil.clear();
            denied(() -> server.findResourceName(request), "anonymous name request denied");
            denied(() -> server.detailList(123L), "anonymous metadata request denied");
            V3UserUtil.setLink(new LinkIdentity(55L, 10L, 123L, 1L));
            denied(() -> server.findResourceName(request), "share identity cannot use internal name endpoint");
            denied(() -> server.detailList(123L), "share identity cannot access editor metadata");
            verifyNoInteractions(auth, factory);
            check(true, "invalid and public-link requests stop before authorization and field query");
        } finally { V3UserUtil.clear(); }
        try (var context = context(null, false, true)) {
            V3UserUtil.setUid(1L);
            denied(() -> server.detailList(123L), "missing enterprise authorization service fails closed");
        }
        try (var context = context(null, true, false)) {
            V3UserUtil.setUid(1L);
            check(server.findResourceName(new DataVisualizationBaseRequest(123L, "dataV")) != null,
                    "actual single-user community mode remains available");
            V3UserUtil.setUid(10L);
            denied(() -> server.detailList(123L), "community fallback never permits arbitrary user");
        } finally { V3UserUtil.clear(); }
        var internalManage = mock(io.dataease.visualization.manage.CoreVisualizationManage.class);
        var internalResult = new io.dataease.api.visualization.vo.DataVisualizationVO();
        internalResult.setName("audit name");
        when(internalManage.findDvInfo(123L, "dashboard", "core")).thenReturn(internalResult);
        set(server, "coreVisualizationManage", internalManage);
        check(server.findNameById(new DataVisualizationBaseRequest(123L, "dashboard")) == internalResult,
                "internal audit Java contract preserved without HTTP exposure");
        var nameMethod = DataVisualizationApi.class.getMethod("findResourceName", DataVisualizationBaseRequest.class);
        check(nameMethod.getAnnotation(PostMapping.class).value()[0].equals("/findNameById"), "name HTTP URL preserved");
        check(nameMethod.getAnnotation(DePermit.class).value()[0].contains(":read"), "gateway read permission declared");
        check(DataVisualizationApi.class.getMethod("detailList", Long.class).getAnnotation(DePermit.class).value()[0].contains(":manage"), "gateway editor permission declared");
        check(DataVisualizationApi.class.getMethod("findNameById", DataVisualizationBaseRequest.class).getAnnotation(PostMapping.class) == null,
                "internal audit method is no longer mapped to HTTP");
        System.out.println("RESULT PASS=" + passed);
    }
}
