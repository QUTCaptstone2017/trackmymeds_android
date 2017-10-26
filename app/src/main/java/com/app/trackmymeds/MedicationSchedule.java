package com.app.trackmymeds;


import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Declan on 16/08/2017.
 */

public class MedicationSchedule
{
	//Properties.
	public int m_id;
	public String m_brandName; //name
	public int m_productID;
	public String m_productDescription; //description
	public String m_timeToTake;
	public String m_dateToTake;
	public int m_dayInterval;
	public int m_toTake; //dosage
	public int m_toRemaining; //pack_remaining
	public String m_nickname;

	public JSONObject m_json;
	private boolean m_isInitialized;

	//Methods.
	public MedicationSchedule()
	{
		m_id = -1;
		m_brandName = "UNDEFINED";
		m_productID = -1;
		m_productDescription = "UNDEFINED";
		m_timeToTake = "UNDEFINED";
		m_dateToTake = "UNDEFINED";
		m_dayInterval = -1;
		m_toTake = -1;
		m_toRemaining = -1;
		m_nickname = "UNDEFINED";

		m_json = null;
		m_isInitialized = false;
	}

	public boolean Initialize(JSONObject json)
	{
		try
		{
			m_json = json;

			m_id = json.getInt("med_schedule_id");
			m_brandName = json.getString("name");
			m_productID = json.getInt("product_id");
			m_productDescription = json.getString("description");
			m_dateToTake = json.getString("date_to_take");
			m_timeToTake = json.getString("time_to_take");
			m_dayInterval = json.getInt("day_interval");
			m_toTake = json.getInt("dosage");
			m_toRemaining = json.getInt("pack_remaining");
			m_nickname = json.getString("nickname");
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			return false;
		}

		m_isInitialized = true;
		return true;
	}

	public String getDisplayDate()
	{
		String dateString = "";
		try
		{
			SimpleDateFormat parseDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date date = parseDateFormat.parse(m_dateToTake);
			SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd");
			dateString = dateFormat.format(date);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}

		return dateString;
	}

	public String getDisplayTime()
	{
		String timeString = "";
		try
		{
			SimpleDateFormat parseTimeFormat = new SimpleDateFormat("HH:mm:ss");
			Date time = parseTimeFormat.parse(m_timeToTake);
			SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
			timeString = timeFormat.format(time);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}

		return timeString;
	}

	@Override
	public String toString()
	{
		if (!m_isInitialized)
		{
			return "";
		}

		String resultString = "ID: " + String.valueOf(m_id) + "\n" +
				"Brand Name: " + m_brandName + "\n" +
				"Product ID: " + String.valueOf(m_productID) + "\n" +
				"Product Description:\n" + String.valueOf(m_productDescription) + "\n" +
				"Time To Take: " + String.valueOf(m_timeToTake) + "\n" +
				"Date To Take: " + String.valueOf(m_dateToTake) + "\n" +
				"Day Interval: " + String.valueOf(m_dayInterval) + "\n" +
				"Number Of Items To Take: " + String.valueOf(m_toTake) + "\n" +
				"Number Of Items Remaining: " + String.valueOf(m_toRemaining) + "\n" +
				"Nickname: " + String.valueOf(m_nickname);

		return resultString;
	}

	public String toDisplayString()
	{
		if (!m_isInitialized)
		{
			return "";
		}

		//Format the date and time string.
		String resultString = "";

		String dateString = getDisplayDate();
		String timeString = getDisplayTime();

		resultString =
				"Nickname: " + String.valueOf(m_nickname) + "\n" +
				"Brand Name: " + m_brandName + "\n" +
				"Product Description:\n" + String.valueOf(m_productDescription) + "\n" +
				"Starting Date: " + dateString + "\n" +
				"Starting Time: " + timeString;

		return resultString;
	}
}
