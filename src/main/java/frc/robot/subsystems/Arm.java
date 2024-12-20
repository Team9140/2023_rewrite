package frc.robot.subsystems;

import com.revrobotics.CANSparkLowLevel;
import com.revrobotics.CANSparkMax;
import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.Robot;

public class Arm extends SubsystemBase {
    private static Arm instance;
    private CANSparkMax motor;
    private TrapezoidProfile.State currentState;
    private TrapezoidProfile.State finalState;
    private final TrapezoidProfile.Constraints constraints = new TrapezoidProfile.Constraints(Math.PI * 3.0, Math.PI * 1.50);
    private ArmFeedforward feedforward = new ArmFeedforward(Constants.Arm.kS, Constants.Arm.kG, Constants.Arm.kV,
            Constants.Arm.kA);
    private Arm() {
        this.motor = new CANSparkMax(10, CANSparkLowLevel.MotorType.kBrushless);
        this.motor.getEncoder().setPositionConversionFactor(1/100.0 * 2 * Math.PI);
        motor.setInverted(false);
        motor.setIdleMode(CANSparkMax.IdleMode.kBrake);
        this.motor.getPIDController().setP(Constants.Arm.P);
        this.motor.getPIDController().setD(Constants.Arm.D);
        this.motor.setSmartCurrentLimit(Constants.Arm.CURRENT_LIMIT);
        this.currentState = new TrapezoidProfile.State(0, 0.0);
        finalState = new TrapezoidProfile.State(0,0);

    }
    public static Arm getInstance() {
        if (instance == null) {
            instance = new Arm();
        }
        return instance;
    }

    public Command setArmPosition(double position) {
        return this.runOnce(() -> this.finalState = new TrapezoidProfile.State( position, 0.0))
                   .andThen(this.run(() -> {
                       double FF = feedforward.calculate(currentState.position, currentState.velocity);
                       TrapezoidProfile profile = new TrapezoidProfile(constraints);
                       currentState = profile.calculate(Robot.kDefaultPeriod, currentState, finalState);
                       motor.getPIDController().setReference(currentState.position, CANSparkMax.ControlType.kPosition, 0, FF);
                   }));
    };

    @Override
    public void periodic(){
        SmartDashboard.putNumber("Arm Final Target (radians)", this.finalState.position);
        SmartDashboard.putNumber("Arm Current State (radians)", this.currentState.position);
        SmartDashboard.putNumber("Arm Current State Voltage", this.currentState.velocity);
        //SmartDashboard.putNumber("Arm current pos", this.);


    }

}