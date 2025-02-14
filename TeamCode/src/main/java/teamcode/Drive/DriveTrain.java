package teamcode.Drive;


import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp
public class DriveTrain extends OpMode {
    private Intake intake;
    private Outtake outtake;
    private LiftsController liftMotors;
    private IntakeController intakeMotor;

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;
    private ElapsedTime timer = new ElapsedTime();

    private enum LeftTriggerState {
        LIFT_TO_HIGHEST, DROPPER_OPEN_AND_RESET
    }

    // Состояния для left_bumper
    private enum LeftBumperState {
        CLIPS_TAKE, CLIPS_PUT_AND_HIGH_BAR, HIGH_BAR_PUT_AND_RESET
    }

    private LeftTriggerState leftTriggerState = LeftTriggerState.LIFT_TO_HIGHEST;
    private LeftBumperState leftBumperState = LeftBumperState.CLIPS_TAKE;

    private boolean wasLeftTriggerPressed = false;
    private boolean wasLeftBumperPressed = false;
    private boolean wasRightBumperPressed = false;
    private boolean wasRightTriggerPressed = false;
    private boolean wasResetPressed = false;
    private int leftTriggerToggle = 0;
    private int leftBumperToggle = -1;
    private int liftState = 0;

    @Override
    public void init() {
        // Инициализация подсистем
        intake = new Intake(hardwareMap);
        outtake = new Outtake(hardwareMap);
        intakeMotor = new IntakeController(hardwareMap);
        liftMotors = new LiftsController(hardwareMap);
//        liftMotors = new LiftsController(this);
//        intakeMotor = new IntakeController(this);

        // Инициализация Mecanum Drive
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        backLeft = hardwareMap.get(DcMotor.class, "backLeft");
        backRight = hardwareMap.get(DcMotor.class, "backRight");

        // Установка направления вращения моторов
        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        backLeft.setDirection(DcMotor.Direction.REVERSE);
    }


    @Override
    public void loop() {
        drive();
        codeForLift();
        codeForIntake();
        handleResetButton();

        liftMotors.update();
        outtake.update();
        intakeMotor.update();
        intake.update();
        telemetry.addData("Left Trigger State: ", leftTriggerState);
        telemetry.addData("Left Bumper State: ", leftBumperState);
        telemetry.addData("HIGH Basket: ", leftTriggerToggle);
        telemetry.addData("HIGH Bar: ", leftBumperToggle);
        telemetry.addData("Intake: ", wasRightBumperPressed);
        telemetry.addData("Intake Position: ", wasRightTriggerPressed);
        telemetry.addData("Timer: ", timer.seconds());
        telemetry.update();
    }


    private void drive() {
        double y = -gamepad1.left_stick_y;
        double x = gamepad1.left_stick_x * 1.1;
        double rx = gamepad1.right_stick_x;

        double frontLeftPower = y + x + rx;
        double backLeftPower = y - x + rx;
        double frontRightPower = y - x - rx;
        double backRightPower = y + x - rx;

        double maxPower = Math.max(Math.abs(frontLeftPower), Math.max(Math.abs(backLeftPower),
                Math.max(Math.abs(frontRightPower), Math.abs(backRightPower))));
        if (maxPower > 1.0) {
            frontLeftPower /= maxPower;
            backLeftPower /= maxPower;
            frontRightPower /= maxPower;
            backRightPower /= maxPower;
        }

        frontLeft.setPower(frontLeftPower);
        backLeft.setPower(backLeftPower);
        frontRight.setPower(frontRightPower);
        backRight.setPower(backRightPower);
    }



    private void codeForLift() {

        //help me
        if (gamepad2.left_trigger > 0 && !wasLeftTriggerPressed) {
            timer.reset();
            wasLeftTriggerPressed = true;
            leftTriggerToggle = (leftTriggerToggle + 1) % 2;

            if (leftTriggerToggle == 0) {
                intake.setTransfer();
            } else {
                outtake.dropper.setPosition(Outtake.DROPPER_OPEN);
                liftMotors.setTarget(LiftsController.GROUND);
                outtake.setGrabState();
            }

        }
            if (intake.isTransferComplete) {
                liftMotors.setTarget(LiftsController.HIGHEST_BASKET);
                outtake.setScoreState();
            }
            if (gamepad2.left_trigger == 0) {
                wasLeftTriggerPressed = false;
            }

            //help me



        // Обрабатываем left_bumper
        if (gamepad2.left_bumper && !wasLeftBumperPressed) {
            timer.reset();
            wasLeftBumperPressed = true;
            leftBumperToggle = (leftBumperToggle + 1) % 3;

            if (leftBumperToggle == 0) {
                outtake.setClipsTakeState();
            } if (leftBumperToggle == 1) {
                outtake.setClipsPutState();

                if (outtake.isClipsPutComplete) {
                    liftMotors.setTarget(LiftsController.HIGH_BAR);
                }
            } else if (leftBumperToggle == 2) {
                outtake.setClipsTakeState();
                if (outtake.isClipsTakeComplete) {
                    liftMotors.setTarget(LiftsController.GROUND);
                }
            }
        }
        if (!gamepad2.left_bumper) {
            wasLeftBumperPressed = false;
        }

        telemetry.update();
    }


    private void handleResetButton() {
        if (gamepad2.options && !wasResetPressed) {
            wasResetPressed = true;

            if (leftTriggerToggle > 0) {
                leftTriggerToggle--;
                if (leftTriggerToggle == 0) {
                    liftMotors.setTarget(LiftsController.HIGHEST_BASKET);
                } else {
                    liftMotors.setTarget(LiftsController.GROUND);
                    outtake.setGrabState();
                }
            }

            if (leftBumperToggle > 0) {
                leftBumperToggle--;
                if (leftBumperToggle == 0) {
                    outtake.setClipsTakeState();
                } else if (leftBumperToggle == 1) {
                    outtake.setClipsPutState();
                    liftMotors.setTarget(LiftsController.HIGH_BAR);
                    timer.reset();
                }
            }
        }
        if (!gamepad2.y) wasResetPressed = false;
        timer.reset();
    }


    private void codeForIntake() {
        if (gamepad2.right_trigger > 0 && !wasRightTriggerPressed) {
            wasRightTriggerPressed = true;

//            if (gamepad2.right_trigger > 0 && gamepad2.right_trigger <= 0.7) {
//                intakeMotor.setTarget(IntakeController.MEDIUM);
//            }
            if(gamepad2.right_trigger > 0) {
                intakeMotor.setTarget(IntakeController.LONG);
            }
            liftMotors.setTarget(LiftsController.GROUND);
            intake.setOpenState();
            outtake.setGrabState();
        }

        if (gamepad2.right_trigger == 0) {
            wasRightTriggerPressed = false;
        }

        if (gamepad2.right_bumper && !wasRightBumperPressed) {
            wasRightBumperPressed = true;
            intake.setClosedState();
            timer.reset();
        }

        if (wasRightBumperPressed && intake.isClosedComplete) {
            intakeMotor.setTarget(IntakeController.ZERO);
            wasRightBumperPressed = false;
        }

        double stickX = gamepad2.right_stick_x;

        if (stickX > 0.5) {
            intake.setTurnPosition1();
        } else if (stickX < -0.5) {
            intake.setTurnPosition2();
        } else {
            intake.setTurnDefault();
        }

        telemetry.update();
    }
}