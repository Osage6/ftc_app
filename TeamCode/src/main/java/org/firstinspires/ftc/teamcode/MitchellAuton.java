// Copyright (c) 2017 FTC Team 10262 HippoBotamuses

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.vuforia.CameraDevice;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import java.util.List;

import static org.firstinspires.ftc.teamcode.MitchellAuton.State.ADJUST;
import static org.firstinspires.ftc.teamcode.MitchellAuton.State.FINISHED;
import static org.firstinspires.ftc.teamcode.MitchellAuton.State.LOOK_FOR_GOLD;
import static org.firstinspires.ftc.teamcode.MitchellAuton.State.STOP;
import static org.firstinspires.ftc.teamcode.MitchellAuton.State.TF_ACTIVATE;

/**
 * AutonDrive Mode
 * <p>
 *
 *     Modified by Mitchell from 12/18/18
 *     The states that require knowledge of the color alliance are left to the
 *     subclass that extends this one
 *     This sets the initial state and behavior when autonomous begins
 *
 */
//@Disabled
public class MitchellAuton extends MitchellBase {

    enum State {
        BEGIN,
        LOWER_ACTUATOR,
        ADJUST,
        TF_ACTIVATE,
        LOOK_FOR_GOLD,
        DRIVE_THROUGH_GOLD,
        DEPLOY_MARKER,
        REALIGN_ROBOT,
        DRIVE_TO_CRATER,
        STOP,
        FINISHED
    };

    private State current_state = State.BEGIN;
    private ElapsedTime time;
    private long counter = 0;

    private static final String TFOD_MODEL_ASSET = "RoverRuckus.tflite";
    private static final String LABEL_GOLD_MINERAL = "Gold Mineral";
    private static final String LABEL_SILVER_MINERAL = "Silver Mineral";
    private static final String VUFORIA_KEY = "AVGHUv3/////AAABmcdJ2Bhj40jMsHWkpgSOC8MTXXjW6V8+54EbRFI7hqVgq3s60HFO69FCN5V+YnYjVCGDriHQL5nQxn019A5ZARF8cBCTRFQK8d++tokIN6SnnzvRwe94/dmA2hc87+Us6dfomOUlQ/KTuoNd9biHbT2Ez+i9++v/UtubGLA9OEKpUKZEnPwyBdcr3vjpMp2VvJioaSpFrxqQRUTe1ZJrMa22dp7HWhbEBzOFQyXb+dAjbfeMvaD5qjFDeN9kxzdy6PAJqxx8aUH75nmU3K0qvoY9oRb8cPmXxNazNNlgnUWjA1kcF4kZBzvUPyZRK5x1RqAwx4eham+jXee5YwyPCswg8K8PnGpKd8oHRNzTp6ZH";
    private VuforiaLocalizer vuforia;
    private TFObjectDetector tfod;

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
        left_drive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        right_drive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        linear_actuator.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        left_drive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        right_drive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        initVuforia();

        CameraDevice.getInstance().setFlashTorchMode(true);

        if (ClassFactory.getInstance().canCreateTFObjectDetector()) {
            initTfod();
        } else {
            telemetry.addData("Sorry!", "This device is not compatible with TFOD");
        }
    }
    @Override
    public void init_loop() {
        super.init_loop();
        //elbow_servo.setPosition(MitchellBotCalibration.elbow_servo_RETRACTED);
    }

    protected State handleState(State state, double time_in_state) {

        switch (state) {
            case BEGIN:
                return TF_ACTIVATE;

            case LOWER_ACTUATOR:
                //elbow_servo.setPosition(MitchellBotCalibration.GO_TO_LANDING_POS);
                linear_actuator.setPower(0.5);
                if (time_in_state > 1.0) {
                    return ADJUST;
                }
                break;

            case ADJUST:
                if (time_in_state > 1) {
                    return TF_ACTIVATE;
                }
                break;

            case DRIVE_THROUGH_GOLD:
                encoderDrive(MitchellBotCalibration.DRIVE_SPEED,  12,  -12, 10.0);  // S1: Forward 47 Inches with 5 Sec timeout
                encoderDrive(MitchellBotCalibration.TURN_SPEED,   12, 12, 4.0);  // S2: Turn Right 12 Inches with 4 Sec timeout
                encoderDrive(MitchellBotCalibration.DRIVE_SPEED, -12, 12, 4.0);  // S3: Reverse 24 Inches with 4 Sec timeout

                return STOP;

            case TF_ACTIVATE:
                if (tfod != null) {
                    tfod.activate();
                }
                return LOOK_FOR_GOLD;

            case DEPLOY_MARKER:
                //elbow_servo.setPosition(MitchellBotCalibration.GO_TO_HOOK_POS);
                break;

            case LOOK_FOR_GOLD:

                if (tfod != null) {
                    // getUpdatedRecognitions() will return null if no new information is available since
                    // the last time that call was made.
                    List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
                    if (updatedRecognitions != null) {
                        telemetry.addData("# Object Detected", updatedRecognitions.size());
                        if (time_in_state > 5.0 || updatedRecognitions.size() == 3){
                            int goldMineralX = -1;
                            int silverMineral1X = -1;
                            int silverMineral2X = -1;
                            int im_width = -1;
                            int gM_sector = -1;
                            int sM1_sector = -1;
                            int sM2_sector = -1;
                            for (Recognition recognition : updatedRecognitions) {
                                if (recognition.getLabel().equals(LABEL_GOLD_MINERAL)) {
                                    goldMineralX = (int) recognition.getLeft();
                                    im_width = recognition.getImageWidth();
                                    if (goldMineralX < 2*im_width/3){
                                        if (goldMineralX < im_width/3){
                                            gM_sector = 1;
                                        } else {gM_sector = 2;}
                                    } else {gM_sector = 3;}
                                } else if (silverMineral1X == -1) {
                                    silverMineral1X = (int) recognition.getLeft();
                                    im_width = recognition.getImageWidth();
                                    if (silverMineral1X < 2*im_width/3){
                                        if (silverMineral1X < im_width/3){
                                            sM1_sector = 1;
                                        } else {sM1_sector = 2;}
                                    } else {sM1_sector = 3;}
                                } else {
                                    silverMineral2X = (int) recognition.getLeft();
                                    im_width = recognition.getImageWidth();
                                    if (silverMineral2X < 2*im_width/3){
                                        if (silverMineral2X < im_width/3){
                                            sM2_sector = 1;
                                        } else {sM2_sector = 2;}
                                    } else {sM2_sector = 3;}
                                }
                            }
                            double lg_tl = 10*MitchellBotCalibration.LG_TL;
                            double lg_tl1 = 10*MitchellBotCalibration.LG_TL1;
                            double lg_s = 10*MitchellBotCalibration.LG_S;
                            double lg_tr = 10*MitchellBotCalibration.LG_TR;
                            double lg_tr1 = 10*MitchellBotCalibration.LG_TR1;

                            if ((gM_sector == 1) || ((sM1_sector == 2) && (sM2_sector == 3)) || ((sM1_sector == 3) && (sM2_sector == 2)) ) {
                                telemetry.addData("Gold Mineral Position", "Left");
                                encoderDrive(MitchellBotCalibration.TURN_SPEED, lg_tl, lg_tl, 4.0);
                                encoderDrive(MitchellBotCalibration.DRIVE_SPEED, -lg_tl1, lg_tl1, 4.0);
                                if (tfod != null) {
                                    tfod.shutdown();
                                }
                                return STOP;
                           } else if (gM_sector == 3 || (sM1_sector == 1 && sM2_sector == 2) || (sM1_sector == 2 && sM2_sector == 1) ) {
                                telemetry.addData("Gold Mineral Position", "Right");
                                encoderDrive(MitchellBotCalibration.TURN_SPEED, -lg_tr, -lg_tr, 4.0);
                                encoderDrive(MitchellBotCalibration.DRIVE_SPEED, -lg_tr1, lg_tr1, 4.0);
                                if (tfod != null) {
                                    tfod.shutdown();
                                }
                                return STOP;
                            } else if (gM_sector == 2 || (sM1_sector == 1 && sM2_sector == 3) || (sM1_sector == 3 && sM2_sector == 1) ) {
                                telemetry.addData("Gold Mineral Position", "Center");
                                encoderDrive(MitchellBotCalibration.DRIVE_SPEED, -lg_s, lg_s, 4.0);
                                if (tfod != null) {
                                    tfod.shutdown();
                                }
                                return STOP;                            }
                        }

                        telemetry.update();
                    }
                }

            case DRIVE_TO_CRATER:
                break;

            case STOP:
                long stopTime = System.currentTimeMillis();
                set_drive_power(0,0,stopTime);
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

    /**
     * Initialize the Vuforia localization engine.
     */
    private void initVuforia() {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         */
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraDirection = CameraDirection.BACK;

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Loading trackables is not necessary for the Tensor Flow Object Detection engine.
    }


    /**
     * Initialize the Tensor Flow Object Detection engine.
     */
    private void initTfod() {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_GOLD_MINERAL, LABEL_SILVER_MINERAL);
    }

}