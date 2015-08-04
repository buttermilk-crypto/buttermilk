package com.cryptoregistry.protocol.handshake;

import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.github.oxo42.stateless4j.delegates.Action;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PhoneCallJava8 {

    @Test
    public void testPhoneRings() throws Exception {
        StateMachineConfig<State, Trigger> phoneCallConfig = new StateMachineConfig<>();

        phoneCallConfig.configure(State.OffHook)
                .permit(Trigger.CallDialed, State.Ringing);

        phoneCallConfig.configure(State.Ringing)
                .permit(Trigger.HungUp, State.OffHook)
                .permit(Trigger.CallConnected, State.Connected);

        phoneCallConfig.configure(State.Connected)
                .onEntry(this::startCallTimer)
                .onExit(this::stopCallTimer)
                .permit(Trigger.LeftMessage, State.OffHook)
                .permit(Trigger.HungUp, State.OffHook)
                .permit(Trigger.PlacedOnHold, State.OnHold);

        // ...

        StateMachine<State, Trigger> phoneCall = new StateMachine<>(State.OffHook, phoneCallConfig);

        phoneCall.fire(Trigger.CallDialed);
        assertEquals(State.Ringing, phoneCall.getState());
    }

    private void stopCallTimer() {
        // ...
    }

    private void startCallTimer() {
        // ...
    }

    private enum State {
        Ringing, Connected, OnHold, OffHook

    }
    private enum Trigger {
        CallDialed, CallConnected, PlacedOnHold, LeftMessage, HungUp

    }
}
