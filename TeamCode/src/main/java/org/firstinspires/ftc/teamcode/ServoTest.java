package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.Servo;

@Autonomous(name="Servo Test", group="Tests")
public class ServoTest extends LinearOpMode
{
    @Override
    public void runOpMode() throws InterruptedException {
        Servo beaconLeft = hardwareMap.servo.get("leftBeacon");
        Servo beaconRight = hardwareMap.servo.get("rightBeacon");

        waitForStart();

        Utils.showToast("Sensing mode", 0);
        beaconLeft.setPosition(0.1f);
        beaconRight.setPosition(0.3f);
        Thread.sleep(3000);

        Utils.showToast("Pressing left", 0);
        beaconLeft.setPosition(1.0f);
        beaconRight.setPosition(0.9f);
        Thread.sleep(3000);

        Utils.showToast("Pressing right", 0);
        beaconLeft.setPosition(0.1f);
        beaconRight.setPosition(0.3f);
        Thread.sleep(3000);

        requestOpModeStop();
    }
}
