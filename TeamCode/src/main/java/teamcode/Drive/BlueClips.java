package teamcode.Drive;
//package pedroPathing.constants;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import com.pedropathing.follower.Follower;
import com.pedropathing.localization.Pose;
import com.pedropathing.pathgen.BezierCurve;
import com.pedropathing.pathgen.BezierLine;
import com.pedropathing.pathgen.PathChain;
import com.pedropathing.pathgen.Point;
import com.pedropathing.util.Constants;
import com.pedropathing.util.Timer;
import pedroPathing.constants.FConstants;
import pedroPathing.constants.LConstants;
import SubSystems.ExtendoController;
import SubSystems.Intake;
import SubSystems.LiftsController;
import SubSystems.Outtake;


@Disabled
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
    private ExtendoController intakeMotor;

    public void buildPaths() {
        path1 = follower.pathBuilder()
                .addPath(
                        // 1 specimen put
                        new BezierLine(
                                new Point(9.000, 65, Point.CARTESIAN),
                                new Point(33.000, 65, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(360))
                .setPathEndVelocityConstraint(0.5)
                .build();

        path2 = follower.pathBuilder()
                .addPath(
                        // Line 1
                        new BezierCurve(
                                new Point(33.000, 65.000, Point.CARTESIAN),
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
                                new Point(22.0, 10.017, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(360))
                .build();

        path3 = follower.pathBuilder()
                .addPath(
                        // second pixel put
                        new BezierCurve(
                                new Point(22.0, 10.017, Point.CARTESIAN),
                                new Point(26.504, 70.539, Point.CARTESIAN),
                                new Point(34.226, 72.209, Point.CARTESIAN)
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
                                new Point(34.226, 72.209, Point.CARTESIAN),
                                new Point(23.583, 70.539, Point.CARTESIAN),
                                new Point(29.217, 26.922, Point.CARTESIAN),
                                new Point(19.078, 30.261, Point.CARTESIAN)
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
                                new Point(19.078, 30.261, Point.CARTESIAN),
                                new Point(16.278, 66.574, Point.CARTESIAN),
                                new Point(34.017, 71.791, Point.CARTESIAN)
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
        path7 = follower.pathBuilder()
                .addPath(
                        // go put 4th specimen
                        new BezierCurve(
                                new Point(20.035, 28.383, Point.CARTESIAN),
                                new Point(36.939, 24.209, Point.CARTESIAN),
                                new Point(13.357, 76.591, Point.CARTESIAN),
                                new Point(34.139, 70.957, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(360))
                .setPathEndVelocityConstraint(0.6)
                .build();
//
        path8 = follower.pathBuilder()
                .addPath(
                        // go back from 4th specimen
                        new BezierCurve(
                                new Point(34.139, 70.957, Point.CARTESIAN),
                                new Point(20.035, 73.878, Point.CARTESIAN),
                                new Point(39.026, 30.887, Point.CARTESIAN),
                                new Point(21.704, 34.852, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(360))
//                .setPathEndVelocityConstraint(0.6)
                .build();
//
        path9 = follower.pathBuilder()
                .addPath(
                        // go put 5th specimen
                        new BezierCurve(
                                new Point(21.704, 34.852, Point.CARTESIAN),
                                new Point(31.304, 31.513, Point.CARTESIAN),
                                new Point(20.661, 72.000, Point.CARTESIAN),
                                new Point(33.930, 67.826, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(360))
                .setPathEndVelocityConstraint(0.5)
                .build();
//
        path10 = follower.pathBuilder()
                .addPath(
                        // parking
                        new BezierCurve(
                                new Point(33.930, 67.826, Point.CARTESIAN),
                                new Point(20.661, 71.374, Point.CARTESIAN),
                                new Point(17.739, 29.843, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(360))
//                .setPathEndVelocityConstraint(0.6)
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
                if (!follower.isBusy()) {
                    outtake.setClipsPutState();
                    setPathState(4);
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

        follower = new Follower(hardwareMap);
        follower.setStartingPose(startPose);

        buildPaths(); // Генерация путей

        intakeMotor = new ExtendoController(hardwareMap);
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