/**
 * EntryForm.java 1.0 Feb 8, 2012
 *
 * Copyright (c) 2009 Amanda J. Bienz
 * Campus Box 3531, Elon University, Elon, NC 27244
 */
package edu.elon.herps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TabHost.TabSpec;

/**
 * Abstract class for creating a generic form
 * 
 * @author abienz
 * 
 */
public abstract class EntryForm extends FormBase {

	private List<Page> pages;
	private LinearLayout[] mainLayout;
	private TabHost host;
	private TabWidget tabWidget;
	private Drawable defaultTab;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Parameters params = savedInstanceState == null ? null :
			(Parameters)savedInstanceState.getSerializable("params");

		setContentView(createLayout(params));

		if (savedInstanceState != null) {
			host.setCurrentTab(savedInstanceState.getInt("tab"));
		}

		checkCompletion();
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this).setTitle("Leave?")
		.setMessage("Do you want to leave this form without submitting it?")
		.setPositiveButton("Yes", new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		}).setNegativeButton("No", null)
		.show();
	}

	protected abstract List<Page> getPages();

	/**
	 * Creates main layout of two buttons and a scroll view in between.
	 * 
	 * @return
	 */
	protected View createLayout(Parameters params) {
		pages = getPages();
		mainLayout = new LinearLayout[pages.size()];
		createMainView(params);

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);

		/*
		 *  MUST call this constructor -- bug in Android >=3.0
		 *  https://code.google.com/p/android/issues/detail?id=22605
		 */
		host = new TabHost(this, null); 

		host.setId(1);
		LinearLayout content = new LinearLayout(this);
		content.setOrientation(LinearLayout.VERTICAL);
		host.addView(content);

		tabWidget = new TabWidget(this);
		tabWidget.setId(android.R.id.tabs);
		content.addView(tabWidget);

		FrameLayout frames = new FrameLayout(this);
		frames.setId(android.R.id.tabcontent);
		content.addView(frames);

		//tabWidget.setLeftStripDrawable(R.drawable.black);
		//tabWidget.setRightStripDrawable(R.drawable.black);

		host.setup();

		for (int i = 0; i < mainLayout.length; i++) {
			TabSpec ts = host.newTabSpec(pages.get(i).title);
			ts.setIndicator(pages.get(i).title); 
			final int fi = i;

			ts.setContent(new TabHost.TabContentFactory() {
				@Override
				public View createTabContent(String tag) {
					ScrollView sv = new ScrollView(EntryForm.this);
					sv.setPadding(20, 10, 20, 10);
					sv.addView(mainLayout[fi]);
					return sv;
				}
			});

			host.addTab(ts);

			defaultTab = tabWidget.getChildAt(i).getBackground();
			checkCompletion(i);
		}

		LayoutParams lps = new LayoutParams(LayoutParams.FILL_PARENT, 
				LayoutParams.FILL_PARENT);
		lps.weight = 1;
		layout.addView(host, lps);

		final Button submit = new Button(this);
		submit.setText("Submit");
		submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				checkSubmit();
			}
		});
		submit.setVisibility(pages.size() == 1 ? View.VISIBLE : View.GONE);

		host.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				submit.setVisibility(
						host.getCurrentTab() == pages.size() - 1 ?
								View.VISIBLE : View.GONE);
				host.post(new Runnable() {
					@Override
					public void run() {
						checkCompletion();
					}
				});
			}
		});

		layout.addView(submit);

		return layout;
	}

	private void checkCompletion() {
		for (int i = 0; i < pages.size(); i++) {
			checkCompletion(i);
		}
	}

	private void checkCompletion(int page) {
		Page current = pages.get(page);
		TextView tv = (TextView)((RelativeLayout)tabWidget
				.getChildAt(page)).getChildAt(1);
		if (current.isComplete()) {
			//tabWidget.getChildAt(page).setBackgroundDrawable(
			//		defaultTab.getConstantState().newDrawable());

			String color = "#22AA22";
			tv.setText(Html.fromHtml(String.format(
					"<font color='%s'>%s</font>", color, current.title)));
		} else {
			//tabWidget.getChildAt(page).setBackgroundResource(
			//		R.drawable.incomplete_tab);

			tv.setText(current.title);
		}
	}

	protected abstract String getFormName();

	private void checkSubmit() {
		LinkedList<Page> errors = new LinkedList<EntryForm.Page>();
		for (Page page : pages) {
			if (!page.isComplete()) {
				errors.add(page);
			}
		}

		if (errors.size() == 0) {
			submit();
		} else {
			checkCompletion();
			String badPages = "";
			for (int i = 0; i < errors.size(); i++) {
				if (i != 0) badPages += ", ";
				badPages += errors.get(i).title;
			}

			new AlertDialog.Builder(this)
			.setTitle("Submit?")
			.setMessage("The following pages unfinished data: " + badPages
					+ ".\nDo you still want to submit?")
					.setPositiveButton("Submit", new AlertDialog.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							submit();
						}
					})
					.setNegativeButton("Cancel", null)
					.show();
		}
	}

	protected String getFileOut() {
		return System.currentTimeMillis() + ".data";
	}

	protected void submit() {
		submit(getFileOut());
	}

	protected final void submit(String fileOut) {
		UploadData data = new UploadData();
		data.add(new NameValuePair("Category", getFormName()));
		//		String time = String.format("%s %s", getDateString(),
		//				getTimeString());
		//data.add(new NameValuePair("Submit Time", time));
		data.add(new NameValuePair("Submit Time", new Date()));
		for (Page page : pages) {
			for (DataEntry entry : page.entries) {
				if (entry.view instanceof InputView) {
					Parameters ps = new Parameters();
					((InputView)entry.view).writeParameters(ps);
					if (ps.getSize() > 0) {
						Object value;
						if (entry.view instanceof InputPicture) {
							String file = ps.getString();
							if (file != null && new File(file).exists()) {
								value = new UploadData.Picture(ps.getString());
							} else {
								value = null;
							}
						} else {
							value = ps.getObject();
						}
						data.add(new NameValuePair(entry.title, value));
					}
				}
			}
		}

		String name = fileOut;
		try {
			FileOutputStream fos = openFileOutput(name, MODE_WORLD_READABLE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(data);
			oos.close();
			finish();
		} catch (Exception e) {
			Debug.write(e);
			Toast.makeText(this, "Failed to submit data.", Toast.LENGTH_SHORT).show();
		}

	}

	/**
	 * Creates the mainlayout array
	 * @param params 
	 */
	private void createMainView(Parameters params) {

		int paramIndex = 0;
		for (int i = 0; i < pages.size(); i++) {
			mainLayout[i] = new LinearLayout(this);
			mainLayout[i].setOrientation(LinearLayout.VERTICAL);
			Page page = pages.get(i);
			for (int j = 0; j < page.entries.size(); j++) {
				View view = page.entries.get(j).toView(this);
				view.setPadding(0, 5, 0, 5);
				mainLayout[i].addView(view);
				if (params != null) {
					Parameters ps = params.getParameters(paramIndex++);
					page.entries.get(j).readParams(ps);
				}
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		for (int i = 0; i < pages.size(); i++) {
			Page page = pages.get(i);
			for (int j = 0; j < page.entries.size(); j++) {
				if (page.entries.get(j).view instanceof InputPicture) {
					page.entries.get(j).refresh();
				}
			}
		}
	}

	/**
	 * Creates edit text for name of species. Checks for matching scientific
	 * name, filling in automatically.
	 * 
	 * @return
	 */
	//	protected EditText createNameEdit() {
	//		EditText et = MainOptions.createEdit(this);
	//		et.setOnFocusChangeListener(new OnFocusChangeListener() {
	//
	//			@Override
	//			public void onFocusChange(View aV, boolean aHasFocus) {
	//				if (!aHasFocus
	//						&& !((EditText) aV).getText().toString().isEmpty()) {
	//					sciName.setText("");
	//					for (int i = 0; i < names.size(); i++) {
	//						if (names.get(i)[0].equals(((EditText) aV).getText()
	//								.toString().toLowerCase())) {
	//							sciName.setText(names.get(i)[1]);
	//							massSeekBar.setBounds(
	//									Integer.parseInt(names.get(i)[2]),
	//									Integer.parseInt(names.get(i)[3]));
	//							massLabel.setText(names.get(i)[4]);
	//							lengthSeekBar.setBounds(
	//									Integer.parseInt(names.get(i)[5]),
	//									Integer.parseInt(names.get(i)[6]));
	//							lengthSeekBar.invalidate();
	//							massSeekBar.invalidate();
	//							//redraw();
	//							return;
	//						}
	//					}
	//				}
	//			}
	//
	//		});
	//		return et;
	//	}


	/**
	 * Writes message in logcat when chart activity starts.
	 */
	protected void startChartActivity() {
		Log.d("chart", "Starting Chart Activity...");
	}


	/**
	 * Saves entryform to textfile
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt("tab", host.getCurrentTab());

		Parameters params = new Parameters();
		for (Page page : pages) {
			for (DataEntry entry : page.entries) {
				Parameters ps = new Parameters();
				entry.writeParameters(ps);
				params.addParam(ps);
			}
		}
		outState.putSerializable("params", params);
	}

	protected interface CompleteCheck {
		public boolean isComplete();
	}

	protected class Page {
		public String title;
		public ArrayList<DataEntry> entries;
		public CompleteCheck completeCheck;

		private Activity getContext() {
			return EntryForm.this;
		}

		public boolean isComplete() {
			if (completeCheck != null) {
				return completeCheck.isComplete();
			}
			for (DataEntry entry : entries) {
				if (entry.view instanceof InputView) {
					if (!((InputView)entry.view).isSet()) {
						return false;
					}
				}
			}
			return true;
		}

		public Page(String title) {
			this.title = title;
			entries = new ArrayList<EntryForm.DataEntry>();
		}

		public DataEntry find(String name) {
			for (DataEntry entry : entries) {
				if (entry.name != null && entry.name.equals(name)) {
					return entry;
				}
			}

			return null;
		}

		public View addEntry(DataEntry e) {
			entries.add(e);
			return e.view;
		}

		public View addEntry(String name, String title, View view, 
				boolean multiline) {
			entries.add(new DataEntry(name, title, view, multiline));
			return view;
		}

		public View addEntry(String name, String title, View view) {
			entries.add(new DataEntry(name, title, view));
			return view;
		}

		public View addEntryLine(String name, String title) {
			return addEntry(name, title, new InputLine(getContext()));
		}

		public View addEntryDate(String name, String title) {
			return addEntry(name, title, new InputReadOnly(getContext(), getDateString()));
		}

		public View addEntryTime(String name, String title) {
			return addEntry(name, title, new InputReadOnly(getContext(), getTimeString()));
		}

		public View addEntrySelect(String name, String title, String[] options) {
			return addEntry(name, title, new InputSelect(getContext(), options));
		}

		public View addEntrySelect(String name, String title, String[] options, int offset) {
			return addEntry(name, title, new InputSelect(getContext(), options, offset));
		}

		public View addEntryYesNo(String name, String title) {
			return addEntrySelect(name, title, new String[] {"Yes", "No"});
		}

		public View addEntryGPS(String name, String title) {
			return addEntry(name, title, new InputGPS(getContext()));
		}

		public View addEntryGPSUtm(String name, String title) {
			return addEntry(name, title, new InputGPSUtm(getContext()));
		}

		public View addEntryDecimal(String name, String title, int min, int max) {
			return addEntry(name, title, new InputBoundedDecimal(getContext(), min, max));
		}

		public View addEntryTemperature(String name, String title, int min, int max) {
			return addEntry(name, title, new InputTemperature(getContext(), min, max), true);
		}

		public View addEntryTemperatureAir(String name, String title) {
			return addEntryTemperature(name, title, -10, 40);
		}

		public View addEntryTemperatureWater(String name, String title) {
			return addEntryTemperature(name, title, -10, 40);
		}

		public View addEntryWeight(String name, String title, int min, int max) {
			return addEntry(name, title, new InputBoundedDecimal(getContext(), min, max));
		}

		public View addEntryLength(String name, String title, int min, int max) {
			return addEntry(name, title, new InputBoundedDecimal(getContext(), min, max));
		}

		public View addEntryNumber(String name, String title) {
			return addEntry(name, title, new InputNumber(getContext()));
		}

		public View addEntryPicture(String name, String title) {
			return addEntry(name, title, new InputPicture(getContext()));
		}

		public View addEntryDateSelect(String name, String title) {
			return addEntry(name, title, new InputDateSelect(getContext()));
		}

		public View addEntryTimeSelect(String name, String title) {
			return addEntry(name, title, new InputTimeSelect(getContext()));
		}

		public View addEntryArea(String name, String title) {
			return addEntry(name, title, new InputArea(getContext()), true);
		}

		public View addEntryTitle(String title) {
			return addEntry(null, null, new InputTitle(getContext(), title));
		}

		public View addEntryLabel(String text) {
			return addEntry(null, null, new InputLabel(getContext(), text));
		}

		public void addEntryScientificName(String commonName, String commonTitle,
				String sciName, String sciTitle, HashMap<String, String> map) {
			InputAutoComplete auto = new InputAutoComplete(getContext(), getKeys(map));
			addEntry(commonName, commonTitle, auto);
			addEntry(sciName, sciTitle, new InputScientificName(getContext(), map, auto));
		}
		
		public void addEntryScientificName(String commonName, String commonTitle,
				String sciName, String sciTitle, InputAutoComplete auto, InputScientificName auto2) {
			addEntry(commonName, commonTitle, auto);
			addEntry(sciName, sciTitle, auto2);
		}

		public View addEntryDayOfWeek(String name, String title) {
			return addEntry(name, title, new InputDayOfWeek(getContext()));

		}
	}

	protected class DataEntry {
		public String name, title;
		public View view;
		public boolean multiline;

		public DataEntry(String name, String title, View view) {
			this(name, title, view, false);
		}

		public void refresh() {
			if (view instanceof InputView) {
				Parameters params = new Parameters();
				((InputView)view).writeParameters(params);
				((InputView)view).readParameters(params);
			}
		}

		public DataEntry(String name, String title, View view, 
				boolean multiline) {
			this.name = name;
			this.title = title;
			this.view = view;
			this.multiline = multiline;
		}

		public View toView(Context c) {
			LinearLayout layout = new LinearLayout(c);

			layout.setOrientation(multiline ? LinearLayout.VERTICAL :
				LinearLayout.HORIZONTAL);

			LinearLayout.LayoutParams lps = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			lps.weight = 6;
			lps.gravity = Gravity.CENTER_VERTICAL;

			if (title != null) {
				InputLabel label = new InputLabelFun(c, title);
				if (multiline) {
					label.setPadding(0, 0, 0, 15);
				} else {
					//label.setGravity(Gravity.RIGHT);
				}
				layout.addView(label, lps);
			}

			lps = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 
					LayoutParams.WRAP_CONTENT);
			lps.weight = 4;
			lps.gravity = Gravity.CENTER_VERTICAL;
			layout.addView(view, lps);

			return layout;
		}

		public void readParams(Parameters params) {
			if (view instanceof InputView) {
				((InputView)view).readParameters(params.getParameters(1));
			}
		}

		public void writeParameters(Parameters params) { 
			params.addParam(this.name);
			Parameters ps = new Parameters();
			if (view instanceof InputView) {
				((InputView)view).writeParameters(ps);
			}
			params.addParam(ps);
		}
	}
}
