package io.dataease.regression;

import io.dataease.exception.*;
import io.dataease.filter.RequestIdFilter;
import io.dataease.result.ResultMessage;
import io.dataease.result.ResultResponseBodyAdvice;
import io.dataease.i18n.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;
import org.slf4j.MDC;
import com.fasterxml.jackson.databind.*;
import java.util.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/** In-process MVC only: no application startup, database or external service. */
public class Regression {
    static int checks;
    static ObjectMapper json = new ObjectMapper();
    static void check(boolean ok, String name) { if (!ok) throw new AssertionError(name); checks++; }
    public static class Form {
        @NotBlank public String name;
    }
    @RestController
    public static class Controller {
        @GetMapping("/failure/{category}") public Object fail(@PathVariable String category) {
            if (category.equals("NPE")) throw new NullPointerException("internal secret class");
            if (category.equals("EXTENSION")) throw new DEException(10005, "extension validation");
            if (category.equals("LEGACY")) throw new DEException(50002, "legacy chart details");
            throw DEException.of(ErrorCategory.valueOf(category), "i18n_error_internal");
        }
        @PostMapping("/form") public Object form(@Valid @RequestBody Form form) { return form; }
        @GetMapping("/parameter") public Object parameter(@RequestParam Long id) { return id; }
        @GetMapping("/trace") public Object trace() { return Map.of("id", MDC.get("requestId")); }
    }
    public static void main(String[] args) throws Exception {
        var messages = new DeReloadableResourceBundleMessageSource();
        messages.setBasename("classpath:i18n/core");
        new Translator().setMessageSource(messages);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new Controller())
                .setControllerAdvice(new GlobalExceptionHandler(), new ResultResponseBodyAdvice())
                .addFilters(new RequestIdFilter()).build();
        for (ErrorCategory category : ErrorCategory.values()) {
            MvcResult result = mvc.perform(get("/failure/" + category).header(ErrorResponses.CONTRACT, "2")).andReturn();
            JsonNode body = json.readTree(result.getResponse().getContentAsString());
            check(result.getResponse().getStatus() == category.httpStatus(), category + " status");
            check("2".equals(result.getResponse().getHeader(ErrorResponses.CONTRACT)), "server capability declaration");
            check(body.path("code").asInt() == category.code(), category + " code");
            check(body.path("category").asText().equals(category.name()), category + " classification");
            check(body.path("requestId").asText().equals(result.getResponse().getHeader(ErrorResponses.REQUEST_ID)), "trace response agrees");
            check(body.path("requestId").asText().matches("[a-f0-9]{32}"), "trace format");
            check(body.path("retryable").asBoolean() == (category == ErrorCategory.UPSTREAM || category == ErrorCategory.UPSTREAM_TIMEOUT), "retry classification");
            check(!body.path("msg").asText().isBlank(), "public message");
        }
        var legacy = mvc.perform(get("/failure/LEGACY")).andReturn();
        JsonNode body = json.readTree(legacy.getResponse().getContentAsString());
        check(legacy.getResponse().getStatus() == 409 && body.path("code").asInt() == 50002, "legacy chart status/code");
        check(body.path("msg").asText().equals("legacy chart details"), "legacy chart message");
        check(legacy.getResponse().getHeader(ErrorResponses.CONTRACT) == null, "legacy response capability unchanged");
        check(!body.has("category") && !body.has("requestId") && !body.has("retryable"), "legacy body field compatibility");
        for (String contract : new String[]{"1", "2"}) {
            var extension = mvc.perform(get("/failure/EXTENSION").header(ErrorResponses.CONTRACT, contract)).andReturn();
            check(extension.getResponse().getStatus() == 400, "unregistered extension status preserved");
            check(json.readTree(extension.getResponse().getContentAsString()).path("code").asInt() == 10005, "unregistered extension code preserved");
        }
        var npe = mvc.perform(get("/failure/NPE").header(ErrorResponses.CONTRACT, "2")).andReturn();
        body = json.readTree(npe.getResponse().getContentAsString());
        check(npe.getResponse().getStatus() == 500 && body.path("code").asInt() == 40001, "NPE is internal, never empty-parameter/auth by text");
        check(!body.path("msg").asText().contains("secret"), "NPE sanitized");
        var validation = mvc.perform(post("/form").header(ErrorResponses.CONTRACT, "2")
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andReturn();
        body = json.readTree(validation.getResponse().getContentAsString());
        check(validation.getResponse().getStatus() == 400, "bean validation status");
        check(body.path("fieldErrors").get(0).path("field").asText().equals("name"), "field error name");
        check(!body.path("fieldErrors").get(0).has("rejectedValue"), "no rejected value disclosure");
        for (String content : List.of("{", "[]")) {
            check(mvc.perform(post("/form").contentType(MediaType.APPLICATION_JSON).content(content)).andReturn().getResponse().getStatus() == 400, "malformed body");
        }
        check(mvc.perform(get("/parameter")).andReturn().getResponse().getStatus() == 400, "missing query parameter");
        check(mvc.perform(get("/parameter?id=wrong")).andReturn().getResponse().getStatus() == 400, "invalid parameter type");
        var trace = mvc.perform(get("/trace").header(ErrorResponses.REQUEST_ID, "a".repeat(32))).andReturn();
        check(json.readTree(trace.getResponse().getContentAsString()).path("data").path("id").asText().equals("a".repeat(32)), "incoming request id bound to logging");
        check(MDC.get("requestId") == null, "MDC cleaned");
        trace = mvc.perform(get("/trace").header(ErrorResponses.REQUEST_ID, "unsafe supplied text")).andReturn();
        check(!trace.getResponse().getHeader(ErrorResponses.REQUEST_ID).equals("unsafe supplied text"), "unsafe trace id rejected");
        var timeout = new RuntimeException("wrapper", new java.net.SocketTimeoutException("remote host secret"));
        var classified = RemoteFileErrors.classify(timeout);
        check(classified.getCategory() == ErrorCategory.UPSTREAM_TIMEOUT && classified.getCause() == timeout, "timeout cause retained");
        check(!classified.getMessage().contains("secret"), "remote details not disclosed");
        check(RemoteFileErrors.classify(new java.net.ConnectException()).getCategory() == ErrorCategory.UPSTREAM, "connect failure");
        check(RemoteFileErrors.classify(new org.apache.http.client.HttpResponseException(404,"remote body")).getCategory() == ErrorCategory.UPSTREAM, "remote 404 not local missing resource");
        check(RemoteFileErrors.classify(new java.nio.file.AccessDeniedException("local file")).getCategory() == ErrorCategory.INTERNAL, "local storage error not upstream");
        check(RemoteFileErrors.parse(new RuntimeException(new NoClassDefFoundError("parser class"))).getCategory() == ErrorCategory.INTERNAL, "parser program error not invalid input");
        check(RemoteFileErrors.parse(new java.util.zip.ZipException()).getCategory() == ErrorCategory.VALIDATION, "known corrupt file");
        System.out.println("PASS " + checks + " error contract assertions");
    }
}
