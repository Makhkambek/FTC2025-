package Controllers;

import com.qualcomm.robotcore.hardware.Gamepad;

import SubSystems.Intake;
import SubSystems.Outtake;

public class ResetController {
    private LiftsController liftMotors;
    private ExtendoController intakeMotor;
    private Outtake outtake;
    private Intake intake;
    private IntakeController intakeController;
    private DepositController depositController;
    private boolean wasResetPressed = false;

    public ResetController(LiftsController liftMotors, ExtendoController intakeMotor, Outtake outtake, Intake intake, IntakeController intakeController, DepositController depositController) {
        this.liftMotors = liftMotors;
        this.intakeMotor = intakeMotor;
        this.outtake = outtake;
        this.intake = intake;
        this.intakeController = intakeController;
        this.depositController = depositController;
    }

    public void handleResetButton(Gamepad gamepad2) {
        if (gamepad2.options && !wasResetPressed) {
            wasResetPressed = true;
            resetControls();
        }
        if (!gamepad2.options) wasResetPressed = false;
    }

    private void resetControls() {

        intakeController.rightBumperToggle = 0; 
        depositController.leftBumperToggle = -1; 


        intakeMotor.setTarget(ExtendoController.ZERO);
        liftMotors.setTarget(LiftsController.GROUND);
        outtake.setDrop();
        intake.setOpenState();


        wasResetPressed = false;
    }
}
