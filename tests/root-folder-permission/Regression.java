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

            for (BusiResourceEnum type : List.of(BusiResourceEnum.DATASOURCE, BusiResourceEnum.DATASET)) {
                for (Integer weight : new Integer[]{null, 0, 1, 2, 6}) {
                    when(auth.resource(any())).thenReturn(List.of(node(0L, weight, false)));
                    denied(() -> guard.check(0L, type), type + " root denied at " + weight);
                }
                for (int weight : new int[]{7, 9}) {
                    when(auth.resource(any())).thenReturn(List.of(node(0L, weight, false)));
                    guard.check(0L, type);
                    check(true, type + " authorized root");
                }
                when(auth.resource(any())).thenReturn(List.of(node(42L, 9, false)));
                denied(() -> guard.check(0L, type), type + " writable child is not root");
                BusiNodeVO root = node(0L, 0, false);
                root.setChildren(List.of(node(42L, 9, false)));
                when(auth.resource(any())).thenReturn(List.of(root));
                denied(() -> guard.check(null, type), type + " null parent checks root");
                when(auth.resource(any())).thenReturn(List.of(node(0L, 9, true)));
                denied(() -> guard.check(0L, type), type + " leaf cannot authorize root");
                when(auth.resource(any())).thenReturn(null);
                denied(() -> guard.check(0L, type), type + " missing tree denied");
                when(auth.resource(any())).thenReturn(List.of());
                denied(() -> guard.check(0L, type), type + " empty tree denied");
                when(auth.resource(any())).thenAnswer(call -> {
                    BusiNodeRequest request = call.getArgument(0);
                    check(type.name().equals(request.getBusiFlag()) && Boolean.FALSE.equals(request.getLeaf()),
                            "correct resource type and folders only");
                    return List.of(node(0L, 7, false));
                });
                guard.check(0L, type);
                reset(auth);
            }

            guard.check(42L, BusiResourceEnum.DATASET);
            verifyNoInteractions(auth);
            check(true, "non-root retains original checks");
            users.when(V3UserUtil::getUid).thenReturn(null);
            denied(() -> guard.check(0L, BusiResourceEnum.DATASET), "anonymous denied");
            users.when(V3UserUtil::getUid).thenReturn(11L);
            users.when(V3UserUtil::getLink).thenReturn(mock(LinkIdentity.class));
            denied(() -> guard.check(0L, BusiResourceEnum.DATASET), "share identity denied");
            users.when(V3UserUtil::getLink).thenReturn(null);

            when(auth.resource(any())).thenThrow(new IllegalStateException("synthetic permission outage"));
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
            when(auth.resource(any())).thenReturn(List.of(node(0L, 0, false)));

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

            when(auth.resource(any())).thenReturn(List.of(node(0L, 7, false)));
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
            verifyNoInteractions(auth);
            verify(datasourceManage).innerSave(any());
            verify(datasetManage).save(group, false, true);
            check(true, "non-root entries retain original persistence");
            clearInvocations(auth, datasetManage);
            group.setPid(0L);
            group.setNodeType("dataset");
            dataset.create(group);
            verifyNoInteractions(auth);
            verify(datasetManage).save(group, false, true);
            check(true, "dataset leaf creation retains original path");
        }
        System.out.println("PASS " + passed + " root-folder permission assertions (isolated, no database)");
    }
}
