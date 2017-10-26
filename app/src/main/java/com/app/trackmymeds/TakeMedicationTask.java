package com.app.trackmymeds;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Declan on 9/10/2017.
 */

public class TakeMedicationTask extends HTTPTask
{
	//Properties.
	private static final String TAKE_MEDICATION_URL = "https://trackmymeds.frb.io/take_med_schedule_item_mobile";
	private static final String UNTAKE_MEDICATION_URL = "https://trackmymeds.frb.io/untake_med_schedule_item_mobile";

	private int m_scheduleItemID;

	@Override
	protected void onPostExecute(final Boolean sendSucceeded)
	{
		m_sendSucceeded = sendSucceeded;
		m_delegate.onPostExecute(m_sendSucceeded, m_taskSucceeded);
	}

	//Constructor.
	TakeMedicationTask(String mobileToken, int _scheduleItemID, boolean unTake)
	{
		super((unTake ? UNTAKE_MEDICATION_URL : TAKE_MEDICATION_URL), mobileToken);

		this.m_scheduleItemID = _scheduleItemID;
	}

	protected JSONObject getSendJSON()
	{
		JSONObject result;
		try
		{
			result = new JSONObject();

			JSONObject jsonAuth = new JSONObject();
			jsonAuth.put("mobile_token", m_mobileToken);

			JSONObject jsonData = new JSONObject();

			//TODO:
			jsonData.put("item_id", this.m_scheduleItemID);

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
			JSONObject responseJSON = new JSONObject(m_responseString);

			//Get auth object from response JSON.
			JSONObject auth = responseJSON.getJSONObject("auth");

			//Check if this attempt was valid.
			boolean valid = auth.getBoolean("valid");
			if (valid)
			{
				System.out.println("Take/untake medication request successful.");
				m_responseJSON = responseJSON;
				m_taskSucceeded = true;
			}
			else
			{
				//Give user feedback.
				System.out.println("Take/untake medication request failed.");
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