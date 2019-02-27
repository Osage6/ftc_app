// Copyright (c) 2017 FTC Team 10262 Pioπeers

package org.firstinspires.ftc.teamcode;

import android.content.Context;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * Base code common to all other 10262 opmode
 *
 * Modified by Mitchell 4H from 12/18/18
 * Besides mapping the hardware, this includes the methods that control power and
 * position to the motors and servos
 */
//@Disabled()
public class MitchellBase extends OpMode {
    private static Context appContext;
    protected MitchellMenuController menu_controller = null;
    private ElapsedTime runtime = new ElapsedTime();

    // Make private to ensure that our set_power routine
    // is used and power ramping is implemented
    public static DcMotor left_drive = null;
    public static DcMotor right_drive = null;

    protected static DcMotor arm_driver = null;
    protected static DcMotor linear_actuator = null;

    protected static Servo elbow_servo = null;
    protected static CRServo sweeper = null;
    protected static CRServo stabilizer = null;

    protected static TouchSensor arm_check = null;


    /**
     * Constructor
     */
    public MitchellBase() {
        // most if not all of your setup code
        // belongs in init, not here (see below)
    }

    /*
     * Code to run when the op mode is initialized goes here
     */
    @Override
    public void init() {
        appContext = hardwareMap.appContext;
        new MitchellBotCalibration();

        left_drive = hardwareMap.get(DcMotor.class, "left drive");
        right_drive = hardwareMap.get(DcMotor.class, "right drive");
        DcMotor.ZeroPowerBehavior drive_mode = DcMotor.ZeroPowerBehavior.FLOAT;

        if (MitchellBotCalibration.LOCK_DRIVE_WHEELS) {
            drive_mode = DcMotor.ZeroPowerBehavior.BRAKE;
        }
        left_drive.setZeroPowerBehavior(drive_mode);
        right_drive.setZeroPowerBehavior(drive_mode);

        DcMotor.ZeroPowerBehavior arm_mode = DcMotor.ZeroPowerBehavior.FLOAT;
        if (MitchellBotCalibration.LOCK_ARM) {
            arm_mode = DcMotor.ZeroPowerBehavior.BRAKE;
        }
        arm_driver = hardwareMap.get(DcMotor.class, "left_arm");
        arm_driver.setZeroPowerBehavior(arm_mode);

        linear_actuator = hardwareMap.get(DcMotor.class, "linear_actuator");
        linear_actuator.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        elbow_servo = hardwareMap.get(Servo.class, "elbow_servo");
        elbow_servo.setDirection(Servo.Direction.REVERSE);

        sweeper = hardwareMap.get(CRServo.class, "sweeper");
        stabilizer = hardwareMap.get(CRServo.class, "stabilizer");

        menu_controller = new MitchellMenuController(new MitchellBotCalibration());
    }

    @Override
    public void init_loop() {
        menu_controller.loop(telemetry, gamepad1);
    }

    @Override
    public void start() {
        telemetry.clearAll();
    }

    @Override
    public void loop() {
        // do nothing
    }

    static Context getContext() {
        return appContext;
    }

    /*
     * Code to run when the op mode is first disabled goes here
     *
     */
    @Override
    public void stop() {
        long stopTime = System.currentTimeMillis();
        set_drive_power(0, 0, stopTime);
        stop_arm();
        stop_linear_actuator();
    }

    private void stop_arm() {
        arm_driver.setPower(0);
    }

    protected void stop_linear_actuator() {
        linear_actuator.setPower(0);
    }

    protected void elevator(double lift_speed) {
        linear_actuator.setPower(lift_speed);
    }

    protected void sweep(double sweep_speed) {
        sweeper.setPower(sweep_speed);
    }

    protected void move_arm(double arm_speed) {
        arm_driver.setPower(arm_speed);
    }

    /**
     * Limit motor values to the -1.0 to +1.0 range.
     * <p>
     * Maybe this is no longer necessary. See
     * https://ftc-tricks.com/dc-motors/
     */
    protected static double limit(double num) {
        return limit(-1, 1, num);
    }

    protected static double limit(double min, double max, double num) {
        if (num > max) {
            return max;
        }
        if (num < min) {
            return min;
        }

        return num;
    }

    protected static double ramp(double oldVal, double newVal, double maxRamp) {
        double delta = newVal - oldVal;
        if (delta > maxRamp) {
            delta = maxRamp;
        } else if (delta < -maxRamp) {
            delta = -maxRamp;
        }
        return oldVal + delta;
    }

    private double prevLeftPower = 0;
    private double prevRightPower = 0;

    protected void set_drive_power(double left, double right, long lastSetTime) {
        if (MitchellBotCalibration.RAMP_DRIVE_POWER) {
            final double maxChangePerMilliSecond = MitchellBotCalibration.RAMP_DRIVE_DURATION;
            final long ticks = System.currentTimeMillis() - lastSetTime;
            final double maxRamp = Math.max(1, maxChangePerMilliSecond * ticks);

            left = ramp(prevLeftPower, left, maxRamp);
            right = ramp(prevRightPower, right, maxRamp);
            prevLeftPower = left;
            prevRightPower = right;
        }
        this.left_drive.setPower(-left);
        this.right_drive.setPower(right);
    }

    /**
     * Arcade drive implements single stick driving. This function lets you
     * directly provide joystick values from any source.
     * $
     *
     * @param moveValue     The value to use for forwards/backwards
     * @param rotateValue   The value to use for the rotate right_drive/left_drive
     * @param squaredInputs If set, decreases the sensitivity at low speeds
     */
    public void arcadeDrive(double moveValue, double rotateValue, boolean squaredInputs) {
        double leftMotorSpeed;
        double rightMotorSpeed;

        moveValue = limit(moveValue);
        rotateValue = limit(rotateValue);

        if (squaredInputs) {
            // square the inputs (while preserving the sign) to increase fine control
            // while permitting full power
            if (moveValue >= 0.0) {
                moveValue = (moveValue * moveValue);
            } else {
                moveValue = -(moveValue * moveValue);
            }
            if (rotateValue >= 0.0) {
                rotateValue = (rotateValue * rotateValue);
            } else {
                rotateValue = -(rotateValue * rotateValue);
            }
        }

        if (moveValue > 0.0) {
            if (rotateValue > 0.0) {
                leftMotorSpeed = moveValue - rotateValue;
                rightMotorSpeed = Math.max(moveValue, rotateValue);
            } else {
                leftMotorSpeed = Math.max(moveValue, -rotateValue);
                rightMotorSpeed = moveValue + rotateValue;
            }
        } else {
            if (rotateValue > 0.0) {
                leftMotorSpeed = -Math.max(-moveValue, rotateValue);
                rightMotorSpeed = moveValue + rotateValue;
            } else {
                leftMotorSpeed = moveValue - rotateValue;
                rightMotorSpeed = -Math.max(-moveValue, -rotateValue);
            }
        }
        long setTime = System.currentTimeMillis();
        set_drive_power(leftMotorSpeed, rightMotorSpeed, setTime);
    }

    /*
     *  Method to perfmorm a relative move, based on encoder counts.
     *  Encoders are not reset as the move is based on the current position.
     *  Move will stop if any of three conditions occur:
     *  1) Move gets to the desired position
     *  2) Move runs out of time
     *  3) Driver stops the opmode running.
     */
    public void encoderDrive(double speed,
                             double leftInches, double rightInches,
                             double timeoutS) {
        int newLeftTarget;
        int newRightTarget;

        // Ensure that the opmode is still active
        if (true) {//we need an equivalent of opModeIsActive()?

            // Determine new target position, and pass to motor controller
            newLeftTarget = left_drive.getCurrentPosition() + (int) (leftInches * MitchellBotCalibration.COUNTS_PER_INCH);
            newRightTarget = right_drive.getCurrentPosition() + (int) (rightInches * MitchellBotCalibration.COUNTS_PER_INCH);
            left_drive.setTargetPosition(newLeftTarget);
            right_drive.setTargetPosition(newRightTarget);

            // Turn On RUN_TO_POSITION
            left_drive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            right_drive.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            // reset the timeout time and start motion.
            runtime.reset();
            left_drive.setPower(Math.abs(speed));
            right_drive.setPower(Math.abs(speed));

            // keep looping while we are still active, and there is time left, and both motors are running.
            // Note: We use (isBusy() && isBusy()) in the loop test, which means that when EITHER motor hits
            // its target position, the motion will stop.  This is "safer" in the event that the robot will
            // always end the motion as soon as possible.
            // However, if you require that BOTH motors have finished their moves before the robot continues
            // onto the next step, use (isBusy() || isBusy()) in the loop test.
            while (
                    (runtime.seconds() < timeoutS) &&
                            (left_drive.isBusy() && right_drive.isBusy())) {

                // Display it for the driver.
                telemetry.addData("Path1", "Running to %7d :%7d", newLeftTarget, newRightTarget);
                telemetry.addData("Path2", "Running at %7d :%7d",
                        left_drive.getCurrentPosition(),
                        right_drive.getCurrentPosition());
                telemetry.update();
            }

            // Stop all motion;
            left_drive.setPower(0);
            right_drive.setPower(0);

            // Turn off RUN_TO_POSITION
            left_drive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            right_drive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            //  sleep(250);   // optional pause after each move
        }
    }

}