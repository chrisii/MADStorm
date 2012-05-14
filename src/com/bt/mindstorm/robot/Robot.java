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
package com.bt.mindstorm.robot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.util.Log;
import com.bt.mindstorm.nxt.NXT;
import com.bt.mindstorm.robot.sensor.Sensor;

/**
 * Class representing a Lego Mindstorm robot and the common functionality. The
 * class cannot be instantiated. Use a derived class instead!
 * 
 * @author Juerg Luthiger
 * 
 */
public abstract class Robot {
	private static final String TAG = "Robot";

	protected NXT nxt;
	protected boolean isSimulation;
	protected boolean isEmergencyStop;
	/**
	 * Time of last emergency stop cancel
	 */
	private long emergencStopCancelTime;

	private ScheduledThreadPoolExecutor executor;

	public Robot(NXT nxt) {
		this.nxt = nxt;
		isSimulation = nxt.isSimulation();
		isEmergencyStop = false;
	}

	/**
	 * Stops robot.
	 */
	public void stop() {
		setVelocity(0, 0);
		// shutdown all submitted actor and sensor tasks
		executor.shutdown();
	}

	/**
	 * Starts robot.
	 */
	public final void start() {
		executor = new ScheduledThreadPoolExecutor(4);
		executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
		executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
		// start actor task at a fixed interval
		executor.scheduleAtFixedRate(getRobotMotionTask(), getRobotMotionTask()
				.getDelay(), getRobotMotionTask().getPeriod(),
				TimeUnit.MILLISECONDS);
		for (Sensor sensor : getSensors()) {
			// start each sensor task at a fixed interval
			SensorTask sensorTask = sensor.getSensorTask();
			executor.scheduleAtFixedRate(sensorTask, sensorTask.getDelay(),
					sensorTask.getPeriod(), TimeUnit.MILLISECONDS);
			sensor.activate();
		}
	}

	/**
	 * Returns the time of the last emergency stop.
	 * 
	 * @return time or -1, if no emergency has happened yet
	 */
	public long getEmergencStopCancelTime() {
		return (emergencStopCancelTime > 0) ? emergencStopCancelTime : -1;
	}

	/**
	 * Sets emergency stop.
	 */
	public void emergencyStop(boolean stop) {
		if (stop) {
			setVelocity(0.01, 0.01);
			emergencStopCancelTime = new Date().getTime();
			stop();
		}
		this.isEmergencyStop = stop;
		Log.d(TAG, "emergency is " + isEmergencyStop);
	}

	public boolean isEmergencyStop() {
		return isEmergencyStop;
	}

	/**
	 * Sets the velocity vector.
	 * 
	 * @param velX
	 *            velocity along x-axis in the range [-1,1]
	 * @param velY
	 *            velocity along y-axis in the range [-1,1]
	 */
	public void setVelocity(double velX, double velY) {
		throw new UnsupportedOperationException("not implemented");
	}

	/**
	 * Returns list of attached sensors.
	 * 
	 * @return sensors or empty list if no sensors are attached
	 */
	public List<Sensor> getSensors() {
		return new ArrayList<Sensor>();
	}

	/**
	 * Turns robot.
	 * 
	 * @param degree
	 *            degree to turn within range [-90, 90]
	 */
	public void turn(int degree) {
		throw new UnsupportedOperationException("not implemented");
	}

	/**
	 * Handle the sensor data in the specific robot.
	 * 
	 * @param sensor
	 *            The sensor and its data.
	 */
	public void handleSensorData(Sensor sensor) throws EmergencyStopException {
		throw new UnsupportedOperationException("not implemented");
	}

	/**
	 * Resets all actors and sensor to their initial values.
	 */
	public abstract void reset();

	/**
	 * Emits action.
	 * 
	 * @param isStartAction
	 *            signals action start
	 */
	public abstract void action(boolean isStartAction);

	protected abstract RobotMotionTask getRobotMotionTask();

}
