package info.boubakr.ia_01.info.ocr;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.xeustechnologies.jtar.TarEntry;
import org.xeustechnologies.jtar.TarInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import info.boubakr.ia_01.MainActivity;
import info.boubakr.ia_01.R;

/**
 * Created by aboubakr on 08/04/16.
 *
 */
public class InitOCRAsyncTask extends AsyncTask<String, String, Boolean> {

    private ProgressBar progressBar;
    private ProgressDialog dialog;
    private MainActivity mainActivity;
    private Context context;
    private TessBaseAPI baseAPI;
    private int ocrEngineMode;
    private TextView resultOCR;
    private final static  String TAG = InitOCRAsyncTask.class.getSimpleName();
    String languageCode; // code de le langue qu'on vent télécharger ses fichier
    String DATA_PATH = android.os.Environment.getExternalStorageDirectory().getPath(); //path du repertoire sous lequel on va faire le stockage

    private String[] CUBE_DATA_FILES = {    //Suffixes of required date files Cube

            ".cube.bigrams",
            ".cube.fold",
            ".cube.lm",
            ".cube.nn",
            ".cube.params",
            ".cube.size", // not avalable for hindiii
            ".cube.word-freq",
            ".tessract_cube.nn",
            ".traineddata"
    } ;
    //Constructeur
    public InitOCRAsyncTask(MainActivity mainActivity, String languageCode, TessBaseAPI baseAPI, int ocrEngineMode){

        this.mainActivity = mainActivity;
        this.languageCode = languageCode;
        this.baseAPI = baseAPI;
        this.ocrEngineMode = ocrEngineMode;
        this.context = mainActivity.getBaseContext();
        this.progressBar = (ProgressBar) mainActivity.findViewById(R.id.telechargement);
        this.resultOCR = (TextView) mainActivity.findViewById(R.id.detection_result);
        dialog = new ProgressDialog(mainActivity);

    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        progressBar.setVisibility(View.INVISIBLE);
        dialog.dismiss();
        if(result){
          mainActivity.resumeOcr();
        }
        else
        {
            Toast.makeText(mainActivity, "No internet acses plz enable network and restart this app ^^", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected Boolean doInBackground(String... params) {
        boolean isCubeSupported = false;
        String destinationFilenameBase = "tesseract-ocr-3.02." + languageCode + ".tar";
        //vérifier l'existance de repertoire tessdata et e créer si nécessaire
        File tessdataDir = new File(DATA_PATH + File.separator + "tessdata");
        if(!tessdataDir.exists() && !tessdataDir.mkdirs()){
            Log.e(TAG,"Couldnt make directory"+tessdataDir);
            return  false;
        }
        File downloadFile = new File(tessdataDir, destinationFilenameBase);
        //tawa il faut vérifier un téléchargement *.download incomplet se présente dans le repertoire /tessdata
        File incomplete = new File(tessdataDir, destinationFilenameBase + ".download");
        File tessractTestFile = new File(tessdataDir, languageCode + ".traineddata");

        if(incomplete.exists()){
            incomplete.delete();
            if(tessractTestFile.exists()){
                tessractTestFile.delete();
            }
            deleteCubeDataFiles(tessdataDir);
        }
        //Verifier si les ficher Cube data sont déjas installé
        boolean isAllCubeDataInstalled = false ;
        if(isCubeSupported){
            boolean isAFileMissing  =  false;
            File dataFile;
            for(String s : CUBE_DATA_FILES){
                dataFile = new File(tessdataDir.toString() + File.separator + languageCode +s );
                if(!dataFile.exists()){
                    isAFileMissing = true;
                }
            }
            isAllCubeDataInstalled = !isAFileMissing;
        }
        //si les fichier de la languege ne sont pas présent,,, les   intaller
        boolean installSuccess  = false ;
        if(!tessractTestFile.exists() || (isCubeSupported && !isAllCubeDataInstalled)){
            Log.d(TAG, "Language data for" + languageCode + "not found in" + tessdataDir.toString()) ;
            deleteCubeDataFiles(tessdataDir);

        //verfies les Assets du language , Si il ne sont pas présent on les installe
             try{
                     Log.d(TAG, "cheking for language data (" + destinationFilenameBase+ ".zip) in application assets ... ");
                     installSuccess = installFromAssets(destinationFilenameBase + ".zip", tessdataDir, downloadFile);

             }   catch (IOException e){
                        Log.e(TAG, "IOException", e);
             }  catch (Exception e){
                       Log.e(TAG, "Got exception", e);
             }
                if(!installSuccess){
                    //packge n'est pas trouvé donc on va le télécharger
                    Log.d(TAG,"Downloding " + destinationFilenameBase + ".gz .......") ;
                    try {
                          installSuccess = downloadFile(destinationFilenameBase,downloadFile);
                        if(!installSuccess){
                            Log.d(TAG, "download failed");
                            return false;
                        }
                    }   catch (IOException e){
                        Log.e(TAG, "IOException recieved in doInbackgrounnd. Is a network connection available?");
                         return false;
                    }
                }
                // si on a un archive .tar on le untar
            String extension = destinationFilenameBase.substring(destinationFilenameBase.lastIndexOf('.'),destinationFilenameBase.length());
            if(extension.equals(".tar")){
                try{
                          untar(new File(tessdataDir.toString() + File.separator + destinationFilenameBase), tessdataDir);
                            installSuccess =true ;
                }   catch (IOException e){
                    Log.e(TAG,"untar faild (lel2asaf) ");
                    return  false ;
                }
            }
            else
            {
                      Log.d(TAG, "Language data for " + languageCode + "already installed in" + tessdataDir.toString());
                installSuccess = true;
            }
            // si le fichier osd n'est pas présent on le télécharge :3
            File osdFile = new File(tessdataDir, MainActivity.OSD_FILENAME_BASE);
            boolean osdInstallSuccess = false;

            if(!osdFile.exists()){

                // lawej 3al assets for language data elli mech tinstallihom . si nn téléchargihom :3 :3
               String languageName = "orientation and script detection";
                 try {


                String[] badFiles = {
                    MainActivity.OSD_FILENAME + ".gz.download",
                        MainActivity.OSD_FILENAME + ".gz",
                        MainActivity.OSD_FILENAME
                }  ;

                for(String filename : badFiles){
                    File file = new File(tessdataDir, filename);
                    if(file.exists()){
                        file.delete();
                    }
                }

                Log.d(TAG, "Checking for OSD data( " + MainActivity.OSD_FILENAME_BASE + ".zip) in application assets...");
                //vérification de "osd.traineddata.zip "

                  osdInstallSuccess = installFromAssets(MainActivity.OSD_FILENAME_BASE + ".zip", tessdataDir, new File(MainActivity.OSD_FILENAME));
                } catch(IOException e){
                      Log.e(TAG,"IOException",e)    ;
                }
                ///here
                      if(!osdInstallSuccess){
                          //fichier non packager dans assets, donc on l'install ..  ttrés pénible :3
                           Log.d(TAG, "Downloading " + MainActivity.OSD_FILENAME + ".gz...");
                          try{
                                osdInstallSuccess = downloadFile(MainActivity.OSD_FILENAME, new File(tessdataDir,MainActivity.OSD_FILENAME));
                              if(!osdInstallSuccess){
                                  Log.e(TAG,"Download faild");
                                  return  false;
                              }
                          }   catch (IOException e){
                             Log.e(TAG, "IOException received in doInBackground. Is a network connection available?");
                              return false ;
                          }
                      }
                  //untar le fichier OSD .tar
                try{
                      untar(new File(tessdataDir.toString() + File.separator+MainActivity.OSD_FILENAME),tessdataDir);
                }   catch (IOException e){
                    Log.e(TAG, "Untar failed");
                    return false;
                }
            } else {
                Log.d(TAG,"OSD file already present in " + tessdataDir.toString());
                osdInstallSuccess = true;
            }
            //tuer le progress dialog box .. 3:)
            try {
                           dialog.dismiss();
            }   catch (IllegalArgumentException e)   {

            }
            if (baseAPI.init(DATA_PATH+File.separator,languageCode,ocrEngineMode)){
                return  installSuccess && osdInstallSuccess;
            }

        }
        return false;
    }

    private boolean downloadFile(String destinationFilenameBase, File downloadFile) throws  IOException{
        try{

            return  downloadGzippedFileHttp(new URL(MainActivity.DOWNLOAD_BASE + destinationFilenameBase + ".gz"), downloadFile);
        }   catch (MalformedURLException e){
                 throw  new IllegalArgumentException("Bad URL string");
        }
    }

    private boolean downloadGzippedFileHttp(URL url, File destinationFile) throws IOException{

        //envoyer une requete get pour avooir le fichier
        Log.d(TAG, "Sending GET request to " + url + "....")     ;
        publishProgress("Dowloading data for", languageCode + "..", "0");
        HttpURLConnection urlConnection = null;
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setAllowUserInteraction(false);
        urlConnection.setInstanceFollowRedirects(true);
        urlConnection.setRequestMethod("GET");
        urlConnection.connect();

        if(urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK){
            Log.e(TAG,"did not get HTTP_OK response.");
            Log.e(TAG, "Response code: "+ urlConnection.getResponseCode());
            Log.e(TAG, "Response message: " + urlConnection.getResponseMessage().toString())  ;
            return  false;
        }
        int fileSize = urlConnection.getContentLength();
        InputStream inputStream = urlConnection.getInputStream();
        File tempFile = new File(destinationFile.toString() + ".gz.download");

        // copier le contenue de fichier dans un contenue temporaire
        Log.d(TAG, "Stream download to "+ destinationFile.toString()+ "gz.download....");
        final int BUFFER = 8192;
        FileOutputStream fileOutputStream = null;
        Integer percentComplete;
        int percentCompleteLast = 0;
        try{
            fileOutputStream = new FileOutputStream(tempFile);
        }  catch (FileNotFoundException e)    {
            Log.e(TAG, "Exception received when opening FileOutPutStream.", e);
        }
        int downloaded = 0;
        byte[] buffer = new byte[BUFFER];
        int bufferLengh = 0;
        while((bufferLengh = inputStream.read(buffer,0, BUFFER))>0){
            fileOutputStream.write(buffer, 0, bufferLengh);
            downloaded += bufferLengh;
            percentComplete = (int) ((downloaded/(float)fileSize)*100) ;
            if(percentComplete>percentCompleteLast){
                publishProgress("Downloading data for " + languageCode + " ...", percentComplete.toString());
                percentCompleteLast = percentComplete;
            }
        }
        fileOutputStream.close();
        if(urlConnection != null){
            urlConnection.disconnect();
        }
        //décompression le fichier tompraire et puis le supprimer :o !

        try{

            Log.d(TAG, "Unzipping .....jawek behi ya m3allim  :D ");
            gunzip(tempFile, new File(tempFile.toString().replace(".gz.download","")));
            return  true;

        }   catch (FileNotFoundException e){
                  Log.e(TAG, "File not available for unzipping.");
        }   catch (IOException e){
            Log.e(TAG, "Problem unzipping file.");
        }
         return false;
    }
       //incompress
    private void gunzip(File zippedFile, File outFilePath) throws  FileNotFoundException,IOException{
        int uncompressedFileSize = getGzipSizeUncompressed(zippedFile);
        Integer percentComplete;
        int percentCompleteLast = 0;
        int unzippedBytes = 0;
        final Integer progressMin = 0;
        int progressMax = 100- progressMin;
        publishProgress("Uncomressing data for "+ languageCode + "....",progressMin.toString());

        // Si le fichier est un tar alors afficher le progress bar jusk 50% ^^
           String extension = zippedFile.toString().substring(zippedFile.toString().length() - 16);
        if(extension.equals(".tar.gz.download")) {
            progressMax = 50 ;
        }
        GZIPInputStream gzipInputStream = new GZIPInputStream(new BufferedInputStream(new FileInputStream(zippedFile))) ;
        OutputStream outputStream = new FileOutputStream(outFilePath);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

        final int BUFFER= 8192;
        byte[] data = new byte[BUFFER];
        int len;

        while ((len = gzipInputStream.read(data,0, BUFFER))>0){
            bufferedOutputStream.write(data,0, len);
            unzippedBytes += len;
            percentComplete = (int) ((unzippedBytes/(float)uncompressedFileSize) * progressMax) + progressMin ;

            if(percentComplete > percentCompleteLast ) {
                publishProgress("Uncompressigng data ma3neha for " + languageCode + "... " , percentComplete.toString());
                percentCompleteLast = percentComplete ;
            }
        }
        gzipInputStream.close();
        bufferedOutputStream.flush();
        bufferedOutputStream.close();

        if(zippedFile.exists()) {
            zippedFile.delete();
        }
    }

    private int getGzipSizeUncompressed(File zippedFile) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(zippedFile, "r") ;
        raf.seek(raf.length() - 4);
        int b4 = raf.read();
        int b3 = raf.read();
        int b2 = raf.read();
        int b1 = raf.read();

        raf.close();
        return (b1 << 24) | (b2 << 16) + (b3 << 8) + b4 ;
    }


    private boolean installFromAssets(String sourceFileName, File modelRoot, File destinationFile) throws IOException {
        String extension = sourceFileName.substring(sourceFileName.lastIndexOf('.'),sourceFileName.length());
        try{
               if(extension.equals(".zip")){
                   installZipFromAssets(sourceFileName,modelRoot,destinationFile);

               }   else throw  new IllegalArgumentException("Extension" + extension + "is unsuported.");
        }   catch (FileNotFoundException  e) {
              Log.d(TAG, "Language not packaged in application assets.");
        }
        return false;
    }

    //installer a partir d'un .zip
    private boolean installZipFromAssets(String sourceFileName, File modelRoot, File destinationFile) throws  IOException, FileNotFoundException{
      publishProgress("Uncompressing data for" + languageCode + "...", "0");
        //ouvrire le zip archive
        ZipInputStream inputStream = new ZipInputStream(context.getAssets().open(sourceFileName));
        for (ZipEntry entry = inputStream.getNextEntry(); entry != null; entry = inputStream.getNextEntry()){
            destinationFile = new File(DATA_PATH, entry.getName());
            if(entry.isDirectory()){
                destinationFile.mkdir();
            }else{
                long zippedFileSize = entry.getSize();
                FileOutputStream outputStream = new FileOutputStream(destinationFile);
                final  int BUFFER = 8192;
                BufferedOutputStream bufferedOutputStraem = new BufferedOutputStream(outputStream, BUFFER);
                int unzippedSize = 0;

                int count = 0 ;
                Integer perceCompleteLast = 0;
                Integer percentComplete = 0;
                byte[] data = new byte[BUFFER];
                while ((count = inputStream.read(data,0, BUFFER)) != -1){
                     bufferedOutputStraem.write(data, 0, count);
                    unzippedSize+=count;
                     percentComplete = (int) ((unzippedSize/(long)zippedFileSize)*100);
                    if(percentComplete > perceCompleteLast){
                        publishProgress("Uncomressing data for " + languageCode + " .... ", percentComplete.toString(), "0");
                        perceCompleteLast=percentComplete;
                    }
                }
                bufferedOutputStraem.close();
            }
            inputStream.closeEntry();
        }
        inputStream.close();
        return  true;
    }

    //supprime les cub data
    private void deleteCubeDataFiles(File tessdataDir) {
        File badFile;
        for(String s: CUBE_DATA_FILES){
            badFile = new File(tessdataDir.toString() + File.separator + languageCode + s);
                if(badFile.exists()) {
                           Log.d(TAG, "Delete existing file" + badFile.toString());
                           badFile.delete();
                }
            badFile =  new File(tessdataDir.toString() + File.separator + "tesseract-ocr-3.01." + languageCode + ".tar");
              if(badFile.exists()){
                  Log.d(TAG, "Deleting existing file" + badFile.toString());
                  badFile.delete();
              }
        }
    }
        private void untar(File tarFile, File destinationDir) throws IOException{
             Log.d(TAG, "Untarring...");
              final int uncompressedSize = getTarSizeUncompressed(tarFile);
              Integer percentComplete;
              int percentCompleteLast = 0;
               int unzippedBytes = 0;
               final Integer progressMin = 50;
                final int progressMax = 100 - progressMin;
                 publishProgress("Uncompressing data for " + languageCode + "...",     progressMin.toString());
            //extraire tous les fichiers
               TarInputStream tarInputStream = new TarInputStream(new BufferedInputStream(  new FileInputStream(tarFile)));
                     TarEntry entry;
                   while ((entry = tarInputStream.getNextEntry()) != null) {
                       int len;
                       final int BUFFER = 8192;
                        byte data[] = new byte[BUFFER];
                          String pathName = entry.getName();
                          String fileName = pathName.substring(pathName.lastIndexOf('/'), pathName.length());
                           OutputStream outputStream = new FileOutputStream(destinationDir + fileName);
                           BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
                            Log.d(TAG, "Writing " + fileName.substring(1, fileName.length()) + "...");
                             while ((len = tarInputStream.read(data, 0, BUFFER)) != -1) {
                                bufferedOutputStream.write(data, 0, len);
                                 unzippedBytes += len;
                                     percentComplete = (int) ((unzippedBytes / (float) uncompressedSize) * progressMax)+ progressMin;
                                 if (percentComplete > percentCompleteLast) {
                                         publishProgress("Uncompressing data for " + languageCode+ "...", percentComplete.toString());
                                     percentCompleteLast = percentComplete;
                                 }

                             }
                              bufferedOutputStream.flush();
                       bufferedOutputStream.close();
                   }
                tarInputStream.close();
                if (tarFile.exists()) {
                    tarFile.delete();
                }
        }
         private int getTarSizeUncompressed(File tarFile) throws IOException {
          int size = 0;
          TarInputStream tis = new TarInputStream(new BufferedInputStream( new FileInputStream(tarFile)));
           TarEntry entry;
             while ((entry = tis.getNextEntry()) != null) {
                  if (!entry.isDirectory()) {
                         size += entry.getSize();
                  }
             }
             tis.close();
              return size;
         }
    @Override
    protected void onPreExecute() {
        progressBar.setVisibility(View.VISIBLE);
        dialog.setTitle("Please wait");
        dialog.setMessage("Checking for data installation...");
        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(false);
        dialog.show();
        super.onPreExecute();
    }

}
