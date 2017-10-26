package com.app.trackmymeds;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class ViewScheduleItemActivity extends AppCompatActivity
{
	//Properties.
	TakeMedicationTask m_takeMedicationTask;

	StorageManager m_storageManager;

	Snackbar m_snackBar;

	View m_progressView;
	View m_viewScheduleItemRelativeLayout;

	//Value views.
	TextView m_nicknameValueView;
	TextView m_brandNameValueView;
	TextView m_productDescriptionValueView;
	TextView m_dateValueView;
	TextView m_timeValueView;
	TextView m_dayIntervalValueView;
	TextView m_toTakeValueView;
	TextView m_toRemainingValueView;

	int m_scheduleItemID;
	Button m_takeMedicationButton;

	enum TakeButtonState { TAKE, UNTAKE, NONE };
	TakeButtonState m_takeButtonState;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_schedule_item);

		//TODO:
		//Initialise tasks.
		m_takeMedicationTask = null;

		m_storageManager = new StorageManager();

		m_progressView = findViewById(R.id.view_medication_progress);
		m_viewScheduleItemRelativeLayout = findViewById(R.id.view_medication_relative_layout);

		m_scheduleItemID = -1;
		m_takeButtonState = TakeButtonState.NONE;

		m_nicknameValueView = (TextView)findViewById(R.id.view_schedule_item_nickname_value_textview);
		m_brandNameValueView = (TextView)findViewById(R.id.view_schedule_item_brand_name_value_textview);
		m_productDescriptionValueView = (TextView)findViewById(R.id.view_schedule_item_product_description_value_textview);
		m_dateValueView = (TextView)findViewById(R.id.view_schedule_item_date_value_textview);
		m_timeValueView = (TextView)findViewById(R.id.view_schedule_item_time_value_textview);
		m_dayIntervalValueView = (TextView)findViewById(R.id.view_schedule_item_day_interval_value_textview);
		m_toTakeValueView = (TextView)findViewById(R.id.view_schedule_item_to_take_value_textview);
		m_toRemainingValueView = (TextView)findViewById(R.id.view_schedule_item_to_remaining_value_textview);

		m_takeMedicationButton = (Button)findViewById(R.id.button_take_medication);
		m_takeMedicationButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				takeMedication();
			}
		});

		//Get passed extras from last activity.
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		handleExtras(extras);
	}

	private void handleExtras(Bundle extras)
	{
		String m_medScheduleItemJSONString = extras.getString("EXTRA_SCHEDULE_ITEM_JSON");

		if (m_medScheduleItemJSONString != null)
		{
			System.out.println("m_medScheduleItemJSONString");
			System.out.println(m_medScheduleItemJSONString);

			try
			{
				JSONObject scheduleItemJSON = new JSONObject(m_medScheduleItemJSONString);

				MedicationScheduleItem scheduleItem = new MedicationScheduleItem();
				if (scheduleItem.Initialize(scheduleItemJSON))
				{
					m_scheduleItemID = scheduleItem.m_id;

					m_nicknameValueView.setText(scheduleItem.m_nickname);
					m_brandNameValueView.setText(scheduleItem.m_brandName);
					m_productDescriptionValueView.setText(scheduleItem.m_productDescription);
					m_dateValueView.setText(scheduleItem.getDisplayDate());
					m_timeValueView.setText(scheduleItem.getDisplayTime());
					m_dayIntervalValueView.setText(String.valueOf(scheduleItem.m_dayInterval));
					m_toTakeValueView.setText(String.valueOf(scheduleItem.m_toTake));
					m_toRemainingValueView.setText(String.valueOf(scheduleItem.m_toRemaining));

					//toggleTakeButton Returns the opposite state.
					if (scheduleItem.m_isTaken)
					{
						m_takeButtonState = toggleTakeButton(TakeButtonState.TAKE);
					}
					else
					{
						m_takeButtonState = toggleTakeButton(TakeButtonState.UNTAKE);
					}

					boolean notificationRedirect = extras.getBoolean("EXTRA_NOTIFICATION");
					System.out.println("notificationRedirect");
					System.out.println(notificationRedirect);
					if (notificationRedirect)
					{
						NotificationManager mNotificationManager =
								(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

						mNotificationManager.cancel(scheduleItem.m_id);
					}
				}
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void goSchedule(boolean doClear)
	{
		Intent intent = new Intent(this, ScheduleActivity.class);

		if (doClear)
		{
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
		}

		startActivity(intent);
		finish();
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

			m_viewScheduleItemRelativeLayout.setVisibility(show ? View.GONE : View.VISIBLE);
			m_viewScheduleItemRelativeLayout.animate().setDuration(shortAnimTime).alpha(
					show ? 0 : 1).setListener(new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationEnd(Animator animation)
				{
					m_viewScheduleItemRelativeLayout.setVisibility(show ? View.GONE : View.VISIBLE);
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
			m_viewScheduleItemRelativeLayout.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	public void takeMedication()
	{
		if (m_takeMedicationTask != null ||
			m_scheduleItemID == -1 ||
			m_takeButtonState == TakeButtonState.NONE)
		{
			return;
		}

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
			System.out.println("Cancelling take/untake medication request...");
		}
		else
		{
			System.out.println("Connected to the internet.");

			//Show the progress spinner...
			showProgress(true);

			m_storageManager = new StorageManager();
			String mobileToken = m_storageManager.getMobileToken(getApplicationContext());

			//Create and run a task in the background.
			m_takeMedicationTask = new TakeMedicationTask(mobileToken, m_scheduleItemID,
					m_takeButtonState == TakeButtonState.UNTAKE);

			//Set anonymous response callback.
			m_takeMedicationTask.setDelegate(new TakeMedicationTask.AsyncResponse()
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
							//Take/untake medication successful.
							m_takeButtonState = toggleTakeButton(m_takeButtonState);
						}
						else
						{
							//Failed to Take/untake medication.
						}
					}
					else
					{
						//Failed to send request.
					}

					m_takeMedicationTask = null;
					showProgress(false);
				}
			});

			m_takeMedicationTask.execute((Void) null);
		}
	}

	private TakeButtonState toggleTakeButton(TakeButtonState takeButtonState)
	{
		switch (takeButtonState)
		{
			case TAKE:
				m_takeMedicationButton.setText(R.string.button_untake_medication);
				m_takeMedicationButton.setBackgroundColor(getResources().getColor(R.color.colorAccent, getTheme()));
			return TakeButtonState.UNTAKE;

			case UNTAKE:
				m_takeMedicationButton.setText(R.string.button_take_medication);
				m_takeMedicationButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary, getTheme()));
			return TakeButtonState.TAKE;

			case NONE:
			default:
				//DEBUG:
				System.out.println("Tried to toggle take button but state was invalid/unknown!");
			return TakeButtonState.NONE;
		}
	}
}
