package com.mindfulst.pai.conversation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Keeps track of the current conversation situation.
 */
public class ConversationState {
    private Map<String, String> concepts;
    private Queue<ConversationIntent> log;

    public ConversationState(ConversationIntent intent) {
        concepts = new HashMap<>(intent.data);
        log = new LinkedList<>();
        log.add(intent);
    }

    public String getInitialIntent() {
        return log.element().intent;
    }

    public String getConceptValue(String concept) {
        return concepts.get(concept);
    }

    public boolean hasConcept(String concept) {
        return concepts.containsKey(concept);
    }

    public void update(ConversationIntent intent) {
        log.add(intent);
        concepts.putAll(intent.data);
    }

    public void setConcept(String concept, String value) {
        concepts.put(concept, value);
    }
}
