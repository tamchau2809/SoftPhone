package chau.vpphone;

import android.annotation.SuppressLint;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

public class Contacts implements Serializable, Comparable<Contacts>
{
	private static final long serialVersionUID = 1L;
	String name;
	String number;
	String address;
	
	public Contacts(String name, String num, String add)
	{
		this.name = name;
		this.number = num;
		this.address = add;
	}
	
	public Contacts() {
		
	}
	
	String getName()
	{
		return name;
	}
	public void setName(String na)
	{
		this.name = na;
	}
	
	String getNum()
	{
		return number;
	}
	public void setNum(String nu)
	{
		this.number = nu;
	}
	
	String getAdd()
	{
		return address;
	}
	public void setAdd(String ad)
	{
		this.address = ad;
	}

	@Override
	public int compareTo(Contacts another) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public static Comparator<Contacts> compare = new Comparator<Contacts>() {
		
		@SuppressLint("DefaultLocale")
		@Override
		public int compare(Contacts lhs, Contacts rhs) {
			// TODO Auto-generated method stub
			String name1 = lhs.getName().toLowerCase().trim();
			String name2 = rhs.getName().toLowerCase().trim();
			return name1.compareTo(name2);
		}
	};
}