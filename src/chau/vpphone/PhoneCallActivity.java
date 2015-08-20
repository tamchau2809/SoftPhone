package chau.vpphone;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
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
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class PhoneCallActivity extends Activity implements OnClickListener {

	String currentInputNum;
	int inputPhoneNum;
	int endIndex;
	
	Button btn1, btn2, btn3, btn4, btn5,
	btn6, btn7, btn8, btn9, btn0,
	btnStar, btnSharp, btnContacts, btnHistory;
	
	ImageButton btnBackSpace, btnCall, btnAddContact;
	
	TextView tvStatus;
	
	EditText edPhoneNum;
	
	public static final int GET_NUM = 1;
    public static final int GOT_NUM = 2;
    public static final int SEND_NUM = 3;
    public static final int RECEIVED_NUM = 4;
    public static final int RECEIVED_NUM_HISTORY = 5;
    public static final int GET_NUM_HISTORY = 6;
    
    static final int INCOMING_CALL = 12;
    static final int HELLO_ID = 1;
    
    public final static String CALLSTATUS="chau.vpphone.CALLCONNECTED";
    
    static boolean callSomeone=false;
    static boolean endCall=false;
    static boolean backFromSW = false;
    static boolean speakerPhone=true;
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
    
    File file;
    FileOutputStream fos;
    ObjectOutputStream oos;
    
    NotificationManager mNotificationManager;
    
    public SipManager sipManager;
    public SipProfile profile = null;
    public static SipAudioCall call = null; //static
    public IncomingCallReceiver callReceiver;
    
    AudioManager am;
    	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_phone_call);
				
		getFormWiget();
		
		check();
		
		walkieMode = true;
		speakerPhone = true;
		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        am.setSpeakerphoneOn(true);
		
//		Intent test = new Intent(this, RunReceiver.class);
//		boolean isRunning = (PendingIntent.getBroadcast(this, 0, test, PendingIntent.FLAG_NO_CREATE) != null);
//		if(isRunning == false)
//		{
//			PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, test, 0);
//			AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 1800000, pendingIntent);
//		}
		btnCall.setEnabled(false);		
		edPhoneNum.addTextChangedListener(new TextWatcher() {
			
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
		});		
		btnContacts.setOnClickListener(new OnClickListener() 
		{			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(PhoneCallActivity.this, ContactsActivity.class);
				AddContactActivity.checkNum = false;
				startActivityForResult(intent, GET_NUM);
			}
		});
		btnAddContact.setOnClickListener(new OnClickListener() 
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
		});
		btnCall.setOnClickListener(new OnClickListener() 
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
					AlertDialog.Builder callingDialog = new AlertDialog.Builder(PhoneCallActivity.this);
					callingDialog.setTitle("Calling To " + edPhoneNum.getText());
					currentInputNum = edPhoneNum.getText().toString().trim();
//					currentInputNum = "0933612559";
					initCall();
					callingDialog.setCancelable(false);
					callingDialog.setNegativeButton("Hang Out", 
							new DialogInterface.OnClickListener() 
					{					
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							if(call != null)
							{
								try
								{
									call.endCall();
								} catch (SipException e)
								{}
								call.close();
							}
						}
					});
					callingDialog.show();
				}
			}
		});
		btnBackSpace.setOnClickListener(new OnClickListener()
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
		});
		btnHistory.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(PhoneCallActivity.this, HistoryActivity.class);
				if(call != null) intent.putExtra(CALLSTATUS, call.isInCall());
				//startActivityForResult(intent, GET_NUM_HISTORY);
				startActivity(intent);
			}
		});
		
		setOnClick();
		
		IntentFilter filter = new IntentFilter();
        filter.addAction("android.VPhone.INCOMING_CALL");
        callReceiver = new IncomingCallReceiver();
        this.registerReceiver(callReceiver, filter);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		initManager();	
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
//        	Toast.makeText(this, "i m here "+call, Toast.LENGTH_LONG).show();
        	endCurrentCall();
        }
    	
//    	Toast.makeText(this, "wallah", Toast.LENGTH_LONG).show();
        
        if(callSomeone)
        {
        	callSomeone=false;
//        	Toast.makeText(this, "wallah 2", Toast.LENGTH_LONG).show();
//        	Intent intent2=getIntent();
//        	Toast.makeText(this, ""+intent.getStringExtra(History.ADDRTOCALL), Toast.LENGTH_LONG).show();
        	sipAddress=intent.getStringExtra(HistoryActivity.ADDRTOCALL);
//        	Toast.makeText(this, ""+sipAddress, Toast.LENGTH_LONG).show();
//        	Log.d("yaar", "call is intiating");
//        	Toast.makeText(this, "wallah 3", Toast.LENGTH_LONG).show();
        	initCall();
        }
    	
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
		initManager();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		initManager();
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if(call != null && call.isInCall())
		{
			endCurrentCall();
		}
		//closeLocalProfile();
		if(callReceiver != null)
		{
			this.unregisterReceiver(callReceiver);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Button b1 = (Button)v;
	    edPhoneNum.setText(edPhoneNum.getText().toString()+b1.getText().toString());
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
		btnAddContact = (ImageButton)findViewById(R.id.btnAddContact);
		btnCall = (ImageButton)findViewById(R.id.btnCall);
		btnBackSpace = (ImageButton)findViewById(R.id.btnBackSpace);
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
			
			Intent intent = new Intent();
			intent.setAction("android.VPhone.INCOMING_CALL");
			PendingIntent pi = PendingIntent.getBroadcast(this, 
					0, intent, Intent.FILL_IN_DATA);
			sipManager.open(profile, pi, null);
			
			sipManager.setRegistrationListener(profile.getUriString(), new SipRegistrationListener() {
				
				@Override
				public void onRegistrationFailed(String localProfileUri, int errorCode,
						String errorMessage) {
					// TODO Auto-generated method stub
					updateStatus("Failed!!!");
					tvStatus.setTextColor(Color.RED);
//					isAccReady = false;
				}
				
				@Override
				public void onRegistrationDone(String localProfileUri, long expiryTime) {
					// TODO Auto-generated method stub
					updateStatus(username + "\n is Connected");
					tvStatus.setTextColor(Color.GREEN);
					isAccReady = true;
				}
				
				@Override
				public void onRegistering(String localProfileUri) {
					// TODO Auto-generated method stub
					updateStatus("Connecting...");
					tvStatus.setTextColor(Color.WHITE);
//					isAccReady = false;
				}
			});
		} catch (ParseException pe) {
            updateStatus("No Account.");
            tvStatus.setTextColor(Color.WHITE);
//            isAccReady = false;
        } 
		catch (SipException se) {
            updateStatus("Connection error.");
            tvStatus.setTextColor(Color.WHITE);
//            isAccReady = false;
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
	
	public void initCall()
	{
		try
		{
			SipAudioCall.Listener listener = new SipAudioCall.Listener()
			{
				@Override
				public void onCallEstablished(SipAudioCall call)
				{
//					startTime = SystemClock.elapsedRealtime();
//					SimpleDateFormat sdf = new SimpleDateFormat("H:mmaa    EEEE, dd MM, yyyy");
//					callDate = sdf.format(new Date());
//					outgoingCall = true;
					call.startAudio();
					call.setSpeakerMode(true);
//					call.toggleMute();
//					long when = System.currentTimeMillis();
				}
				
				@Override
				public void onCallEnded(SipAudioCall call)
				{
//					try
//					{
//						stopTime = SystemClock.elapsedRealtime();
//						long milisecond = stopTime - startTime;
//						int secs = (int)(milisecond / 1000) % 60;
//						int mins = (int)((milisecond) / (1000*60)) %60;
//						int hours = (int)((milisecond) / (1000*60*60));
//						StringBuilder sb = new StringBuilder(64);
//						if(hours > 0)
//						{
//							sb.append(hours);
//							sb.append(" hrs ");
//						}
//						sb.append(mins);
//						sb.append(" mins ");
//						sb.append(secs);
//            			sb.append(" secs");
//            			callDuration = sb.toString();
//            			startTime = 0;
//            			HistoryInfo historyInfo = new HistoryInfo(profile.getUserName(), callDate, 
//            					callDuration, outgoingCall, false);
//            			
//            			file = new File("/data/data/chau.vpphone/files/" + FILENAME);
//            			if(!file.exists())
//            			{
//            				fos = openFileOutput(FILENAME, Context.MODE_APPEND);
//            				oos = new ObjectOutputStream(fos);
//            			}
//            			else
//            			{
//            				fos = openFileOutput(FILENAME, Context.MODE_APPEND);
//            				oos = new AppendableObjectOutputStream(fos);
//            			}            			
//            			oos.writeObject(historyInfo);
//            			oos.flush();
//            			fos.close();
//            			oos.close();
//					}catch (Exception e)
//					{}
				}
			};
			SipProfile sipTarget = (new SipProfile.Builder(currentInputNum, 
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
	
	public void updateStatus(SipAudioCall call) {
        String useName = call.getPeerProfile().getDisplayName();
        if(useName == null) {
          useName = call.getPeerProfile().getUserName();
        }
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
				
			}
		});
        calledDialog.show();
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

