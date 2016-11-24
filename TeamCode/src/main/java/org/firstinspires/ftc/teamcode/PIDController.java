package org.firstinspires.ftc.teamcode;

public class PIDController {
    //Date previous_time = new Date();
    double total_integral = 0;
    double P = .15; //increase until there is a lot of overshooting
    double D = 0; //increase until the overshooting is reduced to an acceptable level
    double I = 0; //increase until the final error is equal to zero.

    //long d1 = previous_time.getTime();
    long previous_time =  System.nanoTime();

    public double Step( double cError ) {
        double current_error = cError;
        //Date end = new Date();
        //long d2 = end.getTime();
        long end =  System.nanoTime();
        double time_diff = (end - previous_time) / 1000;
        double error_diff = current_error * time_diff;
        double integral_step = current_error * time_diff;
        total_integral = total_integral + integral_step;

        double Kp = current_error;
        if (Kp < 0) {
            Kp *= 1.8;
        }
        double Ki = total_integral;
        double Kd = error_diff / time_diff;

        double R = P*Kp + I*Ki + D*Kd;
        if (R > 100) {
            R = 100;
        }
        else if (R < -100) {
            R = -100;
        }
        Robot.telemetry.addData("Kp: ", (Kp * P));
        Robot.telemetry.addData("Ki: ", (Ki * I));
        Robot.telemetry.addData("Kd: ", (Kd * D));
        Robot.telemetry.addData("R: ", R);
        Robot.telemetry.addData("Current Error", current_error);

        return R;
    }
}
