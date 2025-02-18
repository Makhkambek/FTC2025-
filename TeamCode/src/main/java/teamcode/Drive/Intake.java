package teamcode.Drive;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.telemetry;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

public class Intake {
    // Servo position constants
    public static final double INTAKE_ARM_LEFT_OPEN = 0.6; //checked.   0.5
    public static final double INTAKE_ARM_RIGHT_OPEN = 0.4; //checked.   0.5
    public static final double INTAKE_ROTATE_OPEN = 0.95; // checked.  0.8
    public static final double INTAKE_GRAB_OPEN = 0.30; //checked

    public static final double INTAKE_ARM_LEFT_DEFAULT = 0.7; // сheckde
    public static final double INTAKE_ARM_RIGHT_DEFAULT = 0.3; //checked


    public static final double INTAKE_ARM_LEFT_CLOSED = 1.0; //checked
    public static final double INTAKE_ARM_RIGHT_CLOSED = 0.0; //checked
    public static final double INTAKE_ROTATE_CLOSED = 0.0; //checked
    public static final double INTAKE_GRAB_CLOSED = 0.06; //checked

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
    private IntakeController intakeMotor;
    private LiftsController liftMotors;
    private boolean grabToggled = false;

    // FSM States
    private enum State {
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

    public Intake(HardwareMap hardwareMap, IntakeController intakeMotor, LiftsController liftMotors, Outtake outtake) {
        intakeArmLeft = hardwareMap.get(Servo.class, "intake_arm_left");
        intakeArmRight = hardwareMap.get(Servo.class, "intake_arm_right");
        intakeRotate = hardwareMap.get(Servo.class, "intake_rotate");
        intakeTurn = hardwareMap.get(Servo.class, "intake_turn");
        intakeGrab = hardwareMap.get(Servo.class, "intake_grab");
//        outtake = new Outtake(hardwareMap);
//        liftMotors = new LiftsController(hardwareMap);
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
            case IDLE:
                break;
        }
    }

    private void executeOpen() {
        if (timer.seconds() < 0.3) {
            intakeRotate.setPosition(INTAKE_ROTATE_OPEN);
            intakeTurn.setPosition(INTAKE_TURN_DEFAULT);
            intakeGrab.setPosition(INTAKE_GRAB_OPEN);
            intakeArmLeft.setPosition(INTAKE_ARM_LEFT_DEFAULT);
            intakeArmRight.setPosition(INTAKE_ARM_RIGHT_DEFAULT);
        }
        else {
            currentState = State.IDLE;
            timer.reset();
        }
    }


    private void executeClosed() {
        switch (subState) {
            case 0: //  Открываем руки и закрываем захват
                intakeArmLeft.setPosition(INTAKE_ARM_LEFT_OPEN);
                intakeArmRight.setPosition(INTAKE_ARM_RIGHT_OPEN);
//                intakeGrab.setPosition(INTAKE_GRAB_CLOSED);
                timer.reset();
                subState++;
                break;

            case 1:
                if(timer.seconds() > 0.1) {
                    intakeGrab.setPosition(INTAKE_GRAB_CLOSED);
                    timer.reset();
                    subState++;
                }
                break;
            case 2: //  Ждем 0.3 секунды, затем закрываем остальную часть
                if (timer.seconds() > 0.3) {
                    intakeRotate.setPosition(INTAKE_ROTATE_CLOSED);
                    intakeArmLeft.setPosition(INTAKE_ARM_LEFT_CLOSED);
                    intakeArmRight.setPosition(INTAKE_ARM_RIGHT_CLOSED);
                    intakeTurn.setPosition(INTAKE_TURN_DEFAULT);
                    timer.reset();
                    subState++;
                }
                break;

            case 3: // Шаг 3: Ждем 0.6 секунды и завершаем процесс
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
                intakeMotor.setTarget(IntakeController.ZERO);
                intakeRotate.setPosition(INTAKE_ROTATE_CLOSED);
                timer.reset();
                subState++;
                break;

            case 1:
                if (timer.seconds() > 0.4) {
                    outtake.dropper.setPosition(Outtake.DROPPER_CLOSE);
                    intakeGrab.setPosition(INTAKE_GRAB_OPEN);
                    timer.reset();
                    subState++;
                }
                break;

            case 2:
                if (timer.seconds() > 0.5) {
                    liftMotors.setTarget(LiftsController.HIGHEST_BASKET);
                    outtake.setScoreState();
                    timer.reset();
                    subState++;
                }
                break;

            case 3:
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
        currentState = State.OPEN;
        timer.reset();
    }

    public void setClosedState() {
        isClosedComplete = false; // Обнуляем перед началом закрытия
        currentState = State.CLOSED;
        timer.reset();
    }

    private void setClosedPositions() {
        intakeGrab.setPosition(INTAKE_GRAB_CLOSED);
        intakeArmLeft.setPosition(INTAKE_ARM_LEFT_CLOSED);
        intakeArmRight.setPosition(INTAKE_ARM_RIGHT_CLOSED);
        intakeRotate.setPosition(INTAKE_ROTATE_CLOSED);
        intakeRotate.setPosition(INTAKE_ROTATE_CLOSED);
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

    public void toggleGrab() {
        grabToggled = !grabToggled;
        if (grabToggled) {
            intakeGrab.setPosition(INTAKE_GRAB_OPEN);
        } else {
            intakeGrab.setPosition(INTAKE_GRAB_CLOSED);
        }
    }
}