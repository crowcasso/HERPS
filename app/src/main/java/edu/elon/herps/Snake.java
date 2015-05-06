package edu.elon.herps;


import java.util.ArrayList;
import java.util.List;

import edu.elon.herps.NumberSeekBar.ExpSeekBar;

import android.os.Bundle;
import android.view.View;

/**
 * Creates a Herps Turtle Form.
 * @author abienz
 *
 */
public class Snake extends EntryForm {

	@Override
	protected List<Page> getPages() {
		ArrayList<Page> pages = new ArrayList<EntryForm.Page>();
		
		Page page = new Page("Info");
		pages.add(page);
		page.addEntryScientificName("species", "Common Name:", "sciName", 
				"Scientific Name:", getSnakeNames());
		page.addEntrySelect("confidence", "Confidence level of species identification:", 
				getConfidenceLevels(), 1);
		page.addEntryDateSelect("date", "Date:");
		page.addEntryTimeSelect("time", "Time:");
		page.addEntryDayOfWeek("dayOfWeek", "Day of the Week:");
		page.addEntryLine("observers", "Observer(s):");
		page.addEntrySelect("capture", "Capture Method:", getSnakeCaptureString(), 1);
		page.addEntrySelect("status", "Status:", getAliveString());
		
		
		page = new Page("Location");
		pages.add(page);
		page.addEntryArea("areaDesc", "Exact Location of Capture");
		page.addEntryGPS("gps", "GPS Coordinates:");
		page.addEntrySelect("habitat", "Habitat:", getHabitatString(), 1);
		page.addEntryArea("microhabitat", "Microhabitat (under a rock/coverboard, in a stump hole, rocks near dam or lake, etc.):");
		
		page = new Page("Conditions");
		pages.add(page);
		page.addEntryTemperatureAir("airTemp", "Air Temperature:");
		page.addEntryNumber("sinceRain", "Days since last rainfall:");
		page.addEntryDecimal("realtiveHumidity", "Relative Humidity (%):", 0, 100);
		page.addEntrySelect("skyIndex", "Sky Conditions:", getSkyIndexString(), 0);
		page.addEntrySelect("windCode", "Wind Conditions: ", getWindStrings(), 0);
		
		page = new Page("Observations");
		pages.add(page);
		page.addEntrySelect("gravid", "Gravid?", new String[] {
				"Yes", "No", "Unknown", "N/A"
		});
		page.addEntrySelect("lifeStage", "Life Stage: ", getLifeStageString());
		page.addEntryPicture("photo", "Take photo:");
		page.addEntryArea("injuries", "Description of any injuries, defects or parasites:");
		page.addEntryArea("notes", "Notes (what was the animal doing when you found it, behavior, etc.):");
		//page.addEntryDateSelect("cha", "Date added to CHA (Carolina Herps Atlas");
		
		page = new Page("Measurements");
		pages.add(page);
		page.addEntryWeight("weight", "Weight (g):", 0, 22000);
		page.addEntryLength("length", "Total Length (mm):",
				50, 1250);
		page.addEntryLength("length", "Estimated Length (mm):",
				50, 1250);
        page.addEntrySelect("methodMeasuring", "Method used for Measuring Length:", getMethodMeasuringStrings(), 0);
		
		return pages;
	}

	@Override
	protected String getFormName() {
		return "Snake";
	}

}
