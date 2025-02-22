package teamcode.Drive;


import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.hardware.bosch.BHI260IMU;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.hardware.bosch.BNO055IMU;
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

    private DcMotor leftFront;
    private DcMotor rightFront;
    private DcMotor leftRear;
    private DcMotor rightRear;
    private ElapsedTime timer = new ElapsedTime();
//    private BNO055IMU imu;

    private enum LeftTriggerState {
        LIFT_TO_HIGHEST, DROPPER_OPEN_AND_RESET
    }

    // Состояния для left_bumper
    private enum LeftBumperState {
        CLIPS_TAKE, CLIPS_PUT_AND_HIGH_BAR, HIGH_BAR_PUT_AND_RESET
    }

    private LeftTriggerState leftTriggerState = LeftTriggerState.LIFT_TO_HIGHEST;
    private LeftBumperState leftBumperState = LeftBumperState.CLIPS_TAKE;

    private boolean wasRightBumperPressed = false;
    private boolean wasRightTriggerPressed = false;
    private boolean wasResetPressed = false;
    private int leftTriggerToggle = -1;
    private int leftBumperToggle = -1;
    private int intakeTurnState = 0; // 0 - Default, 1 - 45 градусов, 2 - 90 градусов
    private boolean wasDpadLeftPressed = false;
    private boolean wasDpadRightPressed = false;
    boolean previousLeftTrigger = false;
    boolean previousLeftBumper = false;
    private boolean leftTriggerPressed = false;
    private boolean leftBumperPressed = false;

    @Override
    public void init() {
        deltaTimer = new ElapsedTime();
        intakeMotor = new IntakeController(hardwareMap);
        outtake = new Outtake(hardwareMap);
        liftMotors = new LiftsController(hardwareMap);
        intake = new Intake(hardwareMap, intakeMotor, liftMotors, outtake);

        // Инициализация Mecanum Drive
        leftFront = hardwareMap.get(DcMotor.class, "leftFront");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        leftRear = hardwareMap.get(DcMotor.class, "leftRear");
        rightRear = hardwareMap.get(DcMotor.class, "rightRear");

        leftFront.setDirection(DcMotor.Direction.REVERSE);
        leftRear.setDirection(DcMotor.Direction.REVERSE);
        outtake.setPreloadState();
    }

    ElapsedTime deltaTimer;
    double deltaTime = 0;

    @Override
    public void start(){
        deltaTimer.reset();
    }
    @Override
    public void loop() {
        deltaTime = deltaTimer.milliseconds();
        deltaTimer.reset();
        drive();
        codeForLift(deltaTime);
        codeForIntake();
        handleResetButton();
        previousLeftTrigger = gamepad2.left_trigger > 0;
        previousLeftBumper = gamepad2.left_bumper;

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
        telemetry.addData("IntakePower", intakeMotor.power);
        telemetry.addData("prev button:", previousLeftTrigger);
        telemetry.addData("lifts hight", liftMotors.getCurrentPosition());
        telemetry.update();

    }


    private void drive() {
        double slowModeFactor = gamepad1.right_trigger > 0 ? 0.3 : 1.0;

        double y = -gamepad1.left_stick_y * slowModeFactor; // Вперед-назад
        double x = gamepad1.left_stick_x * 1.1 * slowModeFactor; // Влево-вправо
        double rx = gamepad1.right_stick_x * slowModeFactor; // Поворот


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

        leftFront.setPower(frontLeftPower);
        leftRear.setPower(backLeftPower);
        rightFront.setPower(frontRightPower);
        rightRear.setPower(backRightPower);

//        telemetry.addData("Heading", currentHeading);
//        telemetry.addData("Target Heading", targetHeading);
//        telemetry.addData("Heading Lock Active", headingLockActive);
        telemetry.update();
    }



    private void codeForLift(double deltaTime) {

        if (Math.abs(gamepad2.left_stick_y) > 0) {
            int newTarget = liftMotors.getCurrentTarget() + (int) (gamepad2.left_stick_y * 100);
            liftMotors.setTarget(newTarget);
        }

//            if (leftBumperToggle == 0 && outtake.isClipsTakeComplete) {
//                    liftMotors.setTarget(LiftsController.GROUND);
//                }
//
//
//                if (leftBumperToggle == 2 && outtake.isClipsPutComplete) {
//                    liftMotors.setTarget(LiftsController.HIGH_BAR);
//                }
//            }

            liftMotors.update();


                if(gamepad2.triangle) {
                    intake.setTransfer();
                } else if(gamepad2.circle) {
                    timer.reset();
                    outtake.setDrop();
                } else if(gamepad2.cross && liftMotors.getCurrentTarget() != LiftsController.GROUND) {
                    timer.reset();
                    liftMotors.setTarget(LiftsController.GROUND);
                }



//        if (gamepad2.left_trigger > 0 && !leftTriggerPressed ) {
//            leftTriggerPressed = true;
//            leftTriggerToggle = (leftTriggerToggle + 1) % 3;
//
//            if (leftTriggerToggle == 0) {
//                intake.setTransfer();
//            } else if (leftTriggerToggle == 1) {
//                timer.reset();
//                outtake.setDrop();
//            } else if (leftTriggerToggle == 2 && liftMotors.getCurrentTarget() != LiftsController.GROUND) {
//                timer.reset();
//                liftMotors.setTarget(LiftsController.GROUND);
//            }
//        }
//
//        if (gamepad2.left_trigger < 0.1) {
//            leftTriggerPressed = false;
//        }

        if (gamepad2.left_bumper && !leftBumperPressed) {
            leftBumperPressed = true;
            leftBumperToggle = (leftBumperToggle + 1) % 3;

            if (leftBumperToggle == 0) {
                timer.reset();
                outtake.setClipsTakeState();
                outtake.isClipsTakeComplete = false;
            } else if (leftBumperToggle == 1) {
                timer.reset();
                outtake.setClipsPutState();
                outtake.isClipsPutComplete = false;
            } else if (leftBumperToggle == 2) {
                timer.reset();
                liftMotors.setTarget(LiftsController.HIGH_BAR);
            }
        }

        if (!gamepad2.left_bumper) {
            leftBumperPressed = false;

        }

//        liftMotors.deviatePosition(gamepad2.left_stick_y, deltaTime);
////        liftMotors.deviatePosition(gamepad1.right_trigger, deltaTime);
//
//        if (leftBumperToggle == 0 && outtake.isClipsTakeComplete) {
//            liftMotors.setTarget(LiftsController.GROUND);
//        }
//
//        if (leftBumperToggle == 2 && outtake.isClipsPutComplete) {
//            liftMotors.setTarget(LiftsController.HIGH_BAR);
//        }
//        liftMotors.update();
    }


    private void handleResetButton() {
        if (gamepad2.options && !wasResetPressed) {
            wasResetPressed = true;
            resetControls();
        }
        if (!gamepad2.options) wasResetPressed = false;
    }

    private void resetControls() {
        leftTriggerToggle = 0;  // Сброс состояний лифта
        leftBumperToggle = 0;   // Сброс состояний клипсов
        wasRightBumperPressed = false;
        wasRightTriggerPressed = false;
        wasResetPressed = false;
        wasDpadLeftPressed = false;
        wasDpadRightPressed = false;

        intake.setClosedState();
        intakeMotor.setTarget(IntakeController.ZERO);
        liftMotors.setTarget(LiftsController.GROUND);
        outtake.setGrabState();
    }


    private void codeForIntake() {
        if (gamepad2.right_trigger > 0 && !wasRightTriggerPressed) {
            wasRightTriggerPressed = true;

            if (gamepad2.right_trigger > 0) {
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
//            intakeMotor.setTarget(IntakeController.ZERO);
            wasRightBumperPressed = false;
        }

        if(gamepad1.right_bumper) {
            intakeMotor.setTarget(IntakeController.ZERO);
            intake.setClosedState();
        }
        telemetry.update();


        if (gamepad2.dpad_left && !wasDpadLeftPressed) {
            wasDpadLeftPressed = true;

            if (intakeTurnState >= 3) {
                intakeTurnState = 1;
            } else {
                intakeTurnState = Math.min(intakeTurnState + 1, 2); // 0 → 1 → 2 → 2
            }

            if (intakeTurnState == 1) {
                intake.setTurnPosition4(); // 45° влево.  4
            } else if (intakeTurnState == 2) {
                intake.setTurnPosition2(); // 90° влево.   2
            }
        }
        if (!gamepad2.dpad_left) wasDpadLeftPressed = false;


        if (gamepad2.dpad_right && !wasDpadRightPressed) {
            wasDpadRightPressed = true;

            // Если уже был поворот влево — сбросить и начать с правого
            if (intakeTurnState <= 2) {
                intakeTurnState = 3;
            } else {
                intakeTurnState = Math.min(intakeTurnState + 1, 4); // 0 → 3 → 4 → 4
            }

            if (intakeTurnState == 3) {
                intake.setTurnPosition3(); // 45° вправо.  3
            } else if (intakeTurnState == 4) {
                intake.setTurnPosition1(); // 90° вправо.   1
            }
        }
        if (!gamepad2.dpad_right) wasDpadRightPressed = false;


        if (gamepad2.dpad_up) {
            intakeTurnState = 0;
            intake.setTurnDefault();
        }
    }

    }