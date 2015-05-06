package edu.elon.herps;


import java.util.HashMap;
import java.util.List;

import edu.elon.herps.FormBase.InputAutoComplete;
import edu.elon.herps.FormBase.InputScientificName;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemSelectedListener;

public class FieldData extends RunForm {

	@Override
	protected String getFormName() {
		return "Field Data";
	}

	@Override
	protected void addStartPages(List<Page> pages) {
		Page page = new Page("Info");
		pages.add(page);

		page.addEntryLine("observer's", "Observer's Name(s):");
		page.addEntryDateSelect("date:", "Date:");
		page.addEntryTitle("Location");
		page.addEntryLine("county:", "County:");
		page.addEntrySelect("state", "State", getStatesString());
		page.addEntryLine("site", "Site Name:");
		page.addEntryNumber("number", "Number in party:");


		page = new Page("Start Params");
		pages.add(page);
		page.addEntryTitle("Environmental Parameters at Start of Survey");
		page.addEntryTimeSelect("beginning", "Time at Start:");
		page.addEntryDecimal("relative", "Relative Humidity (%):", 0, 100);
		page.addEntrySelect("wind", "Wind Code:", getWindStrings(), 0);
		page.addEntrySelect("sky", "Sky Code:", getSkyCodeString(), 0);
		page.addEntryNumber("rain", "Rain in past 24 hours (mm):");
		page.addEntryNumber("lastRain", "Number of days since last rainfall:");
		page.addEntryTemperatureAir("air", "Air Temp:");
		page.addEntryTemperatureAir("groundTemp", "Ground Temperature (top 3cm):");
	}

	@Override
	protected void addDataPages(List<Page> pages) {
		Page page = new Page("Data");
		pages.add(page);
		page.addEntryTitle("Organism Information");
		class InputSelectSp extends InputSelect {
			public InputSelectSp(Context context, String[] options) {
				super(context, options, false);
			}

			@Override
			public void readParameters(Parameters params) {
				super.readParameters(params);
				int selected = getSelectedItemPosition();
				getOnItemSelectedListener().onItemSelected(
						this, null, selected, 0);
			}
		}
		final String[] types = {"Frog", "Salamander", "Lizard", "Snake", "Other"};
		InputSelect type = new InputSelectSp(this, types);
		final int[] massRanges = new int[] {
				0, 400,
				0, 400,
				0, 500,
				0, 22000,
				0, 1000
		};
		final int[] lengthRanges = new int[] {
				5, 200,
				10, 100,
				40, 150,
				50, 1250,
				5, 500
		};
		final InputBoundedDecimal inputWeight = new InputBoundedDecimal(this, 1, 3);
		final InputBoundedDecimal inputLength = new InputBoundedDecimal(this, 1, 3);
		final HashMap<String, String> map = new HashMap<String, String>();
		final InputAutoComplete auto = new InputAutoComplete(getBaseContext(), getKeys(map));
		final InputScientificName auto2 = new InputScientificName(getBaseContext(), map, auto);
		OnItemSelectedListener listener = new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int index, long id) {
				inputWeight.setBounds(massRanges[index * 2], massRanges[index * 2 + 1]);
				inputLength.setBounds(lengthRanges[index * 2], lengthRanges[index * 2 + 1]);
				if (index == 0) {
					System.out.println("FROGS");
					map.clear();
					map.putAll(getCallingNames());
					auto.clearListSelection();
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), 
							R.layout.autocomplete_item, R.id.textView1, getKeys(map));
					auto.setAdapter(adapter);
					auto2.clearListSelection();
					ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getBaseContext(), 
							R.layout.autocomplete_item, R.id.textView1, getValues(map));
					auto2.setAdapter(adapter2);
				}else if (index == 1) { 
					System.out.println("SALAMANDER");
					map.clear();
					map.putAll(getSalamanderNames());
					auto.clearListSelection();
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), 
							R.layout.autocomplete_item, R.id.textView1, getKeys(map));
					auto.setAdapter(adapter);
					auto2.clearListSelection();
					ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getBaseContext(), 
							R.layout.autocomplete_item, R.id.textView1, getValues(map));
					auto2.setAdapter(adapter2);
				} else if (index == 2) {
					System.out.println("LIZARDS");
					map.clear();
					map.putAll(getLizardNames());
					auto.clearListSelection();
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), 
							R.layout.autocomplete_item, R.id.textView1, getKeys(map));
					auto.setAdapter(adapter);
					auto2.clearListSelection();
					ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getBaseContext(), 
							R.layout.autocomplete_item, R.id.textView1, getValues(map));
					auto2.setAdapter(adapter2);
				} else if (index == 3) {
					System.out.println("SNKAES");
					map.clear();
					map.putAll(getSnakeNames());
					auto.clearListSelection();
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), 
							R.layout.autocomplete_item, R.id.textView1, getKeys(map));
					auto.setAdapter(adapter);
					auto2.clearListSelection();
					ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getBaseContext(), 
							R.layout.autocomplete_item, R.id.textView1, getValues(map));
					auto2.setAdapter(adapter2);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		};

		//View v = 
		page.addEntry("type", "Type:", type);
		//v.performClick();
		page.addEntryScientificName("species", "Common Name:", "sciName", "Scientific Name:", auto, auto2);
		page.addEntryLine("cover", "Cover piece, PVC, trap number, or other site description:");
		page.addEntry("weight", "Weight (g):", inputWeight, true);
		page.addEntry("length", "Length (Frog, Lizard or Salamander - Snout Vent Length; Snake - Total Length (mm):", inputLength, true);
		page.addEntrySelect("age", "Age Class", getLifeStageString());
		page.addEntryGPS("gps", "GPS Coordinates");
		page.addEntryPicture("take", "Take Photo");
		page.addEntryArea("comments", "Comments");

		listener.onItemSelected(null, null, 0, 0);
		type.setOnItemSelectedListener(listener);
	}

	@Override
	protected void addEndPages(List<Page> pages) {
		Page page = new Page("Ending Params");
		pages.add(page);
		page.addEntryTitle("Environmental Parameters at End of Survey");
		page.addEntryTimeSelect("endingTime", "Time at End:");
		page.addEntryTemperatureAir("air", "Air Temp:");
		page.addEntryDecimal("relative", "Relative Humidity (%):", 0, 100);
		page.addEntrySelect("wind", "Wind Code:", getWindStrings(), 0);
		page.addEntrySelect("sky", "Sky Code:", getSkyCodeString(), 0);
	}

}
