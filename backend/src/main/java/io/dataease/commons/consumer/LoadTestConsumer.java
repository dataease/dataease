package io.dataease.commons.consumer;

import com.alibaba.fastjson.JSON;
import io.dataease.Application;
import io.dataease.base.domain.LoadTestReport;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.commons.utils.LogUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.reflections8.Reflections;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class LoadTestConsumer {
    public static final String CONSUME_ID = "load-test-data";

    @KafkaListener(id = CONSUME_ID, topics = "${kafka.test.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<?, String> record) {
        LoadTestReport loadTestReport = JSON.parseObject(record.value(), LoadTestReport.class);
        Reflections reflections = new Reflections(Application.class);
        Set<Class<? extends LoadTestFinishEvent>> subTypes = reflections.getSubTypesOf(LoadTestFinishEvent.class);
        subTypes.forEach(s -> {
            try {
                CommonBeanFactory.getBean(s).execute(loadTestReport);
            } catch (Exception e) {
                LogUtil.error(e);
            }
        });
    }
}
