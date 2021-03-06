package chau.vpphone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.TextView;

public class ContactsAdapter extends ArrayAdapter<Contacts> implements Filterable{

	Activity context;
	int layoutId;
	ArrayList<Contacts> arrContact = new ArrayList<Contacts>();
	private List<Contacts> origlistContact_temp;
//	private ContactsFilter filter;
	String name;
	String num;
	ContentResolver roshan;
	
	public ContactsAdapter(Activity context, int resource, 
			List<Contacts> objects) {
		super(context, resource, objects);
		// TODO Auto-generated constructor stub
		this.context = context;
		this.layoutId = resource;
		this.origlistContact_temp = objects;
		this.arrContact.addAll(origlistContact_temp);
	}
	
	public void getContact(ContentResolver resolver)
	{
		Contacts object = new Contacts();
		Cursor phone = resolver.query(Phone.CONTENT_URI, null, null, 
				null, null);
		while(phone.moveToNext())
		{
			object.name = phone.getString(phone.getColumnIndex(Phone.DISPLAY_NAME));
			object.number = phone.getString(phone.getColumnIndex(Phone.NUMBER));
			origlistContact_temp.add(object);
		}
		phone.close();
	}

	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		convertView = context.getLayoutInflater().inflate(layoutId, null);
		
		TextView tvViewNameContact = (TextView)convertView.findViewById(R.id.tvViewNameContact);
		TextView tvViewNumber = (TextView)convertView.findViewById(R.id.tvViewNumber);
		
		Contacts ct = origlistContact_temp.get(position);
		tvViewNameContact.setText(ct.getName().trim());
		tvViewNumber.setText(ct.getNum().trim());
		return convertView;
	}
	
	@SuppressLint("DefaultLocale")
	public void cusFilter(String input)
	{
		input = input.trim().toLowerCase();
		origlistContact_temp.clear();
		if(input.length() == 0 || input == null)
		{
			origlistContact_temp.addAll(arrContact);
		}
		else
		{
			for(Contacts object : arrContact)
			{
				if(object.getName().toString().trim().toLowerCase().contains(input))
				{
					origlistContact_temp.add(object);
				}
			}
		}
		notifyDataSetChanged();
	}
	
	public void SortList()
	{
		Collections.sort(origlistContact_temp, Contacts.compare);
	}
//	@Override
//	public Filter getFilter()
//	{
//		if(filter == null)
//			filter = new ContactsFilter();
//		return filter;
//	}
//	
//	private class ContactsFilter extends Filter
//	{
//		@Override
//		protected FilterResults performFiltering(CharSequence constraint) {
//			// TODO Auto-generated method stub
//			constraint = constraint.toString().toLowerCase();
//			FilterResults res = new FilterResults();
//			if(constraint != null &&
//					constraint.toString().length()> 0)
//			{
//				ArrayList<Contacts> filterItem = new ArrayList<Contacts>();
//				for(int i = 0; i < origlistContact.size(); i++)
//				{
//					Contacts ct = origlistContact.get(i);
//					if(ct.toString().toLowerCase().contains(constraint))
//					{
//						filterItem.add(ct);
//					}
//				}
//				res.count = filterItem.size();
//				res.values = filterItem;
//			}
//			else
//			{
//				synchronized (this)
//				{
//					res.values = origlistContact;
//					res.count = origlistContact.size();
//				}
//			}
//			return res;
//		}
//
//		@SuppressWarnings("unchecked")
//		@Override
//		protected void publishResults(CharSequence constraint,
//				FilterResults results) {
//			// TODO Auto-generated method stub
//			arrContact = (ArrayList<Contacts>)results.values;
//			notifyDataSetChanged();
//			clear();
//			for(int i = 0; i < arrContact.size(); i++)
//				add(arrContact.get(i));
//			notifyDataSetInvalidated();
//		}
//		
//	}
}
