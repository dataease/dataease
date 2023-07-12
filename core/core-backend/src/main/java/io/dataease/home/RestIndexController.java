package io.dataease.home;

import io.dataease.utils.CacheUtils;
import io.dataease.utils.IDUtils;
import io.dataease.utils.ModelUtils;
import io.dataease.utils.RsaUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping
public class RestIndexController {

    @GetMapping("/dekey")
    @ResponseBody
    public String dekey() {
        return RsaUtils.publicKey();
    }

    @GetMapping("/model")
    @ResponseBody
    public boolean model() {
        return ModelUtils.isDesktop();
    }

    @GetMapping("/test/{type}")
    @ResponseBody
    public String test(@PathVariable("type") int type) {

        if (type == 0) {
            String randomID = IDUtils.randomID(10);
            CacheUtils.put("test_cache", "1", randomID, 20L, TimeUnit.SECONDS);
            return "add cache " + randomID;
        }
        if (type == 1) {
            Object testCache = CacheUtils.get("test_cache", "1");
            CacheUtils.keyRemove("test_cache", "1");
            CacheUtils.put("test_cache", "1", testCache, 5L, TimeUnit.SECONDS);
            return "edit cache " + testCache.toString();
        }
        if (type == 2) {
            CacheUtils.keyRemove("test_cache", "1");
            return "edit delete";
        }
        if (type == 3) {
            Object testCache = CacheUtils.get("test_cache", "1");
            if (ObjectUtils.isNotEmpty(testCache)) {
                return testCache.toString();
            }
            return "empty";
        }
        return null;
    }
}
