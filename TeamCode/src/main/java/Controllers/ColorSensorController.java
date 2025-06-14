package Controllers;

import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class ColorSensorController {
    private RevColorSensorV3 outtakeColorSensor;
    private boolean isSampleDetected = false;
    private static final double DETECTION_DISTANCE_CM = 0.5;

    public ColorSensorController(HardwareMap hardwareMap) {
        try {
            outtakeColorSensor = hardwareMap.get(RevColorSensorV3.class, "outtakeColorSensor");
        } catch (Exception e) {
            outtakeColorSensor = null;
        }
    }

    public void update(Telemetry telemetry) {
        if (outtakeColorSensor == null) {
            telemetry.addData("Error", "Sensor not initialized");
            return;
        }

        double distanceCm = outtakeColorSensor.getDistance(DistanceUnit.CM);

        isSampleDetected = distanceCm <= DETECTION_DISTANCE_CM;

        debug(telemetry);
    }

    public boolean isSampleDetected() {
        return isSampleDetected;
    }

    private void debug(Telemetry telemetry) {
        telemetry.addData("Outtake Sensor Distance", outtakeColorSensor != null ? "%.2f cm" : "N/A",
                outtakeColorSensor != null ? outtakeColorSensor.getDistance(DistanceUnit.CM) : 0);
        telemetry.addData("Sample Detected", isSampleDetected ? "detected" : "not detected");
    }
}