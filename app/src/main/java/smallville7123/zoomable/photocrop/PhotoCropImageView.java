package smallville7123.zoomable.photocrop;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import smallville7123.zoomable.R;

public class PhotoCropImageView extends FrameLayout {
    private static final String TAG = "PhotoCropImageView";

    public PhotoCropImageView(Context context) {
        super(context);
        init();
    }

    public PhotoCropImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PhotoCropImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public PhotoCropImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    BoundedImageView imageMain;
    Button export;

    public void setOnRequestStoragePermissionCallback(BoundedImageView.Request onRequestStoragePermissionCallback) {
        imageMain.setOnRequestStoragePermissionCallback(onRequestStoragePermissionCallback);
    }

    void init() {
        inflate(getContext(), R.layout.photocrop_content, this);
        imageMain = findViewById(R.id.imageMain);
        export = findViewById(R.id.export);

        export.setOnClickListener(v -> {
            imageMain.saveImage("MAIN_IMAGE_VIEW", "Photo Crop");
        });
    }
    Drawable imageDrawable;

    public void setImageDrawable(Drawable drawable) {
        imageDrawable = drawable;
        imageMain.setImageDrawable(drawable);
    }
}
