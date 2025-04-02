package io.dataease.i18n;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Set;
import java.util.stream.Collectors;

public class DeReloadableResourceBundleMessageSource extends ReloadableResourceBundleMessageSource {

    @Override
    public Set<String> getBasenameSet() {
        return super.getBasenameSet().stream().sorted(this::compare).collect(Collectors.toSet());
    }

    private int compare(String o1, String o2) {
        return o1.substring(0, 1).compareTo(o2.substring(0, 1));
    }

}
