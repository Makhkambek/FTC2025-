package Controllers;

import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class ColorSensorController {
    private RevColorSensorV3 outtakeColorSensor;
    private RevColorSensorV3 intakeColorSensor;
    private boolean isSampleDetected = false;
    private boolean isIntakeSampleDetected = false;
    private static final double DETECTION_DISTANCE_CM = 0.5;

    public ColorSensorController(HardwareMap hardwareMap) {
        try {
            outtakeColorSensor = hardwareMap.get(RevColorSensorV3.class, "outtakeColorSensor");
        } catch (Exception e) {
            outtakeColorSensor = null;
        }
        try {
            intakeColorSensor = hardwareMap.get(RevColorSensorV3.class, "intakeColorSensor");
        } catch (Exception e) {
            intakeColorSensor = null;
        }
    }

    public void update(Telemetry telemetry) {
        if (outtakeColorSensor == null && intakeColorSensor == null) {
            telemetry.addData("Error", "Both sensors not initialized");
            return;
        }

        if (outtakeColorSensor != null) {
            double distanceCm = outtakeColorSensor.getDistance(DistanceUnit.CM);
            isSampleDetected = distanceCm <= DETECTION_DISTANCE_CM;
        }

        if (intakeColorSensor != null) {
            double distanceCm = intakeColorSensor.getDistance(DistanceUnit.CM);
            isIntakeSampleDetected = distanceCm <= DETECTION_DISTANCE_CM;
        }

        debug(telemetry);
    }

    public boolean isSampleDetected() {
        return isSampleDetected;
    }

    public boolean isSampleDetected(String sensorType) {
        if (sensorType.equalsIgnoreCase("intake")) {
            return isIntakeSampleDetected;
        }
        return isSampleDetected;
    }

    public double getDistance(DistanceUnit unit) {
        return getDistance(unit, "outtake");
    }

    public double getDistance(DistanceUnit unit, String sensorType) {
        RevColorSensorV3 sensor = sensorType.equalsIgnoreCase("intake") ? intakeColorSensor : outtakeColorSensor;
        if (sensor == null) {
            return -1;
        }
        return sensor.getDistance(unit);
    }

    public boolean isRed() {
        return isRed("outtake");
    }

    public boolean isRed(String sensorType) {
        RevColorSensorV3 sensor = sensorType.equalsIgnoreCase("intake") ? intakeColorSensor : outtakeColorSensor;
        if (sensor == null) {
            return false;
        }
        return sensor.red() > sensor.green() && sensor.red() > sensor.blue() && sensor.red() > 600;
    }

    public boolean isBlue() {
        return isBlue("outtake");
    }

    public boolean isBlue(String sensorType) {
        RevColorSensorV3 sensor = sensorType.equalsIgnoreCase("intake") ? intakeColorSensor : outtakeColorSensor;
        if (sensor == null) {
            return false;
        }
        return sensor.blue() > sensor.green() && sensor.blue() > sensor.red() && sensor.blue() > 1000;
    }

    public boolean isYellow() {
        return isYellow("outtake");
    }

    public boolean isYellow(String sensorType) {
        RevColorSensorV3 sensor = sensorType.equalsIgnoreCase("intake") ? intakeColorSensor : outtakeColorSensor;
        if (sensor == null) {
            return false;
        }
        return sensor.green() > sensor.red() && sensor.green() > sensor.blue() && sensor.red() > 1000;
    }

    public int getRed(String sensorType) {
        RevColorSensorV3 sensor = sensorType.equalsIgnoreCase("intake") ? intakeColorSensor : outtakeColorSensor;
        if (sensor == null) {
            return 0;
        }
        return sensor.red();
    }

    public int getGreen(String sensorType) {
        RevColorSensorV3 sensor = sensorType.equalsIgnoreCase("intake") ? intakeColorSensor : outtakeColorSensor;
        if (sensor == null) {
            return 0;
        }
        return sensor.green();
    }

    public int getBlue(String sensorType) {
        RevColorSensorV3 sensor = sensorType.equalsIgnoreCase("intake") ? intakeColorSensor : outtakeColorSensor;
        if (sensor == null) {
            return 0;
        }
        return sensor.blue();
    }

    private void debug(Telemetry telemetry) {
        telemetry.addData("Outtake Sensor Distance", outtakeColorSensor != null ? "%.2f cm" : "N/A",
                outtakeColorSensor != null ? outtakeColorSensor.getDistance(DistanceUnit.CM) : 0);
        telemetry.addData("Outtake Sample Detected", isSampleDetected ? "detected" : "not detected");
        telemetry.addData("Outtake Is Red", isRed("outtake") ? "yes" : "no");
        telemetry.addData("Outtake Is Blue", isBlue("outtake") ? "yes" : "no");
        telemetry.addData("Outtake Is Yellow", isYellow("outtake") ? "yes" : "no");
        telemetry.addData("Outtake RGB", "R:%d G:%d B:%d",
                getRed("outtake"), getGreen("outtake"), getBlue("outtake"));

        telemetry.addData("Intake Sensor Distance", intakeColorSensor != null ? "%.2f cm" : "N/A",
                intakeColorSensor != null ? intakeColorSensor.getDistance(DistanceUnit.CM) : 0);
        telemetry.addData("Intake Sample Detected", isIntakeSampleDetected ? "detected" : "not detected");
        telemetry.addData("Intake Is Red", isRed("intake") ? "yes" : "no");
        telemetry.addData("Intake Is Blue", isBlue("intake") ? "yes" : "no");
        telemetry.addData("Intake Is Yellow", isYellow("intake") ? "yes" : "no");
        telemetry.addData("Intake RGB", "R:%d G:%d B:%d",
                getRed("intake"), getGreen("intake"), getBlue("intake"));
    }
}