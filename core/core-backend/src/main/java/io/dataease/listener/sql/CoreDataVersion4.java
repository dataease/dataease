package io.dataease.listener.sql;

import io.dataease.initSql.Version;
import io.dataease.map.dao.auto.entity.Area;
import io.dataease.map.dao.auto.mapper.AreaRepository;
import io.dataease.map.dao.ext.entity.CoreAreaCustom;
import io.dataease.map.dao.ext.mapper.CoreAreaCustomRepository;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static io.dataease.constant.CacheConstant.CommonCacheConstant.WORLD_MAP_CACHE;

@Component
public class CoreDataVersion4 implements CoreSqlBlock {

    @Resource
    private AreaRepository areaRepository;

    @Resource
    private CoreAreaCustomRepository coreAreaCustomRepository;

    @Override
    public Version getVersion() {
        return new Version("2.43");
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = WORLD_MAP_CACHE, key = "'world_map'")
    public void execute() {
        addAreaIfMissing("156710000", "province", "台湾省", "156");
        migrateDistrict("156330103", "156330113", "临平区");
        migrateDistrict("156330104", "156330114", "钱塘区");
    }

    /**
     * 已存在的区域保持原样，兼容新安装、V2 升级及失败后的重试
     */
    private void addAreaIfMissing(String id, String level, String name, String pid) {
        if (areaRepository.existsById(id)) {
            return;
        }
        Area area = new Area();
        area.setId(id);
        area.setLevel(level);
        area.setName(name);
        area.setPid(pid);
        areaRepository.save(area);
    }

    /**
     * 先迁移子节点再移除旧目录，避免已上传地图脱离区域树
     */
    private void migrateDistrict(String oldId, String newId, String name) {
        addAreaIfMissing(newId, "district", name, "156330100");
        areaRepository.updatePid(newId, oldId);
        Specification<CoreAreaCustom> spec = (root, query, cb) -> cb.equal(root.get("pid"), oldId);
        List<CoreAreaCustom> children = coreAreaCustomRepository.findAll(spec);
        children.forEach(child -> child.setPid(newId));
        coreAreaCustomRepository.saveAll(children);
        areaRepository.deleteById(oldId);
    }
}
