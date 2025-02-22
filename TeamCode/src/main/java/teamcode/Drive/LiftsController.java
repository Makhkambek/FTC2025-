package teamcode.Drive;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

public class LiftsController {
    private DcMotorEx leftLift;
    private DcMotorEx rightLift;

    public static final int HIGHEST_BASKET = 1450;
    public static final int HIGH_BAR = 950;
    public static final int GROUND = 0;

    private int reference = GROUND;
    private boolean forced = false;

    private double integralSum = 0;
    private double lastError = 0;

    private ElapsedTime timer = new ElapsedTime();

    public static double kP = 0.002;
    public static double kI = 0.0;
    public static double kD = 0.000;
    public static double kF = 0.0;

    public LiftsController(HardwareMap hardwareMap) {
        leftLift = hardwareMap.get(DcMotorEx.class, "leftLift");
        rightLift = hardwareMap.get(DcMotorEx.class, "rightLift");
        rightLift.setDirection(DcMotorEx.Direction.REVERSE);

        resetEncoders();
    }

    private void resetEncoders() {
        leftLift.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        rightLift.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        leftLift.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        rightLift.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        reference = 0;
    }

    public void setReference(int newReference) {
        newReference = Math.min(newReference, HIGHEST_BASKET);
        newReference = Math.max(newReference, GROUND);
        this.reference = newReference;
    }

    public void setTarget(int newTarget) {
        setReference(newTarget);
    }

    public int getCurrentTarget() {
        return reference;
    }

    public int getCurrentPosition() {
        return leftLift.getCurrentPosition();
    }

    public void forceMove(double power) {
        reference = 0;
        leftLift.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        rightLift.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);

        leftLift.setPower(power);
        rightLift.setPower(power);

        forced = true;
    }

    public void stopForceMove() {
        leftLift.setPower(0);
        rightLift.setPower(0);

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

        double position = leftLift.getCurrentPosition();
        double error = reference - position;

        double derivative = (error - lastError) / timer.seconds();
        integralSum += error * timer.seconds();

        double output = (kP * error) + (kI * integralSum) + (kD * derivative) + kF;

        leftLift.setPower(output);
        rightLift.setPower(output);

        lastError = error;
        timer.reset();
    }
}