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
 * Implementation of the color sensor. The sensor is able to distinguish between six colors.
 * 
 * @author Juerg Luthiger
 *
 */
public class ColorSensor extends Sensor {
	public enum Color {
		BLACK("black"), BLUE("blue"), GREEN("green"), YELLOW("yellow"), 
		RED("red"), WHITE("white"), UNDEFINED("undefined");

		private final String name;

		private Color(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
	
	@SuppressWarnings("unused")
	private static final String TAG = "ColorSensor";
	
	// COLOR sensor
	private static byte NXT_SENSOR_TYPE = 0x0d;	
	// RAW mode
	private static byte NXT_SENSOR_MODE = 0x00;
	// actual state
	private Color color;

	public ColorSensor(NXT nxt, SensorPin pin) {
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
			short value = message.getData().getShort("value");
			
			byte val = (byte) (value >> 8);
			if ((val<1) || (val > Color.values().length-1)) {
				color = Color.UNDEFINED;
			} else {
				color = Color.values()[val-1];
			}
//			Log.d(TAG, "Color: " + color.getName() + " (" + value + ")");
		}
	}

	@Override
	public Object getState() {
		return color;
	}

	@Override
	public void reset() {

	}

}
