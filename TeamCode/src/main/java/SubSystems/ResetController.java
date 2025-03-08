package SubSystems;

import com.qualcomm.robotcore.hardware.Gamepad;

public class ResetController {
    private int leftBumperToggle = 0;
    private boolean wasResetPressed = false;

    private LiftsController liftMotors;

    public ResetController(LiftsController liftMotors) {
        this.liftMotors = liftMotors;
    }

    public void handleResetButton(Gamepad gamepad2) {
        if (gamepad2.options && !wasResetPressed) {
            wasResetPressed = true;
            adjustLeftBumper();
        }
        if (!gamepad2.options) wasResetPressed = false;
    }

    private void adjustLeftBumper() {
        if (leftBumperToggle > 0) {
            leftBumperToggle--;
        }
    }
}