package io.dataease.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.dataease.exception.DEException;
import io.dataease.result.ResultCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/** HTTP boundary copies only. Persisted entities and internal execution retain their credentials. */
public final class ConfigurationCredentials {
    public static final String MASK = "******";
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Set<String> SECRETS = Set.of("password", "passwd", "pwd", "sshpassword",
            "sshkey", "sshkeypassword", "privatekey", "passphrase", "token", "accesstoken", "refreshtoken",
            "sslkey", "clientkey", "keystorepassword", "secret", "clientsecret", "appsecret", "secretkey", "apikey", "accesskey", "accesssecret",
            "authorization", "proxyauthorization", "cookie", "setcookie");
    private static final Set<String> DESTINATIONS = Set.of("host", "hostname", "port", "feport", "url",
            "jdbc", "jdbcurl", "username", "user", "account", "domain", "sship", "sshhost", "sshport",
            "sshusername", "verification", "authentication", "authtype", "authmethod", "usessh", "usessl",
            "sslmode", "sslverify", "verifyssl", "sslca", "sslcert", "trustservercertificate",
            "endpoint", "server", "address", "hosts", "nodes", "brokers");
    private static final Pattern EMBEDDED = Pattern.compile(
            "(?i)(?:password|passwd|pwd|token|secret|api[_-]?key|authorization)[\"']?\\s*[=:]|://[^/\\s]*:[^/\\s]*@|-----BEGIN[^-]*PRIVATE KEY-----");

    private ConfigurationCredentials() { }

    private static String normalized(String name) {
        return name.replace("-", "").replace("_", "").toLowerCase(Locale.ROOT);
    }

    private static boolean secret(String name) {
        String key = normalized(name);
        // 飞书多维表格的 appToken 不是凭证，仅用于定位多维表格应用，不参与服务端鉴权。
        if ("apptoken".equals(key)) {
            return false;
        }
        return SECRETS.contains(key) || key.endsWith("password") || key.endsWith("secret") || key.endsWith("token") || key.endsWith("apikey");
    }

    public static JsonNode mask(JsonNode value) {
        return mask(value, "");
    }

    private static JsonNode mask(JsonNode value, String field) {
        if (value == null || value.isNull()) return value;
        if (value.isTextual() && Set.of("configuration", "paramsStr", "apiConfigurationStr", "parameter").contains(field)) {
            try { return TextNode.valueOf(mask(JSON.readTree(value.textValue())).toString()); }
            catch (Exception e) { return TextNode.valueOf(MASK); }
        }

        if (secret(field) || value.isTextual() && EMBEDDED.matcher(value.textValue()).find()) {
            return value.isTextual() && value.textValue().isEmpty() ? value : TextNode.valueOf(MASK);
        }
        if (value.isObject()) {
            ObjectNode copy = ((ObjectNode) value).deepCopy();
            List<String> names = new ArrayList<>();
            copy.fieldNames().forEachRemaining(names::add);
            for (String name : names) copy.set(name, mask(copy.get(name), name));
            // API headers/parameters are also represented as {name|key, value} entries.
            String name = value.path("name").asText(value.path("key").asText(
                    value.path("parameterName").asText(value.path("requestParameterName").asText())));
            if (secret(name)) {
                for (String valueField : List.of("value", "parameterDefaultValue", "defaultValue")) {
                    JsonNode secretValue = copy.get(valueField);
                    if (secretValue != null && !secretValue.isNull()
                            && !(secretValue.isTextual() && secretValue.textValue().isEmpty())) {
                        copy.put(valueField, MASK);
                    }
                }
            }
            return copy;
        }
        if (value.isArray()) {
            var copy = JSON.createArrayNode();
            value.forEach(item -> copy.add(mask(item, "")));
            return copy;
        }
        // Configuration DTOs serialize these JSON documents as strings.
        return value.deepCopy();
    }

    public static String restore(String requested, String stored) {
        try {
            JsonNode value = JSON.readTree(requested);
            JsonNode original = stored == null ? null : JSON.readTree(stored);
            if (value == null || !value.isContainerNode()) throw invalid();
            boolean[] kept = {false};
            JsonNode result = restoreNode(value, original, mask(original), "", kept);
            if (kept[0] && !destinations(result).equals(destinations(original))) throw invalid();
            return result.toString();
        } catch (DEException e) { throw e; }
        catch (Exception e) { throw invalid(); }
    }

    private static JsonNode restoreNode(JsonNode value, JsonNode original, JsonNode maskedOriginal,
                                        String field, boolean[] kept) {
        if (value.isTextual() && MASK.equals(value.textValue())) {
            if (maskedOriginal != null && maskedOriginal.isTextual() && MASK.equals(maskedOriginal.textValue())) {
                if (original == null || original.isNull()) throw invalid();
                kept[0] = true;
                return original.deepCopy();
            }
            if (secret(field)) throw invalid();
            return value.deepCopy();
        }
        if (value.isObject()) {
            ObjectNode result = JSON.createObjectNode();
            String label = value.path("name").asText(value.path("key").asText(
                    value.path("parameterName").asText(value.path("requestParameterName").asText())));
            value.fields().forEachRemaining(entry -> result.set(entry.getKey(), restoreNode(entry.getValue(),
                    original == null ? null : original.get(entry.getKey()),
                    maskedOriginal == null ? null : maskedOriginal.get(entry.getKey()),
                    secret(label) && List.of("value", "parameterDefaultValue", "defaultValue").contains(entry.getKey())
                            ? "password" : entry.getKey(), kept)));
            return result;
        }
        if (value.isArray()) {
            var result = JSON.createArrayNode();
            for (int i = 0; i < value.size(); i++) {
                JsonNode item = value.get(i);
                JsonNode previous = original != null && original.isArray() && i < original.size() ? original.get(i) : null;
                boolean[] itemKept = {false};
                JsonNode restored = restoreNode(item, previous, maskedOriginal != null && maskedOriginal.isArray()
                        && i < maskedOriginal.size() ? maskedOriginal.get(i) : null, "", itemKept);
                // An entry must not borrow credentials from another array element after a reorder.
                if (itemKept[0] && item.isObject()) {
                    for (String id : List.of("serialNumber", "id", "name", "key")) {
                        if (item.hasNonNull(id) && (previous == null || !item.get(id).equals(previous.get(id)))) throw invalid();
                    }
                }
                kept[0] |= itemKept[0];
                result.add(restored);
            }
            return result;
        }
        return value.deepCopy();
    }

    public static boolean containsMask(JsonNode value) {
        if (value == null) return false;
        if (value.isTextual()) return MASK.equals(value.textValue());
        for (JsonNode child : value) if (containsMask(child)) return true;
        return false;
    }

    private static java.util.Map<String, String> destinations(JsonNode value) {
        var result = new java.util.TreeMap<String, String>();
        collectDestinations(value, "", result);
        return result;
    }

    private static void collectDestinations(JsonNode value, String path, java.util.Map<String, String> out) {
        if (value == null) return;
        if (value.isObject()) value.fields().forEachRemaining(entry -> {
            String next = path + "/" + entry.getKey();
            if (DESTINATIONS.contains(normalized(entry.getKey()))) out.put(next, entry.getValue().toString());
            else if (!secret(entry.getKey())) collectDestinations(entry.getValue(), next, out);
        });
        else if (value.isArray()) {
            for (int i = 0; i < value.size(); i++) collectDestinations(value.get(i), path + "/" + i, out);
        }
    }

    private static DEException invalid() {
        return new DEException(ResultCode.PARAM_IS_INVALID.code(),
                io.dataease.i18n.Translator.get("i18n_configuration_credentials_reenter"));
    }
}
