package edu.elon.herps;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import edu.elon.herps.EntryForm.DataEntry;
import edu.elon.herps.UploadData.Picture;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class Upload extends Activity {

	private final static String UPLOAD_URL = HERPS.WEB_ROOT + "upload";
	
	private LinearLayout layoutUploads;
	private Button buttonGo;
	private TextView textViewStatus;
	private ProgressBar progressBar;

	private boolean uploading;
	private LinkedList<UploadData> uploads;

	private Handler handler = new Handler();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upload);

		layoutUploads =	(LinearLayout)findViewById(R.id.linearLayoutUploads);
		progressBar = (ProgressBar)findViewById(R.id.progressBar);
		textViewStatus = (TextView)findViewById(R.id.textViewUploading);
		buttonGo = (Button)findViewById(R.id.buttonGo);

		populateUploads();

		buttonGo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!uploading) {
					verifyUser();
				} else {
					stopUpload();
				}
			}
		});

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Delete Selected").setOnMenuItemClickListener(
				new MenuItem.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				new AlertDialog.Builder(Upload.this).setTitle("Delete?")
				.setMessage("This will delete any submissions you have not yet uploaded. Proceed?")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						for (int i = 0; i < layoutUploads.getChildCount(); i++) {
							if (((CheckBox)layoutUploads.getChildAt(i)).isChecked()) {
								UploadData data = uploads.get(i);
								deleteFile(data.fileName);
							}	
						}
						populateUploads();
					}
				})
				.setNegativeButton("No", null)
				.show();
				return true;
			}
		});
		
		return true;
	}
	
	private void verifyUser() {
		int toUpload = 0;
		for (int i = 0; i < layoutUploads.getChildCount(); i++) {
			if (((CheckBox)layoutUploads.getChildAt(i)).isChecked()) {
				toUpload++;
			}	
		}
		if (toUpload == 0) return;
		
		LayoutInflater inflator = getLayoutInflater();
		View view = inflator.inflate(R.layout.user_verify, null);
		final EditText editTextName = (EditText)view.findViewById(R.id.editTextName);
		final EditText editTextEmail = (EditText)view.findViewById(R.id.editTextEmail);

		final AlertDialog alert = new AlertDialog.Builder(this)
		.setView(view)
		.setTitle("Uploader Info")
		.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(editTextName.getWindowToken(), 0);
				imm.hideSoftInputFromWindow(editTextEmail.getWindowToken(), 0);

				String name = editTextName.getText().toString();
				setName(name);
				String email = editTextEmail.getText().toString();
				setEmail(email);
				startUpload(name, email);
			}
		}).setNegativeButton("Cancel", null)
		.create();
		
		editTextEmail.setText(getEmail());
		editTextName.setText(getName());
		
		TextWatcher watcher = new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Button button = alert.getButton(AlertDialog.BUTTON_POSITIVE);
				String name = editTextName.getText().toString();
				String email = editTextEmail.getText().toString(); 
				button.setEnabled(
						email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")
						&& name.length() > 0);
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) { }
			
			@Override
			public void afterTextChanged(Editable s) { }
		};

		editTextName.addTextChangedListener(watcher);
		editTextEmail.addTextChangedListener(watcher);
		
		alert.show();
		watcher.onTextChanged(null, 0, 0, 0);
	}
	
	private String getEmail() {
		SharedPreferences sps = getPreferences(MODE_PRIVATE);
		return sps.getString("email", "");
	}
	
	private void setEmail(String email) {
		SharedPreferences sps = getPreferences(MODE_PRIVATE);
		sps.edit().putString("email", email).commit();
	}
	
	private String getName() {
		SharedPreferences sps = getPreferences(MODE_PRIVATE);
		return sps.getString("name", "");
	}
	
	private void setName(String name) {
		SharedPreferences sps = getPreferences(MODE_PRIVATE);
		sps.edit().putString("name", name).commit();
	}

	private void populateUploads() {
		layoutUploads.removeAllViews();

		String[] files = fileList();
		uploads = new LinkedList<UploadData>();
		for (String file : files) {
			if (file.endsWith(".data")) {
				try {
					ObjectInputStream ois = new ObjectInputStream(openFileInput(file));
					UploadData upload = (UploadData)ois.readObject();
					uploads.add(upload);
					upload.fileName = file;

					SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy h:mm aa");
					String name = String.format("%s (%s)",
							(String)upload.get(0).value,
							df.format(upload.get(1).value));
					CheckBox cb = new CheckBox(this);
					cb.setText(name);
					cb.setChecked(true);
					layoutUploads.addView(cb);
				} catch (Exception e) { }
			}
		}
		
		if (uploads.isEmpty()) {
			cleanup();
		}
	}
	
	private void cleanup() {
		String storage = Environment.getExternalStorageDirectory()
		.getAbsolutePath() + "/HERPS/";
		File path = new File(storage);
		if (path.exists()) {
			for (String file : path.list()) {
				new File(storage + file).delete();
				Debug.write(storage + file);
			}
		}
	}

	private void startUpload(String name, String email) {
		LinkedList<UploadData> toUpload = 
			new LinkedList<UploadData>();
		for (int i = 0; i < layoutUploads.getChildCount(); i++) {
			if (((CheckBox)layoutUploads.getChildAt(i)).isChecked()) {
				toUpload.add(uploads.get(i));
				uploads.get(i).add(2, new NameValuePair("Uploader Email", email));
				uploads.get(i).add(2, new NameValuePair("Uploader Name", name));
			}	
			layoutUploads.getChildAt(i).setEnabled(false);
		}

		if (toUpload.size() > 0) {
			uploading = true;
			buttonGo.setText("Stop Upload");
			doUploads(toUpload);
		}
	}

	private void stopUpload() {
		uploading = false;
		buttonGo.setText("Go");
		textViewStatus.setText("");
		progressBar.setProgress(0);
		populateUploads();
	}

	public void doUploads(final LinkedList<UploadData> toUpload) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < toUpload.size(); i++) {
					final UploadData data = toUpload.get(i);
					final int fi = i;
					if (!uploading) {
						return;
					}
					handler.post(new Runnable() {
						@Override
						public void run() {
							String text = String.format("Uploading entry %d/%d: %s",
									fi + 1, toUpload.size(), data.get(0).value);
							textViewStatus.setText(text);
							progressBar.setMax(toUpload.size());
							progressBar.setProgress(fi);
						}
					});
					boolean success = upload(data);
					if (success) {
						deleteFile(data.fileName);
					} else {
						handler.post(new Runnable() {
							@Override
							public void run() {
								uploadFailed();
							}
						});
						return;
					}
				}
				handler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(Upload.this, "Upload Successful!", 
								Toast.LENGTH_LONG).show();
						stopUpload();
					}
				});
			}
		});
		thread.start();
	}

	private void uploadFailed() {
		Toast.makeText(this, "Upload Failed...", Toast.LENGTH_SHORT).show();
		stopUpload();
	}


	@Override
	public void onBackPressed() {
		if (!uploading) {
			super.onBackPressed();
		}
	}

	private boolean upload(UploadData data) {
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(UPLOAD_URL);
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		ByteArrayOutputStream boas = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(boas);
			oos.writeObject(data);
			byte[] bytes = boas.toByteArray();
			ByteArrayBody body = new ByteArrayBody(bytes, "filename"); 
			entity.addPart("byteArray", body);

			LinkedList<String> pictures = new LinkedList<String>();

			for (NameValuePair nvp : data) {
				if (nvp.value instanceof Picture) {
					String fileName = ((Picture)nvp.value).file;
					if (fileName != null && new File(fileName).exists()) {
						pictures.add(fileName);
					} else {
						nvp.value = null;
					}
					
				}
				Debug.write("%s: %s", nvp.name, nvp.value);
			}

			for (String fileName : pictures) {
				
				// picture might be too big -- resize and compress
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				Bitmap bm = BitmapFactory.decodeFile(fileName);
				int height = bm.getHeight();
				int width = bm.getWidth();
				if (height > 1024 || width > 1024) {
					height /= 2;
					width /= 2;
				}
				Bitmap resized = Bitmap.createScaledBitmap(bm, width, height, false);
				resized.compress(CompressFormat.JPEG, 60, bos);
				ContentBody mimePart = new ByteArrayBody(bos.toByteArray(), fileName);
				
				// attach to the multipart
				entity.addPart(new FormBodyPart(fileName, mimePart));
				Debug.write(fileName);
			}

			post.setEntity(entity);
			String response = EntityUtils.toString(client.execute(post).getEntity());
			
			Log.d("upload", "response = " + response);
			
			if (response.startsWith("true")) {
				for (String fileName : pictures) {
					File file = new File(fileName);
					file.delete();
				}
				return true;
			}
		} catch (Exception e) {
			Debug.write(e);
		}

		return false;
	}
}
