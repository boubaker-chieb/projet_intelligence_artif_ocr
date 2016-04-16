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

    public static  SharedPreferences sharedPreferences;

    public static ListPreference listPreferenceSourceLanguage;
    public static ListPreference listPreferenceTargetLanguage;
    public static CheckBoxPreference activateTranslation;
    public static CheckBoxPreference activateRecongnition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        listPreferenceSourceLanguage = (ListPreference) getPreferenceScreen().findPreference(KEY_SOURCE_LANGUAGE_PREFERENCE);
        listPreferenceTargetLanguage = (ListPreference) getPreferenceScreen().findPreference(KEY_TRGET_LANGUAGE_PREFERENCE);
        activateRecongnition = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_ACTIVATE_RECOGNITION);
        activateTranslation = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_ACTIVATE_TRANSLATION);
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
