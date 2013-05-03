package com.example.btremote;

import android.app.Activity;
import android.bluetooth.*;
import android.content.Intent;
import android.os.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BlåTann extends Thread{
	private MainActivity ref;
	private BluetoothAdapter mmBluetoothAdapter;
	private BluetoothDevice mmDevice;
	private BluetoothSocket mmSocket;
	private OutputStream mmOutputStream;
	
	public BlåTann(MainActivity ref){
		this.ref = ref;
	}
	
	@Override
	public void run(){
		
		mmBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		if(!mmBluetoothAdapter.isEnabled()){
			Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		}//ENDIF
		Set<BluetoothDevice>pairedDevices = mmBluetoothAdapter.getBondedDevices();
		
		if(pairedDevices.size() > 0){
			for(BluetoothDevice device : pairedDevices){
				if(device.getName().equals("ArduinoRemote")){
					mmDevice = device;
					break;
				}//ENDIF
			}//ENDFOR
		}//ENDPAIR
		UUID uuid= UUID.fromString("00001101-0000-1000-8000-00805f9bd34fb");//Std serial BT interface;
		
		try{
			mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
			mmSocket.connect();
			mmOutputStream = mmSocket.getOutputStream();
			String msg = "connected";
			mmOutputStream.write(msg.getBytes());
		}catch(IOException e){
			e.printStackTrace();
		}
		 ref.actionMan();
	

}
}
