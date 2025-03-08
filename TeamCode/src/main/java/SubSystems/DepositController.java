package SubSystems;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;
import SubSystems.LiftsController;
import SubSystems.Outtake;
import SubSystems.Intake;

public class DepositController {
    private LiftsController liftMotors;
    private Outtake outtake;
    private Intake intake;
    private boolean leftBumperPressed = false;
    private int leftBumperToggle = -1;
    private ElapsedTime timer = new ElapsedTime();

    public DepositController(HardwareMap hardwareMap, LiftsController liftMotors, Outtake outtake, Intake intake) {
        this.liftMotors = liftMotors;
        this.outtake = outtake;
        this.intake = intake;
    }

    public void update(Gamepad gamepad2, Gamepad gamepad1) {
        if (Math.abs(gamepad2.left_stick_y) > 0) {
            int newTarget = liftMotors.getCurrentTarget() + (int) (gamepad2.left_stick_y * 100);
            liftMotors.setTarget(newTarget);
        }

        if (gamepad1.dpad_down) {
            liftMotors.forceMove(-0.2);
        } else if (liftMotors.isForcedMode()) {
            liftMotors.stopForceMove();
        }

        if (gamepad2.triangle) {
            intake.setTransfer();
        } else if (gamepad2.circle) {
            timer.reset();
            outtake.setDrop();
        } else if (gamepad2.cross && liftMotors.getCurrentTarget() != LiftsController.GROUND) {
            timer.reset();
            liftMotors.setTarget(LiftsController.GROUND);
        }

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

        liftMotors.update();
    }
}