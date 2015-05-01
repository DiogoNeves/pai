package com.mindfulst.pai.actuators;

/**
 * Results returns from actuators that can be outputed to the user.
 */
public final class ActionResult {
    public final String result;

    public ActionResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return result;
    }
}
