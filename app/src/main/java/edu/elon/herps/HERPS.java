package edu.elon.herps;

/**
 *  The main activity.
 * 
 *  @author J. Hollingsworth
 */

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class HERPS extends Activity implements LocationListener {

	public static final String VERSION = "v1.5";
		
	public final static String WEB_ROOT = "http://nc-herps.appspot.com/";
	//public final static String WEB_ROOT = "http://127.0.0.1:8888/";
	
	public final static String TIME = WEB_ROOT + "time";
	public final static String UPDATE = WEB_ROOT + "HERPS.apk";

	private final static long TIME_THRESH = 5 * 60 * 1000;
	private Handler handler = new Handler();
	
	private LocationManager lm;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choice);
        
        Button aquaticTurtles = (Button) findViewById(R.id.aquaticturtle);
        Button boxTurtles = (Button) findViewById(R.id.boxturtle);
        Button fieldData = (Button) findViewById(R.id.fielddata);
        Button frogCall = (Button) findViewById(R.id.frogcall);
        Button aquaticHabitat = (Button) findViewById(R.id.aquaticHabitat);
        Button upload = (Button) findViewById(R.id.snake);
        Button lizard = (Button) findViewById(R.id.lizard);
        
        TextView version = (TextView) findViewById(R.id.textViewVersion);
        version.setText(VERSION);
        
        aquaticTurtles.setOnClickListener(aquaticTurtleListener);
        boxTurtles.setOnClickListener(boxTurtleListener);
        fieldData.setOnClickListener(fieldDataListener);
        frogCall.setOnClickListener(frogCallListener);
        aquaticHabitat.setOnClickListener(acquaticHabitatListener);
        upload.setOnClickListener(snakeListener);
        lizard.setOnClickListener(lizardListener);
        
        Button about = (Button) findViewById(R.id.aboutbutton);
        Button collecting = (Button) findViewById(R.id.collectingbutton);
        
        about.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				startActivity(new Intent(HERPS.this, About.class));
			}
        });
        
        collecting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(HERPS.this, CollectingData.class));
			}
        });
        
        Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				checkTime();
			}
		});
        t.start();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	// warming up the GPS
    	lm = (LocationManager)getSystemService(LOCATION_SERVICE);
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0, this);
    	
		for (String file : fileList()) {
			if (file.endsWith(".3gp")) {
				Debug.write("Cleaned %s", file);
				deleteFile(file);
			}
		}
    }
    
    @Override
	protected void onPause() {
    	super.onPause();
    	
    	// close down GPS -- saves the battery
    	lm.removeUpdates(this);
    }
    
    private void checkTime() {
    	SharedPreferences sps = getPreferences(MODE_PRIVATE);
    	if (!sps.getBoolean("time", true)) {
    		return;
    	}
    	
    	HttpClient client = new DefaultHttpClient();
    	HttpGet get = new HttpGet(TIME);
    	try {
    		HttpResponse resp = client.execute(get);
    		String respS = EntityUtils.toString(resp.getEntity());
    		//Debug.write(respS);
    		String[] data = respS.split("\n");
    		String version = data[1];
    		if (!version.equals(VERSION)) {
    			handler.post(new Runnable() {
					@Override
					public void run() {
		    			new AlertDialog.Builder(HERPS.this)
		    			.setTitle("Out of Date")
		    			.setMessage("HERPs is out of date. Update? This will bring you to a " +
		    					"download webpage. Install the download to update HERPs.")
		    			.setPositiveButton("Yes", new AlertDialog.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Uri uri = Uri.parse(UPDATE);
							    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uri);
							    startActivity(launchBrowser);
							}
						})
						.setNegativeButton("No", null)
		    			.show();
					}
				});
    			return;
    		}
    		
    		long time = Long.parseLong(data[0]);
    		if (Math.abs(time - System.currentTimeMillis()) > TIME_THRESH) {
    			Debug.write("correcting");
    			handler.post(new Runnable() {
					@Override
					public void run() {
						new AlertDialog.Builder(HERPS.this)
						.setTitle("Set Time")
						.setMessage("It appears your device's date/time is incorrect. " +
								"Would you like to set it now?")
						.setPositiveButton("Yes", new AlertDialog.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								startActivity(new Intent(
										android.provider.Settings.ACTION_DATE_SETTINGS));
							}
						})
						.setNeutralButton("No", null)
						.setNegativeButton("Ignore", new AlertDialog.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								SharedPreferences sps = getPreferences(MODE_PRIVATE);
								sps.edit().putBoolean("time", false).commit();
							}
						})
						.show();
					}
				});
    		}
    	} catch (Exception e) {
    		Debug.write(e);
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add("Upload").setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				upload();
				return true;
			}
		});
    	return true;
    }
    
    private OnClickListener aquaticTurtleListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			startActivity(new Intent(HERPS.this, AquaticTurtle.class));
		}
    };
    
    private OnClickListener boxTurtleListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			startActivity(new Intent(HERPS.this, BoxTurtle.class));
		}
    };
    
    private OnClickListener fieldDataListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			System.out.println("FIELD DATA!");
			fieldData();
		}
    };
    
    private OnClickListener frogCallListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			startActivity(new Intent(HERPS.this, FrogCall.class));
		}
    };
    
    private OnClickListener acquaticHabitatListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			acquaticHabitat();
		}
    };
    
    private OnClickListener snakeListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			startActivity(new Intent(HERPS.this, Snake.class));
		}
    };
    
    private OnClickListener lizardListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			startActivity(new Intent(HERPS.this, Lizard.class));
		}
    };
    
    private void upload() {
    	String running = null;
    	if (runningFieldData()) {
    		running = "Field Data";
    	} else if (runningEphemeralPool()) {
    		running = "Ephemeral Pool";
    	}
    	
    	if (running == null) {
    		startActivity(new Intent(HERPS.this, Upload.class));
    	} else {
    		new AlertDialog.Builder(this)
    		.setTitle("Cannot Upload")
    		.setMessage("You are currently in a \"" + running + "\" run. " +
    				"Please end the run to upload data.")
    		.setPositiveButton("Ok", null)
    		.show();
    	}
    }
    
    private boolean runningFieldData() {
    	SharedPreferences sps = getPreferences(MODE_PRIVATE);
    	return sps.getBoolean("inRunF", false);
    }
    
    private boolean runningEphemeralPool() {
    	SharedPreferences sps = getPreferences(MODE_PRIVATE);
    	return sps.getBoolean("inRunE", false);
    }
    
    private void fieldData() {
    	runForm("F", FieldData.class);
    }
    
    private void acquaticHabitat() {
    	runForm("E", AquaticHabitat.class);
    }
    
    private void runForm(final String suffix, 
    		final Class<? extends RunForm> launch) {
    	View view = getLayoutInflater().inflate(R.layout.run, null);
    	Button buttonStart = (Button)view.findViewById(R.id.buttonStart);
    	Button buttonAdd = (Button)view.findViewById(R.id.buttonAdd);
    	Button buttonEnd = (Button)view.findViewById(R.id.buttonEnd);
    	
    	SharedPreferences sps = getPreferences(MODE_PRIVATE);
    	boolean inRun = sps.getBoolean("inRun" + suffix, false);
    	
    	buttonStart.setEnabled(!inRun);
    	buttonAdd.setEnabled(inRun);
    	buttonEnd.setEnabled(inRun);
    	
    	
    	final AlertDialog alert = new AlertDialog.Builder(this)
    	.setView(view)
    	.setNeutralButton("Cancel", null)
    	.create();
    	
    	buttonStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				alert.cancel();
				Intent intent = new Intent(HERPS.this, launch);
				intent.putExtra("run", RunForm.RUN_START);
				intent.putExtra("suffix", suffix);
				startActivityForResult(intent, RunForm.DO_RUN);
			}
		});
    	buttonAdd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				alert.cancel();
				Intent intent = new Intent(HERPS.this, launch);
				intent.putExtra("run", RunForm.RUN_DATA);
				startActivity(intent);
			}
		});
    	buttonEnd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				alert.cancel();
				Intent intent = new Intent(HERPS.this, launch);
				intent.putExtra("run", RunForm.RUN_END);
				intent.putExtra("suffix", suffix);
				startActivityForResult(intent, RunForm.DO_RUN);
			}
		});
    	
    	alert.show();
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, 
    		Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if (requestCode == RunForm.DO_RUN && resultCode == RESULT_OK) {
    		int run = data.getExtras().getInt("run");
    		String suffix = data.getExtras().getString("suffix");
    		String category = data.getExtras().getString("category");
			SharedPreferences sps = getPreferences(MODE_PRIVATE);
    		if (run == FieldData.RUN_START) {
        		String file = data.getExtras().getString("file");
    			sps.edit().putBoolean("inRun" + suffix, true).commit();
    			sps.edit().putString("runStart" + suffix, file).commit();
    		} else if (run == FieldData.RUN_END) {
    			try {
    				String[] files = fileList();
    				
    				String startFile = sps.getString("runStart" + suffix, null);
    				ObjectInputStream ois = 
    					new ObjectInputStream(openFileInput(startFile));
					UploadData start = (UploadData)ois.readObject();
					Date startDate = (Date)start.get(1).value;
    				ois.close();
    				
    				String endFile = data.getExtras().getString("file");
					ois = new ObjectInputStream(openFileInput(endFile));
					UploadData end = (UploadData)ois.readObject();
					Date endDate = (Date)end.get(1).value;
					ois.close();
					end.remove(0); end.remove(0);
					
    				for (String file : files) {
    					ois = new ObjectInputStream(openFileInput(file));
    					UploadData upload = (UploadData)ois.readObject();
    					ois.close();

    					if (upload.get(0).value.equals(category)) {
        					if (file.equals(startFile) || file.equals(endFile)) {
        						deleteFile(file);
        						continue;
        					}
        					
        					Date date = (Date)upload.get(1).value;
        					if (date.before(endDate) && date.after(startDate)) {
        						upload.remove(0); upload.remove(0);
        						upload.addAll(0, start);
        						upload.addAll(end);
        						ObjectOutputStream oos = new ObjectOutputStream(
        								openFileOutput(file, MODE_PRIVATE));
        						oos.writeObject(upload);
        					}
    					}
    				}
    				
    				sps.edit().putBoolean("inRun" + suffix, false).commit();
    				sps.edit().putString("runStart" + suffix, null).commit();
    			} catch (Exception e) {
    				Debug.write(e);
    			}
    		}
    	}
    }

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}
}
