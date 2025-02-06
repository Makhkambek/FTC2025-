package teamcode.Drive;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;
@TeleOp
public class ServoTester extends OpMode {
    private Servo test;



    @Override
    public void init() {
        test = hardwareMap.get(Servo.class, "test");
    }

    @Override
    public void loop() {

        if(gamepad1.a) {
            test.setPosition(0.5);
        } else {
            test.setPosition(0);
        }

    }
}
