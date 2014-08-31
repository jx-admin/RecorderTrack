package com.compal.bsp.Audio_abs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.compal.bsp.Audio_abs.AudioRecordTrack.TrackFileUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

public class RecorderTrack extends Activity {
	private String TAG = "session";
	
	private RadioGroup mRadioGroup1, mRadioGroup2, mRadioGroup3;
	private RadioButton mRadio1, mRadio2, mRadio3, mRadio4, mRadio5, mRadio6,
	mRadio7;
	
	SeekBar skbVolume;// 调节音量
	AudioRecordTrack mRecordUtils;
	TrackFileUtils mTrackFileUtils=new TrackFileUtils();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		setButtonHandlers();
		enableButton1(false);
		mRecordUtils = new AudioRecordTrack();
	}
	
	private void setButtonHandlers() {
		((Button) findViewById(R.id.btnRecord)).setOnClickListener(btnClick);
		((Button) findViewById(R.id.btnStop)).setOnClickListener(btnClick);
		((Button) findViewById(R.id.btnTrack)).setOnClickListener(btnClick);
		((Button) findViewById(R.id.btnStop2)).setOnClickListener(btnClick);
		// ((Button)findViewById(R.id.btnExit)).setOnClickListener(btnClick);
		mRadioGroup1 = (RadioGroup) findViewById(R.id.myRadioGroup1);
		mRadioGroup2 = (RadioGroup) findViewById(R.id.myRadioGroup2);
		mRadioGroup3 = (RadioGroup) findViewById(R.id.myRadioGroup3);
		mRadio1 = (RadioButton) findViewById(R.id.myRadio1Button1);
		mRadio2 = (RadioButton) findViewById(R.id.myRadio1Button2);
		mRadio3 = (RadioButton) findViewById(R.id.myRadio2Button1);
		mRadio4 = (RadioButton) findViewById(R.id.myRadio2Button2);
		mRadio5 = (RadioButton) findViewById(R.id.myRadio2Button3);
		// EncodingBitRate
		mRadio6 = (RadioButton) findViewById(R.id.myRadio3Button1);
		mRadio7 = (RadioButton) findViewById(R.id.myRadio3Button2);
		
		mRadioGroup1.setOnCheckedChangeListener(mChangeRadio1);
		mRadioGroup2.setOnCheckedChangeListener(mChangeRadio2);
		mRadioGroup3.setOnCheckedChangeListener(mChangeRadio3);
	}
	
	private void enableButton(int id, boolean isEnable) {
		((Button) findViewById(id)).setEnabled(isEnable);
	}
	
	private void enableButton0(boolean isRecording) {
		enableButton(R.id.btnRecord, !isRecording);
//		enableButton(R.id.btnTrack, !isRecording);
//		enableButton(R.id.btnStop2, !isRecording);
		enableButton(R.id.btnStop, isRecording);
	}
	
	private void enableButton1(boolean isRecording) {
		enableButton(R.id.btnRecord, !isRecording);
//		enableButton(R.id.btnTrack, isRecording);
//		enableButton(R.id.btnStop2, isRecording);
		enableButton(R.id.btnStop, isRecording);
	}
	
	private void enableButton2(boolean isRecording) {
		enableButton(R.id.btnRecord, !isRecording);
//		enableButton(R.id.btnTrack, !isRecording);
//		enableButton(R.id.btnStop2, isRecording);
		enableButton(R.id.btnStop, isRecording);
	}
	
	private void enableButton3(boolean isTracking) {
		//enableButton(R.id.btnRecord, !isTracking);
//		enableButton(R.id.btnStop, !isTracking);
		enableButton(R.id.btnTrack, !isTracking);
		enableButton(R.id.btnStop2, isTracking);
	}
	
	private void enableButton4(boolean isTracking) {
		enableButton(R.id.btnRecord, !isTracking);
		enableButton(R.id.btnStop, isTracking);
//		enableButton(R.id.btnTrack, !isTracking);
//		enableButton(R.id.btnStop2, isTracking);
	}
	
	private View.OnClickListener btnClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnRecord: {
				AppLog.logString("Start Recording");
				
				enableButton0(true);
				mRecordUtils.startRecording(mTrackFileUtils.getPipedOutputStream());
				
				break;
			}
			case R.id.btnStop: {
				AppLog.logString("Start Recording");
				
				enableButton2(false);
				mRecordUtils.stopRecording();
				break;
			}
			case R.id.btnTrack: {
				AppLog.logString("Start Tracking");
				
				enableButton3(true);
				
				mRecordUtils.startAudioTrack(mTrackFileUtils.getPipedInputStream());
				Log.i(TAG, "xxxxxxxxxxxxxxxxxxxxxxxxxxxx");
				
				break;
			}
			case R.id.btnStop2: {
				AppLog.logString("Stop Tracking");
				enableButton4(false);
				mRecordUtils.stopAudioTrack();
				
				break;
			}
			
			}
		}
	};
	
	RadioGroup.OnCheckedChangeListener mChangeRadio1 = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			// TODO Auto-generated method stub
			if (checkedId == mRadio1.getId()) {
				mRecordUtils
				.setChannelConfiguration(AudioFormat.CHANNEL_CONFIGURATION_STEREO);
				
			} else if (checkedId == mRadio2.getId()) {
				mRecordUtils
				.setChannelConfiguration(AudioFormat.CHANNEL_CONFIGURATION_MONO);
			}
		}
	};
	
	RadioGroup.OnCheckedChangeListener mChangeRadio2 = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			// TODO Auto-generated method stub
			if (checkedId == mRadio3.getId()) {
				mRecordUtils.setFrequency(44100);
			} else if (checkedId == mRadio4.getId()) {
				
				mRecordUtils.setFrequency(22050);
			} else if (checkedId == mRadio5.getId()) {
				
				mRecordUtils.setFrequency(11025);
			}
		}
	};
	
	RadioGroup.OnCheckedChangeListener mChangeRadio3 = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			// TODO Auto-generated method stub
			if (checkedId == mRadio6.getId()) {
				mRecordUtils.setEncodingBitRate(AudioFormat.ENCODING_PCM_16BIT);
			} else if (checkedId == mRadio7.getId()) {
				
				mRecordUtils.setEncodingBitRate(AudioFormat.ENCODING_PCM_8BIT);
			}
		}
	};
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid());
	}
}
