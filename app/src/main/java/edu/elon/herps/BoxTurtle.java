package edu.elon.herps;


import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

/**
 * Creates a Herps Turtle Form.
 * @author abienz
 *
 */
public class BoxTurtle extends EntryForm {

	@Override
	protected List<Page> getPages() {
		ArrayList<Page> pages = new ArrayList<EntryForm.Page>();
		
		Page page = new Page("Info");
		pages.add(page);
		page.addEntryLine("site", "Site Name:");
		InputTurtleId turtleId = new InputTurtleId(this);
		page.addEntry("turtle", "Turtle ID (if applicable)", turtleId);
		page.addEntryYesNo("transmitter?", "Transmitter?");
		//page.addEntryNumber("freq", "Freq #:"); Removed by Dan Eagle on 3/13/2013
		page.addEntryLine("observers", "Observer(s):"); // Changed from Scribe by Dan Eagle on 3/13/2013
		//page.addEntryNumber("group", "Group Number:"); Removed by Dan Eagle on 3/13/2013
		page.addEntryDateSelect("date:", "Date:");
		page.addEntryTimeSelect("time:", "Time:");
		page.addEntryDayOfWeek("day", "Day of Week:");
		
		page = new Page("Capture");
		pages.add(page);
		page.addEntryYesNo("recap?", "Recapture?");
		page.addEntrySelect("status:", "Status:", getAliveString());
		page.addEntrySelect("capture", "Capture Method:", getCaptureString(), 1);
		page.addEntryGPSUtm("coordinates", "Coordinates (UTM):");
		//page.addEntrySelect("zone:", "UTM Zone:", getUtmZone());
		/*page.addEntrySelect("datum:", "Datum:", new String[] {
				"WGS84", "NAD27", "NAD83"
		});*/
		page.addEntryGPS("gps", "GPS Coordinates:");
		page.addEntrySelect("inside", "Inside Defined Study Site?", new String[] {
				"Yes", "No", "Does not Apply"
		});
		page.addEntryLine("which", "Which Defined Study Site?");
		
		page = new Page("Conditions");
		pages.add(page);
		page.addEntryLine("location", "Location Description:");
		page.addEntryTemperatureAir("air", "Air Temp:");
		page.addEntrySelect("sky", "Sky Index:", getSkyIndexString(), 0);
		page.addEntrySelect("weather:", "Weather:", getRainStrings(), 1);
		page.addEntryNumber("days", "Days Since Last Rain:");
		
		page = new Page("Observations");
		pages.add(page);
		page.addEntrySelect("life", "Life Stage:", getLifeStageString());
		page.addEntrySelect("sex:", "Sex:", getGenderStrings());
		page.addEntryNumber("annuli", "Estimated Number of Annuli:");
		page.addEntrySelect("habitat:", "Habitat:", getHabitatString(), 1);
		page.addEntryPicture("carapace", "Take photo of carapace");
		page.addEntryPicture("plastron", "Take photo of plastron");
		//page.addEntryPicture("take", "Take Photo");
		
		page = new Page("Measurements");
		pages.add(page);
		page.addEntryWeight("weight:", "Weight (g):", 30, 500);
		int min = 20, max = 360;
		page.addEntryLength("straightline", "Straightline CL (mm):",
				min, max);
		page.addEntryLength("max", "Max CW (mm):",
				min, max);
		page.addEntryLength("pl", "PL Anterior to hinge (mm):",
				min, max);
		page.addEntryLength("pl", "PL Hinge to posterior (mm):",
				min, max);
		page.addEntryLength("shell", "Shell height at hinge (mm):",
				min, max);
		page.addEntryTitle("CL= Carapace Length; CW=Carapace Width; PL=Plastron Length");
		
		page = new Page("Notes");
		pages.add(page);
		page.addEntryArea("injuries", "Description of any injuries, such as bite marks, " +
		"unusual scute patterns, defects or parasites:");
		page.addEntryArea("notes", "Notes (behavior, habitat, etc.)");

		return pages;
	}

	@Override
	protected String getFormName() {
		return "Box Turtle";
	}

}
