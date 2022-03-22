package io.dataease.commons.constants;

public interface ParamConstants {

    String getValue();

    enum Type implements ParamConstants {

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

    enum Classify implements ParamConstants {
        MAIL("smtp"),
        BASE("base"),
        LDAP("ldap"),
        UI("ui"),
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

    enum Registry implements ParamConstants {
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

    enum I18n implements ParamConstants {

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

    enum MAIL implements ParamConstants {
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

    enum BASIC implements ParamConstants {
        FRONT_TIME_OUT("basic.frontTimeOut"),
        MSG_TIME_OUT("basic.msgTimeOut"),
        OPEN_HOME_PAGE("ui.openHomePage");

        private String value;

        public String getValue() {
            return this.value;
        }

        private BASIC(String value) {
            this.value = value;
        }
    }

    enum BASE implements ParamConstants {
        URL("base.url");

        private String value;

        private BASE(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    enum LDAP implements ParamConstants {
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
}
