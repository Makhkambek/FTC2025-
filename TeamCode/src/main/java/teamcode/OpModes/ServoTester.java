package teamcode.OpModes;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.controller.PIDController;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

@Config
@TeleOp
//@Disabled
public class ServoTester extends OpMode {

    private DcMotorEx leftLift;
    private DcMotorEx rightLeft;
    private DcMotorEx leftFront;
    private DcMotorEx rightFront;
    private DcMotorEx rightLift;
    private DcMotorEx middleLift;
    private DcMotorEx intake;
    private PIDController controller;
    private Servo servo;
    private Servo servo_1;
    private Servo dropper;
    private Servo turn;
    private Servo rotate;
    private Servo rotate_2;
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
//        leftRear = hardwareMap.get(DcMotorEx.class, "leftRear");
//        rightLeft = hardwareMap.get(DcMotorEx.class, "rightLeft");
//        leftFront = hardwareMap.get(DcMotorEx.class, "leftFront");
//        rightFront = hardwareMap.get(DcMotorEx.class, "rightFront");
//        intake = hardwareMap.get(DcMotorEx.class, "intake");

        servo = hardwareMap.get(Servo.class, "servo");
        servo_1 = hardwareMap.get(Servo.class, "servo_1");
        servo.setDirection(Servo.Direction.REVERSE);
//        servo_1.setDirection(Servo.Direction.REVERSE);


        //for lifts
//        leftLift = hardwareMap.get(DcMotorEx.class, "leftLift");
//        rightLift = hardwareMap.get(DcMotorEx.class, "rightLift");
//        middleLift = hardwareMap.get(DcMotorEx.class, "middleLift");
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

         servo.setPosition(0.5);
         servo_1.setPosition(0.55);
        }else {

         servo.setPosition(0.26);
         servo_1.setPosition(0.285);
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