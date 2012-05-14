/**
 * 
 */
package com.bt.mindstorm.robot.sensor;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import com.bt.mindstorm.SensorPin;
import com.bt.mindstorm.nxt.NXT;
import com.bt.mindstorm.robot.SensorTask;

/**
 * @author luthiger
 * 
 */
public class UltraSonic extends Sensor {
	private static final String TAG = "UltraSonic";

	// LOWSPEED sensor
	private static byte NXT_SENSOR_TYPE = (byte) 0x0B;
	// RAW mode
	private static byte NXT_SENSOR_MODE = (byte) 0x00;

	private LsCommunicationState lsCommunicationState = LsCommunicationState.READY;

	protected short distance = -1;

	public UltraSonic(NXT nxt, SensorPin pin) {
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
		switch (message.getData().getInt("message")) {
		case NXT.LSWRITE:
			((UltraSonicPollTask) task).switchState(LsCommunicationState.WAITING_FOR_RESULT);
			break;
		case NXT.LSSTATUS:
			byte bytesReady = message.getData().getByte("bytesReady");
			if (bytesReady > 0) {
				// bytes are ready to be read
				((UltraSonicPollTask) task).switchState(LsCommunicationState.READING);
			}
			break;
		case NXT.LSREAD:
			byte status = message.getData().getByte("status");
			byte length = message.getData().getByte("bytesRead");
			if ((status==0) && (length>0)) {
				distance = message.getData().getByte("data");
			} else {
				distance = -1;
			}
			Log.i(TAG, "distance is " + distance + " cm");
			((UltraSonicPollTask) task).switchState(LsCommunicationState.READY);
		}
	}

	@Override
	public Object getState() {
		return distance;
	}

	@Override
	public void reset() {

	}
	
	@Override
	public SensorTask getSensorTask() {
		if (task == null) {
			task = new UltraSonicPollTask();
		}
		return task;
	}

	enum LsCommunicationState {
		READY, WAITING_FOR_RESULT, READING
	}

	class UltraSonicPollTask implements SensorTask {
		private long period;
		
		@Override
		public void run() {
			switch (lsCommunicationState) {
			case READY:
				// Start read cycle using LSWRITE
				connector.getBluetoothMessageHandler().sendMessage(createLSWriteMessage());
				break;
			case WAITING_FOR_RESULT:
				// Wait for the result using LSSTATUS
				connector.getBluetoothMessageHandler().sendMessage(createLSStatusMessage());
				break;
			case READING:
				// Read result using LSREAD
				connector.getBluetoothMessageHandler().sendMessage(createLSReadMessage());
				break;
			default:
				break;
			}
		}

		public void switchState(LsCommunicationState newState) {
			lsCommunicationState = newState;
			Log.d(TAG, "LsCommunicationState set to " + newState);
		}

		private Message createLSStatusMessage() {
			Message msg = Message.obtain();
			Bundle msgBundle = new Bundle();
			msgBundle.putInt("action", NXT.LSSTATUS);
			msgBundle.putByte("inputPort", pin.getPinNr());
			msg.setData(msgBundle);
			return msg;
		}

		private Message createLSWriteMessage() {
			Message msg = Message.obtain();
			Bundle msgBundle = new Bundle();
			msgBundle.putInt("action", NXT.LSWRITE);
			msgBundle.putByte("inputPort", pin.getPinNr());
			msg.setData(msgBundle);
			return msg;
		}
		
		private Message createLSReadMessage() {
			Message msg = Message.obtain();
			Bundle msgBundle = new Bundle();
			msgBundle.putInt("action", NXT.LSREAD);
			msgBundle.putByte("inputPort", pin.getPinNr());
			msg.setData(msgBundle);
			return msg;
		}

		@Override
		public long getDelay() {
			return 10;
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
}
