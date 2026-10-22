package io.dataease.api.material;

import io.dataease.api.material.vo.MaterialBundle;
import java.util.Map;

/** Internal import capability, called inside an already authorized resource import transaction. */
public interface MaterialBundleService {
    void validate(MaterialBundle bundle);
    Map<String, String> restore(MaterialBundle bundle);
}
