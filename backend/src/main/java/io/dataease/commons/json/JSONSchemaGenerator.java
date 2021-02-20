package io.dataease.commons.json;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.processors.syntax.SyntaxValidator;
import com.google.gson.*;
import io.dataease.commons.utils.ScriptEngineUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

public class JSONSchemaGenerator {

    private static void generator(String json, JSONObject obj) {
        analyzeSchema(json, obj);
    }

    private static void analyzeSchema(String json, JSONObject rootObj) {
        // Let's start with the root element of the file
        JsonObject rootElement = null;
        try {
            JsonParser jsonParser = new JsonParser();
            JsonElement inputElement = jsonParser.parse(json);
            rootElement = inputElement.getAsJsonObject();
        } catch (Exception e) {
            e.printStackTrace();
        }

        analyzeRootSchemaElement(rootElement, rootObj);
    }

    private static void analyzeRootSchemaElement(JsonObject rootElement, JSONObject rootObj) {
        if (rootElement.has("type") || rootElement.has("allOf")) {
            analyzeObject(rootElement, rootObj);
        }

        if (rootElement.has("definitions")) {
            // Section 9 in json-validation
            analyzeDefinitions(rootElement);
        }
    }

    private static void analyzeObject(JsonObject object, JSONObject rootObj) {
        // Creating the concept

        if (object.has("title")) {
            // 暂不处理，后续使用时再加
            String title = object.get("title").getAsString();
        }

        if (object.has("description")) {
            // 暂不处理，后续使用时再加
            String description = object.get("description").getAsString();
        }

        if (object.has("allOf")) {
            JsonArray allOfArray = object.get("allOf").getAsJsonArray();
            for (JsonElement allOfElement : allOfArray) {
                JsonObject allOfElementObj = allOfElement.getAsJsonObject();
                if (allOfElementObj.has("$ref")) {
                    // 暂不处理，后续使用时再加
                    String ref = allOfElementObj.get("$ref").getAsString();
                } else if (allOfElementObj.has("properties")) {
                    // Properties elements will become the attributes/references of the element
                    JsonObject propertiesObj = allOfElementObj.get("properties").getAsJsonObject();
                    for (Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
                        String propertyKey = entry.getKey();
                        JsonObject propertyObj = propertiesObj.get(propertyKey).getAsJsonObject();
                        analyzeProperty(rootObj, propertyKey, propertyObj);
                    }
                }
            }
        } else if (object.has("oneOf")) {
            // 暂不处理，后续使用时再加
        } else if (object.has("properties")) {
            JsonObject propertiesObj = object.get("properties").getAsJsonObject();
            for (Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
                String propertyKey = entry.getKey();
                JsonObject propertyObj = propertiesObj.get(propertyKey).getAsJsonObject();
                analyzeProperty(rootObj, propertyKey, propertyObj);
            }
        } else if (object.has("type") && !object.get("type").getAsString().equals("object")) {
            analyzeProperty(rootObj, object.getAsString(), object);
        }

        if (object.has("required")) {
            // 必选项暂不处理，后续使用时再加
        }
    }

    private static void analyzeProperty(JSONObject concept, String propertyName, JsonObject object) {
        if (object.has("type")) {
            String propertyObjType = null;
            if (object.get("type") instanceof JsonPrimitive) {
                propertyObjType = object.get("type").getAsString();
            } else if (object.get("type") instanceof JsonArray) {
                JsonArray typeArray = (JsonArray) object.get("type").getAsJsonArray();
                propertyObjType = typeArray.get(0).getAsString();
                if (typeArray.size() > 1) {
                    if (typeArray.get(1).getAsString().equals("null")) {
                        // 暂不处理，后续使用时再加
                    }
                }
            }

            if (object.has("enum")) {
                concept.put(propertyName, analyzeEnumProperty(object));
            } else if (propertyObjType.equals("string")) {
                // 先设置空值
                concept.put(propertyName, null);
                if (object.has("format")) {
                    String propertyFormat = object.get("format").getAsString();
                    if (propertyFormat.equals("date-time")) {

                    }
                }
                if (object.has("default")) {
                    concept.put(propertyName, object.get("default"));
                }
                if (object.has("mock") && object.get("mock").getAsJsonObject() != null && StringUtils.isNotEmpty(object.get("mock").getAsJsonObject().get("mock").getAsString()) && StringUtils.isNotEmpty(object.get("mock").getAsJsonObject().get("mock").getAsString())) {
                    String value = ScriptEngineUtils.calculate(object.get("mock").getAsJsonObject().get("mock").getAsString());
                    concept.put(propertyName, value);
                }
                if (object.has("maxLength")) {

                }
                // Section 6.3.1 in json-schema-validation. Resolved as OCL

                if (object.has("minLength")) {

                }
                // Section 6.3.2 in json-schema-validation. Resolved as OCL

                if (object.has("pattern")) {
                    // Section 6.3.3 in json-schema-validation. Resolved as OCL, possible?
                    // TODO 6.3.3 in json-schema-validation
                }

            } else if (propertyObjType.equals("integer") || propertyObjType.equals("number")) {
                // 先设置空值
                concept.put(propertyName, 0);
                if (object.has("default")) {
                    concept.put(propertyName, object.get("default"));
                }
                if (object.has("mock") && object.get("mock").getAsJsonObject() != null && StringUtils.isNotEmpty(object.get("mock").getAsJsonObject().get("mock").getAsString())) {
                    String value = ScriptEngineUtils.calculate(object.get("mock").getAsJsonObject().get("mock").toString());
                    concept.put(propertyName, value);
                }
                if (object.has("multipleOf")) {

                }
                // Section 6.2.1 in json-schema-validation. Resolved as OCL

                if (object.has("maximum")) {

                }
                // Section 6.2.2 in json-schema-validation. Resolved as OCL

                if (object.has("exclusiveMaximum")) {

                }
                // Section 6.2.3 in json-schema-validation. Resolved as OCL

                if (object.has("minimum")) {

                }
                // Section 6.2.4 in json-schema-validation. Resolved as OCL

                if (object.has("exclusiveMinimum")) {

                }
                // Section 6.2.5 in json-schema-validation. Resolved as OCL

            } else if (propertyObjType.equals("boolean")) {
                // 先设置空值
                concept.put(propertyName, false);
                if (object.has("default")) {
                    concept.put(propertyName, object.get("default"));
                }
                if (object.has("mock") && object.get("mock").getAsJsonObject() != null && StringUtils.isNotEmpty(object.get("mock").getAsJsonObject().get("mock").getAsString())) {
                    String value = ScriptEngineUtils.calculate(object.get("mock").getAsJsonObject().get("mock").toString());
                    concept.put(propertyName, value);
                }
            } else if (propertyObjType.equals("array")) {
                // 先设置空值
                List<Object> array = new LinkedList<>();

                JsonObject itemsObject = null;
                if (object.has("items") && object.get("items").isJsonArray()) {
                    itemsObject = object.get("items").getAsJsonArray().get(0).getAsJsonObject();
                } else {
                    itemsObject = object.get("items").getAsJsonObject();
                }

                if (object.has("items")) {
                    if (itemsObject.has("enum")) {
                        array.add(analyzeEnumProperty(object));
                    } else if (itemsObject.has("type") && itemsObject.get("type").getAsString().equals("string")) {
                        if (object.has("default")) {
                            array.add(object.get("default"));
                        } else if (object.has("mock") && object.get("mock").getAsJsonObject() != null && StringUtils.isNotEmpty(object.get("mock").getAsJsonObject().get("mock").getAsString())) {
                            String value = ScriptEngineUtils.calculate(object.get("mock").getAsJsonObject().get("mock").toString());
                            array.add(object.get(value));
                        } else {
                            array.add(null);
                        }
                    } else if (itemsObject.has("type") && itemsObject.get("type").getAsString().equals("number")) {
                        if (object.has("default")) {
                            array.add(object.get("default"));
                        } else if (object.has("mock") && object.get("mock").getAsJsonObject() != null && StringUtils.isNotEmpty(object.get("mock").getAsJsonObject().get("mock").getAsString())) {
                            String value = ScriptEngineUtils.calculate(object.get("mock").getAsJsonObject().get("mock").toString());
                            array.add(object.get(value));
                        } else {
                            array.add(0);
                        }
                    } else if (itemsObject.has("oneOf")) {

                    } else if (itemsObject.has("anyOf")) {

                    } else if (itemsObject.has("allOf")) {
                        // TODO
                    } else if (itemsObject.has("properties")) {
                        JSONObject propertyConcept = new JSONObject();
                        JsonObject propertiesObj = itemsObject.get("properties").getAsJsonObject();
                        for (Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
                            String propertyKey = entry.getKey();
                            JsonObject propertyObj = propertiesObj.get(propertyKey).getAsJsonObject();
                            analyzeProperty(propertyConcept, propertyKey, propertyObj);
                        }
                        array.add(propertyConcept);

                    } else if (itemsObject.has("$ref")) {
                        analyzeRef(concept, propertyName, itemsObject);
                    }
                } else if (object.has("items") && object.get("items").isJsonArray()) {
                    JsonArray itemsObjectArray = object.get("items").getAsJsonArray();
                    array.add(itemsObjectArray);
                }

                concept.put(propertyName, array);

            } else if (propertyObjType.equals("object")) {
                JSONObject obj = new JSONObject();
                concept.put(propertyName, obj);
                analyzeObject(object, obj);
            }
        } else if (object.has("$ref")) {
            analyzeRef(concept, propertyName, object);
        } else if (object.has("oneOf")) {
            // Section 6.7.3 in json-schema-validation

        } else if (object.has("anyOf")) {
            // Section 6.7.2 in json-schema-validation

        }
    }

    private static List<Object> analyzeEnumProperty(JsonObject object) {
        JsonArray enumValues = object.get("enum").getAsJsonArray();
        List<Object> list = new LinkedList<>();
        for (JsonElement enumValueElem : enumValues) {
            String enumValue = enumValueElem.getAsString();
            list.add(enumValue);
        }
        return list;
    }

    private static void analyzeRef(JSONObject concept, String propertyName, JsonObject object) {
        String ref = object.get("$ref").getAsString();
    }

    private static void analyzeDefinitions(JsonObject object) {
        JsonObject definitionsObj = object.get("definitions").getAsJsonObject();
        for (Entry<String, JsonElement> entry : definitionsObj.entrySet()) {
            String definitionKey = entry.getKey();
            JsonObject definitionObj = definitionsObj.get(definitionKey).getAsJsonObject();
            JSONObject obj = new JSONObject();
            analyzeRootSchemaElement(definitionObj, obj);
        }
    }

    private static final SyntaxValidator VALIDATOR = new SyntaxValidator(ValidationConfiguration.byDefault());

    public static String getJson(String jsonSchema) {
        if (StringUtils.isEmpty(jsonSchema)) {
            return null;
        }
        try {
            JsonNode jsonNode = JsonLoader.fromString(jsonSchema);
            ProcessingReport report = VALIDATOR.validateSchema(jsonNode);
            if (report.isSuccess()) {
                JSONObject root = new JSONObject();
                generator(jsonSchema, root);
                // 格式化返回
                Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
                return gson.toJson(root);
            } else {
                return report.getExceptionThreshold().toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return ex.getMessage();
        }
    }
}
