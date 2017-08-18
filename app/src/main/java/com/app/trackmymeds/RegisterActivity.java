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
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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

public class RegisterActivity extends AppCompatActivity
{
	/**
	 * Keep track of the registration task to ensure we can cancel it if requested.
	 */
	private UserRegistrationTask m_registrationTask = null;

	// UI references.
	private EditText m_firstNameEdit;
	private EditText m_lastNameEdit;
	private EditText m_yobEdit;
	private EditText m_emailEditOne;
	private EditText m_emailEditTwo;
	private EditText m_passwordEditOne;
	private EditText m_passwordEditTwo;
	private View m_progressView;
	private View m_registrationFormView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		//Set up the registration form.
		m_firstNameEdit = (EditText) findViewById(R.id.firstNameEdit);
		m_lastNameEdit = (EditText) findViewById(R.id.lastNameEdit);
		m_yobEdit = (EditText) findViewById(R.id.yobEdit);

		m_emailEditOne = (EditText) findViewById(R.id.emailEditOne);
		m_emailEditTwo = (EditText) findViewById(R.id.emailEditTwo);

		m_passwordEditOne = (EditText) findViewById(R.id.passwordEditOne);
		m_passwordEditTwo = (EditText) findViewById(R.id.passwordEditTwo);

		m_registrationFormView = findViewById(R.id.registration_form);
		m_progressView = findViewById(R.id.registration_progress);

		Button registerButton = (Button) findViewById(R.id.button_register);
		registerButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				attemptRegistration();
			}
		});
	}

	private boolean isEmailValid(String email)
	{
		//TODO: Replace this with your own logic
		return email.contains("@");
	}

	private boolean isPasswordValid(String password)
	{
		//TODO: Replace this with your own logic
		return password.length() > 4;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	private void attemptRegistration()
	{
		if (m_registrationTask != null)
		{
			return;
		}

		//Reset errors.
		m_firstNameEdit.setError(null);
		m_lastNameEdit.setError(null);
		m_yobEdit.setError(null);

		m_emailEditOne.setError(null);
		m_emailEditTwo.setError(null);

		m_passwordEditOne.setError(null);
		m_passwordEditTwo.setError(null);

		//Store values at the time of the login attempt.
		String firstName = m_firstNameEdit.getText().toString();
		String lastName = m_lastNameEdit.getText().toString();
		String yob = m_yobEdit.getText().toString();

		String emailOne = m_emailEditOne.getText().toString();
		String emailTwo = m_emailEditTwo.getText().toString();

		String passwordOne = m_passwordEditOne.getText().toString();
		String passwordTwo = m_passwordEditTwo.getText().toString();

		boolean cancel = false;
		View focusView = null;

		//Check that emails fields match.
		if (!TextUtils.equals(emailOne, emailTwo))
		{
			//TODO: Change error text string.
			m_emailEditOne.setError(getString(R.string.error_invalid_email));
			focusView = m_emailEditOne;
			cancel = true;
		}

		//Check that password fields match.
		if (!TextUtils.equals(passwordOne, passwordTwo))
		{
			//TODO: Change error text string.
			m_passwordEditOne.setError(getString(R.string.error_invalid_password));
			focusView = m_passwordEditOne;
			cancel = true;
		}

		//Check for a valid password, if the user entered one.
		if (!TextUtils.isEmpty(passwordOne) && !isPasswordValid(passwordOne))
		{
			m_passwordEditOne.setError(getString(R.string.error_invalid_password));
			focusView = m_passwordEditOne;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(emailOne))
		{
			m_emailEditOne.setError(getString(R.string.error_field_required));
			focusView = m_emailEditOne;
			cancel = true;
		}
		else if (!isEmailValid(emailOne))
		{
			m_emailEditOne.setError(getString(R.string.error_invalid_email));
			focusView = m_emailEditOne;
			cancel = true;
		}
		else
		{
			//Check internet connectivity.
			ConnectivityManager connMgr = (ConnectivityManager)
					getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

			if (networkInfo == null || !networkInfo.isConnected())
			{
				System.out.println("Not connected to the internet.");
				m_emailEditOne.setError(getString(R.string.error_no_internet));
				cancel = true;
			}
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
			m_registrationTask = new RegisterActivity.UserRegistrationTask(firstName, lastName, yob, emailOne, passwordOne);
			m_registrationTask.execute((Void) null);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
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

			m_registrationFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			m_registrationFormView.animate().setDuration(shortAnimTime).alpha(
					show ? 0 : 1).setListener(new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationEnd(Animator animation)
				{
					m_registrationFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
			m_registrationFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	public void goSchedule()
	{
		Intent intent = new Intent(this, ScheduleActivity.class);
		startActivity(intent);
		finish();
	}

	/**
	 * Represents an asynchronous registration task used to authenticate the user.
	 */
	public class UserRegistrationTask extends AsyncTask<Void, Void, Boolean>
	{

		//TODO: Move this.
		private static final String PREFS_NAME = "TrackMyMedsPref";

		private static final String LOGIN_URL = "https://trackmymeds.frb.io/create_account_mobile";
		private final String m_firstName;
		private final String m_lastName;
		private final String m_yob;
		private final String m_email;
		private final String m_password;

		private String m_response;

		UserRegistrationTask(String firstName, String lastName, String yob,
							 String email, String password)
		{
			m_firstName = firstName;
			m_lastName = lastName;
			m_yob = yob;

			m_email = email;
			m_password = password;

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
				JSONObject jsonRegister = new JSONObject();
				JSONObject jsonDetails = new JSONObject();
				jsonDetails.put("first_name", m_firstName);
				jsonDetails.put("last_name", m_lastName);
				jsonDetails.put("yob", m_yob);
				jsonDetails.put("email", m_email);
				jsonDetails.put("password", m_password);

				jsonRegister.put("auth", jsonDetails);

				OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");
				osw.write(jsonRegister.toString());
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

		private boolean authenticate()
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
			if (authenticate())
			{
				//Successful login.
				System.out.println("Successfully sent registration request.");

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
						System.out.println("Registration successful.");
						storeMobileToken(auth.getString("mobile_token"));
						return true;
					}
					else
					{
						System.out.println("Registration failed.");
						System.out.println("ERROR CODE: ");
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
				System.out.println("Failed to send registration request.");
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
			m_registrationTask = null;
			showProgress(false);

			if (success)
			{
				Intent intent = new Intent(getBaseContext(), ScheduleActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
			else
			{
				m_passwordEditOne.setError(getString(R.string.error_incorrect_password));
				m_passwordEditOne.requestFocus();
			}
		}

		@Override
		protected void onCancelled()
		{
			m_registrationTask = null;
			showProgress(false);
		}
	}
}
