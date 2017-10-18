package com.app.trackmymeds;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Declan on 12/10/2017.
 */

public class SearchBrandTask extends HTTPTask
{
	//TODO:
	//Properties.
	private static final String BRAND_SEARCH_URL = "https://trackmymeds.frb.io/med_search_json_mobile";

	public enum SearchBrandType {SEARCH, FILTER};
	private SearchBrandType m_searchBrandType;

	private String m_searchString;

	@Override
	protected void onPostExecute(final Boolean sendSucceeded)
	{
		m_sendSucceeded = sendSucceeded;
		m_delegate.onPostExecute(m_sendSucceeded, m_taskSucceeded);
	}

	//Constructor.
	SearchBrandTask(String mobileToken, String searchString, SearchBrandType searchBrandType)
	{
		super(BRAND_SEARCH_URL, mobileToken);

		m_searchString = searchString;
		m_searchBrandType = searchBrandType;
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
			jsonData.put("s", m_searchString);

			switch (m_searchBrandType)
			{
				case SEARCH:
					jsonData.put("sb", "s");
				break;

				case FILTER:
					jsonData.put("sb", "f");
				break;

				default:
					System.out.println("SearchBrandTask::getSendJSON(): Search brand type invalid.");
				return null;

			}

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
				System.out.println("Brand search/filter successful.");

				m_taskSucceeded = true;
			}
			else
			{
				//Give user feedback.
				System.out.println("Brand search/filter failed.");
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
