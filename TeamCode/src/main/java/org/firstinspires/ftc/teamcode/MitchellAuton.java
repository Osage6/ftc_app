// Copyright (c) 2017 FTC Team 10262 HippoBotamuses

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.util.ElapsedTime;

import static org.firstinspires.ftc.teamcode.MitchellAuton.State.FINISHED;
import static org.firstinspires.ftc.teamcode.MitchellAuton.State.READ_COLOR_SENSOR;
import static org.firstinspires.ftc.teamcode.MitchellAuton.State.WAIT_FOR_COLOR_SENSOR;

/**
 * AutonDrive Mode
 * <p>
 */
@Disabled
public class MitchellAuton extends MitchellBase {

    enum State {
        BEGIN,
        LOWER_ARM,
        WAIT_FOR_COLOR_SENSOR,
        READ_COLOR_SENSOR,
        FOUND_BLUE_JEWEL,
        FOUND_RED_JEWEL,
        KNOCK_BLUE_JEWEL,
        KNOCK_RED_JEWEL,
        RAISE_ARM,
        REALIGN_ROBOT,
        RAMP_UP,
        DRIVE,
        RAMP_DOWN,
        STOP,
        FINISHED
    };

    private State current_state = State.BEGIN;
    private ElapsedTime time;
    private long counter = 0;

    /**
     * Constructor
     */
    public MitchellAuton() {
        // most if not all of your setup code
        // belongs in init, not here (see below)
    }

    @Override
    public void init() {
        super.init();
        current_state = State.BEGIN;
        time = new ElapsedTime();
    }
    @Override
    public void init_loop() {
        super.init_loop();
        arm_servo.setPosition(MitchellBotCalibration.arm_servo_RETRACTED);
    }

    protected State handleState(State state, double time_in_state) {

        switch (state) {
            case BEGIN:
                break;

            case LOWER_ARM:
                arm_servo.setPosition(MitchellBotCalibration.COLOR_SENSOR_POS_DOWN);
                if (time_in_state > 1.0) {
                    return WAIT_FOR_COLOR_SENSOR;
                }
                break;

            case WAIT_FOR_COLOR_SENSOR:
                if (time_in_state > 1) {
                    return READ_COLOR_SENSOR;
                }
                break;

            case KNOCK_BLUE_JEWEL:
                break;

            case KNOCK_RED_JEWEL:
                break;

            case RAISE_ARM:
                arm_servo.setPosition(MitchellBotCalibration.COLOR_SENSOR_POS_UP);
                break;

            case REALIGN_ROBOT:
                break;

            case RAMP_UP:
                break;

            case DRIVE:
                break;

            case RAMP_DOWN:
                break;

            case STOP:
                set_drive_power(0,0);
                return FINISHED;

            case FINISHED:
                break;
        }

        return state;
    }

    @Override
    public void loop() {
        double elapsed = time.seconds();

        telemetry.addData("timer: ", elapsed);
        telemetry.addData("State:", "pre " + current_state + " / " + counter);
        State new_state = handleState(current_state, elapsed);
        telemetry.addData("State:", "post " + current_state + " / " + counter);

        counter += 1;

        if (new_state != current_state) {
            time.reset();
            current_state = new_state;
        }
    }
}
