package teamcode.Drive;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

public class Outtake {
    // Servo position constants
    public static final double ARM_LEFT_GRAB = 0.2; //checked
    public static final double ARM_RIGHT_GRAB = 0.8; //checked
    public static final double CLAW_GRAB = 0.1;  //checked
    public static final double DROPPER_CLOSE = 0.1;

    public static final double ARM_LEFT_SCORE = 0.6; // checked
    public static final double ARM_RIGHT_SCORE = 0.4; //checked
    public static final double CLAW_SCORE = 0.8; //checked
    public static final double DROPPER_OPEN = 0.3;

    public static final double CLAW_CLIPS = 0.4; // I HAVE TO CHECK THIS SHIT
    public static final double ARM_LEFT_CLIPS = 1.0; //checked
    public static final double ARM_RIGHT_CLIPS = 0.0; //checked

    // Servo objects
    private final Servo armLeft;
    private final Servo armRight;
    public final Servo claw;
    public Servo dropper;

    // FSM States
    private enum State {
        GRAB,
        SCORE,
        CLIPS_TAKE,
        CLIPS_PUT,
        IDLE
    }

    private State currentState = State.IDLE;
    private ElapsedTime timer = new ElapsedTime();
    public boolean isClipsPutComplete = false;
    public boolean isClipsTakeComplete = false;
    private int subState = 0;

    public Outtake(HardwareMap hardwareMap) {
        armLeft = hardwareMap.get(Servo.class, "arm_left");
        armRight = hardwareMap.get(Servo.class, "arm_right");
        claw = hardwareMap.get(Servo.class, "claw");
        dropper = hardwareMap.get(Servo.class, "dropper");

        setGrabPositions();
    }

    // Main FSM logic
    public void update() {
        switch (currentState) {
            case GRAB:
                executeGrab();
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
            case IDLE:
                break;
        }
    }

    private void executeGrab() {
        switch (subState) {
            case 0:
                armLeft.setPosition(ARM_LEFT_GRAB);
                armRight.setPosition(ARM_RIGHT_GRAB);
                timer.reset();
                subState++;
                break;

            case 1:
                if (timer.seconds() > 0.3) {
                    claw.setPosition(CLAW_GRAB);
                    dropper.setPosition(DROPPER_OPEN);
                    timer.reset();
                    subState++;
                }
                break;

            case 2:
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
                timer.reset();
                subState++;
                break;

            case 1:
                if (timer.seconds() > 0.5) {
                    claw.setPosition(CLAW_SCORE);
                    timer.reset();
                    subState++;
                }
                break;

            case 2:
                if (timer.seconds() > 0.8) {
                    currentState = State.IDLE;
                    subState = 0;
                }
                break;
        }
    }

    private void executeClipsTake() {
        switch (subState) {
            case 0:
                armLeft.setPosition(ARM_LEFT_CLIPS);
                armRight.setPosition(ARM_RIGHT_CLIPS);
                timer.reset();
                subState++;
                break;

            case 1:
                if (timer.seconds() > 0.5) {
                    dropper.setPosition(DROPPER_OPEN);
                    claw.setPosition(0.7);
                    timer.reset();
                    subState++;
                }
                break;

            case 2:
                if (timer.seconds() > 0.8) {
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
                    claw.setPosition(CLAW_CLIPS);
                    armLeft.setPosition(ARM_LEFT_GRAB);
                    armRight.setPosition(ARM_RIGHT_GRAB);
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

    public void setGrabState() {
        currentState = State.GRAB;
        timer.reset();
    }

    public void setScoreState() {
        currentState = State.SCORE;
        timer.reset();
    }

    public void setClipsTakeState() {
        currentState = State.CLIPS_TAKE;
        isClipsTakeComplete = false;
        timer.reset();
    }

    public void setClipsPutState() {
        currentState = State.CLIPS_PUT;
        isClipsPutComplete = false;
        timer.reset();
    }

    private void setGrabPositions() {
        armLeft.setPosition(ARM_LEFT_GRAB);
        armRight.setPosition(ARM_RIGHT_GRAB);
        claw.setPosition(CLAW_GRAB);
        dropper.setPosition(DROPPER_CLOSE);
    }
}