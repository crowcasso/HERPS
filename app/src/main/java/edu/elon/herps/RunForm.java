package edu.elon.herps;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;

import edu.elon.herps.EntryForm.Page;

public abstract class RunForm extends EntryForm {

	public final static int RUN_START = 0;
	public final static int RUN_DATA = 1;
	public final static int RUN_END = 2;

	public final static int DO_RUN = 100;
	
	@Override
	protected List<Page> getPages() {
		int run = getIntent().getExtras().getInt("run");
		ArrayList<Page> pages = new ArrayList<EntryForm.Page>();
		if (run == RUN_START) {
			addStartPages(pages);
		} 
		if (run == RUN_DATA) { 
			addDataPages(pages);
		} 
		if (run == RUN_END) {
			addEndPages(pages);
		}
		return pages;
	}
	
	@Override
	protected void submit() {
		String file = getFileOut();
		Intent intent = new Intent();
		intent.putExtra("run", getIntent().getExtras().getInt("run"));
		intent.putExtra("suffix", getIntent().getExtras().getString("suffix"));
		intent.putExtra("category", getFormName());
		intent.putExtra("file", file);
		setResult(RESULT_OK, intent);
		super.submit(file);
	}
	
	protected abstract void addStartPages(List<Page> pages);
	protected abstract void addDataPages(List<Page> pages);
	protected abstract void addEndPages(List<Page> pages);
}
