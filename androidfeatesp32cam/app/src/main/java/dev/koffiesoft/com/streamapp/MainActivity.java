package dev.koffiesoft.com.streamapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    VideoView videoView;
    Button button;
    ProgressDialog mProgress;
    MediaController mediaController;
    WebView mWebView;

    Button mButtonRefresh;
    Button mButtonCapture;

    final String esp32CAM = "192.168.143.44";
    final String VIDEO_URL = "https://dedykuncoro.com/childrens-song/uploads/videos/japanese_childrens_song_-_okina_kuri_no_ki_no_shita_de.mp4";

    private static final int REQUEST_EXTERNAL_STORAGE=1;
    private static String[] PERMISSION_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        setContentView(R.layout.layout_video_stream);

        videoView = (VideoView) findViewById(R.id.videoView);
        button = (Button) findViewById(R.id.button);

        mButtonRefresh = (Button) findViewById(R.id.buttonRefresh);

        mButtonCapture = (Button) findViewById(R.id.buttonCapture);
        verifyStoragePermission(this);

        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);

        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebView.getSettings().setMixedContentMode(0);
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        String newUA= "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0";
        mWebView.getSettings().setUserAgentString(newUA);

        mWebView.loadUrl(esp32CAM);
    }

    public void buttonClicked(View view) {
    }

    public void buttonRefreshClicked(View view) {
        Toast.makeText(this, "Reload webview", Toast.LENGTH_SHORT).show();
        mWebView.loadUrl(esp32CAM);
    }


    public void buttonCaptureClicked(View view) {
        Log.d("Debug", "Test");
        takeScreenshoot(getWindow().getDecorView().getRootView(), "result");
    }


    protected File takeScreenshoot(View view, String filename) {
        Date date = new Date();
        CharSequence format = DateFormat.format("yyyy-MM-dd_hh:mm:ss", date);

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {

                // UPDATED
                Log.d("debug ", "HERE ");

                mWebView.measure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                mWebView.layout(0, 0, mWebView.getMeasuredWidth(), mWebView.getMeasuredHeight());
                mWebView.setDrawingCacheEnabled(true);

                mWebView.buildDrawingCache();

                Bitmap bm = Bitmap.createBitmap(mWebView.getMeasuredWidth(), mWebView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

                Canvas bigcanvas = new Canvas(bm);
                Paint paint = new Paint();

                Log.d("debug ", "HERE 2");

                int iHeight = bm.getHeight();
                bigcanvas.drawBitmap(bm, 0, iHeight, paint);

                mWebView.draw(bigcanvas);

                String dirPath = getApplicationContext().getFilesDir().getPath();
                File fileDir = new File(dirPath);

                if (!fileDir.exists()) {
                    boolean mkdir = fileDir.mkdir();
                    Log.d("debug", mkdir ? "true" : "false");
                }

                String path = dirPath + "/" + filename + "-" + format + ".jpeg";
                Log.d("info path", path);

//                view.setDrawingCacheEnabled(true);
//                Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
//                view.setDrawingCacheEnabled(false);

                File imageFile = new File(path);

                FileOutputStream fos = new FileOutputStream(imageFile);

                int quality = 100;
//                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);

                bm.compress(Bitmap.CompressFormat.JPEG, quality, fos);
                fos.flush();
                fos.close();

                MediaStore.Images.Media.insertImage(getContentResolver(), bm, "TITLE", "DESC");
                Toast.makeText(this, "Image captured. saved to gallery", Toast.LENGTH_SHORT).show();

                return imageFile;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void verifyStoragePermission(Activity activity) {
        int p = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (p != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSION_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }


}