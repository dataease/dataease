package io.dataease.api.xpack.settings.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

@Data
public class XpackSaml2VO implements Serializable {

    private String spEntityId;
    private String spAcs;

    private String idpSsoUrl;
    private String idpEntityId;
    private String idpLogoutUrl;


    @JsonIgnore
    private PrivateKey spPrivateKey;
    @JsonIgnore
    private X509Certificate spCertificate;
    @JsonIgnore
    private X509Certificate idpCertificate;

    private int assertionValidityTime = 300; // 5分钟
    private boolean wantAssertionsSigned = true;
    private boolean wantAuthnRequestsSigned = true;

}
