package chau.vpphone;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

public class ContactsActivity extends Activity {

	//Khai báo các Request + Result code để xử lý Intent for result
	public static final int ACTIVITY_OPEN_ADD_CONT=1;
	public static final int ACTIVITY_OPEN_EDIT_CONT=2;
	public static final int ADD_CONTACT_SU=3;
	public static final int EDIT_CONTACT_SU=4;
	public static final int UPDATE_LIST=5;
	
	ListView lv;
	EditText edSearch;
	
	ImageButton btnAddCTSmall;
	
	ContactsAdapter contactsAdapter = null;
	
	ArrayList<Contacts> arrContacts;
	AppContext appContext;
	
	Contacts contactSelected = null;
	Contacts ct = null;
	private int position=-1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts);
		
		appContext = (AppContext)getApplication();
		arrContacts = appContext.getContacts();
		SetLayout();
		setEventClick();
		//fakeData();
		
		//bật chức năng lọc cho listview
		lv.setTextFilterEnabled(true);
		//Edittext dùng để search
		edSearch = (EditText)findViewById(R.id.edSearch);
		edSearch.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				contactsAdapter.cusFilter(s.toString());
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
		
		btnAddCTSmall = (ImageButton)findViewById(R.id.btnAddCT);
		btnAddCTSmall.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				doSaveContact();
				//SavePreferences(arrContacts);
			}
		});
	}
	
	@Override
	protected void onStop()
	{
//		if(edSearch != null)
//		{
//			edSearch.setText(null);
//		}
		SavePreferences(arrContacts);
		super.onStop();
	}
	
	public void fakeData()
	{
		Contacts cont = null;
		
		cont = new Contacts("MoBi", "090", "ops");
		arrContacts.add(cont);
		
		cont = new Contacts("CV", "559", "ops");
		arrContacts.add(cont);
		
		cont = new Contacts("A", "01", "ops");
		arrContacts.add(cont);
		
		contactsAdapter.notifyDataSetChanged();
	}
	
	public void SetLayout()
	{
		lv = (ListView)findViewById(R.id.lvContact);
		contactsAdapter = new ContactsAdapter(this, 
				R.layout.activity_contacts_view, arrContacts);
		lv.setAdapter(contactsAdapter);
		registerForContextMenu(lv);
	}
	
	public void setEventClick()
	{
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) 
			{
				contactSelected=arrContacts.get(arg2);
				position = arg2;
				return false;
			}
		});
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.activity_menu_contact, menu);
		menu.getItem(0).setTitle("Call To " + contactSelected.getName());
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		switch (item.getItemId()) 
		{
		case R.id.menuCall:
			
			break;
		case R.id.menuEdit:
			doEditContact();
			break;
		case R.id.menuDelete:
			doDeleteContact();
			break;		
		default:
			break;
		}
		
		return super.onContextItemSelected(item);
	}
	
	public void doDeleteContact()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Deleting...");
		builder.setMessage("Are You Sure Want To Delete [" + 
				contactSelected.getName() + "]?");
		//builder.setIcon(R.drawable.ic) //thêm Icon
		builder.setNegativeButton("No", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
			}
		});
		builder.setPositiveButton("Yes", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				arrContacts.remove(contactSelected);
				contactsAdapter.notifyDataSetChanged();
			}
		});
		builder.show();
		//SavePreferences(arrContacts);
	}

	public void doEditContact()
	{
		Intent intent = new Intent(this, EditContactActivity.class);
		Bundle b = new Bundle();
		b.putSerializable("CONTACT", contactSelected);
		intent.putExtra("DATA", b);
		startActivityForResult(intent, ACTIVITY_OPEN_EDIT_CONT);
	}
	
	public void doSaveContact()
	{
		String number;
		Intent intent1 = getIntent();
		Bundle bundle = intent1.getBundleExtra("NUM_EXTRA");
		number = bundle.getString("NUMBER");
		
		bundle.putString("NUMBER", number);
		
		Intent intent = new Intent(this, AddContactActivity.class);
		intent.putExtra("NUM", bundle);
		startActivityForResult(intent, ACTIVITY_OPEN_ADD_CONT);
	}

	/**
	 * Hàm onActivityResult để xử lý kết quả trả về
	 * sau khi startActivityForResult kết thúc
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == EDIT_CONTACT_SU)
		{
			
			Bundle bundle = data.getBundleExtra("DATA");
			Contacts ct = (Contacts)bundle.getSerializable("CONTACT");
			addCT(ct, arrContacts);
			contactsAdapter.notifyDataSetChanged();
			//SavePreferences(arrContacts);
		}
		else if (resultCode == ADD_CONTACT_SU)
		{
			Bundle bundle = data.getBundleExtra("DATA");
			Contacts ct = (Contacts)bundle.getSerializable("ADDCONTACT");
			addCT(ct, arrContacts);
			contactsAdapter.notifyDataSetChanged();
			//SavePreferences(arrContacts);
		}
		//SavePreferences(arrContacts);
	}
		
	/*
	 * update contact để không bị trùng
	 */
	public void addCT(Contacts ct, ArrayList<Contacts> arrContacts)
	{
		int index = 0;
		for(; index < arrContacts.size(); index++)
		{
			Contacts nvOld = arrContacts.get(index);
			if(nvOld.getNum().trim().equalsIgnoreCase(ct.getNum().trim()))
				break;
		}
		if(index < arrContacts.size())
			arrContacts.set(index, ct);
		else arrContacts.add(ct);
		SavePreferences(arrContacts);
	}

	/*
	 * Hàm lưu danh sách contact lại
	 * dùng lưu Internal Storage
	 */
	public void SavePreferences(ArrayList<Contacts> ct)
	{
		try
		{
			FileOutputStream fOS = openFileOutput("CONTACT_LISTs", MODE_PRIVATE);
			ObjectOutputStream oOS = new ObjectOutputStream(fOS);
			oOS.writeObject(ct);
			oOS.flush();
			oOS.close();
			fOS.close();
		}
		catch(Exception e)
		{
			Log.e("InternalStorage", e.getMessage());
		}
	}
}
