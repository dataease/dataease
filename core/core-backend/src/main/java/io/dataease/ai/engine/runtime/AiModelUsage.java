package io.dataease.ai.engine.runtime;

import com.fasterxml.jackson.databind.JsonNode;
import io.dataease.ai.support.AiErrors;
import io.dataease.api.ai.conversation.AiConversationDtos.Metering;

import java.util.ArrayList;
import java.util.List;

/** Provider-reported usage for the bounded upstream requests of one execution attempt. */
public final class AiModelUsage {
    private final List<Request> sent = new ArrayList<>();

    final class Request {
        private Integer input;
        private Integer output;

        void sent() {
            synchronized (AiModelUsage.this) {
                sent.add(this);
            }
        }

        void report(JsonNode usage, int allocation) {
            if (usage == null || usage.isNull()) return;
            if (!usage.isObject()) throw AiErrors.error("response_invalid");
            synchronized (AiModelUsage.this) {
                input = reported(usage.get("prompt_tokens"), input);
                output = reported(usage.get("completion_tokens"), output);
                if (allocation > 0 && output != null && output > allocation) throw AiErrors.error("output_limit");
            }
        }

        private Integer reported(JsonNode field, Integer previous) {
            if (field == null || field.isNull()) return previous;
            if (!field.isIntegralNumber() || !field.canConvertToInt() || field.intValue() < 0
                    || previous != null && previous != field.intValue()) throw AiErrors.error("response_invalid");
            return field.intValue();
        }
    }

    public synchronized Metering snapshot(long elapsedMillis) {
        long input = 0, output = 0;
        int inputReports = 0, outputReports = 0;
        for (Request request : sent) {
            if (request.input != null) {
                input += request.input;
                inputReports++;
            }
            if (request.output != null) {
                output += request.output;
                outputReports++;
            }
        }
        String status = inputReports == 0 && outputReports == 0 ? "MISSING"
                : inputReports == sent.size() && outputReports == sent.size() ? "COMPLETE" : "PARTIAL";
        return new Metering(elapsedMillis, inputReports == 0 ? null : input, outputReports == 0 ? null : output, status);
    }
}
