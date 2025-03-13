package Vision;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.sun.tools.javac.util.ArrayUtils;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.opencv.core.RotatedRect;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvInternalCamera;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IntSummaryStatistics;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@TeleOp
public class CameraTest extends LinearOpMode {
    OpenCvCamera cam;
    double centerCoord;
    double Angle, x, y;
    int index;
    String sampleCoordinatesStatus;
    RotatedRect theRect;
    boolean tf;
    public double extendingError, handError, clawError;
    public boolean sample_status;


    @Override
    public void runOpMode() throws InterruptedException {
        int cameraMonitorViewId = hardwareMap.appContext
                .getResources().getIdentifier("cameraMonitorViewId",
                        "id", hardwareMap.appContext.getPackageName());
        cam = OpenCvCameraFactory.getInstance()
                .createInternalCamera(OpenCvInternalCamera.CameraDirection.BACK, cameraMonitorViewId);
        cam = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);

        SampleDetectionPipelinePNP pip = new SampleDetectionPipelinePNP();
        cam.setPipeline(pip);

        cam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                cam.startStreaming(640, 480, OpenCvCameraRotation.UPRIGHT);
            }

            @Override
            public void onError(int errorCode) {

            }
        });



        waitForStart();

        while (!isStopRequested()){
            List<Double> Ang = new ArrayList<Double>(pip.RotateStone);

            telemetry.addData("x", x);
            telemetry.addData("y", y);
            telemetry.addData("index", index);
            telemetry.addData("CenterX", centerCoord);
            telemetry.addData("Angle", Angle);
            telemetry.addData("ang", pip.Center);



            if (gamepad1.a) {
                tf = true;
                x = 0;
                y = 0;
                Angle = 0;
            }
            if (gamepad1.b) {
                tf = false;
                x = 0;
                y = 0;
                Angle = 0;
            }
            if (tf == true) {
                pip.YELLOW_MASK_THRESHOLD = 57;
                pip.BLUE_MASK_THRESHOLD = 150;
                pip.RED_MASK_THRESHOLD = 198;

            } else {

                pip.YELLOW_MASK_THRESHOLD = 100;
                pip.BLUE_MASK_THRESHOLD = 57;
                pip.RED_MASK_THRESHOLD = 198;

            }

            if (pip.Center.size() != 0) {


                centerCoord = Collections.min(pip.Center);

                if (Math.round(pip.rotatedRectFitToContour.center.x/96*2.54) == centerCoord){
                    theRect = pip.rotatedRectFitToContour;
                    x = theRect.center.x / 96 * 2.54;
                    y = theRect.center.y / 96 * 2.54;
                    Angle = theRect.angle;
                }
                extendingError = Math.round(y);
                handError = Math.round(x);
                clawError = Math.round(90-Angle);
                if(extendingError == 0 && handError == 0 && clawError == 0){
                    sample_status = true;
                }
                else {
                    sample_status = false;
                }

            }
            telemetry.update();


        }
        cam.stopStreaming();
    }
}