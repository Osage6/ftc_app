// Copyright (c) 2017 FTC Team 10262 HippoBotamuses

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import static org.firstinspires.ftc.teamcode.MitchellAuton.State.TF_ACTIVATE;

/**
 * AutonDrive Mode
 * <p>
 *
 *     Modified by Mitchell 4H from 12/18/18
 *
 *     handleState includes "default:" which means we finally check any of the states in
 *     the superclass that are not included in this color/alliance dependent list of
 *     states. So you need to have MitchellAuton.java opened along with this one if you
 *     want to get a fuller picture of the state machine
 *
 *     Other than dealing with the states that are specific to the color alliance, this
 *     does not add anything new
 */
//@Disabled()
@Autonomous(name="BlueMitchellAuton", group="BeyondTheRealm")
public class BlueMitchellAuton extends MitchellAuton {
    /**
     * Constructor
     */
    public BlueMitchellAuton() {
        // most if not all of your setup code
        // belongs in init, not here (see below)
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    protected State handleState(State state, double time_in_state) {
        switch (state) {
            case BEGIN:
                state = TF_ACTIVATE;
                break;

            default:
                state = super.handleState(state, time_in_state);
        }

        return state;
    }

//    private void imu_loop() {
//        Orientation pos = imu.getAngularOrientation();
//        telemetry.addData("IMU: ", "h:" + pos.firstAngle + " y: " + pos.secondAngle + " p:" + pos.thirdAngle);
//    }

}