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
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Declan on 14/08/2017.
 */

public class MedListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener
{
	//Properties.
	//FilterMedicationTask m_filterMedicationTask;
	SearchBrandTask m_searchMedicationTask;
	StorageManager m_storageManager;

	private View m_progressView;
	private ListView m_medListView;

	private View m_noResultsLabel;
	private View m_searchGuideLabel;

	private MedicationList m_medListAdapter;
	private ArrayList<MedicationBrand> m_medListItems;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_medication_list);

		Toolbar medicationListToolbar = (Toolbar) findViewById(R.id.medication_list_toolbar);
		setSupportActionBar(medicationListToolbar);

		m_storageManager = new StorageManager();

		m_medListView = (ListView) findViewById(R.id.medication_list);
		m_progressView = findViewById(R.id.medication_list_progress);

		m_noResultsLabel = findViewById(R.id.label_medication_list_no_results);
		m_searchGuideLabel = findViewById(R.id.medication_list_label_search_guide);

		m_medListItems = new ArrayList<MedicationBrand>();

		//TODO: Write a custom styling for this.
		m_medListAdapter = new MedicationList(getBaseContext(), m_medListItems);
		m_medListView.setAdapter(m_medListAdapter);

		m_medListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int position, long id)
			{
				MedicationBrand brand = (MedicationBrand)adapter.getItemAtPosition(position);

				System.out.println("Item Clicked!");
				System.out.println(brand.toString());

				Intent intent = new Intent(getBaseContext(), AddMedicationActivity.class);
				intent.putExtra("EXTRA_MED_ADD_TYPE", "add");
				intent.putExtra("EXTRA_BRAND_JSON", brand.m_json.toString());
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.medication_list, menu);

		MenuItem searchItem = menu.findItem(R.id.menu_action_search);
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
		if (searchView != null)
		{
			searchView.setOnQueryTextListener(this);
			searchView.setQueryHint(getString(R.string.prompt_medication_search));
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			//Filter the medication list by letter
			case R.id.letter_filter_a:
				filterByLetter("a");
			return true;

			case R.id.letter_filter_b:
				filterByLetter("b");
			return true;

			case R.id.letter_filter_c:
				filterByLetter("c");
			return true;

			case R.id.letter_filter_d:
				filterByLetter("d");
			return true;

			case R.id.letter_filter_e:
				filterByLetter("e");
			return true;

			case R.id.letter_filter_f:
				filterByLetter("f");
			return true;

			case R.id.letter_filter_g:
				filterByLetter("g");
			return true;

			case R.id.letter_filter_h:
				filterByLetter("h");
			return true;

			case R.id.letter_filter_i:
				filterByLetter("i");
			return true;

			case R.id.letter_filter_j:
				filterByLetter("j");
			return true;

			case R.id.letter_filter_k:
				filterByLetter("k");
			return true;

			case R.id.letter_filter_l:
				filterByLetter("l");
			return true;

			case R.id.letter_filter_m:
				filterByLetter("m");
			return true;

			case R.id.letter_filter_n:
				filterByLetter("n");
			return true;

			case R.id.letter_filter_o:
				filterByLetter("o");
			return true;

			case R.id.letter_filter_p:
				filterByLetter("p");
			return true;

			case R.id.letter_filter_q:
				filterByLetter("q");
			return true;

			case R.id.letter_filter_r:
				filterByLetter("r");
			return true;

			case R.id.letter_filter_s:
				filterByLetter("s");
			return true;

			case R.id.letter_filter_t:
				filterByLetter("t");
			return true;

			case R.id.letter_filter_u:
				filterByLetter("u");
			return true;

			case R.id.letter_filter_v:
				filterByLetter("v");
			return true;

			case R.id.letter_filter_w:
				filterByLetter("w");
			return true;

			case R.id.letter_filter_x:
				filterByLetter("x");
			return true;

			case R.id.letter_filter_y:
				filterByLetter("y");
			return true;

			case R.id.letter_filter_z:
				filterByLetter("z");
			return true;

			//If we got here, the user's action was not recognized.
			//Invoke the superclass to handle it.
			default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onQueryTextSubmit(String query)
	{
		searchMedication(query, SearchBrandTask.SearchBrandType.SEARCH);

		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText)
	{
		return false;
	}

	private void filterByLetter(String filterLetter)
	{
		searchMedication(filterLetter, SearchBrandTask.SearchBrandType.FILTER);
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

			m_medListView.setVisibility(show ? View.GONE : View.VISIBLE);
			m_medListView.animate().setDuration(shortAnimTime).alpha(
					show ? 0 : 1).setListener(new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationEnd(Animator animation)
				{
					m_medListView.setVisibility(show ? View.GONE : View.VISIBLE);
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
			m_medListView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	public void searchMedication(String searchString,
								 SearchBrandTask.SearchBrandType searchBrandType)
	{
		if (m_searchMedicationTask != null)
		{
			return;
		}

		//Hide the guide text.
		m_searchGuideLabel.setVisibility(View.GONE);

		//Reset errors.
		m_noResultsLabel.setVisibility(View.GONE);

		boolean cancel = false;
		View focusView = m_medListView;

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

			m_storageManager = new StorageManager();
			String mobileToken = m_storageManager.getMobileToken(getApplicationContext());

			//TODO: Determine the search type.
			//Create and run an auth task in the background.
			m_searchMedicationTask = new SearchBrandTask(mobileToken, searchString,
					searchBrandType);

			//Set anonymous response callback.
			m_searchMedicationTask.setDelegate(new SearchBrandTask.AsyncResponse()
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
							//Search/filter successful.
							m_medListItems = populateMedList(m_searchMedicationTask.m_responseJSON);

							if (m_medListItems.size() == 0)
							{
								m_noResultsLabel.setVisibility(View.VISIBLE);
							}

							m_medListAdapter.clear();
							m_medListAdapter.addAll(m_medListItems);
							m_medListAdapter.notifyDataSetChanged();
						}
						else
						{
							//Failed to search/filter.
						}
					}
					else
					{
						//Failed to send request.
					}

					m_searchMedicationTask = null;
					showProgress(false);
				}
			});

			m_searchMedicationTask.execute((Void) null);
		}
	}

	public ArrayList<MedicationBrand> populateMedList(JSONObject response)
	{
		ArrayList<MedicationBrand> resultList = new ArrayList<MedicationBrand>();

		//DEBUG:
		System.out.println("JSON: ");
		System.out.println(response.toString());

		try
		{
			//Convert JSON object to JSON array.
			JSONArray data = response.getJSONArray("data");

			for (int i = 0; i < data.length(); i++)
			{
				JSONObject row = data.getJSONObject(i);

				MedicationBrand brand = new MedicationBrand();
				if (brand.Initialize(row))
				{
					resultList.add(brand);
				}
				else
				{
					System.out.println("MedicationBrand::Initialize(): Failed.");
				}
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}

		return resultList;
	}

}
