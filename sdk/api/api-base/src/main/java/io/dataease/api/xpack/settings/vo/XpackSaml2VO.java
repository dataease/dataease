package io.dataease.api.xpack.settings.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.X509Certificate;

import java.io.Serializable;
import java.security.PrivateKey;

@Data
public class XpackSaml2VO implements Serializable {

    private String idpMetaUrl;
    private String spEntityId;
    private String spAcs;

    private String idpSsoUrl;
    private String idpEntityId;
    private String idpLogoutUrl;

    private String privateKey;
    private String certificate;

    private String mapping;


    @JsonIgnore
    private PrivateKey spPrivateKey;
    @JsonIgnore
    private BasicX509Credential spCertificate;
    @JsonIgnore
    private X509Certificate idpCertificate;

    private int assertionValidityTime = 300;
    private boolean wantAssertionsSigned = true;
    private boolean wantAuthnRequestsSigned = true;

}
