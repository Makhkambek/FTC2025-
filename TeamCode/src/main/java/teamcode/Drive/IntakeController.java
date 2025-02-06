package teamcode.Drive;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.FtcDashboard;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

@Config
public class IntakeController {

    private DcMotorEx intake;

    public static double p = 0.0;
    public static double i = 0.0;
    public static double d = 0.0;
    public static double f = 0.0; // Feedforward

    public static int LONG = 2000;
    public static int MEDIUM = 1000;
    public static int ZERO = 0;

    private FtcDashboard dashboard;

    public IntakeController(OpMode opMode) {
        intake = opMode.hardwareMap.get(DcMotorEx.class, "intake");


        intake.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);

        intake.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);

        intake.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);

        dashboard = FtcDashboard.getInstance();
        opMode.telemetry = dashboard.getTelemetry();
    }

    public void moveToPosition(int target) {
        int leftCurrentPosition = intake.getCurrentPosition();

        double leftError = target - leftCurrentPosition;

        double leftPID = (p * leftError) + (i * leftError) + (d * (leftError / 1));

        double leftFF = f;

        double leftPower = leftPID + leftFF;

        leftPower = Math.max(-1, Math.min(1, leftPower));

        intake.setPower(leftPower);
    }
}