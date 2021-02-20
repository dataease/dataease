package io.dataease.commons.utils;

import org.apache.commons.io.IOUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.nio.charset.StandardCharsets;

public class ScriptEngineUtils {
    private static final String ENGINE_NAME = "graal.js";
    private static ScriptEngine engine;

    static {
        final ScriptEngineManager engineManager = new ScriptEngineManager();
        engine = engineManager.getEngineByName(ENGINE_NAME);
        try {
            String script = IOUtils.toString(ScriptEngineUtils.class.getResource("/javascript/func.js"), StandardCharsets.UTF_8);
            engine.eval(script);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    public static String calculate(String input) {
        try {
            return engine.eval("calculate('" + input + "')").toString();
        } catch (ScriptException e) {
            LogUtil.error(e.getMessage(), e);
            return input;
        }
    }
}
