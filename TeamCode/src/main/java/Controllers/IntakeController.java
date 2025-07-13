package Controllers;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import SubSystems.Intake;
import SubSystems.Outtake;

public class IntakeController {
    private Intake intake;
    private ExtendoController intakeMotor;
    private LiftsController liftMotors;
    private Outtake outtake;
    private DepositController depositController;
    private ColorSensorController colorSensorController;
    private boolean wasRightTriggerPressed = false;
    private boolean wasLeftBumperPressed = false;
    private boolean wasDpadLeftPressed = false;
    private boolean wasDpadRightPressed = false;
    private boolean rightBumperPressed = false;
    public int rightBumperToggle = 0;
    private int intakeTurnState = 0;
    private ElapsedTime timer = new ElapsedTime();
    private boolean isInitialPositionActive = false;
    private static final double AUTO_DETECTION_DISTANCE_CM = 0.7;
    private static final double INITIAL_POSITION_TIMEOUT = 1.0;

    // Конструктор
    public IntakeController(HardwareMap hardwareMap, Intake intake, ExtendoController intakeMotor,
                            LiftsController liftMotors, Outtake outtake, DepositController depositController) {
        this.intake = intake;
        this.intakeMotor = intakeMotor;
        this.liftMotors = liftMotors;
        this.outtake = outtake;
        this.depositController = depositController;
        this.colorSensorController = new ColorSensorController(hardwareMap);
    }

    public void update(Gamepad gamepad2, Gamepad gamepad1, Telemetry telemetry) {
        telemetry.addData("Gamepad1 Left Bumper", gamepad1.left_bumper);
        telemetry.addData("Gamepad2 Right Trigger", gamepad2.right_trigger);
        telemetry.addData("Intake State", intake.getCurrentState());
        telemetry.addData("Specimen Complete", intake.isSpecimenComplete);
        if (colorSensorController != null) {
            telemetry.addData("Color RGB", "R:%d G:%d B:%d",
                    colorSensorController.getRed("intake"),
                    colorSensorController.getGreen("intake"),
                    colorSensorController.getBlue("intake"));
            telemetry.addData("Is Red", colorSensorController.isRed("intake"));
            telemetry.addData("Is Blue", colorSensorController.isBlue("intake"));
            telemetry.addData("Is Yellow", colorSensorController.isYellow("intake"));
        }

        if (Math.abs(gamepad2.right_stick_x) > 0) {
            int newTarget = intakeMotor.getCurrentTarget() + (int) (gamepad2.right_stick_x * 20);
            intakeMotor.setTarget(newTarget);
        }

        if (gamepad2.right_trigger > 0.1 && !wasRightTriggerPressed) {
            wasRightTriggerPressed = true;
            telemetry.addData("Intake Action", "Правый триггер нажат, открытие интейка");
            outtake.setDrop();
            intake.setOpenState();
            intakeMotor.setTarget(ExtendoController.LONG);
//            liftMotors.setTarget(LiftsController.GROUND);
            if (depositController != null) {
                depositController.leftBumperToggle = -1;
            }
            rightBumperToggle = 0;
            isInitialPositionActive = false;
            intake.isSpecimenComplete = false;
        }

        if (gamepad2.right_trigger <= 0.1) {
            wasRightTriggerPressed = false;
        }

        if (gamepad1.left_bumper && !wasLeftBumperPressed) {
            wasLeftBumperPressed = true;
            telemetry.addData("Intake Action", "Левый бампер нажат, установка specimen");
            intake.setSpecimen();
            rightBumperToggle = 0;
            isInitialPositionActive = false;
        }

        if (!gamepad1.left_bumper) {
            wasLeftBumperPressed = false;
        }

        if (gamepad2.right_bumper && !rightBumperPressed) {
            rightBumperPressed = true;
            rightBumperToggle = (rightBumperToggle + 1) % 2;

            if (rightBumperToggle == 0) {
                timer.reset();
                intake.setOppenerState();
                intake.isOpenComplete = false;
                isInitialPositionActive = false;
            } else if (rightBumperToggle == 1) {
                timer.reset();
                intake.setClosedState();
                intake.isClosedComplete = false;
                isInitialPositionActive = false;
            }
        }

        if (!gamepad2.right_bumper) {
            rightBumperPressed = false;
        }

        if (rightBumperToggle == 1 && colorSensorController != null) {
            double distanceCm = colorSensorController.getDistance(DistanceUnit.CM, "intake");
            telemetry.addData("Intake Distance", "%.2f см", distanceCm);

            if (!isInitialPositionActive && intake.getCurrentState() != Intake.State.SPECIMEN) {
                if (distanceCm < AUTO_DETECTION_DISTANCE_CM && distanceCm >= 0) {
                    if (colorSensorController.isBlue("intake") || colorSensorController.isYellow("intake")) {
                        telemetry.addData("Intake Action", "InitialPosition активирован");
                        intake.initialPosition();
                        isInitialPositionActive = true;
                        timer.reset();
                    } else if (colorSensorController.isRed("intake")) {
                        telemetry.addData("Intake Action", "Обнаружен красный, открытие интейка");
                        intake.setOpenState();
                        intake.isOpenComplete = false;
                        rightBumperToggle = 0;
                        isInitialPositionActive = false;
                    }
                }
            }

            if (isInitialPositionActive && intake.getCurrentState() != Intake.State.SPECIMEN) {
                if (intake.isInitialPositionComplete || timer.seconds() > INITIAL_POSITION_TIMEOUT) {
                    telemetry.addData("Intake Action", "InitialPosition завершен");
                    isInitialPositionActive = false;
                }
            }
        }

        if (isInitialPositionActive) {
            if (colorSensorController.isBlue("intake") || colorSensorController.isYellow("intake")) {
                telemetry.addData("Intake Action", "InitialPosition in progress, color detected");
                if (intake.isInitialPositionComplete || timer.seconds() > INITIAL_POSITION_TIMEOUT) {
                    telemetry.addData("Intake Action", "InitialPosition completed");
                    isInitialPositionActive = false;
                }
            } else {
                telemetry.addData("Intake Action", "Invalid color or no color, reverting to OpenState");
                intakeMotor.setTarget(ExtendoController.LONG);
                intake.setOpenState();
                intake.isOpenComplete = false;
                rightBumperToggle = 0;
                isInitialPositionActive = false;
            }
    }

        if (gamepad1.triangle) {
            intakeMotor.forceMove(-0.5);
        } else if (intakeMotor.isForcedMode()) {
            intakeMotor.stopForceMove();
        }

        if (gamepad2.dpad_left && !wasDpadLeftPressed) {
            wasDpadLeftPressed = true;
            if (intakeTurnState >= 3) {
                intakeTurnState = 1;
            } else {
                intakeTurnState = Math.min(intakeTurnState + 1, 2);
            }
            if (intakeTurnState == 1) {
                intake.setTurnPosition4();
            } else if (intakeTurnState == 2) {
                intake.setTurnPosition2();
            }
        }
        if (!gamepad2.dpad_left) wasDpadLeftPressed = false;

        if (gamepad2.dpad_right && !wasDpadRightPressed) {
            wasDpadRightPressed = true;
            if (intakeTurnState <= 2) {
                intakeTurnState = 3;
            } else {
                intakeTurnState = Math.min(intakeTurnState + 1, 4);
            }
            if (intakeTurnState == 3) {
                intake.setTurnPosition3();
            } else if (intakeTurnState == 4) {
                intake.setTurnPosition1();
            }
        }
        if (!gamepad2.dpad_right) wasDpadRightPressed = false;

        if (gamepad2.dpad_up) {
            intakeTurnState = 0;
            intake.setTurnDefault();
        }

        intakeMotor.update();
        outtake.update();
        intake.update();

        telemetry.addData("Right Bumper Toggle", rightBumperToggle);
        telemetry.addData("Initial Position Active", isInitialPositionActive);
        telemetry.addData("Intake Current State", intake.getCurrentState());
        telemetry.addData("Is Initial Position Complete", intake.isInitialPositionComplete);
    }

    public void resetRightBumperToggle() {
        rightBumperToggle = 0;
        isInitialPositionActive = false;
    }
}