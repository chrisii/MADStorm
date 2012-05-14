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

import android.os.Bundle;
import android.os.Message;
import com.bt.BluetoothChannel;
import com.bt.mindstorm.LegoBrickSensorListener;
import com.bt.mindstorm.SensorPin;
import com.bt.mindstorm.nxt.NXT;
import com.bt.mindstorm.robot.SensorTask;

/**
 * Abstract class which implements the basic functionalities of a sensor.
 * Each sensor must extend this abstract classes.
 * 
 * @author Juerg Luthiger
 *
 */
public abstract class Sensor implements LegoBrickSensorListener {
	protected SensorPin pin;
	protected SensorTask task;
	protected BluetoothChannel connector;

	public Sensor(NXT nxt, SensorPin pin) {
		this.pin = pin;
		connector = nxt.getBluetoothConnector();
		nxt.addSensorListener(this);
	}

	/**
	 * Activates sensor. The sensor will now be polled at a fix rate as long as
	 * deactivate is called.
	 */
	public final void activate() {
		connector.getBluetoothMessageHandler().sendMessage(createActivateMessage());
	}

	public final SensorPin getSensorPin() {
		return pin;
	}

	public abstract byte getSensorType();

	public abstract byte getSensorMode();
	
	public abstract void handleLegoBrickMessage(Message message);

	/**
	 * Creates message, used by the bluetooth channel, to activate the sensor.
	 * 
	 * @return the message itself
	 */
	protected Message createActivateMessage() {
		Message msg = Message.obtain();
		Bundle msgBundle = new Bundle();
		msgBundle.putInt("action", NXT.SET_SENSOR_INPUTPORT);
		msgBundle.putByte("inputPort", pin.getPinNr());
		msgBundle.putByte("inputType", getSensorType());
		msgBundle.putByte("inputMode", getSensorMode());
		msg.setData(msgBundle);
		return msg;
	}

	/**
	 * Creates message, used by the bluetooth channel, to read out the sensor.
	 * 
	 * @return the message itself
	 */
	protected Message createReadMessage() {
		Message msg = Message.obtain();
		Bundle msgBundle = new Bundle();
		msgBundle.putInt("action", NXT.READ_SENSOR_STATE);
		msgBundle.putByte("inputPort", pin.getPinNr());
		msg.setData(msgBundle);
		return msg;
	}
	
	class SensorPollTask implements SensorTask {
		private long period;
		
		@Override
		public void run() {
			connector.getBluetoothMessageHandler().sendMessage(createReadMessage());
		}

		@Override
		public long getDelay() {
			return 5;
		}

		@Override
		public long getPeriod() {
			return period;
		}

		@Override
		public void setPeriod(long period) {
			this.period = period;
		}
	}

	public abstract Object getState();
	
	public abstract void reset();

	public SensorTask getSensorTask() {
		if (task == null) {
			task = new SensorPollTask();
		}
		return task;
	}
}
