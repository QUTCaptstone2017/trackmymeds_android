package com.app.trackmymeds;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Declan on 30/08/2017.
 */

public class AddMedicationTask extends HTTPTask
{
	//Properties.
	private static final String ADD_MEDICATION_URL = "https://trackmymeds.frb.io/med_add_mobile";

	private String m_medAddType;
	private int m_medScheduleID;

	private int m_productSelect;
	private String m_reminderDate;
	private String m_reminderTime;
	private String m_repeatSelect;
	private String m_repeatCustom;
	private int m_toTake;
	private int m_toRemaining;
	private String m_nickname;

	@Override
	protected void onPostExecute(final Boolean sendSucceeded)
	{
		m_sendSucceeded = sendSucceeded;
		m_delegate.onPostExecute(m_sendSucceeded, m_taskSucceeded);
	}

	//Constructor.
	AddMedicationTask(String mobileToken, String _medAddType, int _medScheduleID, int _productSelect,
					  String _reminderDate, String _reminderTime, String _repeatSelect,
					  String _repeatCustom, int _toTake, int _toRemaining, String _nickname)
	{
		super(ADD_MEDICATION_URL, mobileToken);

		m_medAddType = _medAddType;
		m_medScheduleID = _medScheduleID;

		m_productSelect = _productSelect;
		m_reminderDate = _reminderDate;
		m_reminderTime = _reminderTime;
		m_repeatSelect = _repeatSelect;
		m_repeatCustom = _repeatCustom;
		m_toTake = _toTake;
		m_toRemaining = _toRemaining;
		m_nickname = _nickname;
	}

	protected JSONObject getSendJSON()
	{
		JSONObject result;
		try
		{
			result = new JSONObject();

			JSONObject jsonAuth = new JSONObject();
			jsonAuth.put("mobile_token", m_mobileToken);

			//TODO:
			JSONObject jsonData = new JSONObject();
			jsonData.put("medAddType", m_medAddType);

			jsonData.put("medScheduleID", m_medScheduleID);

			jsonData.put("productSelect", m_productSelect);
			jsonData.put("reminderDate", m_reminderDate);
			jsonData.put("reminderTime", m_reminderTime);
			jsonData.put("repeatSelect", m_repeatSelect);
			jsonData.put("repeatCustom", m_repeatCustom);
			jsonData.put("toTake", m_toTake);
			jsonData.put("toRemaining", m_toRemaining);
			jsonData.put("nickname", m_nickname);

			result.put("auth", jsonAuth);
			result.put("data", jsonData);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			return null;
		}

		return result;
	}

	protected void handleResponse()
	{
		//DEBUG:
		System.out.println("RESPONSE: ");
		System.out.println(m_responseString);

		//Handle response.
		try
		{
			//Convert response string to JSON.
			m_responseJSON = new JSONObject(m_responseString);

			//Get auth object from response JSON.
			JSONObject auth = m_responseJSON.getJSONObject("auth");

			//Check if this attempt was valid.
			boolean valid = auth.getBoolean("valid");
			if (valid)
			{
				System.out.println("Adding medication successful.");

				m_taskSucceeded = true;
			}
			else
			{
				//Give user feedback.
				System.out.println("Adding medication failed.");
				System.out.println("ERROR CODE:");
				System.out.println(auth.getString("error_code"));

				m_errorMessage = auth.getString("error_message");
				m_errorCode = auth.getString("error_code");

				m_taskSucceeded = false;
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
}