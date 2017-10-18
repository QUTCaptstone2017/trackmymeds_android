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

public class ViewScheduleItemActivity extends AppCompatActivity
{
	//Properties.
	int m_medScheduleItemID;

	StorageManager m_storageManager;

	Snackbar m_snackBar;

	View m_progressView;
	View m_viewScheduleItemRelativeLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_schedule_item);

		//TODO:
		//Initialise tasks.

		m_storageManager = new StorageManager();

		m_progressView = findViewById(R.id.view_medication_progress);
		m_viewScheduleItemRelativeLayout = findViewById(R.id.view_medication_relative_layout);

		m_medScheduleItemID = -1;

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

				MedicationScheduleItem schedule = new MedicationScheduleItem();
				if (schedule.Initialize(scheduleItemJSON))
				{
					String displayString = schedule.toString();

					TextView m_detailsView = (TextView)findViewById(R.id.view_schedule_item_label_schedule_item_details);
					m_detailsView.setText(displayString);
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
}
