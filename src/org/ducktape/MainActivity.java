package org.ducktape;

import org.ducktape.provider.SettingsProvider;
import org.ducktape.provider.SettingsProvider.Constants;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;

public class MainActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener{
	private CheckBoxPreference mApps2SDPrompt;
	private String[] DEFAULT_PROJECTION = new String[] {
			SettingsProvider.Constants._ID,
			SettingsProvider.Constants.KEY,
			SettingsProvider.Constants.VALUE
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.prefs);
		PreferenceScreen prefSet = getPreferenceScreen();
		mApps2SDPrompt = (CheckBoxPreference) prefSet.findPreference("apps2sd_prompt");
		loadValues();
		final PreferenceGroup parentPreference = getPreferenceScreen();
		parentPreference.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
		if (key.equals("apps2sd_prompt")) {
			writeToProvider("apps2sd_prompt",mApps2SDPrompt.isChecked()? "1" : "0");
		}
	}
	
	private void writeToProvider(String key, String value) {
		Log.i("DuckProvider-writetoProvider", ""+key+"=>"+value);

		ContentValues values = new ContentValues();

		values.put(Constants.KEY, key);
		values.put(Constants.VALUE, value);

		int id = getRowId(key);
		if (id == -1){
			getContentResolver().insert(SettingsProvider.CONTENT_URI, values);
		} else {
			final String selection = Constants.KEY + "='" + key + "'";
			Uri rowuri = Uri.parse("content://"+SettingsProvider.AUTHORITY+"/"+SettingsProvider.TABLE_NAME+"/"+id); 
			getContentResolver().update(rowuri, values, selection, null);
		}
	}
	
	private void loadValues() {
		mApps2SDPrompt.setChecked(getInt("apps2sd_prompt",0)!=0);
	}
	
	private int getInt(String key, int def) {
		String value = getString(key);
		try {
            return value != null ? Integer.parseInt(value) : def;
        } catch (NumberFormatException e) {
            return def;
        }
	}
	
	private String getString(String key) {
		String value = null;
        Cursor c = null;
        String selection = "key='"+key+"'";
        try {
        	c = managedQuery(SettingsProvider.CONTENT_URI, DEFAULT_PROJECTION, selection, null, null);
            if (c != null && c.moveToNext()) value = c.getString(2);
        } catch (SQLException e) {
            // return null
        } finally {
            if (c != null) c.close();
        }
        return value;
	}
	
	private int getRowId(String key) {
		String selection = "key='"+key+"'";
		Cursor cur = managedQuery(SettingsProvider.CONTENT_URI, DEFAULT_PROJECTION, selection, null, null);
		cur.moveToFirst();
		if (cur.getCount() == 0) {
			return -1;
		} else {
			return cur.getInt(0);
		}
	}
}
