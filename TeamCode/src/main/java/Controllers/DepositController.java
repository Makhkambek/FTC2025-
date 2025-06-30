package Controllers;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import SubSystems.Intake;
import SubSystems.Outtake;

public class DepositController {
    private LiftsController liftMotors;
    private Outtake outtake;
    private Intake intake;
    public IntakeController intakeController;
    private ExtendoController extendoController;
    private ColorSensorController colorSensorController;
    public int leftBumperToggle = -1;
    private boolean leftBumperPressed = false;
    private ElapsedTime timer = new ElapsedTime();
    private static final double AUTO_DETECTION_DISTANCE_CM = 2.5;

    public DepositController(HardwareMap hardwareMap, LiftsController liftMotors, Outtake outtake, Intake intake,
                             IntakeController intakeController, ExtendoController extendoController) {
        this.liftMotors = liftMotors;
        this.outtake = outtake;
        this.intake = intake;
        this.intakeController = intakeController;
        this.extendoController = extendoController;
        this.colorSensorController = new ColorSensorController(hardwareMap);
    }

    public void update(Gamepad gamepad2, Gamepad gamepad1, Telemetry telemetry) {
        if (Math.abs(gamepad2.left_stick_y) > 0) {
            int newTarget = liftMotors.getCurrentTarget() + (int) (-gamepad2.left_stick_y * 50);
            liftMotors.setTarget(newTarget);
        }

        if (gamepad1.cross) {
            liftMotors.forceMove(-0.2);
        } else if (liftMotors.isForcedMode()) {
            liftMotors.stopForceMove();
        }

        if (gamepad2.left_trigger > 0) {
            intake.setTransfer();
            intakeController.resetRightBumperToggle();
        }

        if (gamepad2.circle) {
            timer.reset();
            outtake.setDrop();
        }

        if (gamepad2.cross && liftMotors.getCurrentTarget() != LiftsController.GROUND) {
            timer.reset();
            liftMotors.setTarget(LiftsController.GROUND);
        }

        if (gamepad2.left_bumper && !leftBumperPressed) {
            telemetry.addData("Deposit Action", "Left bumper toggled");
            extendoController.setTarget(ExtendoController.ZERO);
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
                telemetry.addData("Deposit Action", "Auto ClipsPut triggered");
                timer.reset();
                outtake.setClipsPutState();
                outtake.isClipsPutComplete = false;
                leftBumperToggle = 1;
            }
        }

        telemetry.addData("Lift Target", liftMotors.getCurrentTarget());
        telemetry.addData("Left Bumper Toggle", leftBumperToggle);

        liftMotors.update();
        outtake.update();
        intake.update();
    }
}