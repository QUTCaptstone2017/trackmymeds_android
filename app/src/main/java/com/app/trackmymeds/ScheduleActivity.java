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
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ImageButton;

import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScheduleActivity extends AppCompatActivity
{
	//Properties.
	DailyScheduleTask m_dailyScheduleTask;

	private View m_progressView;
	private View m_scheduleFormView;

	private ExpandListAdapter ExpAdapter;
	private ArrayList<ExpandListGroup> ExpListItems;
	private ExpandableListView ExpandList;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule);

		Toolbar myToolbar = (Toolbar) findViewById(R.id.schedule_toolbar);
		setSupportActionBar(myToolbar);

		ExpandList = (ExpandableListView) findViewById(R.id.list_view_medication_schedule);
		ExpListItems = setStandardGroups();
		ExpAdapter = new ExpandListAdapter(ScheduleActivity.this, ExpListItems);
		ExpandList.setAdapter(ExpAdapter);

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
				if (groupPosition != 2)
				{
					return false;
				}

				ExpandListChild child = ExpListItems.get(groupPosition).getItems().get(childPosition);

				System.out.println("Item Clicked!");
				System.out.println(child.getJSON().toString());
				JSONObject json = child.getJSON();

				Intent intent = new Intent(getBaseContext(), AddMedicationActivity.class);
				intent.putExtra("EXTRA_MED_ADD_TYPE", "edit");

				try
				{
					//TODO: Don't need brand ID.
					intent.putExtra("EXTRA_MED_SCHEDULE_ID", json.getInt("med_schedule_id"));
					intent.putExtra("EXTRA_BRAND_ID", 0);
					intent.putExtra("EXTRA_BRAND_NAME", json.getString("name"));
					intent.putExtra("EXTRA_PRODUCT_ID", Integer.parseInt(json.getString("product_id")));
					intent.putExtra("EXTRA_PRODUCT_DESCRIPTION", json.getString("description"));
					intent.putExtra("EXTRA_TIME_TO_TAKE", json.getString("time_to_take"));
					intent.putExtra("EXTRA_DATE_TO_TAKE", json.getString("date_to_take"));
					intent.putExtra("EXTRA_REPEAT_CUSTOM", json.getInt("day_interval"));
					intent.putExtra("EXTRA_TO_TAKE", Integer.parseInt(json.getString("dosage")));
					intent.putExtra("EXTRA_TO_REMAINING", Integer.parseInt(json.getString("pack_remaining")));
					intent.putExtra("EXTRA_NICKNAME", json.getString("nickname"));
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}

				startActivity(intent);

				return true;
			}
		});

		getDailySchedule();
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
			case R.id.action_settings:
				goDeleteAccount();
				return true;

			case R.id.action_medication_history:
				goMedicationHistory();
				return true;

			default:
				// If we got here, the user's action was not recognized.
				// Invoke the superclass to handle it.
				return super.onOptionsItemSelected(item);
		}
	}

	public void goMedicationHistory()
	{
		Intent intent = new Intent(this, MedicationHistoryActivity.class);
		startActivity(intent);
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

	private ExpandListChild makeMedicationItem(JSONObject row)
	{
		try
		{
			int id = row.getInt("id");
			int medScheduleID = row.getInt("med_schedule_id");
			String dateToTake = row.getString("date_to_take");
			String timeToTake = row.getString("time_to_take");
			String nickname = row.getString("nickname");
			String name = row.getString("name");
			String description = row.getString("description");
			int dosage = row.getInt("dosage");
			int isTaken = row.getInt("is_taken");
			int isDismissed = row.getInt("is_dismissed");

			String itemString = "ID: " + String.valueOf(id) + "\n" +
					"Med Schedule ID: " + String.valueOf(medScheduleID) + "\n" +
					"Date To Take: " + dateToTake + "\n" +
					"Time To Take: " + timeToTake + "\n" +
					"Nickname: " + nickname + "\n" +
					"Name: " + name + "\n" +
					"Description: " + description + "\n" +
					"Dosage: " + String.valueOf(dosage) + "\n" +
					"Is Taken: " + String.valueOf(isTaken) + "\n" +
					"Is Dismissed: " + String.valueOf(isDismissed);

			ExpandListChild child = new ExpandListChild();
			child.setName(itemString);
			child.setJSON(row);
			child.setTag(null);

			return child;
		} catch (JSONException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	private ExpandListChild makeMedicationScheduleItem(JSONObject row)
	{
		try
		{
			int id = row.getInt("id");
			int productID = row.getInt("product_id");
			int toTake = row.getInt("dosage");
			int toRemaining = row.getInt("pack_remaining");
			int repeatCustom = row.getInt("day_interval");
			String timeToTake = row.getString("time_to_take");
			String nickname = row.getString("nickname");

			//Need to get timeToTake in a HH:MM format.
			Pattern pattern = Pattern.compile("^(\\d{2}:\\d{2}):\\d{2}$");
			Matcher match = pattern.matcher(timeToTake);
			boolean matched = match.matches();
			if (matched)
			{
				timeToTake = match.group(1);
				row.remove("time_to_take");
				row.put("time_to_take", timeToTake);
			}
			else
			{
				System.out.println("Regex for timeToTake failed!.");
			}

			String itemString = "ID: " + String.valueOf(id) + "\n" +
					"Product ID: " + String.valueOf(productID) + "\n" +
					"Items To Take: " + String.valueOf(toTake) + "\n" +
					"Items Remaining: " + String.valueOf(toRemaining) + "\n" +
					"Repeat Custom: " + String.valueOf(repeatCustom) + "\n" +
					"Time To Take: " + timeToTake + "\n" +
					"Nickname: " + nickname;

			ExpandListChild child = new ExpandListChild();
			child.setName(itemString);
			child.setJSON(row);
			child.setTag(null);

			return child;
		} catch (JSONException e)
		{
			e.printStackTrace();
		}

		return null;
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
				ExpandListChild child = makeMedicationItem(row);
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
				ExpandListChild child = makeMedicationItem(row);

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
				ExpandListChild child = makeMedicationScheduleItem(row);

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

			//Create and run an auth task in the background.
			m_dailyScheduleTask = new ScheduleActivity.DailyScheduleTask();
			m_dailyScheduleTask.execute((Void) null);
		}
	}

	/**
	 * Represents an asynchronous daily schedule request.
	 */
	public class DailyScheduleTask extends AsyncTask<Void, Void, Boolean>
	{

		//TODO: Move this.
		private static final String PREFS_NAME = "TrackMyMedsPref";

		private static final String ROUTE_URL = "https://trackmymeds.frb.io/get_daily_meds_mobile";
		private String m_response;
		private JSONObject m_responseJSON;

		DailyScheduleTask()
		{
			m_response = "";
			m_responseJSON = null;
		}

		private String getMobileToken()
		{
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			String mobileToken = settings.getString("mobile_token", "");

			return mobileToken;
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
				System.out.println("SENDING:");
				System.out.println(jsonAuth.toString());

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

		private boolean authenticate()
		{
			try
			{
				URL url = new URL(ROUTE_URL);
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
				System.out.println("Successfully sent daily schedule request.");

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
						m_responseJSON = responseJSON.getJSONObject("data");
						System.out.println("Daily schedule request successful.");
						return true;
					}
					else
					{
						System.out.println("Registration failed.");
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
				System.out.println("Failed to send daily schedule request.");
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
			m_dailyScheduleTask = null;
			showProgress(false);

			if (success)
			{
				ExpListItems = populateDailySchedule(m_responseJSON);
				ExpAdapter = new ExpandListAdapter(ScheduleActivity.this, ExpListItems);
				ExpandList.setAdapter(ExpAdapter);

				//Expand all groups by default.
				int groupCount = ExpAdapter.getGroupCount();
				for (int i = 0; i < groupCount; i++)
				{
					ExpandList.expandGroup(i);
				}
			}
			else
			{

			}
		}

		@Override
		protected void onCancelled()
		{
			m_dailyScheduleTask = null;
			showProgress(false);
		}
	}
}
