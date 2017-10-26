package com.app.trackmymeds;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class ViewScheduleActivity extends AppCompatActivity
{
	//Properties.
	int m_medScheduleID;
	JSONObject m_scheduleJSON;

	DeleteMedicationTask m_deleteMedicationTask;

	StorageManager m_storageManager;

	Snackbar m_snackBar;

	View m_progressView;
	View m_viewMedicationRelativeLayout;

	//Value views.
	TextView m_nicknameValueView;
	TextView m_brandNameValueView;
	TextView m_productDescriptionValueView;
	TextView m_dateValueView;
	TextView m_timeValueView;
	TextView m_dayIntervalValueView;
	TextView m_toTakeValueView;
	TextView m_toRemainingValueView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_schedule);

		m_medScheduleID = -1;
		m_scheduleJSON = null;

		//Initialise tasks.
		m_deleteMedicationTask = null;

		m_storageManager = new StorageManager();

		Toolbar myToolbar = (Toolbar) findViewById(R.id.view_med_toolbar);
		setSupportActionBar(myToolbar);

		m_progressView = findViewById(R.id.view_medication_progress);
		m_viewMedicationRelativeLayout = findViewById(R.id.view_medication_relative_layout);

		m_nicknameValueView = (TextView)findViewById(R.id.view_medication_nickname_value_textview);
		m_brandNameValueView = (TextView)findViewById(R.id.view_medication_brand_name_value_textview);
		m_productDescriptionValueView = (TextView)findViewById(R.id.view_medication_product_description_value_textview);
		m_dateValueView = (TextView)findViewById(R.id.view_medication_date_value_textview);
		m_timeValueView = (TextView)findViewById(R.id.view_medication_time_value_textview);
		m_dayIntervalValueView = (TextView)findViewById(R.id.view_medication_day_interval_value_textview);
		m_toTakeValueView = (TextView)findViewById(R.id.view_medication_to_take_value_textview);
		m_toRemainingValueView = (TextView)findViewById(R.id.view_medication_to_remaining_value_textview);

		ImageButton editMedButton = (ImageButton)findViewById(R.id.view_medication_edit_fab);
		editMedButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				goEditSchedule();
			}
		});

		//Get passed extras from last activity.
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		handleExtras(extras);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.view_med, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menu_action_delete_medication:
				confirmDeleteMedication();
			return true;

			//If we got here, the user's action was not recognized.
			//Invoke the superclass to handle it.
			default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void goEditSchedule()
	{
		//DEBUG:
		//Edit schedule.
		System.out.println("Editing schedule!");

		Intent intent = new Intent(getBaseContext(), AddMedicationActivity.class);
		intent.putExtra("EXTRA_MED_ADD_TYPE", "edit");
		intent.putExtra("EXTRA_SCHEDULE_JSON", m_scheduleJSON.toString());

		startActivity(intent);
	}

	public void confirmDeleteMedication()
	{
		// 1. Instantiate an AlertDialog.Builder with its constructor
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		// 2. Chain together various setter methods to set the dialog characteristics
		builder.setMessage(R.string.dialog_confirm_delete_medication_prompt)
				.setTitle(R.string.dialog_confirm_delete_medication_title);

		// Add the buttons
		builder.setPositiveButton(R.string.dialog_confirm_delete_medication_yes,
				new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				// User clicked OK button
				deleteMedication();
			}
		});

		builder.setNegativeButton(R.string.dialog_confirm_delete_medication_cancel,
				new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				// User cancelled the dialog
			}
		});

		// 3. Get the AlertDialog from create()
		AlertDialog dialog = builder.create();

		dialog.show();
	}

	private void handleExtras(Bundle extras)
	{
		String m_medScheduleJSONString = extras.getString("EXTRA_SCHEDULE_JSON");

		if (m_medScheduleJSONString != null)
		{
			System.out.println("m_medScheduleJSONString");
			System.out.println(m_medScheduleJSONString);

			try
			{
				m_scheduleJSON = new JSONObject(m_medScheduleJSONString);

				MedicationSchedule schedule = new MedicationSchedule();
				if (schedule.Initialize(m_scheduleJSON))
				{
					m_nicknameValueView.setText(schedule.m_nickname);
					m_brandNameValueView.setText(schedule.m_brandName);
					m_productDescriptionValueView.setText(schedule.m_productDescription);
					m_dateValueView.setText(schedule.getDisplayDate());
					m_timeValueView.setText(schedule.getDisplayTime());
					m_dayIntervalValueView.setText(String.valueOf(schedule.m_dayInterval));
					m_toTakeValueView.setText(String.valueOf(schedule.m_toTake));
					m_toRemainingValueView.setText(String.valueOf(schedule.m_toRemaining));
				}

				m_medScheduleID = schedule.m_id;
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

			m_viewMedicationRelativeLayout.setVisibility(show ? View.GONE : View.VISIBLE);
			m_viewMedicationRelativeLayout.animate().setDuration(shortAnimTime).alpha(
					show ? 0 : 1).setListener(new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationEnd(Animator animation)
				{
					m_viewMedicationRelativeLayout.setVisibility(show ? View.GONE : View.VISIBLE);
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
			m_viewMedicationRelativeLayout.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	public void deleteMedication()
	{
		if (m_deleteMedicationTask != null)
		{
			return;
		}

		//Reset errors.
		//m_firstNameEdit.setError(null);

		boolean cancel = false;
		View focusView = m_viewMedicationRelativeLayout;

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
			System.out.println("Cancelling daily schedule request...");
			//There was an error; don't attempt login and focus the first
			//form field with an error.
			focusView.requestFocus();
		}
		else
		{
			System.out.println("Connected to the internet.");

			//Show the progress spinner...
			showProgress(true);

			//Get mobile token from application storage.
			String mobileToken = m_storageManager.getMobileToken(getApplicationContext());

			//Create and run an auth task in the background.
			m_deleteMedicationTask = new DeleteMedicationTask(mobileToken, m_medScheduleID);
			m_deleteMedicationTask.setDelegate(new DeleteMedicationTask.AsyncResponse()
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
							//Task successful.

							//DEBUG:
							System.out.println("Deleted medication schedule.");

							goSchedule(true);
						}
						else
						{
							//Task failed.

						}
					}
					else
					{
						//Failed to send request.

					}

					m_deleteMedicationTask = null;
				}
			});

			m_deleteMedicationTask.execute((Void) null);
		}
	}
}
