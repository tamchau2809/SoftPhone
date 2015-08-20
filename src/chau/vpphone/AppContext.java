/**
 * 
 */
package chau.vpphone;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;

public class AppContext extends Application {
	
	ArrayList<Contacts> arrContacts = new ArrayList<Contacts>();
	
	@Override
	public void onCreate() {
		super.onCreate();
		arrContacts = getContact();
	}

	public ArrayList<Contacts> getContacts() {
		return arrContacts;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<Contacts> getContact()
	{ 
	    ArrayList<Contacts> ct = new ArrayList<Contacts>();
	    try 
	    { 
	        FileInputStream fIS = openFileInput("CONTACT_LISTs");
	        ObjectInputStream oi = new ObjectInputStream(fIS);
	        ct = (ArrayList<Contacts>) oi.readObject();
	        oi.close();
	        fIS.close();
	    	
	    } catch (FileNotFoundException e) {
	        Log.e("InternalStorage", e.getMessage());       
	    } catch (IOException e) {
	        Log.e("InternalStorage", e.getMessage()); 
	    } catch (ClassNotFoundException e) {
	        Log.e("InternalStorage", e.getMessage()); 
	    } 
	    return ct;
	} 
}
