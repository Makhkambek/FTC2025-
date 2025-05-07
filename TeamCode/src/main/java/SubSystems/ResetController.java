package SubSystems;

import com.qualcomm.robotcore.hardware.Gamepad;

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
        // Прямой сброс публичных переменных
        intakeController.rightBumperToggle = 0; // Сбрасываем rightBumperToggle
        depositController.leftBumperToggle = -1; // Сбрасываем leftBumperToggle

        // Сброс состояний подсистем
        intakeMotor.setTarget(ExtendoController.ZERO);
        liftMotors.setTarget(LiftsController.GROUND);
        outtake.setGrabState();
        intake.setOpenState();

        // Сброс флага сброса
        wasResetPressed = false;
    }
}