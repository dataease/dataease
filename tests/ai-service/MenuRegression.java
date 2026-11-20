package io.dataease.ai;

import io.dataease.listener.sql.AiRecordsMenuInit;
import io.dataease.menu.dao.auto.entity.CoreMenu;
import io.dataease.menu.dao.auto.mapper.CoreMenuRepository;
import io.dataease.menu.manage.MenuManage;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

public final class MenuRegression {
    public static void run(EntityManager em, PlatformTransactionManager manager, CoreMenuRepository menus) throws Exception {
        var tx = new TransactionTemplate(manager);
        tx.executeWithoutResult(status -> {
            CoreMenu system = new CoreMenu();system.setId(15L);system.setPid(0L);system.setType(1);
            system.setName("sys-setting");system.setPath("/sys-setting");system.setMenuSort(6);em.persist(system);
        });
        var migration = new AiRecordsMenuInit(em, manager);
        migration.execute();migration.execute();
        var config = menus.findById(140L).orElseThrow();
        var group = menus.findById(141L).orElseThrow();
        var records = menus.findById(142L).orElseThrow();
        if (menus.count() != 4 || config.getPid() != 141L || !"ai-models".equals(config.getName())
                || !"system/ai-service".equals(config.getComponent()) || !config.getAuth()
                || group.getType() != 1 || group.getAuth() || records.getPid() != 141L || !records.getAuth()) throw new AssertionError("AI menu hierarchy or authorization changed");
        MenuManage manage = new MenuManage();
        var field = MenuManage.class.getDeclaredField("coreMenuRepository");field.setAccessible(true);field.set(manage, menus);
        for (long allowed : new long[]{140L, 142L}) {
            var visible = menus.findAll().stream().filter(menu -> menu.getId() != (allowed == 140L ? 142L : 140L)).toList();
            var tree = manage.query(visible);
            var children = tree.getFirst().getChildren().getFirst().getChildren();
            if (children.size() != 1 || !children.getFirst().getName().equals(allowed == 140L ? "ai-models" : "ai-records")) throw new AssertionError("AI leaf permissions were combined");
        }
        if (!manage.query(menus.findAll().stream().filter(menu -> menu.getId() != 140L && menu.getId() != 142L).toList()).isEmpty()) throw new AssertionError("empty AI directory remains visible");
        tx.executeWithoutResult(status -> {
            var old = em.find(CoreMenu.class, 140L);old.setPid(15L);old.setName("ai-service");old.setPath("/ai-service");
            em.find(CoreMenu.class, 142L).setComponent("unrelated/component");
        });
        try { migration.execute();throw new AssertionError("menu collision accepted"); } catch (IllegalStateException expected) { }
        if (menus.findById(140L).orElseThrow().getPid() != 15L) throw new AssertionError("failed migration partly moved configuration");
        tx.executeWithoutResult(status -> em.find(CoreMenu.class, 142L).setComponent("system/ai-records"));
        migration.execute();
        System.out.println("PASS AI 2.46 menu migration, repeated execution, retained configuration ID, independent leaf visibility and collision rollback");
    }
}
