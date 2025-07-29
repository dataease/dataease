package io.dataease.traffic.dao.mapper;


import io.dataease.traffic.dao.entity.CoreApiTraffic;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CoreApiTrafficRepository extends JpaRepository<CoreApiTraffic, Long>, JpaSpecificationExecutor<CoreApiTraffic> {

    default Optional<Integer> getAlive(String api) {
        Specification<CoreApiTraffic> spec = (root, query, cb) ->
                cb.equal(root.get("api"), api);
        return findOne(spec).map(CoreApiTraffic::getAlive);
    }

    @Transactional
    default void upgrade(String api) {
        Specification<CoreApiTraffic> spec = (root, query, cb) ->
                cb.equal(root.get("api"), api);
        List<CoreApiTraffic> trafficList = findAll(spec);
        if (!trafficList.isEmpty()) {
            trafficList.forEach(traffic -> {
                traffic.setAlive(traffic.getAlive() + 1);
            });
            saveAllAndFlush(trafficList);
        }
    }

    @Transactional
    default void insert(Long id, String api, int threshold) {
        CoreApiTraffic apiTraffic = new CoreApiTraffic();
        apiTraffic.setId(id);
        apiTraffic.setApi(api);
        apiTraffic.setThreshold(threshold);
        apiTraffic.setAlive(0);
        saveAndFlush(apiTraffic);
    }

    default long apiCount(String api) {
        Specification<CoreApiTraffic> spec = (root, query, cb) ->
                cb.equal(root.get("api"), api);
        return count(spec);
    }

    @Transactional
    default void releaseAlive(String api) {
        Specification<CoreApiTraffic> spec = (root, query, cb) ->
                cb.equal(root.get("api"), api);
        List<CoreApiTraffic> trafficList = findAll(spec);
        if (!trafficList.isEmpty()) {
            trafficList.forEach(traffic -> {
                if (traffic.getAlive() > 0) {
                    traffic.setAlive(traffic.getAlive() - 1);
                }
            });
            saveAllAndFlush(trafficList);
        }
    }

    @Transactional
    default void cleanTraffic() {
        deleteAllInBatch();
    }
}
