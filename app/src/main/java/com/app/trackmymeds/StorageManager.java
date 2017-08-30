package com.app.trackmymeds;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Declan on 29/08/2017.
 */

public class StorageManager
{
	StorageManager()
	{

	}

	protected void saveMobileToken(Context context, String mobileToken)
	{
		final String filename = "tmmAuth";
		File file = new File(context.getFilesDir(), filename);

		FileOutputStream outputStream;
		try
		{
			outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
			outputStream.write(mobileToken.getBytes());
			outputStream.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	protected String getMobileToken(Context context)
	{
		final String filename = "tmmAuth";

		File file = new File(context.getFilesDir(), filename);
		if (!file.exists())
		{
			return "";
		}

		String result = "";
		FileInputStream inputStream;
		try
		{
			inputStream = context.openFileInput(filename);
			result = convertToString(inputStream);

			inputStream.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			return result;
		}
	}

	protected String convertToString(InputStream is) throws IOException
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
}
