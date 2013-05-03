package com.example.btremote;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.Menu;
import android.widget.TextView;
import android.bluetooth.*;
import android.content.Intent;

public class MainActivity extends Activity {
	private Handler handler;
	private BlaaTann link;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		handler = new Handler();
		link = new BlaaTann(this);
		link.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void klikkefaen() {
		TextView tv = (TextView) findViewById(R.id.textView1);
		tv.setText("You've clicked, you've handled and you've waited!");
	}

	public void actionMan() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				klikkefaen();
			}
		});
	}

	public void enableBT() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				Intent enableBluetooth = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBluetooth, 0);
			}
		});
	}

	public void feilmelding(String feil) {
		handler.post(new Feilmelder(feil));
	}

	/*
	 * Denne klassen får en feilmeldingsdialog til å poppe opp
	 * 
	 */
	class Feilmelder implements Runnable { 
		private String feil;

		public Feilmelder(String x) {
			this.feil = x;
		}

		@Override
		public void run() {
			new AlertDialog.Builder(MainActivity.this).setTitle("Argh")
					.setMessage(feil).setNeutralButton("Close", null).show();
		}
	}
}
