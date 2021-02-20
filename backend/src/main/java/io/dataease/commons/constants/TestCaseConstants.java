package io.dataease.commons.constants;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TestCaseConstants {

    public static final int MAX_NODE_DEPTH = 8;

    public enum Type {
        Functional("functional"), Performance("performance"), Aapi("api");

        private String value;

        Type(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        public static List<String> getValues() {
            List<Type> types = Arrays.asList(Type.values());
            return  types.stream().map(Type::getValue).collect(Collectors.toList());
        }
    }

    public enum Method {
        Manual("manual"), Auto("auto");

        private String value;

        Method(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        public static List<String> getValues() {
            List<Method> types = Arrays.asList(Method.values());
            return  types.stream().map(Method::getValue).collect(Collectors.toList());
        }
    }
}
