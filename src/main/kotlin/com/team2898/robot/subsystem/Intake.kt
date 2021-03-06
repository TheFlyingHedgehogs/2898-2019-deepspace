package com.team2898.robot.subsystem

import com.ctre.phoenix.motorcontrol.ControlMode
import com.team2898.engine.async.AsyncLooper
import com.team2898.engine.logic.GamePeriods
import com.team2898.engine.logic.Subsystem
import com.team2898.engine.motion.TalonWrapper
import com.team2898.robot.OI
import com.team2898.robot.config.CARGOINTAKE
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj.DoubleSolenoid
import edu.wpi.first.wpilibj.PneumaticsModuleType
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import kotlin.math.sign


object Intake: Subsystem(50.0, "intake") {
    override val enableTimes: List<GamePeriods> = listOf(GamePeriods.TELEOP, GamePeriods.AUTO)

    private val hatchSelenoid = DoubleSolenoid(0, PneumaticsModuleType.CTREPCM, 1, 0)
    private val latching = DoubleSolenoid(0, PneumaticsModuleType.CTREPCM, 5, 6)
    private val shooter = DoubleSolenoid(1, PneumaticsModuleType.CTREPCM, 1, 0)
    private val cargoTalon = TalonWrapper(CARGOINTAKE)

    var one = false
    var two = false
    var three = false
    var four = false
    var fifth = false
    var six = false
    var autoIntake = false
    var isHatchAuto = false

    val hatchLimit = DigitalInput(7)
    init {
        AsyncLooper(50.0) {
            SmartDashboard.putNumber("cargo A", cargoTalon.outputCurrent)
        }.start()

        AsyncLooper(50.0) {
            if (six) reset()
            if (fifth) six = fifth
            if (four) fifth = four
            if (three) four = three
            if (two) three = two
            if (one) two = one

            val isHatchIn = !hatchLimit.get()

            var shooterState = OI.opCtl.getRawButton(1)
            var hatchState = (OI.opCtl.getRawButton(2) || OI.opCtl.getRawButton(1)) || isHatchAuto
            var latchState = (OI.opCtl.getRawAxis(1) < 0.5 && OI.opCtl.getRawAxis(1) > -0.5)
            var cargoValue = if (latchState) 0.0 else sign(OI.opCtl.getRawAxis(1)) * -0.8

            if (isHatchIn && isHatchAuto) {
                isHatchAuto = false
            }
            if (isHatchAuto) hatchState = true


            if (OI.opCtl.getRawButton(4) || autoIntake) {
                autoIntake = true
                latchState = false
                cargoValue = -0.7
            }

            if ((cargoTalon.outputCurrent > 20.0 && autoIntake) || OI.opCtl.getRawButton(6)) {
                one = true
                autoIntake = false
                latchState = true
            }

            if (six) cargoValue = 0.0


            updateShooter(shooterState)
            updateHatch(hatchState)
            updateLatch(latchState)
            updateCargoTalon(cargoValue)
        }.start()
    }

    fun updateShooter(state: Boolean) {
        if (state) shooter.set(DoubleSolenoid.Value.kReverse)
        else shooter.set(DoubleSolenoid.Value.kForward)
    }

    fun updateHatch(state: Boolean) {
        if (state) hatchSelenoid.set(DoubleSolenoid.Value.kReverse)
        else hatchSelenoid.set(DoubleSolenoid.Value.kForward)
    }

    fun updateLatch(state: Boolean) {
        if (state) latching.set(DoubleSolenoid.Value.kReverse)
        else latching.set(DoubleSolenoid.Value.kForward)
    }

    fun updateCargoTalon(value: Double) {
        cargoTalon.set(ControlMode.PercentOutput, value)
    }

    fun reset() {
        autoIntake = false
        one = false
        two = false
        three = false
        four = false
        fifth = false
        six = false
    }
}

//            if ((OI.opCtl.getRawAxis(1) < 0.5 && OI.opCtl.getRawAxis(1) > -0.5)) {
//                latching.set(DoubleSolenoid.Value.kReverse)
//            } else {
//                latching.set(DoubleSolenoid.Value.kForward)
//            }

//            if (OI.opCtl.getRawAxis(1) < 0.6 && OI.opCtl.getRawAxis(1) > -0.5) {
//                cargoTalon.set(ControlMode.PercentOutput, 0.0)
//            } else {
//                cargoTalon.set(ControlMode.PercentOutput, -0.5 * OI.opCtl.getRawAxis(1))
//            }
//            if (OI.opCtl.getRawButton(3) || OI.opCtl.getRawButton(1)) {
//                shooter.set(DoubleSolenoid.Value.kReverse)
//            } else {
//                shooter.set(DoubleSolenoid.Value.kForward)
//            }
//            if (OI.opCtl.getRawButton(2) || OI.opCtl.getRawButton(1) || hatch) {
//                hatchSelenoid.set(DoubleSolenoid.Value.kReverse)
//            } else {
//                hatchSelenoid.set(DoubleSolenoid.Value.kForward)
//            }
