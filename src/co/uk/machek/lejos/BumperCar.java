package co.uk.machek.lejos;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
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
		Behavior listen = new Listen();
		Behavior[] behaviorList = { drive, detect, listen };
		Arbitrator arbitrator = new Arbitrator(behaviorList);
		LCD.drawString("Bumper Car", 0, 1);
		Button.waitForAnyPress();
		arbitrator.start();
	}
}

class DriveForward implements Behavior {

	private boolean _suppressed = false;
	private SoundSensor ear;
	
	public DriveForward(){
		ear = new SoundSensor(SensorPort.S2);
	}

	/**
	 * always go forward
	 */
	public boolean takeControl() {
		return true;
	}

	public void suppress() {
		_suppressed = true;// standard practice for suppress methods
	}

	public void action() {
		_suppressed = false;
		
		LCD.clear();
		LCD.drawString("Moving forward", 0, 1);
		
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

	/**
	 * Stops robot if obstacle is detected
	 */
	public boolean takeControl() {
		sonar.ping();

		return touch.isPressed() || sonar.getDistance() < 25;
	}

	public void suppress() {
		// Since this is highest priority behavior, suppress will never be
		// called.
	}

	public void action() {
		LCD.clear();
		LCD.drawString("Obstacle detected", 0, 1);
		
		BumperCar.leftMotor.rotate(-180, true);// start Motor.A rotating
												// backward
		BumperCar.rightMotor.rotate(-360); // rotate C farther to make the turn
	}

	private TouchSensor touch;
	private UltrasonicSensor sonar;
}

/**
 * Sound sensor listening for command to turn
 * @author Zdenek
 *
 */
class Listen implements Behavior {
	
	private SoundSensor ear;
	private boolean _suppressed = false;
	
	public Listen(){
		 ear = new SoundSensor(SensorPort.S2);
	}

	/**
	 * Listens for loudish sound
	 */
	public boolean takeControl() {
		return ear.readValue() > 30;
	}

	
	public void action() {
		
		LCD.clear();
		LCD.drawString("Turning", 0, 1);
		Sound.twoBeeps();
		BumperCar.leftMotor.rotate(-180, true);// start Motor.A rotating
		// backward
		BumperCar.rightMotor.rotate(-360); // rotate C farther to make the turn
	}

	
	public void suppress() {
		_suppressed = true;
	}
}
