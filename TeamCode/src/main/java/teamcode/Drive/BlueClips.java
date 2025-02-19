package teamcode.Drive;
//package pedroPathing.constants;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
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


@Autonomous(name = "BlueClips", group = "Autonomous")
public class BlueClips extends OpMode {

    private Follower follower;
    private Timer pathTimer, opmodeTimer;
    private boolean poseSet = false;
    private int pathState = 0; // FSM начальное состояние

    private LiftsController lifts;
    private Outtake outtake;
    private boolean clips = false;

    private PathChain path1, path2, path3, path4, path5, path6, path7, path8, path9, path10;

    private final Pose startPose = new Pose(9, 65, 0);
    private Intake intake;
//    private Outtake outtake;
//    private LiftsController liftMotors;
    private IntakeController intakeMotor;

    public void buildPaths() {
        path1 = follower.pathBuilder()
                .addPath(
                        // 1 specimen put
                        new BezierLine(
                                new Point(9.000, 65, Point.CARTESIAN),
                                new Point(30.000, 65, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(360))
                .build();

        path2 = follower.pathBuilder()
                .addPath(
                        // Line 1
                        new BezierCurve(
                                new Point(30.000, 65.000, Point.CARTESIAN),
                                new Point(1.252, 21.913, Point.CARTESIAN),
                                new Point(68.661, 40.696, Point.CARTESIAN),
                                new Point(58.226, 25.878, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(360))

                .addPath(
//                         Line 2
                        new BezierLine(
                                new Point(58.226, 25.878, Point.CARTESIAN),
                                new Point(32.765, 27.548, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(360))
                .addPath(
                        // Line 3
                        new BezierCurve(
                                new Point(32.765, 27.548, Point.CARTESIAN),
                                new Point(55.513, 31.304, Point.CARTESIAN),
                                new Point(60.104, 16.487, Point.CARTESIAN)

                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(360))
                .addPath(
                        // Line 4
                        new BezierLine(
                                new Point(60.104, 16.487, Point.CARTESIAN),
                                new Point(33.809, 17.322, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(360))
                .addPath(
                        // Line 5
                        new BezierCurve(
                                new Point(33.809, 17.322, Point.CARTESIAN),
                                new Point(56.974, 18.574, Point.CARTESIAN),
                                new Point(58.226, 9.809, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(360))
                .addPath(
                        // Line 6
                        new BezierLine(
                                new Point(58.226, 9.809, Point.CARTESIAN),
                                new Point(22.330, 10.017, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(360))
                .build();

        path3 = follower.pathBuilder()
                .addPath(
                        // second pixel put
                        new BezierCurve(
                                new Point(22.330, 10.017, Point.CARTESIAN),
                                new Point(26.504, 70.539, Point.CARTESIAN),
                                new Point(34.226, 72.209, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(360))
                .build();
//
        path4 = follower.pathBuilder()
                .addPath(
                        // go back after second specimen
                        new BezierCurve(
                                new Point(34.226, 72.209, Point.CARTESIAN),
                                new Point(23.583, 70.539, Point.CARTESIAN),
                                new Point(29.217, 26.922, Point.CARTESIAN),
                                new Point(21.078, 30.261, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(360))
                .build();
//
        path5 = follower.pathBuilder()
                .addPath(
                        // go put 3d specimen
                        new BezierCurve(
                                new Point(21.078, 30.261, Point.CARTESIAN),
                                new Point(16.278, 66.574, Point.CARTESIAN),
                                new Point(34.017, 71.791, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(360))
                .build();
//
        path6 = follower.pathBuilder()
                .addPath(
                        // go back after 3d specimen
                        new BezierCurve(
                                new Point(34.017, 71.791, Point.CARTESIAN),
                                new Point(16.278, 66.365, Point.CARTESIAN),
                                new Point(20.870, 30.261, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(360))
                .build();
//
        path7 = follower.pathBuilder()
                .addPath(
                        // go put 4th specimen
                        new BezierCurve(
                                new Point(20.870, 30.261, Point.CARTESIAN),
                                new Point(16.278, 66.365, Point.CARTESIAN),
                                new Point(34.017, 72.835, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(360))
                .build();
//
        path8 = follower.pathBuilder()
                .addPath(
                        // go back from 4th specimen
                        new BezierCurve(
                                new Point(34.017, 72.835, Point.CARTESIAN),
                                new Point(15.443, 76.174, Point.CARTESIAN),
                                new Point(22.957, 27.965, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(360))
                .build();
//
        path9 = follower.pathBuilder()
                .addPath(
                        // go put 5th specimen
                        new BezierLine(
                                new Point(14.817, 34.852, Point.CARTESIAN),
                                new Point(36.313, 72.000, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
                .build();
//
        path10 = follower.pathBuilder()
                .addPath(
                        // parking
                        new BezierLine(
                                new Point(36.313, 72.000, Point.CARTESIAN),
                                new Point(13.148, 29.635, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
                .build();
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
                    lifts.setTarget(LiftsController.HIGH_BAR);
                    setPathState(2);
                }
                break;

//
            case 2: //ставит клипс
                if (lifts.getCurrentPosition() >= 780) {
                    outtake.setClipsTakeState();
                    lifts.setTarget(LiftsController.GROUND);
                    follower.followPath(path2, true);
                    setPathState(3);
                }
                break;

            case 3: // Отпускает клипс и едет толкать сэмплы
//                lifts.setTarget(LiftsController.GROUND);
                if (!follower.isBusy()) {
                    outtake.setClipsPutState();
                    setPathState(4);
                }
                break;

            case 4:
                if (!follower.isBusy()) {
                    follower.followPath(path3, true);
                    setPathState(5);
                }
                break;

            case 5:
                if (!follower.isBusy()) {
                    lifts.setTarget(LiftsController.HIGH_BAR);
                    setPathState(6);
                }
                break;

            case 6:
                if (lifts.getCurrentPosition() >= 780) {
                    lifts.setTarget(LiftsController.GROUND);
                    outtake.setClipsTakeState();
                    setPathState(7);
                }
                break;

            case 7:
                if (!follower.isBusy()) {
                    follower.followPath(path4, true);
                    setPathState(8);
                }
                break;

            case 8: // Отпускает клипс и едет толкать сэмплы
                if (!follower.isBusy()) {
                    outtake.setClipsPutState();
                    setPathState(9);
                }
                break;

            case 9:
                if (!follower.isBusy()) {
                follower.followPath(path5, true);
                setPathState(10);
                }
                break;

            case 10:
                if (!follower.isBusy()) {
                    lifts.setTarget(LiftsController.HIGH_BAR);
                    setPathState(11);
                }
                break;

            case 11:
                if (lifts.getCurrentPosition() >= 780) {
                    lifts.setTarget(LiftsController.GROUND);
                    outtake.setClipsTakeState();
                    setPathState(12);
                }
                break;

            case 12:
                if (!follower.isBusy()) {
                    follower.followPath(path6, true);
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
            case 18:
                if(!follower.isBusy()) {
                    outtake.setClipsPutState();
                    setPathState(19);
                }
            case 19:
                if (!follower.isBusy()) {
                    follower.followPath(path9, true);
                    setPathState(20);
                }
                break;
            case 20:
                if(!follower.isBusy()) {
                    lifts.setTarget(LiftsController.HIGH_BAR);
                    setPathState(21);
                }
                break;





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