package teamcode.Drive;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

@Config
public class LiftsController {

    private DcMotorEx leftLift;
    private DcMotorEx rightLift;

    // PIDF параметры для настройки через FTC Dashboard
    public static double p = 0.0;
    public static double i = 0.0;
    public static double d = 0.0;
    public static double f = 0.0; // Feedforward

    // Предустановленные высоты лифта (в тиках энкодера)
    public static int HIGHEST_BASKET = 1500;
    public static int HIGH_BAR = 500;
//    public static int HIGH_BAR_PUT = 1500;
    public static int GROUND = 0;

    private FtcDashboard dashboard;

    // Конструктор
    public LiftsController(OpMode opMode) {
        leftLift = opMode.hardwareMap.get(DcMotorEx.class, "leftLift");
        rightLift = opMode.hardwareMap.get(DcMotorEx.class, "rightLift");

        leftLift.setDirection(DcMotorEx.Direction.REVERSE);

        // Сброс энкодеров и установка режимов
        leftLift.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        rightLift.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);

        leftLift.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        rightLift.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);

        leftLift.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        rightLift.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);

        dashboard = FtcDashboard.getInstance();
        opMode.telemetry = dashboard.getTelemetry();
    }

    // Метод для управления лифтом
    public void moveToPosition(int target) {
        int leftCurrentPosition = leftLift.getCurrentPosition();
        int rightCurrentPosition = rightLift.getCurrentPosition();

        double leftError = target - leftCurrentPosition;
        double rightError = target - rightCurrentPosition;

        double leftPID = (p * leftError) + (i * leftError) + (d * (leftError / 1));
        double rightPID = (p * rightError) + (i * rightError) + (d * (rightError / 1));

        double leftFF = f;
        double rightFF = f;

        double leftPower = leftPID + leftFF;
        double rightPower = rightPID + rightFF;

        // Ограничение мощности
        leftPower = Math.max(-1, Math.min(1, leftPower));
        rightPower = Math.max(-1, Math.min(1, rightPower));

        leftLift.setPower(leftPower);
        rightLift.setPower(rightPower);
    }

    public int getHeight() {
        return (leftLift.getCurrentPosition() + rightLift.getCurrentPosition()) / 2;
    }

}