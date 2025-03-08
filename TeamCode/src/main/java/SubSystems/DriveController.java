package SubSystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;
import com.pedropathing.follower.Follower;
import com.pedropathing.localization.Pose;
import com.pedropathing.util.Constants;
import pedroPathing.constants.FConstants;
import pedroPathing.constants.LConstants;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Gamepad;

public class DriveController {
    private DcMotor leftFront, rightFront, leftRear, rightRear;
    private Follower follower;
    private IMU imu;
    private boolean useFollower = false;
    private double heading = 0;
    private double d = 0.05;
    private final Pose startPose = new Pose(0, 0, 0);

    public DriveController(HardwareMap hardwareMap) {
        leftFront = hardwareMap.get(DcMotor.class, "leftFront");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        leftRear = hardwareMap.get(DcMotor.class, "leftRear");
        rightRear = hardwareMap.get(DcMotor.class, "rightRear");

        leftFront.setDirection(DcMotor.Direction.REVERSE);
        leftRear.setDirection(DcMotor.Direction.REVERSE);

        Constants.setConstants(FConstants.class, LConstants.class);
        follower = new Follower(hardwareMap);
        follower.setStartingPose(startPose);
        imu = hardwareMap.get(IMU.class, "imu");
        imu.resetYaw();
    }

    public void startFollower() {
        follower.startTeleopDrive();
    }

    public void drive(Gamepad gamepad1) {
        double slowModeFactor = gamepad1.right_trigger > 0 ? 0.3 : 1.0;
        double y = -gamepad1.left_stick_y * slowModeFactor;
        double x = gamepad1.left_stick_x * 1.1 * slowModeFactor;
        double rx;

        if (gamepad1.options) {
            useFollower = true;
        } else {
            useFollower = false;
        }

        if (useFollower) {
            double realControlHeading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS);
            rx = (-realControlHeading + heading) * d;
            follower.setTeleOpMovementVectors(y, x, rx, true);
            follower.update();
        } else {
            rx = gamepad1.right_stick_x * slowModeFactor;

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
}