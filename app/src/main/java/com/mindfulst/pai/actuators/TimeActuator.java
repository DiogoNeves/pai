package com.mindfulst.pai.actuators;

import android.text.format.Time;

import com.mindfulst.pai.conversation.ConversationState;

/**
 * Class responsible for executing time related actions.
 */
public class TimeActuator implements Actuator {
    @Override
    public boolean canActOn(ConversationState state) {
        return state.getInitialIntent().equals("time");
    }

    @Override
    public ActionResult actOn(ConversationState state) {
        Time time = new Time();
        time.setToNow();
        return new ActionResult(time.format3339(false));
    }
}
