package SubSystems;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import Controllers.LiftsController;

public class Outtake {
    public static final double ARM_LEFT_GRAB = 0.35; //checked.
    public static final double ARM_RIGHT_GRAB = 0.35; //checked.
    public static final double CLAW_GRAB = 0.9;  //checked //0.1
    public static final double DROPPER_CLOSE = 0.25;
    public static final double OUTTAKE_LIFT_CLOSED = 0.0;

    public static final double ARM_LEFT_SCORE = 0.72; // checked 0.76
    public static final double ARM_RIGHT_SCORE = 0.72; //checked 0.76
    public static final double CLAW_SCORE = 0.1; //0.3
    public static final double DROPPER_OPEN = 0.6;
    public static final double OUTTAKE_LIFT_OPEN = 0.7; //0.75

    public static final double CLAW_CLIPS_TAKE = 0.1; // I HAVE TO CHECK THIS SHIT
    public static final double ARM_LEFT_CLIPS_TAKE = 0.9; //checked.  1.0
    public static final double ARM_RIGHT_CLIPS_TAKE = 0.9; //checked.  0.0

    public static final double ARM_LEFT_CLIPS_PUT = 0.23;
    public static final double ARM_RIGHT_CLIPS_PUT = 0.23;
    public static final double CLAW_CLIPS_PUT = 0.45;

    public static final double ARM_RIGHT_DEFAULT = 0.46;
    public static final double ARM_LEFT_DEFAULT = 0.46;


    // Servo objects
    public final Servo armLeft;
    public final Servo armRight;
    public final Servo claw;
    public Servo dropper;
    public Servo outtake_lift;
    private LiftsController liftMotors;

    // FSM States
    private enum State {
        GRAB,
        DROP,
        SCORE,
        CLIPS_TAKE,
        CLIPS_PUT,
        PRE_LOAD,
        CLIPS_OPEN,
        IDLE
    }

    private State currentState = State.IDLE;
    private ElapsedTime timer = new ElapsedTime();
    public boolean isClipsPutComplete = false;
    public boolean isClipsTakeComplete = false;
    public boolean isScoreComplete = false;
    public boolean isDropComplete = false;
    private int subState = 0;

    public Outtake(HardwareMap hardwareMap, LiftsController liftMotors) {
        armLeft = hardwareMap.get(Servo.class, "arm_left");
        armRight = hardwareMap.get(Servo.class, "arm_right");
        claw = hardwareMap.get(Servo.class, "claw");
        dropper = hardwareMap.get(Servo.class, "dropper");
        outtake_lift = hardwareMap.get(Servo.class, "outtake_lift");
        this.liftMotors = liftMotors;
//        setPreloadPosition();
        setPreloadState();
    }

    // Main FSM logic
    public void update() {
        switch (currentState) {
            case GRAB:
                executeGrab();
                break;
            case DROP:
                executeDrop();
                break;
            case SCORE:
                executeScore();
                break;
            case CLIPS_TAKE:
                executeClipsTake();
                break;
            case CLIPS_PUT:
                executeClipsPut();
                break;
            case PRE_LOAD:
                executePreLoad();
                break;
            case CLIPS_OPEN:
                executeClipsOpen();
                break;
            case IDLE:
                break;
        }
    }

    private void executeDrop() {
        switch (subState) {
            case 0:
                dropper.setPosition(DROPPER_OPEN);
                timer.reset();
                subState++;
                break;

            case 1:
                if(timer.seconds() > 0.1) {
                    armLeft.setPosition(ARM_LEFT_DEFAULT);
                    armRight.setPosition(ARM_RIGHT_DEFAULT);
                    timer.reset();
                    subState++;
                }
                break;

            case 2:
                if (timer.seconds() > 0.2) {
                    subState = 0;
                    setGrabState();
                }
                break;

            case 3:
                if (timer.seconds() > 0.2) {
                    isDropComplete = true;
                    currentState = State.IDLE;
                    subState = 0;
                }
                break;
        }
    }

    private void executeClipsOpen() {
        switch (subState) {
            case 0:
                dropper.setPosition(DROPPER_OPEN);
                armLeft.setPosition(ARM_LEFT_DEFAULT);
                armRight.setPosition(ARM_RIGHT_DEFAULT);
                timer.reset();
                subState++;
                break;

            case 1:
                if (timer.seconds() > 0.2) {
                    isDropComplete = true;
                    currentState = State.IDLE;
                    subState = 0;
                }
                break;
        }
    }


    private void executeGrab() {
        switch (subState) {
            case 0:
                armLeft.setPosition(ARM_LEFT_GRAB);
                armRight.setPosition(ARM_RIGHT_GRAB);
                claw.setPosition(CLAW_GRAB);
                dropper.setPosition(DROPPER_OPEN);
                outtake_lift.setPosition(OUTTAKE_LIFT_CLOSED);
//                liftMotors.setTarget(LiftsController.GROUND);
                timer.reset();
                subState++;
                break;

            case 1:
                if (timer.seconds() > 0.2) {
                    currentState = State.IDLE;
                    subState = 0;
                }
                break;
        }
    }

    private void executeScore() {
        switch (subState) {
            case 0:
                if(timer.seconds() > 0.1) {
                    armLeft.setPosition(ARM_LEFT_SCORE);
                    armRight.setPosition(ARM_RIGHT_SCORE);
                    claw.setPosition(CLAW_SCORE);
                    dropper.setPosition(DROPPER_CLOSE);
                    outtake_lift.setPosition(OUTTAKE_LIFT_OPEN);
                    timer.reset();
                    subState++;
                }
                break;

            case 1:
                if (timer.seconds() > 0.2) {
                    isScoreComplete = true;
                    currentState = State.IDLE;
                    subState = 0;
                }
                break;
        }
    }

    private void executeClipsTake() {
        switch (subState) {
            case 0:
                dropper.setPosition(DROPPER_OPEN);
                outtake_lift.setPosition(0.25);
                timer.reset();
                subState++;
                break;

            case 1:
                if (timer.seconds() > 0.2) {
                    armLeft.setPosition(ARM_LEFT_CLIPS_TAKE);
                    armRight.setPosition(ARM_RIGHT_CLIPS_TAKE);
                    liftMotors.setTarget(LiftsController.GROUND);
                    claw.setPosition(CLAW_CLIPS_TAKE);
                    timer.reset();
                    subState++;
                }
                break;

            case 2:
                if (timer.seconds() > 0.2) {
                    isClipsTakeComplete = true;
                    subState = 0;
                    currentState = State.IDLE;
                }
                break;
        }
    }

    private void executeClipsPut() {
        switch (subState) {
            case 0:
                dropper.setPosition(DROPPER_CLOSE);
                timer.reset();
                subState++;
                break;

            case 1:
                if(timer.seconds() > 0.2) {
                    liftMotors.setTarget(LiftsController.HIGH_BAR);
                    timer.reset();
                    subState++;
            }
                break;
            case 2:
                if (timer.seconds() > 0.1) {
//                    outtake_lift.setPosition(OUTTAKE_LIFT_OPEN);
                    claw.setPosition(CLAW_CLIPS_PUT);
                    armLeft.setPosition(ARM_LEFT_CLIPS_PUT);
                    armRight.setPosition(ARM_RIGHT_CLIPS_PUT);
                    timer.reset();
                    subState++;
                }
                break;


            case 3:
                if (timer.seconds() > 0.2 && Math.abs(liftMotors.getCurrentPosition() - LiftsController.HIGH_BAR) < 50) {
                    isClipsPutComplete = true;
                    subState = 0;
                    currentState = State.IDLE;
                }
                break;
        }
    }

    private void executePreLoad() {
        switch (subState) {
            case 0:
                outtake_lift.setPosition(OUTTAKE_LIFT_CLOSED);
                armLeft.setPosition(ARM_LEFT_DEFAULT);
                armRight.setPosition(ARM_RIGHT_DEFAULT);
                claw.setPosition(CLAW_GRAB);
                dropper.setPosition(DROPPER_CLOSE);
                timer.reset();
                subState++;
                break;

            case 1:
                if (timer.seconds() > 0.5) {
                    currentState = State.IDLE;
                    subState = 0;
                }
                break;
        }
    }


    public void setDrop() {
        currentState = State.DROP;
        timer.reset();
        isDropComplete = false;
    }

    public void setGrabState() {
        currentState = State.GRAB;
        subState = 0;
        timer.reset();
    }

    public void setClipsOpen() {
        currentState = State.CLIPS_OPEN;
        subState = 0;
        timer.reset();
    }
    public void setPreloadState() {
        currentState = State.PRE_LOAD;
        timer.reset();
    }

    public void setScoreState() {
        currentState = State.SCORE;
        timer.reset();
        isScoreComplete = false;
    }

    public void setClipsTakeState() {
        currentState = State.CLIPS_TAKE;
        isClipsTakeComplete = false;
        subState = 0;
        timer.reset();
    }

    public void setClipsPutState() {
        currentState = State.CLIPS_PUT;
        isClipsPutComplete = false;
        subState = 0;
        timer.reset();
    }

    public void setPreloadPosition() {
        armLeft.setPosition(0.25);
        armRight.setPosition(0.25);
        claw.setPosition(0.3);
        dropper.setPosition(DROPPER_CLOSE);
    }
}