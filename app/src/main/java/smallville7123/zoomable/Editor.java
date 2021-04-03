package smallville7123.zoomable;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class Editor {

    private static final String TAG = "Editor";

    private TabbedZoomImageView imageView;
    private final Context mContext;
    private Activity savedActivity;
    private Uri savedUri;
    private Runnable savedOnGranted;
    private int savedRequestCode;

    static String messageIMAGE = "In order to save this image, READ_EXTERNAL_STORAGE permission is required";
    static String messageURI = "The received Uri with Intent.ACTION_VIEW does not have" +
            " the Intent.FLAG_GRANT_READ_URI_PERMISSION flag set.\n\n" +
            "In order to read this URI, READ_EXTERNAL_STORAGE permission is required";

    public Editor(Context context) {
        mContext = context;
    }

    static int REQUEST_CODE_SET_IMAGE = 8888;

    public void setImageView(TabbedZoomImageView imageView, Activity activity) {
        imageView.setOnRequestStoragePermissionCallback(onGranted -> {
            if (activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                showDialog(
                        activity,
                                messageIMAGE + ".\n\n" +
                                "do you want to grant this permission?",
                        "Grant",
                        () -> requestReadStorage(activity, onGranted, REQUEST_CODE_SET_IMAGE),
                        "Cancel",
                        null
                );
            } else {
                showDialog(
                        activity,
                        messageIMAGE + ".\n\n" +
                                "However this permission has already been granted.\n\n" +
                                "Would you like to continue saving the image?",
                        "yes i would",
                        onGranted,
                        "no, i want to cancel",
                        null
                );
            }
        });
        this.imageView = imageView;
    }

    public boolean loadDrawable(Drawable drawable) {
        imageView.setImageDrawable(drawable);
        return drawable != null;
    }

    public boolean loadUri(Uri imageUri) {
        if (imageUri != null) {
            InputStream imageStream;
            try {
                imageStream = mContext.getContentResolver().openInputStream(imageUri);
            } catch (FileNotFoundException e) {
                return false;
            } catch (java.lang.SecurityException e) {
                Log.e(TAG, "loadUri: caught security exception: ", e);
                return false;
            }
            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
            Drawable drawable = new BitmapDrawable(mContext.getResources(), selectedImage);
            loadDrawable(drawable);
            return true;
        }
        return false;
    }

    void showDialog(Context context, String message, String positiveButtonText, Runnable onPositiveButtonClicked, String negativeButtonText, Runnable onNegativeButtonClicked) {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    if (onPositiveButtonClicked != null) {
                        onPositiveButtonClicked.run();
                    } else {
                        dialog.dismiss();
                    }
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    if (onNegativeButtonClicked != null) {
                        onNegativeButtonClicked.run();
                    } else {
                        dialog.dismiss();
                    }
                    break;
            }
        };

        if (positiveButtonText == null || positiveButtonText.isEmpty()) {
            positiveButtonText = "Positive Button";
        }

        if (negativeButtonText == null || negativeButtonText.isEmpty()) {
            negativeButtonText = "Negative Button";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder
                .setMessage(message)
                .setPositiveButton(positiveButtonText, dialogClickListener)
                .setNegativeButton(negativeButtonText, dialogClickListener)
                .show();
    }

    public void loadUriWithPermissionCheck(Activity activity, Intent intent, Uri imageUri, int requestCode) {
        if (requestCode == REQUEST_CODE_SET_IMAGE) {
            throw new IllegalArgumentException(
                    "the request code " + REQUEST_CODE_SET_IMAGE + " is private, please use a different request code"
            );
        }
        if ((intent.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION) == 0) {
            if (activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                showDialog(
                        activity,
                        messageURI + ".\n\n" +
                                "do you want to grant this permission?",
                        "Grant",
                        () -> requestReadStorage(activity, imageUri, requestCode),
                        "Exit",
                        activity::finishAndRemoveTask
                );
            } else {
                showDialog(
                        activity,
                        messageURI + ".\n\n" +
                                "However this permission has already been granted.\n\n" +
                                "Would you like to continue reading the Uri?",
                        "yes i would",
                        () -> loadUri(imageUri),
                        "no, i want to exit",
                        activity::finishAndRemoveTask
                );
            }
        } else {
            loadUri(imageUri);
        }
    }

    void requestReadStorage(Activity activity, Uri imageUri, int requestCode) {
        if (activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            savedActivity = activity;
            savedUri = imageUri;
            savedRequestCode = requestCode;
            activity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestCode);
        } else {
            loadUri(imageUri);
        }
    }

    void requestReadStorage(Activity activity, Runnable onGranted, int requestCode) {
        if (activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            savedActivity = activity;
            savedOnGranted = onGranted;
            savedRequestCode = requestCode;
            activity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestCode);
        } else {
            onGranted.run();
        }
    }

    public void onRequestPermissionsResult(int requestCode, int[] grantResults) {
        if (requestCode == savedRequestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (requestCode == REQUEST_CODE_SET_IMAGE) {
                    savedOnGranted.run();
                } else {
                    loadUri(savedUri);
                }
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                if (requestCode == REQUEST_CODE_SET_IMAGE) {
                    showDialog(
                            savedActivity,
                            messageIMAGE + ", and you have denied this permission.\n\n" +
                                    "Do you want to request it again?",
                            "Yes i do",
                            () -> requestReadStorage(savedActivity, savedOnGranted, requestCode),
                            "No, i want to cancel",
                            null
                    );
                } else {
                    showDialog(
                            savedActivity,
                            messageURI + ", and you have denied this permission.\n\n" +
                                    "Do you want to request it again?",
                            "Yes i do",
                            () -> requestReadStorage(savedActivity, savedUri, requestCode),
                            "No, i want to exit",
                            savedActivity::finishAndRemoveTask
                    );
                }
            }
        }
    }
}
