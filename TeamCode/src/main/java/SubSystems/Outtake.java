package SubSystems;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

public class Outtake {
    // Servo position constants
    public static final double ARM_LEFT_GRAB = 0.15; //checked.  0.16
    public static final double ARM_RIGHT_GRAB = 0.85; //checked.   0.84
    public static final double CLAW_GRAB = 0.0;  //checked //0.1
    public static final double DROPPER_CLOSE = 0.0;

    public static final double ARM_LEFT_DEFAULT = 0;
    public static final double ARM_RIGHT_DEFAULT = 0;

    public static final double ARM_LEFT_SCORE = 0.7; // checked 0.6
    public static final double ARM_RIGHT_SCORE = 0.3; //checked 0.4
    public static final double CLAW_SCORE = 0.55; //checked
    public static final double DROPPER_OPEN = 0.3; //checked

    public static final double ARM_LEFT_CLIPS = 1.0; //checked.  1.0
    public static final double ARM_RIGHT_CLIPS = 0.0; //checked.  0.0

    // Servo objects
    private final Servo armLeft;
    private final Servo armRight;
    public final Servo claw;
    public Servo dropper;
    private LiftsController liftMotors;


    // FSM States
    private enum State {
        GRAB,
        DROP,
        SCORE,
        CLIPS_TAKE,
        CLIPS_PUT,
        PRE_LOAD,
        DEFAULT,
        IDLE
    }

    private State currentState = State.IDLE;
    private ElapsedTime timer = new ElapsedTime();
    public boolean isClipsPutComplete = false;
    public boolean isClipsTakeComplete = false;
    public boolean isScoreComplete = false;
    public boolean isDropComplete = false;
    private int subState = 0;

    public Outtake(HardwareMap hardwareMap) {
        armLeft = hardwareMap.get(Servo.class, "arm_left");
        armRight = hardwareMap.get(Servo.class, "arm_right");
        claw = hardwareMap.get(Servo.class, "claw");
        dropper = hardwareMap.get(Servo.class, "dropper");
        liftMotors = new LiftsController(hardwareMap);

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
            case DEFAULT:
                executeDefault();
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
                if (timer.seconds() > 0.3) {
                    subState = 0;
                    setDefault();
                }
                break;

            case 2:
                if (timer.seconds() > 0.5) {
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

    private void executeDefault() {
        switch (subState) {
            case 0:
                armLeft.setPosition(ARM_LEFT_DEFAULT);
                armRight.setPosition(ARM_RIGHT_DEFAULT);
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

    private void executeScore() {
        switch (subState) {
            case 0:
                armLeft.setPosition(ARM_LEFT_SCORE);
                armRight.setPosition(ARM_RIGHT_SCORE);
                claw.setPosition(CLAW_SCORE);
                dropper.setPosition(DROPPER_CLOSE);
                timer.reset();
                subState++;
                break;

            case 1:
                if (timer.seconds() > 0.5) {
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
                timer.reset();
                subState++;
                break;

            case 1:
                if (timer.seconds() > 0.3) {
                    liftMotors.setTarget(LiftsController.GROUND);
                    armLeft.setPosition(ARM_LEFT_CLIPS);
                    armRight.setPosition(ARM_RIGHT_CLIPS);
                    claw.setPosition(0.7);
                    timer.reset();
                    subState++;
                }
                break;

            case 2:
                if (timer.seconds() > 0.5) {
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
                if (timer.seconds() > 0.3) {
                    claw.setPosition(0.3);
                    armLeft.setPosition(0.25);
                    armRight.setPosition(0.75);
                    timer.reset();
                    subState++;
                }
                break;


            case 2:
                if (timer.seconds() > 0.6) {
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
                armLeft.setPosition(ARM_LEFT_GRAB);
                armRight.setPosition(ARM_RIGHT_GRAB);
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
        timer.reset();
    }

    public void setDefault() {
        currentState = State.DEFAULT;
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
//        timer.reset();
    }

    public void setClipsPutState() {
        currentState = State.CLIPS_PUT;
        isClipsPutComplete = false;
//        timer.reset();
    }

}