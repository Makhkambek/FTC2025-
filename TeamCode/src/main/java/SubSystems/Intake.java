package SubSystems;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import Controllers.ExtendoController;
import Controllers.LiftsController;

public class Intake {
    // Servo position constants
    public static final double INTAKE_ARM_LEFT_OPEN = 0.23; //checked.   0.5
    public static final double INTAKE_ARM_RIGHT_OPEN = 0.23; //checked.   0.5
    public static final double INTAKE_ROTATE_OPEN = 0.13; // checked.  0.67
    public static final double INTAKE_GRAB_OPEN = 0.5; //checked //0.23

    public static final double INTAKE_ARM_LEFT_DEFAULT = 0.47; // checked
    public static final double INTAKE_ARM_RIGHT_DEFAULT = 0.47; //checked

    public static final double INTAKE_ARM_LEFT_CLOSED = 0.47; //checked
    public static final double INTAKE_ARM_RIGHT_CLOSED = 0.47; //checked
    public static final double INTAKE_ROTATE_CLOSED = 0.8; //checked  0.83
    public static final double INTAKE_GRAB_CLOSED = 0.2 //0.21
            ; //checked 0.06

    public static final double INTAKE_TURN_POSITION_1 = 0;  // checked // вправо
    public static final double INTAKE_TURN_POSITION_2 = 0.35;  // checked // влево
    public static final double INTAKE_TURN_DEFAULT = 0.49;     // checked
    public static final double INTAKE_TURN_POSITION_3 = 0.85;  // checked // влево
    public static final double INTAKE_TURN_POSITION_4 = 0.65;

    // Servo objects
    public final Servo intakeArmLeft;
    public final Servo intakeArmRight;
    public final Servo intakeRotate;
    public Servo intakeTurn;
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

        setClosedPositions(); // Изначально закрыто
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
                intakeTurn.setPosition(INTAKE_TURN_DEFAULT);
                intakeArmLeft.setPosition(INTAKE_ARM_LEFT_DEFAULT);
                intakeArmRight.setPosition(INTAKE_ARM_RIGHT_DEFAULT);
                timer.reset();
                subState++;
                break;

            case 1:
                if(timer.seconds() > 0.2) {
                    intakeRotate.setPosition(INTAKE_ROTATE_OPEN);
                    intakeGrab.setPosition(INTAKE_GRAB_OPEN);
                    timer.reset();
                    subState++;
                }
                break;

            case 2:
                if (timer.seconds() > 0.15) {
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
                intakeGrab.setPosition(INTAKE_GRAB_OPEN);
//                intakeArmLeft.setPosition(INTAKE_ARM_LEFT_OPEN);
//                intakeArmRight.setPosition(INTAKE_ARM_RIGHT_OPEN);
                intakeRotate.setPosition(0.13);
                timer.reset();
                subState++;
                break;

            case 1:
                if(timer.seconds() > 0.15) {
                    intakeArmLeft.setPosition(INTAKE_ARM_LEFT_OPEN);
                    intakeArmRight.setPosition(INTAKE_ARM_RIGHT_OPEN);
                    timer.reset();
                    subState++;
                }
                break;

            case 2:
                if (timer.seconds() > 0.18) {
                    intakeGrab.setPosition(INTAKE_GRAB_CLOSED);
                    timer.reset();
                    subState++;
                }
                break;
            case 3:
                if (timer.seconds() > 0.2) {
                    intakeArmLeft.setPosition(INTAKE_ARM_LEFT_DEFAULT);
                    intakeArmRight.setPosition(INTAKE_ARM_RIGHT_DEFAULT);
                    timer.reset();
                    subState++;
                }
                break;

            case 4:
                if (timer.seconds() > 0.1) {
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
                intakeRotate.setPosition(INTAKE_ROTATE_CLOSED);
                intakeMotor.setTarget(ExtendoController.MINUS_ZERO);
                timer.reset();
                subState++;


                break;

            case 1:
                if (timer.seconds() > 0.05) {
                    intakeTurn.setPosition(INTAKE_TURN_DEFAULT);
                    intakeArmLeft.setPosition(INTAKE_ARM_LEFT_CLOSED);
                    intakeArmRight.setPosition(INTAKE_ARM_RIGHT_CLOSED);
                    outtake.outtake_lift.setPosition(0.12);
                    outtake.claw.setPosition(0.97);
                    timer.reset();
                    subState++;
                }
                break;

            case 2:
                if (timer.seconds() > 0.2) {
                    outtake.armLeft.setPosition(0.34); //0.358
                    outtake.armRight.setPosition(0.34); //0.9
                    timer.reset();
                    subState++;
                }
                break;

            case 3:
                if (timer.seconds() > 0.35) {  //0.25
                    outtake.dropper.setPosition(Outtake.DROPPER_CLOSE);
                    timer.reset();
                    subState++;
                }
                break;
            case 4:
                if (timer.seconds() > 0.1) {  //0.5
                    intakeGrab.setPosition(INTAKE_GRAB_OPEN);
                    timer.reset();
                    subState++;
                }
            case 5:
                if (timer.seconds() > 0.3) {
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

            case 6:
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
        subState = 0; // Явно сбрасываем subState для корректного перехода
    }

    public void setClosedState() {
        isClosedComplete = false; // Обнуляем перед началом закрытия
        currentState = State.CLOSED;
        timer.reset();
    }

    public void setClosedPositions() {
        intakeGrab.setPosition(INTAKE_GRAB_CLOSED);
        intakeArmLeft.setPosition(INTAKE_ARM_LEFT_DEFAULT);
        intakeArmRight.setPosition(INTAKE_ARM_RIGHT_DEFAULT);
        intakeRotate.setPosition(0.4);
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