/**
 *   This file is derived from the MINDdroid project.
 *
 *	 MINDdroid
 *   Copyright 2010 Guenther Hoelzl, Shawn Brown
 *   
 *   MINDdroid is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   MINDdroid is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with MINDdroid.  If not, see <http://www.gnu.org/licenses/>.
**/

package com.bt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.bt.mindstorm.nxt.NXT;

/**
 * Helper class for communication over bluetooth with the NXT Brick.<p/>
 * After starting the thread it will start a loop and call for the lego brick state regularly. This state 
 * can be read out with {@linkplain #getActualState()}. <br/>
 * Messages sent by the lego brick itself, either as a result of an action or 
 */
public class BluetoothChannel extends Thread {
	//private static final String TAG = "BluetoothChannel";
    public static final int DISCONNECT = 99;
    public static final int DISPLAY_TOAST = 1000;
    public static final int STATE_CONNECTED = 1001;
    public static final int STATE_CONNECTERROR = 1002;
    public static final int STATE_RECEIVEERROR = 1004;
    public static final int STATE_SENDERROR = 1005;
    public static final int FIRMWARE_VERSION = 1006;

    public static final int NO_DELAY = 0;

    private static final UUID SERIAL_PORT_SERVICE_CLASS_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // this is the only OUI registered by LEGO, see http://standards.ieee.org/regauth/oui/index.shtml
    public static final String OUI_LEGO = "00:16:53";
	private static final String TAG = "BluetoothChannel";

    private BluetoothAdapter btAdapter;
    private BluetoothSocket nxtBTsocket = null;
    private DataOutputStream nxtDos = null;
    private DataInputStream nxtDin = null;
    private boolean connected = false;

    private Handler.Callback callbackHandler;
    private String macAddress;

    private byte[] actualState;

    public BluetoothChannel(Handler.Callback callback) {
        this.btAdapter = BluetoothAdapter.getDefaultAdapter();
        this.callbackHandler = callback;
    }

    public byte[] getActualState() {
        return actualState;
    }

    public boolean isBTAdapterEnabled() {
        return (btAdapter == null) ? false : btAdapter.isEnabled();
    }

    @Override
    public void run() {
        createNXTconnection();
        while (connected) {
            int length;
            try {
            	// read answer
                length = nxtDin.readByte();
                length = (nxtDin.readByte() << 8) + length;
                actualState = new byte[length];
                nxtDin.read(actualState);
                // send answer to the caller using the callback interface
                if ((length >= 2) && (actualState[0] == 0x02)) {
                    dispatchMessage(actualState);
                }
            } catch (IOException e) {
                // don't inform the user when connection is already closed
                if (connected)
                    sendState(STATE_RECEIVEERROR);
                return;
            }
        }
    }

    public void setMACAddress(String macAddress) {
	    this.macAddress = macAddress;
	}

	public Handler getBluetoothMessageHandler() {
		return bluetoothMessageHandler;
	}

	public boolean isConnected() {
		return connected;
	}

	public void disconnect() {
		Message msg = bluetoothMessageHandler.obtainMessage();
		Bundle bundle = new Bundle();
		bundle.putInt("message", BluetoothChannel.DISCONNECT);
		msg.setData(bundle);
		bluetoothMessageHandler.handleMessage(msg);
	}

	/**
	 * Create bluetooth-connection with SerialPortServiceClass_UUID
	 *
	 * @see <a href=
	 *      "http://lejos.sourceforge.net/forum/viewtopic.php?t=1991&highlight=android"
	 *      />
	 */
	private void createNXTconnection() {
	    try {
	        BluetoothSocket nxtBTsocketTEMPORARY;
	        BluetoothDevice nxtDevice = null;
	        nxtDevice = btAdapter.getRemoteDevice(macAddress);
	
	        if (nxtDevice == null) {
	            sendToast("No paired NXT robot found!");
	            sendState(STATE_CONNECTERROR);
	            return;
	        }
	
	        nxtBTsocketTEMPORARY = nxtDevice.createRfcommSocketToServiceRecord(SERIAL_PORT_SERVICE_CLASS_UUID);
	        nxtBTsocketTEMPORARY.connect();
	        nxtBTsocket = nxtBTsocketTEMPORARY;
	
	        nxtDin = new DataInputStream(nxtBTsocket.getInputStream());
	        nxtDos = new DataOutputStream(nxtBTsocket.getOutputStream());
	
	        connected = true;
	        sendState(STATE_CONNECTED);
	    } catch (IOException e) {
	         sendState(STATE_CONNECTERROR);
	         Log.e(BluetoothChannel.class.getName(), e.getMessage());
	    }
	}

	private void dispatchMessage(byte[] message) {
        switch (message[1]) {
            case 0x06:
                // GETOUTPUTSTATE return message
                if (message.length >= 25)
                    sendState(NXT.MOTOR_STATE);
                break;
            case 0x07:
                // GETINPUTSTATE return message
                if (message.length >= 16)
                    sendState(NXT.GET_INPUTSTATE, message);
                break;
            case 0x0E:
            	// LSSTATUS
            	if (message.length == 4) 
            		sendLSStatus(NXT.LSSTATUS, message);
            case 0x0F:
            	// LSWRITE
            	if (message.length == 3) 
            		sendLSWrite(NXT.LSWRITE, message);
            case 0x10:
            	// LSREAD
            	if (message.length == 20) 
            		sendLSRead(NXT.LSREAD, message);
        }
    }

	private void destroyNXTconnection() {
		if (nxtBTsocket != null) {
			try {
				// send stop messages before closing
				changeMotorSpeed(NXT.NXTActorPin.PIN_A.getPinNr(), 0);
				changeMotorSpeed(NXT.NXTActorPin.PIN_B.getPinNr(), 0);
				changeMotorSpeed(NXT.NXTActorPin.PIN_C.getPinNr(), 0);
				waitSomeTime(500);
				connected = false;
				nxtBTsocket.close();
				nxtBTsocket = null;
				if (nxtDin != null) {
					nxtDin.close();
					nxtDos.close();
				}
			} catch (IOException e) {
	            sendToast("Problem in closing the connection!");
				Log.e(BluetoothChannel.class.getName(), e.getMessage());
			}
		}
	}
    
    private void changeMotorSpeed(int motor, int speed) {
        if (speed > 100)
            speed = 100;
        else if (speed < -100)
            speed = -100;
        byte[] message = BluetoothMessage.getMotorMessage(motor, speed);
        sendMessage(message);
    }

    private void rotateTo(int motor, int end) {
        byte[] message = BluetoothMessage.getMotorMessage(motor, -80, end);
        sendMessage(message);
    }

    private void reset(int motor) {
        byte[] message = BluetoothMessage.getResetMessage(motor);
        sendMessage(message);
    }

    private void readMotorState(int actor) {
        byte[] message = BluetoothMessage.getActorStateMessage(actor);
        sendMessage(message);
    }

	private void readSensorState(byte sensorPort) {
        byte[] message = BluetoothMessage.getSensorStateMessage(sensorPort);
        sendMessage(message);
	}

	private void setLSWrite(byte sensorPort) {
        byte[] message = BluetoothMessage.getSetLSWriteMessage(sensorPort);
        sendMessage(message);
	}
	
	private void setLSRead(byte sensorPort) {
        byte[] message = BluetoothMessage.getSetLSReadMessage(sensorPort);
        sendMessage(message);
	}

	private void setLSStatus(byte sensorPort) {
        byte[] message = BluetoothMessage.getSetLSStatusMessage(sensorPort);
        sendMessage(message);
	}

	private void setInputPort(byte inputPort, byte inputType, byte inputMode) {
    	byte[] message = BluetoothMessage.getSetInputModeMessage(inputPort, inputType, inputMode);
    	Log.d(TAG, "inputPort = " + inputPort);
    	Log.d(TAG, "inputType = " + inputType);
    	Log.d(TAG, "inputMode = " + inputMode);
    	if (! sendMessage(message)) {
    		sendToast("Problem setting sensor!");;
    	}
    }

    private boolean sendMessage(byte[] message) {
        if (nxtDos == null) {
            return false;
        }
        try {
            // send message length first
            int messageLength = message.length;
            nxtDos.writeByte(messageLength);
            nxtDos.writeByte(messageLength >> 8);
            // send message
            nxtDos.write(message, 0, message.length);
            nxtDos.flush();
            return true;
        } catch (IOException ioe) {
            sendState(STATE_SENDERROR);
            return false;
        }
    }

    private void waitSomeTime(int millis) {
        try {
            Thread.sleep(millis);

        } catch (InterruptedException e) {
        }
    }

    private void sendToast(String toastText) {
        Bundle myBundle = new Bundle();
        myBundle.putInt("message", DISPLAY_TOAST);
        myBundle.putString("toastText", toastText);
        sendBundle(myBundle);
    }

    private void sendState(int message) {
        Bundle myBundle = new Bundle();
        myBundle.putInt("message", message);
        sendBundle(myBundle);
    }

    private void sendState(int getInputstate, byte[] message) {
    	short value = 0;
    	value |= message[12] & 0xFF;
        value <<= 8;
        value |= message[13] & 0xFF;
        Bundle myBundle = new Bundle();
        myBundle.putInt("message", getInputstate);
        myBundle.putByte("sensorport", message[3]);
        myBundle.putByte("sensortype", message[6]);
        myBundle.putShort("value", value);
        sendBundle(myBundle);
	}

	private void sendLSStatus(int getLsState, byte[] message) {
        Bundle myBundle = new Bundle();
        myBundle.putInt("message", getLsState);
        myBundle.putByte("status", message[2]);
        myBundle.putByte("bytesReady", message[3]);
        sendBundle(myBundle);
	}

	private void sendLSWrite(int getLsWrite, byte[] message) {
        Bundle myBundle = new Bundle();
        myBundle.putInt("message", getLsWrite);
        myBundle.putByte("status", message[2]);
        sendBundle(myBundle);
	}
	
	private void sendLSRead(int getLsRead, byte[] message) {
        Bundle myBundle = new Bundle();
        myBundle.putInt("message", getLsRead);
        myBundle.putByte("status", message[2]);
        myBundle.putByte("bytesRead", message[3]);
        myBundle.putByte("data", message[4]);
        sendBundle(myBundle);
	}

	private void sendBundle(Bundle myBundle) {
        Message myMessage = Message.obtain();
        myMessage.setData(myBundle);
        callbackHandler.handleMessage(myMessage);
    }
    
    // receive messages from the UI
    final Handler bluetoothMessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.getData().getInt("action")) {
                case NXT.MOTOR_SPEED:
                	int actor = msg.getData().getInt("actor");
                    changeMotorSpeed(actor, msg.getData().getInt("value"));
                    break;
                case NXT.MOTOR_B_ACTION:
                    rotateTo(NXT.NXTActorPin.PIN_B.getPinNr(), msg.getData().getInt("value1"));
                    break;
                case NXT.MOTOR_RESET:
                    reset(msg.getData().getInt("value1"));
                    break;
                case NXT.READ_MOTOR_STATE:
                    readMotorState(msg.getData().getInt("value1"));
                    break;
                case NXT.READ_SENSOR_STATE:
                    readSensorState(msg.getData().getByte("inputPort"));
                    break;  
                case NXT.READ_LOWSPEED:
                    setInputPort(msg.getData().getByte("inputPort"), 
                    		msg.getData().getByte("inputType"),
                    		msg.getData().getByte("inputMode"));
                    break;                     
                case NXT.LSWRITE:
                    setLSWrite(msg.getData().getByte("inputPort"));
                    break;                     
                case NXT.LSSTATUS:
                    setLSStatus(msg.getData().getByte("inputPort"));
                    break;                     
                case NXT.LSREAD:
                    setLSRead(msg.getData().getByte("inputPort"));
                    break;                     
                case NXT.SET_SENSOR_INPUTPORT:
                    setInputPort(msg.getData().getByte("inputPort"), 
                    		msg.getData().getByte("inputType"),
                    		msg.getData().getByte("inputMode"));
                    break;
                case DISCONNECT:
                    destroyNXTconnection();
                    break;
            }
        }

    };

}
