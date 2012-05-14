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

import java.lang.reflect.Constructor;

import android.util.Log;
import com.bt.mindstorm.nxt.NXT;
import com.bt.mindstorm.robot.sensor.Sensor;

/**
 * Collection of the sensor types supported by this system.
 * 
 * @author Juerg Luthiger
 *
 */
public enum SensorType {
	TOUCH_SENSOR("ch.fhnw.edu.mad.mindstorm.robot.sensor.TouchSensor"), 
	LIGHT_SENSOR("ch.fhnw.edu.mad.mindstorm.robot.sensor.LightSensor"), 
	ULTRASONIC_SENSOR("ch.fhnw.edu.mad.mindstorm.robot.sensor.UltraSonic"),
	COLOR_SENSOR("ch.fhnw.edu.mad.mindstorm.robot.sensor.ColorSensor");
	
	private String className;
	private String TAG = "SensorType";
	
	private SensorType(String className) {
		this.className = className;
	}
	
	/**
	 * Create sensor instance for this sensor type and attach it to the 
	 * given pin on the NXT lego brick.
	 * 
	 * @param nxt NXT lego brick to which sensor will be attached
	 * @param pin Pin on the NXT lego brick.
	 * 
	 * @return sensor instance or null due to a failure 
	 */
	public Sensor createSensor(NXT nxt, SensorPin pin) {
		Sensor sensor = null;
		try {
			@SuppressWarnings("unchecked")
			Class<Sensor> clazz = (Class<Sensor>) Class.forName(className);
			Constructor<Sensor> constructor = clazz.getConstructor(NXT.class, SensorPin.class);
			sensor = constructor.newInstance(nxt, pin);
		} catch (Exception e) {
			Log.e(TAG, "sensor could not be instantiated:" + e.getMessage());
		} 
		return sensor;
	}
}
