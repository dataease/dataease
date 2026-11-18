package io.dataease.config;

import io.dataease.license.bo.F2CLicResult;
import io.dataease.license.utils.CommonBeanFactory;
import io.dataease.license.utils.LicenseUtil;
import org.springframework.context.support.StaticApplicationContext;

import static org.mockito.Mockito.mockStatic;

final class MaterialTestLicense implements AutoCloseable {
    private final F2CLicResult previous;

    MaterialTestLicense() {
        try (var context = new StaticApplicationContext();
             var beans = mockStatic(CommonBeanFactory.class)) {
            context.refresh();
            beans.when(CommonBeanFactory::getApplicationContext).thenReturn(context);
            previous = LicenseUtil.get();
        }
        var validLicense = new F2CLicResult();
        validLicense.setStatus(F2CLicResult.Status.valid);
        LicenseUtil.set(validLicense);
    }

    @Override
    public void close() {
        LicenseUtil.set(previous);
    }
}
