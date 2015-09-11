package chau.vpphone;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressWarnings("deprecation")
public class PhoneCallActivity extends Activity implements OnClickListener {

	static String currentInputNum;
	int inputPhoneNum;
	int endIndex;
	
	String useName;
	
	Button btn1, btn2, btn3, btn4, btn5,
	btn6, btn7, btn8, btn9, btn0,
	btnStar, btnSharp, btnContacts, btnHistory;
	
	ImageButton btnBackSpace, btnCall, btnAddContact;
	
	TextView tvStatus;
	static boolean exit = false;
	static boolean shadowFiend = false;
	
	EditText edPhoneNum;
	
	TextWatcher watcher;
	OnClickListener listenerContact, listenerAddContact, listenerCall, listnerBaS,
	listenerHis;
	
	public static final int GET_NUM = 1;
    public static final int GOT_NUM = 2;
    public static final int SEND_NUM = 3;
    public static final int RECEIVED_NUM = 4;
    public static final int RECEIVED_NUM_HISTORY = 5;
    public static final int GET_NUM_HISTORY = 6;
    
    static final int INCOMING_CALL = 12;
    static final int HELLO_ID = 7;
    private static final int INCORRECT_ADDRESS = 8;
    private static final int CALL_CONNECTED_DIALOG = 9;
    
    public final static String CALLSTATUS="chau.vpphone.CALLCONNECTED";
    
    static boolean callSomeone=false;
    static boolean endCall=false;
    static boolean backFromSW = false;
    static boolean speakerPhone;
    static boolean walkieMode=true;
    static boolean callHeld=false;
    static boolean incomingCallHeld;
    
    public static boolean isReady;
    public static boolean isAccReady = false;
    
    public static String sipAddress = null;
    static String callDate = "";
    static String callDuration = "";
    static long startTime=0;
    static long stopTime=0;
    static boolean outgoingCall;
    static String FILENAME = "history";
    
    static File file;
    FileOutputStream fos;
    ObjectOutputStream oos;
    
    NotificationManager mNotificationManager;
    
    public static SipManager sipManager;
    public static SipProfile profile = null;
    public static SipAudioCall call = null; //static
    public IncomingCallReceiver callReceiver;
    public static SipProfile sipTarget;
    public static SipAudioCall.Listener listener;
    
    public static AudioManager am;
    PowerManager.WakeLock mProximityWakeLock;
    PowerManager pm;
    KeyguardManager keyguardManager;
    	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_phone_call);
				
		getFormWiget();
		eventCreated();
		check();
		
		InputMethodManager ipm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		ipm.hideSoftInputFromWindow(edPhoneNum.getWindowToken(), 0);
		
//		walkieMode = true;
//		speakerPhone = true;
		speakerPhone = false;
		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        am.setSpeakerphoneOn(false);
		
//		Intent test = new Intent(this, RunReceiver.class);
//		boolean isRunning = (PendingIntent.getBroadcast(this, 0, test, PendingIntent.FLAG_NO_CREATE) != null);
//		if(isRunning == false)
//		{
//			PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, test, 0);
//			AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//          alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 1800000, pendingIntent);
//		}
                
		btnCall.setEnabled(false);		
		edPhoneNum.addTextChangedListener(watcher);		
		btnContacts.setOnClickListener(listenerContact);
		btnAddContact.setOnClickListener(listenerAddContact);
		btnCall.setOnClickListener(listenerCall);
		btnBackSpace.setOnClickListener(listnerBaS);
		btnHistory.setOnClickListener(listenerHis);
		
		setOnClick();
		
		IntentFilter filter = new IntentFilter();
        filter.addAction("android.VPhone.INCOMING_CALL");
        callReceiver = new IncomingCallReceiver();
        this.registerReceiver(callReceiver, filter);
		

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		String ns = NOTIFICATION_SERVICE;
		mNotificationManager = (NotificationManager)getSystemService(ns);
		pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		try
		{
			Method method = pm.getClass().getDeclaredMethod("getSupportedWakeLockFlags");
			int supportFlags = (Integer)method.invoke(pm);
			Field f = PowerManager.class.getDeclaredField("PROXIMITY_SCREEN_OFF_WAKE_LOCK");
			int proximityScreenOffWakeLock = (Integer)f.get(null);
			if((supportFlags & proximityScreenOffWakeLock) != 0x0)
			{
				mProximityWakeLock = pm.newWakeLock(proximityScreenOffWakeLock, "Tag");
				mProximityWakeLock.setReferenceCounted(false);
			}
		}
		catch(Exception e)
		{}
		keyguardManager = (KeyguardManager)getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
		initManager();	
	}
	
	public void eventCreated()
	{
//		edPhoneNum.setInputType(InputType.TYPE_NULL);
		
		watcher = new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				enableCallBtn();
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				enableCallBtn();
			}
		};
		listenerContact = new OnClickListener() 
		{			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(PhoneCallActivity.this, ContactsActivity.class);
				AddContactActivity.checkNum = false;
				startActivityForResult(intent, GET_NUM);
			}
		};
		listenerAddContact = new OnClickListener() 
		{			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(PhoneCallActivity.this, ContactsActivity.class);
				AddContactActivity.checkNum = true;
				Bundle bundle = new Bundle();
				bundle.putString("NUMBER", edPhoneNum.getText().toString());
				intent.putExtra("NUM_EXTRA", bundle);
				
				startActivity(intent);
			}
		};
		listenerCall = new OnClickListener() 
		{			
			@Override
			public void onClick(View v) 
			{
				
				// TODO Auto-generated method stub
				if(isAccReady == false)
				{
					AlertDialog.Builder noLogin = new AlertDialog.Builder(PhoneCallActivity.this);
					noLogin.setTitle("Oops!!!");
					noLogin.setMessage("Please LOGIN or CHECK your account again!!!");
					noLogin.setPositiveButton("OK", null);
					noLogin.setCancelable(false);
					noLogin.show();
				}
				else
				{
//					AlertDialog.Builder callingDialog = new AlertDialog.Builder(PhoneCallActivity.this);
//					callingDialog.setTitle("Calling To " + edPhoneNum.getText());
					currentInputNum = edPhoneNum.getText().toString().trim();
					Intent intent = new Intent(getBaseContext(), OnCallingActivity.class);
					startActivity(intent);
//					initCall();
//					callingDialog.setCancelable(false);
//					callingDialog.setNegativeButton("Hang Out", 
//							new DialogInterface.OnClickListener() 
//					{					
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							// TODO Auto-generated method stub
//							if(call != null)
//							{
//								try
//								{
//									call.endCall();
//									call.close();
//									endCurrentCall();
//								} catch (SipException e)
//								{}
//							}
//						}
//					});
//					AlertDialog ad = callingDialog.show();
				}
			}
		};
		listnerBaS = new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub				
		        String str=edPhoneNum.getText().toString();
		        if (str.length() >1 ) { 
		            str = str.substring(0, str.length() - 1);
		            edPhoneNum.setText(str);
		            }
		       else if (str.length() <=1 ) {
		    	   edPhoneNum.setText(null);
		    	   edPhoneNum.setHint("Number...");
		        }
			}
		};
		listenerHis = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(PhoneCallActivity.this, HistoryActivity.class);
//				if(call != null) intent.putExtra(CALLSTATUS, call.isInCall());
				startActivityForResult(intent, GET_NUM_HISTORY);
//				startActivity(intent);
			}
		};
	}
	
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.phone_call, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_Login)
		{
			Intent intent = new Intent(PhoneCallActivity.this, LoginActivity.class);
			startActivity(intent);
		}
		if(id == R.id.action_LogOut)
		{
			SharedPreferences prefs = getBaseContext().getSharedPreferences("LOGIN", MODE_PRIVATE);
			prefs.edit().clear().apply();
			updateStatus("Logged Out!");
//			isAccReady = false;
			tvStatus.setTextColor(Color.WHITE);
		}
		if(id == R.id.action_About)
		{
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}
	
    @Override
    protected void onNewIntent(Intent intent)
    {
    	super.onNewIntent(intent);
    	Toast.makeText(this, "Nava intent", Toast.LENGTH_LONG).show();
    	
    	if(endCall)
        {
        	endCall=false;
        	endCurrentCall();
        }
        
        if(callSomeone)
        {
        	callSomeone=false;
        	sipAddress=intent.getStringExtra(HistoryActivity.ADDRTOCALL);
        	initCall();
        }
    	finish();
    }
	
	/**
	 * kiểm tra điện thoại có hỗ
	 * trợ SIP
	 */
	public void check()
	{
		if(SipManager.isVoipSupported(getApplicationContext()))
		{
			final SharedPreferences pref = getBaseContext().getSharedPreferences("CHECK_FIRST", MODE_PRIVATE);
			if(!pref.contains("FirstTime"))
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Welcome...");
				builder.setCancelable(false);
				builder.setMessage("Hope you like it. :)");
				
				{
					builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							Editor editor = pref.edit();
							editor.putBoolean("FirstTime", true);
							editor.commit();
						}
					});			
					builder.show();
				}
			}
		}
		else
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Oops..");
			builder.setMessage("Your Device does NOT SUPPORT SIP-based VOIP API");
			builder.setPositiveButton("I Agree", null);
			builder.setCancelable(false);
			builder.show();
		}
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		if(backFromSW)
		{
			backFromSW = false;
			initManager();
		}
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		if(mNotificationManager != null && call == null)
			mNotificationManager.cancel(HELLO_ID);
	}

	@Override
	public void onStop()
	{
		super.onStop();
		if(mProximityWakeLock != null && mProximityWakeLock.isHeld())
			mProximityWakeLock.release();
	}
	
	@SuppressLint("Wakelock")
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if(call != null && call.isInCall())
		{
			endCurrentCall();
		}
		if(call != null)
		{
			call.close();
		}
		closeLocalProfile();
		if(callReceiver != null)
		{
			this.unregisterReceiver(callReceiver);
		}
		Context c = getApplicationContext();
		am = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
		am.setMode(AudioManager.MODE_NORMAL);
		am.setSpeakerphoneOn(false);
		
		if(mProximityWakeLock != null && mProximityWakeLock.isHeld())
			mProximityWakeLock.release();
		if(mNotificationManager != null)
		{
			mNotificationManager.cancel(HELLO_ID);
		}
		if(keyguardManager != null)
		{
			KeyguardLock keyguard = keyguardManager.newKeyguardLock("tag");
			keyguard.reenableKeyguard();
		}
	}
	
	@Override
	public void onBackPressed()
	{
		if(call != null && call.isInCall())
		{
			showDialog(CALL_CONNECTED_DIALOG);
		}
		else
		{
			super.onBackPressed();
		}
	}
	
	/*
	 * nút nhập số để gọi
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Button b1 = (Button)v;
	    edPhoneNum.setText(edPhoneNum.getText().toString()+b1.getText().toString());
	}
	
	public void getFormWiget()
	{
		btn1 = (Button)findViewById(R.id.btnOne);
		btn2 = (Button)findViewById(R.id.btnTwo);
		btn3 = (Button)findViewById(R.id.btnThree);
		btn4 = (Button)findViewById(R.id.btnFour);
		btn5 = (Button)findViewById(R.id.btnFive);
		btn6 = (Button)findViewById(R.id.btnSix);
		btn7 = (Button)findViewById(R.id.btnSeven);
		btn8 = (Button)findViewById(R.id.btnEight);
		btn9 = (Button)findViewById(R.id.btnNine);
		btn0 = (Button)findViewById(R.id.btnZero);
		btnStar = (Button)findViewById(R.id.btnStar);
		btnSharp = (Button)findViewById(R.id.btnSharp);
		
		btnContacts = (Button)findViewById(R.id.btnContact);
		btnAddContact = (ImageButton)findViewById(R.id.btnHold);
		btnCall = (ImageButton)findViewById(R.id.btnEndCall);
		btnBackSpace = (ImageButton)findViewById(R.id.btnSpeaker);
		btnHistory = (Button)findViewById(R.id.btnHistory);

		edPhoneNum = (EditText)findViewById(R.id.edPhoneNum);
		tvStatus = (TextView)findViewById(R.id.tvStatus);
	}
	
	public void enableCallBtn()
	{
		isReady = edPhoneNum.getText().toString().length() > 0;
		btnCall.setEnabled(isReady);
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
		
		SharedPreferences prefs = getBaseContext().getSharedPreferences("LOGIN", MODE_PRIVATE);		
		final String username = prefs.getString("username", "");
		String password = prefs.getString("password", "");
		String domain = prefs.getString("domain", "");
		
		if(username.length() == 0 || password.length() == 0
				|| domain.length() == 0)
			return;
		
		try
		{
			SipProfile.Builder builder = new SipProfile.Builder(username, domain);
			builder.setPassword(password);
			profile = builder.build();
			
//			Intent intent = new Intent();
//			intent.setAction("android.VPhone.INCOMING_CALL");
//			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//			PendingIntent pi = PendingIntent.getBroadcast(this, 
//					0, intent, Intent.FILL_IN_DATA);
//			sipManager.open(profile, pi, null);
			
			Intent intent = new Intent();
			intent.setAction("android.VPhone.INCOMING_CALL");
			PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, Intent.FILL_IN_DATA);
			sipManager.open(profile, pi, null);
			
			sipManager.setRegistrationListener(profile.getUriString(), new SipRegistrationListener() {
				
				@Override
				public void onRegistrationFailed(String localProfileUri, int errorCode,
						String errorMessage) {
					// TODO Auto-generated method stub
					updateStatus("Failed!!!");
					tvStatus.setTextColor(Color.RED);
				}
				
				@Override
				public void onRegistrationDone(String localProfileUri, long expiryTime) {
					// TODO Auto-generated method stub
					updateStatus(username.substring(5) + "\n is Connected");
					tvStatus.setTextColor(Color.GREEN);
					isAccReady = true;
				}
				
				@Override
				public void onRegistering(String localProfileUri) {
					// TODO Auto-generated method stub
					updateStatus("Connecting...");
					tvStatus.setTextColor(Color.WHITE);
				}
			});
		} catch (ParseException pe) {
            updateStatus("No Account.");
            tvStatus.setTextColor(Color.WHITE);
        } 
		catch (SipException se) {
            updateStatus("Connection error.");
            tvStatus.setTextColor(Color.WHITE);
        }
	}
	
	public void closeLocalProfile() {
        if (sipManager == null) {
            return;
        }
        try {
            if (profile != null) {
                sipManager.close(profile.getUriString());
                Log.d("CLOSE", "CLOSE_PROFILE");
            }
        } catch (Exception ee) {
            Log.d("/onDestroy", "Oops.. I did it again.", ee);
        }
    }
	
	@SuppressLint("SimpleDateFormat")
	public void initCall()
	{
		exit = false;
		try
		{
			listener = new SipAudioCall.Listener()
			{
				@Override
				public void onCallEstablished(SipAudioCall call)
				{					
					if(call != null && call.isInCall())
					{
						if(!callHeld)
						{
							callHeld = true;
							Toast.makeText(getBaseContext(), "Held", Toast.LENGTH_LONG).show();
							return;
						}
						else
						{
							callHeld = false;
							updateStatus(call);
							return;
						}
					}
					startTime = SystemClock.elapsedRealtime();
					SimpleDateFormat sdf = new SimpleDateFormat("H:mmaa   EEEE, dd MM, yyyy");
					callDate = sdf.format(new Date());
					outgoingCall = true;
					callHeld = false;
					call.startAudio();
					call.setSpeakerMode(true);
					final Context c = getApplicationContext();
					if(speakerPhone)
					{
						am = (AudioManager)c.getSystemService(Context.AUDIO_SERVICE);
						am.setSpeakerphoneOn(true);
					}
					am.setMode(AudioManager.MODE_IN_COMMUNICATION);
//					if(walkieMode)
//					{
//						call.toggleMute(); //làm nút mute
//					}
					updateStatus(call);
					
					int icon = R.drawable.ic_launcher;
                    long when = System.currentTimeMillis();
                    CharSequence contentTitle = "VPhone";
                    CharSequence contentText = "Return to ongoing call";
                    
                    Intent notificationIntent = new Intent(c,PhoneCallActivity.class);
                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    PendingIntent contentIntent = PendingIntent.getActivity(c, 0, notificationIntent, 0);
                    
                    Notification notification = new Notification(icon, "", when);
                    notification.setLatestEventInfo(c, contentTitle, contentText, contentIntent);
                    notification.flags |= Notification.FLAG_ONGOING_EVENT;
                    notification.flags |= Notification.FLAG_NO_CLEAR;
                    
                    mNotificationManager.notify(HELLO_ID, notification);
				}
				
				@Override
				public void onCalling(SipAudioCall call)
				{
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(PhoneCallActivity.this, "Calling...", Toast.LENGTH_LONG).show();
						}
					});
				}
				
				@Override
				public void onRinging(SipAudioCall call, SipProfile caller)
				{
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(PhoneCallActivity.this, "Ring Ring", Toast.LENGTH_SHORT).show();
						}
					});
				}
				
				@Override
				public void onCallHeld(SipAudioCall call)
				{
					callHeld = true;
					Toast.makeText(PhoneCallActivity.this, "Held", Toast.LENGTH_SHORT).show();
					super.onCallHeld(call);
				}
				
				@Override
				public void onCallEnded(SipAudioCall call)
				{
					try
					{
						stopTime = SystemClock.elapsedRealtime();
						long milisecond = stopTime - startTime;
						int secs = (int)(milisecond / 1000) % 60;
						int mins = (int)((milisecond) / (1000*60)) %60;
						int hours = (int)((milisecond) / (1000*60*60));
						StringBuilder sb = new StringBuilder(64);
						if(hours > 0)
						{
							sb.append(hours);
							sb.append(" hrs ");
						}
						sb.append(mins);
						sb.append(" mins ");
						sb.append(secs);
            			sb.append(" secs");
            			callDuration = sb.toString();
            			startTime = 0;
//            			HistoryInfo historyInfo = new HistoryInfo(profile.getUserName(), callDate, 
//            					callDuration, outgoingCall, false);
            			HistoryInfo historyInfo = new HistoryInfo(sipTarget.getUserName(), callDate, 
            					callDuration, outgoingCall, false);
            			
            			Context c = getApplicationContext();
            			am = (AudioManager)c.getSystemService(Context.AUDIO_SERVICE);
            			am.setMode(AudioManager.MODE_NORMAL);
            			am.setSpeakerphoneOn(false);
            			
            			file = new File("/data/data/chau.vpphone/files/" + FILENAME);
            			if(!file.exists())
            			{
            				fos = openFileOutput(FILENAME, Context.MODE_APPEND);
            				oos = new ObjectOutputStream(fos);
            			}
            			else
            			{
            				fos = openFileOutput(FILENAME, Context.MODE_APPEND);
            				oos = new AppendableObjectOutputStream(fos);
            			}            			
            			oos.writeObject(historyInfo);
            			oos.flush();
            			fos.close();
            			oos.close();
            			mNotificationManager.cancel(HELLO_ID);
					}catch (Exception e)
					{}
				}
				
				@Override
				public void onError(SipAudioCall call, final int errorcode, final String errorMessage)
				{
					exit = true;
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(getApplicationContext(), "onError: " + errorcode + " " + errorMessage, Toast.LENGTH_SHORT).show();
							if(errorcode == -7)
							{
								PhoneCallActivity.this.showDialog(INCORRECT_ADDRESS);
							}
						}
					});
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
			sipTarget = (new SipProfile.Builder(currentInputNum, 
					profile.getSipDomain())).build();
			call = sipManager.makeAudioCall(profile.getUriString(), 
				sipTarget.getUriString(), listener, 30);	
			call.setListener(listener, true);
//			call = sipManager.makeAudioCall(profile.getUriString(), currentInputNum, listener, 30);
			
//			Toast.makeText(this, sipTarget.getUriString(), Toast.LENGTH_SHORT).show();
		} catch (Exception e)
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
	
	@Override
	protected Dialog onCreateDialog(int id)
	{
		switch(id)
		{
		case CALL_CONNECTED_DIALOG:
			return new AlertDialog.Builder(this)
			.setMessage("You are in call..")
			.setPositiveButton("I got it", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					endCurrentCall();
					PhoneCallActivity.this.finish();
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			})
			.setCancelable(false)
			.create();
		case INCORRECT_ADDRESS:
			return new AlertDialog.Builder(this)
			.setTitle("Oops..")
			.setMessage("Incorrect Number")
			.setPositiveButton("Okay", null)
			.create();
//		case INCOMING_CALL:
//			return new AlertDialog.Builder(this)
//			.setMessage("Incoming Call")
//			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//				
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					// TODO Auto-generated method stub
//					IncomingCallActivity.accept = true;
//					Intent intent = new Intent(getBaseContext(), IncomingCallActivity.class);
//					startActivity(intent);
//					
//				}
//			})
//			.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
//				
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					// TODO Auto-generated method stub
//				}
//			})
//			.setCancelable(false)
//			.create();
		}
		return null;
	}
	
	public void updateStatus(final String status)
	{
		this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				tvStatus.setText(status);
			}
		});
	}
	
	public void updateStatus(final SipAudioCall call) {
        useName = call.getPeerProfile().getDisplayName();
        if(useName == null) {
          useName = call.getPeerProfile().getUserName();
        }
        Toast.makeText(getBaseContext(), "InComing...", Toast.LENGTH_SHORT).show();
        AlertDialog.Builder calledDialog = new AlertDialog.Builder(this);
        calledDialog.setTitle(useName + " is Calling to you...");
        calledDialog.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
				
			}
		});
        calledDialog.setNegativeButton("Not Now", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				try
				{
					call.endCall();
					
					logMissedCall();
				}
				catch(Exception e)
				{}
			}
		});
        calledDialog.setCancelable(false);
        calledDialog.show();
    }
	
	public void showToast(final String text)
	{
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
	}
	
	public void endCurrentCall()
	{
		if(call != null && call.isInCall())
		{
			try
			{
				Toast.makeText(this, "Call ended by user", Toast.LENGTH_LONG).show();
				try
				{
					if(startTime > 0 && call.isInCall())
					{
						stopTime = SystemClock.elapsedRealtime();
						long miliSecs = stopTime - startTime;
						int secs = (int)(miliSecs / 1000) % 60;
						int mins = (int)((miliSecs / (1000*60)) % 60);
						int hours = (int)((miliSecs / (1000*60*60)));
						StringBuilder sb = new StringBuilder(64);
						if(hours > 0)
						{
							sb.append(hours);
							sb.append(" hrs ");
						}
						sb.append(mins);
						sb.append(" mins ");
						sb.append(secs);
						sb.append(" secs ");
						callDuration = sb.toString();
						HistoryInfo hisInfo = new HistoryInfo(sipAddress.substring(4), callDate, callDuration, outgoingCall, false);
						startTime = 0;
						
						file = new File("/data/data/chau.vpphone/files/" + FILENAME);
						if(!file.exists())
						{
							fos = openFileOutput(FILENAME, Context.MODE_APPEND);
							oos = new ObjectOutputStream(fos);
						}
						else
						{
							fos = openFileOutput(FILENAME, Context.MODE_APPEND);
							oos = new AppendableObjectOutputStream(fos);
						}
						oos.writeObject(hisInfo);
						oos.flush();
						fos.close();
						oos.close();
						call.endCall();
					}
				}
				catch(Exception e)
				{
					Toast toast=Toast.makeText(getApplicationContext(), "Unable to enter data to file "+e, Toast.LENGTH_LONG);
	                toast.show();
				}
			}
			finally{}
			call.close();
		}
	}
	
	@Override
	protected void onActivityResult(int request, int result, Intent data)
	{
		super.onActivityResult(request, result, data);
		if(result == GOT_NUM)
		{
			Bundle bundle = data.getBundleExtra("numExtra");
			edPhoneNum.setText(bundle.getString("Contact_num").replace(" ", ""));
		}
		if(result == RECEIVED_NUM_HISTORY)
		{
			Bundle bundle = data.getBundleExtra("numHisExtra");
			edPhoneNum.setText(bundle.getString("His_num"));
		}
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
    		HistoryInfo hinfo=new HistoryInfo(useName,sdf.format(new Date()), PhoneCallActivity.callDuration, PhoneCallActivity.outgoingCall,true);
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
	
//	public class BootUpReceiver extends BroadcastReceiver
//	{
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			// TODO Auto-generated method stub
//			Intent i = new Intent(context, PhoneCallActivity.class);
//			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			context.startActivity(i);
//		}
//	}
}

