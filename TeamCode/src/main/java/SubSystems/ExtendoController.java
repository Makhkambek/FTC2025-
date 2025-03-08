package SubSystems;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

public class ExtendoController {
    private DcMotorEx intakeMotor;

    public static final int LONG = 550;
    public static final int ZERO = 0;
    public static final int MINUS_ZERO = -20;

    private int reference = ZERO;
    private boolean forced = false;

    private double integralSum = 0;
    private double lastError = 0;

    private ElapsedTime timer = new ElapsedTime();

    public static double kP = 0.002;
    public static double kI = 0.0;
    public static double kD = 0.000;
    public static double kF = 0.0;

    public ExtendoController(HardwareMap hardwareMap) {
        intakeMotor = hardwareMap.get(DcMotorEx.class, "intake_motor");

        resetEncoders();
    }

    private void resetEncoders() {
        intakeMotor.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        intakeMotor.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        reference = ZERO;
    }

    public void setReference(int newReference) {
        newReference = Math.min(newReference, LONG);
        newReference = Math.max(newReference, MINUS_ZERO);
        this.reference = newReference;
    }

    public void setTarget(int newTarget) {
        setReference(newTarget);
    }

    public int getCurrentTarget() {
        return reference;
    }

    public int getCurrentPosition() {
        return intakeMotor.getCurrentPosition();
    }

    public void forceMove(double power) {
        reference = ZERO;
        intakeMotor.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        intakeMotor.setPower(power);
        forced = true;
    }

    public void stopForceMove() {
        intakeMotor.setPower(0);
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

        double position = intakeMotor.getCurrentPosition();
        double error = reference - position;

        double derivative = (error - lastError) / timer.seconds();
        integralSum += error * timer.seconds();

        double output = (kP * error) + (kI * integralSum) + (kD * derivative) + kF;

        intakeMotor.setPower(output);

        lastError = error;
        timer.reset();
    }
}