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
import com.qualcomm.robotcore.util.ElapsedTime;

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

    private final Pose startPose = new Pose(8.974, 102.678, 0);
    private Intake intake;

    private IntakeController intakeMotor;
    private ElapsedTime timer = new ElapsedTime();

    public void buildPaths() {
        path1 = follower.pathBuilder()
                .addPath(
                        // 1 specimen put
                        new BezierCurve(
                                new Point(8.974, 102.678, Point.CARTESIAN),
                                new Point(28.800, 108.730, Point.CARTESIAN),
                                new Point(17.991, 126.052, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(-45))
//                .setPathEndVelocityConstraint(0.5)
                .build();

        path2 = follower.pathBuilder()
                .addPath(
                        // second specimen
                        new BezierLine(
                                new Point(17.991, 126.052, Point.CARTESIAN),
                                new Point(21.0, 122, Point.CARTESIAN)  //21.330
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(-45), Math.toRadians(0))
                .build();

        path3 = follower.pathBuilder()
                .addPath(
                        // second pixel put
                        new BezierLine(
                                new Point(8.974, 102.678, Point.CARTESIAN),
                                new Point(17.991, 130.017, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(-45))
//                .setPathEndVelocityConstraint(0.1)
                .build();
//
        path4 = follower.pathBuilder()
                .addPath(
                        // go back after second specimen
                        new BezierLine(
                                new Point(16.904, 130.017, Point.CARTESIAN),
                                new Point(21.287, 131.061, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(-45), Math.toRadians(0))
//                .setPathEndVelocityConstraint(0.6)
                .build();
//
        path5 = follower.pathBuilder()
                .addPath(
                        // go put 3d specimen
                        new BezierLine(
                                new Point(21.287, 131.061, Point.CARTESIAN),
                                new Point(17.991, 129.600, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(-45))
//                .setPathEndVelocityConstraint(0.5)
                .build();
//
        path6 = follower.pathBuilder()
                .addPath(
                        // go back after 3d specimen
                        new BezierLine(
                                new Point(16.904, 129.600, Point.CARTESIAN),
                                new Point(20.243, 133.148, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(-45), Math.toRadians(20))
//                .setPathEndVelocityConstraint(0.6)
                .build();


        path7 = follower.pathBuilder()
                .addPath(
                        // go back after 3d specimen
                                new BezierLine(
                                        new Point(20.243, 133.148, Point.CARTESIAN),
                                        new Point(17.991, 129.809, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(20), Math.toRadians(-45))
                .build();

        path8 = follower.pathBuilder()
                .addPath(
                        // go back after 3d specimen
                        new BezierCurve(
                                new Point(16.904, 129.809, Point.CARTESIAN),
                                new Point(64.070, 130.852, Point.CARTESIAN),
                                new Point(64.904, 90.365, Point.CARTESIAN)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(-45), Math.toRadians(90))
                .build();



    }


    /** FSM (Логика автономки) */
    public void autonomousPathUpdate() {
        switch (pathState) {
            case 0: // едет и поднимает лифт
                follower.followPath(path1, true);
                outtake.setScoreState();
                lifts.setTarget(LiftsController.HIGHEST_BASKET);
                timer.reset();
                setPathState(2);
                break;
//
            case 2:  //ставит первый
                if (lifts.getCurrentPosition() >= 1000 && timer.seconds() >= 2.0) {
                    outtake.setDrop();
                    timer.reset();
                    setPathState(3);
                }
                break;
//
            case 3:
                if (!follower.isBusy() && timer.seconds() > 0.5) {
                    follower.followPath(path2, true);
                    lifts.setTarget(LiftsController.GROUND);
                    intake.setOpenState();
                    setPathState(4);
                }
                break;



//
            case 4:
                if (!follower.isBusy()) {
                    intakeMotor.setTarget(IntakeController.LONG);
                    intake.setOpenState();
                    setPathState(5);
        }
        break;

            case 5:
                if(!follower.isBusy() && intakeMotor.getCurrentPosition() >= 450) {
                    intake.setClosedState();
//                    outtake.setGrabState();
                    timer.reset();
                    setPathState(6);
                }
                break;
//
            case 6:
                if (timer.seconds() > 1.2) {
                    intake.setTransfer();
                    timer.reset();
                    setPathState(7);
                }
                break;


            case 7:
                if (!follower.isBusy()) {
                    follower.followPath(path3, true);
                    setPathState(8);
                }
                break;

            case 8:
                if(lifts.getCurrentPosition() >= 1000 && timer.seconds() >= 2.1) {
                    outtake.setDrop();
                    timer.reset();
                    setPathState(9);
                }
                break;

            case 9:
                if (!follower.isBusy() && timer.seconds() > 0.5) {
                    follower.followPath(path4, true);
                    lifts.setTarget(LiftsController.GROUND);
                    intake.setOpenState();
                    setPathState(10);
                }
                break;

            case 10:
                if (!follower.isBusy()) {
                    intakeMotor.setTarget(IntakeController.LONG);
                    setPathState(11);
                }
                break;

            case 11:
                if(!follower.isBusy() && intakeMotor.getCurrentPosition() >= 450) {
                    intake.setClosedState();
                    timer.reset();
                    setPathState(12);
                }
                break;

            case 12:
                if (timer.seconds() > 1.2) {
                    intake.setTransfer();
                    timer.reset();
                    setPathState(13);
                }
                break;

            case 13:
                if (!follower.isBusy()) {
                    follower.followPath(path5, true);
                    setPathState(14);
                }
                break;

            case 14:
                if(lifts.getCurrentPosition() >= 1200 && timer.seconds() >= 2.1) {
                    outtake.setDrop();
                    timer.reset();
                    setPathState(15);
                }
                break;

            case 15:
                if (!follower.isBusy() && timer.seconds() > 0.5) {
                    follower.followPath(path6, true);
                    lifts.setTarget(LiftsController.GROUND);
                    intake.setOpenState();
                    intake.setTurnPosition3();
                    setPathState(16);
                }
                break;
            case 16:
                if (!follower.isBusy()) {
                    intakeMotor.setTarget(IntakeController.LONG);
                    setPathState(17);
                }
                break;
            case 17:
                if(!follower.isBusy() && intakeMotor.getCurrentPosition() >= 450) {
                    intake.setClosedState();
                    timer.reset();
                    setPathState(18);
                }
                break;

            case 18:
                if (timer.seconds() > 1.2) {
                    intake.setTransfer();
                    timer.reset();
                    setPathState(19);
                }
                break;
            case 19:
                if (!follower.isBusy()) {
                    follower.followPath(path7, true);
                    setPathState(20);
                }
                break;

            case 20:
                if(lifts.getCurrentPosition() >= 1200 && timer.seconds() >= 2.1) {
                    outtake.setDrop();
                    timer.reset();
                    setPathState(21);
                }
                break;
            case 21:
                if (!follower.isBusy() && timer.seconds() > 0.5) {
                    follower.followPath(path8);
                    lifts.setTarget(LiftsController.GROUND);
                    setPathState(22);
                }
                break;
            case 22:
                if (!follower.isBusy()) {
                    lifts.setTarget(650);
                    setPathState(23);
                }
                break;





//
//            case 7:
//                if (!follower.isBusy()) {
//                    lifts.setTarget(LiftsController.HIGHEST_BASKET);
//                    outtake.setScoreState();
//                    setPathState(8);
//                }
//                break;
//
//            case 8: // cтавит второй
//                if (lifts.getCurrentPosition() >= 1300) {
//                    outtake.setDrop();
//                    setPathState(9);
//                }
//                break;
//
//            case 9:
//                if (outtake.isDropComplete) {
//                    follower.followPath(path4, true);
//                outtake.setGrabState();
//                lifts.setTarget(LiftsController.GROUND);
//                setPathState(10);
//                }
//                break;
//
//            case 10:
//                if (!follower.isBusy()) {
//                    intakeMotor.setTarget(IntakeController.LONG);
//                    intake.setOpenState();
//                    setPathState(11);
//                }
//                break;
//
//            case 11:
//                if (intakeMotor.getCurrentPosition() >= 500) {
//                    intake.setClosedState();
//                    setPathState(12);
//                }
//                break;
//
//            case 12:
//                if (intake.isClosedComplete) {
//                    intakeMotor.setTarget(IntakeController.ZERO);
//                    intake.setTransfer();
//                    follower.followPath(path5, true);
//                    setPathState(13);
//                }
//                break;
//
//            case 13: //ставит третий
//                if (!follower.isBusy()) {
//                    lifts.setTarget(LiftsController.HIGHEST_BASKET);
//                    outtake.setScoreState();
//                    setPathState(14);
//                }
//                break;
//
//            case 14:
//                if(lifts.getCurrentPosition() >= 1300) {
//                    outtake.setDrop();
//                    setPathState(15);
//                }
//                break;








//
//            case 15:
//                if (!follower.isBusy()) {
//                    lifts.setTarget(LiftsController.HIGH_BAR);
//                    setPathState(16);
//                }
//                break;
//            case 16:
//                if (lifts.getCurrentPosition() >= 780) {
//                    lifts.setTarget(LiftsController.GROUND);
//                    outtake.setClipsTakeState();
//                    setPathState(17);
//                }
//                break;
//            case 17:
//                if(!follower.isBusy()) {
//                    follower.followPath(path8, true);
//                    setPathState(18);
//                }
//                break;
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
//        outtake.dropper.setPosition(Outtake.DROPPER_CLOSE);
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
        lifts = new LiftsController(hardwareMap);
        intake = new Intake(hardwareMap, intakeMotor, lifts, outtake);

    }

    @Override
    public void loop() {
        follower.update();
        autonomousPathUpdate();
        intake.update();
        outtake.update();
        lifts.update();
        intakeMotor.update();
//        double imuHeading = follower.getPose().getHeading();

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