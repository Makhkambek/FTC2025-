package teamcode.Drive;

import com.arcrobotics.ftclib.controller.PIDController;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class IntakeController {

    private DcMotorEx intake;
    private PIDController controller;

    public static final int LONG = 550;
    public static final int MEDIUM = 250;
    public static final int ZERO = 0;

    public static double p = 0.004;
    public static double i = 0.0;
    public static double d = 0.0002;
    public static double f = 0.0;

    private final double ticks_in_degree = 700 / 180.0;
    private int target = ZERO;

    public IntakeController(HardwareMap hardwareMap) {
        intake = hardwareMap.get(DcMotorEx.class, "intake_motor");
        intake.setDirection(DcMotorEx.Direction.REVERSE);
//        intake.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
//        intake.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
//        intake.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);

        controller = new PIDController(p, i, d);
    }

    public void setTarget(int newTarget) {
        target = newTarget;
    }

    public void update() {
        controller.setPID(p, i, d);

        int intakePos = intake.getCurrentPosition();
        double pid = controller.calculate(intakePos, target);
        double ff = Math.cos(Math.toRadians(target / ticks_in_degree)) * f;
        double power = pid + ff;

//        if (target == ZERO && Math.abs(intakePos) <= 10) {
//            power = -0.1;
//        }
//
//        if (target == ZERO && intakePos == 0) {
//            power = 0;
//        }

        intake.setPower(power);
    }
}