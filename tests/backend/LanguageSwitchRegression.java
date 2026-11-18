package io.dataease.i18n;

import io.dataease.api.permissions.user.dto.LangSwitchRequest;
import io.dataease.cache.DECacheService;
import io.dataease.constant.StaticResourceConstants;
import io.dataease.exception.DEException;
import io.dataease.substitute.permissions.user.SubstituteUserServer;
import io.dataease.utils.CommonBeanFactory;
import jakarta.validation.Validation;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** Standalone main: temporary language files and an in-memory cache; no application or database. */
public class LanguageSwitchRegression {
    private static int checks;

    @RestControllerAdvice
    public static class Errors {
        @ExceptionHandler(DEException.class)
        public ResponseEntity<Map<String, String>> invalid(DEException error) {
            return ResponseEntity.badRequest().body(Map.of("message", error.getMessage()));
        }
    }

    private static void check(boolean condition) {
        checks++;
        if (!condition) {
            throw new AssertionError("Language assertion " + checks);
        }
    }

    private static void reject(String language) {
        try {
            LanguageUtils.requireSupported(language);
            throw new AssertionError("Invalid language accepted");
        } catch (DEException expected) {
            check(true);
        }
    }

    public static void main(String[] args) throws Exception {
        StaticMessageSource messages = new StaticMessageSource();
        messages.addMessage("i18n_language_required", Locale.getDefault(), "Language is required");
        messages.addMessage("i18n_language_unsupported", Locale.getDefault(), "Unsupported language");
        new Translator().setMessageSource(messages);
        String originalDirectory = StaticResourceConstants.I18N_DIR;
        Path directory = Files.createTempDirectory("language-switch-");
        List<String> writes = new ArrayList<>();
        Object cache = Proxy.newProxyInstance(DECacheService.class.getClassLoader(), new Class[]{DECacheService.class},
                (proxy, method, values) -> {
                    if (method.getName().equals("put")) {
                        writes.add((String) values[2]);
                    }
                    return null;
                });
        try (StaticApplicationContext context = new StaticApplicationContext();
             var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            context.getBeanFactory().registerSingleton("dECacheService", cache);
            new CommonBeanFactory().setApplicationContext(context);
            StaticResourceConstants.I18N_DIR = directory.toString();
            for (String invalid : new String[]{null, "", " ", "\t\n", "removed", "en-invalid"}) {
                reject(invalid);
                if (invalid == null || invalid.isBlank()) {
                    LangSwitchRequest request = new LangSwitchRequest();
                    request.setLang(invalid);
                    check(!validatorFactory.getValidator().validate(request).isEmpty());
                }
            }
            for (String[] pair : new String[][]{{" zh_CN ", "zh-CN"}, {"EN_us", "en"}, {"zh_TW", "tw"}, {"tw", "tw"}}) {
                check(pair[1].equals(LanguageUtils.requireSupported(pair[0])));
            }
            reject("fr");
            Files.writeString(directory.resolve("custom_fr_front_French.js"), "export default {};");
            Files.writeString(directory.resolve("custom_en_US_front_US.js"), "export default {};");
            Files.writeString(directory.resolve("README.txt"), "ignored");
            Files.createDirectory(directory.resolve("custom_invalid_front_Directory.js"));
            check(LanguageUtils.customOptions().size() == 2);
            check("fr".equals(LanguageUtils.requireSupported(" FR ")));
            check("en-US".equals(LanguageUtils.requireSupported("en_US")));
            Files.delete(directory.resolve("custom_fr_front_French.js"));
            reject("fr");
            MockMvc mvc = MockMvcBuilders.standaloneSetup(new SubstituteUserServer())
                    .setControllerAdvice(new Errors()).build();
            for (String body : List.of("null", "{}", "{\"lang\":null}", "{\"lang\":\"\"}",
                    "{\"lang\":\"   \"}", "{\"lang\":\"removed\"}")) {
                int status = mvc.perform(post("/user/switchLanguage").contentType(MediaType.APPLICATION_JSON)
                        .content(body)).andReturn().getResponse().getStatus();
                check(status == 400);
                check(writes.isEmpty());
            }
            check(mvc.perform(post("/user/switchLanguage").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"lang\":\" zh_CN \"}")).andReturn().getResponse().getStatus() == 200);
            check(writes.equals(List.of("zh-CN")));
            Files.writeString(directory.resolve("custom_x_company_front_Company.js"), "export default {};");
            check(mvc.perform(post("/user/switchLanguage").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"lang\":\" X_company \"}")).andReturn().getResponse().getStatus() == 200);
            check(writes.equals(List.of("zh-CN", "x-company")));
            Files.delete(directory.resolve("custom_x_company_front_Company.js"));
            check(mvc.perform(post("/user/switchLanguage").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"lang\":\"x-company\"}")).andReturn().getResponse().getStatus() == 400);
            check(writes.equals(List.of("zh-CN", "x-company")));
            System.out.println("LanguageSwitchRegression: " + checks + " checks passed");
        } finally {
            StaticResourceConstants.I18N_DIR = originalDirectory;
            try (var files = Files.walk(directory)) {
                for (Path file : files.sorted(java.util.Comparator.reverseOrder()).toList()) {
                    Files.delete(file);
                }
            }
        }
    }
}
