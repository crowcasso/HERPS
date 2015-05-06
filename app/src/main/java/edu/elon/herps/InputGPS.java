package edu.elon.herps;

import java.io.Serializable;

import uk.me.jstott.jcoord.LatLng;
import uk.me.jstott.jcoord.UTMRef;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import edu.elon.herps.FormBase.InputView;

public class InputGPS extends Button 
implements InputView, Serializable {
	private static final long serialVersionUID = 1L;

	private Coord latitude, longitude;

	public InputGPS(Activity context) {
		super(context);

		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				View view = ((Activity)getContext()).
				getLayoutInflater().inflate(R.layout.gps, null);

				final EditText
				editDegNS = (EditText)view.findViewById(R.id.editTextDegreeNS),
				editMinNS = (EditText)view.findViewById(R.id.editTextMinuteNS),
				editSecNS = (EditText)view.findViewById(R.id.editTextSecondNS),
				editDegEW = (EditText)view.findViewById(R.id.editTextDegreeEW),
				editMinEW = (EditText)view.findViewById(R.id.editTextMinuteEW),
				editSecEW = (EditText)view.findViewById(R.id.editTextSecondEW);

				final EditText[] edits = new EditText[] {
						editDegNS, editMinNS, editSecNS,
						editDegEW, editMinEW, editSecEW
				};

				for (int i = 0; i < edits.length; i++) {
					InputFilter[] filters = new InputFilter[] {
							new InputFilter.LengthFilter(i % 3  == 0 ? 3 : 2)
					};
					edits[i].setFilters(filters);
				}

				final Spinner
				spinnerNS = (Spinner)view.findViewById(R.id.spinnerNS),
				spinnerEW = (Spinner)view.findViewById(R.id.spinnerEW);

				final Button buttonGPS = (Button)view.findViewById(R.id.buttonGPS);
				final ProgressBar progress = (ProgressBar)view.findViewById(R.id.progressBarGPS);

				if (latitude != null) {
					editDegNS.setText("" + latitude.degree);
					editMinNS.setText("" + latitude.minutes);
					editSecNS.setText("" + latitude.seconds);
					editDegEW.setText("" + longitude.degree);
					editMinEW.setText("" + longitude.minutes);
					editSecEW.setText("" + longitude.seconds);

					spinnerNS.setSelection(latitude.positive ? 0 : 1);
					spinnerEW.setSelection(longitude.positive ? 0 : 1);
				}

				final AlertDialog alert = new AlertDialog.Builder(getContext())
				.setTitle("Set Coordinates")
				.setView(view)
				.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						int[] vs = new int[6];
						for (int i = 0; i < 6; i++) {
							vs[i] = Integer.parseInt(edits[i].getText().toString());
						}
						boolean north = spinnerNS.getSelectedItemPosition() == 0;
						boolean east = spinnerEW.getSelectedItemPosition() == 0;
						latitude = new Coord(vs[0], vs[1], vs[2], north, true);
						longitude = new Coord(vs[3], vs[4], vs[5], east, false);
						updateString();
					}
				})
				.setNegativeButton("Cancel", null)
				.show();

				final Button okButton = 
					alert.getButton(AlertDialog.BUTTON_POSITIVE);

				final TextWatcher tw = new TextWatcher() {
					@Override
					public void onTextChanged(CharSequence s, int start, 
							int before, int count) { }

					@Override
					public void beforeTextChanged(CharSequence s, int start, int count,
							int after) { }

					@Override
					public void afterTextChanged(Editable s) {
						boolean ok = true;
						for (EditText et : edits) {
							if (et.getText().length() == 0) {
								ok = false;
								break;
							}
						}
						okButton.setEnabled(ok);
					}
				};
				for (EditText ed : edits) {
					ed.addTextChangedListener(tw);
				}
				tw.afterTextChanged(null);

				buttonGPS.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						for (EditText ed: edits) {
							ed.setEnabled(false);
						}
						spinnerNS.setEnabled(false);
						spinnerEW.setEnabled(false);
						okButton.setEnabled(false);
						buttonGPS.setEnabled(false);
						progress.setVisibility(VISIBLE);

						final LocationManager lm = (LocationManager)getContext()
						.getSystemService(Activity.LOCATION_SERVICE);
						lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
								new LocationListener() {
							@Override
							public void onLocationChanged(Location location) {
								latitude = new Coord(location.getLatitude(), true);
								longitude = new Coord(location.getLongitude(), false);
								updateString();
								lm.removeUpdates(this);
								alert.cancel();
							}

							@Override
							public void onProviderDisabled(String provider) { 
								
							}

							@Override
							public void onProviderEnabled(String provider) { 
								
							}

							@Override
							public void onStatusChanged(String provider,
									int status, Bundle extras) { 
								
							}
						});

						Thread t = new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									Thread.sleep(15000);
									if (alert.isShowing()) {
										InputGPS.this.post(new Runnable() {

											@Override
											public void run() {

												for (EditText ed: edits) {
													ed.setEnabled(true);
												}
												spinnerNS.setEnabled(true);
												spinnerEW.setEnabled(true);
												buttonGPS.setEnabled(true);
												tw.afterTextChanged(null);
												progress.setVisibility(INVISIBLE);

												new AlertDialog.Builder(getContext())
												.setTitle("GPS Failed")
												.setMessage("Could not get GPS coordinates. " +
												"Make sure the GPS is activated.")
												.setPositiveButton("Ok", null)
												.show();

											}
										});
									}
								} catch (Exception e) {
									Debug.write(e);
								}
							}
						});
						t.start();

					}
				});
			}
		});
	}


	private void updateString() {
		if (latitude == null) {
			setText("");
			return;
		}
		setText(latitude + "\n" + longitude);
	}

	private class Coord implements Serializable {
		private static final long serialVersionUID = 1L;

		public int degree, minutes, seconds;
		boolean vertical;
		boolean positive;

		public Coord(double coord, boolean vertical) {
			this.vertical = vertical;
			set(coord);
		}

		public Coord(int degree, int minutes, int seconds, 
				boolean positive, boolean vertical) {
			this.vertical = vertical;
			set(degree, minutes, seconds, positive);
		}

		public void set(int degree, int minutes, int seconds, boolean positive) {
			this.positive = positive;
			this.degree = degree;
			this.minutes = minutes;
			this.seconds = seconds;
		}

		public void set(double coord) {
			positive = coord >= 0;
			coord = Math.abs(coord);
			degree = (int)coord;
			coord = (coord - degree) * 60;
			minutes = (int)coord;
			coord = (coord - minutes) * 60;
			seconds = (int)(coord);
		}

		public String getCard() {
			if (vertical) {
				return positive ? "N" : "S";
			} else {
				return positive ? "E" : "W";
			}
		}

		public String toString() {
			return String.format("%d\u00b0%d'%d\" %s", degree, minutes,
					seconds, getCard());
		}
	}


	@Override
	public void writeParameters(Parameters params) {
		params.addParam(getText());
		params.addParam(latitude);
		params.addParam(longitude);
	}

	@Override
	public void readParameters(Parameters params) {
		setText(params.getString());
		latitude = (Coord)params.getObject(1);
		longitude = (Coord)params.getObject(2);

	}

	@Override
	public boolean isSet() {
		return getText().length() > 0;
	}

	public void clearLocation() {
		latitude = null;
		longitude = null;
		updateString();
	}
}

