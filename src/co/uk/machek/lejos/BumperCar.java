package co.uk.machek.lejos;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.SoundSensor;
import lejos.nxt.TouchSensor;
import lejos.nxt.UltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;

/**
 * Demonstration of the Behavior subsumption classes.
 * 
 * Requires a wheeled vehicle with two independently controlled motors connected
 * to motor ports A and C, and a touch sensor connected to sensor port 1,
 * an ultrasonic sensor connected to port 3
 * and sound sensor connected to port 2
 * 
 * @author Brian Bagnall and Lawrie Griffiths, modified by Roger Glassey, Zdenek Machek
 * 
 */
public class BumperCar {
	static RegulatedMotor leftMotor = Motor.A;
	static RegulatedMotor rightMotor = Motor.C;

	// Use these definitions instead if your motors are inverted
	// static RegulatedMotor leftMotor = MirrorMotor.invertMotor(Motor.A);
	// static RegulatedMotor rightMotor = MirrorMotor.invertMotor(Motor.C);

	public static void main(String[] args) {
		leftMotor.setSpeed(400);
		rightMotor.setSpeed(400);
		Behavior drive = new DriveForward();
		Behavior detect = new DetectWall();
		Behavior listen = new DetectWall();
		Behavior[] behaviorList = { drive, detect, listen };
		Arbitrator arbitrator = new Arbitrator(behaviorList);
		LCD.drawString("Bumper Car", 0, 1);
		Button.waitForAnyPress();
		arbitrator.start();
	}
}

class DriveForward implements Behavior {

	private boolean _suppressed = false;

	public boolean takeControl() {
		return true; // this behavior always wants control.
	}

	public void suppress() {
		_suppressed = true;// standard practice for suppress methods
	}

	public void action() {
		_suppressed = false;
		BumperCar.leftMotor.forward();
		BumperCar.rightMotor.forward();
		while (!_suppressed) {
			Thread.yield(); // don't exit till suppressed
		}
		BumperCar.leftMotor.stop();
		BumperCar.leftMotor.stop();
	}
}

class DetectWall implements Behavior {

	public DetectWall() {
		touch = new TouchSensor(SensorPort.S1);
		sonar = new UltrasonicSensor(SensorPort.S3);
	}

	public boolean takeControl() {
		sonar.ping();

		return touch.isPressed() || sonar.getDistance() < 25;
	}

	public void suppress() {
		// Since this is highest priority behavior, suppress will never be
		// called.
	}

	public void action() {
		BumperCar.leftMotor.rotate(-180, true);// start Motor.A rotating
												// backward
		BumperCar.rightMotor.rotate(-360); // rotate C farther to make the turn
	}

	private TouchSensor touch;
	private UltrasonicSensor sonar;
}

/**
 * Sound sensor listening for command to start, stop car
 * @author Zdenek
 *
 */
class Listen implements Behavior {
	
	private SoundSensor ear;
	
	public Listen(){
		 ear = new SoundSensor(SensorPort.S2);
	}

	@Override
	public boolean takeControl() {
		
		return ear.readValue() > 100;
		
	}

	@Override
	public void action() {
		
		if(BumperCar.leftMotor.isMoving()){
			BumperCar.leftMotor.stop();
			BumperCar.rightMotor.stop();
		}
		else{
			BumperCar.leftMotor.forward();
			BumperCar.rightMotor.forward();
		}
	}

	@Override
	public void suppress() {
		// TODO Auto-generated method stub
		
	}
}
