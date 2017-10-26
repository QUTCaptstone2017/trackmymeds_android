package com.app.trackmymeds;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Declan on 20/10/2017.
 */

public class AlarmReceiver extends BroadcastReceiver
{
	ScheduleTask m_scheduleTask;
	StorageManager m_storageManager;

	//Context m_context;

	public AlarmReceiver()
	{
		m_scheduleTask = null;
		m_storageManager = new StorageManager();
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		System.out.println("ALARM RINGING!!!");

		//Creates an intent.
		Intent resultIntent = new Intent(context, ViewScheduleItemActivity.class);

		String scheduleItemJSONString = intent.getStringExtra("EXTRA_SCHEDULE_ITEM_JSON");
		if (scheduleItemJSONString != null)
		{
			System.out.println("scheduleItemJSONString");
			System.out.println(scheduleItemJSONString);

			try
			{
				JSONObject scheduleJSON = new JSONObject(scheduleItemJSONString);

				MedicationScheduleItem item = new MedicationScheduleItem();
				if (item.Initialize(scheduleJSON))
				{
					resultIntent.putExtra("EXTRA_SCHEDULE_ITEM_JSON", scheduleJSON.toString());

					//Build notification.
					NotificationCompat.Builder mBuilder =
							new NotificationCompat.Builder(context)
									.setSmallIcon(R.drawable.add_icon)
									.setContentTitle("Medication Reminder (" + item.m_nickname + ")")
									.setContentText("Brand: " + item.m_brandName)
									.setStyle(new NotificationCompat.BigTextStyle().bigText("Brand: " + item.m_brandName + "\n" + item.toDisplayString()))
									.setVibrate(new long[]{300, 300});

					resultIntent.setAction(Intent.ACTION_MAIN);
					resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
					resultIntent.putExtra("EXTRA_NOTIFICATION", true);

					PendingIntent pendingIntent = PendingIntent.getActivity(context, item.m_id,
							resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
					mBuilder.setContentIntent(pendingIntent);

					NotificationManager mNotificationManager =
							(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

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

		/*if (m_scheduleTask != null)
		{
			return;
		}

		m_context = context;

		//Get mobile token from application storage.
		String mobileToken = m_storageManager.getMobileToken(context);

		//Create and run an auth task in the background.
		m_scheduleTask = new ScheduleTask(mobileToken);
		m_scheduleTask.setDelegate(new ScheduleTask.AsyncResponse()
		{
			@Override
			public void onPostExecute(boolean sendSucceeded, boolean taskSucceeded)
			{
				if (sendSucceeded)
				{
					//Request sent successfully.
					if (taskSucceeded)
					{
						//Task successful.
						try
						{
							JSONArray notificationMedications = m_scheduleTask.m_responseJSON.getJSONArray("notification_meds");

							//TODO:
							for (int i = 0; i < notificationMedications.length(); i++)
							{
								//Creates an intent.
								Intent resultIntent = new Intent(m_context, ViewScheduleItemActivity.class);

								JSONObject json = (JSONObject) notificationMedications.get(1);
								MedicationScheduleItem item = new MedicationScheduleItem();

								item.Initialize(json);

								resultIntent.putExtra("EXTRA_SCHEDULE_ITEM_JSON", json.toString());

								//Build notification.
								NotificationCompat.Builder mBuilder =
										new NotificationCompat.Builder(m_context)
												.setSmallIcon(R.drawable.add_icon)
												.setContentTitle("Medication Reminder")
												.setContentText(item.toDisplayString())
												.setVibrate(new long[]{300, 300});

								resultIntent.setAction(Intent.ACTION_MAIN);
								resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
								resultIntent.putExtra("EXTRA_NOTIFICATION", true);

								PendingIntent pendingIntent = PendingIntent.getActivity(m_context, 0,
										resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
								mBuilder.setContentIntent(pendingIntent);

								NotificationManager mNotificationManager =
										(NotificationManager)m_context.getSystemService(Context.NOTIFICATION_SERVICE);

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
					else
					{
						//Task failed.
					}
				}
				else
				{
					//Failed to send request.
				}

				m_scheduleTask = null;
			}
		});

		m_scheduleTask.execute((Void) null);*/
	}
}
