package com.app.trackmymeds;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 * Created by Declan on 28/08/2017.
 */

public class LoginTask extends HTTPTask
{
	//Properties.
	private String m_email;
	private String m_password;

	@Override
	protected void onPostExecute(final Boolean sendSucceeded)
	{
		m_sendSucceeded = sendSucceeded;
		m_delegate.onPostExecute(m_sendSucceeded, m_taskSucceeded);
	}

	//Constructor.
	LoginTask(String targetURL, String mobileToken, String email, String password)
	{
		super(targetURL, mobileToken);

		m_email = email;
		m_password = password;
	}

	protected JSONObject getSendJSON()
	{
		JSONObject result;
		try
		{
			result = new JSONObject();

			JSONObject jsonCred = new JSONObject();
			jsonCred.put("email", m_email);
			jsonCred.put("password", m_password);

			result.put("auth", jsonCred);
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

			//Check if this login attempt was valid.
			boolean valid = auth.getBoolean("valid");
			if (valid)
			{
				System.out.println("Authentication successful.");
				m_mobileToken = auth.getString("mobile_token");

				m_taskSucceeded = true;
			}
			else
			{
				//Give user feedback.
				System.out.println("Authentication failed.");
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
