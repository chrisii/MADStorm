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
package com.bt.mindstorm.robot.sensor;

import android.os.Message;
import com.bt.mindstorm.SensorPin;
import com.bt.mindstorm.nxt.NXT;

/**
 * @author Lavanchy
 * 
 */
public class LightSensor extends Sensor {
	// LIGHT sensor
	private static byte NXT_SENSOR_TYPE = (byte) 0x05;
	// RAW mode
	private static byte NXT_SENSOR_MODE = (byte) 0x00;
	// actual sensor value
	private short value;

	/**
	 * @param nxt
	 * @param pin
	 */
	public LightSensor(NXT nxt, SensorPin pin) {
		super(nxt, pin);
	}

	@Override
	public byte getSensorType() {
		return NXT_SENSOR_TYPE;
	}

	@Override
	public byte getSensorMode() {
		return NXT_SENSOR_MODE;
	}

	@Override
	public void handleLegoBrickMessage(Message message) {
		if (message.getData().getByte("sensortype") == NXT_SENSOR_TYPE) {
			value = message.getData().getShort("value");
		}
	}

	@Override
	public Object getState() {
		return value;
	}

	@Override
	public void reset() {
	}

}
