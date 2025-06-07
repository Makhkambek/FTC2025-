package SubSystems;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

public class DepositController {
    private LiftsController liftMotors;
    private Outtake outtake;
    private Intake intake;
    public IntakeController intakeController;
    private ExtendoController extendoController;
    public int leftBumperToggle = -1;
    private boolean leftBumperPressed = false;
    private ElapsedTime timer = new ElapsedTime();

    public DepositController(HardwareMap hardwareMap, LiftsController liftMotors, Outtake outtake, Intake intake, IntakeController intakeController, ExtendoController extendoController) {
        this.liftMotors = liftMotors;
        this.outtake = outtake;
        this.intake = intake;
        this.intakeController = intakeController;
        this.extendoController = extendoController;
    }

    public void update(Gamepad gamepad2, Gamepad gamepad1) {
        if (Math.abs(gamepad2.left_stick_y) > 0) {
            int newTarget = liftMotors.getCurrentTarget() + (int) (-gamepad2.left_stick_y * 50);
            liftMotors.setTarget(newTarget);
        }

        if (gamepad1.cross) {
            liftMotors.forceMove(-0.2);
        } else if (liftMotors.isForcedMode()) {
            liftMotors.stopForceMove();
        }

        if (gamepad2.left_trigger > 0) {
            intake.setTransfer();
            intakeController.resetRightBumperToggle();
        } else if (gamepad2.circle) {
            timer.reset();
            outtake.setDrop();
        } else if (gamepad2.cross && liftMotors.getCurrentTarget() != LiftsController.GROUND) {
            timer.reset();
            liftMotors.setTarget(LiftsController.GROUND);
        }

        if (gamepad2.left_bumper && !leftBumperPressed) {
            extendoController.setTarget(ExtendoController.ZERO);
            intake.setOpenState();
            leftBumperPressed = true;
            leftBumperToggle = (leftBumperToggle + 1) % 2;

            if (leftBumperToggle == 0) {
                timer.reset();
                outtake.setClipsTakeState();
                outtake.isClipsTakeComplete = false;
            } else if (leftBumperToggle == 1) {
                timer.reset();
                outtake.setClipsPutState();
                outtake.isClipsPutComplete = false;
            }
        }

        if (!gamepad2.left_bumper) {
            leftBumperPressed = false;
        }

        liftMotors.update();
        outtake.update();
        intake.update();
    }
}