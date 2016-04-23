package info.boubakr.ia_01;

import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final static  String TAG = PreferenceActivity.class.getSimpleName();
    public static  final String KEY_TRGET_LANGUAGE_PREFERENCE = "translate_to";
    public static  final String KEY_SOURCE_LANGUAGE_PREFERENCE = "language_to_recognize";
    public static final String KEY_ACTIVATE_TRANSLATION = "preference_translate";
    public static final String KEY_ACTIVATE_RECOGNITION= "preference_recongnize";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if(key.equals(KEY_SOURCE_LANGUAGE_PREFERENCE)) {
            // TODO
        }
        else if(key.equals(KEY_TRGET_LANGUAGE_PREFERENCE)){
          // TODO 
        }

    }
}
