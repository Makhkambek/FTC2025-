package teamcode.OpModes;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;

@Config
@TeleOp
@Disabled
public class LiftTester extends OpMode {

    private DcMotorEx leftLift;
    private DcMotorEx rightLift;
    private DcMotorEx middleLift;

    public static final int HIGHEST_BASKET = 1750;
    public static final int HIGH_BAR = 700;
    public static final int GROUND = 0;

    private int reference = GROUND;
    private boolean forced = false;

    private double integralSum = 0;
    private double lastError = 0;

    private ElapsedTime timer = new ElapsedTime();

    // PID constants
    public static double kP = 0.005;
    public static double kI = 0.0001;  //0.00
    public static double kD = 0.000;
    public static double kF = 0.0;  //0.0

    @Override
    public void init() {
        leftLift = hardwareMap.get(DcMotorEx.class, "leftLift");
        rightLift = hardwareMap.get(DcMotorEx.class, "rightLift");
        middleLift = hardwareMap.get(DcMotorEx.class, "middleLift");
        rightLift.setDirection(DcMotorEx.Direction.REVERSE);
        middleLift.setDirection(DcMotorEx.Direction.REVERSE);
        resetEncoders();

        telemetry.addData("Status", "Initialized");
        telemetry.update();
    }

    private void resetEncoders() {
        leftLift.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        rightLift.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        middleLift.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        leftLift.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        rightLift.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        middleLift.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        reference = 0;
    }

    public void setReference(int newReference) {
        newReference = Math.min(newReference, HIGHEST_BASKET);
        newReference = Math.max(newReference, GROUND);
        this.reference = newReference;
    }

    public int getCurrentTarget() {
        return reference;
    }

    public void setTarget(int newTarget) {
        setReference(newTarget);
    }

    public void forceMove(double power) {
        reference = 0;
        leftLift.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        rightLift.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        middleLift.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        leftLift.setPower(power);
        rightLift.setPower(power);
        middleLift.setPower(power);
        forced = true;
    }

    public void stopForceMove() {
        leftLift.setPower(0);
        rightLift.setPower(0);
        middleLift.setPower(0);
        resetEncoders();
        forced = false;
    }

    public void updateLifts() {
        if (forced) {
            return;
        }

        double position = rightLift.getCurrentPosition();
        double error = reference - position;

        double derivative = (error - lastError) / timer.seconds();
        integralSum += error * timer.seconds();

        double output = (kP * error) + (kI * integralSum) + (kD * derivative) + kF;

        leftLift.setPower(output);
        rightLift.setPower(output);
        middleLift.setPower(output);

        lastError = error;
        timer.reset();
    }

    @Override
    public void loop() {
        if (Math.abs(gamepad2.left_stick_y) > 0) {
            int newTarget = getCurrentTarget() + (int) (-gamepad2.left_stick_y * 30);
            setTarget(newTarget);
        }

        updateLifts();

        telemetry.addData("Target", reference);
        telemetry.addData("Current Position", rightLift.getCurrentPosition());
        telemetry.addData("Power", rightLift.getPower());
        telemetry.update();
    }
}