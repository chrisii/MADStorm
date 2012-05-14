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
package com.bt.mindstorm.nxt;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.Message;
import com.bt.BluetoothChannel;
import com.bt.mindstorm.ActorPin;
import com.bt.mindstorm.LegoBrick;
import com.bt.mindstorm.LegoBrickSensorListener;
import com.bt.mindstorm.SensorPin;
import com.bt.mindstorm.SensorType;
import com.bt.mindstorm.robot.motor.Actor;
import com.bt.mindstorm.robot.sensor.Sensor;

/**
 * Class that represents the Lego Mindstorm NXT kit, which is a programmable
 * robotic device. It can handle input from up to four sensors and can control up
 * to three motors.
 * 
 * @author Juerg Luthiger
 * 
 */
public class NXT implements LegoBrick {
	public static final int MOTOR_RESET = 10;
	public static final int MOTOR_SPEED = 30;
	public static final int MOTOR_B_ACTION = 40;
	public static final int SET_SENSOR_INPUTPORT = 50;
	public static final int READ_MOTOR_STATE = 60;
	public static final int READ_SENSOR_STATE = 61;
	public static final int READ_LOWSPEED = 70;
	public static final int LSWRITE = 71;
	public static final int LSSTATUS = 72;
	public static final int LSREAD = 73;
	public static final int MOTOR_STATE = 1003;
	public static final int GET_INPUTSTATE = 1004;

	private List<LegoBrickSensorListener> listeners;
	private int delay = 0;
	private BluetoothChannel connector;
	private boolean isSimulation = false;

	/**
	 * Default constructor.
	 */
	public NXT() {
		listeners = new ArrayList<LegoBrickSensorListener>();
		Handler.Callback bluetoothCallback = new Handler.Callback() {
			/**
			 * Handles the messages sent by the NXT brick over bluetooth.
			 */
			public boolean handleMessage(Message msg) {
				for (LegoBrickSensorListener listener : listeners) {
					listener.handleLegoBrickMessage(msg);
				}
				return true;
			}
		};
		connector = new BluetoothChannel(bluetoothCallback);
	}

	/**
	 * Represents the different actor pins on a NXT brick.
	 */
	public enum NXTActorPin implements ActorPin {
		PIN_A((byte) 0), PIN_B((byte) 1), PIN_C((byte) 2);

		private final byte pinNr;

		private NXTActorPin(byte pinNr) {
			this.pinNr = pinNr;
		}

		public byte getPinNr() {
			return pinNr;
		}
	}

	/**
	 * Represents the different sensor pins on a NXT brick.
	 */
	public enum NXTSensorPin implements SensorPin {
		PIN_1((byte) 0), PIN_2((byte) 1), PIN_3((byte) 2), PIN_4((byte) 3);

		private final byte pinNr;

		private NXTSensorPin(byte pinNr) {
			this.pinNr = pinNr;
		}

		public byte getPinNr() {
			return pinNr;
		}
	}

	@Override
	public Actor createActor(ActorPin pin) {
		Actor motor = new Actor(this, pin, delay);
		return motor;
	}

	@Override
	public Sensor createSensor(SensorType type, SensorPin pin) {
		Sensor sensor = type.createSensor(this, pin);
		return sensor;
	}

	@Override
	public void connectAndStart(String address) {
		connector.setMACAddress(address);
		connector.start();
	}

	@Override
	public void setCommunicationDelay(int delay) {
		this.delay = delay;
	}

	@Override
	public void addSensorListener(LegoBrickSensorListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeSensorListener(LegoBrickSensorListener listener) {
		listeners.remove(listener);
	}

	public BluetoothChannel getBluetoothConnector() {
		return connector;
	}

	public boolean isSimulation() {
		return isSimulation;
	}

	public void setSimulation(boolean isSimulation) {
		this.isSimulation = isSimulation;
	}

	public boolean isConnected() {
		return connector.isConnected();
	}
}
