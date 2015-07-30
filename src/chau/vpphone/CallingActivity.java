package chau.vpphone;

import java.text.ParseException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.net.sip.SipAudioCall.Listener;
import android.net.sip.SipManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

public class CallingActivity extends Activity {

	TextView tvCallingTo;
	public SipManager sipManager = null;
    public SipProfile profile = null;
    public SipAudioCall call = null;
    public IncomingCallReceiver callReceiver; 
    
    public String address = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calling);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.VPhone.INCOMING_CALL");
		callReceiver = new IncomingCallReceiver();
		this.registerReceiver(callReceiver, filter);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		initManager();
		
		initCall();
		
		CallToSO();
	}
	
	/*
	 * Make a call
	 */
	public void initCall()
	{
		try
		{
			SipAudioCall.Listener listener = new Listener()
			{
				@Override
				public void onCallEstablished(SipAudioCall call)
				{
					call.startAudio();
					call.setSpeakerMode(true);
					call.toggleMute();
					updateStatus(call);
				}
				
				@Override
				public void onCallEnded(SipAudioCall call)
				{
					
				}
			};
			call = sipManager.makeAudioCall(profile.getUriString(), address, listener, 30);
		}
		catch (Exception e)
		{
			if(profile != null)
			{
				try
				{
					sipManager.close(profile.getUriString());
				} catch (Exception ee)
				{
					ee.printStackTrace();
				}
			}
			if(call != null)
			{
				call.close();
			}
		}
	}
	
	public void initManager()
	{
		if(sipManager == null)
		{
			sipManager = SipManager.newInstance(this);
		}
		initProfile();
	}
	
	public void initProfile()
	{
		if(sipManager == null)
		{
			return;
		}
		if(profile != null)
		{
			closeLocalProfile();
		}
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());		
		String username = prefs.getString("namePref", "");
		String domain = prefs.getString("domainPref", "");
		String password = prefs.getString("passPref", "");
				
		try
		{
			SipProfile.Builder builder = new SipProfile.Builder(username, domain);
			builder.setPassword(password);
			profile = builder.build();
			
			Intent intent = new Intent();
			intent.setAction("android.VPhone.INCOMING_CALL");
			PendingIntent pi = PendingIntent.getBroadcast(this, 
					0, intent, Intent.FILL_IN_DATA);
			sipManager.open(profile, pi, null);
		} catch (ParseException pe) {
            updateStatus("Connection1 Error.");
        } catch (SipException se) {
            updateStatus("Connection error.");
        }
	}
	
	public void closeLocalProfile() {
        if (sipManager == null) {
            return;
        }
        try {
            if (profile != null) {
                sipManager.close(profile.getUriString());
            }
        } catch (Exception ee) {
            Log.d("WalkieTalkieActivity/onDestroy", "Failed to close local profile.", ee);
        }
    }
	
	public void updateStatus(final String status)
	{
		this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				tvCallingTo.setText(status);
			}
		});
	}
	
	public void updateStatus(SipAudioCall call)
	{
		String useName = call.getPeerProfile().getDisplayName();
		if(useName == null)
		{
			useName = call.getPeerProfile().getUserName();
		}
		updateStatus(useName + "@" + call.getPeerProfile().getSipDomain());;		
	}
	
	public void CallToSO()
	{
		tvCallingTo = (TextView)findViewById(R.id.tvCallingTo);
		Intent in = getIntent();
		Bundle b = in.getBundleExtra("NUM_EXTRA");
		address = b.getString("NUMBER");
		//updateStatus(address + "@" + call.getPeerProfile().getSipDomain());;
		tvCallingTo.setText("Calling To " + address);
	}
}
