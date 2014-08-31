package com.compal.bsp.Audio_abs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

public class AudioRecordTrack {
	
	private static final int RECORDER_BPP = 16;
	private static int frequency = 44100;
	private static int channelConfiguration = AudioFormat.CHANNEL_IN_STEREO;
	private static int EncodingBitRate = AudioFormat.ENCODING_PCM_16BIT;
	
	private AudioRecord audioRecord = null;
	private AudioTrack audioTrack = null;
	private int recBufSize = 0;
	private int playBufSize = 0;
	private Thread recordingThread = null;
	private boolean isRecording = false;
	private boolean isTracking = false;
	private boolean m_keep_running;
	private OutputStream os = null;
	
	protected PCMAudioTrack m_player;
	TrackFileUtils mTrackFileUtils=new TrackFileUtils();
	public void setChannelConfiguration(int channelConfiguration) {
		this.channelConfiguration = channelConfiguration;
	}
	
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
	
	public void setEncodingBitRate(int EncodingBitRate) {
		this.EncodingBitRate = EncodingBitRate;
	}
	
	@SuppressLint("NewApi")
	public void startRecording(OutputStream os) {
		setOutStream(os);
		
		createAudioRecord();
		
		audioRecord.startRecording();
		
		isRecording = true;
		
		recordingThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				writeAudioDataToFile();
			}
		}, "AudioRecorder Thread");
		
		recordingThread.start();
	}
	
	public void setOutStream(OutputStream os){
		this.os=os;
	}
	@SuppressLint("NewApi")
	private void writeAudioDataToFile() {
		byte data[] = new byte[recBufSize];
		
		int read = 0;
		
		if (null != os) {
			while (isRecording) {
				read = audioRecord.read(data, 0, recBufSize);
				
				if (AudioRecord.ERROR_INVALID_OPERATION != read) {
					try {
						os.write(data);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			TrackFileUtils.copyWaveFile(mTrackFileUtils.getTempFilename(), mTrackFileUtils.getFilename());
			mTrackFileUtils.deleteTempFile();
		}
	}
	
	@SuppressLint("NewApi")
	public void stopRecording() {
		if (null != audioRecord) {
			isRecording = false;
			
			audioRecord.stop();
			audioRecord.release();
			
			audioRecord = null;
			recordingThread = null;
		}
	}
	
	@SuppressLint("NewApi")
	private void createAudioRecord() {
		recBufSize = AudioRecord.getMinBufferSize(frequency,
				channelConfiguration, EncodingBitRate);
		
		audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency,
				channelConfiguration, EncodingBitRate, recBufSize);
	}
	
	@SuppressLint("NewApi")
	private void createAudioTrack() {
		playBufSize = AudioTrack.getMinBufferSize(frequency,
				channelConfiguration, EncodingBitRate);
		
		audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency,
				channelConfiguration, EncodingBitRate, playBufSize,
				AudioTrack.MODE_STREAM);
	}
	
	public void startAudioTrack(InputStream in) {
		m_player = new PCMAudioTrack();
		m_player.init();
		m_player.setSourceStream(in);
		m_player.start();
	}
	
	public void stopAudioTrack() {
		m_player.free();
		m_player = null;
	}
	
	public static class TrackFileUtils{
		// private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
		private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
		public static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
		final String FILE_PATH = "/sdcard/AudioRecorder/";
		final String FILE_NAME = "session.wav";
		
		File file;
		public InputStream getTrackFileInputStream(){
			InputStream in=null;
			try {
				file = new File(FILE_PATH, FILE_NAME);
				file.createNewFile();
				in = new FileInputStream(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return in;
		}
		
		public OutputStream getOutputStream(){
			
			String filename =getTempFilename();
			
			try {
				return new FileOutputStream(filename);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		public String getFilename() {
			String filepath = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
			File file = new File(filepath, AUDIO_RECORDER_FOLDER);
			
			if (file.exists()) {
				file.delete();
			}
			
			return (file.getAbsolutePath() + "/session.wav");
		}
		
		public String getTempFilename() {
			String filepath = Environment.getExternalStorageDirectory().getPath();
			File file = new File(filepath, AUDIO_RECORDER_FOLDER);
			
			if (!file.exists()) {
				file.mkdirs();
			}
			
			File tempFile = new File(filepath, AUDIO_RECORDER_TEMP_FILE);
			
			if (tempFile.exists())
				tempFile.delete();
			
			return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
		}
		
		public void deleteTempFile() {
			File file = new File(getTempFilename());
			
			file.delete();
		}
		
		public static void copyWaveFile(String inFilename, String outFilename) {
			FileInputStream in = null;
			FileOutputStream out = null;
			long totalAudioLen = 0;
			long totalDataLen = totalAudioLen + 36;
			long longSampleRate = frequency;
			int channels = 2;
			long byteRate = RECORDER_BPP * frequency * channels / 8;
			
			byte[] data;
			
			try {
				in = new FileInputStream(inFilename);
				data = new byte[in.available()];
				out = new FileOutputStream(outFilename);
				totalAudioLen = in.getChannel().size();
				totalDataLen = totalAudioLen + 36;
				
				AppLog.logString("File size: " + totalDataLen);
				
				WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
						longSampleRate, channels, byteRate);
				
				while (in.read(data) != -1) {
					out.write(data);
				}
				
				in.close();
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private static void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
				long totalDataLen, long longSampleRate, int channels, long byteRate)
						throws IOException {
			
			byte[] header = new byte[44];
			
			header[0] = 'R'; // RIFF/WAVE header
			header[1] = 'I';
			header[2] = 'F';
			header[3] = 'F';
			header[4] = (byte) (totalDataLen & 0xff);
			header[5] = (byte) ((totalDataLen >> 8) & 0xff);
			header[6] = (byte) ((totalDataLen >> 16) & 0xff);
			header[7] = (byte) ((totalDataLen >> 24) & 0xff);
			header[8] = 'W';
			header[9] = 'A';
			header[10] = 'V';
			header[11] = 'E';
			header[12] = 'f'; // 'fmt ' chunk
			header[13] = 'm';
			header[14] = 't';
			header[15] = ' ';
			header[16] = 16; // 4 bytes: size of 'fmt ' chunk
			header[17] = 0;
			header[18] = 0;
			header[19] = 0;
			header[20] = 1; // format = 1
			header[21] = 0;
			header[22] = (byte) channels;
			header[23] = 0;
			header[24] = (byte) (longSampleRate & 0xff);
			header[25] = (byte) ((longSampleRate >> 8) & 0xff);
			header[26] = (byte) ((longSampleRate >> 16) & 0xff);
			header[27] = (byte) ((longSampleRate >> 24) & 0xff);
			header[28] = (byte) (byteRate & 0xff);
			header[29] = (byte) ((byteRate >> 8) & 0xff);
			header[30] = (byte) ((byteRate >> 16) & 0xff);
			header[31] = (byte) ((byteRate >> 24) & 0xff);
			header[32] = (byte) (2 * 16 / 8); // block align
			header[33] = 0;
			header[34] = RECORDER_BPP; // bits per sample
			header[35] = 0;
			header[36] = 'd';
			header[37] = 'a';
			header[38] = 't';
			header[39] = 'a';
			header[40] = (byte) (totalAudioLen & 0xff);
			header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
			header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
			header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
			
			out.write(header, 0, 44);
		}
		
		public OutputStream getPipedOutputStream(){
			if(pos==null){
				try {
					pos=new  PipedOutputStream(pis);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return pos;
		}
		PipedInputStream pis=new PipedInputStream() ;   //构造管道输入流 
		PipedOutputStream pos; //构造输出流并且连接输入流形成管道
		public InputStream getPipedInputStream(){
			return pis;
		}
	}
	
	class PCMAudioTrack extends Thread {
		
		protected byte[] m_out_bytes;
		InputStream in;
		
		public void init() {
			try {
				m_keep_running = true;
				
				createAudioTrack();
				
				m_out_bytes = new byte[playBufSize];
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void setSourceStream(InputStream is){
			in=is;
		}
		
		public void free() {
			m_keep_running = false;
			try {
				Log.d("ddd","free "+Thread.currentThread().getName());
				Thread.sleep(1000);
			} catch (Exception e) {
				Log.d("sleep exceptions...\n", "");
			}
		}
		
		@SuppressLint("NewApi")
		public void run() {
			byte[] bytes_pkg = null;
			audioTrack.play();
			while (m_keep_running) {
				try {
					int rSize=in.read(m_out_bytes);
//					bytes_pkg = m_out_bytes.clone();
					audioTrack.write(m_out_bytes, 0, rSize);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			audioTrack.stop();
			audioTrack = null;
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
