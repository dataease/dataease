package io.dataease.ai;

import io.dataease.ai.support.AiErrors;
import io.dataease.ai.support.AiOutboundPolicy;

public final class OutboundRegression {
    public static void run() throws Exception {
        AiOutboundPolicy policy = new AiOutboundPolicy();
        String[] addresses = {
                "https://ai.fit2cloud.cn/gateway/v1", "https://another-provider.example/v1",
                "http://models.internal:8000/v1", "http://10.0.0.8:11434/v1",
                "http://127.0.0.1:11434/v1", "http://100.100.100.200/v1",
                "http://198.18.0.48:8000/v1", "http://[2001:2::31]:8000/v1",
                "http://[fd00::1]:65535/v1", "http://[::1]:1/v1",
                "https://model.invalid/gateway%20one/v1"
        };
        for (String address : addresses) {
            if (!address.equals(policy.normalize(address))) throw new AssertionError("valid configured base URL rejected");
        }
        if (!"https://ai.fit2cloud.cn/gateway/v1".equals(policy.normalize(" HTTPS://AI.FIT2CLOUD.CN/gateway/v1/ "))) {
            throw new AssertionError("base URL normalization");
        }
        if (!"/models".equals(policy.discoveryPath(addresses[0]))
                || !"/chat/completions".equals(policy.completionPath(addresses[0]))) throw new AssertionError("gateway prefix changed");
        for (String address : new String[]{null, "", "ftp://model/v1", "file:///etc/passwd", "https:///v1",
                "http://host:0/v1", "http://host:65536/v1", "http://host:-1/v1", "http://host:/v1",
                "https://user:password@host/v1", "https://host/v1?key=synthetic", "https://host/v1#secret",
                "https://host/bad%path", "https://host/has space"}) {
            try {
                policy.normalize(address);
                throw new AssertionError("invalid base URL accepted");
            } catch (AiErrors.Failure expected) {
                if (!"address_invalid".equals(expected.safeCode())) throw new AssertionError("incorrect format error");
            }
        }
        // 使用 IP 字面量验证真实解析结果，无外部 DNS 或网络依赖。
        for (String host : new String[]{"198.18.0.48", "2001:2::31", "10.0.0.8", "fd00::1", "127.0.0.1", "::1"}) {
            if (policy.resolve(host).length == 0) throw new AssertionError("private or proxy address denied");
        }
        System.out.println("PASS AI configurable public/private/loopback/IPv6/Fake-IP URLs and ports, gateway paths, no DNS during validation, invalid formats rejected");
    }
}
