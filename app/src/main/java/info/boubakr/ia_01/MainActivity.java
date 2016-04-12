package info.boubakr.ia_01;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.IOException;

import info.boubakr.ia_01.info.ocr.InitOCRAsyncTask;
import info.boubakr.ia_01.info.ocr.OcrOperation;
import info.boubakr.ia_01.info.translation.TranslationAsyncTask;


public class MainActivity extends AppCompatActivity {

    //ints
    private int ocrEngineMode = TessBaseAPI.OEM_TESSERACT_ONLY;

    //Strings[]
    static final String[] CUBE_SUPPORTED_LANGUAGES = {"eng","fr","ara"};
    private static final String[] CUBE_REQUIRED_LANGUAGES = {"ara"};

    //booleans
    boolean hidden = true;



    private boolean isEngineReady;
    //Strings


    public static final String DEFAULT_SOURCE_LANGUAGE_CODE = "eng";/**  code ISO 639-1  de la language source*/
    public static final String DEFAULT_TARGET_LANGUAGE_CODE = "fr";    /**  code ISO 639-1  de la language destinition*/

    private final static  String TAG = MainActivity.class.getSimpleName();
    public static final String OSD_FILENAME_BASE = "osd.traineddata";
    public static final String DOWNLOAD_BASE = "http://tesseract-ocr.googlecode.com/files/";
    public static final String OSD_FILENAME = "tesseract-ocr-3.01.osd.tar";
    private static final String  DIRNAME = "/tessdata";
    private String recongnizedText;
    private String DATA_PATH = Environment.getExternalStorageDirectory().getPath();
    private String lang;

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
    //les autres objets

    private TessBaseAPI baseApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        lang = "eng";
        appContext = getApplicationContext();
        baseApi = new TessBaseAPI();
        capture = (ImageButton) findViewById(R.id.button_capture);
        image = (ImageView) findViewById(R.id.result);

        menu = (LinearLayout) findViewById(R.id.reveal_items);
        settings = (ImageButton) findViewById(R.id.settings);
        help = (ImageButton) findViewById(R.id.Help);
        this.resultOCR = (TextView)findViewById(R.id.detection_result);

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 0);
            }
        });
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

                // TODO
                /*
                La dernière langage utilisé par l'aplication sera enregistré dans un shared settings
                si le language courant est le que le language de la dernière utilisation de l'appppppp
                on passse directement à la reconnaissance si nn on initialise l'ocr ...
                 */
                baseApi = new TessBaseAPI();
                initOcrIngine(Environment.getExternalStorageDirectory(),lang,"eng");
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
    public void startOcr() {
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

    //initialiser ocr

    private void  initOcrIngine(File storageRoot, String languageCode, String languageName) {
            isEngineReady = false;
        if(dialog != null){
            dialog.dismiss();
        }
        dialog = new ProgressDialog(this);
        // si on a un lannguege qui marche on utulisant cube seulement on change le mmode à cube
        if (ocrEngineMode != TessBaseAPI.OEM_CUBE_ONLY) {
            for (String s : CUBE_REQUIRED_LANGUAGES) {
                if (s.equals(languageCode)) {
                    ocrEngineMode = TessBaseAPI.OEM_CUBE_ONLY;
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    //prefs.edit().putString(PreferencesActivity.KEY_OCR_ENGINE_MODE, getOcrEngineModeName()).commit();
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
}
