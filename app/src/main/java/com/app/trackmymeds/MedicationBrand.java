package com.app.trackmymeds;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Declan on 16/08/2017.
 */

public class MedicationBrand
{
	//Properties.
	public int m_id;
	public int m_supplierID;
	public String m_name;

	public JSONObject m_json;
	private boolean m_isInitialized;

	//Methods.
	public MedicationBrand()
	{
		m_id = -1;
		m_supplierID = -1;
		m_name = "";

		m_json = null;
		m_isInitialized = false;
	}

	public boolean Initialize(JSONObject json)
	{
		try
		{
			m_json = json;

			m_id = json.getInt("id");
			m_supplierID = json.getInt("supplier_fk");
			m_name = json.getString("name");
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			return false;
		}

		m_isInitialized = true;
		return true;
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
