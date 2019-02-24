package com.team2898.robot.config.DrivetrainConf

import com.team2898.engine.math.linear.Matrix
import com.team2898.engine.math.linear.row

val Dt_A = Matrix(arrayOf(
        row(1.00000000e+00, 1.83785584e-02, 0.00000000e+00, -8.19273385e-04),
        row(0.00000000e+00, 8.43577841e-01, 0.00000000e+00, -7.72924069e-02),
        row(0.00000000e+00, -8.19273385e-04, 1.00000000e+00, 1.83785584e-02),
        row(0.00000000e+00, -7.72924069e-02, 0.00000000e+00, 8.43577841e-01)
))
val Dt_B = Matrix(arrayOf(
        row(0.00061386, 0.00031017),
        row(0.05921959, 0.029262),
        row(0.00031017, 0.00061386),
        row(0.029262, 0.05921959)
))

val Dt_Kc = Matrix(arrayOf(
        row(69.64370108, 8.94143369, -10.75358192, -2.72169993),
        row(-10.75358192, -2.72169993, 69.64370108, 8.94143369)
))

val Dt_Kff = Matrix(arrayOf(
        row(9.04458066e+02, 2.20322927e+00, -1.49016407e+02, -3.94766169e-01),
        row(-1.49016407e+02, -3.94766169e-01, 9.04458066e+02, 2.20322927e+00)
))

val Dt_M = Matrix(arrayOf(
        row(9.99996933e-01, -1.71776713e-07),
        row(1.07787176e+01, -2.47180469e+00),
        row(-1.71776713e-07, 9.99996933e-01),
        row(-2.47180469e+00, 1.07787176e+01)
))
