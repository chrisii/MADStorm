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

import java.util.ArrayList;
import java.util.List;

import com.bt.mindstorm.SensorType;
import com.bt.mindstorm.nxt.NXT;
import com.bt.mindstorm.nxt.NXT.NXTSensorPin;
import com.bt.mindstorm.robot.EmergencyStopException;
import com.bt.mindstorm.robot.sensor.Sensor;
import com.bt.mindstorm.robot.sensor.TouchSensor;

/**
 * Robot derived from {@link NXTCastorBot}. It adds a touch sensor in pin 1 to raise 
 * an emergency stop if touched.
 * 
 * @author Juerg Luthiger
 *
 */
public class NXTMADbot extends NXTCastorBot {
	private static int SENSOR_POLL_RATE = 100;
	List<Sensor> sensors = new ArrayList<Sensor>();

	public NXTMADbot(NXT nxt) {
		super(nxt);
		if (isSimulation) {
			return;
		} else if (nxt.isConnected()) {
			createSensors();
		} else {
			throw new IllegalStateException("nxt is not connected");
		}
	}

	private void createSensors() {
		createAndAddSensorToSensorList(SensorType.TOUCH_SENSOR, NXTSensorPin.PIN_1, SENSOR_POLL_RATE);
	}
	
	private void createAndAddSensorToSensorList(SensorType type, NXTSensorPin pin, int period) {
		Sensor sensor = nxt.createSensor(type, pin);
		sensor.getSensorTask().setPeriod(period);
		if (sensor != null) {
			sensors.add(sensor);
		}
	}

	@Override
	public List<Sensor> getSensors() {
		return sensors;
	}

	@Override
	public void reset() {
		super.reset();
		for (Sensor sensor : sensors) {
			sensor.reset();
		}
	}
	
	@Override
	public void action(boolean isSingleAction) {
		if (! isEmergencyStop) {
			emergencyStop(true);
		}
	}
	
	@Override
	public void handleSensorData(Sensor sensor) throws EmergencyStopException {
		if (sensor instanceof TouchSensor) {
			boolean touched = ((Boolean) sensor.getState()).booleanValue();
			if (touched) {
				throw new EmergencyStopException("touch sensor active");
			}
		}
	}
}
