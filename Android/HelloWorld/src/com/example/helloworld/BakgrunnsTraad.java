package com.example.helloworld;

import java.io.IOException;

import android.util.Log;



 
public class BakgrunnsTraad extends Thread {
	private MainActivity ref;
	
	public BakgrunnsTraad(MainActivity ref){
		this.ref = ref;	}
	
	public void sove(){
		try{
			Thread.sleep(10000);
		} catch (Exception e) {}
	}
	
	public void run(){
		sove();
		ref.actionMan();
		}
	
}
