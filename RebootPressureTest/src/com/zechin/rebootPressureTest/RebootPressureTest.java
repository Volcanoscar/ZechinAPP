package com.zechin.rebootPressureTest;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.os.PowerManager;
import android.text.TextWatcher;
import android.text.Editable;
public class RebootPressureTest extends Activity {

	public static final String LOG_TAG = "AutoRebootActivity";


	public static final String AUTOBOOT = "autoreboot";
	public static final String CURRENT_REBOOT_COUNTS = "currentRebootCounts";
	public static final String REBOOT_FREQUENCY = "rebootFrequency";
	public static final String TOTAL_REBOOT_COUNTS = "totalRebootCounts";
	public static final int TYPE_REBOOT = 0x01;

	
	private int totalRebootCounts;	
	private int rebootFrequency;	
	private int currentRebootCounts;	
	private int currentRemainTimers;

	
	private boolean rebootFlag;

	private SharedPreferences prefs;
	private EditText rebootTotalCountsNumberEditText;
	private EditText rebootFrequencyEditText;
	private TextView rebootDescriptionText;

	private Button setRebootBtn;
	private Button cancleOkToggleBtn;

	private Handler mHandler;
	private Runnable rebootThread;
	private Handler rebootHandler;

	public RebootPressureTest() {
		// TODO Auto-generated constructor stub
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case TYPE_REBOOT: {
					currentRemainTimers = Integer.parseInt(msg.obj.toString());
					updateDescription();
					break;
				}
				}
				super.handleMessage(msg);
			}
		};
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auto_reboot);

		if (prefs == null) {
			prefs = PreferenceManager.getDefaultSharedPreferences(this);
		}

		updateRebootDataFromDefaultSharedPref();//get reboot data from shard backup file.

		currentRemainTimers = rebootFrequency; //init currentRemainTimers from rebootFrequency
		
		setupView();//create view
		
		setupHandle();
        
		if (currentRebootCounts > totalRebootCounts) {
			resetRebootData();
			updateViewFinishOrError(true);
		} else {
			if(rebootFlag){
			    rebootHandler.postDelayed(rebootThread, 1000);
			}
			updateView();
		    
		}		
		saveRebootDataToSharedPref();		
	}

	private void setupHandle() {
		// TODO Auto-generated method stub
        if(rebootHandler == null) {
        	rebootHandler = new Handler();
        }
        if(rebootThread == null) {
            rebootThread = new Runnable() {
                
                public void run() {
					if(totalRebootCounts - currentRebootCounts < 0){
						updateViewFinishOrError(false);
						return ;
					}
					if(currentRemainTimers > 0) {
                        try {
                        	sendMsg(TYPE_REBOOT,currentRemainTimers-1);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                        rebootHandler.postDelayed(rebootThread, 1000);
                        return;
                    }else{
						currentRebootCounts = currentRebootCounts+1;
						saveRebootDataToSharedPref();
						rebootStart();
					}
					
                }
            };
        }
	}

	private void updateView() {
		// TODO Auto-generated method stub

		rebootTotalCountsNumberEditText.setText(String.valueOf(totalRebootCounts));
		rebootTotalCountsNumberEditText.setEnabled(false);
		rebootFrequencyEditText.setText(String.valueOf(rebootFrequency));
		rebootFrequencyEditText.setEnabled(false);

		updateDescription();
	}

	private void updateViewFinishOrError(boolean flag){
		rebootTotalCountsNumberEditText.setText(String.valueOf(totalRebootCounts));
		rebootTotalCountsNumberEditText.setEnabled(false);
		rebootFrequencyEditText.setText(String.valueOf(rebootFrequency));
		rebootFrequencyEditText.setEnabled(false);
        if(flag){
		    rebootDescriptionText.setText(getString(R.string.RebootFinishTest));	
		}else{
		    rebootDescriptionText.setText(getString(R.string.RebootFinishError));	
		}
		
	}
	
	private void updateDescription(){
		if(rebootFlag){
		    rebootDescriptionText.setText(String.format(getString(R.string.rebootDescription), currentRebootCounts,
				(totalRebootCounts - currentRebootCounts), currentRemainTimers));	
		}else{
			rebootDescriptionText.setText(String.format(getString(R.string.rebootDescription_cancel), currentRebootCounts,
				(totalRebootCounts - currentRebootCounts)));
		}
		
	}
	private void setupView() {
		// TODO Auto-generated method stub
		rebootTotalCountsNumberEditText = (EditText) findViewById(R.id.edit_rebootTotalCounts_number);
		rebootFrequencyEditText = (EditText) findViewById(R.id.edit_rebootFrequency_number);
		
		rebootDescriptionText = (TextView) findViewById(R.id.text_rebootDescription);

		setRebootBtn = (Button) findViewById(R.id.btn_set_reboot);
		setRebootBtn.setText(getString(R.string.setReboot));
		
		cancleOkToggleBtn = (Button) findViewById(R.id.btn_cancle_ok_toggle);
		rebootToggleBtn(rebootFlag);
		
		setRebootBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if(rebootFlag){
					rebootTotalCountsNumberEditText.setEnabled(false);
					rebootFrequencyEditText.setEnabled(false);
				}else{
					rebootTotalCountsNumberEditText.setEnabled(true);
					rebootFrequencyEditText.setEnabled(true);
				}								
			}
		});

		cancleOkToggleBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				rebootFlag = (!rebootFlag) ? true : false;
				if (rebootFlag) {
					updateRebootDataFromView();
					rebootHandler.postDelayed(rebootThread, 1000);
					rebootToggleBtn(rebootFlag);					
				} else {
                    rebootHandler.removeCallbacks(rebootThread);
					rebootToggleBtn(rebootFlag);
				}
				saveRebootDataToSharedPref();
			}
		});
	}

	private void resetRebootData() {
		// TODO Auto-generated method stub
		currentRebootCounts = 1;
		rebootFrequency = 10;
		totalRebootCounts = 5000;
		rebootFlag = true;
	}

	private void updateRebootDataFromDefaultSharedPref() {
		// TODO Auto-generated method stub
		currentRebootCounts = prefs.getInt(CURRENT_REBOOT_COUNTS, 1);
		rebootFrequency = prefs.getInt(REBOOT_FREQUENCY, 10);
		totalRebootCounts = prefs.getInt(TOTAL_REBOOT_COUNTS, 5000);
		rebootFlag = prefs.getBoolean(AUTOBOOT, true);
	}

	private void saveRebootDataToSharedPref() {
		// TODO Auto-generated method stub
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(CURRENT_REBOOT_COUNTS, currentRebootCounts);
		editor.putInt(REBOOT_FREQUENCY, rebootFrequency);
		editor.putInt(TOTAL_REBOOT_COUNTS, totalRebootCounts);
		editor.putBoolean(AUTOBOOT, rebootFlag);
		editor.commit();
	}

	
	private void sendMsg(int type, int remainSecond) {
        Message msg = mHandler.obtainMessage();
        msg.what = type;
        msg.obj = remainSecond;
        mHandler.sendMessage(msg);
    }
	
	private void rebootStart(){
		PowerManager pm = (PowerManager)getSystemService("power");
        pm.reboot(LOG_TAG+" reboot");
	}
	
	private void rebootToggleBtn(boolean flag){
		if(flag){//reboot is going on
			cancleOkToggleBtn.setText(getString(R.string.rebootCancel));
			setRebootBtn.setEnabled(false);//can not to set parameters
            rebootTotalCountsNumberEditText.setEnabled(false);
			rebootFrequencyEditText.setEnabled(false);			
		}else{
			//cancel reboot,so can be set the reboot parameters
			cancleOkToggleBtn.setText(getString(R.string.rebootOK));
		    setRebootBtn.setEnabled(true);
		}
	}
	
	private void updateRebootDataFromView(){
		rebootFrequency = Integer.parseInt(rebootFrequencyEditText.getText().toString());
		currentRemainTimers = rebootFrequency;
		totalRebootCounts = Integer.parseInt(rebootTotalCountsNumberEditText.getText().toString());
	}
}
