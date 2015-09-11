package chau.vpphone;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.sip.SipAudioCall;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("InlinedApi")
public class IncomingCallActivity extends Activity implements OnClickListener{
	
	FileOutputStream fos;
	ObjectOutputStream oos;
	File file;
	
	Button btn1, btn2, btn3, btn4, btn5,
	btn6, btn7, btn8, btn9, btn0,
	btnStar, btnSharp;
	EditText edTransfer;
	
	TextView tvTime;
	ImageButton btnHold, btnSpeaker, btnEndCall;
	View.OnClickListener listenerHold, listenerSpeaker, listenerEndcall;
	
	boolean holdIsPressed = true;
	boolean speakerIsPressed = true;
	
	public static boolean accept;
	
	public static SipAudioCall incomingCall = null;
	AudioManager am;
	NotificationManager mNotificationManager;
	String name;
	
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle save)
	{
		super.onCreate(save);
		setContentView(R.layout.activity_on_calling);
		
		initWiget();
		initListener();
		setOnClick();
		
		Intent i = getIntent();
		Bundle bundle = i.getBundleExtra("ADD");
		name = bundle.getString("INCOM");
		
		this.setFinishOnTouchOutside(false);
		
		btnHold.setOnClickListener(listenerHold);
		btnSpeaker.setOnClickListener(listenerSpeaker);
		btnEndCall.setOnClickListener(listenerEndcall);
		
		am = (AudioManager)getSystemService(AUDIO_SERVICE);
		am.setMode(AudioManager.MODE_RINGTONE);
		
		String ns = NOTIFICATION_SERVICE;
		mNotificationManager = (NotificationManager)getSystemService(ns);
		
		takeCall();
		
	}
	
	@Override
	public void onBackPressed()
	{
		
	}
	
	@SuppressLint("SimpleDateFormat")
	public void takeCall()
	{
		try
		{
			IncomingSO.accept = true;
			incomingCall.answerCall(30);
			incomingCall.startAudio();
			PhoneCallActivity.startTime = SystemClock.elapsedRealtime();
			SimpleDateFormat sdf = new SimpleDateFormat("H:mmaa   EEEE, dd MM, yyyy");
			PhoneCallActivity.callDate = sdf.format(new Date());
			PhoneCallActivity.outgoingCall = false;
			PhoneCallActivity.callHeld = false;
			incomingCall.setSpeakerMode(false);
			am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			am.setMode(AudioManager.MODE_IN_COMMUNICATION);	
			if(PhoneCallActivity.speakerPhone)
			{
				am.setSpeakerphoneOn(true);
			}
		}
		catch(Exception e)
		{
			
		}
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
						if(incomingCall != null && incomingCall.isInCall())
						{
							incomingCall.holdCall(30);
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
						incomingCall.continueCall(30);
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
						am = (AudioManager)c.getSystemService(Context.AUDIO_SERVICE);
					}
					am.setSpeakerphoneOn(true);
					PhoneCallActivity.speakerPhone = true;
					Toast.makeText(getApplicationContext(), "Loudspeaker on", Toast.LENGTH_SHORT).show();
				}
				else
				{
					btnSpeaker.setImageResource(R.drawable.speakerx);
					if(am == null)
					{
						Context c = getApplicationContext();
						am = (AudioManager)c.getSystemService(Context.AUDIO_SERVICE);
					}
					am.setSpeakerphoneOn(false);
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
				if(incomingCall != null)
				{
					endCurrentCall();
					finish();
				}
			}
		};
	}
	
	@SuppressLint("SdCardPath")
	public void endCurrentCall()
	{
		try
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
			HistoryInfo info = new HistoryInfo(name,
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
			incomingCall.endCall();
		}
		catch(Exception e)
		{
			
		}
		incomingCall.close();
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
