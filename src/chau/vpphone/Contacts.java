package chau.vpphone;

import java.io.Serializable;
import java.util.ArrayList;

public class Contacts implements Serializable
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
}