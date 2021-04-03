package smallville7123.example.photocrop;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ReceivedSendDataActivity extends AppCompatActivity {

    private static final String TAG = "ReceivedSendDataActivity";

    Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PhotoCropImageView photoCropImageView = new PhotoCropImageView(this);
        setContentView(photoCropImageView);
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        Log.d(TAG, "intent = [" + (intent) + "]");
        Log.d(TAG, "action = [" + (action) + "]");
        Log.d(TAG, "type = [" + (type) + "]");

        if (type != null) {
            boolean isSend = Intent.ACTION_SEND.equals(action);
            boolean isSendMultiple = Intent.ACTION_SEND_MULTIPLE.equals(action);
            if (isSend || isSendMultiple) {
                if (type.startsWith("image/")) {
                    editor = new Editor(this);
                    editor.setImageView(photoCropImageView, this);
                    if (isSend) {
                        // Handle single image being sent
                        handleSendImage(intent);
                    } else {
                        // Handle multiple images being sent
                        handleSendMultipleImages(intent);
                    }
                    return;
                }
            }
        }
        finishAndRemoveTask();
    }

    void handleSendImage(Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            editor.loadUriWithPermissionCheck(this, intent, imageUri, 1);
        }
    }

    void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
//            editor.loadUris(imageUris);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        editor.onRequestPermissionsResult(requestCode, grantResults);
    }
}