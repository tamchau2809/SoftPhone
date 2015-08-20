//package chau.vpphone;
//
//import android.os.Bundle;
//import android.preference.EditTextPreference;
//import android.preference.PreferenceActivity;
//
///**
// * Handles SIP authentication settings
// */
//public class LoginActivity extends PreferenceActivity {
//    
//	EditTextPreference ed = new 
//	
//	@SuppressWarnings("deprecation")
//	@Override
//    public void onCreate(Bundle savedInstanceState) {
//        // Note that none of the preferences are actually defined here.
//        // They're all in the XML file res/xml/preferences.xml.
//        super.onCreate(savedInstanceState);
//        addPreferencesFromResource(R.xml.preferences);
//    }
//}


package chau.vpphone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class LoginActivity extends Activity {

	EditText editUsername, editPass, editDomain;
	Button btnOkay, btnCancel;
	
	public String username,password,domain;
	public SharedPreferences loginPreferences;
    public SharedPreferences.Editor loginPrefsEditor;   
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		getFormWigets();		
		
		loginPreferences = getSharedPreferences("LOGIN", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();
        
        editUsername.setText(loginPreferences.getString("username", ""));
        editPass.setText(loginPreferences.getString("password", ""));
        editDomain.setText(loginPreferences.getString("domain", ""));       
			
		btnOkay.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	            imm.hideSoftInputFromWindow(editUsername.getWindowToken(), 0);

	            username = editUsername.getText().toString();
	            password = editPass.getText().toString();
	            domain = editDomain.getText().toString();

                loginPrefsEditor.putString("username", username);
                loginPrefsEditor.putString("password", password);
                loginPrefsEditor.putString("domain", domain);
                loginPrefsEditor.commit();
                finish();
//				else 
//				{
//	                loginPrefsEditor.clear();
//	                loginPrefsEditor.commit();
//		        }
			}
		});
		
		btnCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}
	
	private void getFormWigets()
	{
		editUsername = (EditText)findViewById(R.id.setup_username);
		editPass = (EditText)findViewById(R.id.setup_password);
		editDomain = (EditText)findViewById(R.id.setup_domain);
		btnOkay = (Button)findViewById(R.id.btnOkay);
		btnCancel = (Button)findViewById(R.id.btnCancel);
	}
	

}
