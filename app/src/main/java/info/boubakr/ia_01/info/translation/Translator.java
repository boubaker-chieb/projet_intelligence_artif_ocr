package info.boubakr.ia_01.info.translation;

import android.util.Log;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

/**
 * Created by aboubakr on 06/04/16.
 */
public class Translator {

    public static final String BAD_TRANSLATION_MSG = "Translation unavailable";
    private static final String TAG = Translator.class.getSimpleName();
    private static final String CLIENT_ID = "MDEVTRANSLATOR"; //id client
    private static final String CLIENT_SECRET = "eu+tTDVfPYatNCJLXlrOjJcwQqaG/2DqGelaLf78n0w="; // key de notre applivction dans la platforme Microsoft

    static String translate( String sourceLanguageCode, String targetLanguageCode, String sourceText) {

        Translate.setClientId(CLIENT_ID);
        Translate.setClientSecret(CLIENT_SECRET);

        try {
            Log.d(TAG, sourceLanguageCode + " -> " + targetLanguageCode);
            Log.d(TAG, sourceText);
            return  Translate.execute(sourceText, Language.ENGLISH, Language.FRENCH);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Translator.BAD_TRANSLATION_MSG;
    }

    private Translator() {

    }

}
