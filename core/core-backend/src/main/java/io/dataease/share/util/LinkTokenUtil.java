package io.dataease.share.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.dataease.share.dao.auto.entity.XpackShare;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class LinkTokenUtil {
    private static final long TOKEN_LIFETIME = 8 * 60 * 60 * 1000L;

    public static String generate(XpackShare share, String serverSecret) {
        long now = System.currentTimeMillis();
        long expires = now + TOKEN_LIFETIME;
        if (share.getExp() != null && share.getExp() > 0) expires = Math.min(expires, share.getExp());
        if (expires <= now) throw new IllegalArgumentException("Share expired");
        return JWT.create().withIssuer("dataease").withAudience("share")
                .withClaim("purpose", "link").withClaim("version", 2)
                .withClaim("shareId", share.getId()).withClaim("uid", share.getCreator())
                .withClaim("resourceId", share.getResourceId()).withClaim("oid", share.getOid())
                .withJWTId(UUID.randomUUID().toString()).withIssuedAt(new Date(now))
                .withExpiresAt(new Date(expires)).sign(algorithm(share, serverSecret));
    }

    public static DecodedJWT verify(String token, XpackShare share, String serverSecret) {
        DecodedJWT jwt = JWT.require(algorithm(share, serverSecret)).withIssuer("dataease")
                .withAudience("share").withClaim("purpose", "link").withClaim("version", 2)
                .withClaim("shareId", share.getId()).withClaim("uid", share.getCreator())
                .withClaim("resourceId", share.getResourceId()).build().verify(token);
        if (jwt.getExpiresAt() == null || jwt.getIssuedAt() == null || jwt.getId() == null
                || jwt.getIssuedAt().after(new Date())
                || jwt.getExpiresAt().getTime() - jwt.getIssuedAt().getTime() > TOKEN_LIFETIME
                || !Objects.equals(jwt.getClaim("oid").asLong(), share.getOid())
                || (share.getExp() != null && share.getExp() > 0 && share.getExp() <= System.currentTimeMillis())) {
            throw new IllegalArgumentException("Invalid link token");
        }
        return jwt;
    }

    private static Algorithm algorithm(XpackShare share, String secret) {
        if (secret == null || secret.isBlank()) throw new IllegalStateException("Share secret unavailable");
        // Bind credentials to current settings, so password/URL/permission changes revoke old tokens.
        // jti identifies an issuance; revocation is per share configuration, not a one-time-token cache.
        List<Object> state = java.util.Arrays.asList(share.getId(), share.getCreator(), share.getResourceId(),
                share.getOid(), share.getUuid(), share.getPwd(), share.getExp(), share.getTicketRequire(),
                share.getVisitorPermissions());
        try {
            byte[] data = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsBytes(state);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Algorithm.HMAC256(mac.doFinal(data));
        } catch (Exception e) {
            throw new IllegalStateException("Share signing unavailable", e);
        }
    }
}
