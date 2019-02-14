package com.team2898.robot.subsystem

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.team254.lib.util.motion.*
import com.team2898.engine.async.AsyncLooper
import com.team2898.engine.math.linear.Matrix
import com.team2898.engine.math.linear.T
import com.team2898.engine.math.linear.get
import com.team2898.engine.math.linear.row
import com.team2898.engine.motion.TalonWrapper
import com.team2898.engine.subsystems.SingleJointedArmLQR
import com.team2898.robot.config.ARM_LEFT_MASTER
import com.team2898.robot.config.ARM_LEFT_SLAVE
import com.team2898.robot.config.ARM_RIGHT_MASTER
import com.team2898.robot.config.ARM_RIGHT_SLAVE
import com.team2898.robot.config.ArmConf.ARM_KF
import com.team2898.robot.config.ArmConf.ARM_OFFSET
import edu.wpi.first.wpilibj.Encoder
import edu.wpi.first.wpilibj.Talon
import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import kotlin.math.PI
import kotlin.math.cos

object Arm : SingleJointedArmLQR() {
    val armEnc = Encoder(4, 5)
    var prevDist = 0.0
    var prevTime = 0.0
    var prevVel = 0.0
    var acc = 0.0
    val constrains = MotionProfileConstraints(3.0, 1.0)

    val leftMaster = TalonWrapper(ARM_LEFT_MASTER)
    val leftSlace = TalonWrapper(ARM_LEFT_SLAVE)
    val rightMaster = TalonWrapper(ARM_RIGHT_MASTER)
    val rightSlave = TalonWrapper(ARM_RIGHT_SLAVE)

    var brake: Boolean = false
//        get() = Timer.getFPGATimestamp() >= profile.endTime()

    var target: Double = 0.0


    val currentPos: MotionState
        get() = MotionState(
                Timer.getFPGATimestamp(),
                x[0, 0],
                x[1, 0],
                acc
        )


    var profile: MotionProfile = MotionProfileGenerator.generateProfile(constrains, MotionProfileGoal(currentPos.pos()), currentPos)
        set(value) {
            brake = false
            field = value
        }

    init {

        armEnc.distancePerPulse = ((22 / 60.0) * 2 * PI) / 600.0 // rad

//        leftSlace slaveTo leftMaster
//        rightMaster slaveTo leftMaster
//        rightSlave slaveTo leftMaster

        listOf(leftMaster, leftSlace, rightSlave, rightMaster).forEach {
            it.apply {
                configVoltageCompSaturation(12.0)
            }
        }

        AsyncLooper(50.0) {
            val armVel = (armEnc.distance - prevDist) / (Timer.getFPGATimestamp() - prevTime)
            acc = (armVel - prevVel) / (Timer.getFPGATimestamp() - prevTime)
            prevTime = Timer.getFPGATimestamp()
            prevDist = armEnc.distance
            x = Matrix(arrayOf(row(armEnc.distance, armVel))).T
            SmartDashboard.putNumber("arm pos", x[0, 0] + ARM_OFFSET)
            SmartDashboard.putNumber("arm vel", x[1, 0])
            SmartDashboard.putNumber("arm raw", armEnc.get().toDouble())
            SmartDashboard.putNumber("arm dist raw", armEnc.distance)
            SmartDashboard.putNumber("lm talon output", leftMaster.outputCurrent)
            SmartDashboard.putNumber("ls talon output", leftSlace.outputCurrent)
            SmartDashboard.putNumber("rm talon output", rightMaster.outputCurrent)
            SmartDashboard.putNumber("rs talon output", rightSlave.outputCurrent)
            SmartDashboard.putNumber("Kf", -ARM_KF * cos(x[0, 0] + ARM_OFFSET))
        }.start()


        // Control Loop
        AsyncLooper(50.0) {
            val currentTime = Timer.getFPGATimestamp()
            val state = profile.stateByTimeClamped(currentTime)
            val r = Matrix(Matrix(arrayOf(row(state.pos(), state.vel()))).T.data)
            val voltage = Arm.genU(r)[0, 0]
            val outPutVoltage = voltage + ARM_KF * cos(x[0, 0] + ARM_OFFSET)
            if (!brake) talons(clampU(-outPutVoltage / 12))
            else talons(0.0)
            println("$currentTime, $target, ${x[0, 0]}, ${x[1, 0]}, ${xHat[0, 0]}, ${xHat[1, 0]}, $voltage")
        }
    }

    fun talons(value: Double) {
        listOf(leftMaster, leftSlace, rightMaster, rightSlave).forEach {
            it.apply {
                set(ControlMode.PercentOutput, value)
            }
        }
    }


    fun updateTarget(targetPos: Double) {
        target = targetPos
        profile = MotionProfileGenerator.generateProfile(constrains, MotionProfileGoal(targetPos - ARM_OFFSET), currentPos)
    }

    fun resetEncoder() {
        armEnc.reset()
    }
}