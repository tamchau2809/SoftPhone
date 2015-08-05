package chau.vpphone;

import java.text.ParseException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

	static String currentInputNum;
	int inputPhoneNum;
	int endIndex;
	
	Button btn1, btn2, btn3, btn4, btn5,
	btn6, btn7, btn8, btn9, btn0,
	btnStar, btnSharp, btnContacts;
	
	ImageButton btnBackSpace, btnCall, btnAddContact;
	
	static EditText edPhoneNum;
	
	public static final int GET_NUM = 1;
    public static final int GOT_NUM = 2;
    public static final int SEND_NUM = 3;
    public static final int RECEIVED_NUM = 4;
    
    public static SipManager sipManager = null;
    public static SipProfile profile = null;
    public static SipAudioCall call = null;
    public static IncomingCallReceiver callReceiver;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_phone_call);
		
		getFormWiget();
		
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
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				AlertDialog.Builder callingDialog = new AlertDialog.Builder(PhoneCallActivity.this);
				callingDialog.setTitle("Calling To " + edPhoneNum.getText());
				currentInputNum = edPhoneNum.getText().toString();				
				initCall();
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
		
		IntentFilter filter = new IntentFilter();
        filter.addAction("android.VPhone.INCOMING_CALL");
        callReceiver = new IncomingCallReceiver();
        this.registerReceiver(callReceiver, filter);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		initManager();
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
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			prefs.edit().clear().apply();
			updateStatus("Logged Out!");
		}
		if(id == R.id.action_About)
		{
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
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
		if(call != null)
		{
			call.close();
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

		edPhoneNum = (EditText)findViewById(R.id.edPhoneNum);
	}
	
	public void enableCallBtn()
	{
		boolean isReady = edPhoneNum.getText().toString().length() > 0;
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
			
			sipManager.setRegistrationListener(profile.getUriString(), new SipRegistrationListener() {
				
				@Override
				public void onRegistrationFailed(String localProfileUri, int errorCode,
						String errorMessage) {
					// TODO Auto-generated method stub
					updateStatus("Failed!!!");
				}
				
				@Override
				public void onRegistrationDone(String localProfileUri, long expiryTime) {
					// TODO Auto-generated method stub
					updateStatus("Connected.");
				}
				
				@Override
				public void onRegistering(String localProfileUri) {
					// TODO Auto-generated method stub
					updateStatus("Connecting...");
				}
			});
		} catch (ParseException pe) {
            updateStatus("No Account.");
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
                Log.d("CLOSE", "CLOSE_PROFILE");
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
				TextView tvStatus = (TextView)findViewById(R.id.tvStatus);
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
	
	public static void initCall()
	{
		try
		{
			SipAudioCall.Listener listener = new SipAudioCall.Listener()
			{
				@Override
				public void onCallEstablished(SipAudioCall call)
				{
					call.startAudio();
					call.setSpeakerMode(true);
					call.toggleMute();
				}
				
				@Override
				public void onCallEnded(SipAudioCall call)
				{
				}
			};
			call = sipManager.makeAudioCall(profile.getUriString(), 
					currentInputNum, listener, 30);			
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
	protected void onActivityResult(int request, int result, Intent data)
	{
		super.onActivityResult(request, result, data);
		if(result == GOT_NUM)
		{
			Bundle bundle = data.getBundleExtra("numExtra");
			edPhoneNum.setText(bundle.getString("Contact_num"));
		}
	}
}
