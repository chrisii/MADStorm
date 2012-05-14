/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bt.mindstorm.robot.model;

import com.bt.mindstorm.nxt.NXT;
import com.bt.mindstorm.nxt.NXT.NXTActorPin;
import com.bt.mindstorm.robot.Robot;
import com.bt.mindstorm.robot.RobotMotionTask;
import com.bt.mindstorm.robot.motor.Actor;

/**
 * Class that represents the CastorBot robot {@linkplain http://www.nxtprograms.com/castor_bot/index.html}. <br/>
 * This simple robot uses two motor drives, plugged into connector B (left
 * wheel) and C (right wheel). <br/>
 * No sensors are used.
 * 
 * @author Juerg Luthiger
 * 
 */
public class NXTCastorBot extends Robot {
	@SuppressWarnings("unused")
	private static final String TAG = "NXTCastorBot";
	private int DIR = -1;

	private Actor motorB;
	private Actor motorC;
	
	private RobotMotionTask actorTask = null;
	
	private Velocity velocity = new Velocity();


	/**
	 * Default constructor.
	 */
	public NXTCastorBot(NXT nxt) {
		super(nxt);
		if (isSimulation) {
			return;
		} else if (nxt.isConnected()) {
			createMotors();
		} else {
			throw new IllegalStateException("nxt is not connected");
		}
	}

	protected void createMotors() {
		motorB = nxt.createActor(NXTActorPin.PIN_B);
		motorC = nxt.createActor(NXTActorPin.PIN_C);
	}

	@Override
	public void stop() {
		if (!isSimulation) {
			motorB.setSpeed(0);
			motorC.setSpeed(0);
		}
		super.stop();
	}

	@Override
	public void setVelocity(double vx, double vy) {
		if (isEmergencyStop)
			return;

		double vl;
		double vr;

		// calculate speed on left and right actor
		if (vx < 0) {
			vl = getDirection() * vy + vx;
			vr = getDirection() * vy;
		} else {
			vl = getDirection() * vy;
			vr = getDirection() * vy - vx;
		}

		// check the limit, cannot be more than 1
		double signLeft = vl / Math.abs(vl);
		double signRight = vr / Math.abs(vr);
		vl = (Math.abs(vl) >= 1.0) ? signLeft * 1.0 : vl;
		vr = (Math.abs(vr) >= 1.0) ? signRight * 1.0 : vr;

		// make data ready to be read by the MotionTask
		synchronized (velocity) {
			velocity.left = vl;
			velocity.right = vr;
		}
	}

	protected int getDirection() {
		return DIR;
	}

	@Override
	public void reset() {
		stop();
	}

	public void action(boolean isStartAction) {
		// do nothing special
	}
	
	@Override
	public RobotMotionTask getRobotMotionTask() {
		if (actorTask == null) {
			actorTask = new NXTCastorBotMotionTask();
		}
		return actorTask;
	}

	private class NXTCastorBotMotionTask implements RobotMotionTask {
		// time values are in milliseconds
		private static final long DELAY = 0; 
		private static final long PERIOD = 400; 

		@Override
		public void run() {
			// set the calculated speed
			if (!isSimulation) {
				synchronized (velocity) {
					motorB.setSpeed((int) (velocity.left * Actor.SPEED_LIMIT));
					motorC.setSpeed((int) (velocity.right * Actor.SPEED_LIMIT));
				}
			}
		}

		@Override
		public long getDelay() {
			return DELAY;
		}

		@Override
		public long getPeriod() {
			return PERIOD;
		}
	}

	private class Velocity {
		double left;
		double right;
		
		@Override
		public String toString() {
			return "v(left)="+ left + "; v(right)=" + right;
		}
	}
}
