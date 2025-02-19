package teamcode.Drive;

import com.arcrobotics.ftclib.controller.PIDController;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class LiftsController {
    private DcMotorEx leftLift;
    private DcMotorEx rightLift;
    private PIDController controller;

    public static final int HIGHEST_BASKET = 1450;
    public static final int HIGH_BAR = 900;
    public static final int GROUND = 0;

    private int target = GROUND;
    private boolean manualOverride = false;

    public LiftsController(HardwareMap hardwareMap) {
        leftLift = hardwareMap.get(DcMotorEx.class, "leftLift");
        rightLift = hardwareMap.get(DcMotorEx.class, "rightLift");
        rightLift.setDirection(DcMotorEx.Direction.REVERSE);
        leftLift.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        rightLift.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);;
//        leftLift.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        leftLift.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        rightLift.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        controller = new PIDController(0.002, 0.000, 0.000); //p = 0.006. d = 0.0004
    }

    public void setTarget(int newTarget) {
        target = newTarget;
        manualOverride = false;
    }

    public int getCurrentTarget() {
        return target;
    }

    public int getCurrentPosition() {
        return leftLift.getCurrentPosition();
    }

    public void manualControl(double power) {
        manualOverride = (power != 0); // Включаем ручное управление только при ненулевой мощности
        leftLift.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        rightLift.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        leftLift.setPower(power);
        rightLift.setPower(power);
    }

    public boolean isManualMode() {
        return manualOverride;
    }


    public void update() {
        int leftPos = leftLift.getCurrentPosition();

        double pid = controller.calculate(leftPos, target);
        double power = pid;

        leftLift.setPower(power);
        rightLift.setPower(leftLift.getPower());
    }
}