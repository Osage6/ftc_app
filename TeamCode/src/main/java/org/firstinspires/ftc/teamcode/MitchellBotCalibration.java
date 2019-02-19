package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;

/**
 * Inspired by phurley (created original on 11/10/17)
 *
 * Modified for Mitchell 4H team, Beyond the Realm of Probabilities by J Happer, Feb 2019
 *
 * This class holds all values that we might want to calibrate
 * The MitchellBotConstants class has methods used in conjunction with the
 * MitchellMenuController to allow adjustment of these values during the init_loop
 */
@Disabled()
public class MitchellBotCalibration extends MitchellBotConstants {

    public static double DRIVE_SPEED = 0.4;
    public static double TURN_SPEED = 0.5;

    public static int SAMPLES = 50;
    public static double MAX_INTAKE_POWER = 0.9;
    public static double ELEVATOR_POWER = 0.5;

    public static boolean LOCK_DRIVE_WHEELS = false;
    public static boolean LOCK_ARM = true;

    public static boolean RAMP_DRIVE_POWER = true;
    public static double RAMP_DRIVE_DURATION = 0.0005;

    public static boolean INITIALIZE_IMU = false;
    public static boolean INITIALIZE_VUFORIA = false;

    static final double     COUNTS_PER_MOTOR_REV    = 1440 ;    // eg: TETRIX Motor Encoder
    static final double     DRIVE_GEAR_REDUCTION    = 1.0 ;     // This is < 1.0 if geared UP
    static final double     WHEEL_DIAMETER_INCHES   = 4.0 ;     // For figuring circumference
    static final double     COUNTS_PER_INCH         = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
            (WHEEL_DIAMETER_INCHES * 3.1415);

    public MitchellBotCalibration() {}
}