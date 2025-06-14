package SubSystems;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Gamepad;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import Controllers.DepositController;
import Controllers.DriveController;
import Controllers.ExtendoController;
import Controllers.IntakeController;
import Controllers.LiftsController;
import Controllers.ResetController;

public class Robot {
    public DriveController driveController;
    public DepositController depositController;
    public IntakeController intakeController;
    public Intake intake;
    public Outtake outtake;
    public LiftsController liftMotors;
    public ExtendoController intakeMotor;
    public ResetController resetController;

    public Robot(HardwareMap hardwareMap, Telemetry telemetry) {
        liftMotors = new LiftsController(hardwareMap);
        outtake = new Outtake(hardwareMap, liftMotors);
        intakeMotor = new ExtendoController(hardwareMap);
        intake = new Intake(hardwareMap, intakeMotor, liftMotors, outtake);

        driveController = new DriveController(hardwareMap, telemetry);
        intakeController = new IntakeController(hardwareMap, intake, intakeMotor, liftMotors, outtake, depositController);
        depositController = new DepositController(hardwareMap, liftMotors, outtake, intake, intakeController, intakeMotor);
        resetController = new ResetController(liftMotors, intakeMotor, outtake, intake, intakeController, depositController);
    }


    public void update(Gamepad gamepad1, Gamepad gamepad2, Telemetry telemetry) {
        driveController.drive(gamepad1, gamepad2, telemetry);
        depositController.update(gamepad2, gamepad1);
        intakeController.update(gamepad2, gamepad1);
        resetController.handleResetButton(gamepad2);
    }
}