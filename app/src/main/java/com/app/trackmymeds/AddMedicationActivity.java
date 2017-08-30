package com.app.trackmymeds;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

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
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddMedicationActivity extends AppCompatActivity
{
	//Constants.
	final int REPEAT_CUSTOM_MAX = 50;
	final int TO_TAKE_MIN = 1;
	final int TO_TAKE_MAX = 50;
	final int TO_REMAINING_MAX = 1000;
	final int NICKNAME_MIN = 3;
	final int NICKNAME_MAX = 30;

	//Properties.
	DeleteMedicationTask m_deleteMedicationTask;
	AddMedicationTask m_addMedicationTask;
	GetProductsTask m_getProductsTask;

	StorageManager m_storageManager;

	String m_medAddType;
	int m_medScheduleID;

	TextView m_brandNameView;
	Spinner m_productSelectView;
	TextView m_reminderDateView;
	TextView m_reminderTimeView;
	Spinner m_repeatSelectView;
	TextView m_repeatCustomView;
	TextInputLayout m_repeatCustomLayout;
	TextView m_toTakeView;
	TextView m_toRemainingView;
	TextView m_nicknameView;

	View m_progressView;
	View m_addMedicationFormView;

	int m_brandID;
	int m_brandSupplierID;
	String m_brandName;

	private ArrayAdapter<String> m_productDescriptionAdapter;
	private ArrayList<String> m_productDescriptionItems;
	private int[] m_productIDItems;

	private ArrayAdapter<String> m_repeatAdapter;
	private ArrayList<String> m_repeatItems;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_medication);

		//Initialise tasks.
		m_deleteMedicationTask = null;
		m_addMedicationTask = null;
		m_getProductsTask = null;

		m_storageManager = new StorageManager();

		Toolbar myToolbar = (Toolbar) findViewById(R.id.add_med_toolbar);
		setSupportActionBar(myToolbar);

		m_progressView = findViewById(R.id.add_medication_progress);
		m_addMedicationFormView = findViewById(R.id.add_medication_form);

		m_medAddType = "";

		m_productSelectView = (Spinner)findViewById(R.id.add_medication_spinner_product);
		m_reminderDateView = (TextView)findViewById(R.id.add_medication_edit_text_date);
		m_reminderTimeView = (TextView)findViewById(R.id.add_medication_edit_text_time);
		m_repeatSelectView = (Spinner)findViewById(R.id.add_medication_spinner_repeat);
		m_repeatCustomLayout = (TextInputLayout) findViewById(R.id.add_medication_text_layout_repeat_custom);
		m_repeatCustomView = (TextView)findViewById(R.id.add_medication_edit_text_repeat_custom);
		m_toTakeView = (TextView)findViewById(R.id.add_medication_edit_text_to_take);
		m_toRemainingView = (TextView)findViewById(R.id.add_medication_edit_text_to_remaining);
		m_nicknameView = (TextView)findViewById(R.id.add_medication_edit_text_nickname);

		m_brandNameView = (TextView)findViewById(R.id.brand_name_value);
		m_brandNameView.setText(m_brandName);

		//Product spinner.
		m_productDescriptionItems = new ArrayList<String>();
		m_productDescriptionAdapter = new ArrayAdapter(this, R.layout.spinner_item, m_productDescriptionItems);
		m_productSelectView.setAdapter(m_productDescriptionAdapter);

		//Repeat spinner.
		m_repeatItems = new ArrayList<String>(Arrays.asList(this.getResources().getStringArray(R.array.add_medication_repeat)));
		m_repeatAdapter = new ArrayAdapter(this, R.layout.spinner_item, m_repeatItems);
		m_repeatSelectView.setAdapter(m_repeatAdapter);

		ImageButton addMedButton = (ImageButton) findViewById(R.id.button_add_medication);
		addMedButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				addMedication();
			}
		});

		m_productSelectView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> adapter, View v, int position, long id)
			{
				String productDescription = (String)adapter.getItemAtPosition(position);

				System.out.println("Item selected!");
				System.out.println(productDescription);

				m_productDescriptionAdapter.notifyDataSetChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
				System.out.println("Nothing selected.");
			}
		});

		m_repeatSelectView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> adapter, View v, int position, long id)
			{
				String repeatValue = (String)adapter.getItemAtPosition(position);

				System.out.println("Repeat item selected!");
				System.out.println(repeatValue);

				if (repeatValue.toLowerCase().compareTo("custom") == 0)
				{
					m_repeatCustomLayout.setVisibility(View.VISIBLE);
					System.out.println("Showing!");
				}
				else
				{
					m_repeatCustomLayout.setVisibility(View.GONE);
					System.out.println("Hiding!");
				}

				m_repeatAdapter.notifyDataSetChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
				System.out.println("Nothing selected.");
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
		inflater.inflate(R.menu.add_med, menu);

		System.out.println(m_medAddType);
		if (m_medAddType.compareTo("add") == 0)
		{
			System.out.println("Hiding delete button...");
			menu.findItem(R.id.action_delete_medication).setVisible(false);
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_delete_medication:
				confirmDeleteMedication();
			return true;

			default:
				// If we got here, the user's action was not recognized.
				// Invoke the superclass to handle it.
			return super.onOptionsItemSelected(item);
		}
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
		//Always get these details.
		m_brandID = (int)extras.getInt("EXTRA_BRAND_ID");
		m_brandSupplierID = (int)extras.getInt("EXTRA_BRAND_SUPPLIER_ID");
		m_brandName = (String)extras.getString("EXTRA_BRAND_NAME");

		System.out.println("m_brandID");
		System.out.println(m_brandID);

		System.out.println("m_brandSupplierID");
		System.out.println(m_brandSupplierID);

		System.out.println("m_brandName");
		System.out.println(m_brandName);

		//Get details for editing an existing schedule.
		m_medAddType = extras.getString("EXTRA_MED_ADD_TYPE");
		if (m_medAddType.compareTo("edit") == 0)
		{
			getSupportActionBar().setTitle("Edit Medication Schedule");

			//EXTRA_MED_SCHEDULE_ID
			int medScheduleID = extras.getInt("EXTRA_MED_SCHEDULE_ID");
			String brandName = extras.getString("EXTRA_BRAND_NAME");
			int productID = extras.getInt("EXTRA_PRODUCT_ID");
			String productDescription = extras.getString("EXTRA_PRODUCT_DESCRIPTION");
			String timeToTake = extras.getString("EXTRA_TIME_TO_TAKE");
			String dateToTake = extras.getString("EXTRA_DATE_TO_TAKE");
			int repeatCustom = extras.getInt("EXTRA_REPEAT_CUSTOM");
			int toTake = extras.getInt("EXTRA_TO_TAKE");
			int toRemaining = extras.getInt("EXTRA_TO_REMAINING");
			String nickname = extras.getString("EXTRA_NICKNAME");

			m_medScheduleID = medScheduleID;
			m_brandNameView.setText(brandName);
			m_productIDItems = new int[1];
			m_productIDItems[0] = productID;
			m_productDescriptionItems.add(productDescription);
			m_productDescriptionAdapter.notifyDataSetChanged();
			m_reminderTimeView.setText(timeToTake);
			m_reminderDateView.setText(dateToTake);

			switch (repeatCustom)
			{
				//Never repeat.
				case 0:
					m_repeatSelectView.setSelection(0);
				break;

				//Daily.
				case 1:
					m_repeatSelectView.setSelection(1);
				break;

				//Weekly.
				case 7:
					m_repeatSelectView.setSelection(2);
				break;

				//Custom.
				default:
					m_repeatSelectView.setSelection(3);
					m_repeatCustomView.setText(String.valueOf(repeatCustom));
				break;
			}

			m_toTakeView.setText(String.valueOf(toTake));
			m_toRemainingView.setText(String.valueOf(toRemaining));
			m_nicknameView.setText(nickname);

			showProgress(false);
		}
		else
		{
			//Get products.
			getProducts();
		}
	}

	public void goSchedule()
	{
		Intent intent = new Intent(this, ScheduleActivity.class);
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

			m_addMedicationFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			m_addMedicationFormView.animate().setDuration(shortAnimTime).alpha(
					show ? 0 : 1).setListener(new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationEnd(Animator animation)
				{
					m_addMedicationFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
			m_addMedicationFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	public void getProducts()
	{
		if (m_addMedicationTask != null)
		{
			return;
		}

		//Reset errors.
		//m_firstNameEdit.setError(null);

		//Get values.
		int brandID = m_brandID;

		//TODO: Input validation.

		boolean cancel = false;
		View focusView = m_addMedicationFormView;

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

			//Create and run a get products task in the background.
			m_getProductsTask = new GetProductsTask(brandID);
			m_getProductsTask.execute((Void) null);
		}
	}

	/**
	 * Represents an asynchronous products with brand id request.
	 */
	public class GetProductsTask extends AsyncTask<Void, Void, Boolean>
	{
		private static final String PREFS_NAME = "TrackMyMedsPref";
		private static final String LOGIN_URL = "https://trackmymeds.frb.io/med_products_json_mobile";

		String m_response;
		private JSONArray m_responseJSON;

		private int m_brandID;

		private int m_productID;
		private String m_productDescription;

		GetProductsTask(int brandID)
		{
			m_response = "";
			m_responseJSON = null;

			this.m_brandID = brandID;

			m_productID = -1;
			m_productDescription = "";
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
				JSONObject jsonAll = new JSONObject();

				String authToken = getMobileToken();
				JSONObject json = new JSONObject();
				json.put("mobile_token", authToken);
				jsonAll.put("auth", json);

				json = new JSONObject();

				json.put("brandID", this.m_brandID);

				jsonAll.put("data", json);

				//DEBUG:
				System.out.println("SENDING:");
				System.out.println(jsonAll.toString());

				OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");
				osw.write(jsonAll.toString());
				osw.flush();
				osw.close();

			}
			catch (JSONException e)
			{
				e.printStackTrace();
				return false;
			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
				return false;
			}
			catch (IOException e)
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
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			return false;
		}

		private boolean send()
		{
			try
			{
				URL url = new URL(LOGIN_URL);
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
			if (send())
			{
				//Successful post.
				System.out.println("Successfully sent products with brand id request.");

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
						m_responseJSON = responseJSON.getJSONArray("data");
						//DEBUG:
						System.out.println("Products with brand id request successful.");

						return true;
					}
					else
					{
						//DEBUG:
						System.out.println("Products with brand id request failed.");
						System.out.println("ERROR CODE:");
						System.out.println(auth.getString("error_code"));

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
				System.out.println("Failed to send products with brand id request.");
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

		public ArrayList<String> populateProductSpinner(JSONArray data)
		{
			ArrayList<String> resultList = new ArrayList<String>();

			//DEBUG:
			System.out.println("JSON: ");
			System.out.println(data.toString());


			m_productIDItems = new int[data.length()];
			for (int i = 0; i < data.length(); i++)
			{
				try
				{
					JSONObject row = data.getJSONObject(i);
					MedicationBrand brand = new MedicationBrand();

					//TODO: Store product id.
					//int productID = row.getInt("id");

					String productDescription = row.getString("description");
					m_productIDItems[i] = row.getInt("id");

					resultList.add(productDescription);

				} catch (JSONException e)
				{
					e.printStackTrace();
				}
			}

			return resultList;
		}

		@Override
		protected void onPostExecute(final Boolean success)
		{
			m_addMedicationTask = null;
			showProgress(false);

			if (success)
			{
				//Fetched products with brand id.

				//TODO: Create lookup array of product details in order of spinner ids.

				//TODO: Add descriptions to the product spinner.
				m_productDescriptionItems.clear();
				m_productDescriptionItems = populateProductSpinner(m_responseJSON);
				m_productDescriptionAdapter.addAll(m_productDescriptionItems);
				m_productDescriptionAdapter.notifyDataSetChanged();

				//TODO: Show the add medication form.

			}
			else
			{
				//Failed to fetch products with brand id.
				//m_passwordEditOne.setError(getString(R.string.error_incorrect_password));
				//m_passwordEditOne.requestFocus();
			}
		}

		@Override
		protected void onCancelled()
		{
			m_addMedicationTask = null;
			showProgress(false);
		}
	}

	private boolean addMedicationValidation(int productSelect, String reminderDate, String reminderTime,
											String repeatSelect, String repeatCustom, int toTake,
											int toRemaining, String nickname)
	{
		//TODO: Check if the user is trying to schedule in the past or too far in the future.
		//This would involve getting the current datetime (probably from the server).

		//Product ID.
		if (productSelect < 0)
		{
			System.out.println("Product id is not valid.");
			return false;
		}

		//Reminder date.
		if (reminderDate.length() == 0)
		{
			m_reminderDateView.setError("Reminder date was not given.");
			m_reminderDateView.requestFocus();
			return false;
		}

		Pattern pattern = Pattern.compile("^((\\d{4})-(\\d{2})-(\\d{2}))$");
		Matcher match = pattern.matcher(reminderDate);
		boolean matched = match.matches();
		if (!matched)
		{
			m_reminderDateView.setError("Reminder date was not written in the correct format.");
			m_reminderDateView.requestFocus();
			return false;
		}

		//Reminder time.
		if (reminderTime.length() == 0)
		{
			m_reminderTimeView.setError("Reminder time was not given.");
			m_reminderTimeView.requestFocus();

			return false;
		}

		pattern = Pattern.compile("^(\\d{2}):(\\d{2})$");
		match = pattern.matcher(reminderTime);
		matched = match.matches();
		if (matched)
		{
			int hours = Integer.parseInt(match.group(1));
			int minutes = Integer.parseInt(match.group(2));

			if (hours > 24 || hours < 0)
			{
				m_reminderTimeView.setError("Reminder time hour value is not valid.");
				m_reminderTimeView.requestFocus();

				return false;
			}

			if (minutes < 0 || minutes > 60)
			{
				m_reminderTimeView.setError("Reminder time minutes value is not valid.");
				m_reminderTimeView.requestFocus();

				return false;
			}
		}
		else
		{
			m_reminderTimeView.setError("Reminder time was not written in the correct format.");
			m_reminderTimeView.requestFocus();

			return false;
		}

		//Items to take.
		if (toTake < TO_TAKE_MIN || toTake > TO_TAKE_MAX)
		{
			m_toTakeView.setError("Number of items to take must be more than 0 and less than " +
					TO_TAKE_MAX + ".");
			m_toTakeView.requestFocus();

			return false;
		}

		//Items remaining.
		if (toRemaining < 0 || toRemaining < toTake || toRemaining > TO_REMAINING_MAX)
		{
			m_toRemainingView.setError("Number of items remaining must be more than 0 and less than " +
					TO_REMAINING_MAX + " and greater than the number of items to take.");
			m_toRemainingView.requestFocus();

			return false;
		}

		//Nickname.
		int nicknameLength = nickname.length();
		if (nicknameLength < NICKNAME_MIN || nicknameLength > NICKNAME_MAX)
		{
			m_nicknameView.setError("Nickname length must be greater than " +
					NICKNAME_MIN + " and less than " + NICKNAME_MAX + ".");
			m_nicknameView.requestFocus();

			return false;
		}

		//Repeat options.
		if (repeatSelect.length() == 0 ||
				repeatSelect.compareTo("never") != 0 &&
				repeatSelect.compareTo("daily") != 0 &&
				repeatSelect.compareTo("weekly") != 0 &&
				repeatSelect.compareTo("custom") != 0)
		{
			//TODO: User feedback.
			System.out.println(repeatSelect);
			System.out.println("Unknown repeat value given.");

			return false;
		}

		//Repeat custom
		//repeatCustom can be blank UNLESS repeatSelect is set to 'custom'.
		if (repeatSelect.compareTo("custom") == 0)
		{
			//Repeat custom day interval.
			//TODO: repeatCustom should always be an int.
			int repeatCustomInt = Integer.parseInt(repeatCustom);
			if (repeatCustomInt < 0 || repeatCustomInt > REPEAT_CUSTOM_MAX)
			{
				System.out.println("Custom repeat day interval must be more than 0 and less than " +
						REPEAT_CUSTOM_MAX + ".");
				return false;
			}
		}

		return true;
	}

	//Returns -1 on failure.
	private int getNumberFromInput(String inputString)
	{
		int result = -1;
		if (inputString.length() > 0)
		{
			try
			{
				result = Integer.parseInt(inputString);
			}
			catch (NumberFormatException e)
			{
				System.out.println("Input value must be a number.");
			}
		}
		else
		{
			System.out.println("Input value not given.");
		}

		return result;
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
		View focusView = m_addMedicationFormView;

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

							goSchedule();
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

	public void addMedication()
	{
		if (m_addMedicationTask != null)
		{
			return;
		}

		//Reset errors.
		m_reminderDateView.setError(null);
		m_reminderTimeView.setError(null);
		m_toTakeView.setError(null);
		m_toRemainingView.setError(null);
		m_nicknameView.setError(null);

		//Get values.
		//TODO: Lookup the product id based on product name.
		int idArrayIndex = m_productSelectView.getSelectedItemPosition();
		int productSelect =  m_productIDItems[idArrayIndex];

		String reminderDate = m_reminderDateView.getText().toString();
		String reminderTime = m_reminderTimeView.getText().toString();
		String repeatSelect = m_repeatSelectView.getSelectedItem().toString().toLowerCase();

		String repeatCustom = m_repeatCustomView.getText().toString();
		int toTake = getNumberFromInput(m_toTakeView.getText().toString());
		if (toTake == -1)
		{
			m_toTakeView.setError("Items to take is blank or not a number.");
			m_toTakeView.requestFocus();

			return;
		}
		int toRemaining = getNumberFromInput(m_toRemainingView.getText().toString());
		if (toRemaining == -1)
		{
			m_toRemainingView.setError("Items remaining is blank or not a number.");
			m_toRemainingView.requestFocus();

			return;
		}
		String nickname = m_nicknameView.getText().toString();

		//TODO: Input validation.
		if (!addMedicationValidation(productSelect, reminderDate,
				reminderTime, repeatSelect, repeatCustom, toTake, toRemaining, nickname))
		{
			return;
		}

		boolean cancel = false;
		View focusView = m_addMedicationFormView;

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
			System.out.println("Cancelling add medication request...");
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
			m_addMedicationTask = new AddMedicationTask(productSelect, reminderDate,
					reminderTime, repeatSelect, repeatCustom, toTake, toRemaining, nickname);
			m_addMedicationTask.execute((Void) null);
		}
	}

	/**
	 * Represents an asynchronous delete account request.
	 */
	public class AddMedicationTask extends AsyncTask<Void, Void, Boolean>
	{
		private static final String PREFS_NAME = "TrackMyMedsPref";
		private static final String LOGIN_URL = "https://trackmymeds.frb.io/med_add_mobile";

		String m_errorMessage;
		String m_response;
		private JSONArray m_responseJSON;

		private int m_productSelect;
		private String m_reminderDate;
		private String m_reminderTime;
		private String m_repeatSelect;
		private String m_repeatCustom;
		private int m_toTake;
		private int m_toRemaining;
		private String m_nickname;

		AddMedicationTask(int productSelect, String reminderDate, String reminderTime,
						  String repeatSelect, String repeatCustom, int toTake,
						  int toRemaining, String nickname)
		{
			m_errorMessage = "";
			m_response = "";
			m_responseJSON = null;

			m_productSelect = productSelect;
			m_reminderDate = reminderDate;
			m_reminderTime = reminderTime;
			m_repeatSelect = repeatSelect;
			m_repeatCustom = repeatCustom;
			m_toTake = toTake;
			m_toRemaining = toRemaining;
			m_nickname = nickname;
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
				JSONObject jsonAll = new JSONObject();

				//TODO:
				String authToken = getMobileToken();
				JSONObject json = new JSONObject();
				json.put("mobile_token", "d37db33490d3d828e89e3447cbf0b2cb9cfa98a3931675e612aa9b4b81aa95d8e151e6f57d29601c765472f11d48371e5989b1a6998dc1796ea14adb84ab4ff8");
				jsonAll.put("auth", json);

				json = new JSONObject();

				json.put("medAddType", m_medAddType);
				json.put("medScheduleID", m_medScheduleID);

				json.put("productSelect", m_productSelect);
				json.put("reminderDate", m_reminderDate);
				json.put("reminderTime", m_reminderTime);
				json.put("repeatSelect", m_repeatSelect);
				json.put("repeatCustom", m_repeatCustom);
				json.put("toTake", m_toTake);
				json.put("toRemaining", m_toRemaining);
				json.put("nickname", m_nickname);

				jsonAll.put("data", json);

				//DEBUG:
				System.out.println("SENDING:");
				System.out.println(jsonAll.toString());

				OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");
				osw.write(jsonAll.toString());
				osw.flush();
				osw.close();

			}
			catch (JSONException e)
			{
				e.printStackTrace();
				return false;
			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
				return false;
			}
			catch (IOException e)
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
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			return false;
		}

		private boolean send()
		{
			try
			{
				URL url = new URL(LOGIN_URL);
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
			if (send())
			{
				//Successful post.
				System.out.println("Successfully sent add medication request.");

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
						//DEBUG:
						System.out.println("Add medication successful.");

						return true;
					}
					else
					{
						//DEBUG:
						System.out.println("Add medication failed.");
						System.out.println("ERROR CODE:");
						System.out.println(auth.getString("error_code"));
						m_errorMessage = auth.getString("error_message");

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
				System.out.println("Failed to send add medication request.");
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
			m_addMedicationTask = null;
			showProgress(false);

			if (success)
			{
				//Added medication to user schedule.
				Intent intent = new Intent(getBaseContext(), ScheduleActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); //TODO: Might not need these.
				startActivity(intent);
			}
			else
			{
				//Failed to delete account.
				//m_passwordEditOne.setError(getString(R.string.error_incorrect_password));
				//m_passwordEditOne.requestFocus();
				m_reminderDateView.setError(m_errorMessage);
			}
		}

		@Override
		protected void onCancelled()
		{
			m_addMedicationTask = null;
			showProgress(false);
		}
	}
}
