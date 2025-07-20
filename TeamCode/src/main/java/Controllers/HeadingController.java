package Controllers;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.controller.PIDController;
import com.pedropathing.localization.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.Telemetry;

@Config
public class HeadingController {
    private GoBildaPinpointDriver pinpoint;
    private static double currentHeading = 0;
    private double currentRaw = 0;
    private static double targetHeading = 0;
    private double prevRawHeading = 0;
    private boolean isHeadingLocked = false;

    public static double incrementCoefficient = 180;
    public static double kP = 0.024;
    public static double kI = 0.0;
    public static double kD = 0.0003;
    public static double kF = 0.005;    //0.01

    private final PIDController controller;
    private double headingOffset;
    private boolean wasNan = false;
    private final ElapsedTime resetTimer = new ElapsedTime();

    public HeadingController(HardwareMap hardwareMap) {
        pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, "pinpoint");
        pinpoint.setOffsets(-107.95, -63.5);
        pinpoint.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        pinpoint.setEncoderDirections(GoBildaPinpointDriver.EncoderDirection.REVERSED, GoBildaPinpointDriver.EncoderDirection.FORWARD);
        pinpoint.resetPosAndIMU();

        controller = new PIDController(kP, kI, kD);
        resetTimer.reset();
    }

    public void update(Telemetry telemetry) {
        pinpoint.update(GoBildaPinpointDriver.readData.ONLY_UPDATE_HEADING);
        double rawHeading = (pinpoint.getHeading() / (Math.PI * 2)) * 360.0;
        currentRaw = rawHeading;

        double deltaHeading = currentRaw - prevRawHeading;
        prevRawHeading = currentRaw;

        if (Double.isFinite(deltaHeading)) {
            currentHeading += deltaHeading;
        } else {
            wasNan = true;
        }

        while (currentHeading - targetHeading > 180) {
            currentHeading -= 360;
        }
        while (currentHeading - targetHeading < -180) {
            currentHeading += 360;
        }

        debug(telemetry);
    }

    public void lockHeading() {
        if (!isHeadingLocked) {
            targetHeading = currentHeading;
            isHeadingLocked = true;
            controller.reset();
        }
    }

    public void unlockHeading() {
        isHeadingLocked = false;
    }

    public double calculateTurnPower() {
        if (!isHeadingLocked) {
            return 0;
        }
        controller.setPID(kP, kI, kD);
        double error = currentHeading - targetHeading;
        double pidOutput = -controller.calculate(currentHeading, targetHeading);
        double feedforward = kF * error;
        return pidOutput + feedforward;
    }

    public void reset() {
        double rawHeading = pinpoint.getHeading();
        if (Double.isFinite(rawHeading)) {
            headingOffset = rawHeading / (Math.PI * 2) * 360.0;
        } else {
            wasNan = true;
        }
        currentHeading = 0;
        targetHeading = 0;
        controller.reset();
        resetTimer.reset();
        pinpoint.resetPosAndIMU();
    }

    public static double getCappedCurrentHeading() {
        double cappedCurrent = currentHeading;
        while (cappedCurrent < 0) {
            cappedCurrent += 360;
        }
        while (cappedCurrent > 360) {
            cappedCurrent -= 360;
        }
        return cappedCurrent;
    }

    public static double getTargetHeading() {
        return targetHeading;
    }

    public void debug(Telemetry telemetry) {
        telemetry.addData("Target Heading", "%.2f degrees", targetHeading);
        telemetry.addData("Current Heading", "%.2f degrees", currentHeading);
        telemetry.addData("Current Raw", "%.2f degrees", currentRaw);
        telemetry.addData("Prev Raw", "%.2f degrees", prevRawHeading);
        telemetry.addData("Was NaN", wasNan);
        telemetry.addData("X", pinpoint.getPosX());
        telemetry.addData("Y", pinpoint.getPosY());
        telemetry.addData("Is Heading Locked", isHeadingLocked);
        telemetry.addData("Turn Power", "%.3f", calculateTurnPower());
        telemetry.addData("Feedforward", "%.3f", kF * (currentHeading - targetHeading));
    }
}