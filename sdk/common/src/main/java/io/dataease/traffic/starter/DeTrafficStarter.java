package io.dataease.traffic.starter;

import io.dataease.traffic.dao.mapper.CoreApiTrafficRepository;
import io.dataease.utils.LogUtil;
import jakarta.annotation.Resource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DeTrafficStarter implements ApplicationRunner {

    @Resource
    private CoreApiTrafficRepository coreApiTrafficRepository;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            coreApiTrafficRepository.cleanTraffic();
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), new Throwable(e));
        }
    }
}
