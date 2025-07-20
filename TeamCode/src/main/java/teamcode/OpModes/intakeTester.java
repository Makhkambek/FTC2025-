package teamcode.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import Controllers.ExtendoController;

@Disabled
@TeleOp(name="ExtendoTestOpMode", group="Test")
public class intakeTester extends LinearOpMode {
    private ExtendoController extendoController;

    @Override
    public void runOpMode() {
        extendoController = new ExtendoController(hardwareMap);
        waitForStart();

        while (opModeIsActive()) {
            if (gamepad1.right_trigger > 0.3) {
                extendoController.setTarget(ExtendoController.LONG);
            } else if (gamepad1.right_bumper) {
                extendoController.setTarget(ExtendoController.ZERO);
            }

            extendoController.update();
            telemetry.addData("Target Position", extendoController.getCurrentTarget());
            telemetry.addData("Current Position", extendoController.getCurrentPosition());
            telemetry.update();
        }
    }
}