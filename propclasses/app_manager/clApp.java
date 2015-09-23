
package org.androidtown.sleeper.propclasses.app_manager;

//
//
//  Generated by StarUML(tm) Java Add-In
//
//  @ Project : Sleeper
//  @ File Name : clApp.java
//  @ Date : 2015-09-06
//  @ Author : Kang Shin Wook, Kim Hyun Woong, Lim Hyun Woo
//  @ Email : rkdtlsdnr102@naver.com

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import org.androidtown.sleeper.propclasses.com_manager.clComManager;
import org.androidtown.sleeper.propclasses.dataprocessor_manager.clDataProcessor;

import java.net.InetAddress;

/**
 * Controls basic app's behavior. In order to use this class, you have to add user permission below:
 *
 * uses-permission android:name="android.permission.ACCESS_WIFI_STATE"
 * uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"
 * uses-permission android:name="android.permission.INTERNET"
 * uses-permission android:name="android.permission.WAKE_LOCK"
 */
public class clApp{

	private clDataProcessor DataProcessor;
	private Context AttachedContext=null ;
	private PowerManager.WakeLock wakelock=null ;

	private AlarmManager alarmManager=null ;
	private WifiManager wifiManager=null ;
	private PendingIntent alarmIntent =null ;
	private WifiReceiver wifiReceiver=null ;
	private AlarmReceiver alarmReceiver=null ;

	private boolean isRunning=false ;

	public static final String APP_ACTION_ALARM_TRIGGERED ="APP_ACTION_ALARM_TRIGGERED" ;

	/**
	 * Constructor
	 * @param context context to run app on
	 * @param _DataProcessor data processor
	 */
	public clApp(Context context, clDataProcessor _DataProcessor) {

		//set data processor, communcation manager
        this.DataProcessor=_DataProcessor ;
		this.AttachedContext=context ;

		initializeApp();
	}

	/**
	 * Initialize app
	 */
	private void initializeApp(){


        //get wakelock
		PowerManager powerManager = (PowerManager) AttachedContext.getSystemService(Context.POWER_SERVICE);
		wakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				"MyWakelockTag");


		//register broadcast receivers
		alarmManager=(AlarmManager)AttachedContext.getSystemService(Context.ALARM_SERVICE) ;
		wifiManager = (WifiManager)AttachedContext.getSystemService(Context.WIFI_SERVICE);

	}

	/**
	 * Start sleep mode
	 * @param ringTimeMillis time to ring alarm from current time
	 */
	public void startSleepMode(long ringTimeMillis) {

		if(!isRunning) {

			//if wakelock is not held
			if (!wakelock.isHeld())
				wakelock.acquire();


			//register alarm receiver
			alarmReceiver = new AlarmReceiver();
			IntentFilter filter = new IntentFilter(APP_ACTION_ALARM_TRIGGERED);
			AttachedContext.registerReceiver(alarmReceiver, filter);

			//register alarm receiver
			Intent intent = new Intent(APP_ACTION_ALARM_TRIGGERED);
			alarmIntent = PendingIntent.getBroadcast(AttachedContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + ringTimeMillis, alarmIntent);

			DataProcessor.measureStart();

			isRunning=true ;
		}
	
	}

	/**
	 * Stop sleep mode
	 */
	public void stopSleepMode() {

		if(isRunning) {

			DataProcessor.measureStop();

			AttachedContext.unregisterReceiver(alarmReceiver);

			//if wakelock is held
			if (wakelock.isHeld())
				wakelock.release();

			alarmManager.cancel(alarmIntent);

			isRunning=false ;
		}
	}

	/**
	 * Call this function in onResume method in attached context.
	 */
	public void onResume(){

		//register wifi receiver
		IntentFilter filter=new IntentFilter() ;
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION) ;
		wifiReceiver=new WifiReceiver() ;
		AttachedContext.registerReceiver(wifiReceiver, filter) ;
	}

	/**
	 * Call this function in onPause method in attached context.
	 */
	public void onPause(){

		AttachedContext.unregisterReceiver(wifiReceiver) ;
	}

	/**
	 * Call this function in onDestroy method in attached context.
	 */
	public void onDestroy() {

		//alarmManager.cancel(alarmIntent) ;
	}

	/**
	 * Get data processor
	 * @return data processor that is attached to this app
	 */
	public clDataProcessor getDataProcessor(){

		return DataProcessor ;
	}

    @Deprecated
    /**
     * Checks if app is currently connected via wifi
     * @return true if connected, otherwise false
     */
	private boolean isConnectedViaWifi() {
		ConnectivityManager connectivityManager = (ConnectivityManager)AttachedContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		return mWifi.isConnected();
	}

    /**
     * Receives alarm intent
     */
	private class AlarmReceiver extends BroadcastReceiver {

		public AlarmReceiver() {
			super();
		}

		//it receives only alarm related intent
		@Override
		public void onReceive(Context context, Intent intent) {

			//else if alarm ring intent received
			//you should put condition to check if intent is correct
			if(intent.getAction().equals(APP_ACTION_ALARM_TRIGGERED)) {

				stopSleepMode();

				Toast.makeText(context, "Alarm Rang!!", Toast.LENGTH_SHORT).show();
			}
		}
	}


    /**
     * used to receive wifi status change receive
     */
	private class WifiReceiver  extends BroadcastReceiver {


		public WifiReceiver() {
			super();
		}

		@Override
		public void onReceive(Context context, Intent intent) {

			if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {

				SetIp();
			}
		}

        /**
         * set ip of connected device into com manager
         */
		private void SetIp(){

			wifiManager = (WifiManager)AttachedContext.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();

			int ipAddress = wifiInfo.getIpAddress();


			byte firstIpGroup = (byte) (ipAddress);
			byte secondIpGroup = (byte) (ipAddress >> 8);
			byte thirdIpGroup = (byte) (ipAddress >> 16);
			byte fourthIpGroup = 0x01 ;

			byte[] ipAddr = {firstIpGroup, secondIpGroup, thirdIpGroup, fourthIpGroup};

			try {

				clComManager.setIpAddr(InetAddress.getByAddress(ipAddr));

			} catch (Exception e) {

				Log.i(toString(), e.getMessage());
			}

		}

	}

}
