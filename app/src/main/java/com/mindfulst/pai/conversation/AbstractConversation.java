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
        nextQuestion = null;
        state = new ConversationState(intent);
        processUpdate(intent);
    }

    @Override
    public boolean update(ConversationIntent intent) {
        assert state != null;

        // Should happen in this order
        nextQuestion = null;
        state.update(intent);
        return processUpdate(intent);
    }

    /**
     * Internal processing of the current update.
     * @param intent Current intent.
     * @return true if the update was successful, false if the intent doesn't make sense in the
     * conversation.
     */
    protected abstract boolean processUpdate(ConversationIntent intent);

    protected void setNextQuestion(String question) {
        nextQuestion = question;
    }

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
