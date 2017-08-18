package com.app.trackmymeds;

/**
 * Created by Declan on 16/08/2017.
 */

public class MedicationBrand
{
	//Properties.
	public int m_id;
	public int m_supplierID;
	public String m_name;

	//Methods.
	public MedicationBrand()
	{
		m_id = -1;
		m_supplierID = -1;
		m_name = "";
	}

	@Override
	public String toString()
	{
		String resultString = "ID: " + String.valueOf(m_id) + "\n" +
				"Supplier ID: " + String.valueOf(m_supplierID) + "\n" +
				"Name: " + m_name;

		return resultString;
	}
}
