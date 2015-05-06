/**
 * Preferences.java 1.0 Mar 12, 2012
 *
 * Copyright (c) 2009 Amanda J. Bienz
 * Campus Box 3531, Elon University, Elon, NC 27244
 */
package edu.elon.herps;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import uk.me.jstott.jcoord.LatLng;
import uk.me.jstott.jcoord.UTMRef;

import edu.elon.herps.NumberSeekBar.ExpSeekBar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

/**
 * Creates different views for forms.
 * @author abienz
 * 
 */
public class FormBase extends Activity {

	public final static int LABEL_SIZE = 15;
	public final static int TITLE_SIZE = 22;
	public final static int FOOTER_SIZE = 15;

	private static ArrayList<String[]> sciNameArray = new ArrayList<String[]>();


	public static String getDateString() {
		SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy");
		return format.format(new Date());
	}

	public static String getTimeString() {
		SimpleDateFormat format = new SimpleDateFormat("h:mm aa");
		return format.format(new Date());
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public interface InputView {
		public void writeParameters(Parameters params);
		public void readParameters(Parameters params);
		public boolean isSet();
	}

	public static class InputLine extends EditText implements InputView {
		public InputLine(Context context) {
			super(context);
			setTextSize(LABEL_SIZE);
			setGravity(Gravity.CENTER_VERTICAL);
			setBackgroundColor(Color.DKGRAY);
			setTextColor(Color.WHITE);
			setPadding(5, 10, 5, 10);
			setSingleLine(true);
		}

		@Override
		public void writeParameters(Parameters params) {
			params.addParam(getText().toString());
		}

		@Override
		public void readParameters(Parameters params) {
			setText(params.getString());
		}

		@Override
		public boolean isSet() {
			return getText().length() > 0;
		}

	}

	public static class InputAutoComplete extends AutoCompleteTextView 
	implements InputView {

		public InputAutoComplete(Context context, List<String> options) {
			super(context);
			setTextSize(LABEL_SIZE);
			setGravity(Gravity.CENTER_VERTICAL);
			setBackgroundColor(Color.DKGRAY);
			setTextColor(Color.WHITE);
			setPadding(5, 10, 5, 10);

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, 
					R.layout.autocomplete_item, R.id.textView1, options);
			setAdapter(adapter);

			setSingleLine(true);
		}

		@Override
		public void writeParameters(Parameters params) {
			params.addParam(getText().toString());
		}

		@Override
		public void readParameters(Parameters params) {
			setText(params.getString());
		}

		@Override
		public boolean isSet() {
			return getText().length() > 0;
		}
	}

	public static class InputScientificName extends InputAutoComplete {

		public InputScientificName(Context context, final HashMap<String, String> map, 
				final InputAutoComplete commonName) {
			super(context, getValues(map));
			commonName.setOnEditorActionListener(new OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					String name = v.getText().toString();
					if (map.containsKey(name)) {
						setText(map.get(name));
					}
					return false;
				}
			});
			commonName.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					String name = (String) commonName.getAdapter().getItem(position);
					if (map.containsKey(name)) {
						setText(map.get(name));
					}
				}
			});

		}
	}

	public static class InputNumber extends InputLine {

		public InputNumber(Context context) {
			super(context);
			setFilters(new InputFilter[] {
					new InputFilter.LengthFilter(10)
			});
			setInputType(InputType.TYPE_CLASS_NUMBER | 
					InputType.TYPE_NUMBER_FLAG_SIGNED);
		}


		@Override
		public void writeParameters(Parameters params) {
			String text = getText().toString();
			if (text.length() > 0) {
				try {
					params.addParam(Integer.parseInt(getText().toString()));
					return;
				} catch (Exception e) {
					Debug.write(e);
				}
			}
			params.addParam("");
		}

		@Override
		public void readParameters(Parameters params) {
			setText(params.getObject().toString());
		}

		@Override
		public boolean isSet() {
			return getText().length() > 0;
		}
	}

	public static class InputArea extends InputLine {

		public InputArea(Context context) {
			super(context);
			setSingleLine(false);
		}

	}

	public static class InputSelect extends Spinner implements InputView {

		private int offset;
		boolean addBlank;

		public InputSelect(Context context, String[] options) {
			this(context, options, -1, true);
		}

		public InputSelect(Context context, String[] options, boolean addBlank) {
			this(context, options, -1, addBlank);
		}

		public InputSelect(Context context, String[] options, int offset) {
			this(context, options, offset, true);
		}

		public InputSelect(Context context, String[] options, 
				int offset, boolean addBlank) {
			super(context);
			this.addBlank = addBlank;
			this.offset = offset;
			if (offset >= 0) {
				for (int i = 0; i < options.length; i++) {
					options[i] = (i + offset) + " - " + options[i];
				}
			}
			if (addBlank) {
				String[] newOptions = new String[options.length + 1];
				for (int i = 0; i < options.length; i++) {
					newOptions[i+1] = options[i];
				}
				newOptions[0] = "";
				options = newOptions;
			}
			ArrayAdapter<CharSequence> adapter = 
				new ArrayAdapter<CharSequence>(context, R.layout.spintext,
						options);
			adapter.setDropDownViewResource(R.layout.multiline_spinner_dropdown_item);
			setAdapter(adapter);
		}

		@Override
		public void writeParameters(Parameters params) {
			if (offset >= 0) {
				boolean blank = addBlank && 
				getSelectedItemPosition() == 0;
				if (blank) {
					params.addParam("");
					params.addParam(false);
				} else {
					int pos = getSelectedItemPosition() + offset;
					if (addBlank) pos--;
					params.addParam(pos);
					params.addParam(true);
				}
			} else {
				params.addParam(getSelectedItem().toString());
				params.addParam(getSelectedItemPosition());
			}
		}

		@Override
		public void readParameters(Parameters params) {
			if (offset >= 0) {
				if (params.getBoolean(1)) {
					int pos = params.getInt() - offset;
					if (addBlank) pos++;
					setSelection(pos);
				} else {
					setSelection(0);
				}
			} else {
				setSelection(params.getInt(1));
			}
		}

		@Override
		public boolean isSet() {
			return !addBlank || getSelectedItemPosition() > 0;
		}

	}

	public static class InputLabel extends TextView {
		public InputLabel(Context context, String text) {
			super(context);
			setText(text);
			setTextSize(LABEL_SIZE);
		}
	}

	public static class InputLabelFun extends InputLabel {
		public InputLabelFun(Context context, String text) {
			super(context, text);
			//setTypeface(Typeface.createFromAsset(context.getAssets(), "COOPBL.TTF"));
			setPadding(0, 0, 10, 0);
			if (text == null || text.length() == 0) return;
			String html = "";
			StringTokenizer tok = new StringTokenizer(text);
			while (tok.hasMoreTokens()) {
				if (html.length() > 0) html += " ";
				String token = tok.nextToken();
				String letter = token.substring(0, 1);
				if (Character.isUpperCase(letter.charAt(0))) {
					html += "<big>" + letter + "</big>";
					html += token.substring(1);
				} else {
					html += token;
				}
			}
			setText(Html.fromHtml(html));
		}

	}

	public static class InputTitle extends InputLabel {
		public InputTitle(Context context, String text) {
			super(context, text);
			setTextSize(TITLE_SIZE);
		}

	}

	public static class InputFooter extends InputLabel {
		public InputFooter(Context context, String text) {
			super(context, text);
			setTextSize(FOOTER_SIZE);
		}

	}

	public static class InputReadOnly extends InputLabel implements InputView{
		public InputReadOnly(Context context, String text) {
			super(context, text);
			setBackgroundColor(Color.LTGRAY);
			setTextColor(Color.BLACK);
			setPadding(6, 10, 6, 10);
			setGravity(Gravity.CENTER_HORIZONTAL);
		}

		@Override
		public void writeParameters(Parameters params) {
			params.addParam(getText().toString());
		}

		@Override
		public void readParameters(Parameters params) {
			setText(params.getString());
		}

		@Override
		public boolean isSet() {
			return getText().length() > 0;
		}
	}

	public static class InputTemperature extends LinearLayout implements InputView {
		private InputBoundedDecimal seekBar;
		private InputSelect select;
		private int min, max;

		public InputTemperature(Context context, int min, int max) {
			super(context);
			this.min = min;
			this.max = max;

			setOrientation(HORIZONTAL);
			LayoutParams lps = new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			lps.weight = 1;
			lps.rightMargin = 10;
			addView(seekBar = new InputBoundedDecimal(context, min, max), lps);
			addView(select = new InputSelect(context, new String[] {"C", "F"}, false));

			select.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> adapter, View parent,
						int selection, long id) {
					setBounds();
				}

				@Override
				public void onNothingSelected(AdapterView<?> adapter) {	}

			});
		}

		private void setBounds() {
			int selection = select.getSelectedItemPosition();
			if (selection == 0) {
				seekBar.setBounds(InputTemperature.this.min, 
						InputTemperature.this.max);
			} else {
				int min = (int)(InputTemperature.this.min * 1.8 + 32);
				int max = (int)(InputTemperature.this.max * 1.8 + 32);
				seekBar.setBounds(min, max);
			}
		}

		@Override
		public void writeParameters(Parameters params) {
			params.addParam(getTempString());
			Parameters ps = new Parameters();
			seekBar.writeParameters(ps);
			params.addParam(ps);
			params.addParam(select.getSelectedItemPosition());
		}

		@Override
		public void readParameters(Parameters params) {
			select.setSelection(params.getInt(2));
			setBounds();
			Parameters ps = params.getParameters(1);
			seekBar.readParameters(ps);
		}

		private String getTempString() {
			if (seekBar.isSet()) {
				return String.format("%.01f %s", seekBar.getValue(),
						select.getSelectedItem().toString());
			}
			return "";
		}

		@Override
		public boolean isSet() {
			return seekBar.isSet();
		}
	}

	public static class InputWeight extends ExpSeekBar implements InputView {

		public InputWeight(Context context, int min, int max) {
			super(context, min, max);
			setPadding(10, 0, 10, 0);
		}

		@Override
		public void writeParameters(Parameters params) {
			if (hasValue()) {
				params.addParam(getValue());
				params.addParam(true);
			} else {
				params.addParam("");
				params.addParam(false);
			}
		}

		@Override
		public void readParameters(final Parameters params) {
			post(new Runnable() {
				@Override
				public void run() {
					if (params.getBoolean(1)) {
						setValue(params.getDouble());
					} else {
						setValue(false);
					}
				}
			});
		}

		@Override
		public boolean isSet() {
			return hasValue();
		}
	}

	public static class InputBoundedDecimal extends InputLine implements InputView {
		int min, max;
		String lastText;
		int lastSelelection;
		Toast lastToast;

		public InputBoundedDecimal(Context context, int _min, int _max) {
			super(context);
			this.min = _min;
			this.max = _max;
			setInputType(InputType.TYPE_CLASS_NUMBER | 
					InputType.TYPE_NUMBER_FLAG_DECIMAL |
					InputType.TYPE_NUMBER_FLAG_SIGNED);

			setOnFocusChangeListener(new OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					checkText(true, null);
					if (isSet()) setValue(getValue());
				}
			});

			addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					checkText(false, lastText);
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
					lastText = getText().toString();
					lastSelelection = start;
				}

				@Override
				public void afterTextChanged(Editable s) { }
			});
		}

		private void checkText(boolean checkMin, String backup) {
			String s = getText().toString();
			if (s.length() == 0) return;
			double d = parse(s);
			if (d > max || (checkMin && d < min)) {
				int sel = lastSelelection;
				if (backup != null) {
					setText(backup);
				} else {
					setValue(d);
				}
				if (sel >=0 && sel <= getText().length())
					setSelection(sel, sel);
				if (lastToast == null) {
					lastToast =	Toast.makeText(getContext(), 
							String.format("Value should be between %d and %d.", 
									min, max), 
									Toast.LENGTH_SHORT);
				}
				lastToast.show();
			}
		}

		public double getValue() {
			if (!isSet()) {
				throw new RuntimeException("Not Set!");
			}
			return parse(getText().toString());
		}

		private double parse(String s) {
			if (s.contains("-.")) s = s.replace("-.", "-0.");
			try {
				return Double.parseDouble(s);
			} catch (Exception e) {
				Debug.write(e);
			}
			return 0;
		}

		private String format(double d) {
			return format("" + d);
		}

		private String format(String s) {
			if (s.endsWith(".0")) s = s.replace(".0", "");
			return s;
		}

		public void setValue(double value) {
			if (value > max) value = max;
			if (value < min) value = min;
			setText(format(value));
		}

		public void setValue(boolean value) {
			if (!value) {
				setText("");
			}
		}

		public void setBounds(int min, int max) {
			if (isSet()) {
				double value = getValue();
				this.min = min;
				this.max = max;
				setValue(value);
			} else {
				this.min = min;
				this.max = max;
			}
		}

		@Override
		public void writeParameters(Parameters params) {
			String text = getText().toString();
			if (text.length() > 0) {
				params.addParam(parse(getText().toString()));
				return;
			}
			params.addParam("");
		}

		@Override
		public void readParameters(Parameters params) {
			String s = params.getObject().toString();
			setText(format(s));
		}

		@Override
		public boolean isSet() {
			return getText().length() > 0;
		}
	}

	public static class InputSeekbar extends NumberSeekBar implements InputView{
		public InputSeekbar(Context context, int min, int max) {
			super(context, min, max);
			setPadding(10, 0, 10, 0);
		}

		@Override
		public void writeParameters(Parameters params) {
			if (hasValue()) {
				params.addParam(getValue());
				params.addParam(true);
			} else {
				params.addParam("");
				params.addParam(false);
			}
		}

		@Override
		public void readParameters(final Parameters params) {
			post(new Runnable() {
				@Override
				public void run() {
					if (params.getBoolean(1)) {
						setValue(params.getDouble());
					} else {
						setValue(false);
					}
				}
			});
		}

		@Override
		public boolean isSet() {
			return hasValue();
		}
	}

	

		
	public static class InputPicture extends ImageButton implements InputView {

		private String image, oldImage;

		public InputPicture(final Context context) {
			super(context);

			//setPadding(10, 10, 10, 10);
			setImageResource(android.R.drawable.ic_menu_camera);

			setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					oldImage = image;

					String storage = Environment.getExternalStorageDirectory()
					.getAbsolutePath()
					+ "/HERPS/";
					File dir = new File(storage);
					if (!dir.exists()) {
						dir.mkdirs();
					}
					image = storage
					+ System.currentTimeMillis()
					+ ".bmp";

					Intent intent = new Intent(
							android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(image)));
					((Activity)context).startActivityForResult(intent, 1);
				}
			});
		}

		@Override
		public void writeParameters(Parameters params) {
			params.addParam(image);
			params.addParam(oldImage);
		}

		@Override
		public void readParameters(Parameters params) {
			image = params.getString();
			oldImage = params.getString(1);
			if (image != null) {
				File f = new File(image);
				if (!f.exists() && oldImage != null) {
					image = oldImage;
					f = new File(image);
				}
				if (f.exists()) {
					try {
						BitmapFactory.Options options = new Options();
						options.inSampleSize = 8;
						options.outWidth = 100;

						Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
						setImageBitmap(bmp);
					} catch (Exception e) {
						Debug.write(e);
					}
				}
			}
			invalidate();
		}

		@Override
		public boolean isSet() {
			return image != null && new File(image).exists();
		}
	}

	public static class InputDateSelect extends Button implements InputView {

		Date date;

		public InputDateSelect(final Context context) {
			super(context);
			date = new Date();
			setDateText();

			setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					new DatePickerDialog(context, new OnDateSetListener() {
						@Override
						public void onDateSet(DatePicker view, int year, int monthOfYear,
								int dayOfMonth) {
							date.setYear(year - 1900);
							date.setMonth(monthOfYear);
							date.setDate(dayOfMonth);
							setDateText();
						}
					}, date.getYear() + 1900, date.getMonth(), date.getDate()).show();
				}
			});
		}

		@Override
		public void writeParameters(Parameters params) {
			params.addParam(getText());
			params.addParam(date);
		}

		@Override
		public void readParameters(Parameters params) {
			setText(params.getString());
			date = (Date)params.getObject(1);
		}

		private void setDateText() {
			SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy");
			setText(format.format(date));
		}

		@Override
		public boolean isSet() {
			return true;
		}

	}

	public static class InputTimeSelect extends Button implements InputView {
		private Date date;

		public void setDate(Date date) { 
			this.date = date;
			setTimeText();
		}

		public InputTimeSelect(Context context) {
			super(context);

			date = new Date();
			setTimeText();

			setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					new TimePickerDialog(getContext(), new OnTimeSetListener() {

						@Override
						public void onTimeSet(TimePicker view, int hourOfDay,
								int minute) {
							date.setHours(hourOfDay);
							date.setMinutes(minute);
							setTimeText();
						}

					}, date.getHours(), date.getMinutes(), false).show();
				}
			});
		}

		@Override
		public void writeParameters(Parameters params) {
			params.addParam(getText());
			params.addParam(date);
		}

		@Override
		public void readParameters(Parameters params) {
			setText(params.getString());
			date = (Date)params.getObject(1);
		}	

		private void setTimeText() {
			SimpleDateFormat format = new SimpleDateFormat("h:mm aa");
			setText(format.format(date));
		}

		@Override
		public boolean isSet() {
			return true;
		}
	}

	public static class InputDayOfWeek extends InputSelect {
		public InputDayOfWeek(Context context) {
			super(context, getDayOfWeekString(), false);
			int index = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
			setSelection(index);
		}
	}

	public static class InputTurtleId extends Button implements InputView {

		public InputTurtleId(Activity context) {
			super(context);
			setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					View view = ((Activity)getContext()).getLayoutInflater()
					.inflate(R.layout. box_turtle_id_dialog, null);


					final EditText editTextId = (EditText)view.findViewById(R.id.editTextTurtleId);
					editTextId.setText(InputTurtleId.this.getText());

					AlertDialog alert = new AlertDialog.Builder(getContext())
					.setView(view)
					.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							InputTurtleId.this.setText(editTextId.getText()
									.toString().toUpperCase());
						}
					})
					.setNegativeButton("Cancel", null)
					.show();

					final Button okButton = alert.getButton(AlertDialog.BUTTON_POSITIVE);
					okButton.setEnabled(editTextId.getText().length() == 3);

					editTextId.addTextChangedListener(new TextWatcher() {
						@Override
						public void onTextChanged(CharSequence s, int start, int before, int count) {
							okButton.setEnabled(s.length() == 3);
						}

						@Override
						public void beforeTextChanged(CharSequence s, int start, int count,
								int after) { }

						@Override
						public void afterTextChanged(Editable s) { }
					});

				}
			});
		}
		
		@Override
		public void writeParameters(Parameters params) {
			params.addParam(getText().toString());
		}

		@Override
		public void readParameters(Parameters params) {
			setText(params.getString());

		}

		@Override
		public boolean isSet() {
			return getText().length() > 0;
		}

	}
	

	/**
	 * Returns string array for whether animal is alive
	 * @return
	 */
	public static String[] getAliveString() {
		return new String[] { "Alive", "Dead" };
	}

	/**
	 * Returns String array on type of capture
	 * @return
	 */
	public static String[] getCaptureString() {
		return new String[] { "Road Capture", "Observed While Mowing",
				"Visual Search (you found the turtle by looking)",
				"Incidental", "Radio Signal", "Dog (a dog located the turtle)",
				"Turtle Trap", "Other" };
	}
	
	public static String[] getSnakeCaptureString() {
		return new String[] { "Road", 
				"Observed while moving",
				"Visual Search hand capture",
				//"", "", "", "",
				"Drift fence funnel trap", 
				"Coverboard plywood",
				"Coverboard tin", "",
				"Aquatic minnow trap", "",
				"Visual seach did not handle",
				"Snake stick", "",
				"Other"};
	}

    public static String[] getMethodMeasuringStrings() {
        return new String[] {"Stretch", "Squeeze Box", "Tube", "Other"};
    }
	
	public static String[] getLizardCaptureString() {
		return new String[] { "Hand", "Lasso", "Other" };
	}

	/**
	 * Returns string array on type of habitat of animal
	 * @return
	 */
	public static String[] getHabitatString() {
		return new String[] { "Road", "Field/Forest Edge (within 6m of edge)",
				"Field", "Pine Forest", "Hardwood Forest",
				"Stream or Stream Bank", "Open Wetland", "Forested Wetland",
				"Lake", "Other" };
	}

	/**
	 * Returns string array of percent clouds in sky
	 * @return
	 */
	public static String[] getSkyIndexString() {
		return new String[] { "0-24% clouds", "25-49% clouds", "50-74% clouds",
		"75-100% clouds" };
	}

	/**
	 * Returns string array of type of sky index
	 * @return
	 */
	public static String[] getSkyCodeString() {
		return new String[] { "Few Clouds",
				"Partly Cloudy (scattered) or variable sky",
				"Cloudy or overcast", "Fog or smoke", "Drizzle or light rain" };
	}

	/**
	 * Returns string array of rain statuses
	 * @return
	 */
	public static String[] getRainStrings() {
		return new String[] { "No Precipitation", "Light Drizzle/Mist", "Rain" };
	}

	/**
	 * Returns string array of wind
	 * @return
	 */
	public static String[] getWindStrings() {
		return new String[] { "Calm (< 1 mph)", "Light Air (1-3 mph)",
				"Light Breeze (4-7 mph), leaves rustle, can feel wind on face",
		"Gentle Breeze (8-12 mph), leaves & twigs move around, small flag extends" };
	}

	/**
	 * Returns string array of gender options
	 * @return
	 */
	public static String[] getGenderStrings() {
		return new String[] { "Male", "Female", "Unable to identify" };
	}

	/**
	 * Returns yes/no string array
	 * @return
	 */
	public static String[] getYesString() {
		return new String[] { "Yes", "No", "Unsure" };
	}

	/**
	 * Returns no/yes string array
	 * @return
	 */
	public static String[] getNoString() {
		return new String[] { "No", "Yes" };
	}

	/**
	 * Returns am/pm string array
	 * @return
	 */
	public static String[] getAmPmString() {
		return new String[] { "AM", "PM" };
	}

	/**
	 * Returns f/c string array
	 * @return
	 */
	public static String[] getTempMeasureString() {
		return new String[] { "F", "C" };
	}

	/**
	 * Returns states string array
	 * @return
	 */
	public static String[] getStatesString() {
		return new String[] { "North Carolina", "Alabama", "Alaska", "Arizona", "Arkansas",
				"California", "Colorado", "Connecticut", "Delaware", "Florida",
				"Georgia", "Hawaii", "Idaho", "Illinois", "Indiana", "Iowa",
				"Kansas", "Kentucky", "Louisiana", "Maine", "Maryland",
				"Massachusetts", "Michigan", "Minnesota", "Mississippi",
				"Missouri", "Montana", "Nebraska", "Nevada", "New Hampshire",
				"New Jersey", "New Mexico", "New York",
				"North Dakota", "Ohio", "Oklahoma", "Oregon", "Pennsylvania",
				"Rhode Island", "South Carolina", "South Dakota", "Tennessee",
				"Texas", "Utah", "Vermont", "Virginia", "Washington",
				"West Virginia", "Wisconsin", "Wyoming" };
	}

	/**
	 * Returns day of week string array
	 * @return
	 */
	private static String[] getDayOfWeekString() {
		return new String[] { "Sunday", "Monday", "Tuesday", "Wednesday",
				"Thursday", "Friday", "Saturday" };
	}

	/**
	 * Returns life stage string array
	 * @return
	 */
	public static String[] getLifeStageString() {
		return new String[] { "Egg", "Hatchling", "Small Juvenile", "Large Juvenile", "Adult", "Unknown" };
	}
	
	public static String[] getLifeStageStringSalamander() {
		return new String[] { "Egg", "Larvae", "Adult" };
	}
	
	public static String[] getLifeStageStringFrog() {
		return new String[] { "Egg", "Tadpole", "Sub-Adult", "Adult" };
	}

	/**
	 * Returns utm zone string array
	 * @return
	 */
	public static String[] getUtmZone() {
		return new String[] { "17", "18" };
	}

	public static String[] getMoonPhases() {
		return new String[] { "New",
				"Waxing Crescent",
				"First Quarter", "Waxing Gibbous",
				"Full", "Waning Gibbous",
				"Last Quarter", "Waning Crescent" };
	}

	public static String[] getConfidenceLevels() {
		return new String[] {
				"Certain",
				"Not Positive",
				"Guessing"
		};
	}

	public static String[] getCallingStrings() {
		return new String[] {
				"Not heard",
				"Individuals can be counted; there is space between calls",
				"Calls of individuals can be distinguished. but there is some overlapping of calls",
				"Full chorus; calls are constant, continuous and overlapping"
		};
	}
	
	public HashMap<String, String> getFrogNames() {
		return getNameMap("calling.txt");
	}
	
	public HashMap<String, String> getSalamanderNames() {
		return getNameMap("salamander.txt");
	}
	
	public HashMap<String, String> getLizardNames() {
		return getNameMap("lizards.txt");
	}

	public HashMap<String, String> getTurtleNames() {
		return getNameMap("aquatic.txt");
	}

	public HashMap<String, String> getCallingNames() {
		return getNameMap("calling.txt");
	}

	public HashMap<String, String> getSnakeNames() {
		return getNameMap("snakes.txt");
	}

	public HashMap<String, String> getNameMap(String file) {
		HashMap<String, String> names = new HashMap<String, String>();
		try {
			Scanner sc = new Scanner(getAssets().open(file));
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				int index = line.indexOf(",");
				String common = line.substring(0, index);
				String sci = line.substring(index + 2);
				names.put(common, sci);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return names;
	}
	
	public static <T> List<T> getKeys(HashMap<T,?> set) {
		LinkedList<T> list = new LinkedList<T>();
		for (T t : set.keySet()) {
			list.add(t);
		}
		return list;
	}

	public static <T> List<T> getValues(HashMap<?,T> set) {
		LinkedList<T> list = new LinkedList<T>();
		for (T t : set.values()) {
			if (!list.contains(t)) {
				list.add(t);
			}
		}
		return list;
	}

	/**
	 * Returns array list of scientific names
	 * @return
	 */
	public static ArrayList<String[]> getSciName() {
		return sciNameArray;
	}


}
