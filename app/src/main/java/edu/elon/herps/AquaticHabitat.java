package edu.elon.herps;


import java.util.ArrayList;
import java.util.List;

import edu.elon.herps.EntryForm.Page;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Creates a Herps Turtle Form.
 * @author abienz
 *
 */
public class AquaticHabitat extends RunForm {

	private OnClickListener[] listeners;
	
	@Override
	protected void submit() {
		if (listeners != null) {
			for (OnClickListener l : listeners) {
				l.onClick(null);
			}
		}
		super.submit();
	}

	@Override
	protected String getFormName() {
		return "Aquatic Habitat Data";
	}

	private class TrapBase extends TextView implements InputView {

		protected String info = "";
		protected int num = 1;

		public TrapBase(Context context) {
			super(context);
			setTextSize(LABEL_SIZE);
		}

		@Override
		public void writeParameters(Parameters params) {
			params.addParam(info);
			params.addParam(num);
		}

		@Override
		public void readParameters(Parameters params) {
			info = params.getString();
			num = params.getInt(1);
			setText(info);
		}

		@Override
		public boolean isSet() {
			return info.length() > 0;
		}
	}

	private class Others extends TrapBase {
		public Others(Context context) {
			super(context);
		}
		
		public boolean add(String species, String sciName, InputSelect age, InputNumber legs, InputNumber number) {
			String info = "";
			boolean set = false;
			if (this.info.length() > 0) {
				info += "\n";
				set = true;
			} 
			if (species.length() > 0) {
				info += "species: " + species + "; ";
				set = true;
			} 
			if (sciName.length() > 0) {
				info += "scientific name: " + sciName + "; ";
				set = true;
			}
			if (age.isSet()) {
				info += "age: " + age.getSelectedItem().toString() + "; ";
				set = true;
			}
			if (legs.isSet()) {
				info += String.format("%s legs; ", legs.getText().toString());
				set = true;
			}
			if (number.isSet()) {
				info += number.getText().toString();
				set = true;
			}
			if (set) {
				this.info += info;
				num++;
				setText(this.info);
			}
			return set;
		}

		public boolean add(InputLine name, InputNumber number) {
			String info = "";
			boolean set = true;
			if (this.info.length() > 0) {
				info += "\n";
			}
			if (name.isSet()) {
				info += name.getText().toString() + ": ";
			} else {
				set = false;
			}
			if (number.isSet()) {
				info += number.getText().toString();
			} else {
				set = false;
			}
			if (set) {
				this.info += info;
				num++;
				setText(this.info);
			}
			return set;
		}
	}

	private class Salamanders extends TrapBase {
		public Salamanders(Context context) {
			super(context);
		}

		public boolean add(String species, String sciName, InputSelect age, 
				InputBoundedDecimal length, InputBoundedDecimal weight, InputSelect sex, 
				InputNumber legs, InputSelect gils) {
			String info = "";
			boolean set = false;
			if (this.info.length() > 0) {
				info += "\n";
				set = true;
			}
			info += String.format("Salamander %d: ", num);
			if (species.length() > 0) {
				info += String.format("species: %s; ", species);
				set = true;
			}
			if (sciName.length() > 0) {
				info += String.format("scientific name: %s; ", sciName);
				set = true;
			}
			if (age.isSet()) {
				info += String.format("age: %s; ", age.getSelectedItem().toString());
				set = true;
			}
			if (length.isSet()) {
				info += String.format("length: %.01fmm; ", length.getValue());
				set = true;
			}
			if (weight.isSet()) {
				info += String.format("weight: %.01fmm; ", weight.getValue());
				set = true;
			}
			if (sex.isSet()) { 
				info += String.format("sex: %s; ", sex.getSelectedItem().toString());
				set = true;
			}
			if (legs.isSet()) {
				info += String.format("%s legs; ", legs.getText().toString());
				set = true;
			}
			if (gils.isSet()) { 
				info += String.format("gils are %s", gils.getSelectedItem().toString());
				set = true;
			}
			if (set) {
				this.info += info;
				num++;
				setText(this.info);
			}
			return set;
		}
	}

	@Override
	protected void addStartPages(List<Page> pages) {
		Page page = new Page("Info");
		pages.add(page);
		page.addEntryLine("observer's", "Observer's Name(s):");
		page.addEntryNumber("number", "Number in party:");
		page.addEntryDateSelect("date:", "Date:");
		page.addEntryTitle("Location of Body of Water");
		page.addEntryLine("county:", "County:");
		page.addEntryLine("site", "Site Name:");
		page.addEntrySelect("state", "State:", getStatesString());
		page.addEntryGPS("gps", "GPS Coordinates:");

		page = new Page("Start Params");
		pages.add(page);
		page.addEntryTitle("Environmental Parameters at Start of Survey");
		page.addEntryTimeSelect("beginning", "Time at Start:");
		page.addEntryDecimal("realtiveHumidity", "Relative Humidity (%):", 0, 100);
		page.addEntrySelect("wind", "Wind Code:", getWindStrings(), 0);
		page.addEntrySelect("sky", "Sky Code:", getSkyCodeString(), 0);
		page.addEntryNumber("rain", "Rain in past 24 hours (mm):");
		page.addEntryNumber("lastRain", "Number of days since last rainfall:");
		page.addEntryTemperatureAir("air", "Air Temp:");
		page.addEntryTemperatureAir("airMax", "Max Air Temp (in past 24 hours):");
		page.addEntryTemperatureAir("airMin", "Min Air Temp (in past 24 hours):");
		page.addEntryTemperatureAir("groundTemp", "Ground Temerature (top 3cm):");
		page.addEntryDecimal("ph", "Aquatic Sampling pH:", 4, 9);
		page.addEntryTemperatureWater("waterTemp", "Surface Water Temperature:");
	}

	@Override
	protected void addDataPages(List<Page> pages) {
		Page page = new Page("Trap");
		pages.add(page);
        page.addEntrySelect("captureMethod", "Capture Method: ", new String[] {
                "Trap",
                "Leaf Pack",
                "Dip Net",
                "Other"
        });
		page.addEntryNumber("trap", "Trap Number:");
        page.addEntryYesNo("baited", "Baited: ");
		page.addEntryLine("typeOfBait", "Type of Bait: ");
        page.addEntryLine("trapNotes", "Trap Notes (please explain the nature of your trap types and/or condition");

		page = new Page("Salamander");
		pages.add(page);
		page.addEntryTitle("Salamander");
		page.addEntryScientificName("species", "Common Name:", "sciName", 
				"Scientific Name:", getSalamanderNames());
		final DataEntry entrySpecies = page.find("species");
		final DataEntry entrySciName = page.find("sciName");
		final InputSelect salAge = new InputSelect(this, getLifeStageStringSalamander());
		page.addEntry("", "Age class:", salAge);
		final InputBoundedDecimal salLength = new InputBoundedDecimal(this, 5, 400);
		page.addEntry("", "Total Length (mm):", salLength, true);
		final InputBoundedDecimal salWeight = new InputBoundedDecimal(this, 1, 100);
		page.addEntry("", "Mass (g):", salWeight, true);
		final InputSelect salSex = new InputSelect(this, getGenderStrings());
		page.addEntry("", "Sex:", salSex);
		final InputNumber salLegs = new InputNumber(this);
		page.addEntry("", "Number of Legs:", salLegs);
		final InputSelect salGils = new InputSelect(this, new String[] {
				"Absent", "Present"
		});
		page.addEntry("", "Gils present:", salGils);
		Button addSalamander = new Button(this);
		addSalamander.setText("Add Salamander");
		page.addEntry(null, null, addSalamander);
		final Salamanders sals = new Salamanders(this);
		page.addEntry(null, "List of salamanders: ", sals, true);
        page.addEntryPicture("salPicture", "Take photo of the salamander");
		OnClickListener salClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				String species = "";
				if (entrySpecies != null) {
					species = ((EditText) entrySpecies.view).getText().toString();
					((EditText) entrySpecies.view).setText("");
				}
				String sciName = "";
				if (entrySciName != null) {
					sciName = ((EditText) entrySciName.view).getText().toString();
					((EditText) entrySciName.view).setText("");
				}
				
				sals.add(species, sciName, salAge, salLength, salWeight, salSex, salLegs, salGils);
				salLength.setValue(false);
				salWeight.setValue(false);
				salSex.setSelection(0);
				salLegs.setText("");
				salGils.setSelection(0);
				salAge.setSelection(0);
			}
		};
		addSalamander.setOnClickListener(salClick);
		page.completeCheck = new CompleteCheck() {
			@Override
			public boolean isComplete() {
				return sals.isSet();
			}
		};

		page = new Page("Frog");
		pages.add(page);
		page.addEntryTitle("Frogs");
		page.addEntryScientificName("species", "Common Name:", "sciName", 
				"Scientific Name:", getFrogNames());
		final DataEntry entrySpeciesAmph = page.find("species");	
		final DataEntry entrySciNameAmph = page.find("sciName");
		final InputNumber amphNumber = new InputNumber(this);
		final InputSelect amphAge = new InputSelect(this, getLifeStageStringFrog());
		page.addEntry("", "Age class:", amphAge);
		final InputNumber amphLegs = new InputNumber(this);
		page.addEntry("", "Number of Legs:", amphLegs);
		page.addEntry("", "Number Observed:", amphNumber);
		Button addAmph= new Button(this);
		addAmph.setText("Add Frog");
		page.addEntry(null, null, addAmph);
		final Others amphs = new Others(this);
		page.addEntry(null, "List of Frogs: ", amphs, true);
		OnClickListener amphClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				String amphSpecies = "";
				if (entrySpeciesAmph != null) {
					amphSpecies = ((EditText) entrySpeciesAmph.view).getText().toString();
					((EditText) entrySpeciesAmph.view).setText("");
				}
				String amphSciName = "";
				if (entrySciNameAmph != null) {
					amphSciName = ((EditText) entrySciNameAmph.view).getText().toString();
					((EditText) entrySciNameAmph.view).setText("");
				}
				if (amphs.add(amphSpecies, amphSciName, amphAge, amphLegs, amphNumber)) {
					amphNumber.setText("");
					amphAge.setSelection(0);
					amphLegs.setText("");
				}
			}
		};
		addAmph.setOnClickListener(amphClick);
		page.completeCheck = new CompleteCheck() {
			@Override
			public boolean isComplete() {
				return amphs.isSet();
			}
		};

		page = new Page("Other");
		pages.add(page);
		page.addEntryTitle("Macroinvertebrates");
		final InputLine macroName = new InputLine(this);
		page.addEntry("", "Species:", macroName);
		final InputNumber macroNumber = new InputNumber(this);
		page.addEntry("", "Number Observed:", macroNumber);
		Button addMacro = new Button(this);
		addMacro.setText("Add Macroinvertebrate");
		page.addEntry(null, null, addMacro);
		final Others macros = new Others(this);
		page.addEntry(null, "Macroinvertebrates: ", macros, true);
        page.addEntryLine("otherAnimals", "Other Animals: ");
		OnClickListener macroClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (macros.add(macroName, macroNumber)) {
					macroName.setText("");
					macroNumber.setText("");
				}
			}
		};
		addMacro.setOnClickListener(macroClick);
		page.completeCheck = new CompleteCheck() {
			@Override
			public boolean isComplete() {
				return macros.isSet();
			}
		};

		listeners = new OnClickListener[] {
			salClick, amphClick, macroClick	
		};
	}

	@Override
	protected void addEndPages(List<Page> pages) {
		Page page = new Page("End Params");
		pages.add(page);
		page.addEntryTitle("Environmental Parameters at End of Survey");
		page.addEntryTemperatureAir("air", "Air Temp:");
		page.addEntryDecimal("realtiveHumidity", "Relative Humidity (%):", 0, 100);
		page.addEntrySelect("wind", "Wind Code:", getWindStrings(), 0);
		page.addEntrySelect("sky", "Sky Code:", getSkyCodeString(), 0);
	}

}
