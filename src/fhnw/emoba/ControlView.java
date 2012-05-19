package fhnw.emoba;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ControlView extends SurfaceView implements SurfaceHolder.Callback{
	/**
	 * Thread-Class that does the actual drawing
	 * Drawing of the Surface-View should be handled in a non-UI thread
	 * so the UI stays responsive
	 */
	class ControlThread extends Thread{
        /**
         * Current height of the surface/canvas.
         * 
         * @see #setSurfaceSize
         */
        private int mCanvasHeight = 1;

        /**
         * Current width of the surface/canvas.
         * 
         * @see #setSurfaceSize
         */
        private int mCanvasWidth = 1;
        
        /** Indicates whether the surface size has been changed*/
        private boolean mSurfaceSizeChanged = false;
        
        /** Indicate whether the surface has been created & is ready to draw */
        private volatile boolean mRunning = false;
        
		/** Handle to the surface manager object we interact with */
		private SurfaceHolder mSurfaceHolder;
		
		/** Handle to the control - point */
		private ControlPoint mControlPoint;
		
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
				
				setRunning(true);
			}
		}		
		@Override
		public void run() {
			while (mRunning){
				Canvas c = null;
				try {
					c = mSurfaceHolder.lockCanvas(null);
					synchronized(mSurfaceHolder){
						doDraw(c);
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
		public void setRunning(boolean b) {
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
    private Context mContext;
    
    /** The thread that actually draws the animation */
    private ControlThread mThread;
    
	public ControlView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		// register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        mContext = context;
        //create thread only; it's started in surfaceCreated
        mThread = new ControlThread(holder, context, new Handler());
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return mThread.doTouchEvent(event);
		
	}
	
	/**
	 * Pauses the drawing thread
	 */
	public void pause(){
		mThread.setRunning(false);
		mThread.interrupt();
	}
	
	/**
	 * start the thread here so that we don't busy-wait in run()
	 * waiting for the surface to be created
	 * @param holder
	 */
	public void restart(){
		if (!mThread.isRunning()) { 
            mThread = new ControlThread(getHolder(), mContext, new Handler());
            Log.v(MADStromActivity.TAG, "Creating new drawing Thread");
       }
		mThread.setRunning(true);
		mThread.doStart();
		mThread.start();
	}

	/* (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder, int, int, int)
	 */
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		mThread.setSurfaceSize(width, height);
		
	}

	/* (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder)
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
        // start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
		restart();
		
	}

	/* (non-Javadoc)
	 * @see android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view.SurfaceHolder)
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
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
	
	class ControlPoint {
		private PointF position;
		private Paint style;
		private final static float RADIUS = 5;
		private final static int COLOR = Color.YELLOW;
		
		
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
				setX(ControlView.this.mThread.mCanvasWidth / 2);
				setY(ControlView.this.mThread.mCanvasHeight / 2);
			}
			canvas.drawCircle(position.x, position.y, RADIUS, style);
		}
		
	}

	class HomePosition{
		private Paint style;
		private final static float RADIUS = 25;
		private final static float WIDTH = 5;
		private final static int COLOR = Color.GRAY;
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
				setmX(ControlView.this.mThread.mCanvasWidth/2);
				setmY(ControlView.this.mThread.mCanvasHeight/2);
			}
			canvas.drawCircle(mX, mY, RADIUS, style);
			
		}
		
	}
}
