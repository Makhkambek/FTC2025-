package Controllers;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Gamepad;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class DriveController {
    private DcMotor leftFront, rightFront, leftRear, rightRear;
    private HeadingController headingController;
    private boolean wasTriggerPressed = false;
    private double slowModeFactor;

    public DriveController(HardwareMap hardwareMap, Telemetry telemetry) {
        leftFront = hardwareMap.get(DcMotor.class, "leftRear");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        leftRear = hardwareMap.get(DcMotor.class, "leftFront");
        rightRear = hardwareMap.get(DcMotor.class, "rightRear");

        leftFront.setDirection(DcMotor.Direction.REVERSE);
        leftRear.setDirection(DcMotor.Direction.REVERSE);

        headingController = new HeadingController(hardwareMap);
    }

    public void drive(Gamepad gamepad1, Gamepad gamepad2, Telemetry telemetry) {
        headingController.update(telemetry);

        if (gamepad1.left_bumper && !wasTriggerPressed) {
            headingController.lockHeading();
            wasTriggerPressed = true;
        } else if (!gamepad1.left_bumper && wasTriggerPressed) {
            headingController.unlockHeading();
            wasTriggerPressed = false;
        }

        if (gamepad1.right_bumper) {
            slowModeFactor = 0.3;
        }else{
            slowModeFactor = 1.0;
        }
        double y = -gamepad1.left_stick_y * slowModeFactor;
        double x = gamepad1.left_stick_x * slowModeFactor;
        double rx;

        if (gamepad1.left_bumper) {
            rx = headingController.calculateTurnPower();
        } else {
            rx = gamepad1.right_stick_x * slowModeFactor;
        }

        double frontLeftPower = y + x + rx;
        double backLeftPower = y - x + rx;
        double frontRightPower = y - x - rx;
        double backRightPower = y + x - rx;

        double maxPower = Math.max(Math.abs(frontLeftPower), Math.max(Math.abs(backLeftPower),
                Math.max(Math.abs(frontRightPower), Math.abs(backRightPower))));
        if (maxPower > 1.0) {
            frontLeftPower /= maxPower;
            backLeftPower /= maxPower;
            frontRightPower /= maxPower;
            backRightPower /= maxPower;
        }

        leftFront.setPower(frontLeftPower);
        leftRear.setPower(backLeftPower);
        rightFront.setPower(frontRightPower);
        rightRear.setPower(backRightPower);

        telemetry.addData("Front Left Power", frontLeftPower);
        telemetry.addData("Back Left Power", backLeftPower);
        telemetry.addData("Front Right Power", frontRightPower);
        telemetry.addData("Back Right Power", backRightPower);
        telemetry.addData("X Input", x);
    }
}