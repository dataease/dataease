package io.dataease.listener.sql;

import io.dataease.constant.AuthResourceEnum;
import io.dataease.initSql.Version;
import io.dataease.menu.dao.auto.entity.CoreMenu;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;

/** 修复早期开发迁移的菜单 ID 冲突；既有 130 权限属于其他菜单，不转移授权。 */
@Component
public class AiConfigurationMenuInit implements CoreSqlBlock {
    private final EntityManager em;
    private final TransactionTemplate tx;

    public AiConfigurationMenuInit(EntityManager em, PlatformTransactionManager manager) {
        this.em = em;
        this.tx = new TransactionTemplate(manager);
    }

    public Version getVersion() { return new Version("2.45"); }

    public void execute() {
        tx.executeWithoutResult(status -> {
            Long menuId = AuthResourceEnum.AI_CONFIG.getMenuId();
            CoreMenu current = em.find(CoreMenu.class, menuId);
            if (current != null && !aiMenu(current)) {
                throw new IllegalStateException("AI configuration menu ID is already in use");
            }
            if (current == null) {
                current = new CoreMenu();
                current.setId(menuId);
                current.setPid(15L);
                current.setType(2);
                current.setName("ai-service");
                current.setComponent("system/ai-service");
                current.setPath("/ai-service");
                current.setMenuSort(30);
                current.setHidden(false);
                current.setInLayout(true);
                current.setAuth(true);
                current.setIcon("dv-ai");
                em.persist(current);
            }
            CoreMenu previous = em.find(CoreMenu.class, 130L);
            if (previous != null && aiMenu(previous)) em.remove(previous);
            em.flush();
        });
    }

    private boolean aiMenu(CoreMenu menu) {
        return Objects.equals(menu.getPid(), 15L)
                && Objects.equals(menu.getType(), 2)
                && Objects.equals(menu.getName(), "ai-service")
                && Objects.equals(menu.getComponent(), "system/ai-service");
    }
}
