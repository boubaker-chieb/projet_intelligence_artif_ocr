package info.boubakr.ia_01.info.translation;

import android.os.AsyncTask;
import android.widget.TextView;


import info.boubakr.ia_01.MainActivity;
import info.boubakr.ia_01.R;

/**
 * Created by boubakr on 06/04/16.
 */
public class TranslationAsyncTask extends AsyncTask<String, String, Boolean> {

    private MainActivity mainActivity;
    private TextView translationText;
    private String textSource;
    private String translatedText="";

    private String sourceLanguageCode ;
    private String targetLanguageCode ;
    //
    //appeler le translator Microsoft .. j'ai utilisé Microsoft translate au lieu de Google translate car je le déjas tester
    @Override
    protected Boolean doInBackground(String... params) {
        translatedText = Translator.translate(sourceLanguageCode,targetLanguageCode,textSource);
        if(translatedText.equals(Translator.BAD_TRANSLATION_MSG)) return false;
        return true;
    }
    /*
    constructor :3
     */
    public TranslationAsyncTask(MainActivity mainActivity, String textSource,String sourceLanguageCode,String targetLanguageCode){
        this.mainActivity = mainActivity;
        this.translationText = (TextView) mainActivity.findViewById(R.id.translated_text);
        this.textSource = textSource ;
        this.sourceLanguageCode = sourceLanguageCode;
        this.targetLanguageCode = targetLanguageCode;
    }
    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        translationText.setText(translatedText);
    }
}
