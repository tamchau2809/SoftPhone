package chau.vpphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.sip.*;
import android.os.SystemClock;

/**
 * Listens for incoming SIP calls, intercepts and hands them off to WalkieTalkieActivity.
 */
public class IncomingCallReceiver extends BroadcastReceiver {
    /**
     * Processes the incoming call, answers it, and hands it over to the
     * WalkieTalkieActivity.
     * @param context The context under which the receiver is running.
     * @param intent The intent being received.
     */
	
    static SipAudioCall incomingCall = null;
    static long timeStart, timeStop;
	
    @Override
    public void onReceive(Context context, Intent intent) {
        try {

            SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                @Override
                public void onRinging(SipAudioCall call, SipProfile caller) {
                    try {
                        call.answerCall(30);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    timeStart = SystemClock.elapsedRealtime();
                }
                
                @Override
                public void onCallEnded(SipAudioCall call)
                {
                	IncomingSO.missed = false;
                	timeStop = SystemClock.elapsedRealtime();
                	IncomingSO.AntiMage();
                }
            };

            PhoneCallActivity wtActivity = (PhoneCallActivity) context;

            incomingCall = PhoneCallActivity.sipManager.takeAudioCall(intent, listener);
//            incomingCall.answerCall(30);
//            incomingCall.startAudio();
//            incomingCall.setSpeakerMode(true);

//            PhoneCallActivity.call = incomingCall;
//
//            wtActivity.updateStatus(incomingCall);
            
            IncomingSO.incomingCall = incomingCall;
            Intent i = new Intent();
            i.setClassName("chau.vpphone", "chau.vpphone.IncomingSO");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);

        } catch (Exception e) {
            if (incomingCall != null) {
                incomingCall.close();
            }
        }
    }
    
    public static void answerIncomingCall() {

        try {
            incomingCall.answerCall(30);
            incomingCall.startAudio();

            if (incomingCall.isMuted()) {
                incomingCall.toggleMute();
            }
        }

        catch (Exception e) {

            System.out.println(e.toString());
        }

    }
}


//package chau.vpphone;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//
//public class IncomingCallReceiver extends BroadcastReceiver
//{
//	@Override
//	public void onReceive(Context context, Intent intent) 
//	{
////		Intent i = new Intent();
////		i.setClassName("chau.vpphone", "chau.vpphone.IncomingSO");
//		Intent i = new Intent(context, IncomingSO.class);
//		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		context.startActivity(i);
//	}
//}

////------------------------------------------------
////package chau.vpphone;
////
////import java.io.File;
////import java.io.FileOutputStream;
////import java.io.ObjectOutputStream;
////import java.text.SimpleDateFormat;
////import java.util.Date;
////
////import android.annotation.SuppressLint;
////import android.app.Notification;
////import android.app.PendingIntent;
////import android.content.BroadcastReceiver;
////import android.content.Context;
////import android.content.Intent;
////import android.media.AudioManager;
////import android.net.sip.SipAudioCall;
////import android.net.sip.SipAudioCall.Listener;
////import android.net.sip.SipProfile;
////import android.os.SystemClock;
////import android.util.Log;
////import android.widget.Toast;
////
////public class IncomingCallReceiver extends BroadcastReceiver{
////
////	FileOutputStream fos;
////	ObjectOutputStream oos;
////	File file;
////	static boolean callHeld=true;
////	static boolean flag;
////	static boolean accept = false;
////	static boolean decline;
////	boolean missed = false;
////	
////	String peerAddress;
////	
////	SipAudioCall.Listener listener;
////	
////	@Override
////	public void onReceive(final Context context, Intent intent) {
////		// TODO Auto-generated method stub
////		SipAudioCall incomingCall = null;
////		try
////		{
////			listener = new Listener()
////			{
////				@SuppressWarnings("deprecation")
////				@SuppressLint("SimpleDateFormat")
////				@Override
////				public void onRinging(SipAudioCall call, SipProfile caller)
////				{
//////					Toast.makeText(context.getApplicationContext(), "Ring Ring", Toast.LENGTH_SHORT).show();
////					try
////					{
////						if(accept)
////						{
////							call.answerCall(30);
////							call.startAudio();
////							call.setSpeakerMode(true);
////							
////							PhoneCallActivity.call = call;
////							
////							PhoneCallActivity.startTime = SystemClock.elapsedRealtime();
////							SimpleDateFormat sdf = new SimpleDateFormat("H:mmaa    EEEE, dd MM, yyyy");
////							PhoneCallActivity.callDate = sdf.format(new Date());
////							PhoneCallActivity.sipAddress = call.getPeerProfile().getUriString();
////							PhoneCallActivity.outgoingCall = false;
////							
////							long when = System.currentTimeMillis();
////							
////							Intent notiIntent = new Intent(context.getApplicationContext(), PhoneCallActivity.class);
////							notiIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
////							PendingIntent contentIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, notiIntent, 0);
////							
////							Notification noti = new Notification(R.drawable.ic_launcher, "", when);
////							noti.setLatestEventInfo(context.getApplicationContext(), "VPhone", "Return to ongoing call", contentIntent);
////							noti.flags |= Notification.FLAG_ONGOING_EVENT;
////							noti.flags |= Notification.FLAG_NO_CLEAR;
////							mNotificationManager.notify(PhoneCallActivity.HELLO_ID, noti);
////							
////							accept = false;
////						}
////					}
////					catch (Exception e)
////					{
////						e.printStackTrace();
////					}
////				}
////				
////				@SuppressLint("SdCardPath")
////				@Override
////				public void onCallEnded(SipAudioCall call)
////				{
////					try
////					{
////						PhoneCallActivity.stopTime = SystemClock.elapsedRealtime();
////						long miliSecs = PhoneCallActivity.stopTime - PhoneCallActivity.startTime;
////						int secs = (int)((miliSecs / 1000) % 60);
////						int mins = (int)((miliSecs / (1000*60)) % 60);
////						int hours = (int)((miliSecs / (1000*60*60)));
////						StringBuilder sb = new StringBuilder(64);
////						if(hours > 0)
////						{
////							sb.append(hours);
////							sb.append(" hrs ");
////						}
////						sb.append(mins);
////						sb.append(" mins ");
////						sb.append(secs);
////						sb.append(" secs ");
////						PhoneCallActivity.callDuration = sb.toString();
////						PhoneCallActivity.startTime = 0;
////						HistoryInfo hInfo = new HistoryInfo(PhoneCallActivity.sipAddress.substring(4), PhoneCallActivity.callDate, PhoneCallActivity.callDuration, PhoneCallActivity.outgoingCall, false);
////						
////						AudioManager am = (AudioManager)context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
////						am.setMode(AudioManager.MODE_NORMAL);
////						am.setSpeakerphoneOn(false);
////						
////						file = new File("/data/data/chau.vpphone/files/" + PhoneCallActivity.FILENAME);
////						if(!file.exists())
////						{
////							fos = context.getApplicationContext().openFileOutput(PhoneCallActivity.FILENAME, Context.MODE_APPEND);
////							oos = new ObjectOutputStream(fos);
////						}
////						else
////						{
////							fos = context.getApplicationContext().openFileOutput(PhoneCallActivity.FILENAME, Context.MODE_APPEND);
////							oos = new AppendableObjectOutputStream(fos);
////						}
////						oos.writeObject(hInfo);
////						oos.flush();
////						fos.close();
////						oos.close();
////					}
////					catch(Exception e)
////					{
////						Toast toast=Toast.makeText(context, "Unable to enter data to file "+e, Toast.LENGTH_LONG);
////                        toast.show();
////					}
////				}
////			};
////			
//////			PhoneCallActivity cActivity = (PhoneCallActivity)context;
//////			incomingCall = cActivity.sipManager.takeAudioCall(intent, listener);
//////			//cActivity = PhonecallActivity
////////			incomingCall.answerCall(30);
//////			incomingCall.startAudio();
//////			incomingCall.setSpeakerMode(true);
////////			if(incomingCall.isMuted())
////////			{
////////				incomingCall.toggleMute();
////////			}
//////			PhoneCallActivity.call = incomingCall; //luc dau la PhoneCallActivity
//////			cActivity.updateStatus(incomingCall);
////		}
////		catch(Exception e)
////		{
////			if(incomingCall != null)
////			{
////				incomingCall.close();
////			}
////		}
////	}
////	
////	public void logMissedCall(Context ctx)
////	{
////		try
////		{
////			int seconds = 0;
////    		int minutes = 0;
////    		StringBuilder sb = new StringBuilder(64);
////    		sb.append(minutes);
////    		sb.append(" mins ");
////    		sb.append(seconds);
////    		sb.append(" secs");
////    		PhoneCallActivity.callDuration=sb.toString();
////    		SimpleDateFormat sdf = new SimpleDateFormat("H:mmaa   EEEE, MMMM d, yyyy");
////    		Log.d("creating callinfo", "i m here");
////    		HistoryInfo cinfo=new HistoryInfo(peerAddress,sdf.format(new Date()),PhoneCallActivity.callDuration,PhoneCallActivity.outgoingCall,true);
////    		Log.d("creating callinfo done", "i m here");
////    	
////    		AudioManager am;
////    		am = (AudioManager) ctx.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
////    		am.setMode(AudioManager.MODE_NORMAL);
////    		am.setSpeakerphoneOn(false);
////    	
////    		file=new File("/data/data/chau.vpphone/files/"+PhoneCallActivity.FILENAME);
////    		if(!file.exists())
////    		{
////    			fos = ctx.getApplicationContext().openFileOutput(PhoneCallActivity.FILENAME, Context.MODE_APPEND);
////    			oos=new ObjectOutputStream(fos);
////    		}
////    		else
////    		{
////    			fos = ctx.getApplicationContext().openFileOutput(PhoneCallActivity.FILENAME, Context.MODE_APPEND);
////    			oos=new AppendableObjectOutputStream(fos);
////    		}
////    		oos.writeObject(cinfo);
////    		oos.flush();
////    		fos.close();
////    		oos.close();
////		}
////		catch(Exception e)
////		{
////			Toast toast=Toast.makeText(ctx.getApplicationContext(), "Unable to enter data to file "+e, Toast.LENGTH_LONG);
////			toast.show();
////		}
////	}
////	
//////	private Runnable myRunnable = new Runnable() {
//////	    public void run() {
////////	    	incomingCall.setListener(listener, true);
//////	    	try{
//////	    		sendUpdateBroadcast("Call Missed");
//////	    		am.setMode(AudioManager.MODE_NORMAL);
//////	    		missed=true;
//////	    		incomingCall.endCall();
//////	    		logMissedCall(Con);
////////	    		IncomingCallActivity.this.finish();
//////	    	}
//////	    	catch(Exception e)
//////	    	{}
//////	        finish();
//////	    }
//////	};
////}
