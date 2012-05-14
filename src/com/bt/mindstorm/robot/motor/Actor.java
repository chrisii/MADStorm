package com.bt.mindstorm.robot.motor;

import android.os.Bundle;
import android.os.Message;
import com.bt.BluetoothChannel;
import com.bt.mindstorm.ActorPin;
import com.bt.mindstorm.nxt.NXT;

public class Actor {
	public static int SPEED_LIMIT = 100;
	private ActorPin pin;
	private BluetoothChannel connector;
	private int delay;

	public Actor(NXT nxt, ActorPin pin, int delay) {
		this.pin = pin;
		this.delay = delay;
		connector = nxt.getBluetoothConnector();
	}

	/**
	 * Sets motor speed. The speed must be between -100 and 100, 0 stops the
	 * motor.
	 * 
	 * @param speed
	 */
	public void setSpeed(int val) throws IllegalArgumentException {
		if ((val > SPEED_LIMIT) || (val < -SPEED_LIMIT)) {
			throw new IllegalArgumentException(
					"speed must be within the range [-100,100]");
		}
		Message msg = Message.obtain();
		Bundle msgBundle = new Bundle();
		msgBundle.putInt("action", NXT.MOTOR_SPEED);
		msgBundle.putInt("actor", pin.getPinNr());
		msgBundle.putInt("value", val);
		msg.setData(msgBundle);

		if (delay == 0) {
			connector.getBluetoothMessageHandler().sendMessage(msg);
		} else {
			connector.getBluetoothMessageHandler().sendMessageDelayed(msg, delay);
		}

	}

}
