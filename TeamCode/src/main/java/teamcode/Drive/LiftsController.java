package teamcode.Drive;

import com.arcrobotics.ftclib.controller.PIDController;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class LiftsController {

    private DcMotorEx leftLift;
    private DcMotorEx rightLift;
    private PIDController controller;

    public static final int HIGHEST_BASKET = 700;
    public static final int HIGH_BAR = 200;
    public static final int GROUND = 0;

    public static double p = 0.006;
    public static double i = 0.0;
    public static double d = 0.0003;
    public static double f = 0.0;

    private final double ticks_in_degree = 700 / 180.0;
    private int target = GROUND;

    public LiftsController(HardwareMap hardwareMap) {
        leftLift = hardwareMap.get(DcMotorEx.class, "leftLift");
        rightLift = hardwareMap.get(DcMotorEx.class, "rightLift");

        leftLift.setDirection(DcMotorEx.Direction.REVERSE);
        controller = new PIDController(p, i, d);
    }

    public void setTarget(int newTarget) {
        target = newTarget;
    }

    public void update() {

        controller.setPID(p, i, d);
        int leftPos = leftLift.getCurrentPosition();
        int rightPos = rightLift.getCurrentPosition();

        double pid = controller.calculate(leftPos, target);
        double ff = Math.cos(Math.toRadians(target / ticks_in_degree)) * f;
        double power = pid + ff;

        leftLift.setPower(power);
        rightLift.setPower(leftLift.getPower());

//        if (target == GROUND && Math.abs(leftPos) <= 10) {
//            power = -0.1;
//        }
//
//        if (target == GROUND && leftPos == 0) {
//            power = 0;
//        }

    }
}