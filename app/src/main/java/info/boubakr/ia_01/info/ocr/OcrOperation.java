package info.boubakr.ia_01.info.ocr;

import android.graphics.Bitmap;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.IOException;

/**
 * Created by aboubakr on 06/04/16.
 */
public class OcrOperation {

    private Bitmap bitmap; //image surlaquelle on va faire le reconnassance de text
    private String recognizedText; //Le text rconnue
    private String path; //Stockage du device
    private String languageCode; // code ISO du language !
    private  TessBaseAPI baseAPI; //La clase qui fait la reconnaissance.

    // constructeur ;)
    public OcrOperation(Bitmap bitmap , String path, String languageCode, TessBaseAPI baseAPI){
        this.bitmap = bitmap ;
        this.path = path;
        this.languageCode = languageCode;
        this.baseAPI = baseAPI;
    }

    public void runOCR() throws IOException{
      baseAPI.setDebug(true);
        baseAPI.init(path, languageCode);
        baseAPI.setImage(bitmap);
        recognizedText = baseAPI.getUTF8Text();
        baseAPI.end();
    }

    public String getRecognizedText() {
        return recognizedText;
    }
}
