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

    // PID коэффициенты и Feedforward
    public static double incrementCoefficient = 180;
    public static double kP = 0.024;
    public static double kI = 0.0;
    public static double kD = 0.0003;
    public static double kF = 0.005;    //0.01

    private final PIDController controller;
    private double headingOffset;
    private boolean wasNan = false;
    private final ElapsedTime resetTimer = new ElapsedTime();

    // Конструктор
    public HeadingController(HardwareMap hardwareMap) {
        pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, "pinpoint");
        pinpoint.setOffsets(-107.95, -63.5);
        pinpoint.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        pinpoint.setEncoderDirections(GoBildaPinpointDriver.EncoderDirection.REVERSED, GoBildaPinpointDriver.EncoderDirection.FORWARD);
        pinpoint.resetPosAndIMU();

        controller = new PIDController(kP, kI, kD);
        resetTimer.reset();
    }

    // Обновление состояния
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

        // Нормализация currentHeading
        while (currentHeading - targetHeading > 180) {
            currentHeading -= 360;
        }
        while (currentHeading - targetHeading < -180) {
            currentHeading += 360;
        }

        debug(telemetry);
    }

    // Блокировка текущего heading
    public void lockHeading() {
        if (!isHeadingLocked) {
            targetHeading = currentHeading;
            isHeadingLocked = true;
            controller.reset();
        }
    }

    // Разблокировка heading
    public void unlockHeading() {
        isHeadingLocked = false;
    }

    // Расчет мощности поворота с учетом Feedforward
    public double calculateTurnPower() {
        if (!isHeadingLocked) {
            return 0;
        }
        controller.setPID(kP, kI, kD);
        double error = currentHeading - targetHeading;
        double pidOutput = -controller.calculate(currentHeading, targetHeading);
        // Добавление Feedforward-компоненты
        double feedforward = kF * error;
        return pidOutput + feedforward;
    }

    // Сброс состояния
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

    // Получение нормализованного текущего heading
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

    // Получение целевого heading
    public static double getTargetHeading() {
        return targetHeading;
    }

    // Телеметрия для отладки
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