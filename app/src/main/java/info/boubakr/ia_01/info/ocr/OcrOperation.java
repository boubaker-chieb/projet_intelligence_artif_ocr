package info.boubakr.ia_01.info.ocr;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.IOException;

import info.boubakr.ia_01.MainActivity;

/**
 * Created by aboubakr on 06/04/16.
 */
public class OcrOperation {

    private Bitmap bitmap; //image surlaquelle on va faire le reconnassance de text
    private String recognizedText; //Le text rconnue
    private String path; //Stockage du device
    private String languageCode; // code ISO du language !
    private  TessBaseAPI baseAPI; //La clase qui fait la reconnaissance.
    private MainActivity mainActivity;
    private byte[] data;
    // constructeur ;)
    public OcrOperation(Bitmap bitmap , String path, String languageCode, TessBaseAPI baseAPI,MainActivity mainActivity,byte[] data){
        this.bitmap = bitmap ;
        this.path = path;
        this.languageCode = languageCode;
        this.baseAPI = baseAPI;
        this.mainActivity=mainActivity;
        this.data = data;
    }

    public void runOCR() throws IOException{
        handleBitmap(bitmap);
        baseAPI.setDebug(true);
        baseAPI.init(path, languageCode);
        if(bitmap == null) Log.d("TAG", "bitmap null*********************************************************************");
        baseAPI.setImage(bitmap);
        recognizedText = baseAPI.getUTF8Text();
        baseAPI.end();
    }

    public String getRecognizedText() {
        return recognizedText;
    }

    public void handleBitmap(Bitmap image) {
        int w = image.getWidth(), h = image.getHeight();
        int[] rgb = new int[w * h];
        byte[] yuv = new byte[w * h];

        image.getPixels(rgb, 0, w, 0, 0, w, h);
        populateYUVLuminanceFromRGB(rgb, yuv, w, h);
    }

    // Inspired in large part by:
// http://ketai.googlecode.com/svn/trunk/ketai/src/edu/uic/ketai/inputService/KetaiCamera.java
    private void populateYUVLuminanceFromRGB(int[] rgb, byte[] yuv420sp, int width, int height) {
        for (int i = 0; i < width * height; i++) {
            float red = (rgb[i] >> 16) & 0xff;
            float green = (rgb[i] >> 8) & 0xff;
            float blue = (rgb[i]) & 0xff;
            int luminance = (int) ((0.257f * red) + (0.504f * green) + (0.098f * blue) + 16);
            yuv420sp[i] = (byte) (0xff & luminance);
        }
    }
}
