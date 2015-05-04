package com.mindfulst.pai.actuators;

import com.mindfulst.pai.conversation.ConversationState;

/**
 * Class responsible for executing weather related actions.
 */
public class WeatherActuator implements Actuator {
    @Override
    public boolean canActOn(ConversationState state) {
        return state.getInitialIntent().equals("weather");
    }

    @Override
    public ActionResult actOn(ConversationState state) {
        String forecast = "It's sunny";
        if (state.hasConcept("location")) {
            forecast += " in " + state.getConceptValue("location");
        }
        return new ActionResult(forecast);
    }
}
