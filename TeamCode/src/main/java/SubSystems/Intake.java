package SubSystems;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

public class Intake {
    // Servo position constants
    public static final double INTAKE_ARM_LEFT_OPEN = 0.3; //checked.   0.5
    public static final double INTAKE_ARM_RIGHT_OPEN = 0.7; //checked.   0.5
    public static final double INTAKE_ROTATE_OPEN = 1.0; // checked.  0.67
    public static final double INTAKE_GRAB_OPEN = 0.30; //checked //0.30

    public static final double INTAKE_ARM_LEFT_DEFAULT = 0.53; // checked
    public static final double INTAKE_ARM_RIGHT_DEFAULT = 0.47; //checked

    public static final double INTAKE_ARM_LEFT_CLOSED = 0.67; //checked
    public static final double INTAKE_ARM_RIGHT_CLOSED = 0.33; //checked
    public static final double INTAKE_ROTATE_CLOSED = 0.58; //checked  1.0
    public static final double INTAKE_GRAB_CLOSED = 0.05; //checked 0.06

    public static final double INTAKE_TURN_POSITION_1 = 0;  // checked // вправо
    public static final double INTAKE_TURN_POSITION_2 = 0.8;  // checked // влево
    public static final double INTAKE_TURN_DEFAULT = 0.41;     // checked
    public static final double INTAKE_TURN_POSITION_3 = 0.2;  // checked // влево
    public static final double INTAKE_TURN_POSITION_4 = 0.6;

    // Servo objects
    public final Servo intakeArmLeft;
    public final Servo intakeArmRight;
    public final Servo intakeRotate;
    public final Servo intakeTurn;
    public Servo intakeGrab;
    private Outtake outtake;
    private ExtendoController intakeMotor;
    private LiftsController liftMotors;
    private boolean grabToggled = false;

    // FSM States
    public enum State {
        OPEN,
        CLOSED,
        TRANSFERRING,
        IDLE
    }

    private State currentState = State.IDLE;
    private ElapsedTime timer = new ElapsedTime();
    private int subState = 0;
    public boolean isClosedComplete = false;
    public boolean isTransferComplete = false;
    public boolean isOpenComplete = false;

    public Intake(HardwareMap hardwareMap, ExtendoController intakeMotor, LiftsController liftMotors, Outtake outtake) {
        intakeArmLeft = hardwareMap.get(Servo.class, "intake_arm_left");
        intakeArmRight = hardwareMap.get(Servo.class, "intake_arm_right");
        intakeRotate = hardwareMap.get(Servo.class, "intake_rotate");
        intakeTurn = hardwareMap.get(Servo.class, "intake_turn");
        intakeGrab = hardwareMap.get(Servo.class, "intake_grab");
        this.liftMotors = liftMotors;
        this.outtake = outtake;
        this.intakeMotor = intakeMotor;

        setClosedPositions(); 
    }

    public void update() {
        switch (currentState) {
            case OPEN:
                executeOpen();
                break;
            case CLOSED:
                executeClosed();
                break;
            case TRANSFERRING:
                executeTransfer();
                break;
            case IDLE:
                break;
        }
    }

    private void executeOpen() {
        switch (subState) {
            case 0:
                intakeRotate.setPosition(INTAKE_ROTATE_OPEN);
                intakeTurn.setPosition(INTAKE_TURN_DEFAULT);
                intakeGrab.setPosition(INTAKE_GRAB_OPEN);
                intakeArmLeft.setPosition(INTAKE_ARM_LEFT_DEFAULT);
                intakeArmRight.setPosition(INTAKE_ARM_RIGHT_DEFAULT);
                timer.reset();
                subState++;
                break;

            case 1:
                if (timer.seconds() > 0.3) { // Используем 
                    currentState = State.IDLE;
                    isOpenComplete = true;
                    subState = 0;
                }
                break;
        }
    }

    private void executeClosed() {
        switch (subState) {
            case 0: 
                intakeArmLeft.setPosition(INTAKE_ARM_LEFT_OPEN);
                intakeArmRight.setPosition(INTAKE_ARM_RIGHT_OPEN);
                // intakeGrab.setPosition(INTAKE_GRAB_CLOSED);
                timer.reset();
                subState++;
                break;

            case 1:
                if (timer.seconds() > 0.2) {
                    intakeGrab.setPosition(INTAKE_GRAB_CLOSED);
                    timer.reset();
                    subState++;
                }
                break;
            case 2:
                if (timer.seconds() > 0.3) {
                    intakeArmLeft.setPosition(INTAKE_ARM_LEFT_DEFAULT);
                    intakeArmRight.setPosition(INTAKE_ARM_RIGHT_DEFAULT);
                    intakeTurn.setPosition(INTAKE_TURN_DEFAULT);
                    timer.reset();
                    subState++;
                }
                break;

            case 3:
                if (timer.seconds() > 0.3) {
                    currentState = State.IDLE;
                    isClosedComplete = true;
                    subState = 0;
                }
                break;
        }
    }

    private void executeTransfer() {
        switch (subState) {
            case 0:
                intakeMotor.setTarget(ExtendoController.MINUS_ZERO);
                timer.reset();
                subState++;
                break;

            case 1:
                if (timer.seconds() > 0.2) {
                    intakeRotate.setPosition(INTAKE_ROTATE_CLOSED);
                    intakeArmLeft.setPosition(INTAKE_ARM_LEFT_CLOSED);
                    intakeArmRight.setPosition(INTAKE_ARM_RIGHT_CLOSED);
                    timer.reset();
                    subState++;
                }
                break;

            case 2:
                if (timer.seconds() > 0.2) {
                    outtake.armLeft.setPosition(0.13); //0.1
                    outtake.armRight.setPosition(0.87); //0.9
                    timer.reset();
                    subState++;
                }
                break;

            case 3:
                if (timer.seconds() > 0.3) {  //0.5
                    outtake.dropper.setPosition(Outtake.DROPPER_CLOSE);
                    intakeGrab.setPosition(INTAKE_GRAB_OPEN);
                    timer.reset();
                    subState++;
                }
                break;

            case 4:
                if (timer.seconds() > 0.5) {
                    liftMotors.setTarget(LiftsController.HIGHEST_BASKET);
                    intakeMotor.setTarget(ExtendoController.ZERO);
                    intakeRotate.setPosition(INTAKE_ROTATE_OPEN);
                    intakeArmLeft.setPosition(INTAKE_ARM_LEFT_DEFAULT);
                    intakeArmRight.setPosition(INTAKE_ARM_RIGHT_DEFAULT);
                    outtake.setScoreState();
                    timer.reset();
                    subState++;
                }
                break;

            case 5:
                if (timer.seconds() > 0.2) {
                    currentState = State.IDLE;
                    isTransferComplete = true;
                    subState = 0;
                }
                break;
        }
    }

    public void setTransfer() {
        isTransferComplete = false;
        currentState = State.TRANSFERRING;
        subState = 0;
        timer.reset();
    }

    public void setOpenState() {
        isOpenComplete = false;
        currentState = State.OPEN;
        timer.reset();
        subState = 0; 
    }

    public void setClosedState() {
        isClosedComplete = false; 
        currentState = State.CLOSED;
        timer.reset();
    }

    private void setClosedPositions() {
        intakeGrab.setPosition(INTAKE_GRAB_CLOSED);
        intakeArmLeft.setPosition(INTAKE_ARM_LEFT_CLOSED);
        intakeArmRight.setPosition(INTAKE_ARM_RIGHT_CLOSED);
    }

    // Управление intakeTurn
    public void setTurnPosition1() {
        intakeTurn.setPosition(INTAKE_TURN_POSITION_1);
    }

    public void setTurnPosition2() {
        intakeTurn.setPosition(INTAKE_TURN_POSITION_2);
    }

    public void setTurnDefault() {
        intakeTurn.setPosition(INTAKE_TURN_DEFAULT);
    }

    public void setTurnPosition3() {
        intakeTurn.setPosition(INTAKE_TURN_POSITION_3);
    }

    public void setTurnPosition4() {
        intakeTurn.setPosition(INTAKE_TURN_POSITION_4);
    }

    public State getCurrentState() {
        return currentState;
    }
}
