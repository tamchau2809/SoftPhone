package chau.vpphone;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;

@SuppressLint("SdCardPath")
public class HistoryActivity extends ListActivity {

	private String FILE_NAME = "history";
	private ArrayList<HistoryInfo> historyInfo = new ArrayList<HistoryInfo>();
	FileInputStream fis;
	ObjectInputStream ois;
	HistoryAdapter adapter;
	
	public final static String ADDRTOCALL="chau.vpphone.ADDRTOCALL";
	public static String PATH = "/data/data/chau.vpphone/files/";
	
	boolean callConnected;
	static final int CALL_CONNECTED_DIALOG=0;
	AdapterContextMenuInfo info;
	
	private EditText filter = null;
		
	private static final int CLEAR_LOG = 1;
	private static final int SEARCH = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		getHistoryContact();
		
		filter = (EditText)findViewById(R.id.search_box);
		filter.addTextChangedListener(watcher);
		
		adapter = new HistoryAdapter(historyInfo);
		setListAdapter(adapter);
		registerForContextMenu(getListView());
		Intent intent = getIntent();
		callConnected = intent.getBooleanExtra(PhoneCallActivity.CALLSTATUS, false);
	}
	
	private TextWatcher watcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			if(filter.getText() == null || filter.getText().length() == 0)
			{
				adapter.notifyDataSetChanged();
			}
			else 
				adapter.getFilter().filter(s.toString());
		}
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(0, CLEAR_LOG, 0, "Clear History");
		menu.add(0, SEARCH, 0, "Search");
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch(item.getItemId())
		{
		case CLEAR_LOG:
			showDialog(CLEAR_LOG);
			break;
		case SEARCH:
			filter = (EditText)findViewById(R.id.search_box);
			filter.setVisibility(View.VISIBLE);
			break;
		}
		return true;
	}
	
	public void getHistoryContact()
	{
		try
		{
			fis = openFileInput(FILE_NAME);
			ois = new ObjectInputStream(fis);
			HistoryInfo info;
			while((info = (HistoryInfo)ois.readObject()) != null)
			{
				historyInfo.add(info);
			}
		} catch(Exception e)
		{
			try
			{
				ois.close();
			} catch(Exception ex)
			{}
		}
		Collections.reverse(historyInfo);
	}
	
	@Override
	public void onListItemClick(ListView parent, View v, int pos, long id)
	{
		openContextMenu(v);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_menu, menu);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		info = (AdapterContextMenuInfo)item.getMenuInfo();
		switch(item.getItemId())
		{
		case R.id.itemCall:
			if(callConnected)
			{
				showDialog(CALL_CONNECTED_DIALOG);
			}
			else
			{
				PhoneCallActivity.callSomeone = true;
				Intent intent = new Intent(this, PhoneCallActivity.class);
				intent.putExtra(ADDRTOCALL, String.valueOf(historyInfo.get(info.position)));
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
			}
			return true;
		case R.id.itemDel:
			historyInfo.remove(info.position);
			adapter.notifyDataSetChanged();
			try
			{
				int i = historyInfo.size() - 1;
				File oldFile = new File(PATH + FILE_NAME);
				File newFile = new File(PATH + "history2");
				FileOutputStream fos = openFileOutput("history2", Context.MODE_APPEND);
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				for(; i >= 0; i--)
				{
					oos.writeObject(historyInfo.get(i));
				}
				oos.flush();
				fos.close();
				oos.close();
				boolean deleted = oldFile.delete();
				if(deleted)
				{
					boolean renamed = newFile.renameTo(oldFile);
					if(renamed)
					{
						Toast.makeText(this, "Record deleted sucessfully", Toast.LENGTH_SHORT).show();
					}
				}
			}
			catch(Exception e)
			{
				Toast.makeText(this, "Oops.." + e, Toast.LENGTH_SHORT).show();
			}
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	@Override
	public Dialog onCreateDialog(int id)
	{
		AlertDialog dialog = null;
		switch(id)
		{
		case CALL_CONNECTED_DIALOG:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("You're in call. \nIf you select this option your current call will be disconnected")
					.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							PhoneCallActivity.endCall = true;
							PhoneCallActivity.callSomeone = true;
							
							Intent intent = new Intent(getBaseContext(), PhoneCallActivity.class);
							intent.putExtra(ADDRTOCALL, String.valueOf(historyInfo.get(info.position)));
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
							startActivity(intent);
						}
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							
						}
					})
					.setCancelable(false);
			dialog = builder.create();
			break;
		case CLEAR_LOG:
			AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
			builder2.setMessage("Are you sure want to CLEAR history?")
			.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					File f = new File(PATH + FILE_NAME);
					boolean b = f.delete();
					if(b)
					{
						historyInfo.clear();
						adapter.notifyDataSetChanged();
						Toast.makeText(getApplicationContext(), "Completed!", Toast.LENGTH_SHORT).show();
					}
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			})
			.setCancelable(false);
			dialog = builder2.create();
			break;
		}
		return dialog;
	}
	
	class HistoryAdapter extends ArrayAdapter<HistoryInfo>
	{

		public HistoryAdapter(ArrayList<HistoryInfo> objects) {
			super(HistoryActivity.this, R.layout.activity_history_custom_view, 
					R.id.sipaddr, objects);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public View getView(int pos, View convertView, ViewGroup parent)
		{
			View view = super.getView(pos, convertView, parent);
			if(view == null)
			{
				LayoutInflater inflater = getLayoutInflater();
				view = inflater.inflate(R.layout.activity_history_custom_view, parent, false);
			}
			
			TextView sipaddr = (TextView)view.findViewById(R.id.sipaddr);
			sipaddr.setText(historyInfo.get(pos).getSipAddr());
			
			TextView calldate = (TextView)view.findViewById(R.id.calldate);
			calldate.setText(historyInfo.get(pos).getCallDate());
			
			TextView callduration = (TextView)view.findViewById(R.id.callduration);
			callduration.setText(historyInfo.get(pos).getCallDuration());
			
			ImageView icon = (ImageView)view.findViewById(R.id.icon);
			if(historyInfo.get(pos).isOutgoingCall())
			{
				icon.setImageResource(R.drawable.out_call);
			}
			else icon.setImageResource(R.drawable.in_call);
			if(historyInfo.get(pos).isMissedCall())
				icon.setImageResource(R.drawable.miss_call);			
			return view;
		}		
	}
}
