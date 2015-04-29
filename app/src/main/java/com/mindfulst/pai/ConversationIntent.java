package com.mindfulst.pai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

import ai.wit.sdk.model.WitOutcome;

/**
 * Wrapper for Wit intents.
 */
public final class ConversationIntent {
    public final String intent;
    public final String statement;
    public final Map<String, String> data;

    public ConversationIntent(String intent, String statement, Map<String, String> data) {
        this.intent = intent;
        this.statement = statement;
        this.data = data;
    }

    public static ConversationIntent createFrom(WitOutcome outcome) {
        String intent = outcome.get_intent();
        String statement = outcome.get_text();

        // TODO: Improve this with a list of data extractors
        Map<String, String> data = new HashMap<>();
        if (!intent.equals("UNKNOWN")) {
            Map<String, JsonElement> entities = outcome.get_entities();
            for (String k : entities.keySet()) {
                JsonArray values = entities.get(k).getAsJsonArray();
                if (values.size() > 0) {
                    JsonElement valueElement = values.get(0);
                    data.put(k, valueElement.getAsString());
                }
            }
        }

        return new ConversationIntent(intent, statement, data);
    }
}
