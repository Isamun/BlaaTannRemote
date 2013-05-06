package com.example.guibt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.*;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.util.Log;

public class MainActivity extends Activity {
	  private BluetoothAdapter mBluetoothAdapter;  //the local device for conecting and pairing with bluetooth
	  private BluetoothDevice mmDevice; //a bluetooth device..
	  private BluetoothSocket mmSocket; // socket used to maintain serialconnection
	  private OutputStream mmOutputStream; //outputstream for bluetooth
	  private InputStream mmInputStream; //inputstream for bluetooth
	  
	  private Button power;
	  private Button source;
	  private Button volumePluss;
	  private Button volumeMinus;
	  private Button channelPluss;
	  private Button channelMinus;
	  private CheckBox connected;
	  
	  
	  
	  private static final String TAG = "Kakelogg";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		power = (Button) findViewById(R.id.power);
		source = (Button) findViewById(R.id.source);
		volumePluss = (Button) findViewById(R.id.volumeUp);
		volumeMinus = (Button) findViewById(R.id.volumeDown);
		channelPluss = (Button) findViewById(R.id.channelUp);
		channelMinus = (Button) findViewById(R.id.channelDown);
		connected = (CheckBox) findViewById(R.id.connected);
		
	      mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //find the local bluetooth adapter
	      
	      //check for bluetooth enabling
	      if(!mBluetoothAdapter.isEnabled())
	      {
	         Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	         startActivityForResult(enableBluetooth, 0);
	      }//end if
	      
	      //find bonded devices
	      Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
	      
	      //check for devicename "Joaquin", our bluetooth module
	      if(pairedDevices.size() > 0)
	      {
	          for(BluetoothDevice device : pairedDevices)
	          {
	              if(device.getName().equals("ArduinoRemote")) //Note, you will need to change this to match the name of your device
	              {
	                  mmDevice = device;
	                  break;
	              }//end if
	          }//end for
	      } // end pairedDevicing
	      
	    //  Log.d(TAG, mmDevice.getUuids()[0].toString());
	      
	      UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
	      //
	      
	      //try to setup connection
	      try {
	    	BluetoothDevice actual = mBluetoothAdapter.getRemoteDevice(mmDevice.getAddress());
	    	  
			mmSocket = actual.createInsecureRfcommSocketToServiceRecord(uuid);
			
			mBluetoothAdapter.cancelDiscovery();

			mmSocket.connect();

			mmOutputStream = mmSocket.getOutputStream();

			mmInputStream = mmSocket.getInputStream();
			
			mmOutputStream.write("Connected!".getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						new AlertDialog.Builder(this).setTitle("Error").setMessage("Cant connect!").setNeutralButton("Close", null).show();  
						
					}
	      power.setOnTouchListener(new OnTouchListener(){
		      @Override
		      public boolean onTouch(View v, MotionEvent event){
		    	  if(event.getAction() == MotionEvent.ACTION_DOWN){
		    		  String msg = "1";
		    		  try{
		    			  mmOutputStream.write(msg.getBytes());
		    		  }catch(IOException e){
		    			  e.printStackTrace();
		    		  }
		    	  }
		    	  return false;
		      }

	      });
	      
	      source.setOnTouchListener(new OnTouchListener(){
		      @Override
		      public boolean onTouch(View v, MotionEvent event){
		    	  if(event.getAction() == MotionEvent.ACTION_DOWN){
		    		  String msg = "2";
		    		  try{
		    			  mmOutputStream.write(msg.getBytes());
		    		  }catch(IOException e){
		    			  e.printStackTrace();
		    		  }
		    	  }
		    	  return false;
		      }

	      });
	      
	      volumePluss.setOnTouchListener(new OnTouchListener(){
		      @Override
		      public boolean onTouch(View v, MotionEvent event){
		    	  if(event.getAction() == MotionEvent.ACTION_DOWN){
		    		  String msg = "3";
		    		  try{
		    			  mmOutputStream.write(msg.getBytes());
		    		  }catch(IOException e){
		    			  e.printStackTrace();
		    		  }
		    	  }
		    	  return false;
		      }

	      });
	      
	      volumeMinus.setOnTouchListener(new OnTouchListener(){
		      @Override
		      public boolean onTouch(View v, MotionEvent event){
		    	  if(event.getAction() == MotionEvent.ACTION_DOWN){
		    		  String msg = "4";
		    		  try{
		    			  mmOutputStream.write(msg.getBytes());
		    		  }catch(IOException e){
		    			  e.printStackTrace();
		    		  }
		    	  }
		    	  return false;
		      }

	      });
	      
	      channelPluss.setOnTouchListener(new OnTouchListener(){
		      @Override
		      public boolean onTouch(View v, MotionEvent event){
		    	  if(event.getAction() == MotionEvent.ACTION_DOWN){
		    		  String msg = "5";
		    		  try{
		    			  mmOutputStream.write(msg.getBytes());
		    		  }catch(IOException e){
		    			  e.printStackTrace();
		    		  }
		    	  }
		    	  return false;
		      }

	      });
	      
	      channelMinus.setOnTouchListener(new OnTouchListener(){
		      @Override
		      public boolean onTouch(View v, MotionEvent event){
		    	  if(event.getAction() == MotionEvent.ACTION_DOWN){
		    		  String msg = "6";
		    		  try{
		    			  mmOutputStream.write(msg.getBytes());
		    		  }catch(IOException e){
		    			  e.printStackTrace();
		    		  }
		    	  }
		    	  return false;
		      }

	      });
	      
	      connected.setChecked(mmSocket.isConnected());
	      
	}
	
}
