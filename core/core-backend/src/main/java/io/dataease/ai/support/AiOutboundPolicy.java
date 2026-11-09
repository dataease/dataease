package io.dataease.ai.support;

import org.springframework.stereotype.Component;
import java.net.*;
import java.util.*;

/** 管理员配置的 HTTP(S) 基础地址；网络可达性由实际连接判断。 */
@Component
public class AiOutboundPolicy {
    public String normalize(String address) {
        try {
            if (address == null || address.length() > 2048) throw new IllegalArgumentException();
            URI uri = URI.create(address.trim());
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if (!("https".equals(scheme) || "http".equals(scheme)) || uri.getHost() == null
                    || uri.getRawUserInfo() != null || uri.getRawQuery() != null || uri.getRawFragment() != null) throw new IllegalArgumentException();
            if (uri.getPort() == 0 || uri.getPort() > 65535 || uri.getRawAuthority().endsWith(":")) throw new IllegalArgumentException();
            // 只规范化协议、主机及路径，不在表单校验阶段执行 DNS 或网络请求。
            String result = new URI(scheme + "://" + uri.getRawAuthority().toLowerCase(Locale.ROOT)
                    + (uri.getRawPath() == null ? "" : uri.getRawPath())).normalize().toASCIIString();
            while (result.endsWith("/")) result = result.substring(0, result.length() - 1);
            return result;
        } catch (Exception e) { throw AiErrors.error("address_invalid"); }
    }
    public InetAddress[] resolve(String host) throws UnknownHostException {
        return InetAddress.getAllByName(host);
    }
    public String completionPath(String baseUrl) { return baseUrl.endsWith("/v1") ? "/chat/completions" : "/v1/chat/completions"; }
    public String discoveryPath(String baseUrl) { return baseUrl.endsWith("/v1") ? "/models" : "/v1/models"; }
}
