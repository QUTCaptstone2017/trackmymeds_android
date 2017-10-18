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
	AddMedicationTask(String mobileToken, int productSelect, String reminderDate,
					  String reminderTime, String repeatSelect, String repeatCustom, int toTake,
					  int toRemaining, String nickname)
	{
		super(ADD_MEDICATION_URL, mobileToken);

		m_productSelect = productSelect;
		m_reminderDate = reminderDate;
		m_reminderTime = reminderTime;
		m_repeatSelect = repeatSelect;
		m_repeatCustom = repeatCustom;
		m_toTake = toTake;
		m_toRemaining = toRemaining;
		m_nickname = nickname;
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
			jsonData.put("medAddType", "add");
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