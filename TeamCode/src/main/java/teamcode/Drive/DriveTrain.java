package teamcode.Drive;


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
    private int leftTriggerToggle = 0;
    private int leftBumperToggle = 0;

    @Override
    public void init() {
        // Инициализация подсистем
        intake = new Intake(hardwareMap);
        outtake = new Outtake(hardwareMap);
        liftMotors = new LiftsController(this);
        intakeMotor = new IntakeController(this);

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
        if (gamepad2.left_trigger > 0 && !wasLeftTriggerPressed) {
            wasLeftTriggerPressed = true;
            leftTriggerToggle++;

            if (leftTriggerToggle > 1) {
                leftTriggerToggle = 0;
            }

            if (leftTriggerToggle == 0) {
                if (timer.seconds() < 0.5) {
                    intake.setTransfer();
                }
                liftMotors.moveToPosition(LiftsController.HIGHEST_BASKET);
            } else if (leftTriggerToggle == 1) {
                outtake.dropper.setPosition(Outtake.DROPPER_OPEN);
                liftMotors.moveToPosition(LiftsController.GROUND);
                outtake.setGrabState();
            }
        }
        if (gamepad2.left_trigger == 0) {
            wasLeftTriggerPressed = false;
        }

        if (gamepad2.left_bumper && !wasLeftBumperPressed) {
            wasLeftBumperPressed = true;
            leftBumperToggle++;

            if (leftBumperToggle > 2) {
                leftBumperToggle = 0;
            }

            if (leftBumperToggle == 0) {
                outtake.setClipsTakeState();
            } else if (leftBumperToggle == 1) {
                outtake.setClipsPutState();
                liftMotors.moveToPosition(LiftsController.HIGH_BAR);
                timer.reset();
            } else if (leftBumperToggle == 2) {
                if (timer.seconds() < 1.0) {
                    outtake.setClipsTakeState();
                    liftMotors.moveToPosition(LiftsController.GROUND);
                    timer.reset();
                }
            }
        }
        if (!gamepad2.left_bumper) {
            wasLeftBumperPressed = false;
        }

        outtake.update();
    }


    private void codeForIntake() {
        if (gamepad2.right_trigger > 0 && !wasRightTriggerPressed) {
            wasRightTriggerPressed = true;

            if (gamepad2.right_trigger <= 0.5) {
                intakeMotor.moveToPosition(IntakeController.MEDIUM);
            } else if (gamepad2.right_trigger > 0.6) {
                intakeMotor.moveToPosition(IntakeController.LONG);
            }

            intake.setOpenState();
            outtake.setGrabState();
        }

        if (gamepad2.right_trigger == 0) {
            wasRightTriggerPressed = false;
        }

        if (gamepad2.right_bumper && !wasRightBumperPressed) {
            wasRightBumperPressed = true;
            timer.reset();
        }
        if (wasRightBumperPressed) {
            double timeElapsed = timer.seconds();

            if (timeElapsed < 0.5) {
                intake.setClosedState();
            } else if (timeElapsed < 0.9) {
                intakeMotor.moveToPosition(IntakeController.ZERO);
            } else {
                wasRightBumperPressed = false;
            }
        }

        double stickX = gamepad2.right_stick_x;

        if (stickX > 0.5) {
            intake.setTurnPosition1();
        } else if (stickX < -0.5) {
            intake.setTurnPosition2();
        } else {
            intake.setTurnDefault();
        }

        intake.update();
        outtake.update();
    }
}