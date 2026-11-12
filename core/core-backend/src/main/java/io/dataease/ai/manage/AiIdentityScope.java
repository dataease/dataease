package io.dataease.ai.manage;

import io.dataease.ai.support.AiErrors;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.utils.CommunityUtils;
import org.springframework.web.context.request.RequestContextHolder;
import java.util.Objects;
import java.util.function.Supplier;

/** Installs only verified server-owned identity coordinates, never Servlet or transaction state. */
public final class AiIdentityScope {
    private static final ThreadLocal<Integer> authorityDepth = ThreadLocal.withInitial(() -> 0);
    private final AiAccess access;
    public AiIdentityScope(AiAccess access) { this.access = access; }
    public <T> T call(AiAccess.Identity owner, Supplier<T> operation) {
        // 不复制 Servlet 请求/事务。HTTP 传输线程只安装服务器保存的身份，并可靠清理。
        boolean request = RequestContextHolder.getRequestAttributes() != null;
        int depth = authorityDepth.get(); authorityDepth.set(depth + 1);
        Long uid = V3UserUtil.getUid(); var proxy = V3UserUtil.getProxy(); var link = V3UserUtil.getLink();
        boolean community = CommunityUtils.isCommunityMode();
        V3UserUtil.clear(); CommunityUtils.clear(); V3UserUtil.setUid(owner.actor());
        if (owner.proxy() != null) V3UserUtil.setProxy(owner.proxy());
        CommunityUtils.setCommunityMode(!owner.deployment().organizations());
        try {
            access.verify(owner, false);
            if (!Objects.equals(access.capture(), owner)) throw AiErrors.error("forbidden");
            T result = operation.get();
            access.verify(owner, false);
            return result;
        }
        finally {
            V3UserUtil.clear(); CommunityUtils.clear();
            if (depth == 0) authorityDepth.remove(); else authorityDepth.set(depth);
            if (request || depth > 0) {
                CommunityUtils.setCommunityMode(community);
                if (link != null) V3UserUtil.setLink(link);
                else { V3UserUtil.setUid(uid); if (proxy.isProxy()) V3UserUtil.setProxy(proxy.getProxyOid()); }
            }
        }
    }
}
