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
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

	Snackbar m_snackBar;

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

	Button m_addMedButton;

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

		Toolbar myToolbar = (Toolbar) findViewById(R.id.add_medication_toolbar);
		setSupportActionBar(myToolbar);

		m_progressView = findViewById(R.id.add_medication_progress);
		m_addMedicationFormView = findViewById(R.id.add_medication_form);

		m_medAddType = "";
		m_medScheduleID = -1;

		m_productSelectView = (Spinner)findViewById(R.id.add_medication_spinner_products);
		m_reminderDateView = (TextView)findViewById(R.id.add_medication_edit_text_date);
		m_reminderTimeView = (TextView)findViewById(R.id.add_medication_edit_text_time);
		m_repeatSelectView = (Spinner)findViewById(R.id.add_medication_spinner_repeat);
		m_repeatCustomLayout = (TextInputLayout) findViewById(R.id.add_medication_text_layout_repeat_custom);
		m_repeatCustomView = (TextView)findViewById(R.id.add_medication_edit_text_repeat_custom);
		m_toTakeView = (TextView)findViewById(R.id.add_medication_edit_text_items_to_take);
		m_toRemainingView = (TextView)findViewById(R.id.add_medication_edit_text_to_remaining);
		m_nicknameView = (TextView)findViewById(R.id.add_medication_edit_text_nickname);

		m_brandNameView = (TextView)findViewById(R.id.add_medication_value_brand_name);
		m_brandNameView.setText(m_brandName);

		//Product spinner.
		m_productDescriptionItems = new ArrayList<String>();
		m_productDescriptionAdapter = new ArrayAdapter(this, R.layout.spinner_item, m_productDescriptionItems);
		m_productSelectView.setAdapter(m_productDescriptionAdapter);

		//Repeat spinner.
		m_repeatItems = new ArrayList<String>(Arrays.asList(this.getResources().getStringArray(R.array.options_repeat)));
		m_repeatAdapter = new ArrayAdapter(this, R.layout.spinner_item, m_repeatItems);
		m_repeatSelectView.setAdapter(m_repeatAdapter);

		//Bind click event to add medication button.
		m_addMedButton = (Button)findViewById(R.id.add_medication_button_add_medication);
		m_addMedButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				addMedication();
			}
		});

		//Bind item selection event to product description spinner.
		m_productSelectView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> adapter, View v, int position, long id)
			{
				//Get the selected product description.
				String productDescription = (String)adapter.getItemAtPosition(position);

				//DEBUG:
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

		//Bind item selection event to schedule repeat type spinner.
		m_repeatSelectView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> adapter, View v, int position, long id)
			{
				//Get the repeat type string.
				String repeatValue = (String)adapter.getItemAtPosition(position);

				//DEBUG:
				System.out.println("Repeat item selected!");
				System.out.println(repeatValue);

				//Special case for custom repeat type:
				if (repeatValue.toLowerCase().compareTo("custom") == 0)
				{
					//Show custom interval input.
					m_repeatCustomLayout.setVisibility(View.VISIBLE);

					//DEBUG:
					System.out.println("Showing!");
				}
				else
				{
					//Hide custom interval input.
					m_repeatCustomLayout.setVisibility(View.GONE);

					//DEBUG:
					System.out.println("Hiding!");
				}

				//Tell the UI thread this element has changed.
				m_repeatAdapter.notifyDataSetChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
				//DEBUG:
				System.out.println("Nothing selected.");
			}
		});

		//Get passed extras from last activity.
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		handleExtras(extras);
	}

	private void handleExtras(Bundle extras)
	{
		//Get details for editing an existing schedule.
		m_medAddType = extras.getString("EXTRA_MED_ADD_TYPE");
		if (m_medAddType.compareTo("add") == 0)
		{
			String brandJSONString = extras.getString("EXTRA_BRAND_JSON");

			if (brandJSONString != null)
			{
				System.out.println("brandJSONString");
				System.out.println(brandJSONString);

				try
				{
					JSONObject brandJSON = new JSONObject(brandJSONString);
					MedicationBrand brand = new MedicationBrand();

					if (brand.Initialize(brandJSON))
					{
						//Always get these details.
						m_brandID = brand.m_id;
						m_brandSupplierID = brand.m_supplierID;
						m_brandName = brand.m_name;

						m_brandNameView.setText(brand.m_name);

						//DEBUG:
						System.out.println("m_brandID");
						System.out.println(m_brandID);

						System.out.println("m_brandSupplierID");
						System.out.println(m_brandSupplierID);

						System.out.println("m_brandName");
						System.out.println(m_brandName);
					}
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
			}

			//Get products.
			getProducts();
		}
		else if (m_medAddType.compareTo("edit") == 0)
		{
			getSupportActionBar().setTitle("Edit Medication Schedule:");
			m_addMedButton.setText(R.string.button_edit_medication);
			String medScheduleJSONString = extras.getString("EXTRA_SCHEDULE_JSON");

			if (medScheduleJSONString != null)
			{
				System.out.println("medScheduleJSONString");
				System.out.println(medScheduleJSONString);

				try
				{
					JSONObject scheduleJSON = new JSONObject(medScheduleJSONString);
					MedicationSchedule schedule = new MedicationSchedule();

					if (schedule.Initialize(scheduleJSON))
					{
						m_medScheduleID = schedule.m_id;
						m_brandNameView.setText(schedule.m_brandName);
						m_productIDItems = new int[1];
						m_productIDItems[0] = schedule.m_productID;
						m_productDescriptionItems.add(schedule.m_productDescription);
						m_productDescriptionAdapter.notifyDataSetChanged();

						//TODO:
						String timeToTake = "";
						Pattern pattern = Pattern.compile("^(\\d{2}):(\\d{2}):(\\d{2})$");
						Matcher match = pattern.matcher(schedule.m_timeToTake);
						boolean matched = match.matches();
						if (matched)
						{
							timeToTake = match.group(1) + ":" + match.group(2);
							m_reminderTimeView.setText(timeToTake);
						}
						else
						{
							//Failed to read.
							System.out.println("Failed to parse TimeToTake.");
						}

						m_reminderDateView.setText(schedule.m_dateToTake);

						switch (schedule.m_dayInterval)
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
								m_repeatCustomView.setText(String.valueOf(schedule.m_dayInterval));
							break;
						}

						m_toTakeView.setText(String.valueOf(schedule.m_toTake));
						m_toRemainingView.setText(String.valueOf(schedule.m_toRemaining));
						m_nicknameView.setText(schedule.m_nickname);

						showProgress(false);
					}
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
			}
		}
		else
		{
			System.out.println("Unknown EXTRA_MED_ADD_TYPE value.");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		//Handle the selection of our menu items here.
		switch (item.getItemId())
		{
			case R.id.menu_action_delete_medication:
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
		//Instantiate an AlertDialog.Builder with its constructor
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		//Set the dialog values.
		builder.setMessage(R.string.dialog_confirm_delete_medication_prompt)
				.setTitle(R.string.dialog_confirm_delete_medication_title);

		//Add buttons.
		builder.setPositiveButton(R.string.dialog_confirm_delete_medication_yes,
				new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				//User clicked the yes button
				deleteMedication();
			}
		});

		builder.setNegativeButton(R.string.dialog_confirm_delete_medication_cancel,
				new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				//User cancelled the dialog.
			}
		});

		//Create the AlertDialog object.
		AlertDialog dialog = builder.create();

		//Show it to the user.
		dialog.show();
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

	//Shows the progress bar and hides the delete form.
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show)
	{
		//If available, use recent APIs to fade-in the progress spinner.
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
			//Just hide the relevant UI components.
			m_progressView.setVisibility(show ? View.VISIBLE : View.GONE);
			m_addMedicationFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	public ArrayList<String> populateProductSpinner(JSONObject response)
	{
		ArrayList<String> resultList = new ArrayList<String>();

		//DEBUG:
		System.out.println("JSON: ");
		System.out.println(response.toString());

		try
		{
			//Convert JSON object to JSON array.
			JSONArray data = response.getJSONArray("data");

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
		}
		catch (JSONException e)
		{
			//TODO: Show an error for the user.
			e.printStackTrace();
		}

		return resultList;
	}

	public void getProducts()
	{
		if (m_addMedicationTask != null)
		{
			return;
		}

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

			//Get mobile token from application storage.
			String mobileToken = m_storageManager.getMobileToken(getApplicationContext());

			//Create and run an auth task in the background.
			m_getProductsTask = new GetProductsTask(mobileToken, brandID);
			m_getProductsTask.setDelegate(new GetProductsTask.AsyncResponse()
			{
				@Override
				public void onPostExecute(boolean sendSucceeded, boolean taskSucceeded)
				{
					showProgress(false);

					if (taskSucceeded)
					{
						//Fetched products with brand id.

						//TODO: Create lookup array of product details in order of spinner ids.

						//TODO: Add descriptions to the product spinner.
						m_productDescriptionItems.clear();

						m_productDescriptionItems = populateProductSpinner(m_getProductsTask.m_responseJSON);

						m_productDescriptionAdapter.addAll(m_productDescriptionItems);
						m_productDescriptionAdapter.notifyDataSetChanged();

						//TODO: Show the add medication form.

					}
					else
					{
						//Failed to fetch products with brand id.
					}

					m_getProductsTask = null;
				}

			});

			m_getProductsTask.execute((Void) null);
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

		//Reminder time.
		if (reminderTime.length() == 0)
		{
			m_reminderTimeView.setError("Reminder time was not given.");
			m_reminderTimeView.requestFocus();

			return false;
		}

		Pattern pattern = Pattern.compile("^(\\d{2}):(\\d{2})$");
		Matcher match = pattern.matcher(reminderTime);
		boolean matched = match.matches();
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

		//Reminder date.
		if (reminderDate.length() == 0)
		{
			m_reminderDateView.setError("Reminder date was not given.");
			m_reminderDateView.requestFocus();
			return false;
		}

		pattern = Pattern.compile("^((\\d{4})-(\\d{2})-(\\d{2}))$");
		match = pattern.matcher(reminderDate);
		matched = match.matches();
		if (!matched)
		{
			m_reminderDateView.setError("Reminder date was not written in the correct format.");
			m_reminderDateView.requestFocus();
			return false;
		}

		if (toTake == -1)
		{
			m_toTakeView.setError("Items to take is blank or not a number.");
			m_toTakeView.requestFocus();

			return false;
		}

		if (toRemaining == -1)
		{
			m_toRemainingView.setError("Items remaining is blank or not a number.");
			m_toRemainingView.requestFocus();

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
		String medAddType = m_medAddType;

		//TODO: Lookup the product id based on product name.
		int idArrayIndex = m_productSelectView.getSelectedItemPosition();
		int productSelect =  m_productIDItems[idArrayIndex];

		String reminderDate = m_reminderDateView.getText().toString();
		String reminderTime = m_reminderTimeView.getText().toString();
		String repeatSelect = m_repeatSelectView.getSelectedItem().toString().toLowerCase();

		String repeatCustom = m_repeatCustomView.getText().toString();
		int toTake = getNumberFromInput(m_toTakeView.getText().toString());
		int toRemaining = getNumberFromInput(m_toRemainingView.getText().toString());
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

			m_storageManager = new StorageManager();
			String mobileToken = m_storageManager.getMobileToken(getApplicationContext());

			//Create and run an auth task in the background.
			m_addMedicationTask = new AddMedicationTask(mobileToken, medAddType, m_medScheduleID, productSelect, reminderDate,
					reminderTime, repeatSelect, repeatCustom, toTake, toRemaining, nickname);

			m_addMedicationTask.setDelegate(new ScheduleTask.AsyncResponse()
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
							goSchedule(true);
						}
						else
						{
							//Task failed.
							final int snackBarDurationSeconds = 10;
							String errorString = "Error (" + m_addMedicationTask.m_errorCode +
									"): " + m_addMedicationTask.m_errorMessage;

							m_snackBar = Snackbar.make(findViewById(R.id.add_medication_layout),
									errorString, snackBarDurationSeconds * 1000);

							m_snackBar.show();
						}
					}
					else
					{
						//Failed to send request.
						final int snackBarDurationSeconds = 10;
						String errorString = "Error (" + m_addMedicationTask.m_errorCode +
								"): " + m_addMedicationTask.m_errorMessage;

						m_snackBar = Snackbar.make(findViewById(R.id.add_medication_layout),
								errorString, snackBarDurationSeconds * 1000);

						m_snackBar.show();
					}

					m_addMedicationTask = null;
				}
			});

			m_addMedicationTask.execute((Void) null);
		}
	}
}
