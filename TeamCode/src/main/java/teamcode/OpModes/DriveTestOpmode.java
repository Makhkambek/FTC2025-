package teamcode.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import Controllers.DriveController;

@TeleOp(name="DriveTestOpMode", group="Test")
//@Disabled
public class DriveTestOpmode extends LinearOpMode {
    private DriveController driveController;

    @Override
    public void runOpMode() {
        driveController = new DriveController(hardwareMap, telemetry);

        telemetry.addData("Status", "Initialized. Waiting for start...");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {
            driveController.drive(gamepad1, gamepad2, telemetry);

            telemetry.addData("Status", "Running");
            telemetry.update();
        }
    }
}