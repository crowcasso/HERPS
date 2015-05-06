package edu.elon.herps;

import java.util.ArrayList;
import java.util.List;

import edu.elon.herps.EntryForm.Page;

public class CopyOfLizard extends EntryForm {

	@Override
	protected List<Page> getPages() {
		ArrayList<Page> pages = new ArrayList<EntryForm.Page>();
		
		Page page = new Page("Info");
		pages.add(page);
		page.addEntryNumber("group_number", "Group Number:");
		page.addEntryLine("scribe", "Scribe:");
		page.addEntryDateSelect("date", "Date:");
		page.addEntryNumber("group_size", "Number in party:");
		page.addEntryTitle("Location of Run");
		page.addEntryLine("county:", "County:");
		page.addEntrySelect("state", "State:", getStatesString());
		page.addEntryLine("site", "Site Name:");
		
		page = new Page("Before");
		pages.add(page);
		page.addEntryTitle("Environmental Parameters at Start of Run");
		page.addEntryTimeSelect("beginning", "Beginning Time:");
		page.addEntryTemperatureAir("air", "Air Temp:");
		page.addEntryDecimal("relative", "Relative Humidity (%):", 0, 100);
		page.addEntryNumber("rain", "Rain in past 24 hours (mm):");
		page.addEntryNumber("3", "Number of days since last rainfall:");
		
		page = new Page("Entries");
		pages.add(page);
		//page.addEntryTitle("Instructions");
		//page.addEntryLabel("At each stop listen for the appropriate amount of time (recording start time), then record the location and air temperature, the amphibian calling index for each species heard, and whether moon light was visible or not");
		LizardEntry le = new LizardEntry(this);
		page.addEntry(null, "", le, true);
		
		page = new Page("After");
		pages.add(page);
		page.addEntryTitle("Environmental Parameters at End of Run");
		page.addEntryTimeSelect("beginning", "Ending Time:");
		page.addEntryTemperatureAir("air", "Air Temp:");
		page.addEntryDecimal("relative", "Relative Humidity (%):", 0, 100);
		
		return pages;
	}

	@Override
	protected String getFormName() {
		return "Lizard";
	}

}
