package com.mindfulst.pai.conversation;

/**
 * Interface for conversation modules.
 */
public interface Conversation {
    /**
     * Start a conversation with the given intent.
     * @param intent data to start the conversation.
     */
    public void start(ConversationIntent intent);

    /**
     * Update an on-going conversation with the given intent.
     * @param intent data to update the conversation.
     *
     * @exception ConversationException if the conversation hasn't yet started.
     */
    public void update(ConversationIntent intent) throws ConversationException;

    /**
     * Checks if the conversation module has a question that needs answering before all the data is
     * complete.
     * @return true if it needs to ask a question, false otherwise (or if the conversation didn't
     * start yet).
     */
    public boolean hasQuestion();

    /**
     * Returns the next question to ask.
     * @return next question text.
     */
    public String nextQuestion();

    /**
     * Returns the current conversation state.
     * Usually used when no more questions need to be asked and we want to use the data to plan an
     * action.
     * @return the current conversation state, null if the conversation didn't start yet.
     */
    public ConversationState getState();
}
