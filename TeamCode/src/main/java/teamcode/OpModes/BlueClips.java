package teamcode.OpModes;
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
import com.qualcomm.robotcore.util.ElapsedTime;

import pedroPathing.constants.FConstants;
import pedroPathing.constants.LConstants;
import Controllers.ExtendoController;
import SubSystems.Intake;
import Controllers.LiftsController;
import SubSystems.Outtake;


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

    private final Pose startPose = new Pose(3.339, 78.678, 0);
    private Intake intake;
    private ExtendoController intakeMotor;
    private ElapsedTime timer = new ElapsedTime();

    public void buildPaths() {
        path1 = follower.pathBuilder()
                .addPath(  //вешает первый
                        new BezierLine(
                                new Point(3.339, 78.678, Point.CARTESIAN),
                                new Point(32.6, 72.000, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(0))
//                .setZeroPowerAccelerationMultiplier(2.5)
                .build();

        path2 = follower.pathBuilder()
                .addPath(
                        // едет за вторым
                        new BezierCurve(
                                new Point(32.470, 72.000, Point.CARTESIAN),
                                new Point(1.878, 15.652, Point.CARTESIAN),
                                new Point(69.496, 43.200, Point.CARTESIAN),
                                new Point(53.009, 24.835, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(0))

                .addPath(
//                         едет толкает второй
                        new BezierLine(
                                new Point(53.009, 24.835, Point.CARTESIAN),
                                new Point(18.574, 24.835, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(0))
                .addPath(
                        // едет за третьим
                        new BezierCurve(
                                new Point(18.574, 24.835, Point.CARTESIAN),
                                new Point(63.652, 22.539, Point.CARTESIAN),
                                new Point(51.965, 16.070, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(0))
                .addPath(
                        // толкает третий
                        new BezierLine(
                                new Point(51.965, 16.070, Point.CARTESIAN),
                                new Point(19.530, 15.443, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(0))
                .addPath(
                        // едет за четверым
                        new BezierCurve(
                                new Point(19.530, 15.443, Point.CARTESIAN),
                                new Point(56.557, 17.113, Point.CARTESIAN),
                                new Point(52.591, 8.974, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(0))
                .addPath(
                        // толкает четвертый
                        new BezierLine(
                                new Point(52.591, 8.974, Point.CARTESIAN),
                                new Point(19.5, 9.183, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(0))
                .setZeroPowerAccelerationMultiplier(2.0)
                .addPath(
                        new BezierCurve(
                                new Point(19.5, 9.183, Point.CARTESIAN),
                                new Point(11.478, 26.504, Point.CARTESIAN),
                                new Point(4.0, 33.391, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(0))
                .setZeroPowerAccelerationMultiplier(2.0)
                .build();

        path3 = follower.pathBuilder()
                .addPath(
                        // second pixel put
                        new BezierCurve(
                                new Point(4.0, 33.391, Point.CARTESIAN),
                                new Point(12.104, 64.904, Point.CARTESIAN),
                                new Point(32.470, 69.078, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(0))
//                .setZeroPowerAccelerationMultiplier(2.5)
                .build();
//
        path4 = follower.pathBuilder()
                .addPath(
                        // go back after second specimen
                        new BezierLine(
                                new Point(32.470, 69.078, Point.CARTESIAN),
                                new Point(4.0, 33.391, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(0))
                .setZeroPowerAccelerationMultiplier(2.5)
                .build();
//
        path5 = follower.pathBuilder()
                .addPath(
                        // go put 3d specimen
                        new BezierLine(
                                new Point(4.0, 33.391, Point.CARTESIAN),
                                new Point(33.0, 67.948, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(0))
//                .setZeroPowerAccelerationMultiplier(2.5)
                .build();
//
        path6 = follower.pathBuilder()
                .addPath(
                        // go back after 3d specimen
                        new BezierLine(
                                new Point(33.0, 67.948, Point.CARTESIAN),
                                new Point(4.0, 33.391, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(0))
                .setZeroPowerAccelerationMultiplier(2.5)
                .build();
//
        path7 = follower.pathBuilder()
                .addPath(
                        // go put 4th specimen
                        new BezierCurve(
                                new Point(4.0, 33.391, Point.CARTESIAN),
                                new Point(15.861, 66.991, Point.CARTESIAN),
                                new Point(32.470, 66.487, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(0))
//                .setZeroPowerAccelerationMultiplier(2.5)
                .build();
//
        path8 = follower.pathBuilder()
                .addPath(
                        // go back from 4th specimen
                        new BezierLine(
                                new Point(32.470, 66.487, Point.CARTESIAN),
                                new Point(4.0, 33.391, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(0))
                .setZeroPowerAccelerationMultiplier(2.5)
                .build();
//
        path9 = follower.pathBuilder()
                .addPath(
                        // go put 5th specimen
                        new BezierLine(
                                new Point(4.0, 33.391, Point.CARTESIAN),
                                new Point(33.0, 64.896, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(0))
//                .setZeroPowerAccelerationMultiplier(3.0)
                .build();
//
        path10 = follower.pathBuilder()
                .addPath(
                        // parking
                        new BezierLine(
                                new Point(33.0, 64.896, Point.CARTESIAN),
                                new Point(9.809, 29.009, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(0))
                .build();
    }

    /** FSM (Логика автономки) */
    public void autonomousPathUpdate() {
        switch (pathState) {
            case 0: // едет и поднимает лифт
                follower.followPath(path1, true);
                lifts.setTarget(LiftsController.HIGH_BAR);
                outtake.setClipsPutState();
                setPathState(1);
                break;

            case 1:
                if (!follower.isBusy() && timer.seconds() > 3.0) { //2.0
                    outtake.setClipsTakeState();
                    follower.followPath(path2, true);
                    timer.reset();
                    setPathState(2);
                }
                break;


            case 2: //берет второй клипс
                if (!follower.isBusy() && timer.seconds() > 0.3) {
                    outtake.setClipsPutState();
                    timer.reset();
                    setPathState(3);
                }
                break;


            case 3: // едет ставить второй клипс
                if (!follower.isBusy() && timer.seconds() > 0.5) {
                    follower.setMaxPower(0.9);
                    follower.followPath(path3, true);
                    timer.reset();
                    setPathState(4);
                }
                break;
            case 4:
                if(!follower.isBusy() && timer.seconds() > 0.5) {
                    outtake.setClipsTakeState();
                    timer.reset();
                    setPathState(5);
                }
                break;

            case 5: //едет обратно после второго
                if (!follower.isBusy() && timer.seconds() > 0.5) {
                    follower.setMaxPower(1.0);
                    follower.followPath(path4, true);
//                    outtake.setClipsTakeState();
                    timer.reset();
                    setPathState(6);
                }
                break;

            case 6: //берет третий клипс
                if (!follower.isBusy() && timer.seconds() > 1.0) {
                    outtake.setClipsPutState();
                    timer.reset();
                    setPathState(7);
                }
                break;

            case 7: // едет ставить третий клипс
                if (!follower.isBusy() && timer.seconds() > 0.3) {
                    follower.setMaxPower(0.9);
                    follower.followPath(path5, true);
                    setPathState(8);
                }
                break;

            case 8: //едет обратно после третьего
                if (!follower.isBusy() && timer.seconds() > 1.5) {
                    follower.setMaxPower(1.0);
                    follower.followPath(path6, true);
                    outtake.setClipsTakeState();
                    timer.reset();
                    setPathState(9);
                }
                break;

            case 9: //берет четвертый клипс
                if (!follower.isBusy() && timer.seconds() > 1.0) {
                    outtake.setClipsPutState();
                    timer.reset();
                    setPathState(10);
                }
                break;

            case 10: // едет ставить четвертый клипс
                if (!follower.isBusy() && timer.seconds() > 0.3) {
                    follower.setMaxPower(0.9);
                    follower.followPath(path7, true);
                    timer.reset();
                    setPathState(11);
                }
                break;

            case 11: //едет обратно после четвертого
                if (!follower.isBusy() && timer.seconds() > 1.5) {
                    follower.setMaxPower(1.0);
                    follower.followPath(path8, true);
                    outtake.setClipsTakeState();
                    timer.reset();
                    setPathState(12);
                }
                break;

            case 12: //берет пятый клипс
                if (!follower.isBusy() && timer.seconds() > 1.0) {
                    outtake.setClipsPutState();
                    timer.reset();
                    setPathState(13);
                }
                break;

            case 13: // едет ставить пятый клипс
                if (!follower.isBusy() && timer.seconds() > 0.3) {
                    follower.setMaxPower(0.9);
                    follower.followPath(path9, true);
                    timer.reset();
                    setPathState(15);
                }
                break;

            case 15: //едет обратно после пятого
                if (!follower.isBusy() && timer.seconds() > 1.5) {
                    outtake.setClipsTakeState();
                    follower.setMaxPower(1.0);
                    follower.followPath(path10, true);
//                    lifts.setTarget(LiftsController.GROUND);
                    timer.reset();
                    setPathState(16);
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
        lifts = new LiftsController(hardwareMap);
        intakeMotor = new ExtendoController(hardwareMap);
        outtake = new Outtake(hardwareMap, lifts);
        intake = new Intake(hardwareMap, intakeMotor, lifts, outtake);
        outtake.setPreloadPosition();
        buildPaths();
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
        telemetry.addData("liftTarget", lifts.getCurrentTarget());
        telemetry.addData("liftPower", lifts.rightLift.getPower());
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