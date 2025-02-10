//package teamcode.Drive;
//
//import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
//import com.qualcomm.robotcore.eventloop.opmode.OpMode;
//
//import com.pedropathing.follower.Follower;
//import com.pedropathing.localization.Pose;
//import com.pedropathing.pathgen.BezierCurve;
//import com.pedropathing.pathgen.BezierLine;
//import com.pedropathing.pathgen.Path;
//import com.pedropathing.pathgen.PathChain;
//import com.pedropathing.pathgen.Point;
//import com.pedropathing.util.Constants;
//import com.pedropathing.util.Timer;
//import pedroPathing.constants.FConstants;
//import pedroPathing.constants.LConstants;
//
//@Autonomous(name = "AutoOp", group = "Autonomous")
//public class BlueClips extends OpMode {
//
//    private Follower follower;
//    private Timer pathTimer, opmodeTimer;
//    private boolean poseSet = false;
//    private int pathState = 0; // FSM начальное состояние
//
//    private LiftsController lifts;
//    private Outtake outtake;
//    private boolean clips = false;
//
//    private PathChain path1, path2, path3, path4, path5, path6, path7, path8, path9, path10;
//
//    /** Стартовая позиция */
//    private final Pose startPose = new Pose(9, 65, 180);
//
//    /** Метод для построения всех путей */
//    public void buildPaths() {
//        path1 = follower.pathBuilder()
//                .addPath(
//                        // 1 specimen put
//                        new BezierLine(
//                                new Point(9.000, 65.983, Point.CARTESIAN),
//                                new Point(35.896, 66.157, Point.CARTESIAN)
//                        )
//                )
//                .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
//                .build();
//
//        path2 = follower.pathBuilder()
//                .addPath(
//                        // Line 1
//                        new BezierCurve(
//                                new Point(35.896, 66.157, Point.CARTESIAN),
//                                new Point(7.096, 37.983, Point.CARTESIAN),
//                                new Point(75.965, 32.974, Point.CARTESIAN),
//                                new Point(58.226, 23.374, Point.CARTESIAN)
//                        )
//                )
//                .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
//                .addPath(
//                        // Line 2
//                        new BezierLine(
//                                new Point(58.226, 23.374, Point.CARTESIAN),
//                                new Point(23.583, 23.165, Point.CARTESIAN)
//                        )
//                )
//                .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
//                .addPath(
//                        // Line 3
//                        new BezierCurve(
//                                new Point(23.583, 23.165, Point.CARTESIAN),
//                                new Point(67.409, 24.626, Point.CARTESIAN),
//                                new Point(58.017, 12.939, Point.CARTESIAN)
//                        )
//                )
//                .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
//                .addPath(
//                        // Line 4
//                        new BezierLine(
//                                new Point(58.017, 12.939, Point.CARTESIAN),
//                                new Point(24.000, 12.730, Point.CARTESIAN)
//                        )
//                )
//                .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
//                .addPath(
//                        // Line 5
//                        new BezierCurve(
//                                new Point(24.000, 12.730, Point.CARTESIAN),
//                                new Point(67.617, 14.400, Point.CARTESIAN),
//                                new Point(57.391, 5.635, Point.CARTESIAN)
//                        )
//                )
//                .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
//                .addPath(
//                        // Line 6
//                        new BezierLine(
//                                new Point(57.391, 5.635, Point.CARTESIAN),
//                                new Point(20.035, 6.261, Point.CARTESIAN)
//                        )
//                )
//                .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
//                .build();
//
//        path3 = follower.pathBuilder()
//                .addPath(
//                        // second pixel put
//                        new BezierCurve(
//                                new Point(20.035, 6.261, Point.CARTESIAN),
//                                new Point(23.791, 64.904, Point.CARTESIAN),
//                                new Point(35.270, 65.113, Point.CARTESIAN)
//                        )
//                )
//                .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
//                .build();
//
//        path4 = follower.pathBuilder()
//                .addPath(
//                        // go back after second specimen
//                        new BezierCurve(
//                                new Point(35.270, 65.113, Point.CARTESIAN),
//                                new Point(20.870, 61.565, Point.CARTESIAN),
//                                new Point(14.817, 34.852, Point.CARTESIAN)
//                        )
//                )
//                .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
//                .build();
//
//        path5 = follower.pathBuilder()
//                .addPath(
//                        // go put 3d specimen
//                        new BezierLine(
//                                new Point(14.817, 34.852, Point.CARTESIAN),
//                                new Point(34.435, 68.870, Point.CARTESIAN)
//                        )
//                )
//                .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
//                .build();
//
//        path6 = follower.pathBuilder()
//                .addPath(
//                        // go back after 3d specimen
//                        new BezierLine(
//                                new Point(35.478, 69.078, Point.CARTESIAN),
//                                new Point(14.817, 35.061, Point.CARTESIAN)
//                        )
//                )
//                .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
//                .build();
//
//        path7 = follower.pathBuilder()
//                .addPath(
//                        // go put 4th specimen
//                        new BezierLine(
//                                new Point(14.817, 35.061, Point.CARTESIAN),
//                                new Point(35.687, 68.870, Point.CARTESIAN)
//                        )
//                )
//                .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
//                .build();
//
//        path8 = follower.pathBuilder()
//                .addPath(
//                        // go back from 4th specimen
//                        new BezierLine(
//                                new Point(35.687, 68.870, Point.CARTESIAN),
//                                new Point(14.817, 34.852, Point.CARTESIAN)
//                        )
//                )
//                .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
//                .build();
//
//        path9 = follower.pathBuilder()
//                .addPath(
//                        // go put 5th specimen
//                        new BezierLine(
//                                new Point(14.817, 34.852, Point.CARTESIAN),
//                                new Point(36.313, 72.000, Point.CARTESIAN)
//                        )
//                )
//                .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
//                .build();
//
//        path10 = follower.pathBuilder()
//                .addPath(
//                        // parking
//                        new BezierLine(
//                                new Point(36.313, 72.000, Point.CARTESIAN),
//                                new Point(13.148, 29.635, Point.CARTESIAN)
//                        )
//                )
//                .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(180))
//                .build();
//    }
//
//    /** FSM (Логика автономки) */
//    public void autonomousPathUpdate() {
//        switch (pathState) {
//            case 0: // едет и поднимает лифт
//                follower.followPath(path1, true);
//                outtake.setClipsPutState();
//                lifts.setTarget(LiftsController.HIGH_BAR);
//                setPathState(1);
//                break;
//
//            case 1: //ставит клипс
//                if(follower.getPose().getX() > (36.5) && lifts.getHeight() > 490) {
//                    outtake.setClipsTakeState();
//                    setPathState(2);
//                }
//                break;
//
//            case 2: // Отпускает клипс и едет толкать сэмплы
//                lifts.moveToPosition(LiftsController.GROUND);
//                follower.followPath(path2, true);
//
//                if (!follower.isBusy()) {
//                    setPathState(3); // Ждет, пока path2 завершится, а потом переключается
//                }
//                break;
//
//            case 3:
//                if (follower.getPose().getX() > 20 || !follower.isBusy()) {
//                outtake.setClipsPutState();
//                clips = true;}
//                if(clips) {
//                    lifts.moveToPosition(LiftsController.HIGH_BAR);
//                    follower.followPath(path3, true);
//                }
//                if(!follower.isBusy()) {
//                    clips = false;
//                    setPathState(4);
//                }
//                break;
//
//            case 4:
//                if(follower.getPose().getX() > 35.2 || !follower.isBusy()) {
//                    outtake.setClipsTakeState();
//                    clips = true;
//                }
//                if(clips) {
//                    lifts.moveToPosition(LiftsController.GROUND);
//                    follower.followPath(path4, true);
//                }
//                if(!follower.isBusy()) {
//                    clips = false;
//                    setPathState(5);
//                }
//                break;
//
//            case 5:
//                break;
//
//            case 6:
//                break;
//
//            case 7:
//                break;
//
//            case 8:
//                break;
//
//            case 9:
//                break;
//        }
//    }
//
//    /** Установка нового состояния автономки */
//    public void setPathState(int pState) {
//        pathState = pState;
//        pathTimer.resetTimer();
//    }
//
//    /** Основной цикл autonomous */
//    @Override
//    public void loop() {
//        follower.update();
//        autonomousPathUpdate();
//
//        telemetry.addData("path state", pathState);
//        telemetry.addData("x", follower.getPose().getX());
//        telemetry.addData("y", follower.getPose().getY());
//        telemetry.addData("heading", follower.getPose().getHeading());
//        telemetry.update();
//    }
//
//    /** Инициализация перед стартом */
//    @Override
//    public void init() {
//        pathTimer = new Timer();
//        opmodeTimer = new Timer();
//        opmodeTimer.resetTimer();
//
//        follower = new Follower(hardwareMap);
//        follower.setStartingPose(startPose);
//
//        buildPaths(); // Генерация путей
//
//        lifts = new LiftsController(this);
//        outtake = new Outtake(hardwareMap);
//    }
//
//    @Override
//    public void start() {
//        opmodeTimer.resetTimer();
//        setPathState(0);
//    }
//
//    @Override
//    public void stop() {
//    }
//}