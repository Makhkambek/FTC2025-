package SubSystems;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Gamepad;

public class Robot {
    public DriveController driveController;
    public DepositController depositController;
    public IntakeController intakeController;
    public Intake intake;
    public Outtake outtake;
    public LiftsController liftMotors;
    public ExtendoController intakeMotor;
    public ResetController resetController;

    public Robot(HardwareMap hardwareMap) {
        liftMotors = new LiftsController(hardwareMap);
        outtake = new Outtake(hardwareMap);
        intakeMotor = new ExtendoController(hardwareMap);
        intake = new Intake(hardwareMap, intakeMotor, liftMotors, outtake);

        driveController = new DriveController(hardwareMap);
        depositController = new DepositController(hardwareMap, liftMotors, outtake, intake);
        intakeController = new IntakeController(hardwareMap, intake, intakeMotor, liftMotors, outtake);
        resetController = new ResetController(liftMotors);
    }

    public void start() {
        driveController.startFollower();
    }

    public void update(Gamepad gamepad1, Gamepad gamepad2) {
        driveController.drive(gamepad1);
        depositController.update(gamepad2, gamepad1);
        intakeController.update(gamepad2, gamepad1);
        resetController.handleResetButton(gamepad2);
    }
}