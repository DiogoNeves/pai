package com.mindfulst.pai.conversation;

/**
 * Conversations about weather.
 */
public class WeatherConversation extends AbstractConversation {
    @Override
    protected String processUpdate() {
        ConversationState state = getState();

        if (!state.hasConcept("datetime")) {
            state.setConcept("datetime", "now");
        }

        String nextQuestion = null;
        if (!state.hasConcept("location")) {
            nextQuestion = "Where?";
        }

        return nextQuestion;
    }
}
