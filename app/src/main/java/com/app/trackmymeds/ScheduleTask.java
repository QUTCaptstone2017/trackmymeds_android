package com.app.trackmymeds;

/**
 * Created by Declan on 29/08/2017.
 */

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents an asynchronous schedule request.
 */
public class ScheduleTask extends HTTPTask
{
	//Properties.

	@Override
	protected void onPostExecute(final Boolean sendSucceeded)
	{
		m_sendSucceeded = sendSucceeded;
		m_delegate.onPostExecute(m_sendSucceeded, m_taskSucceeded);
	}

	//Constructor.
	ScheduleTask(String targetURL, String mobileToken)
	{
		super(targetURL, mobileToken);
	}

	protected JSONObject getSendJSON()
	{
		JSONObject result;
		try
		{
			result = new JSONObject();

			JSONObject jsonDetails = new JSONObject();

			jsonDetails.put("mobile_token", m_mobileToken);
			result.put("auth", jsonDetails);
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
			JSONObject json = new JSONObject(m_responseString);
			JSONObject auth = json.getJSONObject("auth");

			boolean valid = auth.getBoolean("valid");
			if (valid)
			{
				m_responseJSON = json.getJSONObject("data");
				System.out.println("Daily schedule request successful.");

				m_taskSucceeded = true;
			}
			else
			{
				//DEBUG:
				System.out.println("Failed to get medication schedule.");
				System.out.println("ERROR CODE:");
				System.out.println(auth.getString("error_code"));
				System.out.println("ERROR MESSAGE:");
				System.out.println(auth.getString("error_message"));

				//Store for user feedback.
				m_errorCode = auth.getString("error_code");
				m_errorMessage = auth.getString("error_message");

				m_taskSucceeded = false;
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
}
