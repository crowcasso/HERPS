/**
 * NumberSeekBar.java 1.0 Apr 12, 2012
 *
 * Copyright (c) 2009 Amanda J. Bienz
 * Campus Box 3531, Elon University, Elon, NC 27244
 */
package edu.elon.herps;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * Creates View with SeekBar and TextView displaying the seekbar's current
 * value.
 * 
 * @author abienz
 * 
 */
public class NumberSeekBar extends LinearLayout {

	protected int min;
	protected int max;
	protected final SeekBar sb;
	protected final TextView progressView;
	protected int scale = 10;
	private int buffer;

	public boolean hasValue() {
		return sb.getProgress() >= buffer;
	}
	
	public double getValue() {
		if (!hasValue()) throw new RuntimeException("No Value!");
		double val = min + getProgress() / (double)scale; 
		return (int)(val * scale + 0.5) / (double)scale;
	}

	public void setValue(double value) {
		if (value < min) value = min;
		setProgress((int)((value - min) * scale));
	}
	
	public void setValue(boolean hasValue) {
		if (hasValue) {
			setValue(Math.max(getValue(), min));
		} else {
			sb.setProgress(0);
		}
	}
	
	protected int getProgress() {
		return sb.getProgress() - buffer;
	}
	
	protected void setProgress(int progress) {
		sb.setProgress(progress + buffer);
	}
	
	protected int getMax() {
		return sb.getMax() - buffer;
	}
	
	protected void setMax(int max) {
		buffer = (int)(max * 0.08);
		sb.setMax(max + buffer);
	}

	/**
	 * @param aContext
	 */
	public NumberSeekBar(Context c, int aMin, int aMax) {
		super(c);

		min = aMin;
		max = aMax;

		setOrientation(LinearLayout.VERTICAL);

		progressView = new FormBase.InputLabel(c, "Not Set");
		progressView.setGravity(Gravity.CENTER);

		sb = new SeekBar(c);
		int m = (max - min) * scale;
		setMax(m);
		sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar aSeekBar, int aProgress,
					boolean aFromUser) {
				int roundDown = buffer / 2;
				int roundUp = buffer;
				if (aProgress == 0) {
					progressView.setText("Not Set");
					return;
				} else if (aProgress < roundDown) {
					aSeekBar.setProgress(0);
					return;
				} else if (aProgress < roundUp) {
					aSeekBar.setProgress(roundUp);
					return;
				}

				String dec = String.format("%%.%02df", scale / 10);
				progressView.setText(String.format(dec, getValue()));

			}

			@Override
			public void onStartTrackingTouch(SeekBar aSeekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStopTrackingTouch(SeekBar aSeekBar) {
				// TODO Auto-generated method stub

			}

		});

		LinearLayout ll = new LinearLayout(c);
		addView(sb);
		addView(progressView);
		addView(ll);

	}

	public void setBounds(int aMin, int aMax) {
		double value = 0;
		if (hasValue()) {
			value = getValue();
		}
		min = aMin;
		max = aMax;
		setMax((max - min) * scale);
		if (hasValue()) {
			setValue(value);
		}
	}

	public static class ExpSeekBar extends NumberSeekBar {
		public ExpSeekBar(Context c, int aMin, int aMax) {
			super(c, aMin, aMax);
		}

		@Override
		public double getValue() {
			//if (getProgress() == getMax()) return max;
			double r = Math.log((double)max / min) / getMax();
			double val = min * Math.exp(getProgress() * r);
			return (int)(val * scale + 0.5) / (double)scale;
		}

		public void setValue(double value) {
			double r = Math.log((double)max / min) / getMax();
			int prog = (int)(Math.log(value / min) / r);
			setProgress(prog);
		}
	}
}
