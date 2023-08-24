package io.dataease.plugins.common.constants;

public interface PluginParamConstants {

    String getValue();

    public enum Type implements PluginParamConstants {

        PASSWORD("password"),
        TEXT("text"),
        JSON("json");

        private String value;

        Type(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public enum Classify implements PluginParamConstants {
        MAIL("smtp"),
        BASE("base"),
        LDAP("ldap"),
        WECOM("wecom"),
        DINGTALK("dingtalk"),
        LARK("lark"),
        LARKSUITE("larksuite"),
        REGISTRY("registry");

        private String value;

        Classify(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }


    public enum Registry implements PluginParamConstants {
        URL("registry.url"),
        REPO("registry.repo"),
        USERNAME("registry.username"),
        PASSWORD("registry.password");

        private String value;

        Registry(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    public enum I18n implements PluginParamConstants {

        LANGUAGE("i18n.language");

        private String value;

        I18n(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    enum MAIL implements PluginParamConstants{
        SERVER("smtp.host"),
        PORT("smtp.port"),
        ACCOUNT("smtp.account"),
        PASSWORD("smtp.password"),
        SSL("smtp.ssl"),
        TLS("smtp.tls"),
        RECIPIENTS("smtp.recipient");

        private String value;

        private MAIL(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    public enum BASE implements PluginParamConstants {
        URL("base.url"),
        CONCURRENCY("base.concurrency"),
        PROMETHEUS_HOST("base.prometheus.host");

        private String value;

        private BASE(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    public enum LDAP implements PluginParamConstants {
        URL("ldap.url"),
        DN("ldap.dn"),
        PASSWORD("ldap.password"),
        OU("ldap.ou"),
        FILTER("ldap.filter"),
        MAPPING("ldap.mapping"),
        OPEN("ldap.open");

        private String value;

        LDAP(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    public enum WECOM implements PluginParamConstants {
        CORPID("wecom.corpid"),
        AGENTID("wecom.agentid"),
        SECRET("wecom.secret"),
        REDIRECT_URI("wecom.redirectUri"),
        OPEN("wecom.open");

        private String value;

        WECOM(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    public enum DINGTALK implements PluginParamConstants {
        CORPID("dingtalk.corpid"),
        AGENTID("dingtalk.agentid"),
        APPKEY("dingtalk.appKey"),
        APPSECRET("dingtalk.appSecret"),
        REDIRECT_URI("dingtalk.redirectUri"),
        OPEN("dingtalk.open");

        private String value;

        DINGTALK(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    public enum LARK implements PluginParamConstants {
        APPK_ID("lark.app_id"),
        APP_SECRET("lark.app_secret"),
        REDIRECT_URI("lark.redirect_uri"),
        OPEN("lark.open");

        private String value;

        LARK(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    public enum LARKSUITE implements PluginParamConstants {
        APPK_ID("larksuite.app_id"),
        APP_SECRET("larksuite.app_secret"),
        REDIRECT_URI("larksuite.redirect_uri"),
        OPEN("larksuite.open");

        private String value;

        LARKSUITE(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }
    
}
