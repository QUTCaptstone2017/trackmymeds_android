package com.app.trackmymeds;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Declan on 9/10/2017.
 */

public class GetProductsTask extends HTTPTask
{
	//Properties.
	private static final String GET_PRODUCTS_URL = "https://trackmymeds.frb.io/med_products_json_mobile";

	private int m_brandID;

	@Override
	protected void onPostExecute(final Boolean sendSucceeded)
	{
		m_sendSucceeded = sendSucceeded;
		m_delegate.onPostExecute(m_sendSucceeded, m_taskSucceeded);
	}

	//Constructor.
	GetProductsTask(String mobileToken, int brandID)
	{
		super(GET_PRODUCTS_URL, mobileToken);

		this.m_brandID = brandID;
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

			jsonData.put("brandID", this.m_brandID);

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
				System.out.println("Get products request successful.");
				m_responseJSON = responseJSON;
				m_taskSucceeded = true;
			}
			else
			{
				//Give user feedback.
				System.out.println("Get products request failed.");
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