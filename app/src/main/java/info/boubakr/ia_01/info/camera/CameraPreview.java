package info.boubakr.ia_01.info.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by aboubakr on 17/04/16.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private String TAG = CameraPreview.class.getName();
    private Camera mCamera;
    private SurfaceHolder mHolder;
    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.mCamera = camera;
        this.mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Camera.Parameters params = mCamera.getParameters();

        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
        {
            params.set("orientation", "portrait");
            mCamera.setDisplayOrientation(90);
        }
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG,"surfaceCreated exception");
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (holder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings

        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
    public void refreshCamera(Camera camera){
        camera = Camera.open();
        if(mHolder.getSurface() == null ){
            return;
        }
        camera.stopPreview();
        setCamera(camera);
    }

    public void setCamera(Camera camera) {
        this.mCamera = camera;
    }
}
