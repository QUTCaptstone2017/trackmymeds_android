package com.app.trackmymeds;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DeleteAccountActivity extends AppCompatActivity
{
	//Properties.
	private DeleteAccountTask m_deleteAccountTask;

	private View m_progressView;
	private View m_deleteAccountFormView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_delete_account);

		m_progressView = findViewById(R.id.delete_progress);
		m_deleteAccountFormView = findViewById(R.id.delete_account_form);

		Button deleteButton = (Button) findViewById(R.id.delete_account_button);
		deleteButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				attemptDeletion();
			}
		});
	}

	public void attemptDeletion()
	{
		if (m_deleteAccountTask != null)
		{
			return;
		}

		//Reset errors.
		//m_firstNameEdit.setError(null);

		boolean cancel = false;
		View focusView = m_deleteAccountFormView;

		//Check internet connectivity.
		ConnectivityManager connMgr = (ConnectivityManager)
				getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo == null || !networkInfo.isConnected())
		{
			System.out.println("Not connected to the internet.");
			cancel = true;
		}

		if (cancel)
		{
			System.out.println("Cancelling login attempt...");
			//There was an error; don't attempt login and focus the first
			//form field with an error.
			focusView.requestFocus();
		}
		else
		{
			System.out.println("Connected to the internet.");

			//Show the progress spinner...
			showProgress(true);

			//Create and run an auth task in the background.
			m_deleteAccountTask = new DeleteAccountActivity.DeleteAccountTask();
			m_deleteAccountTask.execute((Void) null);
		}
	}

	/**
	 * Shows the progress UI and hides the delete form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show)
	{
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
		{
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			m_deleteAccountFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			m_deleteAccountFormView.animate().setDuration(shortAnimTime).alpha(
					show ? 0 : 1).setListener(new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationEnd(Animator animation)
				{
					m_deleteAccountFormView.setVisibility(show ? View.GONE : View.VISIBLE);
				}
			});

			m_progressView.setVisibility(show ? View.VISIBLE : View.GONE);
			m_progressView.animate().setDuration(shortAnimTime).alpha(
					show ? 1 : 0).setListener(new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationEnd(Animator animation)
				{
					m_progressView.setVisibility(show ? View.VISIBLE : View.GONE);
				}
			});
		}
		else
		{
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			m_progressView.setVisibility(show ? View.VISIBLE : View.GONE);
			m_deleteAccountFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous delete account request.
	 */
	public class DeleteAccountTask extends AsyncTask<Void, Void, Boolean>
	{
		private static final String PREFS_NAME = "TrackMyMedsPref";
		private static final String LOGIN_URL = "https://trackmymeds.frb.io/delete_account_mobile";

		String m_response;

		DeleteAccountTask()
		{
			m_response = "";
		}

		private String getMobileToken()
		{
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			String mobileToken = settings.getString("mobile_token", "");

			return mobileToken;
		}

		private void storeMobileToken(String mobileToken)
		{
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("mobile_token", mobileToken);

			editor.commit();
		}

		private boolean writeStream(OutputStream out)
		{
			try
			{
				JSONObject jsonAuth = new JSONObject();
				JSONObject jsonDetails = new JSONObject();

				String authToken = getMobileToken();
				jsonDetails.put("mobile_token", authToken);
				jsonAuth.put("auth", jsonDetails);

				//DEBUG:
				//System.out.println(jsonAuth.toString());

				OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");
				osw.write(jsonAuth.toString());
				osw.flush();
				osw.close();

			} catch (JSONException e)
			{
				e.printStackTrace();
				return false;
			} catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
				return false;
			} catch (IOException e)
			{
				e.printStackTrace();
				return false;
			}

			return true;
		}

		private boolean readStream(InputStream in)
		{
			try
			{
				m_response = convertToString(in);
				return true;
			} catch (IOException e)
			{
				e.printStackTrace();
			}

			return false;
		}

		private boolean send()
		{
			try
			{
				URL url = new URL(LOGIN_URL);
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

				urlConnection.setDoInput(true);
				urlConnection.setDoOutput(true);
				urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
				//urlConnection.setChunkedStreamingMode(0);
				urlConnection.setRequestMethod("POST");
				urlConnection.connect();

				OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
				if (!writeStream(out))
				{
					System.out.println("Problem with reading the output stream.");
					return false;
				}

				InputStream in = new BufferedInputStream(urlConnection.getInputStream());
				if (!readStream(in))
				{
					System.out.println("Problem with reading the input stream.");
					return false;
				}

				urlConnection.disconnect();

			} catch (MalformedURLException e)
			{
				e.printStackTrace();
				return false;
			} catch (IOException e)
			{
				e.printStackTrace();
				return false;
			}

			return true;
		}

		@Override
		protected Boolean doInBackground(Void... params)
		{
			m_response = "";
			if (send())
			{
				//Successful post.
				System.out.println("Successfully sent delete account request.");

				//DEBUG:
				System.out.println("RESPONSE: ");
				System.out.println(m_response);

				//Handle response.
				try
				{
					JSONObject responseJSON = new JSONObject(m_response);
					JSONObject auth = responseJSON.getJSONObject("auth");
					boolean valid = auth.getBoolean("valid");
					if (valid)
					{
						//DEBUG:
						System.out.println("Account deletion successful.");

						return true;
					}
					else
					{
						//DEBUG:
						System.out.println("Account deletion failed.");
						System.out.println("ERROR CODE:");
						System.out.println(auth.getString("error_code"));

						return false;
					}
				} catch (JSONException e)
				{
					e.printStackTrace();
					return false;
				}
			}
			else
			{
				System.out.println("Failed to send delete account request.");
				return false;
			}
		}

		private String convertToString(InputStream is) throws IOException
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
		protected void onPostExecute(final Boolean success)
		{
			m_deleteAccountTask = null;
			showProgress(false);

			if (success)
			{
				//Deleted user account.
				Intent intent = new Intent(getBaseContext(), LoginActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
			else
			{
				//Failed to delete account.
				//m_passwordEditOne.setError(getString(R.string.error_incorrect_password));
				//m_passwordEditOne.requestFocus();
			}
		}

		@Override
		protected void onCancelled()
		{
			m_deleteAccountTask = null;
			showProgress(false);
		}
	}
}