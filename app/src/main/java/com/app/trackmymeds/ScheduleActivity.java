package com.app.trackmymeds;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScheduleActivity extends AppCompatActivity
{
	//Properties.
	ScheduleTask m_dailyScheduleTask;

	StorageManager m_storageManager;

	Snackbar m_snackBar;

	private View m_progressView;
	private View m_scheduleFormView;

	private ExpandListAdapter ExpAdapter;
	private ArrayList<ExpandListGroup> ExpListItems;
	private ExpandableListView ExpandList;

	private PendingIntent m_backgroundIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule);

		m_storageManager = new StorageManager();

		Toolbar myToolbar = (Toolbar) findViewById(R.id.schedule_toolbar);
		setSupportActionBar(myToolbar);

		ExpandList = (ExpandableListView) findViewById(R.id.list_view_medication_schedule);
		ExpListItems = setStandardGroups();
		ExpAdapter = new ExpandListAdapter(ScheduleActivity.this, ExpListItems);
		ExpandList.setAdapter(ExpAdapter);

		m_backgroundIntent = null;

		m_progressView = findViewById(R.id.schedule_progress);
		m_scheduleFormView = findViewById(R.id.schedule_form);

		ImageButton registerButton = (ImageButton) findViewById(R.id.button_add_medication);
		registerButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				goAddMedicine();
			}
		});

		ExpandList.setOnChildClickListener(new ExpandableListView.OnChildClickListener()
		{
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
			{
				//Only interested in editing if the user clicked a schedule and not a daily item.
				if (groupPosition < 2)
				{
					//Clicked on a schedule item.

					ExpandListChild child = ExpListItems.get(groupPosition).getItems().get(childPosition);

					System.out.println("Schedule item clicked!");
					System.out.println(child.getJSON().toString());
					JSONObject json = child.getJSON();

					Intent intent = new Intent(getBaseContext(), ViewScheduleItemActivity.class);
					intent.putExtra("EXTRA_SCHEDULE_ITEM_JSON", json.toString());

					startActivity(intent);

					return true;
				}
				else
				{
					//Clicked on a schedule.

					ExpandListChild child = ExpListItems.get(groupPosition).getItems().get(childPosition);

					System.out.println("Schedule clicked!");
					System.out.println(child.getJSON().toString());
					JSONObject json = child.getJSON();

					Intent intent = new Intent(getBaseContext(), ViewScheduleActivity.class);
					intent.putExtra("EXTRA_MED_ADD_TYPE", "edit");
					intent.putExtra("EXTRA_SCHEDULE_JSON", json.toString());

					startActivity(intent);

					return true;
				}
			}
		});

		getDailySchedule();

		if (m_backgroundIntent == null)
		{
			//Retrieve a PendingIntent that will perform a broadcast.
			Intent intent = new Intent(ScheduleActivity.this, BackgroundReceiver.class);
			m_backgroundIntent = PendingIntent.getBroadcast(ScheduleActivity.this, 0, intent, 0);
			System.out.println("m_backgroundIntent");
			System.out.println(m_backgroundIntent);

			AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			int interval = 60 * 1000;

			manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, m_backgroundIntent);
			Toast.makeText(this, "Starting schedule check service!", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.schedule_menu, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_delete_account:
				goDeleteAccount();
				return true;

			case R.id.action_change_account:
				logout();
			return true;

			//If we got here, the user's action was not recognized.
			//Invoke the superclass to handle it.
			default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onRestart()
	{
		super.onRestart();

		getDailySchedule();
	}

	public void goDeleteAccount()
	{
		Intent intent = new Intent(this, DeleteAccountActivity.class);
		startActivity(intent);
	}

	public void goAddMedicine()
	{
		Intent intent = new Intent(this, MedListActivity.class);
		startActivity(intent);
	}

	public void logout()
	{
		//Replace mobile token with empty string.
		m_storageManager.saveMobileToken(getApplicationContext(), "");

		Intent intent = new Intent(this, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);

		finish();
	}

	public ArrayList<ExpandListGroup> setStandardGroups()
	{
		ArrayList<ExpandListGroup> resultList = new ArrayList<ExpandListGroup>();
		ArrayList<ExpandListChild> groupList = new ArrayList<ExpandListChild>();

		ExpandListGroup group1 = new ExpandListGroup();
		group1.setName("Morning");

		ExpandListGroup group2 = new ExpandListGroup();
		group2.setName("Afternoon");

		resultList.add(group1);
		resultList.add(group2);

		return resultList;
	}

	private ExpandListChild makeMedicationSchedule(JSONObject row)
	{
		MedicationSchedule item = new MedicationSchedule();
		item.Initialize(row);

		ExpandListChild child = new ExpandListChild();
		child.setName(item.toDisplayString());
		child.setJSON(row);
		child.setTag(null);

		return child;
	}

	private ExpandListChild makeMedicationScheduleItem(JSONObject row)
	{

		MedicationScheduleItem item = new MedicationScheduleItem();
		item.Initialize(row);

		ExpandListChild child = new ExpandListChild();
		child.setName(item.toDisplayString());
		child.setJSON(row);
		child.setTag(null);

		return child;
	}

	public void checkNotifications(JSONObject responseJSON)
	{
		//Task successful.
		try
		{
			JSONArray notificationMedications = responseJSON.getJSONArray("notification_meds");

			for (int i = 0; i < notificationMedications.length(); i++)
			{
				//Creates an intent.
				Intent resultIntent = new Intent(this, ViewScheduleItemActivity.class);

				JSONObject json = (JSONObject) notificationMedications.get(1);
				MedicationScheduleItem item = new MedicationScheduleItem();

				item.Initialize(json);

				resultIntent.putExtra("EXTRA_SCHEDULE_ITEM_JSON", json.toString());

				//Build notification.
				NotificationCompat.Builder mBuilder =
						new NotificationCompat.Builder(this)
								.setSmallIcon(R.drawable.add_icon)
								.setContentTitle("Medication Reminder")
								.setContentText(item.toDisplayString())
								.setVibrate(new long[]{300, 300});
				//.setVibrate(new long[]{0, 100, 100, 100, 100, 100, 100, 100, 100, 500, 150, 500, 150, 600, 150, 100, 100, 100, 100, 100, 100, 100, 100, 500, 150, 500, 150, 600, 150});


				resultIntent.setAction(Intent.ACTION_MAIN);
				resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
				resultIntent.putExtra("EXTRA_NOTIFICATION", true);

				PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
						resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				mBuilder.setContentIntent(pendingIntent);

				NotificationManager mNotificationManager =
						(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

				// mNotificationId is a unique integer your app uses to identify the
				// notification. For example, to cancel the notification, you can pass its ID
				// number to NotificationManager.cancel().
				mNotificationManager.notify(item.m_id, mBuilder.build());
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	public ArrayList<ExpandListGroup> populateDailySchedule(JSONObject responseJSON)
	{
		ArrayList<ExpandListGroup> resultList = new ArrayList<ExpandListGroup>();
		ArrayList<ExpandListChild> groupList = new ArrayList<ExpandListChild>();

		//DEBUG:
		System.out.println("JSON: ");
		System.out.println(responseJSON.toString());

		ExpandListGroup group1 = new ExpandListGroup();
		group1.setName("Morning");

		try
		{
			JSONArray morningMeds = responseJSON.getJSONArray("morning_meds");
			for (int i = 0; i < morningMeds.length(); i++)
			{
				JSONObject row = morningMeds.getJSONObject(i);
				ExpandListChild child = makeMedicationScheduleItem(row);
				child.setJSON(row);

				if (child != null)
				{
					groupList.add(child);
				}
			}

			group1.setItems(groupList);
			groupList = new ArrayList<ExpandListChild>();

			ExpandListGroup group2 = new ExpandListGroup();
			group2.setName("Afternoon");

			JSONArray afternoonMeds = responseJSON.getJSONArray("afternoon_meds");
			for (int i = 0; i < afternoonMeds.length(); i++)
			{
				JSONObject row = afternoonMeds.getJSONObject(i);
				ExpandListChild child = makeMedicationScheduleItem(row);

				if (child != null)
				{
					child.setJSON(row);
					groupList.add(child);
				}
			}

			group2.setItems(groupList);
			groupList = new ArrayList<ExpandListChild>();

			ExpandListGroup group3 = new ExpandListGroup();
			group3.setName("Medication Schedule");

			JSONArray medicationSchedules = responseJSON.getJSONArray("scheduled_meds");
			for (int i = 0; i < medicationSchedules.length(); i++)
			{
				JSONObject row = medicationSchedules.getJSONObject(i);
				ExpandListChild child = makeMedicationSchedule(row);

				if (child != null)
				{
					groupList.add(child);
				}
			}

			group3.setItems(groupList);

			resultList.add(group1);
			resultList.add(group2);
			resultList.add(group3);
		}
		catch (JSONException e)
		{
			System.out.println("failed");
			e.printStackTrace();
		}

		return resultList;
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

			m_scheduleFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			m_scheduleFormView.animate().setDuration(shortAnimTime).alpha(
					show ? 0 : 1).setListener(new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationEnd(Animator animation)
				{
					m_scheduleFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
			m_scheduleFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	public void getDailySchedule()
	{
		if (m_dailyScheduleTask != null)
		{
			return;
		}

		//Reset errors.
		//m_firstNameEdit.setError(null);

		boolean cancel = false;
		View focusView = m_scheduleFormView;

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
			m_dailyScheduleTask = new ScheduleTask(mobileToken);
			m_dailyScheduleTask.setDelegate(new ScheduleTask.AsyncResponse()
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
							ExpListItems = populateDailySchedule(m_dailyScheduleTask.m_responseJSON);
							ExpAdapter = new ExpandListAdapter(ScheduleActivity.this, ExpListItems);
							ExpandList.setAdapter(ExpAdapter);

							//Expand all groups by default.
							int groupCount = ExpAdapter.getGroupCount();
							for (int i = 0; i < groupCount; i++)
							{
								ExpandList.expandGroup(i);
							}

							checkNotifications(m_dailyScheduleTask.m_responseJSON);
						}
						else
						{
							//Task failed.
							final int snackBarDurationSeconds = 10;
							String errorString = "Error (" + m_dailyScheduleTask.m_errorCode +
									"): " + m_dailyScheduleTask.m_errorMessage;

							m_snackBar = Snackbar.make(findViewById(R.id.schedule_layout),
									errorString, snackBarDurationSeconds * 1000);

							m_snackBar.show();
						}
					}
					else
					{
						//Failed to send request.
						final int snackBarDurationSeconds = 10;
						String errorString = "Error (" + m_dailyScheduleTask.m_errorCode +
								"): " + m_dailyScheduleTask.m_errorMessage;

						m_snackBar = Snackbar.make(findViewById(R.id.schedule_layout),
								errorString, snackBarDurationSeconds * 1000);

						m_snackBar.show();
					}

					m_dailyScheduleTask = null;
				}
			});

			m_dailyScheduleTask.execute((Void) null);
		}
	}
}
