package teamcode.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
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
import Controllers.ExtendoController;
import Controllers.LiftsController;
import SubSystems.Intake;
import SubSystems.Outtake;
import SubSystems.OuttakeAuto;
import pedroPathing.constants.FConstants;
import pedroPathing.constants.LConstants;

@Autonomous(name="BlueBasket", group="Autonomous")
public class BlueBasket extends OpMode {
    private Follower follower;
    private Timer pathTimer, opmodeTimer;
    private boolean poseSet = false;
    private int pathState = 0;
    private LiftsController lifts;
    private Outtake outtake;
    private OuttakeAuto outtakeAuto;
    private boolean clips = false;
    private PathChain path1, path2, path3, path4, path5, path6, path7, path8;
    private final Pose startPose = new Pose(7.722, 103.304, 0);
    private Intake intake;
    private ExtendoController intakeMotor;
    private ElapsedTime timer = new ElapsedTime();

    public void buildPaths() {
        path1 = follower.pathBuilder()
                .addPath(
                        new BezierCurve(
                                new Point(7.722, 103.304, Point.CARTESIAN),
                                new Point(25.670, 121.252, Point.CARTESIAN),
                                new Point(17.2, 126.261, Point.CARTESIAN)
                        )  //16.487 and 126.261
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(-30))
                .setZeroPowerAccelerationMultiplier(3)
                .build();

        path2 = follower.pathBuilder() //take the second sample
                .addPath(
                        new BezierLine(
                                new Point(17.2, 126.261, Point.CARTESIAN),
                                new Point(33.435, 120, Point.CARTESIAN)
                        ) //33.435
                )
                .setLinearHeadingInterpolation(Math.toRadians(-30), Math.toRadians(0))
                .build();

        path3 = follower.pathBuilder()
                .addPath(
                        new BezierLine(
                                new Point(33.435, 120, Point.CARTESIAN),
                                new Point(16.4, 126.261, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(-30))
                .setZeroPowerAccelerationMultiplier(2)
                .build();

        path4 = follower.pathBuilder() //третий sample взять take the 3rd sample
                .addPath(
                        new BezierLine(
                                new Point(16.4, 126.261, Point.CARTESIAN),
                                new Point(33.435, 130.061, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(-30), Math.toRadians(0))
                .build();

        path5 = follower.pathBuilder()
                .addPath(
                        new BezierLine(
                                new Point(33.435, 130.061, Point.CARTESIAN),
                                new Point(16.4, 126.261, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(-30))
                .setZeroPowerAccelerationMultiplier(1.5)
                .build();

        path6 = follower.pathBuilder() //for the last one
                .addPath(
                        new BezierCurve(
                                new Point(16.4, 126.261, Point.CARTESIAN),
                                new Point(20.661, 121.043, Point.CARTESIAN),
                                new Point(33, 126.5, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(-30), Math.toRadians(90))
                .build();

        path7 = follower.pathBuilder()
                .addPath(
                        new BezierLine(
                                new Point(33, 126.5, Point.CARTESIAN),
                                new Point(16.4, 128.261, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(90), Math.toRadians(-30))
                .setZeroPowerAccelerationMultiplier(2.5)
                .build();
//
        path8 = follower.pathBuilder()
                .addPath(
                        new BezierCurve(
                                new Point(16.4, 128.261, Point.CARTESIAN),
                                new Point(55.096, 127.513, Point.CARTESIAN),
                                new Point(65.809, 90.748, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(-30), Math.toRadians(270))
                .build();
    }

    public void autonomousPathUpdate() {
        switch (pathState) {
            case 0:
                follower.followPath(path1, true);
                outtakeAuto.setScoreState();
                lifts.setTarget(LiftsController.HIGHEST_BASKET);
                timer.reset();
                setPathState(2);
                break;
            case 2:
                if (!follower.isBusy() && timer.seconds() >= 1.5) {
                    outtakeAuto.setDrop();
                    timer.reset();
                    setPathState(3);
                }
                break;
            case 3:
                if (!follower.isBusy() && timer.seconds() > 1.0) {
                    follower.followPath(path2, true);
//                    lifts.setTarget(LiftsController.GROUND);
                    intake.setOpenState();
                    setPathState(4);
                }
                break;

            case 4:
                if(timer.seconds() > 1.5) {
                    lifts.setTarget(LiftsController.GROUND);
                    setPathState(5);
                }
                break;

            case 5:
                if (!follower.isBusy() && timer.seconds() > 2.5) {
                    intake.setClosedState();
                    timer.reset();
                    setPathState(6);
                }
                break;

            case 6:
                if (timer.seconds() > 1.0 && !follower.isBusy()) {
                    intake.setTransfer();
                    timer.reset();
                    setPathState(7);
                }
                break;
            case 7:
                if (timer.seconds() >= 1.0 && !follower.isBusy()) {
                    follower.followPath(path3, true);
                    timer.reset();
                    setPathState(8);
                }
                break;

            case 8:
                if (timer.seconds() >= 2.0 && !follower.isBusy()) {
                    outtakeAuto.setDrop();
                    intake.setOpenState();
                    setPathState(9);
                }
                break;

            case 9: //поехал за третьим сэмплом  went for the 3rd sample
                if (!follower.isBusy() && timer.seconds() > 0.5) {
                    follower.followPath(path4, true);
                    intake.setOpenState();
                    setPathState(10);
                }
                break;
            case 10:
                if(timer.seconds() > 1.5) {
                    lifts.setTarget(LiftsController.GROUND);
                    setPathState(11);
                }
                break;
            case 11:
                if (!follower.isBusy() && timer.seconds() > 3.5) {
//                    lifts.setTarget(LiftsController.GROUND);
                    intake.setClosedState();
                    timer.reset();
                    setPathState(12);
                }
                break;
            case 12:
                if (timer.seconds() > 1.0 && !follower.isBusy()) {
                    intake.setTransfer();
                    timer.reset();
                    setPathState(13);
                }
                break;
            case 13:  //put 3rd sample
                if(!follower.isBusy() && timer.seconds() > 1.0) {
                    follower.followPath(path5, true);
                    timer.reset();
                    setPathState(14);
                }
                break;
            case 14:
                if (!follower.isBusy() && timer.seconds() >= 2.0) {
                    outtakeAuto.setDrop();
                    intake.setOpenState();
                    setPathState(15);
                }
                break;
            case 15:
                if (!follower.isBusy() && timer.seconds() > 1.0) {
                    follower.followPath(path6, true);
                    intake.intakeTurn.setPosition(Intake.INTAKE_TURN_POSITION_1);
                    setPathState(16);
                }
                break;
            case 16:
                if(timer.seconds() > 1.5) {
                lifts.setTarget(LiftsController.GROUND);
                setPathState(17);
            }
                break;
            case 17:
                if (!follower.isBusy() && timer.seconds() > 3.5) {
                    intake.setClosedState();
                    timer.reset();
                    setPathState(18);
                }
                break;
            case 18:
                if (timer.seconds() > 1.0 && !follower.isBusy()) {  //1.5
                    intake.setTransfer();
                    timer.reset();
                    setPathState(19);
                }
                break;
            case 19:
                if(!follower.isBusy() && timer.seconds() > 1.0) {
                    follower.followPath(path7, true);
                    timer.reset();
                    setPathState(20);
                }
            case 20:
                if (timer.seconds() >= 2.5 && !follower.isBusy()) {
                    outtakeAuto.setDrop();
                    intake.setOpenState();
                    setPathState(21);
                }
                break;
            case 21:
                if (!follower.isBusy() && timer.seconds() >= 3.0) {
                    follower.followPath(path8, true);
//                    outtakeAuto.setScoreState();
                    setPathState(22);
                }
                break;
            case 22:
                if(timer.seconds() >= 2.5) {
                    lifts.setTarget(LiftsController.GROUND);
                    setPathState(23);
                }
                break;
//            case 17:
//                if (!follower.isBusy() && intakeMotor.getCurrentPosition() >= 450) {
//                    intake.setClosedState();
//                    timer.reset();
//                    setPathState(18);
//                }
//                break;
//            case 18:
//                if (timer.seconds() > 1.2) {
//                    intake.setTransfer();
//                    timer.reset();
//                    setPathState(19);
//                }
//                break;
//            case 19:
//                if (!follower.isBusy()) {
//                    follower.followPath(path7, true);
//                    setPathState(20);
//                }
//                break;
//            case 20:
//                if (lifts.getCurrentPosition() >= 1200 && timer.seconds() >= 2.1) {
//                    outtake.setDrop();
//                    timer.reset();
//                    setPathState(21);
//                }
//                break;
//            case 21:
//                if (!follower.isBusy() && timer.seconds() > 0.5) {
//                    follower.followPath(path8, true);
//                    lifts.setTarget(LiftsController.GROUND);
//                    setPathState(22);
//                }
//                break;
//            case 22:
//                if (!follower.isBusy()) {
//                    lifts.setTarget(650);
//                    setPathState(23);
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
        follower = new Follower(hardwareMap);
        follower.setStartingPose(startPose);
        lifts = new LiftsController(hardwareMap);
        intakeMotor = new ExtendoController(hardwareMap);
        outtake = new Outtake(hardwareMap, lifts);
        outtakeAuto = new OuttakeAuto(hardwareMap, lifts);

        intake = new Intake(hardwareMap, intakeMotor, lifts, outtake);
//        outtake.setPreloadPosition();
        outtakeAuto.setPreloadPosition();
        buildPaths();
    }

    @Override
    public void loop() {
        follower.update();
        autonomousPathUpdate();
        intake.update();
        outtake.update();
        outtakeAuto.update();
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