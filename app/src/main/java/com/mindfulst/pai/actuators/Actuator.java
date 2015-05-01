package com.mindfulst.pai.actuators;

import com.mindfulst.pai.conversation.ConversationState;

/**
 * Interface for actuators.
 */
public interface Actuator {
    /**
     * Check if this Actuator can act on state.
     *
     * @param state State to check against.
     * @return true if it can act, false otherwise.
     */
    public boolean canActOn(ConversationState state);

    /**
     * Act on state.
     * @param state to act upon.
     * @return the result of the action.
     */
    public ActionResult actOn(ConversationState state);
}
