package com.example.btremote;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;
import android.bluetooth.*;

public class MainActivity extends Activity {
	private Handler handler;
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler();
        BlåTann link = new BlåTann(this);
        link.start();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
	public void klikkefaen(){
	TextView tv = (TextView)findViewById(R.id.textView1);
	tv.setText("You've clicked, you've handled and you've waited!");
	}
	
	public void actionMan() {
    	handler.post(new Runnable(){
    		@Override
    		public void run(){
    			klikkefaen();
    		}
    	});
	}
}
