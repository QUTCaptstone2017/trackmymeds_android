package com.app.trackmymeds;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Declan on 16/08/2017.
 */

public class MedicationScheduleItem
{
	//JSON Example:
	//{
	// "id":1838,
	// "med_schedule_id":233,
	// "date_to_take":"2017-10-12",
	// "is_taken":0,
	// "is_dismissed":0,
	// "dosage":5,
	// "time_to_take":"09:00:00",
	// "description":"paracetamol 500 mg suppository, 24",
	// "name":"Panadol",
	// "pack_remaining":200,
	// "product_id":2839,
	// "day_interval":1,
	// "nickname":"Headache"
	//}

	//Properties.
	public int m_id;
	public int m_scheduleID;
	public String m_brandName; //name
	public int m_productID;
	public String m_productDescription; //description
	public String m_timeToTake;
	public String m_dateToTake;
	public int m_dayInterval;
	public int m_toTake; //dosage
	public int m_toRemaining; //pack_remaining
	public String m_nickname;
	public boolean m_isTaken;
	public boolean m_isDismissed;

	private boolean m_isInitialized;
	public JSONObject m_json;

	//Methods.
	public MedicationScheduleItem()
	{
		m_id = -1;
		m_scheduleID = -1;
		m_brandName = "UNDEFINED";
		m_productID = -1;
		m_productDescription = "UNDEFINED";
		m_timeToTake = "UNDEFINED";
		m_dateToTake = "UNDEFINED";
		m_dayInterval = -1;
		m_toTake = -1;
		m_toRemaining = -1;
		m_nickname = "UNDEFINED";
		m_isTaken = false;
		m_isDismissed = false;

		m_isInitialized = false;
		m_json = null;
	}

	//TODO:
	public boolean Initialize(JSONObject json)
	{
		try
		{
			m_json = json;

			m_id = json.getInt("id");
			m_scheduleID = json.getInt("med_schedule_id");
			m_brandName = json.getString("name");
			m_productID = json.getInt("product_id");
			m_productDescription = json.getString("description");
			m_dateToTake = json.getString("date_to_take");
			m_timeToTake = json.getString("time_to_take");
			m_dayInterval = json.getInt("day_interval");
			m_toTake = json.getInt("dosage");
			m_toRemaining = json.getInt("pack_remaining");
			m_nickname = json.getString("nickname");
			m_isTaken = json.getInt("is_taken") == 1;
			m_isDismissed = json.getInt("is_dismissed") == 1;
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
		if (!m_isInitialized)
		{
			return "";
		}

		String resultString = "ID: " + String.valueOf(m_id) + "\n" +
				"Schedule ID: " + String.valueOf(m_scheduleID) + "\n" +
				"Brand Name: " + m_brandName + "\n" +
				"Product ID: " + String.valueOf(m_productID) + "\n" +
				"Product Description:\n" + String.valueOf(m_productDescription) + "\n" +
				"Time To Take: " + String.valueOf(m_timeToTake) + "\n" +
				"Date To Take: " + String.valueOf(m_dateToTake) + "\n" +
				"Day Interval: " + String.valueOf(m_dayInterval) + "\n" +
				"Number Of Items To Take: " + String.valueOf(m_toTake) + "\n" +
				"Number Of Items Remaining: " + String.valueOf(m_toRemaining) + "\n" +
				"Nickname: " + String.valueOf(m_nickname) + "\n" +
				"Is Taken: " + String.valueOf(m_isTaken) + "\n" +
				"Is Dismissed: " + String.valueOf(m_isDismissed);

		return resultString;
	}

	public String toDisplayString()
	{
		if (!m_isInitialized)
		{
			return "";
		}

		//TODO: Format the time string.

		String resultString =
				"Nickname: " + String.valueOf(m_nickname) + "\n" +
				"Number Of Items To Take: " + String.valueOf(m_toTake) + "\n" +
				"Time To Take: " + String.valueOf(m_timeToTake);

		return resultString;
	}
}
