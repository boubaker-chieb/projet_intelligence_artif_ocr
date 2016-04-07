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

    private static final String CLIENT_ID = "MDEVTRANSLATOR";
    private static final String CLIENT_SECRET = "eu+tTDVfPYatNCJLXlrOjJcwQqaG/2DqGelaLf78n0w=";

    static String translate( String sourceLanguageCode, String targetLanguageCode, String sourceText) {
        /*
                targetLanguageCode &&  sourceLanguageCode  va nous aidez par la suite à récupéré les préférence
                de l'utilisateur choisis dans un menu (à faire)
                mais pour le moment on précise les languge manuellement dans le code... :/

                ====> Il noous reste la detection de l'ecriture puis lié l'ensemble :D !
         */
        Translate.setClientId(CLIENT_ID);
        Translate.setClientSecret(CLIENT_SECRET);

        try {
            Log.d(TAG, sourceLanguageCode + " -> " + targetLanguageCode);
            return  Translate.execute(sourceText, Language.FRENCH,
                    Language.ENGLISH);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Translator.BAD_TRANSLATION_MSG;
    }

    private Translator() {
    }

}
