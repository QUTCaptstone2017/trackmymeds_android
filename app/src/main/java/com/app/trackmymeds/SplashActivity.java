package com.app.trackmymeds;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

public class SplashActivity extends AppCompatActivity
{
	//Properties.
	private static String m_targetURL = "https://trackmymeds.frb.io/sign_in_token_mobile";

	TokenLoginTask m_tokenLoginTask;
	StorageManager m_storageManager;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		m_storageManager = new StorageManager();
		String mobileToken = m_storageManager.getMobileToken(getApplicationContext());
		if (mobileToken.length() > 0)
		{
			attemptTokenLogin(mobileToken);
		}
		else
		{
			goLogin();
		}

		ProgressBar loadingBarView = (ProgressBar)findViewById(R.id.loadingBar);

		//On old phones it does not use progress bar styling.
		int notifColour = ResourcesCompat.getColor(getResources(), R.color.app_notif, null);
		Drawable d = loadingBarView.getIndeterminateDrawable();
		d.setColorFilter(notifColour, PorterDuff.Mode.SRC_IN);
	}

	private void attemptTokenLogin(String mobileToken)
	{
		if (m_tokenLoginTask != null)
		{
			return;
		}

		System.out.println("Login attempt from splash screen.");

		boolean cancel = false;

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
			System.out.println("Cancelling token login request...");
		}
		else
		{
			System.out.println("Connected to the internet.");

			//TODO: Probably do input validation here (mobile token).

			//Create and run an auth task in the background.
			m_tokenLoginTask = new TokenLoginTask(m_targetURL, mobileToken);
			m_tokenLoginTask.setDelegate(new TokenLoginTask.AsyncResponse()
			{
				@Override
				public void onPostExecute(boolean sendSucceeded, boolean taskSucceeded)
				{
					if (sendSucceeded)
					{
						//Request sent successfully.
						if (taskSucceeded)
						{
							//Task successful, go to schedule.
							goSchedule();
						}
						else
						{
							//Task failed, go to login form.
							goLogin();
						}
					}
					else
					{
						//Failed to send request, go to login form.
						goLogin();
					}

					m_tokenLoginTask = null;
				}
			});

			m_tokenLoginTask.execute((Void) null);
		}
	}

	private void goSchedule()
	{
		Intent intent = new Intent(this, ScheduleActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);

		finish();
	}

	private void goLogin()
	{
		Intent intent = new Intent(this, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);

		finish();
	}
}
