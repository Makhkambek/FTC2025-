package teamcode.Drive;

import com.arcrobotics.ftclib.controller.PIDController;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class IntakeController {

    private DcMotorEx intake;
    private PIDController controller;

    public static final int LONG = 550;
    public static final int ZERO = 0;
    public static final int MINUS_ZERO = -20;

    public static double p = 0.005;
    public static double i = 0.0;
    public static double d = 0.0002;
    public static double f = 0.0;
    public static double power = 0.0;

    private final double ticks_in_degree = 700 / 180.0;
    private int target = ZERO;
    private boolean forced = false;
    public IntakeController(HardwareMap hardwareMap) {
        intake = hardwareMap.get(DcMotorEx.class, "intake_motor");
        intake.setDirection(DcMotorEx.Direction.REVERSE);
        resetEncoders();

        controller = new PIDController(p, i, d);
    }

    private void resetEncoders() {
        intake.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        intake.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        target = 0;
    }

    public void setTarget(int newTarget) {
        target = newTarget;
    }

    public int getCurrentPosition() {
        return intake.getCurrentPosition();
    }

    public int getCurrentTarget() {
        return target;
    }

    public void forceMove(double power) {
        target = 0; // Сбрасываем целевую позицию PID-контроля
        intake.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        intake.setPower(power);
        forced = true;
    }

    public void stopForceMove() {
        intake.setPower(0);
        resetEncoders();
        forced = false;
    }

    public boolean isForcedMode() {
        return forced;
    }

    public void update() {
        if (forced) {
            return;
        }

        controller.setPID(p, i, d);

        int intakePos = intake.getCurrentPosition();
        double pid = controller.calculate(intakePos, target);
        double ff = Math.cos(Math.toRadians(target / ticks_in_degree)) * f;
        power = pid + ff;

        intake.setPower(power);
    }
}