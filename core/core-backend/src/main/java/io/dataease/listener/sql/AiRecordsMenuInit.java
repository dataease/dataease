package io.dataease.listener.sql;

import io.dataease.constant.AuthResourceEnum;
import io.dataease.initSql.Version;
import io.dataease.menu.dao.auto.entity.CoreMenu;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;

/** 增加 AI 服务目录和调用记录入口，保留配置菜单 ID 及其已有授权。 */
@Component
public class AiRecordsMenuInit implements CoreSqlBlock {
    private final EntityManager em;
    private final TransactionTemplate tx;

    public AiRecordsMenuInit(EntityManager em, PlatformTransactionManager manager) {
        this.em = em;
        this.tx = new TransactionTemplate(manager);
    }

    public Version getVersion() { return new Version("2.46"); }

    public void execute() {
        tx.executeWithoutResult(status -> {
            long groupId = AuthResourceEnum.AI_SERVICE.getMenuId();
            CoreMenu config = em.find(CoreMenu.class, AuthResourceEnum.AI_CONFIG.getMenuId());
            if (config != null && !(Objects.equals(config.getType(), 2)
                    && Objects.equals(config.getComponent(), "system/ai-service")
                    && (Objects.equals(config.getName(), "ai-service") || Objects.equals(config.getName(), "ai-models"))
                    && (Objects.equals(config.getPid(), 15L) || Objects.equals(config.getPid(), groupId)))) {
                throw new IllegalStateException("AI configuration menu ID is already in use");
            }
            create(AuthResourceEnum.AI_SERVICE.getMenuId(), 15L, 1, "ai-service-group", "system/ai-service-group", "/ai-service", 30, false, "dv-ai");
            if (config == null) {
                config = create(AuthResourceEnum.AI_CONFIG.getMenuId(), groupId, 2, "ai-models", "system/ai-service", "models", 1, true, null);
            } else {
                config.setPid(groupId);
                config.setName("ai-models");
                config.setPath("models");
                config.setMenuSort(1);
                config.setIcon(null);
            }
            create(AuthResourceEnum.AI_RECORDS.getMenuId(), groupId, 2, "ai-records", "system/ai-records", "records", 2, true, null);
            em.flush();
        });
    }

    private CoreMenu create(long id, long pid, int type, String name, String component, String path,
                            int sort, boolean auth, String icon) {
        CoreMenu menu = em.find(CoreMenu.class, id);
        if (menu != null) {
            if (!Objects.equals(menu.getPid(), pid) || !Objects.equals(menu.getType(), type)
                    || !Objects.equals(menu.getName(), name) || !Objects.equals(menu.getComponent(), component)
                    || !Objects.equals(menu.getPath(), path)) throw new IllegalStateException("AI menu ID is already in use");
            return menu;
        }
        menu = new CoreMenu();
        menu.setId(id); menu.setPid(pid); menu.setType(type); menu.setName(name); menu.setComponent(component);
        menu.setPath(path); menu.setMenuSort(sort); menu.setAuth(auth); menu.setIcon(icon);
        menu.setHidden(false); menu.setInLayout(true);
        em.persist(menu);
        return menu;
    }
}
