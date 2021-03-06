package com.zechin.rebootPressureTest;

import com.zechin.rebootPressureTest.RebootPressureTest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {

	private boolean rebootStartFlag;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(context);
        if(pm == null) {
        	rebootStartFlag = false;
        } else {
        	rebootStartFlag = pm.getBoolean(RebootPressureTest.AUTOBOOT, true);
        }
        if(rebootStartFlag) {
            Intent localIntent = new Intent(context, RebootPressureTest.class);
            localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(localIntent);
        }
	}

}
