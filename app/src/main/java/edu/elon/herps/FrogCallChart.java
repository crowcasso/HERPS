package edu.elon.herps;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

import android.app.Activity;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.elon.herps.FormBase.InputAutoComplete;
import edu.elon.herps.FormBase.InputLabel;
import edu.elon.herps.FormBase.InputSelect;
import edu.elon.herps.FormBase.InputTemperature;
import edu.elon.herps.FormBase.InputTimeSelect;
import edu.elon.herps.FormBase.InputView;
import edu.elon.herps.Parameters.Iterator;

public class FrogCallChart extends LinearLayout implements InputView {

	private LinkedList<Parameters> startTimes = new LinkedList<Parameters>();
	private LinkedList<Parameters> moonLights = new LinkedList<Parameters>();
	private LinkedList<Parameters> temps = new LinkedList<Parameters>();
	private LinkedList<Parameters> gps = new LinkedList<Parameters>();
	private LinkedList<String> recordings = new LinkedList<String>();
	private LinkedList<LinkedList<Parameters>> indexes = 
		new LinkedList<LinkedList<Parameters>>();
	private LinkedList<String> frogNames;

	private int stop = -1;

	private InputTimeSelect inputTime;
	private Recorder recorder;
	private InputSelect inputMoon;
	private InputTemperature inputTemp;
	private InputGPS inputGPS;
	private LinearLayout speciesLayout;

	private Button prev;
	private TextView title;
	
	private int getCount() {
		return startTimes.size();
	}

	public FrogCallChart(Activity context) {
		super(context);
		setOrientation(VERTICAL);

		frogNames = getFrogNames();
		
		LinearLayout nav = new LinearLayout(context);
		nav.setOrientation(HORIZONTAL);

		prev = new Button(context);
		prev.setText("Previous");
		prev.setEnabled(false);
		prev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				previousStop();
			}
		});
		LayoutParams lps = new LayoutParams(125, LayoutParams.WRAP_CONTENT);
		lps.gravity = Gravity.LEFT;
		nav.addView(prev, lps);

		title = new TextView(context);
		title.setTextSize(24);
		title.setGravity(Gravity.CENTER);
		lps = new LayoutParams(LayoutParams.FILL_PARENT, 
				LayoutParams.FILL_PARENT);
		lps.weight = 1;
		nav.addView(title, lps);

		Button next = new Button(context);
		next.setText("Next");
		next.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				nextStop();
			}
		});
		lps = new LayoutParams(125, LayoutParams.WRAP_CONTENT);
		lps.gravity = Gravity.RIGHT;
		nav.addView(next, lps);

		addView(nav);

		inputTime = new InputTimeSelect(context);
		addField(this, "Start Time:", inputTime);
		
		InputLabel rLabel = new InputLabel(context, "Record (optional):");
		addView(rLabel);
		recorder = new Recorder(context);
		addView(recorder);
		
		inputGPS = new InputGPS(context);
		addField(this, "Location:", inputGPS);

		TextView tv = new TextView(getContext());
		tv.setText("Moon or moonlight visible?");
		tv.setTextSize(FormBase.LABEL_SIZE);
		inputMoon = new InputSelect(context, FormBase.getYesString());
		addField(this, tv, inputMoon);

		inputTemp = new InputTemperature(context, -10, 40);
		TextView tempLabel = new TextView(getContext());
		tempLabel.setText("Air Temperature:");
		tempLabel.setTextSize(FormBase.LABEL_SIZE);
		addView(tempLabel);
		addView(inputTemp);

		TextView species = new TextView(getContext());
		species.setText("Species Heard:");
		species.setTextSize(FormBase.TITLE_SIZE);
		addView(species);

		speciesLayout = new LinearLayout(context);
		speciesLayout.setOrientation(VERTICAL);
		addView(speciesLayout);

		Button addSpecies = new Button(context);
		addSpecies.setText("Add Species");
		addSpecies.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addNewSpecies("");
			}
		});
		addView(addSpecies);
		
		TextView name = new TextView(getContext());
		name.setText(Html.fromHtml("<b>Species Name</b>"));
		name.setTextSize(FormBase.LABEL_SIZE);
		TextView count = new TextView(getContext());
		count.setText(Html.fromHtml("<b>Calling Index</b>"));
		count.setTextSize(FormBase.LABEL_SIZE);
		count.setGravity(Gravity.RIGHT);
		addField(speciesLayout, name, count);
		post(new Runnable() {
			@Override
			public void run() {
				if (speciesLayout.getChildCount() == 1)
					addNewSpecies("");
			}
		});

		nextStop();
	}

	private void nextStop() {
		save();
		stop++;
		if (stop >= getCount()) {
			inputTime.setDate(new Date());
			inputGPS.clearLocation();
			recorder.load(null);
			for (int i = 1; i < speciesLayout.getChildCount(); i++) {
				InputSelect is = (InputSelect)((LinearLayout)speciesLayout
						.getChildAt(i)).getChildAt(1);
				is.setSelection(0);
			}
			startTimes.add(null);
			gps.add(null);
			temps.add(null);
			moonLights.add(null);
			indexes.add(null);
			recordings.add(null);
			prev.setEnabled(stop > 0);
			title.setText("Stop " + (stop + 1));
		} else {
			loadStop();
		}
	}

	private void previousStop() {
		save();
		stop--;
		loadStop();
	}

	private void loadStop() {
		prev.setEnabled(stop > 0);
		title.setText("Stop " + (stop + 1));

		InputView[] ivs = new InputView[] {
				inputTime, inputGPS, inputTemp, inputMoon
		};
		LinkedList<?>[] lists = new LinkedList<?>[] {
				startTimes, gps, temps, moonLights
		};

		for (int i = 0; i < ivs.length; i++) {
			Parameters params =	(Parameters)lists[i].get(stop);
			ivs[i].readParameters(params);
		}
		
		recorder.load(recordings.get(stop));

		LinkedList<Parameters> specs = indexes.get(stop);
		for (int i = 1; i < speciesLayout.getChildCount(); i++) {
			InputSelect is = (InputSelect)((LinearLayout)speciesLayout
					.getChildAt(i)).getChildAt(1);
			if (i - 1 < specs.size()) {
				Parameters params = specs.get(i - 1);
				is.readParameters(params);
			} else {
				is.setSelection(0);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void save() {
		if (stop >= 0) {
			InputView[] ivs = new InputView[] {
					inputTime, inputGPS, inputTemp, inputMoon
			};
			LinkedList<?>[] lists = new LinkedList<?>[] {
					startTimes, gps, temps, moonLights
			};

			for (int i = 0; i < ivs.length; i++) {
				Parameters params = new Parameters();
				ivs[i].writeParameters(params);
				((LinkedList<Parameters>)lists[i]).set(stop, params);
			}

			recordings.set(stop, recorder.save());
			
			LinkedList<Parameters> specs = new LinkedList<Parameters>();
			for (int i = 1; i < speciesLayout.getChildCount(); i++) {
				InputSelect is = (InputSelect)((LinearLayout)speciesLayout
						.getChildAt(i)).getChildAt(1);
				Parameters params = new Parameters();
				is.writeParameters(params);
				specs.add(params);
			}
			indexes.set(stop, specs);
		}
	}

	private void addNewSpecies(String species) {
		final InputAutoComplete edit = 
			new InputAutoComplete(getContext(), frogNames);
		edit.setText(species);
		edit.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					edit.setSelection(0);
				}
			}
		});
		edit.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				edit.post(new Runnable() {
					@Override
					public void run() {
						edit.setSelection(0);
					}
				});
			}
		});
		
		InputSelect num = new InputSelect(getContext(), 
				FormBase.getCallingStrings(), 0, false);
		LinearLayout ll = new LinearLayout(getContext());
		ll.setOrientation(LinearLayout.HORIZONTAL);
		LayoutParams lps = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		lps.setMargins(5, 5, 10, 5);
		lps.weight = 1;
		ll.addView(edit, lps);
		lps = new LayoutParams(
				115, LayoutParams.MATCH_PARENT);
		ll.addView(num, lps);
		speciesLayout.addView(ll);
		//addField(speciesLayout, edit, num);
	}

	public LinkedList<String> getFrogNames() {
		LinkedList<String> names = new LinkedList<String>();
		try {
			Scanner sc = new Scanner(
					getContext().getAssets().open("frogs.txt"));
			while (sc.hasNextLine()) {
				names.add(sc.nextLine());
			}
		} catch (Exception e) {
			Debug.write(e);
		}
		return names;		
	}

	private void addField(LinearLayout parent, String prompt, View view) {
		TextView tv = new TextView(getContext());
		tv.setText(prompt);
		tv.setTextSize(FormBase.LABEL_SIZE);
		tv.setIncludeFontPadding(false);
		addField(parent, tv, view);
	}

	private void addField(LinearLayout parent, View left, View right) {
		LinearLayout row = new LinearLayout(getContext());
		row.setOrientation(HORIZONTAL);
		row.setBaselineAligned(false);

		LayoutParams lps = new LayoutParams(LayoutParams.MATCH_PARENT, 
				LayoutParams.WRAP_CONTENT);
		lps.weight = 6;
		lps.setMargins(0, 0, 10, 0);
		lps.gravity = Gravity.CENTER_VERTICAL;
		row.addView(left, lps);

		lps = new LayoutParams(LayoutParams.MATCH_PARENT, 
				LayoutParams.WRAP_CONTENT);
		lps.gravity = Gravity.CENTER_VERTICAL;
		lps.weight = 4;
		row.addView(right, lps);

		lps = new LayoutParams(LayoutParams.MATCH_PARENT, 
				LayoutParams.WRAP_CONTENT);
		row.setPadding(0, 5, 0, 5);
		parent.addView(row, lps);
	}
	
	private String getSaveString() {
		StringBuilder sb = new StringBuilder();
		
		LinkedList<Integer> ns = new LinkedList<Integer>();
		LinkedList<String> species = new LinkedList<String>();
		for (int i = 1; i < speciesLayout.getChildCount(); i++) {
			InputAutoComplete line = (InputAutoComplete)((LinearLayout)speciesLayout
					.getChildAt(i)).getChildAt(0);
			String text = "[" + line.getText() + "]";
			species.add(text);
			ns.add(text.length() + 1);
		}
		
		int size = 9;
		
		int gpsSize = size;
		for (int i = 0; i < getCount(); i++) {
			gpsSize = Math.max(
					gps.get(i).getObject().toString()
					.replace("\n", " ").length() + 1,
					gpsSize);
		}
		
		add(sb, "", size);
		add(sb, "Time", size);
		add(sb, "Location", gpsSize);
		add(sb, "Moon", size);
		add(sb, "Temp", size);
		for (int i = 0; i < species.size(); i++) {
			add(sb, species.get(i), ns.get(i));
		}
		sb.append("\n");
		
		for (int i = 0; i < getCount(); i++) {
			add(sb, "Stop " + (i + 1), size);
			add(sb, startTimes.get(i).getObject().toString(), size);
			add(sb, gps.get(i).getObject().toString().replace("\n", " "), gpsSize);
			add(sb, moonLights.get(i).getObject().toString(), size);
			add(sb, temps.get(i).getObject().toString(), size);

			LinkedList<Parameters> params = indexes.get(i);
			for (int j = 0; j < params.size(); j++) {
				add(sb, params.get(j).getObject(0).toString(), ns.get(j));
			}
			
			
			sb.append("\n");
		}
		
		Debug.write(sb.toString());
		return sb.toString();
	}
	
	private void add(StringBuilder sb, String s, int n) {
		sb.append(String.format("%-" + n + "s", s));  
	}
	
	@Override
	public void writeParameters(Parameters params) {
		save();
		
		params.addParam(getSaveString());
		params.addParam(startTimes);
		params.addParam(gps);
		params.addParam(temps);
		params.addParam(moonLights);
		params.addParam(indexes);
		params.addParam(stop);
		
		LinkedList<String> species = new LinkedList<String>();
		for (int i = 1; i < speciesLayout.getChildCount(); i++) {
			InputAutoComplete line = (InputAutoComplete)((LinearLayout)speciesLayout
					.getChildAt(i)).getChildAt(0);
			species.add(line.getText().toString());
		}
		params.addParam(species);
		
		params.addParam(recordings);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readParameters(Parameters params) {
		Iterator iter = params.iterator(); iter.next();
		startTimes = (LinkedList<Parameters>)iter.getObject();
		gps = (LinkedList<Parameters>)iter.getObject();
		temps = (LinkedList<Parameters>)iter.getObject();
		moonLights = (LinkedList<Parameters>)iter.getObject();
		indexes = (LinkedList<LinkedList<Parameters>>)iter.getObject();
		stop = iter.getInt();
		
		LinkedList<String> species = (LinkedList<String>)
			iter.getObject();
		
		for (String s : species) {
			addNewSpecies(s);
		}
		
		recordings = (LinkedList<String>)iter.getObject();
		
		loadStop();
	}

	@Override
	public boolean isSet() {
		return getCount() > 1;
	}
}
