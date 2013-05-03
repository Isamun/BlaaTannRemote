package com.komsys.robocam;

import java.io.IOException;      //imports
import java.net.URI;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import de.mjpegsample.MjpegView.MjpegInputStream;
import de.mjpegsample.MjpegView.MjpegView;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import com.komsys.robocam.R;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class RoboCam extends Activity {
private static final String TAG = "MjpegActivity"; 
   
  private BluetoothAdapter mBluetoothAdapter;  //the local device for conecting and pairing with bluetooth
  private BluetoothDevice mmDevice; //a bluetooth device..
  private BluetoothSocket mmSocket; // socket used to maintain serialconnection
  private OutputStream mmOutputStream; //outputstream for bluetooth
  private InputStream mmInputStream; //inputstream for bluetooth
  private TextView tempText; //text to display temperature
  private TextView distText; ////text to display distance
  //text to display slidervalues
  private TextView sliderTextV; 
  private TextView sliderTextH;
  
  //control buttons for turret
  private Button turretDown;
  private Button turretLeft;
  private Button turretRight;
  private Button turretUp;
  private Button turretReset;
  private TextView deltaSliderText;
  private SeekBar deltaSlider;
  
  //the sliders for controlling vehicle
  private VerticalSeekBar verticalSeekbarV; 
  private VerticalSeekBar verticalSeekbarH;
  
//timers for requesting 
  Timer timer1 = new Timer(); 
  Timer timer2 = new Timer();
  
  byte[] packetBytes; // array for recieving packetbytes
  String data; // the recieved data
  
  Thread workerThread; // thread for bluetooth reception of data
  byte[] readBuffer; // buffer for bluetooth reception 
  int readBufferPosition; // store bufferposistion in array
  int counter;  //used in listeningfordata
  volatile boolean stopWorker; // boolean to check for stop in thread
  
  private MjpegView mv;  // the view for "MjpegView"
    
   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
      // initializing View objects      
      tempText = (TextView) findViewById(R.id.textTemp);
      distText = (TextView) findViewById(R.id.textDist);
      deltaSliderText = (TextView) findViewById(R.id.deltaSliderText);
      turretDown = (Button) findViewById(R.id.turretDown);
      turretLeft = (Button) findViewById(R.id.turretLeft);
      turretRight = (Button) findViewById(R.id.turretRight);
      turretUp = (Button) findViewById(R.id.turretUp);
      turretReset = (Button) findViewById(R.id.turretReset);
      deltaSlider = (SeekBar) findViewById(R.id.deltaSlider);
      sliderTextV = (TextView) findViewById(R.id.textViewVenstre);
      sliderTextH = (TextView) findViewById(R.id.textViewHoyre);
      verticalSeekbarV = (VerticalSeekBar)findViewById(R.id.verticalSeekbar1);
      verticalSeekbarH = (VerticalSeekBar)findViewById(R.id.verticalSeekbar);
      
      String URL = "http://admin:@192.168.0.20/video2.mjpg";  //url to ip-cam livefeed
      mv = (MjpegView) findViewById(R.id.video); //initializing custom mjpegview
      new DoRead().execute(URL); // execute the URL to the DoRead function
      
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
              if(device.getName().equals("Joaquin")) //Note, you will need to change this to match the name of your device
              {
                  mmDevice = device;
                  break;
              }//end if
          }//end for
      } // end pairedDevicing
      
      UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
      //
      
      //try to setup connection
      try {
		mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);

		mmSocket.connect();

		mmOutputStream = mmSocket.getOutputStream();

		mmInputStream = mmSocket.getInputStream();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					new AlertDialog.Builder(this).setTitle("Argh").setMessage("Joaquin nekter å svare!").setNeutralButton("Close", null).show();  
					
				}
      
      sensorTimer(); //start sending request for sensordata
      beginListenForData(); //begin listening for data
      
      //making the sliders "thumb" look better
      ShapeDrawable thumb = new ShapeDrawable(new RectShape() );
      thumb.getPaint().setColor(0x00FF00);
      thumb.setIntrinsicHeight(10);
      thumb.setIntrinsicWidth(10);
      verticalSeekbarV.setThumb(thumb);
      verticalSeekbarH.setThumb(thumb);
      
      //servostyringsknapp NED
      turretDown.setOnTouchListener(new OnTouchListener()
      {
    	  @Override
          public boolean onTouch(View v, MotionEvent event)
          {
              if (event.getAction() == MotionEvent.ACTION_DOWN) { //send 001 if button pressed
                  //Log.d("Pressed", "Button pressed");
            	  String msg = "!k001 ";
            	  try {
					mmOutputStream.write(msg.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
              }  
              else if (event.getAction() == MotionEvent.ACTION_UP) { //send 000 if button released
            	  String msg = "!k000 ";
            	  try {
					mmOutputStream.write(msg.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
               //Log.d("Released", "Button released");
              // TODO Auto-generated method stub
          }  
              return false;
          }
      });
      
    //servostyringsknapp OPP
      turretUp.setOnTouchListener(new OnTouchListener()
      {
          @Override
          public boolean onTouch(View v, MotionEvent event)
          {
              if (event.getAction() == MotionEvent.ACTION_DOWN) {
                  //Log.d("Pressed", "Button pressed");
            	  String msg = "!i001 ";
            	  try {
					mmOutputStream.write(msg.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
              }  
              else if (event.getAction() == MotionEvent.ACTION_UP) {
            	  String msg = "!i000 ";
            	  try {
					mmOutputStream.write(msg.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
               //Log.d("Released", "Button released");
              // TODO Auto-generated method stub
          }  
              return false;
          }
      });
      
    //servostyringsknapp VENSTRE
      turretLeft.setOnTouchListener(new OnTouchListener()
      {
          @Override
          public boolean onTouch(View v, MotionEvent event)
          {
              if (event.getAction() == MotionEvent.ACTION_DOWN) {
                  //Log.d("Pressed", "Button pressed");
            	  String msg = "!j001 ";
            	  try {
					mmOutputStream.write(msg.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
              }  
              else if (event.getAction() == MotionEvent.ACTION_UP) {
            	  String msg = "!j000 ";
            	  try {
					mmOutputStream.write(msg.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
               //Log.d("Released", "Button released");
              // TODO Auto-generated method stub
          }  
              return false;
          }
      });
      
    //servostyringsknapp HØGRE
      turretRight.setOnTouchListener(new OnTouchListener()
      {
          @Override
          public boolean onTouch(View v, MotionEvent event)
          {
              if (event.getAction() == MotionEvent.ACTION_DOWN) {
                  //Log.d("Pressed", "Button pressed");
            	  String msg = "!l001 ";
            	  try {
					mmOutputStream.write(msg.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
              }  
              else if (event.getAction() == MotionEvent.ACTION_UP) {
            	  String msg = "!l000 ";
            	  try {
					mmOutputStream.write(msg.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
               //Log.d("Released", "Button released");
              // TODO Auto-generated method stub
          }  
              return false;
          }
      });
      
    //servostyringsknapp NED
      turretReset.setOnTouchListener(new OnTouchListener()
      {
          @Override
          public boolean onTouch(View v, MotionEvent event)
          {
              if (event.getAction() == MotionEvent.ACTION_DOWN) {
                  //Log.d("Pressed", "Button pressed");
            	  String msg = "!v001 ";
            	  try {
					mmOutputStream.write(msg.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
              }  
              else if (event.getAction() == MotionEvent.ACTION_UP) {
            	  String msg = "!v000 ";
            	  try {
					mmOutputStream.write(msg.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
               //Log.d("Released", "Button released");
              // TODO Auto-generated method stub
          }  
              return false;
          }
      });
      
      //slider for controlling servo speed on turret
      deltaSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
		
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub		
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			
			final int progressValue = Integer.parseInt(String.valueOf(progress));
			deltaSliderText.setText(String.valueOf(progressValue)); //display slider value
			String msg = ""; //string to obtain data ready to be sent
			
			//add necesarry zeroes to fill "gaps"
			if ((progressValue) < 10)
				msg = "!m" + "00" + String.valueOf(progressValue) + " ";
			else if ((progressValue) < 100)	
				msg = "!m" + "0" + String.valueOf(progressValue) + " ";
			else
				msg = "!m" + String.valueOf(progressValue) + " ";
			
			//try to send data
			try {
				mmOutputStream.write(msg.getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
	});
      
      //left control slider
      verticalSeekbarV.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){   
 
          @Override
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        	  final int progressValue = Integer.parseInt(String.valueOf(progress));
        	  //if value is in the zero-range, send !e111
        	  if ( (progressValue < 137) && (progressValue > 127) ) {
        		sliderTextV.setText(String.valueOf(0));
        		
        		String msg = "!e" + String.valueOf(111) + " ";
                
                try {
					mmOutputStream.write(msg.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}//end catch

                //if value is under 127, do necesarry formating and send data
        	  }
        	  else if (progressValue <= 127){
        		  sliderTextV.setText(String.valueOf( (progressValue-127)));
        		  String msg = "";
        		  
        		  if (-(progressValue-127) < 10) 
        			   msg = "!d" + "00" + String.valueOf(-(progressValue-127)) + " ";
        		  
        		  else if (-(progressValue-127) < 100) 
       			   msg = "!d" + "0" + String.valueOf(-(progressValue-127)) + " ";
        		  
       			  else 
       				msg = "!d" + String.valueOf(-(progressValue-127)) + " ";
       		  
                  
                  try {
					mmOutputStream.write(msg.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		  
        	  }//end else if
        	  
        	  //if value over 127, do necesarry formatting and send data
        	  else if (progressValue >= 127) {
        		  sliderTextV.setText(String.valueOf(progressValue-137));
        		  String msg = "";
        		  
        		  	if (progressValue-137 < 10)
        		  		msg = "!c" + "00" + String.valueOf(progressValue-137) + " ";
        		  	else if (progressValue-137 < 100)
        		  		msg = "!c" + "0" + String.valueOf(progressValue-137) + " ";
        		  	else
            		  	msg = "!c" + String.valueOf(progressValue-137) + " ";
        		  	
                  try {
					mmOutputStream.write(msg.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	  }//end else if
        		  
          }
          
          //unused functions
          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {
          }

          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {
        	 seekBar.setProgress(133);
        	 
          }
      });
      
      //the right control slider (see commenting above)
      verticalSeekbarH.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

          @Override
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
             
        	  final int progressValue = Integer.parseInt(String.valueOf(progress));
  
        	  if ( (progressValue < 137) && (progressValue > 127) ) {
        		sliderTextH.setText(String.valueOf(0));
        		
        		String msg = "!f" + String.valueOf(111) + " ";
                
                try {
					mmOutputStream.write(msg.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	
        	  }
        	  else if (progressValue <= 127){
        		  sliderTextH.setText(String.valueOf( (progressValue-127)));
        		  String msg = "";
        		  
        		  if (-(progressValue-127) < 10) 
        			   msg = "!b" + "00" + String.valueOf(-(progressValue-127)) + " ";
        		  
        		  else if (-(progressValue-127) < 100) 
       			   msg = "!b" + "0" + String.valueOf(-(progressValue-127)) + " ";
        		  
       			  else 
       				msg = "!b" + String.valueOf(-(progressValue-127)) + " ";
                  try {
					mmOutputStream.write(msg.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		  
        	  }
        	  else if (progressValue >= 127) {
        		  sliderTextH.setText(String.valueOf(progressValue-137));
        		  String msg = "";
        		  
        		  	if (progressValue-137 < 10)
        		  		msg = "!a" + "00" + String.valueOf(progressValue-137) + " ";
        		  	else if (progressValue-137 < 100)
        		  		msg = "!a" + "0" + String.valueOf(progressValue-137) + " ";
        		  	else
            		  	msg = "!a" + String.valueOf(progressValue-137) + " ";
                  try {
					mmOutputStream.write(msg.getBytes());
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		  
        	  }
        	  
          } //END seekbar
          
          //unused methods
          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {

          }

          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {
        	  seekBar.setProgress(133);
        
          }
      });

   }//End of Method onCreate
   
   //when app is closed
   @Override
   protected void onDestroy()
   {	//string objects to send
	   String stop1 = "!e111 ";
	   String stop2 = "!f111 ";
	   String end = "!z111 ";
	   
	   try {
		//send hault vaules   
		mmOutputStream.write(stop1.getBytes());
		mmOutputStream.write(stop2.getBytes());	
		mmOutputStream.write(end.getBytes());
		
		stopWorker = true; //stops listening thread
		
		//cancel timers
		timer1.cancel();
		timer2.cancel();
		//safely close connection
		mmOutputStream.close();
		mmInputStream.close();
		mmSocket.close();
		
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
       super.onDestroy(); //finally
   }
   
   //function for listening
   void beginListenForData()
   {
       final Handler handler = new Handler(); //create handler
       final byte delimiter = 32; //This is the ASCII code for a newline character
       
       stopWorker = false; // to keep thread alive
       readBufferPosition = 0; //initialize position
       readBuffer = new byte[1024]; //create buffer (I chose 1024 bytes for bufferstorage) 
       
       //start new thread
       workerThread = new Thread(new Runnable()
       {
           public void run()
           {                    	   
              while(!Thread.currentThread().isInterrupted() && !stopWorker)
              {
                   try 
                   {
                       int bytesAvailable = mmInputStream.available();       //store bytes from stream               
                       if(bytesAvailable > 0) 
                       {
                           byte[] packetBytes = new byte[bytesAvailable]; //store packet bytes
                           mmInputStream.read(packetBytes); //read packet bytes
                           for(int i=0;i<bytesAvailable;i++) //check every byte read
                           {
                               byte b = packetBytes[i]; //store byte
                               if(b == delimiter) //check if byte is delimiter
                               {
                                   byte[] encodedBytes = new byte[readBufferPosition]; //new encoded bytes
                                   System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);  //copy value from array encodedbytes
                                   final String data = new String(encodedBytes, "US-ASCII"); //store data in string
                                   readBufferPosition = 0; //reset buffer position
                                   
                                   //new handler for recieved data handling
                                   handler.post(new Runnable()
                                   {
                                	   public void run()              //her blir det utført
                                       {
 
                                    	String String1 = data.substring(0, 1); //store first letter
                                   		int sensor = Integer.parseInt(data.substring(1)); //store rest of string
                                   		
                                   		//if first letter is "t", format and display 
                                   		if(String1.contains("t")) {
                                   			double voltage = (sensor/1024.0) * 5.0; //calculate voltage read
                                   			DecimalFormat df = new DecimalFormat("##.#"); //format decimal
                                   			double temp = (voltage - .5)*100; //calculate temp
                                   			String temperature =  String.valueOf(df.format(temp)); //to string
                                   			
                                   			tempText.setText(temperature + " C");   //display temp
                                   		}//end if
                                   		
                                   		//if first letter is "d", format and display
                                   		if(String1.contains("d")) {  
                                   			
                                   			double distance = sensor * 0.00172; //feet to meter
                                   			DecimalFormat df = new DecimalFormat("##.###"); //format decimal
                                   			String distanceWall =  String.valueOf(df.format(distance)); // to string
                                   			
                                   			distText.setText(distanceWall + " m"); //display distance
                                   	
                                   		}//end if
                                   	
                                      }//end run
                                   });
                               }
                               //if not the correct bytes (until delimiter)
                               else
                               {
                                   readBuffer[readBufferPosition++] = b;
                               }
                           }
                       }
                   } 
                   catch (IOException ex) //stop if exception
                   {
                       stopWorker = true;
                   }//end catch
              }//end whilecurrentthread
           }//end Run
       }); // end workerthread
       try{
       workerThread.start(); //start workerthread
       }
       catch (Exception e){
    	   new AlertDialog.Builder(this).setTitle("Argh").setMessage("Watch out!").setNeutralButton("Close", null).show();  
       }
       	
   }

   public void onPause() { //if screen lock on 
       super.onPause();
       mv.stopPlayback(); //stop videostream
       
       //send stop signals
       String stop1 = "!e111 ";
	   String stop2 = "!f111 ";
	   try {
		mmOutputStream.write(stop1.getBytes());
		mmOutputStream.write(stop2.getBytes());
		
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}//end catch
   }//end onPause

   //
   public class DoRead extends AsyncTask<String, Void, MjpegInputStream> {
       protected MjpegInputStream doInBackground(String... url) {
           
    	   //setting up credentials to pass camera authentication (TURN OFF IF POSSIBLE)
    	   CredentialsProvider credProvider = new BasicCredentialsProvider();
    	   credProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
    	   new UsernamePasswordCredentials("admin", ""));
    	   
    	   //settup http client
           HttpResponse res = null;
           DefaultHttpClient httpclient = new DefaultHttpClient();  
           httpclient.setCredentialsProvider(credProvider);
           
           //eror check
           Log.d(TAG, "1. Sending http request");
           try {
               res = httpclient.execute(new HttpGet(URI.create(url[0])));
               Log.d(TAG, "2. Request finished, status = " + res.getStatusLine().getStatusCode());
               if(res.getStatusLine().getStatusCode()==401){
                   return null;
               }
               return new MjpegInputStream(res.getEntity().getContent());  //start new stream
           } catch (ClientProtocolException e) {
               e.printStackTrace();
               Log.d(TAG, "Request failed-ClientProtocolException", e);
               //Error connecting to camera
           } catch (IOException e) {
               e.printStackTrace();
               Log.d(TAG, "Request failed-IOException", e);
               //Error connecting to camera
           }
           return null;
       }
       		//set view parameters
       protected void onPostExecute(MjpegInputStream result) {
    	   mv.setSource(result);
           mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
           mv.showFps(true);
       }

   }//end DoRead
   
  //function for timers
   void sensorTimer() {
	   int delay = 1000; // delay for 1 sec. 
	   int period1 = 10000; // repeat every 10 sec. 
	   int period2 = 1000; // repeat every 1 sec
	    
	   timer1.scheduleAtFixedRate(new TimerTask() //request temp data every 10 seconds
	       { 
	           public void run() 
	           { 
	               try {
	            	   String msg1 = "!q111 ";
					mmOutputStream.write(msg1.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // display the data
	           } 
	       }, delay, period1); 
	   
	   timer2.scheduleAtFixedRate(new TimerTask() //request temp data every 1 second
       { 
           public void run() 
           { 
        	   try {
        		   String msg2 = "!p111 ";
					mmOutputStream.write(msg2.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
                // display the data
				}
           } 
       }, delay, period2); 
   
   } //end sensorTimer
   
} // THE END




