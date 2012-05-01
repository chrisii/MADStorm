package fhnw.emoba;

import fhnw.emoba.ControlView.ControlThread;
import fhnw.emoba.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MADStromActivity extends Activity {
	//Flag indicating if application is running in a emulator
	//emulator implicates no Bluetooth
	private boolean onEmulator;
	
	LinearLayout mConnectView;
	
	/** A handle to the thread that's actually running the animation. */
	private ControlView mControlView;
	
	/** A handle to the View in which the game is running. */
	private ControlThread mControlThread;
	
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//set initial View to the connection view
		setContentView(R.layout.connectview);
		//creation of Views to accelerate Content-Switching
		mConnectView = (LinearLayout)findViewById(R.id.LinearLayout);
		mControlView = new ControlView(getApplicationContext(), null);
		mControlThread = mControlView.getThread();
		
		onEmulator = "sdk".equals(Build.PRODUCT);

		((Button)findViewById(R.id.connect_button)).setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if (onEmulator){
					//TODO: Switch to ControlView
					onActivityResult(REQUEST_CONNECT_DEVICE, Activity.RESULT_OK, null);
				}

			}
		});

	}
	
	

    /**
     * Invoked when the Activity loses user focus.
     */
	@Override
	protected void onPause() {
		super.onPause();
		//controlView.getThread().pause();
	}



    /**
     * Notification that something is about to happen, to give the Activity a
     * chance to save state.
     * 
     * @param outState a Bundle into which this Activity should save its state
     */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}



	/**
	 * Handles the result returned by the device list activity. */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_ENABLE_BT:
			//TODO: Show Dialog to enable bluetooth
			break;
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect if (resultCode == Activity.RESULT_CANCELED) {
			// do nothing
			if( resultCode == Activity.RESULT_CANCELED){
				//do nothing
			} else if (resultCode == Activity.RESULT_OK) {
				if (onEmulator) { setEmulationSetup();
				} else {
					//TODO: Don't know what I should do here ...
				}
				// TODO: Switch to ControllView
				mControlThread.doStart();
				mConnectView.removeAllViews();
				mConnectView.invalidate();
				LinearLayout.LayoutParams mControlViewParams = new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT, 1.0F);
				mControlView.setLayoutParams(mControlViewParams);
				mConnectView.addView(mControlView);
				
				Button actionButton = new Button(this);
				actionButton.setText(R.string.action_button);
				actionButton.setOnClickListener(new Button.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO: Real Implementation
						Toast.makeText(getApplicationContext(), "Action", Toast.LENGTH_LONG).show();
					}
				});
				mConnectView.addView(actionButton);
			}
			break;
		}
	}

	private void setEmulationSetup() {
		// TODO Auto-generated method stub
		
		
		
	}
	
	
}