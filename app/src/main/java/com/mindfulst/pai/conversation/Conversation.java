package com.mindfulst.pai.conversation;

/**
 * Interface for conversation modules.
 *
 * The conversation logic should be:
 * 1 - start or update
 * 2 - if no question, get state and perform the action
 * 3 - if question, get question and ask
 * 4 - repeat all
 *
 */
public interface Conversation {
    /**
     * Start a conversation with the given intent.
     * @param intent data to start the conversation.
     */
    public void start(ConversationIntent intent);

    /**
     * Update an on-going conversation with the given intent.
     * This can't be called before start.
     * @param intent data to update the conversation.
     *               
     * @return true if the update was successful, false if the intent doesn't make sense in the
     * conversation.
     */
    public boolean update(ConversationIntent intent);

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
