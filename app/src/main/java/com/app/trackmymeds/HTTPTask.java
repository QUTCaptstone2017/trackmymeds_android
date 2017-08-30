package com.app.trackmymeds;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Declan on 23/08/2017.
 */

public abstract class HTTPTask extends AsyncTask<Void, Void, Boolean>
{
	//Properties.
	public String m_mobileToken;
	protected String m_targetURL;

	protected String m_responseString;
	protected JSONObject m_responseJSON;

	public String m_errorMessage;
	public String m_errorCode;

	protected boolean m_sendSucceeded;
	protected boolean m_taskSucceeded;

	public interface AsyncResponse
	{
		void onPostExecute(boolean sendSucceeded, boolean taskSucceeded);
	}
	public AsyncResponse m_delegate;

	//Constructor.
	HTTPTask(String targetURL, String mobileToken)
	{
		m_mobileToken = mobileToken;
		m_targetURL = targetURL;

		m_responseString = "";
		m_responseJSON = null;

		m_errorMessage = "";
		m_errorCode = "";

		m_sendSucceeded = false;
		m_taskSucceeded = false;

		m_delegate = null;
	}

	public void setDelegate(AsyncResponse delegate)
	{
		m_delegate = delegate;
	}

	protected boolean send()
	{
		try
		{
			URL url = new URL(m_targetURL);
			HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();

			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			//urlConnection.setChunkedStreamingMode(0);
			urlConnection.setRequestMethod("POST");
			urlConnection.connect();

			OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
			if (!writeStream(out))
			{
				System.out.println("Problem with writing to the output stream.");
				m_errorMessage = "Problem with writing to the output stream.";
				return false;
			}

			InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			if (!readStream(in))
			{
				System.out.println("Problem with reading the input stream.");
				m_errorMessage = "Problem with reading the input stream.";
				return false;
			}

			urlConnection.disconnect();

		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
			return false;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	protected Boolean doInBackground(Void... params)
	{
		if (send())
		{
			//Successful post.
			System.out.println("Successfully sent HTTPTask.");

			handleResponse();

			return true;
		}
		else
		{
			//Unsuccessful post.
			System.out.println("Failed to send HTTPTask.");
			m_errorMessage = "Failed to send HTTPTask.";

			return false;
		}
	}

	protected String convertToString(InputStream is) throws IOException
	{
		BufferedReader r = new BufferedReader(new InputStreamReader(is));
		StringBuilder total = new StringBuilder();

		String line;
		while ((line = r.readLine()) != null)
		{
			total.append(line);
		}

		return new String(total);
	}

	@Override
	protected void onPostExecute(final Boolean sendSucceeded)
	{
		m_sendSucceeded = sendSucceeded;
	}

	@Override
	protected void onCancelled()
	{
		m_sendSucceeded = false;
	}


	protected boolean writeStream(OutputStream out)
	{
		try
		{
			//Let the specfic task create the send json.
			JSONObject sendJSON = getSendJSON();
			if (sendJSON == null)
			{
				return false;
			}

			//DEBUG:
			System.out.println("SENDING:");
			System.out.println(sendJSON.toString());

			OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");
			osw.write(sendJSON.toString());
			osw.flush();
			osw.close();
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
			return false;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}

		return true;
	}

	protected boolean readStream(InputStream in)
	{
		try
		{
			m_responseString = convertToString(in);
			return true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return false;
	}

	protected abstract JSONObject getSendJSON();

	protected abstract void handleResponse();
}