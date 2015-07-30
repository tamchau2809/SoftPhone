package chau.vpphone;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Handles SIP authentication settings
 */
public class LoginActivity extends PreferenceActivity {
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        // Note that none of the preferences are actually defined here.
        // They're all in the XML file res/xml/preferences.xml.
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}


//package chau.vpphone;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.inputmethod.InputMethodManager;
//import android.widget.Button;
//import android.widget.CheckBox;
//import android.widget.EditText;
//
//public class LoginActivity extends Activity {
//
//	EditText editUsername, editPass, editDomain;
//	Button btnLogin02;
//	CheckBox cbRemember;
//	
//	public String username,password,domain;
//	public SharedPreferences loginPreferences;
//    public SharedPreferences.Editor loginPrefsEditor;
//    private Boolean saveLogin;    
//	
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_login);
//		
//		getFormWigets();		
//		
//		loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
//        loginPrefsEditor = loginPreferences.edit();
//        
//        saveLogin = loginPreferences.getBoolean("saveLogin", false);
//        if (saveLogin == true) {
//            editUsername.setText(loginPreferences.getString("username", ""));
//            editPass.setText(loginPreferences.getString("password", ""));
//            editDomain.setText(loginPreferences.getString("domain", ""));
//            cbRemember.setChecked(true);
//        }        
//			
//		btnLogin02.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				if(v == btnLogin02)
//				{
//					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//		            imm.hideSoftInputFromWindow(editUsername.getWindowToken(), 0);
//
//		            username = editUsername.getText().toString();
//		            password = editPass.getText().toString();
//		            domain = editDomain.getText().toString();
//
//		            if (cbRemember.isChecked()) {
//		                loginPrefsEditor.putBoolean("saveLogin", true);
//		                loginPrefsEditor.putString("username", username);
//		                loginPrefsEditor.putString("password", password);
//		                loginPrefsEditor.putString("domain", domain);
//		                loginPrefsEditor.commit();
//		            } else {
//		                loginPrefsEditor.clear();
//		                loginPrefsEditor.commit();
//		            }
//					
//					if(editUsername.getText().toString().equals("admin") &&				         
//				            editPass.getText().toString().equals("admin") &&
//				            editDomain.getText().toString().equals("280.919.9.3"))
//					{
//						Intent intent = new Intent(LoginActivity.this,PhoneCallActivity.class);
//						startActivity(intent);
//					}
//				}
//			}
//		});
//	}
//	
//	private void getFormWigets()
//	{
//		editUsername = (EditText)findViewById(R.id.setup_username);
//		editPass = (EditText)findViewById(R.id.setup_password);
//		editDomain = (EditText)findViewById(R.id.setup_domain);
//		cbRemember = (CheckBox)findViewById(R.id.cbRemember);
//		btnLogin02 = (Button)findViewById(R.id.btnLogin02);	
//	}
//	
//
//}
