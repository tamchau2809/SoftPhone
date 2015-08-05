package chau.vpphone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class AddContactActivity extends Activity {
	Button btnCancel, btnSave;
	EditText edContactName, edPhoneNum, edAdd, edName;
	public static boolean checkNum;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_contact);
		
		getFormWigets();
		if(checkNum)
		{
			getNumFromMain();
		}
		onClickEvent();
	}
	
	public void getFormWigets()
	{
		btnCancel = (Button)findViewById(R.id.btnCancel);
		btnSave = (Button)findViewById(R.id.btnEditSaveContact);
		
		edName = (EditText)findViewById(R.id.edPhoneNum);		
		edContactName = (EditText)findViewById(R.id.edEditContactName);
		edPhoneNum = (EditText)findViewById(R.id.edEditSavePhonNum);
		edAdd = (EditText)findViewById(R.id.edEditSaveAddress);
	}
	
	void getNumFromMain()
	{
		String num;
		Intent intent = getIntent();
		Bundle bundle = intent.getBundleExtra("N1");
		num = bundle.getString("N");
		if(num != null) edPhoneNum.setText(num);
	}
	
	public void onClickEvent()
	{
		btnCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		btnSave.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Contacts ct = new Contacts();
				ct.setAdd(edAdd.getText() + "");
				ct.setName(edContactName.getText() + "");
				ct.setNum(edPhoneNum.getText() + "");
				
				Intent intent = getIntent();
				Bundle bundle = new Bundle();
				bundle.putSerializable("ADDCONTACT", ct);
				intent.putExtra("DATA", bundle);
				setResult(ContactsActivity.ADD_CONTACT_SU, intent);
				finish();
			}
		});
	}
}
