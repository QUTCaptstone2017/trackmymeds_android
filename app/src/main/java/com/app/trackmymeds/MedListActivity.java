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

/**
 * Created by Declan on 14/08/2017.
 */

public class MedListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener
{
	//Properties.
	//FilterMedicationTask m_filterMedicationTask;
	SearchMedicationTask m_searchMedicationTask;

	private View m_progressView;
	private ListView m_medListView;

	private ArrayAdapter<MedicationBrand> m_medListAdapter;
	private ArrayList<MedicationBrand> m_medListItems;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_med_list);

		Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(myToolbar);

		m_medListView = (ListView) findViewById(R.id.med_list);
		m_progressView = findViewById(R.id.med_list_progress);

		m_medListItems = new ArrayList<MedicationBrand>();
		m_medListAdapter = new ArrayAdapter(MedListActivity.this, android.R.layout.simple_list_item_1, m_medListItems);
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
				intent.putExtra("EXTRA_BRAND_ID", brand.m_id);
				intent.putExtra("EXTRA_BRAND_SUPPLIER_ID", brand.m_supplierID);
				intent.putExtra("EXTRA_BRAND_NAME", brand.m_name);
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.med_list, menu);

		MenuItem searchItem = menu.findItem(R.id.action_search);
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
		if (searchView != null)
		{
			searchView.setOnQueryTextListener(this);
			searchView.setQueryHint("Search by medication brand name...");
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onQueryTextSubmit(String query)
	{
		searchMedication();

		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText)
	{
		return false;
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

	public void searchMedication()
	{
		if (m_searchMedicationTask != null)
		{
			return;
		}

		//Reset errors.
		//m_firstNameEdit.setError(null);

		boolean cancel = false;
		View focusView = m_medListView;

		SearchView searchView = (SearchView) findViewById(R.id.action_search);
		String searchString = searchView.getQuery().toString();

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
			m_searchMedicationTask = new SearchMedicationTask(searchString);
			m_searchMedicationTask.execute((Void) null);
		}
	}

	/**
	 * Represents an asynchronous daily schedule request.
	 */
	public class SearchMedicationTask extends AsyncTask<Void, Void, Boolean>
	{
		//TODO: Move this.
		private static final String PREFS_NAME = "TrackMyMedsPref";

		private static final String ROUTE_URL = "https://trackmymeds.frb.io/med_search_json_mobile";
		private String m_response;
		private JSONArray m_responseJSON;

		private String m_searchString;

		SearchMedicationTask(String searchString)
		{
			m_response = "";
			m_responseJSON = null;

			m_searchString = searchString;
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
				json.put("s", m_searchString);
				json.put("sb", "s");
				jsonAll.put("data", json);

				//DEBUG:
				System.out.println("SENDING:");
				System.out.println(jsonAll.toString());

				OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");
				osw.write(jsonAll.toString());
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

		private boolean send()
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
			if (send())
			{
				//Successful login.
				System.out.println("Successfully sent medication search.");

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
						System.out.println("Medication search successful.");
						return true;
					}
					else
					{
						System.out.println("Medication search failed.");
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
				System.out.println("Failed to send medication search.");
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

		public ArrayList<MedicationBrand> populateMedList(JSONArray data)
		{
			ArrayList<MedicationBrand> resultList = new ArrayList<MedicationBrand>();

			//DEBUG:
			System.out.println("JSON: ");
			System.out.println(data.toString());

			for (int i = 0; i < data.length(); i++)
			{
				try
				{
					JSONObject row = data.getJSONObject(i);
					MedicationBrand brand = new MedicationBrand();

					brand.m_id = row.getInt("id");
					brand.m_supplierID = row.getInt("supplier_fk");
					brand.m_name = row.getString("name");

					resultList.add(brand);
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
			m_searchMedicationTask = null;
			showProgress(false);

			if (success)
			{
				m_medListItems = populateMedList(m_responseJSON);
				m_medListAdapter.clear();
				m_medListAdapter.addAll(m_medListItems);
				m_medListAdapter.notifyDataSetChanged();
			}
			else
			{

			}
		}

		@Override
		protected void onCancelled()
		{
			m_searchMedicationTask = null;
			showProgress(false);
		}
	}
}
