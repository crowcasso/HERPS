package edu.elon.herps;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import android.media.MediaRecorder.OnInfoListener;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class Recorder extends LinearLayout {

	private enum State {
		Init,
		Recording,
		Paused,
		Playing
	}

	MediaRecorder recorder;
	MediaPlayer player;
	String path;

	Button button;
	SeekBar seekBar;
	TextView textViewSeek, textViewLength;

	State state;

	private long startTime;
	private boolean manualSet;

	private FileDescriptor getFDOut() throws FileNotFoundException, IOException {
		Activity context = (Activity)getContext();
		return context.openFileOutput(path, Activity.MODE_PRIVATE).getFD();
	}
	
	private FileDescriptor getFDIn() throws FileNotFoundException, IOException {
		Activity context = (Activity)getContext();
		return context.openFileInput(path).getFD();
	}
	
	private void setSeek(int seek) {
		setSeek(seek, true);
	}

	private void setSeek(int seek, boolean updateSeekbar) {
		textViewSeek.setText(getTimeString(seek));
		if (updateSeekbar) {
			manualSet = true;
			seekBar.setProgress(seek);
		}
	}

	private void setLength(int length) {
		textViewLength.setText(getTimeString(length));
		manualSet = true;
		seekBar.setMax(length);
	}
	
	private String getTimeString(int ms) {
		int minutes = ms / 1000 / 60;
		int seconds = ms / 1000 - minutes * 60;
		return String.format("%d:%02d", minutes, seconds);
	}
	
	public String save() {
		if (state == State.Init) return null;
		if (state == State.Recording) stop();
		if (state == State.Playing) pause();
		return path;
	}
	
	public void load(String path) {
		if (path == null) {
			reset();
			return;
		}
		this.path = path;
		stop();
	}

	public Recorder(Activity context) {
		super(context);

		context.getLayoutInflater().inflate(R.layout.recoder, this);

		recorder = new MediaRecorder();
		player = new MediaPlayer();

		button = (Button)findViewById(R.id.button);
		seekBar = (SeekBar)findViewById(R.id.seekBar);
		textViewSeek = (TextView)findViewById(R.id.textViewSeek);
		textViewLength = (TextView)findViewById(R.id.textViewLength);

		state = State.Init;

		reset();

		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doButton();
			}
		});	    
		
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) { }
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) { }
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (!manualSet) seek();
				manualSet = false;
			}
		});
	}

	private void seek() {
		player.seekTo(seekBar.getProgress());
		setSeek(seekBar.getProgress(), false);
	}
	
	private void doButton() {
		switch (state) {
		case Init:
			record(); break;
		case Recording:
			stop(); break;
		case Paused:
			play(); break;
		case Playing:
			pause(); break;
		}
	}

	private void reset() {

		state = State.Init;
		seekBar.setEnabled(false);
		button.setText("Record");
		setSeek(0);
		setLength(0);

		recorder.reset();
		recorder.setAudioSource(AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		try {
			path = System.currentTimeMillis() + ".3gp";
			recorder.setOutputFile(getFDOut());
			recorder.prepare();
		} catch (Exception e) {
			Debug.write(e);
		}
	}

	private void record() {
		state = State.Recording;
		button.setText("Stop");

		startTime = System.currentTimeMillis();
		autoUpdate(new Runnable() {
			@Override
			public void run() {
				int l = (int)(System.currentTimeMillis() 
						- startTime);
				setLength(l);
			}
		}, State.Recording);
		recorder.start();
	}

	private void stop() {
		if (state == State.Recording)
			recorder.stop();
		
		state = State.Paused;
		button.setText("Play");
		seekBar.setEnabled(true);
		
		player.reset();
		try {
			player.setDataSource(getFDIn());
			player.prepare();
		} catch (Exception e) {
			Debug.write(e);
		}

		setSeek(0);
		setLength(player.getDuration());
	}

	private void pause() {
		state = State.Paused;
		button.setText("Play");
		
		player.pause();
	}

	private void play() {
		state = State.Playing;
		button.setText("Pause");
		
		player.start();
		autoUpdate(new Runnable() {
			
			@Override
			public void run() {
				setSeek(player.getCurrentPosition());
			}
		}, State.Playing);
		player.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				setSeek(seekBar.getMax());
				pause();
			}
		});
	}

	private void autoUpdate(final Runnable todo, final State condition) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (state == condition) {
					Recorder.this.post(todo);
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						Debug.write(e);
					}
				}
			}
		});
		t.start();
	}
}
