package info.boubakr.ia_01.info.translation;

import android.os.AsyncTask;
import android.widget.TextView;


import info.boubakr.ia_01.MainActivity;
import info.boubakr.ia_01.R;

/**
 * Created by aboubakr on 06/04/16.
 */
/*
    cette classe va lancer le thread de la translation on faisant appel ala methode
    translate de la classe Translator ....
    il est obligatoiiire de faiire  cette tache dans un thread séparé puisque à partir
    d'API 11 on ne peut pas faire une connexion Internet dans le thread principal
    ;))
 */
public class TranslationAsyncTask extends AsyncTask<String, String, Boolean> {

    private MainActivity mainActivity;
    private TextView translationText;
    private String textSource;
    private String translatedText="";

    //les deux languages pour le moment on suppose que le languege est  le Fr et on on translate a Eng
    private String sourceLanguageCode = "fr";
    private String targetLanguageCode = "en";
    //

    //la methode do in
    @Override
    protected Boolean doInBackground(String... params) {
        translatedText = Translator.translate(sourceLanguageCode,targetLanguageCode,textSource);

        if(translatedText.equals(Translator.BAD_TRANSLATION_MSG))
           return false;


        return true;
    }
    /*
    constructor :)
     */
    public  TranslationAsyncTask(MainActivity mainActivity, String textSource){
        this.mainActivity = mainActivity;
        this.translationText = (TextView) mainActivity.findViewById(R.id.translated_text);
        this.textSource = textSource ;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

        translationText.setText(translatedText);
        /*
        le text de la translation sera apré invisible par défaut et il ne sera visible que si
        la translation est faite  avec succés ... à faire.
         */
    }
}
