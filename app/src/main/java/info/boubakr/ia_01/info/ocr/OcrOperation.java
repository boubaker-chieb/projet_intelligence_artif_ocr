package info.boubakr.ia_01.info.ocr;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;


import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.IOException;

/**
 * Created by aboubakr on 06/04/16.
 */
public class OcrOperation {
   // ExternelStorage es = new ExternelStorage();
    //String path;
    Bitmap bitmap;
    String recognizedText;
    public OcrOperation(Bitmap bitmap){
        this.bitmap = bitmap ;
        //this.path = path;

    }
    public void runOCR() throws IOException{

        TessBaseAPI baseAPI = new TessBaseAPI();
        baseAPI.setDebug(true);
        baseAPI.init("/mnt/sdcard/tesseract/tessdata/eng.traineddata", "eng");
        baseAPI.setImage(bitmap);
        recognizedText = baseAPI.getUTF8Text();
        baseAPI.end();


    }

    public String getRecognizedText() {
        return recognizedText;
    }
}
