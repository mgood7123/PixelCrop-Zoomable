package smallville7123.zoomable;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class ZoomableImageView extends FrameLayout {
    private static final String TAG = "PhotoCropImageView";

    public ZoomableImageView(Context context) {
        super(context);
        init();
    }

    public ZoomableImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ZoomableImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ZoomableImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    CheckBox zoomCheckBox;
    SeekBar zoomSlider;
    ConstraintLayout imageZoomContainer;
    BoundedImageView imageMain;
    BoundedImageView imageZoomed;
    BoundedImageView.BitmapListener bitmapListener = bitmap -> imageZoomed.imageView.setImageBitmap(bitmap);
    Button exportZoom;
    Button export;

    public void setOnRequestStoragePermissionCallback(BoundedImageView.Request onRequestStoragePermissionCallback) {
        imageMain.setOnRequestStoragePermissionCallback(onRequestStoragePermissionCallback);
        imageZoomed.setOnRequestStoragePermissionCallback(onRequestStoragePermissionCallback);
    }

    void init() {
        inflate(getContext(), R.layout.zoomable_content, this);
        zoomCheckBox = findViewById(R.id.zoomCheckbox);
        zoomSlider = findViewById(R.id.zoomSlider);
        imageZoomContainer = findViewById(R.id.imageZoomContainer);
        imageMain = findViewById(R.id.imageMain);
        imageZoomed = findViewById(R.id.zoomImage);
        exportZoom = findViewById(R.id.exportZoom);
        export = findViewById(R.id.export);

        export.setOnClickListener(v -> {
            imageMain.saveImage("MAIN_IMAGE_VIEW", "Zoomable");
        });

        exportZoom.setOnClickListener(v -> {
            imageZoomed.saveImage("ZOOMABLE_IMAGE_VIEW", "Zoomable");
        });

        zoomSlider.setMin(0);
        zoomSlider.setMax(100);

        zoomSlider.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        imageMain.setZoomPercentage(seekBar.getMax() - progress);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );

        zoomCheckBox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    imageMain.setMagnifying(isChecked);
                    if (isChecked) {
                        zoomSlider.setVisibility(VISIBLE);
                        imageZoomContainer.setVisibility(VISIBLE);
                        imageMain.addBitmapListener(bitmapListener);
                    } else {
                        imageMain.removeBitmapListener(bitmapListener);
                        imageZoomed.setImageDrawable(null);
                        imageZoomContainer.setVisibility(GONE);
                        zoomSlider.setVisibility(GONE);
                    }
                }
        );
    }

    public void addBitmapListener(BoundedImageView.BitmapListener bitmapListener) {
        imageMain.addBitmapListener(bitmapListener);
    }

    public void removeBitmapListener(BoundedImageView.BitmapListener bitmapListener) {
        imageMain.removeBitmapListener(bitmapListener);
    }

    Drawable imageDrawable;

    public void setImageDrawable(Drawable drawable) {
        imageDrawable = drawable;
        imageMain.setImageDrawable(drawable);
    }

    public void updateSelectorLocation() {
        imageMain.updateSelectorLocation();
    }
}
