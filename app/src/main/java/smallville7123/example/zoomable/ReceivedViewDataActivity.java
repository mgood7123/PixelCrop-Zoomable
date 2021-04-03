package smallville7123.example.zoomable;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class ReceivedViewDataActivity extends AppCompatActivity {

    private static final String TAG = "ReceivedViewDataActivity";

    Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TabbedZoomImageView tabbedZoomImageView = new TabbedZoomImageView(this);
        setContentView(tabbedZoomImageView);
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        Log.d(TAG, "intent = [" + (intent) + "]");
        Log.d(TAG, "action = [" + (action) + "]");
        Log.d(TAG, "type = [" + (type) + "]");

        if (type != null) {
            boolean isView = Intent.ACTION_VIEW.equals(action);
            if (isView) {
                if (type.startsWith("image/")) {
                    editor = new Editor(this);
                    editor.setImageView(tabbedZoomImageView, this);
                    // Handle single image being viewed
                    handleSendView(intent);
                    return;
                }
            }
        }
        finishAndRemoveTask();
    }

    void handleSendView(Intent intent) {
        Uri imageUri = intent.getData();
        if (imageUri != null) {
            editor.loadUriWithPermissionCheck(this, intent, imageUri, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        editor.onRequestPermissionsResult(requestCode, grantResults);
    }
}