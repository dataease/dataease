package io.datains.commons.condition;

import io.datains.commons.license.DefaultLicenseService;
import io.datains.commons.license.F2CLicenseResponse;
import io.datains.commons.utils.CommonBeanFactory;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;


public class LicStatusCondition implements Condition {

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {

        DefaultLicenseService defaultLicenseService = CommonBeanFactory.getBean(DefaultLicenseService.class);


        if (ObjectUtils.isNotEmpty(defaultLicenseService)) {
            F2CLicenseResponse f2CLicenseResponse = defaultLicenseService.validateLicense();
            return F2CLicenseResponse.Status.valid == f2CLicenseResponse.getStatus();
        }
        return false;
    }
}
