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

public class InputGPSUtm extends Button 
implements InputView, Serializable {
	private static final long serialVersionUID = 1L;

	private Coord coord;

	public InputGPSUtm(Activity context) {
		super(context);

		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				View view = ((Activity)getContext()).
				getLayoutInflater().inflate(R.layout.gps_utm, null);

				final EditText
				editEasting = (EditText)view.findViewById(R.id.editTextEasting),
				editNorthing = (EditText)view.findViewById(R.id.editTextNorthing),
				editZone = (EditText)view.findViewById(R.id.editTextZone);

				final EditText[] edits = new EditText[] {
						editEasting, editNorthing, editZone
				};

				InputFilter[] filters = new InputFilter[] {
						new InputFilter.LengthFilter(2)
				};
				editZone.setFilters(filters);

				final Button buttonGPS = (Button)view.findViewById(R.id.buttonGPS);
				final ProgressBar progress = (ProgressBar)view.findViewById(R.id.progressBarGPS);

				if (coord != null) {
					editEasting.setText(format(coord.easting));
					editNorthing.setText(format(coord.northing));
					editZone.setText("" + coord.zone);
				}

				final AlertDialog alert = new AlertDialog.Builder(getContext())
				.setTitle("Set Coordinates")
				.setView(view)
				.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						double easting = parse(editEasting.getText().toString());
						double northing = parse(editNorthing.getText().toString());
						int zone = Integer.parseInt(editZone.getText().toString());
						coord = new Coord(easting, northing, zone);
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
						okButton.setEnabled(false);
						buttonGPS.setEnabled(false);
						progress.setVisibility(VISIBLE);

						final LocationManager lm = (LocationManager)getContext()
						.getSystemService(Activity.LOCATION_SERVICE);
						lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
								new LocationListener() {
							@Override
							public void onLocationChanged(Location location) {
								coord = new Coord(location.getLatitude(), 
										location.getLongitude());
								updateString();
								lm.removeUpdates(this);
								alert.cancel();
							}

							@Override
							public void onProviderDisabled(String provider) { }

							@Override
							public void onProviderEnabled(String provider) { }

							@Override
							public void onStatusChanged(String provider,
									int status, Bundle extras) { }
						});

						Thread t = new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									Thread.sleep(15000);
									if (alert.isShowing()) {
										InputGPSUtm.this.post(new Runnable() {

											@Override
											public void run() {

												for (EditText ed: edits) {
													ed.setEnabled(true);
												}
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

	private String format(double d) {
		return String.format("%.02f", d);
	}
	
	private double parse(String s) {
		s = s.replace("-.", "-0.");
		try {
			return Double.parseDouble(s);
		} catch (Exception e) {
			Debug.write(e);
		}
		return 0;
	}

	private void updateString() {
		if (coord == null) {
			setText("");
			return;
		}
		setText(coord.toString());
	}

	private class Coord implements Serializable {
		private static final long serialVersionUID = 1L;

		public double easting, northing;
		public int zone;
		
		public Coord(double lat, double lon) {
			UTMRef utm = new LatLng(lat, lon).toUTMRef();
			easting = utm.getEasting();
			northing = utm.getNorthing();
			zone = utm.getLngZone();
		}
		
		public Coord(double easting, double northing, int zone) {
			this.easting = easting;
			this.northing = northing;
			this.zone = zone;
		}
		
		@Override
		public String toString() {
			return String.format(
					"Easting: %07.02f\nNorthing: %07.02f\nZone: %d",
					easting, northing, zone);
		}
	}


	@Override
	public void writeParameters(Parameters params) {
		params.addParam(getText());
		params.addParam(coord);
	}

	@Override
	public void readParameters(Parameters params) {
		setText(params.getString());
		coord = (Coord)params.getObject(1);

	}

	@Override
	public boolean isSet() {
		return getText().length() > 0;
	}

	public void clearLocation() {
		coord = null;
		updateString();
	}
}

