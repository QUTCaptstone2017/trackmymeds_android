package com.app.trackmymeds;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

public class SplashActivity extends AppCompatActivity
{

	private class LoadingThread extends Thread
	{
		SplashActivity m_parent;
		ProgressBar m_loadingBar;
		int m_progress = 0;
		int m_max;
		boolean isDone = false;

		LoadingThread(SplashActivity _parent, ProgressBar _loadingBar)
		{
			m_parent = _parent;
			m_loadingBar = _loadingBar;
			m_max = m_loadingBar.getMax();
		}

		public void run()
		{
			try
			{
				while (m_progress < m_max)
				{
					m_progress += 2;
					m_loadingBar.setProgress(m_progress);

					this.sleep(50);
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}

			isDone = true;

			Intent intent = new Intent(m_parent, LoginActivity.class);
			startActivity(intent);

			m_parent.finish();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		ProgressBar loadingBarView = (ProgressBar) findViewById(R.id.loadingBar);

		LoadingThread lt = new LoadingThread(this, loadingBarView);
		lt.start();
	}
}
