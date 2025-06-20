package Controllers;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import SubSystems.Intake;
import SubSystems.Outtake;

public class IntakeController {
    private Intake intake;
    private ExtendoController intakeMotor;
    private LiftsController liftMotors;
    private Outtake outtake;
    private DepositController depositController;
    private boolean wasRightTriggerPressed = false;
    private boolean wasDpadLeftPressed = false;
    private boolean wasDpadRightPressed = false;
    private boolean rightBumperPressed = false;
    public int rightBumperToggle = 0;
    private int intakeTurnState = 0;
    private ElapsedTime timer = new ElapsedTime();

    public IntakeController(HardwareMap hardwareMap, Intake intake, ExtendoController intakeMotor, LiftsController liftMotors, Outtake outtake, DepositController depositController) {
        this.intake = intake;
        this.intakeMotor = intakeMotor;
        this.liftMotors = liftMotors;
        this.outtake = outtake;
        this.depositController = depositController;
    }

    public void update(Gamepad gamepad2, Gamepad gamepad1) {
        if (Math.abs(gamepad2.right_stick_x) > 0) {
            int newTarget = intakeMotor.getCurrentTarget() + (int) (gamepad2.right_stick_x * 20);
            intakeMotor.setTarget(newTarget);
        }

        if (gamepad2.right_trigger > 0 && !wasRightTriggerPressed) {
            wasRightTriggerPressed = true;
            outtake.setDrop();
            intake.setOpenState();
            intakeMotor.setTarget(ExtendoController.LONG);
            liftMotors.setTarget(LiftsController.GROUND);
            if (depositController != null) {
                depositController.leftBumperToggle = -1;
            }
        }

        if (gamepad2.right_trigger == 0) {
            wasRightTriggerPressed = false;
        }

        if (gamepad2.right_bumper && !rightBumperPressed) {
            rightBumperPressed = true;
            rightBumperToggle = (rightBumperToggle + 1) % 2;

            if (rightBumperToggle == 0) {
                timer.reset();
                intake.setOpenState();
                intake.isOpenComplete = false;
            } else if (rightBumperToggle == 1) {
                timer.reset();
                intake.setClosedState();
                intake.isClosedComplete = false;
            }
        }

        if (!gamepad2.right_bumper) {
            rightBumperPressed = false;
        }

        if (gamepad1.right_bumper) {
            intakeMotor.setTarget(ExtendoController.ZERO);
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
    }

    public void resetRightBumperToggle() {
        rightBumperToggle = 0;
    }
}