package chau.vpphone;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipAudioCall;

public class IncomingCallReceiver extends BroadcastReceiver
{
	FileOutputStream fos;
	ObjectOutputStream oos;
	File file;
	static boolean callHeld=true;
	static boolean flag;
	SipAudioCall incomingCall = null;
	public static final String ACTION_UPDATE = "chau.VPhone.UPDATE";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		PhoneCallActivity pcActivity = (PhoneCallActivity) context;
		String text = intent.getStringExtra(InComingCallActivity)
	}
	
}

//--------------------------------------------------------------------------
//package chau.vpphone;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.net.sip.SipAudioCall;
//import android.net.sip.SipAudioCall.Listener;
//import android.net.sip.SipProfile;
//
//public class IncomingCallReceiver extends BroadcastReceiver{
//
//	@Override
//	public void onReceive(Context context, Intent intent) {
//		// TODO Auto-generated method stub
//		SipAudioCall incomingCall = null;
//		try
//		{
//			SipAudioCall.Listener listener = new Listener()
//			{
//				@Override
//				public void onRinging(SipAudioCall call, SipProfile caller)
//				{
//					try
//					{
//						call.answerCall(15);
//					}
//					catch (Exception e)
//					{
//						e.printStackTrace();
//					}
//				}
//			};
//			
//			PhoneCallActivity cActivity = (PhoneCallActivity)context;
//			incomingCall = cActivity.sipManager.takeAudioCall(intent, listener);
//			//cActivity = PhonecallActivity
//			incomingCall.answerCall(30);
//			incomingCall.startAudio();
//			incomingCall.setSpeakerMode(true);
//			if(incomingCall.isMuted())
//			{
//				incomingCall.toggleMute();
//			}
//			PhoneCallActivity.call = incomingCall; //luc dau la PhoneCallActivity
//			cActivity.updateStatus(incomingCall);
//		}
//		catch(Exception e)
//		{
//			if(incomingCall != null)
//			{
//				incomingCall.close();
//			}
//		}
//	}
//}
