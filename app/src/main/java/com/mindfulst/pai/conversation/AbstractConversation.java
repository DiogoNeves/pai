package com.mindfulst.pai.conversation;

/**
 * Base functionality for all conversations.
 *
 * Usually you'd extend this class and implement processUpdate().
 */
public abstract class AbstractConversation implements Conversation {
    private ConversationState state;
    private String nextQuestion;

    @Override
    public void start(ConversationIntent intent) {
        state = new ConversationState(intent);
        nextQuestion = null;
    }

    @Override
    public void update(ConversationIntent intent) throws ConversationException {
        if (state == null) {
            throw new ConversationException();
        }

        state.update(intent);
        nextQuestion = processUpdate();
    }

    /**
     * Internal processing of the current update.
     * @return The next question to ask if any, null otherwise.
     */
    protected abstract String processUpdate();

    @Override
    public boolean hasQuestion() {
        return nextQuestion != null && nextQuestion.length() > 0;
    }

    @Override
    public String nextQuestion() {
        return nextQuestion;
    }

    @Override
    public ConversationState getState() {
        return state;
    }
}
