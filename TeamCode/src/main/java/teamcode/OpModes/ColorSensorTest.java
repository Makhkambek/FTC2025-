package teamcode.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import Controllers.ColorSensorController;
import Controllers.ExtendoController;
import Controllers.LiftsController;
import SubSystems.Intake;
import SubSystems.Outtake;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

@TeleOp(name="ColorSensorTestOpMode", group="Test")
public class ColorSensorTest extends LinearOpMode {
    private ColorSensorController colorSensorController;
    private ExtendoController intakeMotor;
    private LiftsController lifts;
    private Intake intake;
    private Outtake outtake;
    private ElapsedTime timer;
    private boolean leftBumperPressed = false;
    private int leftBumperToggle = 0;
    private static final double AUTO_DETECTION_DISTANCE_CM = 1.5;

    @Override
    public void runOpMode() {
        colorSensorController = new ColorSensorController(hardwareMap);
        timer = new ElapsedTime();

        try {
            intakeMotor = new ExtendoController(hardwareMap);
            lifts = new LiftsController(hardwareMap);
            outtake = new Outtake(hardwareMap, lifts);
            intake = new Intake(hardwareMap, intakeMotor, lifts, outtake);
        } catch (Exception e) {
            telemetry.addData("Error", "Failed to initialize controllers: " + e.getMessage());
        }

        telemetry.addData("Status", "Initialized. Waiting for start...");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {
            colorSensorController.update(telemetry);

            if (gamepad2.left_bumper && !leftBumperPressed) {
                intakeMotor.setTarget(ExtendoController.ZERO);
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

            if (leftBumperToggle == 0 && colorSensorController != null) {
                double distanceCm = colorSensorController.getDistance(DistanceUnit.CM);
                if (distanceCm <= AUTO_DETECTION_DISTANCE_CM && distanceCm >= 0 &&
                        (colorSensorController.isRed() || colorSensorController.isBlue())) {
                    timer.reset();
                    outtake.setClipsPutState();
                    outtake.isClipsPutComplete = false;
                    leftBumperToggle = 1;
                }
            }

            telemetry.addData("Status", "Running");
            telemetry.addData("Left Bumper Toggle", leftBumperToggle);
            telemetry.update();
        }
    }
}