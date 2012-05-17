package fhnw.emoba;

import com.bt.DeviceListActivity;

import fhnw.emoba.ControlView.ControlThread;
import fhnw.emoba.R;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class MADStromActivity extends Activity {
	//Flag indicating if application is running in a emulator
	//emulator implicates no Bluetooth
	private boolean onEmulator;
	/** Reference to the main layout in connect-view */
	LinearLayout mConnectView;
	/** Reference to the view flipper in the main layout 8 */
	ViewFlipper mFlipper;
	/** A handle to the thread that's actually running the animation. */
	private ControlView mControlView;
	/** A handle to the View in which the game is running. */
	private ControlThread mControlThread;
	/** Bluetooth adapter to check if device supports bluetooth*/
	private BluetoothAdapter mBluetoothAdapter;
    /** Intent request codes */
    private static final int REQUEST_CONNECT_DEVICE = 1;
    /** Intent request codes for enabling bluetooth */
	private static final int REQUEST_ENABLE_BT = 2;
	/** TAG for logging*/
	private static final String TAG = MADStromActivity.class.getSimpleName();
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//set initial View to the connection view
		setContentView(R.layout.connectview);
		//creation of Views to accelerate Content-Switching
		mConnectView = (LinearLayout)findViewById(R.id.connect_view_main);
		//check if application is running on a avd
		//bluetooth is not available on a avd
		onEmulator = "sdk".equals(Build.PRODUCT);


	}
	
	

    /**
     * Invoked when the Activity loses user focus.
     */
	@Override
	protected void onPause() {
		super.onPause();
		if (mControlView.getThread().isAlive()){
			mControlView.getThread().pause();
			mControlView.getThread().interrupt();
		}
	}
	



	/**
	 * Called just before the activity becomes visible to the user.
	 * Followed by onResume() if the activity comes to the foreground, or onStop() if it becomes hidden.
	 */
	@Override
	protected void onStart() {
		// TODO Mechanism to turn on bluetooth
		super.onStart();
		//Register connect button on Activity
		((Button)findViewById(R.id.connect_button)).setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (onEmulator){
					//TODO: Switch to ControlView
					onActivityResult(REQUEST_CONNECT_DEVICE, Activity.RESULT_OK, null);
				}else{
					//TODO Start new activity to connect to paired bluetooth device
					Intent btSelectDevice = new Intent(MADStromActivity.this, DeviceListActivity.class);
					startActivityForResult(btSelectDevice, REQUEST_CONNECT_DEVICE);
				}

			}
		});
		//add ControlView to Viewflipper
		mFlipper = (ViewFlipper)findViewById(R.id.flipper);
		mFlipper.addView(buildControlViewLayout());
		mControlThread = mControlView.getThread();
	}
	
	/**
	 * Called just before the activity starts interacting with the user. 
	 * At this point the activity is at the top of the activity stack, with user input going to it.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (!onEmulator){
			//Emulator does not support bluetoothSetup
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (mBluetoothAdapter == null){
				//TODO: Device does not support bluetooth
			}else{
				//only turn on bluetooth if its not already turned on
				if (!mBluetoothAdapter.isEnabled()){
					//TODO: start activity to enable bluetooth
					Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);					
				}
			}
		}
	}



	/**
	 * Builds the ControlView by combining the SurfaceView and the action button in a LinearLayout
	 * @return LinearLayout consisting of the surfaceView and the action button at the bottom
	 */
	private View buildControlViewLayout(){
		//sets up the LinearLayout with the paramters
		LinearLayout controlViewLayout = new LinearLayout(this);
		controlViewLayout.setOrientation(LinearLayout.VERTICAL);
		//adding the SurfaceView to the controlViewLayout
		mControlView = new ControlView(getApplicationContext(), null);
		LinearLayout.LayoutParams ControlViewParams = new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.FILL_PARENT, 1.0F);
		controlViewLayout.setLayoutParams(ControlViewParams);
		//Building the ControlView, adding the action button to the ControlViewLayout
		controlViewLayout.addView(mControlView,ControlViewParams);
		Button actionButton = new Button(this);
		actionButton.setText(R.string.action_button);
		actionButton.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO: Real Implementation
				Toast.makeText(MADStromActivity.this, "Action", Toast.LENGTH_SHORT).show();
			}
		});
		LinearLayout.LayoutParams actionButtonParams = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		controlViewLayout.addView(actionButton, actionButtonParams);
		return controlViewLayout;
	}



	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onStop() {
		super.onStop();
		// TODO: Disable bluetooth
		if (BluetoothAdapter.getDefaultAdapter().isEnabled()){
			Log.v(TAG, "Turning off bluetooth");
			mBluetoothAdapter.disable();
		}
	}



	/**
     * Notification that something is about to happen, to give the Activity a
     * chance to save state.
     * 
     * @param outState a Bundle into which this Activity should save its state
     */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}



	/**
	 * Handles the result returned by the device list activity. */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_ENABLE_BT:
			//TODO: Show Dialog to enable bluetooth
			if (resultCode == Activity.RESULT_OK){
				Log.v(TAG, "Bluetooth has been enabled");
			}else {
				Log.v(TAG, "Bluetooth has not been enabled");
			}
			break;
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect if (resultCode == Activity.RESULT_CANCELED) {
			// do nothing
			if( resultCode == Activity.RESULT_CANCELED){
				//do nothing
			} else if (resultCode == Activity.RESULT_OK) {
				if (onEmulator) { setEmulationSetup();
				} else {
					//TODO Connect to Bluetooth Channel with  received device MAC address:
					// When DeviceListActivity returns with a device to connect
		            // Get the device MAC address
		           String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		           Log.v(TAG, "Received Device Address "+ address);
		           Toast.makeText(this, "Received Device Address "+address, Toast.LENGTH_SHORT).show();
				}
				//flips to connect view
				mFlipper.showNext();
				// TODO: initiate NXT Lego Brick and start the robot				
			}
			break;
		}
	}

	private void setEmulationSetup() {
		// TODO ASK Luthiger what this method actually should do
		
		
		
	}

	/**
	 * Overriding the back-button so it doesn't kill the application
	 * as there is only one activity and the back-button would kill
	 * the activity with a call to finish()
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK){
			Log.v(TAG, "Back button has been pressed.. switching back to ConnectView");
			Log.v(TAG, Integer.toString(mFlipper.getDisplayedChild()));
			if (mFlipper.getDisplayedChild() == 1){
				mFlipper.showPrevious();
				Toast.makeText(this, "Press back again to quit", Toast.LENGTH_LONG);
				return true;
			}else{
				//finish
				mControlThread.pause();
				finish();
			}
		}
		return super.onKeyDown(keyCode, event);
	}
		
	
}
