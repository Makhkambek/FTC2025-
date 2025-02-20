package teamcode.Drive;
//package pedroPathing.constants;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import com.pedropathing.follower.Follower;
import com.pedropathing.localization.Pose;
import com.pedropathing.pathgen.BezierCurve;
import com.pedropathing.pathgen.BezierLine;
import com.pedropathing.pathgen.Path;
import com.pedropathing.pathgen.PathChain;
import com.pedropathing.pathgen.Point;
import com.pedropathing.util.Constants;
import com.pedropathing.util.Timer;
import pedroPathing.constants.FConstants;
import pedroPathing.constants.LConstants;


@Autonomous(name = "BlueBasket", group = "Autonomous")
public class BlueBasket extends OpMode {

    private Follower follower;
    private Timer pathTimer, opmodeTimer;
    private boolean poseSet = false;
    private int pathState = 0; // FSM начальное состояние

    private LiftsController lifts;
    private Outtake outtake;
    private boolean clips = false;

    private PathChain path1, path2, path3, path4, path5, path6, path7, path8, path9, path10;

    private final Pose startPose = new Pose(9.6, 88.9, 360);
    private Intake intake;

    private IntakeController intakeMotor;

    public void buildPaths() {
        path1 = follower.pathBuilder()
                .addPath(
                        // 1 specimen put
                        new BezierCurve(
                                new Point(9.600, 88.904, Point.CARTESIAN),
                                new Point(34.643, 113.322, Point.CARTESIAN),
                                new Point(21.078, 122.087, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(360))
                .setPathEndVelocityConstraint(0.5)
                .build();

        path2 = follower.pathBuilder()
                .addPath(
                        // second specimen
                        new BezierLine(
                                new Point(21.078, 122.087, Point.CARTESIAN),
                                new Point(25.339, 121.670, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(360))
                .build();

        path3 = follower.pathBuilder()
                .addPath(
                        // second pixel put
                        new BezierLine(
                                new Point(25.339, 121.670, Point.CARTESIAN),
                                new Point(21.078, 123.757, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(360))
                .setPathEndVelocityConstraint(0.5)
                .build();
//
        path4 = follower.pathBuilder()
                .addPath(
                        // go back after second specimen
                        new BezierCurve(
                                new Point(21.078, 123.757, Point.CARTESIAN),
                                new Point(23.374, 126.678, Point.CARTESIAN),
                                new Point(26.504, 126.470, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(360))
//                .setPathEndVelocityConstraint(0.6)
                .build();
//
        path5 = follower.pathBuilder()
                .addPath(
                        // go put 3d specimen
                        new BezierCurve(
                                new Point(26.504, 126.470, Point.CARTESIAN),
                                new Point(36.522, 110.191, Point.CARTESIAN),
                                new Point(36.939, 119.583, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(360))
                .setPathEndVelocityConstraint(0.5)
                .build();
//
        path6 = follower.pathBuilder()
                .addPath(
                        // go back after 3d specimen
                        new BezierCurve(
                                new Point(34.017, 71.791, Point.CARTESIAN),
                                new Point(19.200, 69.913, Point.CARTESIAN),
                                new Point(38.609, 25.043, Point.CARTESIAN),
                                new Point(20.035, 28.383, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(360))
//                .setPathEndVelocityConstraint(0.6)
                .build();
//

    }

    /** FSM (Логика автономки) */
    public void autonomousPathUpdate() {
        switch (pathState) {
            case 0: // едет и поднимает лифт
                follower.followPath(path1, true);
                outtake.setClipsPutState();
                setPathState(1);
                break;

            case 1:
                if (!follower.isBusy()) {
                    lifts.setTarget(LiftsController.HIGHEST_BASKET);
                    outtake.setScoreState();
                    setPathState(2);
                }
                break;

//
            case 2:
                if (lifts.getCurrentPosition() >= 1300) {
                    outtake.setDrop();
                    setPathState(3);
                }
                break;

            case 3:
                if (outtake.isDropComplete) {
                    follower.followPath(path2, true);
                    lifts.setTarget(LiftsController.GROUND);
                    setPathState(4);
                }
                break;

            case 4:
                if (!follower.isBusy()) {
                    intakeMotor.setTarget(IntakeController.LONG);
                    intake.setOpenState();
                    setPathState(5);
                }
                break;

            case 5:
                if (intakeMotor.getCurrentPosition() >= 500) {
                    intake.setClosedState();
                    setPathState(6);
                }
                break;

            case 6:
                if (intake.isClosedComplete) {
                    intakeMotor.setTarget(IntakeController.ZERO);
                    intake.setTransfer();
                    follower.followPath(path3, true);
                    setPathState(7);
                }
                break;

            case 7:
                if (!follower.isBusy()) {
                    lifts.setTarget(LiftsController.HIGHEST_BASKET);
                    outtake.setScoreState();
                    setPathState(8);
                }
                break;

            case 8: // Отпускает клипс и едет толкать сэмплы
                if (lifts.getCurrentPosition() >= 1300) {
                    outtake.setDrop();
                    setPathState(9);
                }
                break;

            case 9:
                if (outtake.isDropComplete) {
                    follower.followPath(path4, true);
                outtake.setGrabState();
                lifts.setTarget(LiftsController.GROUND);
                setPathState(10);
                }
                break;

            case 10:
                if (!follower.isBusy()) {
                    intakeMotor.setTarget(IntakeController.LONG);
                    intake.setOpenState();
                    setPathState(11);
                }
                break;

            case 11:
                if (intakeMotor.getCurrentPosition() >= 500) {
                    intake.setClosedState();
                    setPathState(12);
                }
                break;

            case 12:
                if (intake.isClosedComplete) {
                    intakeMotor.setTarget(IntakeController.ZERO);
                    intake.setTransfer();
                    follower.followPath(path5, true);
                    setPathState(13);
                }
                break;






            case 13:
                if (!follower.isBusy()) {
                    outtake.setClipsPutState();
                    setPathState(14);
                }
                break;
            case 14:
                if (!follower.isBusy()) {
                    follower.followPath(path7, true);
                    setPathState(15);
                }
                break;

            case 15:
                if (!follower.isBusy()) {
                    lifts.setTarget(LiftsController.HIGH_BAR);
                    setPathState(16);
                }
                break;
            case 16:
                if (lifts.getCurrentPosition() >= 780) {
                    lifts.setTarget(LiftsController.GROUND);
                    outtake.setClipsTakeState();
                    setPathState(17);
                }
                break;
            case 17:
                if(!follower.isBusy()) {
                    follower.followPath(path8, true);
                    setPathState(18);
                }
                break;
//            case 18:
//                if(!follower.isBusy()) {
//                    outtake.setClipsPutState();
//                    setPathState(19);
//                }
//            case 19:
//                if (!follower.isBusy()) {
//                    follower.followPath(path9, true);
//                    setPathState(20);
//                }
//                break;
//            case 20:
//                if(!follower.isBusy()) {
//                    lifts.setTarget(LiftsController.HIGH_BAR);
//                    setPathState(21);
//                }
//                break;





        }
    }

    public void setPathState(int pState) {
        pathState = pState;
        pathTimer.resetTimer();
    }


    @Override
    public void init() {
        pathTimer = new Timer();
        opmodeTimer = new Timer();
        opmodeTimer.resetTimer();
        Constants.setConstants(FConstants.class, LConstants.class);
//        follower.getPose().setHeading(0);

        follower = new Follower(hardwareMap);
        follower.setStartingPose(startPose);

        buildPaths(); // Генерация путей

//        lifts = new LiftsController(this);
//        outtake = new Outtake(hardwareMap);
        intakeMotor = new IntakeController(hardwareMap);
//        liftMotors = new LiftsController(hardwareMap);
        outtake = new Outtake(hardwareMap);
        intake = new Intake(hardwareMap, intakeMotor, lifts, outtake);
        lifts = new LiftsController(hardwareMap);
    }

    @Override
    public void loop() {
        follower.update();
        autonomousPathUpdate();
        intake.update();
        outtake.update();
        lifts.update();
        intakeMotor.update();
        double imuHeading = follower.getPose().getHeading();
//        follower.getPose().setHeading(imuHeading);

        telemetry.addData("path state", pathState);
        telemetry.addData("x", follower.getPose().getX());
        telemetry.addData("y", follower.getPose().getY());
        telemetry.addData("heading", follower.getPose().getHeading());
        telemetry.addData("liftPosition", lifts.getCurrentPosition());
        follower.telemetryDebug(telemetry);
        telemetry.update();
    }


    @Override
    public void start() {
        opmodeTimer.resetTimer();
        setPathState(0);
    }

    @Override
    public void stop() {
    }
}