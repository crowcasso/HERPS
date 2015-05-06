package edu.elon.herps;

import android.util.Log;

public class Debug {
	public static final boolean DEBUG = true;
	
	public static void write(String s) {
		if (!DEBUG) return;
		Log.d("DEBUG", "--- " + s + " ----");
	}
	
	public static void write(Object o) {
		write(o == null ? "null" : o.toString());
	}
	
	public static void write(Exception e) {
		if (DEBUG) {
			e.printStackTrace();
		}
	}
	
	public static void write(String s, Object... args) { 
		write(String.format(s, args));
	}
	
	public static void write(int x) {
		write("" + x);
	}
	
	public static void write(double x) {
		write("" + x);
	}
	
	public static void write(float x) {
		write("" + x);
	}
}
