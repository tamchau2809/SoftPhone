package chau.vpphone;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class RunInBackground extends Service{

	private boolean isRunning;
    private Thread backgroundThread;
    PhoneCallActivity activ;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate()
	{
		this.isRunning = false;
		this.backgroundThread = new Thread(myTask);
		activ = new PhoneCallActivity();
	}
	
	@Override
    public void onDestroy() { 
        this.isRunning = false;
    }
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!this.isRunning) {
            this.isRunning = true;
            this.backgroundThread.start();
        }
        return START_STICKY;
    }
	
	private Runnable myTask = new Runnable() {
        public void run() {
            // Do something here
        	activ.initManager();
        	System.out.println("Bla bla bla");
            stopSelf();
        }
    };

}
