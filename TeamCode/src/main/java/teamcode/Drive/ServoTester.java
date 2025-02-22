package teamcode.Drive;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.arcrobotics.ftclib.controller.PIDController;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

@Config
@TeleOp
public class ServoTester extends OpMode {

    private DcMotorEx leftLift;
    private DcMotorEx rightLift;
    private DcMotorEx intake;
    private PIDController controller;
    private Servo test;
    private Servo dropper;
    private Servo turn;
    private Servo rotate;
    private Servo claw;
    private Servo arm_right;
    private Servo arm_left;

    public static double p = 0.002, i = 0, d = 0.000;
    public static double f = 0.0; // Feedforward
    private final double ticks_in_degree =  700 / 180.0;

    public static int target = 100;

    @Override
    public void init() {

//        intake = hardwareMap.get(DcMotorEx.class, "intake_motor");
//        intake.setDirection(DcMotorEx.Direction.REVERSE);

//
//        test = hardwareMap.get(Servo.class, "arm_right");
//        dropper = hardwareMap.get(Servo.class, "dropper");
//        turn = hardwareMap.get(Servo.class, "turn");
//        rotate = hardwareMap.get(Servo.class, "rotate");
        rotate = hardwareMap.get(Servo.class, "intake_rotate");
//        claw = hardwareMap.get(Servo.class, "claw");
//        arm_left = hardwareMap.get(Servo.class, "arm_left");
//        arm_right = hardwareMap.get(Servo.class, "arm_right");



        //for lifts
//        leftLift = hardwareMap.get(DcMotorEx.class, "leftLift");
//        rightLift = hardwareMap.get(DcMotorEx.class, "rightLift");
//        leftLift.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
//        leftLift.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
//        rightLift.setDirection(DcMotorEx.Direction.REVERSE);
//        controller = new PIDController(p, i, d);
//
//        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
//    }

    }
    @Override
    public void loop() {


        if(gamepad1.dpad_up) {
            rotate.setPosition(0.95);

        } else if(gamepad1.dpad_down) {
          rotate.setPosition(0);
        }



//
//        controller.setPID(p, i, d);
//
//        int intakePos = intake.getCurrentPosition();
//        double pid = controller.calculate(intakePos, target);
//        double ff = Math.cos(Math.toRadians(target / ticks_in_degree)) * f;
//        double power = pid + ff;
//
//        intake.setPower(power);


        //for lifts
//        int leftPos = leftLift.getCurrentPosition();
//        int rightPos = rightLift.getCurrentPosition();
//
//        double pid = controller.calculate(leftPos, target);
//        double ff = Math.cos(Math.toRadians(target / ticks_in_degree)) * f;
//        double power = pid + ff;
//
//        leftLift.setPower(power);
//        rightLift.setPower(leftLift.getPower());
//
//        telemetry.addData("target", target);
//        telemetry.addData("leftLIFT", leftPos);
//        telemetry.addData("rightLIFT", rightPos);
//        telemetry.addData("power", power);
//        telemetry.update();
    }
}