package fhnw.emoba;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class ControlView extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener{
	/**
	 * Thread-Class that does the actual drawing
	 * Drawing of the Surface-View should be handled in a non-UI thread
	 * so the UI stays responsive
	 */
	class ControlThread extends Thread{
        
        /** Indicates whether the surface size has been changed*/
        private boolean mSurfaceSizeChanged = false;
        
        /** Indicate whether the surface has been created & is ready to draw */
        private volatile boolean mRunning = false;
        
		/** Handle to the surface manager object we interact with */
		private SurfaceHolder mSurfaceHolder;
		
		/** Handle to the control - point */
		private volatile ControlPoint mControlPoint;
		
		/** Handle to the home-position */
		private HomePosition mHomePosition;
		
		
		public ControlThread(SurfaceHolder surfaceHolder, Context context,
                Handler handler){
			mSurfaceHolder = surfaceHolder;
			mControlPoint = new ControlPoint();
			mHomePosition = new HomePosition();
		}

		public void doStart(){
			synchronized (mSurfaceHolder) {
				//adjust starting position
				mControlPoint.setX(mCanvasWidth/2);
				mControlPoint.setY(mCanvasHeight/2);

				mHomePosition.setmX(mCanvasWidth/2);
				mHomePosition.setmY(mCanvasHeight/2);
				
			}
		}
		
		@Override
		public void run() {
			while (mRunning){
				Canvas c = null;
				try {
					c = mSurfaceHolder.lockCanvas(null);
					synchronized(mSurfaceHolder){
					//prevents null-pointer exception in rare cases
					if (c!=null) doDraw(c);
						mSurfaceSizeChanged = false;					
					}
				}finally{
					// do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
				}
			}
		}
		
		public Bundle saveState(Bundle map){
			synchronized (mSurfaceHolder) {
				if (map != null){
					//TODO: values to save
				}
			}
			return map;
		}
		

		/**
		 * Used to signal the thread whether it should be running or not.
		 * Passing true allows the thread to run; passing false will shut it
		 * down if it's already running. Calling start() after this was most
		 * recently called with false will result in an immediate shutdown.
		 * 
		 * @param b true to run, false to shut down
		 */
		public synchronized void setRunning(boolean b) {
			mRunning = b;
		}
		
		public boolean isRunning(){
			return mRunning;
		}
		/* Callback invoked when the surface dimensions change. */
		public void setSurfaceSize(int width, int height) {
			// synchronized to make sure these all change atomically
			synchronized (mSurfaceHolder) {
				mCanvasWidth = width;
				mCanvasHeight = height;
				mSurfaceSizeChanged = true;
			}
		}

		public boolean doTouchEvent(MotionEvent event){
			mControlPoint.setX(event.getX());
			mControlPoint.setY(event.getY());
			return true;
		}
		
		/**
         * Draws the Home and Control Point
         */
		private void doDraw(Canvas canvas){
			//Draw the Control Point
			canvas.drawColor(Color.BLACK);
			mControlPoint.draw(canvas);
			mHomePosition.draw(canvas);
		}
        
	}
	
    /** Handle to the application context, used to e.g. fetch drawables. */
    private MADStromActivity mContext;
    
    /** The thread that actually draws the animation */
    private ControlThread mThread;
    
    /** Current height of the surface/canvas. */
    private int mCanvasHeight = 1;

    /** Current width of the surface/canvas. */
    private int mCanvasWidth = 1;
    
    /** Manager provides access to sensor */
    private final SensorManager mSensorManager;
    
    /** Sensor represents the actual sensor we get our data from */
    private final Sensor mAccelerator;
    
	public ControlView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		mAccelerator = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		// register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        mContext = (MADStromActivity) context;       
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return mThread.doTouchEvent(event);
		
	}
	
	
	/**
	 * Incase application switches to the ControlView for a second time
	 * e.g. second connection attempt: this method ensures that
	 * drawing thread is recreated, call to pause() is not necessary
	 */
	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		Log.v(MADStromActivity.TAG, "onVisibilityChanged");
		if (visibility == View.VISIBLE){
			//register Listener to receive events from sensor when ControlView is visible
			mSensorManager.registerListener(this, mAccelerator, SensorManager.SENSOR_DELAY_NORMAL);
			if (mThread != null && !mThread.isAlive()){
	            mThread = new ControlThread(getHolder(), mContext, new Handler());
	            mThread.setSurfaceSize(mCanvasWidth, mCanvasHeight);
	            Log.v(MADStromActivity.TAG, "Creating new drawing Thread");
	            mThread.setRunning(true);
	            mThread.doStart();
				Log.v(MADStromActivity.TAG, "Starting Drawing Thread");
				mThread.start();
			}
		}else{
			if (mThread!=null){
				mThread.setRunning(false);
				mThread.interrupt();
			}
			//unregister Listener to receive events from sensor when ControlView is visible
			mSensorManager.unregisterListener(this);
		}
	}

	/* (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder, int, int, int)
	 */
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		mCanvasWidth = width;
		mCanvasHeight = height;
		mThread.setSurfaceSize(width, height);		
	}

	/* (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder)
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
        // start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
		//create thread only; it's started in surfaceCreated
        mThread = new ControlThread(holder, mContext, new Handler());
		Log.v(MADStromActivity.TAG, "SurfaceView Created");
		Log.v(MADStromActivity.TAG, mThread.isRunning() ? "Thread is running" : "Thread is not running");
		if (!mThread.isAlive()){
			Log.v(MADStromActivity.TAG, "Starting drawing Thread");
			mThread.setRunning(true);
			mThread.doStart();
			mThread.start();	
		}
		
	}

	/* (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view.SurfaceHolder)
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		//unregister Listener to receive events from sensor when ControlView is visible
		mSensorManager.unregisterListener(this);
	    // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        mThread.setRunning(false);
        while (retry) {
            try {
                mThread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
        Log.d("ControlThread", Boolean.toString(mThread.isAlive()));
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Maybe necessary to increase the resolution
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float x = event.values[0];
		float y  = event.values[1];
		//calculate new coordinates of controlpoint
		if (mThread!=null){
			//calculates the new position of the control point based on the input
			//from the accelerometer
			//the values from the accelerometer range
			//x-axis: -10 (left) to 10 (right)
			//y-axis: 10 (bottom) to -10 (top)
			//the division by (2*10) divides the size of the canvas in small sections
			//each section corresponds to 1 step for the accelerometer (-10 to 10) range of 20
			float newY = mThread.mHomePosition.getmY() + y * (mCanvasHeight/(2*10));
			float newX = mThread.mHomePosition.getmX() - x * (mCanvasWidth/(2*10));
			mThread.mControlPoint.setY(newY);
			mThread.mControlPoint.setX(newX);
			//in case the control point is near the home position slow down the robot
			if (Math.abs(mThread.mControlPoint.getX() - mThread.mHomePosition.getmX()) < HomePosition.RADIUS
					&& Math.abs(mThread.mControlPoint.getY() - mThread.mHomePosition.getmY()) < HomePosition.RADIUS){
				mContext.setVelocity(0, 0);
			}else{
				mContext.setVelocity(x/10, -(y/10));				
			}
		}
		
	}

	class ControlPoint {
		private volatile PointF position;
		private Paint style;
		public final static float RADIUS = 5;
		public final static int COLOR = Color.YELLOW;
		
		
		public ControlPoint() {
			this.position = new PointF(-1, -1);
			style = new Paint();
			style.setColor(COLOR);
			style.setStyle(Paint.Style.FILL);
		}

		public ControlPoint(PointF position) {
			this();
			this.position = position;
		}
		
		public ControlPoint (float x, float y){
			super();
			this.position = new PointF(x, y);
		}

		/**
		 * @return the position
		 */
		public PointF getPosition() {
			return position;
		}

		/**
		 * @param position the position to set
		 */
		public void setPosition(PointF position) {
			this.position = position;
		}
		
		public float getX(){
			return position.x;
		}
		
		public float getY(){
			return position.y;
		}
		
		/**
		 * 
		 * @param x the x coordinate to set
		 */
		public void setX(float x){
			this.position.x = x;
		}

		/**
		 * 
		 * @param y the y coordinate to set
		 */
		public void setY(float y){
			this.position.y = y;
		}
		/**
		 * draws the control point
		 * @param canvas where the control point should be drawn
		 */
		private void draw(Canvas canvas) {
			if (ControlView.this.mThread.mSurfaceSizeChanged) {
				setX(ControlView.this.mCanvasWidth / 2);
				setY(ControlView.this.mCanvasHeight / 2);
			}
			canvas.drawCircle(position.x, position.y, RADIUS, style);
		}
		
	}

	class HomePosition{
		private Paint style;
		public final static float RADIUS = 25;
		public final static float WIDTH = 5;
		public final static int COLOR = Color.GRAY;
		private float mX;
		private float mY;
		
		public HomePosition(){
			mX = -1;
			mY = -1;
			style = new Paint();
			style.setStrokeWidth(WIDTH);
			style.setStyle(Paint.Style.STROKE);
			style.setColor(COLOR);
		}

		/**
		 * @return the mX
		 */
		public float getmX() {
			return mX;
		}

		/**
		 * @param mX the mX to set
		 */
		public void setmX(float mX) {
			this.mX = mX;
		}

		/**
		 * @return the mY
		 */
		public float getmY() {
			return mY;
		}

		/**
		 * @param mY the mY to set
		 */
		public void setmY(float mY) {
			this.mY = mY;
		}
		/**
		 * draws the home point
		 * @param canvas where the home point should be drawn
		 */
		private void draw(Canvas canvas){
			if (ControlView.this.mThread.mSurfaceSizeChanged){
				setmX(ControlView.this.mCanvasWidth/2);
				setmY(ControlView.this.mCanvasHeight/2);
			}
			canvas.drawCircle(mX, mY, RADIUS, style);
			
		}
		
	}
}
