package info.boubakr.ia_01;

import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import info.boubakr.ia_01.info.translation.LanguageCodeHelper;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final static  String TAG = PreferenceActivity.class.getSimpleName();
    public static  final String KEY_TRGET_LANGUAGE_PREFERENCE = "translate_to";
    public static  final String KEY_SOURCE_LANGUAGE_PREFERENCE = "language_to_recongnize";
    public static final String KEY_TOGGLE_TRANSLATION = "preference_translate";
    public static final String KEY_TOGGLE_RECOGNITION= "preference_recongnize";

    public static  SharedPreferences sharedPreferences;


    private ListPreference listPreferenceSourceLanguage;
    private ListPreference listPreferenceTargetLanguage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        listPreferenceSourceLanguage = (ListPreference) getPreferenceScreen().findPreference(KEY_SOURCE_LANGUAGE_PREFERENCE);
        listPreferenceTargetLanguage = (ListPreference) getPreferenceScreen().findPreference(KEY_TRGET_LANGUAGE_PREFERENCE);

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
