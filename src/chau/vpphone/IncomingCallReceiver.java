package chau.vpphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipAudioCall;
import android.net.sip.SipAudioCall.Listener;
import android.net.sip.SipProfile;

public class IncomingCallReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		SipAudioCall incomingCall = null;
		try
		{
			SipAudioCall.Listener listener = new Listener()
			{
				@Override
				public void onRinging(SipAudioCall call, SipProfile caller)
				{
					try
					{
						call.answerCall(30);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			};
			
			PhoneCallActivity cActivity = (PhoneCallActivity)context;
			incomingCall = cActivity.sipManager.takeAudioCall(intent, listener);
			incomingCall.answerCall(30);
			incomingCall.startAudio();
			incomingCall.setSpeakerMode(true);
			if(incomingCall.isMuted())
			{
				incomingCall.toggleMute();
			}
			cActivity.call = incomingCall;
			cActivity.updateStatus(incomingCall);
		}
		catch(Exception e)
		{
			if(incomingCall != null)
			{
				incomingCall.close();
			}
		}
	}
}
