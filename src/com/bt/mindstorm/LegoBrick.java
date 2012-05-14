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
package com.bt.mindstorm;

import com.bt.mindstorm.robot.motor.Actor;
import com.bt.mindstorm.robot.sensor.Sensor;

/**
 * Base interface summarizing important functionalities of a ledo brick.
 * 
 * @author Juerg Luthiger
 * 
 */
public interface LegoBrick {
	/**
	 * Connects to Lego Brick with given address, usually the Bluetooth MAC
	 * address. After successful connecting the communication is started.
	 * 
	 * @param address
	 *            address of the Lego Brick to connect to
	 */
	void connectAndStart(String address);

	/**
	 * Creates a actor instance to given pin.
	 * 
	 * @param pin
	 *            actor pin to use
	 * @return the actor instance if successful, else null
	 */
	Actor createActor(ActorPin pin);

	/**
	 * Creates a sensor instance to given pin.
	 * 
	 * @param type
	 *            sensor type to use
	 * @param pin
	 *            sensor pin to use
	 * @return the sensor instance if successful, else null
	 */
	Sensor createSensor(SensorType type, SensorPin pin);

	/**
	 * Delays the communication with the NXT brick.
	 * 
	 * @param delay
	 */
	void setCommunicationDelay(int delay);

	/**
	 * Adds a listener, able to handle lego sensor data.
	 * 
	 * @param listener
	 *            listener to add
	 */
	public void addSensorListener(LegoBrickSensorListener listener);

	/**
	 * Removes given listener from the brick.
	 * 
	 * @param listener
	 *            listener to remove
	 */
	public void removeSensorListener(LegoBrickSensorListener listener);

}
