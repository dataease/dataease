package io.dataease.share.manage;

import com.auth0.jwt.JWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.auth.DeLinkPermit;
import io.dataease.utils.WhitelistUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.server.PathContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.Set;
import io.dataease.permission.model.LinkIdentity;
import io.dataease.permission.template.LinkTokenVerifier;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.share.dao.auto.mapper.XpackShareRepository;
import io.dataease.share.util.LinkTokenUtil;
import io.dataease.system.manage.SysParameterManage;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class LinkTokenValidationManage implements LinkTokenVerifier {
    @Resource
    private XpackShareRepository shares;
    @Resource
    private ShareSecretManage secrets;
    @Resource
    private DataVisualizationInfoRepository visualizations;
    @Resource
    private SysParameterManage parameters;

    @Override
    public LinkIdentity verify(String token) {
        // Unverified shareId is only a lookup key; no identity is established before verification.
        Long id = JWT.decode(token).getClaim("shareId").asLong();
        if (id == null) throw new IllegalArgumentException("Invalid link token");
        var share = shares.findById(id).orElseThrow(() -> new IllegalArgumentException("Share unavailable"));
        LinkTokenUtil.verify(token, share, secrets.getDefaultPwd());
        var settings = parameters.shareBase();
        if (settings != null && (settings.isDisable() || (settings.isPeRequire()
                && (share.getPwd() == null || share.getPwd().isBlank() || share.getExp() == null || share.getExp() <= 0)))) {
            throw new IllegalArgumentException("Share unavailable");
        }
        var resource = visualizations.findById(share.getResourceId())
                .orElseThrow(() -> new IllegalArgumentException("Share resource unavailable"));
        if (Boolean.TRUE.equals(resource.getDeleteFlag())) throw new IllegalArgumentException("Share resource unavailable");
        var user = V3UserUtil.getUser(share.getCreator());
        if (user == null || !Boolean.TRUE.equals(user.getEnable())) throw new IllegalArgumentException("Share owner unavailable");
        return new LinkIdentity(share.getId(), share.getCreator(), share.getResourceId(), share.getOid());
    }

    @Lazy
    @Resource(name = "requestMappingHandlerMapping")
    private RequestMappingHandlerMapping mappings;

    @Override
    public void checkRequestPath(String uri, String method) {
        if (uri == null || method == null || !(method.equalsIgnoreCase("GET") || method.equalsIgnoreCase("POST"))) {
            throw new IllegalArgumentException("Unsupported share request");
        }
        String path = uri.split("\\?", 2)[0];
        String context = WhitelistUtils.getContextPath();
        if (context != null && !context.isBlank() && path.startsWith(context + "/")) path = path.substring(context.length());
        if (path.startsWith("/de2api/")) path = path.substring("/de2api".length());
        if (Set.of("/user/ipInfo", "/datasetData/enumValue", "/datasetData/enumValueObj",
                "/datasetData/getFieldTree", "/dekey", "/symmetricKey", "/share/validate",
                "/sysParameter/queryOnlineMap", "/xpackComponent/viewPlugins").contains(path)) return;
        if (path.startsWith("/static-resource/") && method.equalsIgnoreCase("GET")) {
            checkStaticResource(V3UserUtil.getLink(), path);
            return;
        }
        if (method.equalsIgnoreCase("GET") && (path.equals("/customGeo/geoArea/list")
                || path.matches("/customGeo/geoArea/[0-9]+"))) return;
        String typePrefix = "/dataVisualization/findDvType/";
        if (method.equalsIgnoreCase("GET") && path.startsWith(typePrefix)) {
            if (V3UserUtil.getLink() != null && path.substring(typePrefix.length())
                    .equals(V3UserUtil.getLink().resourceId().toString())) return;
            throw new IllegalArgumentException("Resource outside share scope");
        }
        var parser = new PathPatternParser();
        var requestPath = PathContainer.parsePath(path);
        for (var entry : mappings.getHandlerMethods().entrySet()) {
            var methods = entry.getKey().getMethodsCondition().getMethods();
            if (!methods.isEmpty() && methods.stream().noneMatch(m -> m.name().equalsIgnoreCase(method))) continue;
            if (!entry.getValue().hasMethodAnnotation(DeLinkPermit.class)) continue;
            if (entry.getKey().getPatternValues().stream().anyMatch(pattern -> parser
                    .parse(pattern.startsWith("/de2api/") ? pattern.substring(7) : pattern).matches(requestPath))) return;
        }
        throw new IllegalArgumentException("Endpoint outside share scope");
    }

    @Override
    public void checkStaticResource(LinkIdentity identity, String uri) {
        var resource = visualizations.findById(identity.resourceId())
                .orElseThrow(() -> new IllegalArgumentException("Share resource unavailable"));
        String prefix = "/static-resource/";
        int index = uri.indexOf(prefix);
        if (index < 0) throw new IllegalArgumentException("Invalid resource path");
        String path = uri.substring(index);
        String file = path.substring(prefix.length());
        if (file.isBlank() || file.contains("/") || file.contains("\\") || file.contains("%") || file.contains("..")) {
            throw new IllegalArgumentException("Invalid resource path");
        }
        if (!containsResource(resource.getComponentData(), path) && !containsResource(resource.getCanvasStyleData(), path)) {
            throw new IllegalArgumentException("Resource outside share");
        }
    }

    private boolean containsResource(String json, String path) {
        if (json == null) return false;
        try {
            return containsResource(new ObjectMapper().readTree(json), path);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean containsResource(JsonNode node, String path) {
        if (node.isTextual()) {
            String value = node.asText();
            int index = value.indexOf("/static-resource/");
            if (index < 0) return false;
            String resource = value.substring(index).split("[?#]", 2)[0];
            return resource.equals(path);
        }
        for (var child : node) if (containsResource(child, path)) return true;
        return false;
    }
}
