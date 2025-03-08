package teamcode.Drive;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import SubSystems.DriveController;
import SubSystems.DepositController;
import SubSystems.ExtendoController;
import SubSystems.Intake;
import SubSystems.IntakeController;
import SubSystems.LiftsController;
import SubSystems.Outtake;
import SubSystems.ResetController;

@TeleOp(name="DriveTrain")
public class DriveTrain extends OpMode {
    private DriveController driveController;
    private DepositController depositController;
    private IntakeController intakeController;
    private Intake intake;
    private Outtake outtake;
    private LiftsController liftMotors;
    private ExtendoController intakeMotor;
    private ResetController resetController;

    @Override
    public void init() {
        liftMotors = new LiftsController(hardwareMap);
        outtake = new Outtake(hardwareMap);
        intakeMotor = new ExtendoController(hardwareMap);
        intake = new Intake(hardwareMap, intakeMotor, liftMotors, outtake);
        driveController = new DriveController(hardwareMap);
        depositController = new DepositController(hardwareMap, liftMotors, outtake, intake);
        intakeController = new IntakeController(hardwareMap, intake, intakeMotor, liftMotors, outtake);
        resetController = new ResetController(liftMotors);
    }

    @Override
    public void start() {
        driveController.startFollower();
    }

    @Override
    public void loop() {
        driveController.drive(gamepad1);
        depositController.update(gamepad2, gamepad1);
        intakeController.update(gamepad2, gamepad1);
        resetController.handleResetButton(gamepad2);
    }
}