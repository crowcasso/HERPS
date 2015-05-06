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
public class AquaticTurtle extends EntryForm {

	@Override
	protected List<Page> getPages() {
		ArrayList<Page> pages = new ArrayList<EntryForm.Page>();
		
		Page page = new Page("Info");
		pages.add(page);
		page.addEntryScientificName("species", "Common Name:", "sciName", 
				"Scientific Name:", getTurtleNames());
		page.addEntrySelect("confidence", "Confidence level of species identification:", 
				getConfidenceLevels(), 1);
		page.addEntryLine("code", "Turtle Code:");
		page.addEntryDateSelect("date", "Date:");
		page.addEntryTimeSelect("time", "Time:");
		page.addEntryLine("observers", "Observer(s):");
		page.addEntrySelect("capture", "Capture Method:", getCaptureString(), 1);
		page.addEntryYesNo("recapture", "Recapture?");
		
		
		page = new Page("Location");
		pages.add(page);
		page.addEntryTitle("Exact Location of Capture");
		page.addEntryLine("county", "County: ");
		page.addEntrySelect("state", "State:", getStatesString());
		page.addEntryLine("site", "Site:");
		page.addEntryGPS("gps", "GPS Coordinates:");
		page.addEntrySelect("habitat", "Habitat:", getHabitatString(), 1);
		page.addEntryArea("locationDescription", 
				"Location description (including trap number if applicable):");
		
		page = new Page("Conditions");
		pages.add(page);
		page.addEntrySelect("weather", "Weather Conditions:", getRainStrings(), 1);
		page.addEntryTemperatureWater("waterTemp", "Water Temperature:");
		page.addEntryTemperatureAir("airTemp", "Air Temperature:");
		page.addEntryNumber("sinceRain", "Days since last rainfall:");
		page.addEntrySelect("skyIndex", "Sky Index:", getSkyIndexString(), 0);
		
		page = new Page("Observations");
		pages.add(page);
		page.addEntrySelect("gender", "Gender of Turtle:", getGenderStrings());
		page.addEntryPicture("carapace", "Take photo of carapace");
		page.addEntryPicture("plastron", "Take photo of plastron");
		page.addEntryArea("injuries", "Description of any injuries, such as bite marks, " +
				"unusual scute patterns, defects or parasites:");
		
		page = new Page("Measurements");
		pages.add(page);
		int minLength = 50, maxLength = 360;
		int minHeight = 25, maxHeight = 150;
		page.addEntryWeight("weight", "Weight (g):", 50, 5000);
		page.addEntryLength("plastronLength", "Plastron Length (mm):",
				minLength, maxLength);
		page.addEntryLength("carapaceLength", "Carapace Length (mm):",
				minLength, maxLength);
		page.addEntryLength("width", "Width at Widest Point (mm):",
				minLength, maxLength);
		page.addEntryLength("shellHeight", "Shell Height at Tallest Point (mm):",
				minHeight, maxHeight);
		
		page = new Page("Release");
		pages.add(page);
		page.addEntryDateSelect("releaseDate", "Date of Release:");
		page.addEntryTimeSelect("releaseTime", "Time of Release:");
		page.addEntryYesNo("relCapture", "Was the tutrle released at point of capture?");
		page.addEntryLine("relCaptureExpl", "Please explain:");
		page.addEntryArea("relCaptureDis", "If not released at point of capture, where " +
				"and how far from the point of capture was turtle released?");
		page.addEntryArea("comments", "Additional Comments:");
		
		return pages;
	}

	@Override
	protected String getFormName() {
		return "Aquatic Turtle";
	}

}
