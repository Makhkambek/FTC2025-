package pedroPathing.constants;

import com.pedropathing.localization.*;
import com.pedropathing.localization.constants.*;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;

public class LConstants {
    static {
        TwoWheelConstants.forwardTicksToInches = .001989436789;
//        TwoWheelConstants.forwardTicksToInches = .002513985;
        TwoWheelConstants.strafeTicksToInches = .001989436789;
//        TwoWheelConstants.strafeTicksToInches = .005858009;
        TwoWheelConstants.forwardY = -2.5;
        TwoWheelConstants.strafeX = -4.25;
        TwoWheelConstants.forwardEncoder_HardwareMapName = "leftRear";
        TwoWheelConstants.strafeEncoder_HardwareMapName = "encoder";
        TwoWheelConstants.forwardEncoderDirection = Encoder.REVERSE;
        TwoWheelConstants.strafeEncoderDirection = Encoder.FORWARD;
        TwoWheelConstants.IMU_HardwareMapName = "imu";
        TwoWheelConstants.IMU_Orientation = new RevHubOrientationOnRobot(RevHubOrientationOnRobot.LogoFacingDirection.LEFT, RevHubOrientationOnRobot.UsbFacingDirection.UP);
    }
}




