        package Controllers;

import android.graphics.RenderNode;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import SubSystems.Intake;
import SubSystems.Outtake;

public class IntakeController {
    private Intake intake;

    private Servo intakeTurn, intakeGrab;
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
    private boolean liftopened = false;


    public IntakeController(HardwareMap hardwareMap, Intake intake, ExtendoController intakeMotor, LiftsController liftMotors, Outtake outtake, DepositController depositController) {
        this.intake = intake;
        this.intakeMotor = intakeMotor;
        this.liftMotors = liftMotors;
        this.outtake = outtake;
        this.depositController = depositController;

        intakeTurn = hardwareMap.get(Servo.class, "intake_turn");
        intakeGrab = hardwareMap.get(Servo.class, "intake_grab");

    }

    public void update(Gamepad gamepad2, Gamepad gamepad1) {
//        if (Math.abs(gamepad2.left_stick_x) > 0) {
//            int newTarget = intakeMotor.getCurrentTarget() + (int) (gamepad2.left_stick_x * 20);
//            intakeMotor.setTarget(newTarget);
//        }

        if (0 == 0) {
            double rotatecontroll = -gamepad2.left_stick_x;
            double servopos = 0.517 + (rotatecontroll / 3.0);
            intakeTurn.setPosition(servopos);
        }


        if (gamepad1.cross) {
            intakeMotor.forceMove(-0.7);
        } else if (intakeMotor.isForcedMode()) {
            intakeMotor.stopForceMove();
        }

        if (gamepad2.right_bumper) {
            intakeGrab.setPosition(0.55);
        }
        if (gamepad2.triangle && !liftopened) {
            intakeMotor.setTarget(ExtendoController.LONG);
            liftopened = true;
        }
        if (gamepad2.triangle && liftopened) {
            intakeMotor.setTarget(ExtendoController.MINUS_ZERO);
            liftopened = false;
        }
        if (gamepad1.left_trigger > 0 && !wasRightTriggerPressed) {
            wasRightTriggerPressed = true;
            liftopened = true;
            intakeMotor.setTarget(ExtendoController.LONG);
            outtake.setGrabState();
            liftMotors.setTarget(LiftsController.GROUND);
            intake.setOpenState();
            if (depositController != null) {
                depositController.leftBumperToggle = -1;
            }
        }

        if (gamepad1.left_trigger == 0) {
            wasRightTriggerPressed = false;
        }

        if (gamepad2.right_trigger > 0.3 && !rightBumperPressed) {
            rightBumperPressed = true;
            rightBumperToggle = 1;

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

        if (gamepad2.right_trigger < 0.3) {
            rightBumperPressed = false;
        }

//        if (gamepad1.right_bumper) {
//            intake.setClosedState();
//            intakeMotor.setTarget(ExtendoController.ZERO);
//        }




        if (gamepad2.dpad_left) {
            intake.setTurnPosition4();
        }
        if (gamepad2.dpad_right) {
            intake.setTurnPosition2();
        }
        if (gamepad2.dpad_up) {
            intake.setTurnPosition3();
        }

        intakeMotor.update();
        outtake.update();
        intake.update();
    }

    public void resetRightBumperToggle() {
        rightBumperToggle = 0;
    }
}