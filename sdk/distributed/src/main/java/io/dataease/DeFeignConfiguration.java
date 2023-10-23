package io.dataease;

import io.dataease.rpc.DeFeignRegister;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableFeignClients
@Import(DeFeignRegister.class)
public class DeFeignConfiguration {
}
