package com.app.trackmymeds;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by Declan on 20/10/2017.
 */

public class DeviceBootReceiever extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		System.out.println("~\n~\n~\n~\n~\n~\n~\n~\n~\n~\n~\n~\n");
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
		{
			System.out.println("A\nA\nA\nA\nA\nA\nA\nA\nA\nA\nA\nA\n");
			Intent serviceIntent = new Intent("com.app.trackmymeds.MyService");
			context.startService(serviceIntent);
			/*
			//Set the alarm here:
			Intent alarmIntent = new Intent(context, AlarmReceiver.class);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);

			AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			int interval = 60 * 1000;
			manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);

			Toast.makeText(context, "Starting schedule check service!", Toast.LENGTH_SHORT).show();*/
		}
	}
}