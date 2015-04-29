package com.mindfulst.pai;

import java.util.Map;

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
}
