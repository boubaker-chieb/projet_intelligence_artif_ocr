package info.boubakr.ia_01;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import info.boubakr.ia_01.info.camera.CameraPreview;
import info.boubakr.ia_01.info.ocr.InitOCRAsyncTask;
import info.boubakr.ia_01.info.ocr.OcrOperation;
import info.boubakr.ia_01.info.translation.LanguageCodeHelper;
import info.boubakr.ia_01.info.translation.TranslationAsyncTask;


public class MainActivity extends AppCompatActivity{

    //

    private Camera camera;
    private CameraPreview preview;
    private FrameLayout frameLayout;
    private boolean isCaptured = false;
    //
    private SharedPreferences prefs;
    //ints
    private int ocrEngineMode = TessBaseAPI.OEM_TESSERACT_ONLY;

    //Strings[]
    static final String[] CUBE_SUPPORTED_LANGUAGES = {"eng","fr","ara"};
    private static final String[] CUBE_REQUIRED_LANGUAGES = {"ara"};
    private LinearLayout resultatLayout;
    //booleans
    private boolean hidden = true;
    private boolean isEngineReady;
    private boolean isTranslationActive;
    private boolean isRecongnitionActive;
    public  boolean initOcrStarted = false;
    //Strings


    public static final String DEFAULT_SOURCE_LANGUAGE_CODE = "eng";/**  code ISO 639-1  de la language source*/
    public static final String DEFAULT_TARGET_LANGUAGE_CODE = "fra";    /**  code ISO 639-1  de la language destinition*/

    private final static  String TAG = MainActivity.class.getSimpleName();
    public static final String OSD_FILENAME_BASE = "osd.traineddata";
    public static final String DOWNLOAD_BASE = "http://tesseract-ocr.googlecode.com/files/";
    public static final String OSD_FILENAME = "tesseract-ocr-3.01.osd.tar";
    private static final String  DIRNAME = "/tessdata";
    private String recongnizedText;
    private String DATA_PATH = Environment.getExternalStorageDirectory().getPath();
    private String lang;
    private String sourceLanguageCode;
    private String sourceLanguageName;
    //widjets
    private ImageButton capture;
    private ImageView image;
    private  Bitmap bitmap;
    private TextView resultOCR;
    public static Context appContext;
    private ProgressDialog dialog; // for initOcr - language download & unzip
    private ImageButton settings,help;
    private Toolbar toolbar;
    private LinearLayout menu;
    private View.OnClickListener cl;
    private  TextView transaltedText;
    //les autres objets

    private TessBaseAPI baseApi;
    //private String sourceLanguageCodeTranslation;
    private String targetLanguageCodeTranslation;
    private String targetLanguageName;

    private Camera.PictureCallback mPicture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        
        setContentView(R.layout.activity_capture);
        //
        camera = Camera.open();
        preview = new CameraPreview(this,camera);
        frameLayout = (FrameLayout) findViewById(R.id.camera_preview);
        frameLayout.addView(preview);
        //
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        lang = "eng";
        appContext = getApplicationContext();
        baseApi = new TessBaseAPI();
        capture = (ImageButton) findViewById(R.id.button_capture);
        image = (ImageView) findViewById(R.id.result);
        transaltedText = (TextView) findViewById(R.id.translated_text);
        menu = (LinearLayout) findViewById(R.id.reveal_items);
        settings = (ImageButton) findViewById(R.id.settings);
        help = (ImageButton) findViewById(R.id.Help);
        resultOCR = (TextView)findViewById(R.id.detection_result);
        resultatLayout = (LinearLayout) findViewById(R.id.result_layout);
        mPicture =  new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
                if(bitmap == null){
                    Toast.makeText(MainActivity.this, "empty captured image", Toast.LENGTH_SHORT).show();
                }
                image.setImageBitmap(bitmap);
                baseApi = new TessBaseAPI();
                String previousLangugeCodeOcr = sourceLanguageCode;
                getPreferences();
                Log.d(TAG, sourceLanguageCode + "****************************************************");
                if(isRecongnitionActive){
                    if(!isdataExist("tessdata")){
                        setDefaultPreferences();
                        initOcrIngine(Environment.getExternalStorageDirectory(), sourceLanguageCode, sourceLanguageName);
                    }
                    if("".equals( sourceLanguageCode)){
                        setDefaultPreferences();
                        getPreferences();
                        initOcrIngine(Environment.getExternalStorageDirectory(), sourceLanguageCode, sourceLanguageName);
                    }
                    boolean dontInitAgain = sourceLanguageCode.equals(previousLangugeCodeOcr) ;
                    if(!dontInitAgain) {
                        initOcrIngine(Environment.getExternalStorageDirectory(), sourceLanguageCode, sourceLanguageName); //SourceLanguageName n'a pas d'influence sur l'opération, il est utilisé juste pour aire des affichage
                    }else startOcr();
                }else{
                    Toast.makeText(MainActivity.this, "Activer la récognition pour detecter le texte", Toast.LENGTH_LONG).show();
                }


            }
        };
        //boutton capture
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 0);*/
                if(!isCaptured){
                    camera.takePicture(null,null,mPicture);
                    capture.setBackground(MainActivity.this.getResources().getDrawable(R.drawable.reload));
                    resultatLayout.setVisibility(View.VISIBLE);
                    isCaptured = true;
                }
                else {
                    capture.setBackground(MainActivity.this.getResources().getDrawable(R.drawable.shutter));
                    camera.startPreview();
                    resultatLayout.setVisibility(View.INVISIBLE);
                    isCaptured = false;
                }
            }
        });
        //boutton settngs
        cl = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.settings:
                        Snackbar.make(v, "General settings..", Snackbar.LENGTH_SHORT).show();
                        menu.setVisibility(View.INVISIBLE);
                        MainActivity.this.hidden = true;
                        // TODO
                        /*
                        en cliquant sur ce boutton on lance une Activity contenant les paramtres

                         */
                        Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.Help:
                        Snackbar.make(v, "Language settings..", Snackbar.LENGTH_SHORT).show();
                        menu.setVisibility(View.INVISIBLE);
                        MainActivity.this.hidden = true;
                        break;
                }
            }
        };
        settings.setOnClickListener(cl);
        help.setOnClickListener(cl);
        setSupportActionBar(toolbar);
        menu.setVisibility(View.INVISIBLE);
        getPreferences();
    }


    //// Appel de la classe OcrOperation qui est responsable de faire le precessus de reconnaissance .. 5edma ndhifa :3
    public void startOcr() {
        if(!initOcrStarted) {
            try {
                OcrOperation ocrOperation = new OcrOperation(bitmap, DATA_PATH, sourceLanguageCode, baseApi);
                ocrOperation.runOCR();
                recongnizedText = ocrOperation.getRecognizedText();
                this.resultOCR.setText(recongnizedText);
                runTranslation();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "L'ocr ma mchech :(   :o !! !! !!");
                Toast.makeText(MainActivity.this, "Faild to do the recongnition !", Toast.LENGTH_SHORT).show();
            }
        }
    }
    //lancer la translation
    private void runTranslation() {
        getPreferences();
        if(isTranslationActive){
            new TranslationAsyncTask(this,recongnizedText, LanguageCodeHelper.mapLanguageCode(sourceLanguageCode),
                    LanguageCodeHelper.mapLanguageCode(targetLanguageCodeTranslation)).execute();
        }else Toast.makeText(MainActivity.this, "Pensez à activier la translation", Toast.LENGTH_LONG).show();

    }

    //initialiser ocr

    private void  initOcrIngine(File storageRoot, String languageCode, String languageName) {
        //deleteDirContent("tessdata"); // une solution temporaire pour évité lé exception TODO : évité cette solution
        isEngineReady = false;
        initOcrStarted = true;
        if(dialog != null){
            dialog.dismiss();
        }
        dialog = new ProgressDialog(this);
        // si on a un lannguege qui marche on utulisant cube seulement on change le mmode à cube
        if (ocrEngineMode != TessBaseAPI.OEM_CUBE_ONLY) {
            for (String s : CUBE_REQUIRED_LANGUAGES) {
                if (s.equals(languageCode)) {
                    ocrEngineMode = TessBaseAPI.OEM_CUBE_ONLY;
                }
            }
        }
        if (ocrEngineMode != TessBaseAPI.OEM_TESSERACT_ONLY) {
            boolean cubeOk = false;
            for (String s : CUBE_SUPPORTED_LANGUAGES) {
                if (s.equals(languageCode)) {
                    cubeOk = true;
                }
            }
            if (!cubeOk) {
                ocrEngineMode = TessBaseAPI.OEM_TESSERACT_ONLY;
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                //prefs.edit().putString(PreferencesActivity.KEY_OCR_ENGINE_MODE, getOcrEngineModeName()).commit();
            }
        }
        dialog = new ProgressDialog(this);
        dialog.setTitle("Please wait");
        String ocrEngineModeName = getOcrEngineModeName();

        if (ocrEngineModeName.equals("Both")) {
            dialog.setMessage("Initializing Cube and Tesseract OCR engines for " + languageName + "...");
        } else {
            dialog.setMessage("Initializing " + ocrEngineModeName + " OCR engine for " + languageName + "...");
        }
        dialog.setCancelable(false);
       // dialog.show();

        if (ocrEngineMode == TessBaseAPI.OEM_CUBE_ONLY || ocrEngineMode == TessBaseAPI.OEM_TESSERACT_CUBE_COMBINED) {
            Log.d(TAG, "Disabling continuous preview");
        }
        baseApi = new TessBaseAPI();
        new InitOCRAsyncTask(this, languageCode, baseApi, ocrEngineMode)
                .execute(storageRoot.toString());
    }


    //get ocr engne mode
    String getOcrEngineModeName() {
        String ocrEngineModeName = "";
        String[] ocrEngineModes = getResources().getStringArray(R.array.ocrenginemodes);
        if (ocrEngineMode == TessBaseAPI.OEM_TESSERACT_ONLY) {
            ocrEngineModeName = ocrEngineModes[0];
        } else if (ocrEngineMode == TessBaseAPI.OEM_CUBE_ONLY) {
            ocrEngineModeName = ocrEngineModes[1];
        } else if (ocrEngineMode == TessBaseAPI.OEM_TESSERACT_CUBE_COMBINED) {
            ocrEngineModeName = ocrEngineModes[2];
        }
        return ocrEngineModeName;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_menu){
            if(hidden){
                menu.setVisibility(View.VISIBLE);
                hidden = false;
            }
            else {
                menu.setVisibility(View.INVISIBLE);
                hidden = true;
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void setDefaultPreferences(){
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //récupérer les préférences de l'application si'l ne trouve rien il retyourne null
        // une valeur null est utilisé pour tester si c'est  la première fois l'application se lance ou bien
        // s'il y'a perte des valeur enregistré.

        prefs.edit().putString(SettingsActivity.KEY_SOURCE_LANGUAGE_PREFERENCE,null).commit();
        prefs.edit().putString(SettingsActivity.KEY_TRGET_LANGUAGE_PREFERENCE,null).commit();
    }

   public boolean setSourceLanguageCode(String sourceLanguageCode) {
        this.sourceLanguageCode = sourceLanguageCode;
        //sourceLanguageCodeTranslation = LanguageCodeHelper.mapLanguageCode(sourceLanguageCode);
        
        //sourceLanguageName = LanguageCodeHelper.getTranslationLanguageName(this,sourceLanguageCode);
        
        return  true;
    }
    private boolean setTargetLanguageCode(String languageCode) {
        targetLanguageCodeTranslation = languageCode;
       // targetLanguageName = LanguageCodeHelper.getTranslationLanguageName(this, languageCode);
        return true;
    }
    // méthode utilisé pour supprimer le dossier tessdata
    private void deleteDirContent(String dirName){
        File dir = new File(DATA_PATH + File.separator + dirName);
        if(dir.isDirectory()){
            String[] children = dir.list();
            for( int i = 0 ; i < children.length ; i++){
                new File(dir, children[i]).delete();
            }
        }
    }
    //Récupérer les préférerences à partire de SttingsActivity et les enregitré dans des varible de cette activity
  private void  getPreferences(){
      prefs = PreferenceManager.getDefaultSharedPreferences(this);
      PreferenceManager.setDefaultValues(this,R.xml.settings,false);
      setSourceLanguageCode(prefs.getString(SettingsActivity.KEY_SOURCE_LANGUAGE_PREFERENCE,MainActivity.DEFAULT_SOURCE_LANGUAGE_CODE));
      setTargetLanguageCode(prefs.getString(SettingsActivity.KEY_TRGET_LANGUAGE_PREFERENCE,MainActivity.DEFAULT_TARGET_LANGUAGE_CODE));
      isTranslationActive = prefs.getBoolean(SettingsActivity.KEY_ACTIVATE_TRANSLATION,false);
      isRecongnitionActive = prefs.getBoolean(SettingsActivity.KEY_ACTIVATE_RECOGNITION,false);
  }

    public String getSourceLanguageCode() {
        getPreferences();
        return sourceLanguageCode;
    }

    public String getTargetLanguageCodeTranslation() {
        getPreferences();
        return targetLanguageCodeTranslation;
    }
    private boolean isdataExist(String dir){
        File tessdataDir = new File(DATA_PATH + File.separator + dir);
        File traineddata = new File(DATA_PATH + File.separator + dir + File.separator + sourceLanguageCode+".traineddata");
        if(tessdataDir.exists() &&  traineddata.exists())
            return true;

        return false;
    }
    ///*******
    private Bitmap scaleDownBitmapImage(Bitmap bitmap, int newWidth, int newHeight){
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        return resizedBitmap;
    }
}
