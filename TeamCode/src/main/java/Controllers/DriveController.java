package Controllers;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Gamepad;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class DriveController {
    private DcMotor leftFront, rightFront, leftRear, rightRear;
    private IMU imu;
    private HeadingController headingController;
    private boolean wasTriggerPressed = false;

    public DriveController(HardwareMap hardwareMap, Telemetry telemetry) {
        leftFront = hardwareMap.get(DcMotor.class, "leftFront");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        leftRear = hardwareMap.get(DcMotor.class, "leftRear");
        rightRear = hardwareMap.get(DcMotor.class, "rightRear");

        leftFront.setDirection(DcMotor.Direction.REVERSE);
        leftRear.setDirection(DcMotor.Direction.REVERSE);

        imu = hardwareMap.get(IMU.class, "imu");
        imu.resetYaw();

        headingController = new HeadingController(hardwareMap);
    }

    public void drive(Gamepad gamepad1, Gamepad gamepad2, Telemetry telemetry) {
        headingController.update(telemetry);

        if (gamepad2.right_trigger >= 0.4 && !wasTriggerPressed) {
            headingController.lockHeading();
            wasTriggerPressed = true;
        } else if (gamepad2.right_trigger <= 0.3 && wasTriggerPressed) {
            headingController.unlockHeading();
            wasTriggerPressed = false;
        }

        double slowModeFactor = gamepad1.right_trigger > 0.1 ? 0.3 : 1.0;
        double y = -gamepad1.left_stick_y * slowModeFactor; // Вперед/назад
        double x = gamepad1.left_stick_x * 1.1 * slowModeFactor; // Влево/вправо
        double rx; // Поворот

        if (gamepad2.right_trigger > 0.1) {
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
    }
}