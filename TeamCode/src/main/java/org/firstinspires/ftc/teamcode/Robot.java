package org.firstinspires.ftc.teamcode;

import android.content.Context;
import android.util.Log;

import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cColorSensor;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cRangeSensor;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.sensors.IMU;
import org.firstinspires.ftc.teamcode.sensors.MRColorSensor;
import org.firstinspires.ftc.teamcode.sensors.PhoneGyro;
import org.firstinspires.ftc.teamcode.sensors.Sensor;
import org.firstinspires.ftc.teamcode.sensors.Vuforia;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class Robot {
    // Constants
    public static final double ODS_GRAY_VALUE = 0.2; // TODO: find this

    // Motors
    public static DcMotor leftMotor;
    public static DcMotor rightMotor;

    public static DcMotor flywheelLeft;
    public static DcMotor flywheelRight;

    public static DcMotor nomFront;
    public static DcMotor nomMiddle;

    public static DcMotor conveyor;

    // Servos
    public static Servo beaconLeft;
    public static Servo beaconRight;

    // Sensors
    public static ColorSensor beaconColor = null;
    public static MRColorSensor leftLineColor = null;
    public static MRColorSensor rightLineColor = null;
    public static ModernRoboticsI2cRangeSensor range = null;

    public static OpticalDistanceSensor leftLineLight;
    public static OpticalDistanceSensor rightLineLight;

    public static IMU imu = null;
    public static PhoneGyro phoneGyro = null;

    public static OpticalDistanceSensor frontDist;

    public static Vuforia vuforia;

    public static VoltageSensor voltageSensor;

    // Sensor list
    public static ArrayList<Sensor> sensors;

    // Telemetry
    public static Telemetry telemetry;

    // The original opmode.
    public static LinearOpMode opMode;

    // The app context
    public static Context appContext;

    // The current alliance
    public static Alliance currentAlliance;

    public static void init(LinearOpMode om) {
        opMode = om;
        HardwareMap hardwareMap = opMode.hardwareMap;
        telemetry = opMode.telemetry;
        appContext = hardwareMap.appContext;
        sensors = new ArrayList<Sensor>();

        // Motors
        leftMotor = hardwareMap.dcMotor.get("drive_right");
        leftMotor.setDirection(DcMotor.Direction.FORWARD);

        rightMotor = hardwareMap.dcMotor.get("drive_left");
        rightMotor.setDirection(DcMotor.Direction.REVERSE);

        flywheelLeft = hardwareMap.dcMotor.get("launch_left");
        flywheelLeft.setDirection(DcMotor.Direction.FORWARD);

        flywheelRight = hardwareMap.dcMotor.get("launch_right");
        flywheelRight.setDirection(DcMotor.Direction.REVERSE);

        nomMiddle = hardwareMap.dcMotor.get("inside_nom");
        nomMiddle.setDirection(DcMotor.Direction.FORWARD);

        nomFront = hardwareMap.dcMotor.get("outside_nom");
        nomFront.setDirection(DcMotor.Direction.FORWARD);

        conveyor = hardwareMap.dcMotor.get("lift");
        conveyor.setDirection(DcMotor.Direction.REVERSE);

        // Servos
        beaconLeft = hardwareMap.servo.get("leftBeacon");
        beaconRight = hardwareMap.servo.get("rightBeacon");

        // Sensors
        // Color
        beaconColor = hardwareMap.colorSensor.get("beacon color");
        beaconColor.enableLed(false);
        leftLineColor = new MRColorSensor(hardwareMap.i2cDeviceSynch.get("left line color"), I2cAddr.create8bit(0x6C));
        sensors.add(leftLineColor);
        rightLineColor = new MRColorSensor(hardwareMap.i2cDeviceSynch.get("right line color"), I2cAddr.create8bit(0x4C));
        sensors.add(rightLineColor);

        // Optical distance
        leftLineLight = hardwareMap.opticalDistanceSensor.get("left_line");
        rightLineLight = hardwareMap.opticalDistanceSensor.get("right_line");

        // IMU
        imu = new IMU(hardwareMap.get(BNO055IMU.class, "imu"));
        sensors.add(imu);

        range = hardwareMap.get(ModernRoboticsI2cRangeSensor.class, "range");

        // Phone gyroscope
        //phoneGyro = new PhoneGyro();
        //sensors.add(phoneGyro);

        // Front distance
        frontDist = hardwareMap.opticalDistanceSensor.get("front dist");

        // Vuforia
        vuforia = new Vuforia();
        sensors.add(vuforia);

        // Voltage
        voltageSensor = hardwareMap.voltageSensor.iterator().next();
    }

    public static void start() {
        vuforia.start();
    }

    public static void update() {
        // update sensors
        for (Sensor s : sensors) {
            s.update();
        }

        // and telemetry
        telemetry.update();
    }

    public static void idle() throws InterruptedException {
        opMode.idle();
    }

    // Sensor management
    public static void verifyAllSensors() throws InterruptedException {
        Blackbox.log("SENSOR", "=== Sensor map ===");
        telemetry.addData("Status", "Starting sensors...");
        telemetry.update();
        int passedSensors = 0;
        int failedSensors = 0;
        int currentSensor = 1;
        ArrayList<String> failures = new ArrayList<String>();
        for (Sensor s : Robot.sensors) {
            telemetry.addData("Status", "Starting sensor " + currentSensor + " out of " + Robot.sensors.size() + "...");
            telemetry.update();
            Blackbox.log("SENSOR", s.uniqueName());
            Blackbox.log("SENSOR", "FW: " + Utils.intToHexString(s.firmwareRevision()) + ", MFG: " + Utils.intToHexString(s.manufacturer()) + ", CODE: " + Utils.intToHexString(s.sensorIDCode()));
            if (s.ping()) {
                // yay
                s.init();
                Blackbox.log("SENSOR", "PASS");
                s.update();
                passedSensors++;
            } else {
                // uh oh
                Blackbox.log("SENSOR", "FAIL");
                failures.add(s.uniqueName());
                failedSensors++;
            }
            currentSensor++;
        }

        Blackbox.log("SENSOR", passedSensors + " sensor(s) passed / " + failedSensors + " sensor(s) failed");

        if (failedSensors > 0) {
            // oh no
            Blackbox.log("SENSOR", "SENSOR FAILURE");
            telemetry.addData("Status", "SENSOR FAILURE");
            int failIndex = 1;
            for (String failure : failures) {
                telemetry.addData("Failure #" + (failIndex), failure);
                failIndex++;
            }
            telemetry.addLine(passedSensors + " other sensors passed");
            telemetry.update();
            while (true) {
                Robot.idle();
            }
        }
    }

    // Motor methods

    public static void leftMotors(double power) {
        leftMotor.setPower(power);
    }

    public static void rightMotors(double power) {
        rightMotor.setPower(power);
    }

    // Driving methods

    public static void turnToHeading(float targetHeading, double power) {
        float currentHeading = imu.getHeading();
        boolean turnLeft = (targetHeading - currentHeading > 0 ? true : false);
        while (true) {
            telemetry.addData("hdg", currentHeading);
            //telemetry.addData("phoneHdg", Robot.phoneGyro.getHeading());
            telemetry.update();

            double currentSpeed = power;
            float distanceTo = Math.abs(targetHeading - currentHeading);
            double minimumSpeed = 0.3f;
            double minimumLeftSpeed = (turnLeft ? -minimumSpeed : minimumSpeed);
            double minimumRightSpeed = (turnLeft ? minimumSpeed : -minimumSpeed);

            if (distanceTo < 10) {
                currentSpeed *= 0.20;
                currentSpeed = Math.min(currentSpeed, 0.3f);
            } else if (distanceTo < 20) {
                currentSpeed *= 0.30;
                currentSpeed = Math.min(currentSpeed, 0.35f);
            } else if (distanceTo < 30) {
                currentSpeed *= 0.40;
                currentSpeed = Math.min(currentSpeed, 0.4f);
            }

            leftMotors(Math.max(minimumLeftSpeed, (turnLeft ? -currentSpeed : currentSpeed)));
            rightMotors(Math.max(minimumRightSpeed, (turnLeft ? currentSpeed : -currentSpeed)));

            imu.update();
            currentHeading = imu.getHeading();

            turnLeft = (targetHeading - currentHeading > 0 ? true : false);
            /*if (turnLeft && targetHeading < currentHeading) {
                break;
            } else if (!turnLeft && targetHeading > currentHeading) {
                break;
            }*/
            if (targetHeading == currentHeading) {
                break;
            }
        }
        leftMotors(0.0);
        rightMotors(0.0);
    }


    public static void moveForward_accel(double distanceToDrive, double power) throws InterruptedException {
        double distanceDriven = 0.0f;
        while (distanceDriven < (distanceToDrive * 100)) {
            leftMotors(power);
            rightMotors(power);
            imu.update();
            idle();
            distanceDriven += Math.abs(imu.getGravity().xAccel);
            telemetry.addData("distanceDriven", distanceDriven);
            telemetry.addData("encoder", leftMotor.getCurrentPosition());
            telemetry.update();
        }
        leftMotors(0.0);
        rightMotors(0.0);
    }

    public static void moveForward_encoder(double distanceToDrive, double power) throws InterruptedException {
        double startPos = leftMotor.getCurrentPosition();
        boolean negative = (distanceToDrive < 0);
        if (negative) {
            power *= -1;
        }
        while (
                (!negative && leftMotor.getCurrentPosition() - startPos < distanceToDrive) ||
                (negative && leftMotor.getCurrentPosition() - startPos > distanceToDrive)
        ) {
            double factor = 1.0f;
            if ((leftMotor.getCurrentPosition() - startPos) > (distanceToDrive / 2)) {
                factor = 0.5f;
            }
            leftMotors(power * factor);
            rightMotors(power * factor);
            idle();
            telemetry.addData("curPos", leftMotor.getCurrentPosition());
            telemetry.update();
        }
        leftMotors(0.0);
        rightMotors(0.0);
    }

    // Sensing methods
    public static Alliance getBeaconRightColor() throws InterruptedException {
        beaconLeft.setPosition(0.0);
        beaconRight.setPosition(0.0);
        telemetry.addLine("WAITING FOR SERVO POS");
        telemetry.update();
        Thread.sleep(1000);
        Alliance response;
        if ((beaconColor.red() == beaconColor.blue()) || (beaconColor.red() == 0 && beaconColor.blue() == 255) || (beaconColor.blue() == 0 && beaconColor.red() == 255)) {
            response = Alliance.UNKNOWN;
        } else if (beaconColor.red() > beaconColor.blue()) {
            response = Alliance.RED;
        } else {
            response = Alliance.BLUE;
        }
        return response;
    }

}
