package chau.vpphone;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.sip.SipAudioCall;
import android.net.sip.SipProfile;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class OnCallingActivity extends Activity implements OnClickListener{

	FileOutputStream fos;
	ObjectOutputStream oos;
	File file;
	SipAudioCall call = null;
	
	Button btn1, btn2, btn3, btn4, btn5,
	btn6, btn7, btn8, btn9, btn0,
	btnStar, btnSharp;
	EditText edTransfer;
	
	TextView tvTime;
	ImageButton btnHold, btnSpeaker, btnEndCall;
	View.OnClickListener listenerHold, listenerSpeaker, listenerEndcall;
	
	boolean holdIsPressed = true;
	boolean speakerIsPressed = true;
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_on_calling);
		
		initWiget();
		initListener();
		this.setFinishOnTouchOutside(false);
		
		btnHold.setOnClickListener(listenerHold);
		btnSpeaker.setOnClickListener(listenerSpeaker);
		btnEndCall.setOnClickListener(listenerEndcall);
		
		final Context ctx = getApplicationContext();
		
		try
		{
			PhoneCallActivity.listener = new SipAudioCall.Listener()
			{
				@SuppressWarnings("deprecation")
				@SuppressLint("SimpleDateFormat")
				@Override
				public void onCallEstablished(SipAudioCall call)
				{
					if(call != null && call.isInCall())
					{
						if(!PhoneCallActivity.callHeld)
						{
							PhoneCallActivity.callHeld = true;
							Toast.makeText(ctx, "Held", Toast.LENGTH_SHORT).show();
							return;
						}
						else
						{
							PhoneCallActivity.callHeld = false;
							return;
						}
					}
					PhoneCallActivity.startTime = SystemClock.elapsedRealtime();
					SimpleDateFormat sdf = new SimpleDateFormat("H:mmaa   EEEE, dd MM, yyyy");
					PhoneCallActivity.callDate = sdf.format(new Date());
					PhoneCallActivity.outgoingCall = true;
					PhoneCallActivity.callHeld = false;
					call.startAudio();
//					call.setSpeakerMode(true);
					call.setSpeakerMode(false);
					if(PhoneCallActivity.speakerPhone)
					{
						PhoneCallActivity.am = (AudioManager)ctx.getSystemService(Context.AUDIO_SERVICE);
						PhoneCallActivity.am.setSpeakerphoneOn(true);
					}
					PhoneCallActivity.am.setMode(AudioManager.MODE_IN_COMMUNICATION);
					Toast.makeText(ctx, "Calling..", Toast.LENGTH_SHORT).show();
					
					int icon = R.drawable.ic_launcher;
                    long when = System.currentTimeMillis();
                    CharSequence contentTitle = "VPhone";
                    CharSequence contentText = "Return to ongoing call";
                    
                    Intent notificationIntent = new Intent(ctx,PhoneCallActivity.class);
                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, notificationIntent, 0);
                    
                    Notification notification = new Notification(icon, "", when);
                    notification.setLatestEventInfo(ctx, contentTitle, contentText, contentIntent);
                    notification.flags |= Notification.FLAG_ONGOING_EVENT;
                    notification.flags |= Notification.FLAG_NO_CLEAR;
                    
//                    mNotificationManager.notify(HELLO_ID, notification);
				}
				
				@Override
				public void onCallHeld(SipAudioCall call)
				{
					PhoneCallActivity.callHeld = true;
					Toast.makeText(ctx, "Held..", Toast.LENGTH_LONG).show();
					super.onCallHeld(call);
				}
				
				@Override
				public void onCalling(SipAudioCall call)
				{
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(getApplicationContext(), "Calling to " + PhoneCallActivity.currentInputNum, Toast.LENGTH_LONG).show();
						}
					});
				}
				
				@Override
				public void onRinging(SipAudioCall call, SipProfile caller)
				{
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(getApplicationContext(), "Ring Ring..", Toast.LENGTH_SHORT).show();
						}
					});
				}
				
				@Override
				public void onError(SipAudioCall call, final int errorCode, final String message)
				{
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(getApplicationContext(), 
									"Error: " + errorCode + " " + message, 
									Toast.LENGTH_SHORT).show();
						}
					});
					finish();
				}
				
				@SuppressLint("SdCardPath")
				@Override
				public void onCallEnded(SipAudioCall call)
				{
					try
					{
						PhoneCallActivity.speakerPhone = false;
						PhoneCallActivity.stopTime = SystemClock.elapsedRealtime();
						long miliSecs = PhoneCallActivity.stopTime - PhoneCallActivity.startTime;
						int secs = (int)(miliSecs / 1000)%60;
						int mins = (int)((miliSecs / (1000*60))%60);
						int hrs = (int)((miliSecs / (1000*60*60)));
						StringBuilder builder = new StringBuilder(64);
						if(hrs > 0)
						{
							builder.append(hrs);
							if(hrs > 1)
								builder.append(" hrs ");
							else builder.append("hr");								
						}
						builder.append(mins);
						if(mins > 1) builder.append(" mins ");
						else builder.append(" min ");
						builder.append(secs);
						if(secs > 1) builder.append(" secs ");
						else builder.append(" sec ");
						PhoneCallActivity.callDuration = builder.toString();
						PhoneCallActivity.startTime = 0;
						//-----------------------
						//them vao de setmode cho loa ngoai
						PhoneCallActivity.am = (AudioManager)ctx.getSystemService(Context.AUDIO_SERVICE);
						PhoneCallActivity.am.setMode(AudioManager.MODE_NORMAL);
						PhoneCallActivity.am.setSpeakerphoneOn(false);
						//-----------------------
						HistoryInfo info = new HistoryInfo(PhoneCallActivity.sipTarget.getUserName(),
								PhoneCallActivity.callDate, PhoneCallActivity.callDuration, 
								PhoneCallActivity.outgoingCall, false);
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
						oos.writeObject(info);
						oos.flush();
						fos.close();
						oos.close();
					}
					catch(Exception e)
					{}
					finish();
				}
			
				@Override
				public void onCallBusy(SipAudioCall call)
				{
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(getApplicationContext(), "Busy", Toast.LENGTH_SHORT).show();
						}
					});
					finish();
				}
			};
			PhoneCallActivity.sipTarget = (new SipProfile.Builder(PhoneCallActivity.currentInputNum, 
					PhoneCallActivity.profile.getSipDomain())).build();
			PhoneCallActivity.call = PhoneCallActivity.sipManager.makeAudioCall(
					PhoneCallActivity.profile.getUriString(), 
					PhoneCallActivity.sipTarget.getUriString(), 
					PhoneCallActivity.listener, 30);
			PhoneCallActivity.call.setListener(PhoneCallActivity.listener, true);
		}
		catch(Exception e)
		{
			if(PhoneCallActivity.profile != null)
			{
				try
				{
					PhoneCallActivity.sipManager.close(PhoneCallActivity.profile.getUriString());
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
			if(PhoneCallActivity.call != null)
			{
				PhoneCallActivity.call.close();
			}
		}
	}
	
	@Override
	public void onBackPressed()
	{
		
	}
	
	public void initWiget()
	{
		btnHold = (ImageButton)findViewById(R.id.btnHold);
		btnSpeaker = (ImageButton)findViewById(R.id.btnSpeaker);
		tvTime = (TextView)findViewById(R.id.tvTime);
		btnEndCall = (ImageButton)findViewById(R.id.btnEndCall);
		
		btn1 = (Button)findViewById(R.id.btnOnee);
		btn2 = (Button)findViewById(R.id.btnTwoo);
		btn3 = (Button)findViewById(R.id.btnThreee);
		btn4 = (Button)findViewById(R.id.btnFourr);
		btn5 = (Button)findViewById(R.id.btnFivee);
		btn6 = (Button)findViewById(R.id.btnSixx);
		btn7 = (Button)findViewById(R.id.btnSevenn);
		btn8 = (Button)findViewById(R.id.btnEightt);
		btn9 = (Button)findViewById(R.id.btnNinee);
		btn0 = (Button)findViewById(R.id.btnZeroo);
		btnStar = (Button)findViewById(R.id.btnStarr);
		btnSharp = (Button)findViewById(R.id.btnSharpp);
		edTransfer = (EditText)findViewById(R.id.edTransfer);
	}
	
	public void initListener()
	{
		listenerHold = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(holdIsPressed)
				{
					btnHold.setImageResource(R.drawable.play);
					try
					{
						if(PhoneCallActivity.call != null && PhoneCallActivity.call.isInCall())
						{
							PhoneCallActivity.call.holdCall(30);
						}
					}
					catch(Exception e)
					{
						Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
					}
				}
				else
				{
					btnHold.setImageResource(R.drawable.pause);
					try
					{
						PhoneCallActivity.call.continueCall(30);
					}
					catch(Exception e)
					{
						
					}
				}
				holdIsPressed = !holdIsPressed;
			}
		};
		listenerSpeaker = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(speakerIsPressed)
				{
					btnSpeaker.setImageResource(R.drawable.speaker);
					if(PhoneCallActivity.am == null)
					{
						Context c = getApplicationContext();
						PhoneCallActivity.am = (AudioManager)c.getSystemService(Context.AUDIO_SERVICE);
					}
					PhoneCallActivity.am.setSpeakerphoneOn(true);
					PhoneCallActivity.speakerPhone = true;
					Toast.makeText(getApplicationContext(), "Loudspeaker on", Toast.LENGTH_SHORT).show();
				}
				else
				{
					btnSpeaker.setImageResource(R.drawable.speakerx);
					if(PhoneCallActivity.am == null)
					{
						Context c = getApplicationContext();
						PhoneCallActivity.am = (AudioManager)c.getSystemService(Context.AUDIO_SERVICE);
					}
					PhoneCallActivity.am.setSpeakerphoneOn(false);
					PhoneCallActivity.speakerPhone = false;
					Toast.makeText(getApplicationContext(), "Loudspeaker off", Toast.LENGTH_SHORT).show();
				}
				speakerIsPressed = !speakerIsPressed;
			}
		};	
		listenerEndcall = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(PhoneCallActivity.call != null)
				{
					try
					{
//						PhoneCallActivity.call.endCall();
						endCurrentCall();
//						PhoneCallActivity.call.close();
					}
					catch (Exception e)
					{}
					finish();
				}
			}
		};
	}
	
	public void endCurrentCall()
	{
		if(PhoneCallActivity.call != null && PhoneCallActivity.call.isInCall())
		{
			try
			{
//				Toast.makeText(getApplicationContext(), "Call Ended by User", Toast.LENGTH_SHORT).show();
				try
				{
					if(PhoneCallActivity.startTime > 0 && PhoneCallActivity.call.isInCall())
					{
						PhoneCallActivity.stopTime = SystemClock.elapsedRealtime();
						long miliSecs = PhoneCallActivity.stopTime - PhoneCallActivity.startTime;
						int sec = (int)(miliSecs / 1000) % 60;
						int mins = (int)(miliSecs / (1000 * 60)) % 60;
						int hrs = (int)(miliSecs / (1000 * 60 * 60));
						StringBuilder builder = new StringBuilder(64);
						if(hrs > 0)
						{
							builder.append(hrs);
							if(hrs > 1)
								builder.append(" hrs ");
							else builder.append(" hr ");
						}
						builder.append(mins);
						if(mins > 1)
							builder.append(" mins ");
						else builder.append(" min ");
						builder.append(sec);
						if(sec > 1)
							builder.append(" secs ");
						else builder.append(" sec ");
						
						PhoneCallActivity.callDuration = builder.toString();
						HistoryInfo info = new HistoryInfo(PhoneCallActivity.sipTarget.getUserName(),
								PhoneCallActivity.callDate, PhoneCallActivity.callDuration, 
								PhoneCallActivity.outgoingCall, false);
						PhoneCallActivity.startTime = 0;
						
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
						oos.writeObject(info);
						oos.flush();
						fos.close();
						oos.close();
						PhoneCallActivity.call.endCall();
					}
				}
				catch(Exception ex)
				{
					Toast.makeText(getApplicationContext(), "Oops. " + ex, Toast.LENGTH_SHORT).show();
				}
			}
			finally {}
			PhoneCallActivity.call.close();
		}
	}
	
	@Override
	public void onClick(View v)
	{
		Button b1 = (Button)v;
		edTransfer.setText(edTransfer.getText().toString() + b1.getText().toString());
	}
	
	/*
	 * dial cho cuoc goi
	 */
	public void setOnClick()
	{
		btn1.setOnClickListener(this);
		btn2.setOnClickListener(this);
		btn3.setOnClickListener(this);
		btn4.setOnClickListener(this);
		btn5.setOnClickListener(this);
		btn6.setOnClickListener(this);
		btn7.setOnClickListener(this);
		btn8.setOnClickListener(this);
		btn9.setOnClickListener(this);
		btn0.setOnClickListener(this);
		btnStar.setOnClickListener(this);
		btnSharp.setOnClickListener(this);
	}
}
