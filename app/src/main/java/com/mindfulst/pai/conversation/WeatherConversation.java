package com.mindfulst.pai.conversation;

/**
 * Conversations about weather.
 */
public class WeatherConversation extends AbstractConversation {
    @Override
    protected boolean processUpdate(ConversationIntent intent) {
        ConversationState state = getState();

        if (!state.hasConcept("datetime")) {
            state.setConcept("datetime", "now");
        }

        if (!state.hasConcept("location")) {
            if (!intent.type.equals("weather")) {
                return false;
            } else {
                setNextQuestion("Where?");
            }
        }

        return true;
    }
}
