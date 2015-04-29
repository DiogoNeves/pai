package com.mindfulst.pai.conversation;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

import ai.wit.sdk.model.WitOutcome;

/**
 * Wrapper for Wit intents.
 */
public final class ConversationIntent {
    public final String type;
    public final String statement;
    public final Map<String, String> data;

    public ConversationIntent(String intent, String statement, Map<String, String> data) {
        this.type = intent;
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
                    JsonObject valueElement = values.get(0).getAsJsonObject();
                    try {
                        data.put(k, valueElement.get("value").getAsString());
                    } catch (UnsupportedOperationException e) {
                        Log.e("Conversation", e.toString());
                    }
                }
            }
        }

        return new ConversationIntent(intent, statement, data);
    }
}
