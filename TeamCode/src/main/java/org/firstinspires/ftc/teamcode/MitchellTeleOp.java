// Copyright (c) 2017 FTC Team 10262 Pioπeers

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/**
 * ArcadeDrive Mode
 * <p>
 */
//@Disabled()
@TeleOp(name="MitchellTeleop", group="Teleop")
public class MitchellTeleOp extends MitchellBase {

    private int counter = 0;
    private long last = 0;
    private long total = 0;
    private boolean deploy;
    private double lastArmPosition = 0;

    public MitchellTeleOp() {}

    @Override
    public void init() {
        super.init();

        last = System.nanoTime();

    }

    @Override
    public void init_loop() {
        super.init_loop();

        // Do these in init_loop so we can see the adjustments
        //elbow_servo.setPosition(0.05);
    }

    @Override
    public void loop() {
        arcadeDrive(
                (gamepad1.right_trigger + gamepad1.left_trigger * -1)*.5,
                (-gamepad1.left_stick_x)*.5,
                true);

        sweeper_loop();
        elevator_loop();
  //      color_sensor_loop(); //I want to change this to a touch_sensor_loop
        elbow_servo_loop();
        // timing_info_loop();
        move_arm_loop();
        stabilizer_loop();
    }

    private void move_arm_loop(){
        double armspeed = gamepad2.right_stick_y;
        if(armspeed > 0){armspeed = armspeed * armspeed;}
        else {armspeed = - armspeed * armspeed;}
        if (gamepad2.right_bumper){
            move_arm(armspeed/4);
        }
        else{move_arm(armspeed);}
        //move_arm(armspeed);
    }
    private void elbow_servo_loop() {

        double elbowPosition = (gamepad2.left_stick_y + 1)/2;
            //if(gamepad2.left_bumper){
                elbow_servo.setPosition(elbowPosition);
                //lastArmPosition = elbowPosition;
            //}
            //else {elbow_servo.setPosition(lastArmPosition);}
    }

    protected void timing_info_loop() {
        if (counter++ % MitchellBotCalibration.SAMPLES == 0) {
            telemetry.addData("loop time: ", total / MitchellBotCalibration.SAMPLES / 1000.0);
            /*
            org.firstinspires.ftc.robotcore.external.navigation.Position pos = imu.getPosition();
            Orientation angles = imu.getAngularOrientation();
            telemetry.addData("imu: ", "x: " + angles.firstAngle + ", y: " +
                    angles.secondAngle + ", z: " + angles.thirdAngle);
                    */
            counter = 0;
            total = 0;
        }
        final long now = System.nanoTime();
        final long loop_time = now - last;
        total += loop_time;
        last = now;
    }


    protected void elevator_loop() {
        double elevator_power = MitchellBotCalibration.ELEVATOR_POWER;
        if (gamepad1.dpad_down) {
            elevator(elevator_power);
        } else if (gamepad1.dpad_up){
            elevator(-elevator_power);
        }
        else {
            elevator(0);
        }
    }

    protected void sweeper_loop() {
        double reverse = 1;

        if (gamepad2.b) {
            reverse = -1;
        }

        if (gamepad2.left_bumper) {
            sweep(MitchellBotCalibration.MAX_INTAKE_POWER*reverse);
        }
        else {
            sweep(0);
        }
    }

    protected void stabilizer_loop(){
        if (gamepad2.dpad_up){
            stabilizer.setPower(.5);
        }
        else if (gamepad2.dpad_down){
            stabilizer.setPower(-.5);
        }
        else {
            stabilizer.setPower(0);
        }
    }

}