package chau.vpphone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class EditContactActivity extends Activity {

	Button btnCancel, btnSave;
	EditText edName, edNum, edAdd;
	
	Contacts contact = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_contact);
		
		getFormWiget();		
		getDefaultData();
		onClickEvent();		
	}
	
	public void getDefaultData()
	{
		Intent in = getIntent();
		Bundle b = in.getBundleExtra("DATA");
		contact = (Contacts)b.getSerializable("CONTACT");
		edName.setText(contact.getName());
		edAdd.setText(contact.getAdd());
		edNum.setText(contact.getNum());
	}
	
	public void getFormWiget()
	{
		btnCancel = (Button)findViewById(R.id.btnCancel);
		btnSave = (Button)findViewById(R.id.btnEditSaveContact);
		
		edName = (EditText)findViewById(R.id.edEditContactName);
		edNum = (EditText)findViewById(R.id.edEditSavePhonNum);
		edAdd = (EditText)findViewById(R.id.edEditSaveAddress);
	}
	
	public void onClickEvent()
	{
		btnCancel.setOnClickListener(new OnClickListener() 
		{		
			@Override
			public void onClick(View v) {
			// TODO Auto-generated method stub
				finish();
			}
		});
		
		btnSave.setOnClickListener(new OnClickListener() 
		{			
			@Override
			public void onClick(View v) {
			// TODO Auto-generated method stub
				Intent i = getIntent();
				contact.setName(edName.getText() + "");
				contact.setAdd(edAdd.getText() + "");
				contact.setNum(edNum.getText() + "");
				
				Bundle bun = new Bundle();
				bun.putSerializable("CONTACT", contact);
				i.putExtra("DATA", bun);
				setResult(ContactsActivity.EDIT_CONTACT_SU,i);
				finish();
			}
		});
	}
}

