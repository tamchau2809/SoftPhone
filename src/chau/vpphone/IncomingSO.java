package chau.vpphone;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.sip.SipAudioCall;
import android.net.sip.SipProfile;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class IncomingSO extends Activity {
	File file;
	FileOutputStream fos;
	ObjectOutputStream oos;
	
	Button btnAccept, btnReject;
	TextView tvIncomingCompany, tvDisplayName;
	View.OnClickListener listenerAccept, listenerReject;
	
	public static boolean missed;
	static boolean accept = false;
	private Handler mHandler = new Handler();
	
	String peerAdd;
	
	private static Context ctx;
	
	AudioManager am;
	static SipAudioCall incomingCall = null;
	SipAudioCall.Listener listener;
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreate(Bundle saved)
	{
		super.onCreate(saved);
		setContentView(R.layout.activity_incomingso);
		initWiget();
		
		this.setFinishOnTouchOutside(false);

		ctx = this;
		
		initlistener();
		btnAccept.setOnClickListener(listenerAccept);
		btnReject.setOnClickListener(listenerReject);
		
		am = (AudioManager)getSystemService(AUDIO_SERVICE);
		am.setMode(AudioManager.MODE_RINGTONE);
		
		missed = false;
//		accept = false;
		try
		{
			listener = new SipAudioCall.Listener()
			{	
				@Override
				public void onCallEstablished(SipAudioCall call)
				{
					Toast.makeText(getBaseContext(), "asdasd", Toast.LENGTH_SHORT).show();
				}
				
				@Override
				public void onCallEnded(SipAudioCall call)
				{
					missed = false;
					finish();
				}
			};
//			incomingCall = PhoneCallActivity.sipManager.takeAudioCall(intent, listener);
			peerAdd = incomingCall.getPeerProfile().getUserName(); 
			tvDisplayName.setText(" (" + incomingCall.getPeerProfile().getDisplayName() + ")");
			if(peerAdd == null) peerAdd = incomingCall.getPeerProfile().getDisplayName();
			IncomingCallActivity.incomingCall = incomingCall;
			tvIncomingCompany.setText(peerAdd);
			checkTime();
			mHandler.postDelayed(myRunnable, 30000);
		}
		catch(Exception e)
		{
			
		}
	}
	
	public void initWiget()
	{
		btnAccept = (Button)findViewById(R.id.btnAccept);
		btnReject = (Button)findViewById(R.id.btnReject);
		tvIncomingCompany = (TextView)findViewById(R.id.tvIncomingCallCompany);
		tvDisplayName = (TextView)findViewById(R.id.tvDisplayName);
	}
	
	public void initlistener()
	{
		listenerAccept = new View.OnClickListener() {
			
			@SuppressLint("InlinedApi")
			@Override
			public void onClick(View v) {

				finish();
				// TODO Auto-generated method stub
				Intent i = new Intent(IncomingSO.this, IncomingCallActivity.class);
				Bundle b = new Bundle();
				b.putString("INCOM", peerAdd);
				i.putExtra("ADD", b);
				startActivity(i);
				accept = true;
			}
		};
		
		listenerReject = new View.OnClickListener()
		{
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try
				{
					mHandler.removeCallbacks(myRunnable);
					accept = false;
					am.setMode(AudioManager.MODE_NORMAL);
					incomingCall.setListener(listener, true);
					incomingCall.endCall();
					incomingCall.close();
					missedCall();
				}
				catch(Exception e)
				{}
//				missedCall();
//				finish();
				finish();
			}
		};
	}
	
	/**
	 * kiểm tra nếu người gọi cúp máy trước 30s
	 */
	public void checkTime()
	{
		if(IncomingCallReceiver.timeStop - IncomingCallReceiver.timeStart < 30000 
				&& accept == false)
		{
			missedCall();
		}
	}
	
	@Override
	public void onBackPressed()
	{
		
	}
	
	@SuppressLint({ "SimpleDateFormat", "SdCardPath" })
	public void missedCall()
	{
		Context c = getApplicationContext();
		try
		{
			int sec = 0;
			int min = 0;
			StringBuilder sb = new StringBuilder(64);
			sb.append(min);
			sb.append(" min ");
			sb.append(sec);
			sb.append(" sec ");
			PhoneCallActivity.callDuration = sb.toString();
			SimpleDateFormat sdf = new SimpleDateFormat("H:mmaa   EEEE, dd MM, yyyy");
			HistoryInfo hinfo = new HistoryInfo(peerAdd, sdf.format(new Date()), 
					PhoneCallActivity.callDuration, false, true);
			AudioManager am;
			am = (AudioManager)c.getSystemService(Context.AUDIO_SERVICE);
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
	
	/**
	 * kết thúc Dialog khi người gọi tắt máy 
	 * @param magic shall not prevail
	 */
	public static void AntiMage()
	{
		((Activity) ctx).finish();
	}
	
	private Runnable myRunnable = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try
			{
				am.setMode(AudioManager.MODE_NORMAL);
				missed = true;
				incomingCall.endCall();
				missedCall();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finish();
		}
	};
}
