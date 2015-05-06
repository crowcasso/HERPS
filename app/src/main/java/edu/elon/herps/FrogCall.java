package edu.elon.herps;


import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Creates a Herps Turtle Form.
 * @author abienz
 *
 */
public class FrogCall extends EntryForm {

	@Override
	protected List<Page> getPages() {
		ArrayList<Page> pages = new ArrayList<EntryForm.Page>();
		
		Page page = new Page("Info");
		pages.add(page);
		page.addEntryLine("observer's", "Observer's Name(s):");
		page.addEntryNumber("number", "Number in party:");
		page.addEntryDateSelect("date:", "Date:");
		page.addEntryTitle("Location of Survey");
		page.addEntryLine("county:", "County:");
		page.addEntryLine("site", "Site Name:");
		page.addEntrySelect("state", "State:", getStatesString());

		page = new Page("Before");
		pages.add(page);
		page.addEntryTitle("Environmental Parameters at Start of Run");
		page.addEntryTimeSelect("beginning", "Beginning Time:");
		page.addEntryTemperatureAir("air", "Air Temp:");
		page.addEntryDecimal("relative", "Relative Humidity (%):", 0, 100);
		page.addEntrySelect("wind", "Wind Code:", getWindStrings(), 0);
		page.addEntrySelect("sky", "Sky Code:", getSkyCodeString(), 0);
		page.addEntryNumber("rain", "Rain in past 24 hours (mm):");
		page.addEntryNumber("3", "Number of days since last rainfall:");
		page.addEntrySelect("moon", "Moon Phase:", getMoonPhases());
		
		page = new Page("Call");
		pages.add(page);
		page.addEntryTitle("Instructions");
		page.addEntryLabel("At each stop listen for the appropriate amount of time (recording start time), then record the location and air temperature, the amphibian calling index for each species heard, and whether moon light was visible or not");
		FrogCallChart fcc = new FrogCallChart(this);
		page.addEntry(null, "Frog Call:", fcc, true);
		
		
		page = new Page("After");
		pages.add(page);
		page.addEntryTitle("Environmental Parameters at End of Run");
		page.addEntryTemperatureAir("air", "Air Temp:");
		page.addEntryDecimal("relative", "Relative Humidity (%):", 0, 100);
		page.addEntrySelect("wind", "Wind Code:", getWindStrings(), 0);
		page.addEntrySelect("sky", "Sky Code:", getSkyCodeString(), 0);

		return pages;
	}

	@Override
	protected String getFormName() {
		return "Frog Call";
	}
}
