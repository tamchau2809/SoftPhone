package chau.vpphone;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.sip.SipProfile;
import android.net.sip.SipAudioCall;
import android.net.sip.SipAudioCall.Listener;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("InlinedApi")
public class IncomingCallActivity extends Activity {
	
	FileOutputStream fos;
	ObjectOutputStream oos;
	File file;
	static boolean callHeld = true;
	static boolean flag;
	boolean accept;
	SipAudioCall incomingCall = null;
	SipAudioCall.Listener listener;
	private Handler mHandler = new Handler();
	AudioManager am;
	NotificationManager mNotificationManager;
	boolean missed = false;
	String peerAddress;
	public static String PARAM_OUT_MSG = "omsg";
	public static String PARAM_OUT_MSG2 = "omsg2";
	
	@Override
	public void onCreate(Bundle save)
	{
		super.onCreate(save);
		setContentView(R.layout.activity_incomingcall);
		
		am = (AudioManager)getSystemService(AUDIO_SERVICE);
		am.setMode(AudioManager.MODE_RINGTONE);
		
		String ns = NOTIFICATION_SERVICE;
		mNotificationManager = (NotificationManager)getSystemService(ns);
		
		missed = false;
		
		Intent intent = getIntent();
		final Context c = getApplicationContext();
		try
		{
			listener = new SipAudioCall.Listener()
			{
				@Override
				public void onCallEstablished(final SipAudioCall call)
				{
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(c, "Established", Toast.LENGTH_LONG).show();
						}
					});
					if(call != null && call.isInCall() && !callHeld)
					{
						callHeld = true;
						sendUpdateBroadcast("" + call.getPeerProfile().getUserName());
					}
					else if(call != null && call.isInCall() && callHeld)
					{
						callHeld = false;
						sendUpdateBroadcast("Call held");
						return;
					}
				}
				
				@SuppressWarnings("deprecation")
				@Override
				public void onRinging(SipAudioCall call, SipProfile caller)
				{
					try
					{
						if(accept)
						{
							runOnUiThread(new Runnable() {
								public void run() 
								{
									Toast.makeText(c, "Ring Ring Ring", Toast.LENGTH_LONG).show();
								}
							});
							call.answerCall(30);
							call.startAudio();
							call.setSpeakerMode(true);
							AudioManager am;
							am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
							am.setMode(AudioManager.MODE_IN_COMMUNICATION);
							if(PhoneCallActivity.speakerPhone)
							{
								am.setSpeakerphoneOn(true);
							}
//							if(PhoneCallActivity.walkieMode && !call.isMuted())
//							{
//								call.toggleMute();
//							}
							PhoneCallActivity.call = call;
							callHeld = false;
							PhoneCallActivity.startTime = SystemClock.elapsedRealtime();
							SimpleDateFormat sdf = new SimpleDateFormat("H:mmaa    EEEE, dd MM, yyyy");
							PhoneCallActivity.callDate = sdf.format(new Date());
							PhoneCallActivity.sipAddress = "sip:"+call.getPeerProfile().getUserName() + "@" + call.getPeerProfile().getSipDomain();
							PhoneCallActivity.outgoingCall = false;
							sendUpdateBroadcast(""+call.getPeerProfile().getUserName() + "@" + call.getPeerProfile().getSipDomain());
							long when = System.currentTimeMillis();
							
							Intent notificationIntent = new Intent(getApplicationContext(), PhoneCallActivity.class);
							notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
							PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
							
							Notification noti = new Notification(R.drawable.ic_launcher, "", when);
							noti.setLatestEventInfo(getApplicationContext(), "VPhone", "Return to ongoing call", contentIntent);
							noti.flags |= Notification.FLAG_ONGOING_EVENT;
							noti.flags |= Notification.FLAG_NO_CLEAR;
							mNotificationManager.notify(PhoneCallActivity.HELLO_ID, noti);
						}
						else
						{
							sendUpdateBroadcast("Rejected");
							missed = true;
							call.endCall();
							logMissedCall();
						}
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
				
				@Override
				public void onRingingBack(SipAudioCall call)
				{
					Toast.makeText(c, "RingingBack", Toast.LENGTH_LONG).show();
				}
				
				@Override
				public void onCalling(SipAudioCall call)
				{
					Toast.makeText(c, "Callingback", Toast.LENGTH_LONG).show();
				}
				
				@Override
				public void onError(SipAudioCall call, final int errorCode, final String errorMessage)
				{
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(c, "onError "+" "+errorCode+" "+errorMessage, Toast.LENGTH_LONG).show();
						}
					});
				}
				
				@Override
				public void onCallBusy(SipAudioCall call)
				{
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(c, "busy.. beep beep...", Toast.LENGTH_LONG).show();
						}
					});
				}
				
				@Override
				public void onCallHeld(SipAudioCall call)
				{
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(c, "onCallHeld", Toast.LENGTH_LONG).show();
						}
					});
					callHeld = false;
					sendUpdateBroadcast("Call Held");
					super.onCallHeld(call);
				}
				
				@Override
				public void onCallEnded(SipAudioCall call)
				{
					if(!missed)
					{
						sendUpdateBroadcast("Call ended by other person");
						runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(c, "onCallEnded", Toast.LENGTH_LONG).show();
							}
						});
						try
						{
							PhoneCallActivity.stopTime = SystemClock.elapsedRealtime();
							long miliSecs = PhoneCallActivity.stopTime - PhoneCallActivity.startTime;
							int secs = (int)(miliSecs / 1000) %60;
							int mins = (int)((miliSecs / (1000*60)) %60);
							int hours = (int)((miliSecs / (1000*60*60)));
							StringBuilder sb = new StringBuilder(64);
							if(hours > 0)
							{
								sb.append(hours);
								sb.append("hrs");
							}
							sb.append(mins);
							sb.append("mins");
							sb.append(secs);
							sb.append("secs");
							PhoneCallActivity.callDuration = sb.toString();
							PhoneCallActivity.startTime = 0;
							HistoryInfo hInfo = new HistoryInfo(PhoneCallActivity.sipAddress.substring(4), 
									PhoneCallActivity.callDate, PhoneCallActivity.callDuration, PhoneCallActivity.outgoingCall, false);
							AudioManager am;
							am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
							am.setMode(AudioManager.MODE_NORMAL);
							am.setSpeakerphoneOn(false);
							
							file = new File("/data/data/chau.vpphone/files/" + PhoneCallActivity.FILENAME);
							if(!file.exists())
							{
								fos = openFileOutput(PhoneCallActivity.FILENAME, Context.MODE_APPEND);
								oos = new ObjectOutputStream(fos);
							}
							else
							{
								fos = openFileOutput(PhoneCallActivity.FILENAME, Context.MODE_APPEND);
								oos = new AppendableObjectOutputStream(fos);
							}
						}
						catch(Exception e)
						{
							Toast toast=Toast.makeText(c, "Unable to enter data to file "+e, Toast.LENGTH_LONG);
                			toast.show();
						}
					}
					missed = false;
				}
			};
			Toast.makeText(c, "onReceive 1", Toast.LENGTH_LONG).show();
			incomingCall = PhoneCallActivity.sipManager.takeAudioCall(intent, listener);
			peerAddress=incomingCall.getPeerProfile().getUserName()+"@"+incomingCall.getPeerProfile().getSipDomain();
			TextView tv = (TextView)findViewById(R.id.tvIncomingCall);
			tv.append("\n"+incomingCall.getPeerProfile().getUserName()+"@"+incomingCall.getPeerProfile().getSipDomain());
			Toast.makeText(c, "onReceive 2", Toast.LENGTH_LONG).show();
			mHandler.postDelayed(myRunnable, 30000);
		}
		catch(Exception e)
		{
			Toast.makeText(c, "Excep" + e, Toast.LENGTH_SHORT).show();
			if(incomingCall != null)
			{
				incomingCall.close();
			}
		}
	}
	
	@Override
	public void onBackPressed()
	{
		
	}
	
	public void acceptCall(View v)
	{
		mHandler.removeCallbacks(myRunnable);
		accept = true;
		am.setMode(AudioManager.MODE_IN_COMMUNICATION);
		incomingCall.setListener(listener, true);
		Intent i = new Intent(this, PhoneCallActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
		this.finish();
	}
	
	public void rejectCall(View v)
	{
		mHandler.removeCallbacks(myRunnable);
		accept = false;
		am.setMode(AudioManager.MODE_NORMAL);
		incomingCall.setListener(listener, true);
		this.finish();
	}

	public void sendUpdateBroadcast(String text)
	{
		Intent broadcastIntent = new Intent();
    	//broadcastIntent.setAction(IncomingCallReceiver.ACTION_UPDATE);
    	broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
    	broadcastIntent.putExtra(PARAM_OUT_MSG, text);
    	sendBroadcast(broadcastIntent);
	}
	
	public void logMissedCall()
	{
		try
		{
    		int seconds = 0;
    		int minutes = 0;
    		StringBuilder sb = new StringBuilder(64);
    		sb.append(minutes);
    		sb.append(" mins ");
    		sb.append(seconds);
    		sb.append(" secs");
    		PhoneCallActivity.callDuration=sb.toString();
    		SimpleDateFormat sdf = new SimpleDateFormat("H:mmaa   EEEE, MMMM d, yyyy");
    		Log.d("creating callinfo", "i m here");
    		HistoryInfo hinfo=new HistoryInfo(peerAddress,sdf.format(new Date()), PhoneCallActivity.callDuration, PhoneCallActivity.outgoingCall,true);
    		Log.d("creating callinfo done", "i m here");
    	
    		AudioManager am;
    		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    		am.setMode(AudioManager.MODE_NORMAL);
    		am.setSpeakerphoneOn(false);
    	
    		file=new File("/data/data/chau.vpphone/files/"+PhoneCallActivity.FILENAME);
    		if(!file.exists())
    		{
    			fos = openFileOutput(PhoneCallActivity.FILENAME, Context.MODE_APPEND);
    			oos=new ObjectOutputStream(fos);
    		}
    		else
    		{
    			fos = openFileOutput(PhoneCallActivity.FILENAME, Context.MODE_APPEND);
    			oos=new AppendableObjectOutputStream(fos);
    		}
    		oos.writeObject(hinfo);
    		oos.flush();
    		fos.close();
    		oos.close();
		}
		catch(Exception e)
		{
			Toast toast=Toast.makeText(getApplicationContext(), "Unable to enter data to file "+e, Toast.LENGTH_LONG);
			toast.show();
		}
	}
	
	private Runnable myRunnable = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try
			{
				sendUpdateBroadcast("Call Missed");
				am.setMode(AudioManager.MODE_NORMAL);
				missed = true;
				incomingCall.endCall();
				logMissedCall();
			}
			catch(Exception e)
			{}
			finish();
		}
	};
}
