package io.dataease.regression;

import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.api.ds.vo.BusiCreateFolderRequest;
import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.dataset.manage.DatasetGroupManage;
import io.dataease.dataset.server.DatasetTreeServer;
import io.dataease.datasource.manage.DataSourceManage;
import io.dataease.datasource.server.DatasourceServer;
import io.dataease.exception.DEException;
import io.dataease.exception.ErrorCategory;
import io.dataease.i18n.Translator;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.dataease.permission.model.LinkIdentity;
import io.dataease.permission.model.V3AdminProxy;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.resource.RootFolderCreatePermissionManage;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.IDUtils;
import org.springframework.context.support.StaticMessageSource;

import java.util.List;

import static org.mockito.Mockito.*;

public class Regression {
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

    private static BusiNodeVO node(long id, Integer weight, boolean leaf) {
        BusiNodeVO node = new BusiNodeVO();
        node.setId(id);
        node.setWeight(weight);
        node.setLeaf(leaf);
        return node;
    }

    private static void inject(Object target, String name, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    public static void main(String[] args) throws Exception {
        new Translator().setMessageSource(new StaticMessageSource());
        RootFolderCreatePermissionManage guard = new RootFolderCreatePermissionManage();
        InteractiveAuthApi auth = mock(InteractiveAuthApi.class);
        V3AdminProxy proxy = mock(V3AdminProxy.class);
        try (var users = mockStatic(V3UserUtil.class);
             var beans = mockStatic(CommonBeanFactory.class);
             var ids = mockStatic(IDUtils.class)) {
            ids.when(IDUtils::snowID).thenReturn(123L);
            users.when(V3UserUtil::getUid).thenReturn(11L);
            users.when(V3UserUtil::getProxy).thenReturn(proxy);
            beans.when(() -> CommonBeanFactory.getBean(InteractiveAuthApi.class)).thenReturn(auth);

            final boolean[] allow = {false};
            doAnswer(call -> {
                var request = (io.dataease.api.permissions.auth.dto.BusiPerCheckDTO) call.getArgument(0);
                check(request.getAuthEnum() == io.dataease.constant.AuthEnum.MANAGE, "only manage destination");
                if (!allow[0]) throw DEException.of(ErrorCategory.FORBIDDEN, "i18n_error_forbidden");
                return null;
            }).when(auth).checkWriteDestination(any());
            for (BusiResourceEnum type : List.of(BusiResourceEnum.DATASOURCE, BusiResourceEnum.DATASET,
                    BusiResourceEnum.PANEL, BusiResourceEnum.SCREEN, BusiResourceEnum.SPREADSHEET,
                    BusiResourceEnum.DATA_FILLING)) {
                denied(() -> guard.check(0L, type), type + " root delegates denial");
                denied(() -> guard.check(null, type), type + " null parent delegates denial");
                denied(() -> guard.check(42L, type), type + " child delegates denial");
                allow[0] = true;
                guard.check(0L, type);
                guard.check(42L, type);
                allow[0] = false;
            }
            users.when(V3UserUtil::getUid).thenReturn(null);
            denied(() -> guard.check(0L, BusiResourceEnum.DATASET), "anonymous denied");
            users.when(V3UserUtil::getUid).thenReturn(11L);
            users.when(V3UserUtil::getLink).thenReturn(mock(LinkIdentity.class));
            denied(() -> guard.check(0L, BusiResourceEnum.DATASET), "share identity denied");
            users.when(V3UserUtil::getLink).thenReturn(null);

            doThrow(new IllegalStateException("synthetic permission outage")).when(auth).checkWriteDestination(any());
            try {
                guard.check(0L, BusiResourceEnum.DATASET);
                throw new AssertionError("permission outage must stop creation");
            } catch (IllegalStateException expected) {
                check(true, "permission outage stops creation");
            }
            reset(auth);
            beans.when(() -> CommonBeanFactory.getBean(InteractiveAuthApi.class)).thenReturn(null);
            users.when(V3UserUtil::getUid).thenReturn(1L);
            denied(() -> guard.check(0L, BusiResourceEnum.DATASET), "enterprise missing service denied even for uid 1");
            beans.when(() -> CommonBeanFactory.getBean("substituleLoginServer")).thenReturn(new Object());
            guard.check(0L, BusiResourceEnum.DATASET);
            check(true, "actual single-user substitute mode allowed");
            when(proxy.isProxy()).thenReturn(true);
            denied(() -> guard.check(0L, BusiResourceEnum.DATASET), "proxy cannot use community fallback");
            when(proxy.isProxy()).thenReturn(false);
            beans.when(() -> CommonBeanFactory.getBean("loginServer")).thenReturn(new Object());
            denied(() -> guard.check(0L, BusiResourceEnum.DATASET), "enterprise login prevents community fallback");
            beans.when(() -> CommonBeanFactory.getBean(InteractiveAuthApi.class)).thenReturn(auth);
            users.when(V3UserUtil::getUid).thenReturn(11L);
            doThrow(DEException.of(ErrorCategory.FORBIDDEN, "i18n_error_forbidden")).when(auth).checkWriteDestination(any());

            DatasourceServer datasource = new DatasourceServer();
            DataSourceManage datasourceManage = mock(DataSourceManage.class);
            inject(datasource, "dataSourceManage", datasourceManage);
            inject(datasource, "rootFolderCreatePermissionManage", guard);
            BusiCreateFolderRequest folder = new BusiCreateFolderRequest();
            folder.setPid(0L);
            folder.setName("synthetic-folder");
            folder.setNodeType("folder");
            denied(() -> datasource.createFolder(folder), "datasource entry rejects unauthorized root");
            verifyNoInteractions(datasourceManage);
            ids.verifyNoInteractions();
            check(true, "datasource rejection happens before persistence");

            DatasetTreeServer dataset = new DatasetTreeServer();
            DatasetGroupManage datasetManage = mock(DatasetGroupManage.class);
            inject(dataset, "datasetGroupManage", datasetManage);
            inject(dataset, "rootFolderCreatePermissionManage", guard);
            DatasetGroupInfoDTO group = new DatasetGroupInfoDTO();
            group.setNodeType("folder");
            for (Long pid : new Long[]{0L, null}) {
                group.setPid(pid);
                denied(() -> {
                    try {
                        dataset.create(group);
                    } catch (RuntimeException failure) {
                        throw failure;
                    } catch (Exception failure) {
                        throw new AssertionError(failure);
                    }
                }, "dataset entry rejects root including omitted parent");
            }
            verifyNoInteractions(datasetManage);
            check(true, "dataset rejection happens before persistence");

            doNothing().when(auth).checkWriteDestination(any());
            datasource.createFolder(folder);
            verify(datasourceManage).innerSave(any());
            check(true, "authorized datasource root reaches original persistence");
            group.setPid(0L);
            dataset.create(group);
            verify(datasetManage).save(group, false, true);
            check(true, "authorized dataset root reaches original persistence");
            clearInvocations(auth, datasourceManage, datasetManage);
            folder.setPid(42L);
            datasource.createFolder(folder);
            group.setPid(42L);
            dataset.create(group);
            verify(auth, times(2)).checkWriteDestination(any());
            verify(datasourceManage).innerSave(any());
            verify(datasetManage).save(group, false, true);
            check(true, "non-root entries retain original persistence");
            clearInvocations(auth, datasetManage);
            group.setPid(0L);
            group.setNodeType("dataset");
            dataset.create(group);
            verify(auth).checkWriteDestination(any());
            verify(datasetManage).save(group, false, true);
            check(true, "authorized dataset leaf checks destination");
            clearInvocations(datasetManage);
            doThrow(DEException.of(ErrorCategory.FORBIDDEN, "i18n_error_forbidden")).when(auth).checkWriteDestination(any());
            denied(() -> {
                try { dataset.create(group); } catch (RuntimeException failure) { throw failure; }
                catch (Exception failure) { throw new AssertionError(failure); }
            }, "unauthorized dataset leaf denied");
            verifyNoInteractions(datasetManage);
            var leaf = new io.dataease.api.ds.vo.BusiDsRequest();
            leaf.setPid(0L);
            clearInvocations(datasourceManage);
            denied(() -> datasource.save(leaf), "datasource leaf denied before configuration or connection work");
            verifyNoInteractions(datasourceManage);
            var visual = new io.dataease.visualization.server.DataVisualizationServer();
            inject(visual, "rootFolderCreatePermissionManage", guard);
            for (String type : List.of("dashboard", "dataV")) {
                var canvas = new io.dataease.api.visualization.request.DataVisualizationBaseRequest();
                canvas.setType(type);
                canvas.setPid(0L);
                denied(() -> {
                    try { visual.saveCanvas(canvas); } catch (RuntimeException failure) { throw failure; }
                    catch (Exception failure) { throw new AssertionError(failure); }
                }, "visual canvas denied before persistence: " + type);
            }
            // Applications must authorize all target namespaces before touching any imported data.
            doAnswer(call -> {
                var target = (io.dataease.api.permissions.auth.dto.BusiPerCheckDTO) call.getArgument(0);
                if (target.getBusiEnum() == BusiResourceEnum.DATASET) {
                    throw DEException.of(ErrorCategory.FORBIDDEN, "i18n_error_forbidden");
                }
                return null;
            }).when(auth).checkWriteDestination(any());
            var appCanvas = new io.dataease.api.visualization.request.DataVisualizationBaseRequest();
            appCanvas.setType("dashboard");
            appCanvas.setPid(42L);
            appCanvas.setAppData(new io.dataease.api.visualization.vo.VisualizationExport2AppVO());
            appCanvas.setDatasetFolderPid(0L);
            denied(() -> {
                try { visual.saveCanvas(appCanvas); } catch (RuntimeException failure) { throw failure; }
                catch (Exception failure) { throw new AssertionError(failure); }
            }, "application dataset target checked before import writes");

            // Material restoration runs in an outer aspect: it must also check before restoring files.
            var importedImages = mock(io.dataease.visualization.manage.ImportedImageManage.class);
            var aspect = new io.dataease.config.MaterialImportAspect(importedImages,
                    mock(org.springframework.transaction.PlatformTransactionManager.class));
            appCanvas.setMaterialBundle(new io.dataease.api.material.vo.MaterialBundle());
            var join = mock(org.aspectj.lang.ProceedingJoinPoint.class);
            var signature = mock(org.aspectj.lang.Signature.class);
            when(signature.getName()).thenReturn("saveCanvas");
            when(join.getSignature()).thenReturn(signature);
            when(join.getTarget()).thenReturn(visual);
            when(join.getArgs()).thenReturn(new Object[]{appCanvas});
            denied(() -> {
                try { aspect.restore(join); } catch (RuntimeException failure) { throw failure; }
                catch (Throwable failure) { throw new AssertionError(failure); }
            }, "material restore preflight rejects unauthorized dataset target");
            verifyNoInteractions(importedImages);
            check(true, "no material restore side effect before authorization");

            // Existing dataset saves in the same directory do not require parent management again.
            var groups = mock(io.dataease.dataset.dao.auto.mapper.CoreDatasetGroupRepository.class);
            inject(dataset, "coreDatasetGroupRepository", groups);
            var savedGroup = new io.dataease.dao.auto.entity.CoreDatasetGroup();
            savedGroup.setId(55L);savedGroup.setPid(42L);
            when(groups.findById(55L)).thenReturn(java.util.Optional.of(savedGroup));
            group.setId(55L);group.setPid(42L);
            clearInvocations(auth, datasetManage);
            dataset.save(group);
            verifyNoInteractions(auth);
            verify(datasetManage).save(group, false, true);
            check(true, "unchanged dataset parent preserves update path");
            group.setPid(0L);
            clearInvocations(datasetManage);
            denied(() -> {
                try { dataset.save(group); } catch (RuntimeException failure) { throw failure; }
                catch (Exception failure) { throw new AssertionError(failure); }
            }, "dataset parent change cannot bypass move authorization");
            verifyNoInteractions(datasetManage);
            var canvases = mock(io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoRepository.class);
            var canvasManage = mock(io.dataease.visualization.manage.CoreVisualizationManage.class);
            var charts = mock(io.dataease.chart.manage.ChartDataManage.class);
            inject(visual, "dataVisualizationInfoRepository", canvases);
            inject(visual, "coreVisualizationManage", canvasManage);
            inject(visual, "chartDataManage", charts);
            var savedCanvas = new io.dataease.dao.auto.entity.DataVisualizationInfo();
            savedCanvas.setId(66L);savedCanvas.setPid(-1L);savedCanvas.setType("dashboard");
            when(canvases.lockForSave(66L)).thenReturn(java.util.Optional.of(savedCanvas));
            when(canvases.findDvInfoStats(66L)).thenReturn(0);
            var editCanvas = new io.dataease.api.visualization.request.DataVisualizationBaseRequest();
            editCanvas.setId(66L);editCanvas.setPid(-1L);editCanvas.setType("dashboard");
            editCanvas.setCanvasViewInfo(java.util.Map.of());
            clearInvocations(auth);
            visual.updateCanvas(editCanvas);
            verifyNoInteractions(auth);
            verify(canvasManage).innerEdit(any());
            check(true, "save existing temporary draft retains original path");
            doThrow(DEException.of(ErrorCategory.FORBIDDEN, "i18n_error_forbidden")).when(auth).checkWriteDestination(any());
            clearInvocations(canvasManage, charts);
            editCanvas.setPid(0L);
            denied(() -> visual.updateCanvas(editCanvas), "draft cannot migrate to unauthorized root through update");
            verifyNoInteractions(canvasManage, charts);
            savedCanvas.setPid(0L);
            when(canvases.exists(any(org.springframework.data.jpa.domain.Specification.class))).thenReturn(true);
            clearInvocations(auth);
            visual.updateCanvas(editCanvas);
            verifyNoInteractions(auth);
            check(true, "existing root canvas update does not require root creation permission");
            InteractiveAuthApi legacy = mock(InteractiveAuthApi.class, CALLS_REAL_METHODS);
            denied(() -> legacy.checkWriteDestination(new io.dataease.api.permissions.auth.dto.BusiPerCheckDTO(
                    0L, BusiResourceEnum.DATASET, io.dataease.constant.AuthEnum.MANAGE)), "older provider fails closed");
        }
        System.out.println("PASS " + passed + " root-folder permission assertions (isolated, no database)");
    }
}
