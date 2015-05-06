package edu.elon.herps;

import java.util.Date;
import java.util.LinkedList;
import java.util.Scanner;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import edu.elon.herps.FormBase.InputAutoComplete;
import edu.elon.herps.FormBase.InputBoundedDecimal;
import edu.elon.herps.FormBase.InputScientificName;
import edu.elon.herps.FormBase.InputSelect;
import edu.elon.herps.FormBase.InputView;
import edu.elon.herps.FormBase.InputWeight;
import edu.elon.herps.Parameters.Iterator;

public class LizardEntry extends LinearLayout implements InputView {
	
	private LinkedList<Parameters> gps = new LinkedList<Parameters>();
	private LinkedList<Parameters> species = new LinkedList<Parameters>();
	private LinkedList<Parameters> method = new LinkedList<Parameters>();
	private LinkedList<Parameters> sex = new LinkedList<Parameters>();
	private LinkedList<Parameters> mass = new LinkedList<Parameters>();
	private LinkedList<Parameters> svl = new LinkedList<Parameters>();
	private LinkedList<Parameters> tailLength = new LinkedList<Parameters>();
	private LinkedList<String> lizardNames;
	
	private InputGPS inputGPS;
	private LinearLayout speciesLayout;
	private InputSelect inputMethod;
	private InputSelect inputSex;
	private InputBoundedDecimal inputMass;
	private InputBoundedDecimal inputSVL;
	private InputBoundedDecimal inputTail;
	private InputAutoComplete inputSpecies;
	
	private Button prev;
	private TextView title;

	private int stop = -1;
	
	public LizardEntry(Activity context) {
		super(context);
		setOrientation(VERTICAL);
		
		lizardNames = getLizardNames();
		
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
		
		inputGPS = new InputGPS(context);
		addField(this, "Location:", inputGPS);

		TextView tv = new TextView(getContext());
		tv.setText("Species");
		tv.setTextSize(FormBase.LABEL_SIZE);
		inputSpecies = new InputAutoComplete(getContext(), lizardNames);
		addField(this, tv, inputSpecies);
		
		tv = new TextView(getContext());
		tv.setText("Capture method");
		tv.setTextSize(FormBase.LABEL_SIZE);
		String[] options = {"Hand", "Lasso", "Other"};
		inputMethod = new InputSelect(context, options, true);
		addField(this, tv, inputMethod);
		
		tv = new TextView(getContext());
		tv.setText("Sex");
		tv.setTextSize(FormBase.LABEL_SIZE);
		options = new String[]{"Male", "Female", "Unable to determine"};
		inputSex = new InputSelect(context, options, true);
		addField(this, tv, inputSex);
		
		tv = new TextView(getContext());
		tv.setText("Mass (g)");
		tv.setTextSize(FormBase.LABEL_SIZE);
		inputMass = new InputBoundedDecimal(context, 1, 5000);
		addField(this, tv, inputMass);
		
		tv = new TextView(getContext());
		tv.setText("SVL (mm)");
		tv.setTextSize(FormBase.LABEL_SIZE);
		inputSVL = new InputBoundedDecimal(context, 1, 1000);
		addField(this, tv, inputSVL);
		
		tv = new TextView(getContext());
		tv.setText("Tail Length (mm)");
		tv.setTextSize(FormBase.LABEL_SIZE);
		inputTail = new InputBoundedDecimal(context, 1, 1000);
		addField(this, tv, inputTail);
		
		nextStop();
	}
	
	private void addNewSpecies(String species) {
		final InputAutoComplete edit = 
			new InputAutoComplete(getContext(), lizardNames);
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
		
		LinearLayout ll = new LinearLayout(getContext());
		ll.setOrientation(LinearLayout.HORIZONTAL);
		LayoutParams lps = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		lps.setMargins(5, 5, 10, 5);
		lps.weight = 1;
		ll.addView(edit, lps);
		lps = new LayoutParams(
				115, LayoutParams.MATCH_PARENT);
		speciesLayout.addView(ll);
		//addField(speciesLayout, edit, num);
	}
	
	private void nextStop() {
		save();
		stop++;
		if (stop >= getCount()) {
			inputGPS.clearLocation();
			gps.add(null);
			species.add(null);
			method.add(null);
			sex.add(null);
			mass.add(null);
			svl.add(null);
			tailLength.add(null);
			prev.setEnabled(stop > 0);
			title.setText("Lizard " + (stop + 1));
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
		title.setText("Lizard " + (stop + 1));

		InputView[] ivs = new InputView[] {
				inputGPS, inputSpecies, inputMethod, inputSex, inputMass, inputSVL, inputTail
		};
		LinkedList<?>[] lists = new LinkedList<?>[] {
				gps, species, method, sex, mass, svl, tailLength
		};

		for (int i = 0; i < ivs.length; i++) {
			Parameters params =	(Parameters)lists[i].get(stop);
			ivs[i].readParameters(params);
		}
		
	}

	@SuppressWarnings("unchecked")
	private void save() {
		if (stop >= 0) {
			InputView[] ivs = new InputView[] {
					inputGPS, inputSpecies, inputMethod, inputSex, inputMass, inputSVL, inputTail
			};
			LinkedList<?>[] lists = new LinkedList<?>[] {
					gps, species, method, sex, mass, svl, tailLength
			};

			for (int i = 0; i < ivs.length; i++) {
				Parameters params = new Parameters();
				ivs[i].writeParameters(params);
				((LinkedList<Parameters>)lists[i]).set(stop, params);
			}
			
		}
	}
	
	public LinkedList<String> getLizardNames() {
		LinkedList<String> names = new LinkedList<String>();
		try {
			Scanner sc = new Scanner(
					getContext().getAssets().open("lizards.txt"));
			while (sc.hasNextLine()) {
				names.add(sc.nextLine());
			}
		} catch (Exception e) {
			Debug.write(e);
		}
		return names;		
	}

	@Override
	public void writeParameters(Parameters params) {
		save();
		
		params.addParam(gps);
		params.addParam(species);
		params.addParam(stop);
		params.addParam(method);
		params.addParam(sex);
		params.addParam(mass);
		params.addParam(svl);
		params.addParam(tailLength);
	}

	@Override
	public void readParameters(Parameters params) {
		Iterator iter = params.iterator(); iter.next();
		gps = (LinkedList<Parameters>)iter.getObject();
		species = (LinkedList<Parameters>)iter.getObject();
		stop = iter.getInt();
		method = (LinkedList<Parameters>)iter.getObject();
		sex = (LinkedList<Parameters>)iter.getObject();
		mass = (LinkedList<Parameters>)iter.getObject();
		svl = (LinkedList<Parameters>)iter.getObject();
		tailLength = (LinkedList<Parameters>)iter.getObject();
		
		loadStop();
	}

	@Override
	public boolean isSet() {
		return getCount() > 1;
	}
	
	private int getCount() {
		return gps.size();
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

}
