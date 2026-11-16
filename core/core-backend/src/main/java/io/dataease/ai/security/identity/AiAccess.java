package io.dataease.ai.security.identity;

import io.dataease.ai.support.AiErrors;

import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.permission.util.V3UserUtil;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import java.util.Objects;
import java.util.function.Supplier;

@Component
public class AiAccess {
    public record Identity(Long actor, Long organization, Long proxy, AiDeploymentMode.State deployment) { }
    private final ObjectProvider<InteractiveAuthApi> authorization;
    private final AiDeploymentMode deployment;
    private final ThreadLocal<Identity> verifiedUsage = new ThreadLocal<>();
    public AiAccess(ObjectProvider<InteractiveAuthApi> authorization, AiDeploymentMode deployment) { this.authorization = authorization; this.deployment = deployment; }
    public AiDeploymentMode.State mode() { return deployment.current(); }
    public <T> T inDeploymentScope(Identity owner, Supplier<T> operation) {
        return deployment.inScope(owner.deployment(), operation);
    }
    /** Server-only proof of an accepted foreground use; cannot be constructed from request data. */
    public final class AcceptedUsage {
        private final Identity owner;
        private AcceptedUsage(Identity owner) { this.owner = owner; }
        private AiAccess issuer() { return AiAccess.this; }
    }
    public AcceptedUsage acceptUsage(Identity owner) {
        Identity previous = verifiedUsage.get(); verifiedUsage.remove();
        try {
            // A new question must recheck even when invoked inside an older accepted operation.
            verify(owner, false);
            return new AcceptedUsage(owner);
        } finally {
            if (previous == null) verifiedUsage.remove(); else verifiedUsage.set(previous);
        }
    }
    public <T> T withAcceptedUsage(AcceptedUsage accepted, Supplier<T> operation) {
        if (accepted == null || accepted.issuer() != this) throw AiErrors.error("forbidden");
        Identity previous = verifiedUsage.get(); verifiedUsage.set(accepted.owner);
        try {
            // verify still checks deployment; only the exact accepted usage identity skips storage.
            verify(accepted.owner, false);
            T result = operation.get();
            verify(accepted.owner, false);
            return result;
        } finally {
            if (previous == null) verifiedUsage.remove(); else verifiedUsage.set(previous);
        }
    }
    public Identity capture() {
        if (V3UserUtil.getUid() == null || V3UserUtil.getLink() != null) throw AiErrors.error("forbidden");
        AiDeploymentMode.State state = mode();
        Long proxy = V3UserUtil.getProxy().isProxy() ? V3UserUtil.getProxy().getProxyOid() : null;
        if (!state.organizations() && proxy != null) throw AiErrors.error("forbidden");
        Long org = !state.organizations() ? null : proxy != null ? proxy : V3UserUtil.isSysAdmin() ? null : V3UserUtil.getUser().getOid();
        return new Identity(V3UserUtil.getUid(), org, proxy, state);
    }
    public void verify(Identity identity, boolean configuration) {
        if (identity == null || !Objects.equals(identity.deployment(), mode())) throw AiErrors.error("conflict");
        if (!configuration && identity.equals(verifiedUsage.get())) return;
        InteractiveAuthApi api = authorization.getIfAvailable();
        if (!identity.deployment().organizations()) {
            if (identity.organization() != null || identity.proxy() != null) throw AiErrors.error("forbidden");
            if (api != null) {
                try { if (!Boolean.TRUE.equals(api.checkAiCommunityAllowed(identity.actor()))) throw AiErrors.error("forbidden"); }
                catch (RuntimeException failure) { throw AiErrors.error("forbidden"); }
            } else if (!deployment.substitute() || !Objects.equals(identity.actor(), 1L)) throw AiErrors.error("forbidden");
            return;
        }
        if (api != null) {
            try { if (!Boolean.TRUE.equals(api.checkAiAllowed(identity.actor(), identity.organization(), identity.proxy(), configuration))) throw AiErrors.error("forbidden"); }
            catch (RuntimeException e) { throw AiErrors.error("forbidden"); }
            return;
        }
        throw AiErrors.error("forbidden");
    }
    public void audit(Identity identity, Long targetOrganization) {
        verify(identity, false);
        if (!identity.deployment().organizations()) {
            if (!Objects.equals(identity.actor(), 1L) || identity.organization() != null || identity.proxy() != null || targetOrganization != null)
                throw AiErrors.error("forbidden");
            return;
        }
        InteractiveAuthApi api = authorization.getIfAvailable();
        try {
            if (api == null || !Boolean.TRUE.equals(api.checkAiAuditAccess(identity.actor(), identity.organization(), identity.proxy(), targetOrganization)))
                throw AiErrors.error("forbidden");
        } catch (RuntimeException unavailable) { throw AiErrors.error("forbidden"); }
        verify(identity, false);
    }
    public String identity() { return binding(configurationIdentity()); }
    public Identity configurationIdentity() { Identity identity = capture(); verify(identity, true); return identity; }
    public java.util.List<InteractiveAuthApi.AiOrganization> organizations() {
        Identity identity = capture(); verify(identity, true);
        if (!identity.deployment().organizations()) return null;
        InteractiveAuthApi api = authorization.getIfAvailable();
        if (api == null) throw AiErrors.error("forbidden");
        try { var result = api.aiOrganizations(identity.actor(), identity.organization(), identity.proxy()); verify(identity, true); return result; }
        catch (RuntimeException e) { throw AiErrors.error("forbidden"); }
    }
    public static String binding(Identity identity) { return identity.actor() + ":" + identity.organization() + ":" + identity.proxy() + ":" + identity.deployment().mode() + ":" + identity.deployment().revision(); }
    public void configuration() { configurationIdentity(); }
}
