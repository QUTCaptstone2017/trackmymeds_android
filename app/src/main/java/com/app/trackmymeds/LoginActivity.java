package com.app.trackmymeds;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity
{
	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private static String m_targetURL = "https://trackmymeds.frb.io/sign_in_mobile";
	private LoginTask m_loginTask = null;
	private StorageManager m_storageManager;

	// UI references.
	private AutoCompleteTextView mEmailView;
	private EditText mPasswordView;
	private ProgressBar m_progressView;
	private View mLoginFormView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		//Setup activity.
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		m_storageManager = new StorageManager();

		//Set up the login form.
		mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent)
			{
				if (id == R.id.login || id == EditorInfo.IME_NULL)
				{
					attemptLogin();
					return true;
				}
				return false;
			}
		});

		mLoginFormView = findViewById(R.id.login_form);
		m_progressView = (ProgressBar)findViewById(R.id.login_progress);

		Button mEmailSignInButton = (Button) findViewById(R.id.button_sign_in);
		mEmailSignInButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				attemptLogin();
			}
		});

		Button fbSignInButton = (Button) findViewById(R.id.button_sign_in_fb);
		fbSignInButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				attemptLogin();
			}
		});

		Button registerButton = (Button) findViewById(R.id.button_register);
		registerButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				goRegister();
			}
		});

		//On old phones it does not use progress bar styling.
		int notifColour = ResourcesCompat.getColor(getResources(), R.color.app_notif, null);
		Drawable d = m_progressView.getIndeterminateDrawable();
		d.setColorFilter(notifColour, PorterDuff.Mode.SRC_IN);
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

			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime).alpha(
					show ? 0 : 1).setListener(new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationEnd(Animator animation)
				{
					mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	private void attemptLogin()
	{
		if (m_loginTask != null)
		{
			return;
		}

		//Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		//Store values at the time of the login attempt.
		String email = mEmailView.getText().toString();
		String password = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password, if the user entered one.
		if (!TextUtils.isEmpty(password) && !isPasswordValid(password))
		{
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(email))
		{
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		}
		else if (!isEmailValid(email))
		{
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
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
				mEmailView.setError(getString(R.string.error_no_internet));
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
			m_loginTask = new LoginTask(m_targetURL, "", email, password);

			//Set anonymous response callback.
			m_loginTask.setDelegate(new LoginTask.AsyncResponse()
			{
				@Override
				public void onPostExecute(boolean sendSucceeded, boolean taskSucceeded)
				{
					showProgress(false);

					if (sendSucceeded)
					{
						//Request sent successfully.
						if (taskSucceeded)
						{
							//Login successful.
							Context context = getApplicationContext();
							m_storageManager.saveMobileToken(context, m_loginTask.m_mobileToken);
							goSchedule();
						}
						else
						{
							//Failed to login.
							mEmailView.setError(m_loginTask.m_errorMessage);
						}
					}
					else
					{
						//Failed to send request.
						mEmailView.setError(m_loginTask.m_errorMessage);
					}

					m_loginTask = null;
				}
			});

			m_loginTask.execute((Void)null);
		}
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

	public void goRegister()
	{
		Intent intent = new Intent(this, RegisterActivity.class);
		startActivity(intent);

		//finish();
	}

	public void goSchedule()
	{
		Intent intent = new Intent(this, ScheduleActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);

		finish();
	}
}

