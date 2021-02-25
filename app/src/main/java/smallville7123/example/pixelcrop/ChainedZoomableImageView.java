package smallville7123.example.pixelcrop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;

import smallville7123.example.pixelcrop.BoundedImageView.BitmapListener;

public class ChainedZoomableImageView {
    private static final String TAG = "ChainedZoomableImage";
    ArrayList<Pair<ZoomableImageView, BitmapListener>> zoomableImageViews = new ArrayList<>();

    public ZoomableImageView add(Context context) {
        ZoomableImageView zoomableImageView = new ZoomableImageView(context);
        zoomableImageView.setOnRequestStoragePermissionCallback(onRequestStoragePermissionCallback);
        BitmapListener bitmapListener = new BitmapListener() {
            @Override
            public void onReady(Bitmap bitmap) {
                boolean reached = false;
                for (int i = 0, zoomableImageViewsSize = zoomableImageViews.size(); i < zoomableImageViewsSize; i++) {
                    Pair<ZoomableImageView, BitmapListener> current = zoomableImageViews.get(i);
                    if (i != 0) {
                        Pair<ZoomableImageView, BitmapListener> previous = zoomableImageViews.get(i - 1);
                        if (previous.second == this) {
                            Log.i(TAG, "onReady: updating " + i + " using " + (i - 1));
                            current.first.setImageDrawable(new BitmapDrawable(context.getResources(), bitmap));
                            return;
                        }
                    }
                }
            }
        };
        zoomableImageView.addBitmapListener(bitmapListener);
        ZoomableImageView last = getLast();
        if (last != null) {
            if (!last.imageMain.magnifying) {
                zoomableImageView.setImageDrawable(last.imageDrawable);
            }
        }
        zoomableImageViews.add(new Pair<>(zoomableImageView, bitmapListener));
        return zoomableImageView;
    }

    BoundedImageView.Request onRequestStoragePermissionCallback = null;

    public void setOnRequestStoragePermissionCallback(BoundedImageView.Request onRequestStoragePermissionCallback) {
        this.onRequestStoragePermissionCallback = onRequestStoragePermissionCallback;
        for (Pair<ZoomableImageView, BitmapListener> zoomableImageView : zoomableImageViews) {
            zoomableImageView.first.setOnRequestStoragePermissionCallback(onRequestStoragePermissionCallback);
        }
    }

    public int remove(int index) {
        zoomableImageViews.remove(index);
        return index;
    }

    public int removeLast() {
        int size = zoomableImageViews.size();
        if (size != 0) {
            return remove(size - 1);

        }
        return -1;
    }

    public ZoomableImageView get(int index) {
        return zoomableImageViews.get(index).first;
    }

    public ZoomableImageView getLast() {
        int size = zoomableImageViews.size();
        if (size != 0) {
            return get(size - 1);

        }
        return null;
    }

    int size() {
        return zoomableImageViews.size();
    }
}
