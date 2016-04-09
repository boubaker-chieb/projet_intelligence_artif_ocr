package info.boubakr.ia_01;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.IOException;

import info.boubakr.ia_01.info.ocr.InitOCRAsyncTask;
import info.boubakr.ia_01.info.ocr.OcrOperation;
import info.boubakr.ia_01.info.translation.TranslationAsyncTask;


public class MainActivity extends AppCompatActivity {

    private ProgressDialog dialog; // for initOcr - language download & unzip
    private ProgressDialog indeterminateDialog;

    static final String[] CUBE_SUPPORTED_LANGUAGES = {"eng","fr","ara"};
    private static final String[] CUBE_REQUIRED_LANGUAGES = {"ara"};

    private final static  String TAG = MainActivity.class.getSimpleName();
    public static final String OSD_FILENAME_BASE = "osd.traineddata";
    public static final String DOWNLOAD_BASE = "http://tesseract-ocr.googlecode.com/files/";
    public static final String OSD_FILENAME = "tesseract-ocr-3.01.osd.tar";
    private static final String  DIRNAME = "/tessdata";
    private int ocrEngineMode = TessBaseAPI.OEM_TESSERACT_ONLY;
    private String recongnizedText;
    private String DATA_PATH = Environment.getExternalStorageDirectory().getPath();

    private TessBaseAPI baseApi;

    private boolean isEngineReady;

    private ImageButton capture;
    private ImageView image;
    private  Bitmap bitmap;
    private TextView resultOCR;
    public static Context appContext;
    private String lang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        lang = "eng";
        appContext = getApplicationContext();
        baseApi = new TessBaseAPI();
        capture = (ImageButton) findViewById(R.id.button_capture);
        image = (ImageView) findViewById(R.id.result);
        this.resultOCR = (TextView)findViewById(R.id.detection_result);

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch( resultCode )
        {
            case 0:
                Log.i("Alert", "Cant access to camera");
                break;

            case -1:
                bitmap = (Bitmap) data.getExtras().get("data");
                image.setImageBitmap(bitmap);

               // startOcr(bitmap, lang);
                baseApi = new TessBaseAPI();
                new InitOCRAsyncTask(this, lang,baseApi, ocrEngineMode).execute();
                break;
        }




    }

    public void startOcr(Bitmap bitmap, String lang){

        createDir(); // créer le repertoire tessdata.

        boolean isOcrReady = isOCRReady(lang);

        //si l'ocr n'est pas prés à cause de manque de fichiers necéssaire, on fait le téléchargement de ces fichiers.....
        if(!isOcrReady){

        }

    }
    private boolean isOCRReady(String lang){

        if(!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()){
            Toast.makeText(MainActivity.this, "file not exist", Toast.LENGTH_SHORT).show();
            return  false;
        }
        Toast.makeText(MainActivity.this, "File existe", Toast.LENGTH_SHORT).show();
        return  true;
    }
    public  File createDir() {
        File DIR=null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            DIR=new File(Environment.getExternalStorageDirectory()+DIRNAME);
        else
            DIR=this.getApplicationContext().getCacheDir();
        if(!DIR.exists())
            DIR.mkdirs();
        return DIR;
    }

    //// Appel de la classe OcrOperation qui est responsable de faire le precessus de reconnaissance .. 5edma ndhifa :3
    public void resumeOcr() {
        try {
            OcrOperation ocrOperation = new OcrOperation(bitmap,DATA_PATH,"eng",baseApi);
            ocrOperation.runOCR();
            recongnizedText = ocrOperation.getRecognizedText();
            this.resultOCR.setText(recongnizedText);
            runTranslation();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "L'ocr ma mchech :(");
            Toast.makeText(MainActivity.this, "Faild to do the recongnition !", Toast.LENGTH_SHORT).show();
        }
    }
    //lancer la translation
    private void runTranslation() {
        new TranslationAsyncTask(this,recongnizedText, lang, "fr").execute();
    }
}
