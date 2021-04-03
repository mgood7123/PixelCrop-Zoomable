package smallville7123.example.photocrop;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.FloatRange;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import smallville7123.example.zoomable.R;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class BoundedImageView extends FrameLayout {

    private static final String TAG = "BoundedImageView";

    public boolean DEBUG = false;

    boolean magnifying;
    final boolean showColor = true;
    ImageView imageView;
    Drawable highlight;
    Drawable select;
    private final boolean allowTouchesOutsideOfImage = true;

    int color;
    String colorInt;
    String colorHex;

    private final static Paint paintBlack = new Paint() {
        {
            setColor(Color.BLACK);
        }
    };

    ColorMatrix invertColorMatrix = new ColorMatrix(new float[]{
            -1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, -1.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, -1.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 1.0f, 1.0f, 0.0f
    });
    ColorMatrixColorFilter invertColorMatrixColorFilter = new ColorMatrixColorFilter(invertColorMatrix);

    public BoundedImageView(Context context) {
        super(context);
        init();
    }

    public BoundedImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoundedImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public BoundedImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    Paint shadow = new Paint() {
        {
            ColorFilter shadow = new PorterDuffColorFilter(Color.argb(180,0,0,0), PorterDuff.Mode.SRC_IN);
            setColorFilter(shadow);
        }
    };

    @SuppressLint("AppCompatCustomView")
    void init() {
        setWillNotDraw(false);
        Resources.Theme theme = getContext().getTheme();
        TypedValue value = new TypedValue();
        theme.resolveAttribute(android.R.attr.autofilledHighlight, value, true);
        highlight = theme.getDrawable(value.resourceId);
        select = theme.getDrawable(android.R.drawable.ic_menu_close_clear_cancel);

        imageView = new ImageView(getContext()) {
            private final Canvas canvas = new Canvas();
            private Bitmap result;
            private Bitmap masked;

            int width;
            int height;
            int size;
            int[] maskedPixels;
            int[] inputPixels;

            final protected void createBitmap(int width, int height) {
                if (result != null) result.recycle();
                if (masked != null) masked.recycle();
                this.width = width;
                this.height = height;
                size = width * height;
                maskedPixels = new int[size];
                inputPixels = new int[size];
                result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                masked = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            }

            @Override
            protected void onSizeChanged(int w, int h, int oldw, int oldh) {
                super.onSizeChanged(w, h, oldw, oldh);
                createBitmap(w, h);
            }

            public void mask(Bitmap mask, int targetColor, Bitmap input, Bitmap output) {
                mask.getPixels(maskedPixels, 0, width, 0, 0, width, height);
                input.getPixels(inputPixels, 0, width, 0, 0, width, height);

                for (int i = 0; i < size; i++) {
                    if (maskedPixels[i] == targetColor) inputPixels[i] = inputPixels[i] ^ 0x00ffffff;
                }

                output.setPixels(inputPixels, 0, width, 0, 0, width, height);
            }

            final Paint bitmapPaint = new Paint();

            ConnectionDrawer connectionDrawer = new ConnectionDrawer();

            @Override
            public void draw(Canvas realCanvas) {
                if (magnifying) {
                    super.draw(realCanvas);
                    int x = selectorX;
                    int y = selectorY;
                    int w = selectorWidth + x;
                    int h = selectorHeight + y;
                    realCanvas.clipOutRect(x, y, w, h);
                    realCanvas.drawRect(imageBoundsRelativeToView, shadow);
                } else if (showColor) {
                    canvas.setBitmap(masked);
                    canvas.drawColor(0, PorterDuff.Mode.SRC_IN);

                    ColorFilter saved = getColorFilter();
                    setColorFilter(invertColorMatrixColorFilter);
                    super.draw(canvas);
                    setColorFilter(saved);

                    canvas.setBitmap(result);
                    canvas.drawColor(0, PorterDuff.Mode.SRC_IN);
                    super.draw(canvas);

                    bitmapPaint.setShader(new BitmapShader(masked, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

                    connectionDrawer.setNext(selectorX, selectorY);
                    connectionDrawer.drawCircle(canvas, 20, bitmapPaint);
                    connectionDrawer.drawLine(canvas, 100, -150, bitmapPaint);
                    connectionDrawer.drawRect(canvas, 400, -100, bitmapPaint);

                    realCanvas.drawBitmap(result, 0, 0, null);
                }
                invalidate();
            }
        };
        addView(imageView, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
        setMagnifying(false);
        setImageDrawable(theme.getDrawable(R.mipmap.ic_launcher_round));
    }

    public void setMagnifying(boolean magnifying) {
        this.magnifying = magnifying;
        setZoomPercentage(zoomPercentage);
    }

    void setImageDrawable(Drawable drawable) {
        imageView.setImageDrawable(drawable);
        setZoomPercentage(zoomPercentage);
    }

    int selectorWidth = 0;
    int selectorHeight = 0;
    int selectorCenterX = 0;
    int selectorCenterY = 0;
    int selectorX = 0;
    int selectorY = 0;

    public static Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) return null;
        if (drawable instanceof BitmapDrawable) {
            Bitmap b = ((BitmapDrawable) drawable).getBitmap();
            return b.copy(b.getConfig(), b.isMutable());
        }
        int w = drawable.getIntrinsicWidth();
        if (w <= 0) w = 1;
        int h = drawable.getIntrinsicHeight();
        if (h <= 0) h = 1;
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(b);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return b;
    }

    void updateSelectorLocation() {
        if (DEBUG) {
            Log.d(TAG, "imageBoundsRelativeToImage = [" + (imageBoundsRelativeToImage) + "]");
            Log.d(TAG, "imageBoundsRelativeToView = [" + (imageBoundsRelativeToView) + "]");
            Log.d(TAG, "selectorX = [" + (selectorX) + "]");
            Log.d(TAG, "selectorY = [" + (selectorX) + "]");
            Log.d(TAG, "selectorWidth = [" + (selectorWidth) + "]");
            Log.d(TAG, "selectorHeight = [" + (selectorHeight) + "]");
        }
        if (imageBoundsRelativeToView == null) return;
        if (selectorWidth > imageBoundsRelativeToView.right) {
            if (DEBUG) {
                Log.d(TAG, "updateSelectorLocation: returning due to selectorWidth > imageBoundsRelativeToView.right");
            }
            return;
        }
        if (selectorHeight > imageBoundsRelativeToView.bottom) {
            if (DEBUG) {
                Log.d(TAG, "updateSelectorLocation: returning due to selectorHeight > imageBoundsRelativeToView.bottom");
            }
            return;
        }
        if (selectorX < imageBoundsRelativeToView.left) {
            selectorX = imageBoundsRelativeToView.left;
        }
        if (selectorY < imageBoundsRelativeToView.top) {
            selectorY = imageBoundsRelativeToView.top;
        }
        if (magnifying) {
            if (selectorX + selectorWidth > imageBoundsRelativeToView.right) {
                selectorX = imageBoundsRelativeToView.right - selectorWidth;
            }
            if (selectorY + selectorHeight > imageBoundsRelativeToView.bottom) {
                selectorY = imageBoundsRelativeToView.bottom - selectorHeight;
            }
            if (DEBUG) {
                Log.d(TAG, "selectorX = [" + (selectorX) + "]");
                Log.d(TAG, "selectorY = [" + (selectorX) + "]");
                Log.d(TAG, "selectorWidth = [" + (selectorWidth) + "]");
                Log.d(TAG, "selectorHeight = [" + (selectorHeight) + "]");
            }
            if (bitmapListenerArrayList.size() != 0) {
                Bitmap bitmap = getBitmapFromDrawable(imageView.getDrawable());

                if (bitmap != null) {
                    int w = bitmap.getWidth();
                    float imageWidth = imageBoundsRelativeToView.right - imageBoundsRelativeToView.left;
                    float cropX = selectorX - imageBoundsRelativeToView.left;
                    float cropWidth = selectorWidth;

                    int h = bitmap.getHeight();
                    float imageHeight = imageBoundsRelativeToView.bottom - imageBoundsRelativeToView.top;
                    float cropY = selectorY - imageBoundsRelativeToView.top;
                    float cropHeight = selectorHeight;

                    int x_ = (int) (cropX * (w / imageWidth));
                    int y_ = (int) (cropY * (h / imageHeight));
                    int w_ = (int) (cropWidth * (w / imageWidth));
                    int h_ = (int) (cropHeight * (h / imageHeight));

                    if (w_ < 1) w_ = 1;
                    if (h_ < 1) h_ = 1;

                    if (DEBUG) {

                        Log.d(TAG, "w = [" + (w) + "]");
                        Log.d(TAG, "imageWidth = [" + (imageWidth) + "]");
                        Log.d(TAG, "cropX = [" + (cropX) + "]");
                        Log.d(TAG, "cropWidth = [" + (cropWidth) + "]");
                        Log.d(TAG, "cropX * (w / imageWidth) = [" + (cropX * (w / imageWidth)) + "]");
                        Log.d(TAG, "cropWidth * (w / imageWidth) = [" + (cropWidth * (w / imageWidth)) + "]");

                        Log.d(TAG, "h = [" + (h) + "]");
                        Log.d(TAG, "imageHeight = [" + (imageHeight) + "]");
                        Log.d(TAG, "cropY = [" + (cropY) + "]");
                        Log.d(TAG, "cropHeight = [" + (cropHeight) + "]");
                        Log.d(TAG, "cropY * (h / imageHeight) = [" + (cropY * (h / imageHeight)) + "]");
                        Log.d(TAG, "cropHeight * (h / imageHeight) = [" + (cropHeight * (h / imageHeight)) + "]");

                        Log.d(TAG, "x_ = [" + (x_) + "]");
                        Log.d(TAG, "y_ = [" + (y_) + "]");
                        Log.d(TAG, "w_ = [" + (w_) + "]");
                        Log.d(TAG, "h_ = [" + (h_) + "]");

                    }

                    Matrix m = new Matrix();
                    m.setScale((float) w / w_, (float) h / h_);
                    Bitmap cropped = Bitmap.createBitmap(bitmap, x_, y_, w_, h_, m, false);
                    if (cropped == bitmap) {
                        cropped = cropped.copy(cropped.getConfig(), cropped.isMutable());
                    }

                    bitmap.recycle();

                    Bitmap finalCropped = cropped;
                    bitmapListenerArrayList.forEach(bitmapListener -> bitmapListener.onReady(finalCropped));
                }
            }
        } else {
            if (selectorX > imageBoundsRelativeToView.right) {
                selectorX = imageBoundsRelativeToView.right;
            }
            if (selectorY > imageBoundsRelativeToView.bottom) {
                selectorY = imageBoundsRelativeToView.bottom;
            }
            if (DEBUG) {
                Log.d(TAG, "selectorX = [" + (selectorX) + "]");
                Log.d(TAG, "selectorY = [" + (selectorX) + "]");
            }
            Bitmap bitmap = getBitmapFromDrawable(imageView.getDrawable());

            if (bitmap != null) {
                int w = bitmap.getWidth();
                float imageWidth = imageBoundsRelativeToView.right - imageBoundsRelativeToView.left;
                float cropX = selectorX - imageBoundsRelativeToView.left;

                int h = bitmap.getHeight();
                float imageHeight = imageBoundsRelativeToView.bottom - imageBoundsRelativeToView.top;
                float cropY = selectorY - imageBoundsRelativeToView.top;

                int x_ = (int) (cropX * (w / imageWidth));
                int y_ = (int) (cropY * (h / imageHeight));

                if (DEBUG) {

                    Log.d(TAG, "w = [" + (w) + "]");
                    Log.d(TAG, "imageWidth = [" + (imageWidth) + "]");
                    Log.d(TAG, "cropX = [" + (cropX) + "]");
                    Log.d(TAG, "cropX * (w / imageWidth) = [" + (cropX * (w / imageWidth)) + "]");

                    Log.d(TAG, "h = [" + (h) + "]");
                    Log.d(TAG, "imageHeight = [" + (imageHeight) + "]");
                    Log.d(TAG, "cropY = [" + (cropY) + "]");
                    Log.d(TAG, "cropY * (h / imageHeight) = [" + (cropY * (h / imageHeight)) + "]");

                    Log.d(TAG, "x_ = [" + (x_) + "]");
                    Log.d(TAG, "y_ = [" + (y_) + "]");

                }
                color = ColorTools.getColor(bitmap, x_, y_);
                colorInt = ColorTools.toIntString(color);
                colorHex = ColorTools.toHexString(color);
                bitmapListenerArrayList.forEach(bitmapListener -> bitmapListener.onReady(bitmap));
            }
        }
    }

    @FloatRange(from = 0, to = 100)
    float zoomPercentage = 100;

    public void setZoomPercentage(@FloatRange(from = 0, to = 100) float zoomPercentage) {
        this.zoomPercentage = zoomPercentage;
        if (zoomPercentage == 0.0f) {
            zoomPercentage = 0.001f;
        }
        if (imageBoundsRelativeToView != null) {
            float imageWidth = imageBoundsRelativeToView.right - imageBoundsRelativeToView.left;
            float imageHeight = imageBoundsRelativeToView.bottom - imageBoundsRelativeToView.top;
            float z = zoomPercentage / 100.0f;
            selectorWidth = (int) (imageWidth * z);
            selectorHeight = (int) (imageHeight * z);
            selectorCenterX = selectorWidth / 2;
            selectorCenterY = selectorHeight / 2;
            updateSelectorLocation();
        }
    }

    public File ensureAccessable(String path) {
        return ensureAccessable(new File(path));
    }

    public File ensureAccessable(File root) {
        if (!root.exists()) {
            if (!root.mkdir()) {
                throw new RuntimeException("Failed to create directory: " + root);
            }
        }
        if (!root.canRead()) {
            throw new RuntimeException("Directory is not readable: " + root);
        }
        if (!root.canWrite()) {
            throw new RuntimeException("Directory is not writable: " + root);
        }
        return root;
    }

    public File getAlbumStorageDir(String albumName, String sub_folder) {
        File a = ensureAccessable(Environment.getExternalStorageDirectory());
        File b = ensureAccessable(a + "/" + albumName);
        File file = new File(b + (sub_folder == null ? "" : "/" + sub_folder));
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new RuntimeException("Failed to create directory: " + file);
            }
        }
        return file;
    }

    public static String getNextFileName(String path, String extension) {
        long n = 0;
        String newPath = path;
        File file = new File(newPath + extension);
        while (file.exists()) {
            n++;
            newPath = path + " (" + n + ")";
            file = new File(newPath + extension);
        }
        return newPath;
    }

    public void saveImage_(String prefix, String sub_folder) {
        File ext = Environment.getExternalStorageDirectory();
        String directory = Environment.DIRECTORY_PICTURES + "/" + sub_folder;
        String name = prefix;
        String path = getNextFileName(ext + "/" + directory + "/" + name, ".png");

        OutputStream fos;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = getContext().getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name + ".png");
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, directory);
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            try {
                fos = resolver.openOutputStream(imageUri);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                fos = new FileOutputStream(path);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        Bitmap output = getBitmapFromDrawable(imageView.getDrawable());
        if (output != null) {
            output.compress(Bitmap.CompressFormat.PNG, 0, fos);
        }
        try {
            fos.close();
        } catch (IOException unused) {
            throw new RuntimeException(unused);
        }
        Toast.makeText(getContext(), "image saved to " + path + ".png", Toast.LENGTH_SHORT).show();
    }

    public interface Request {
        void run(Runnable onGranted);
    }

    Request onRequestStoragePermissionCallback = null;

    public void setOnRequestStoragePermissionCallback(Request onRequestStoragePermissionCallback) {
        this.onRequestStoragePermissionCallback = onRequestStoragePermissionCallback;
    }

    public void saveImage(String prefix, String sub_folder) {
        if (onRequestStoragePermissionCallback != null) {
            onRequestStoragePermissionCallback.run(() -> saveImage_(prefix, sub_folder));
        }
    }

    public interface BitmapListener {
        void onReady(Bitmap bitmap);
    }

    ArrayList<BitmapListener> bitmapListenerArrayList = new ArrayList<>();

    public void addBitmapListener(BitmapListener bitmapListener) {
        bitmapListenerArrayList.add(bitmapListener);
        setZoomPercentage(zoomPercentage);
    }

    public void removeBitmapListener(BitmapListener bitmapListener) {
        bitmapListenerArrayList.remove(bitmapListener);
    }

    boolean firstLoad = false;



    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        getBounds();
        Log.d(TAG, "imageBoundsRelativeToView = [" + (imageBoundsRelativeToView) + "]");
        if (imageBoundsRelativeToView != null) {
            if (firstLoad) {
                selectorX = imageBoundsRelativeToView.left;
                selectorY = imageBoundsRelativeToView.top;
                firstLoad = false;
            } else {
                if (selectorX < imageBoundsRelativeToView.left) {
                    selectorX = imageBoundsRelativeToView.left;
                } else if (selectorX > imageBoundsRelativeToView.right) {
                    selectorX = imageBoundsRelativeToView.right;
                }
                if (selectorY < imageBoundsRelativeToView.top) {
                    selectorY = imageBoundsRelativeToView.top;
                } else if (selectorY > imageBoundsRelativeToView.bottom) {
                    selectorY = imageBoundsRelativeToView.bottom;
                }
            }
            setZoomPercentage(zoomPercentage);
            updateSelectorLocation();
        }
    }

    Rect imageBoundsRelativeToView;
    Rect imageBoundsRelativeToImage;

    private void getBounds() {
        int[] array = getBitmapPositionInsideImageView(imageView);
        if (array == null) return;

        imageBoundsRelativeToView = new Rect();
        imageBoundsRelativeToView.left = array[0];
        imageBoundsRelativeToView.top = array[1];
        imageBoundsRelativeToView.right = array[2] + array[0];
        imageBoundsRelativeToView.bottom = array[3] + array[1];

        imageBoundsRelativeToImage = new Rect();
        imageBoundsRelativeToImage.left = 0;
        imageBoundsRelativeToImage.top = 0;
        if (array[2] > array[0]) imageBoundsRelativeToImage.right = array[2] - array[0];
        else if (array[2] < array[0]) imageBoundsRelativeToImage.right = array[0] - array[2];
        if (array[3] > array[1]) imageBoundsRelativeToImage.bottom = array[3] - array[1];
        else if (array[3] < array[1]) imageBoundsRelativeToImage.bottom = array[1] - array[3];
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                return true;
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                PointF location = new PointF(event.getX(), event.getY());
                if (withinImage(location)) {
                    if (magnifying) {
                        selectorX = (int) location.x - selectorCenterX;
                        selectorY = (int) location.y - selectorCenterY;
                    } else {
                        selectorX = (int) location.x;
                        selectorY = (int) location.y;
                    }
                    updateSelectorLocation();
                } else {
                    if (allowTouchesOutsideOfImage) {
                        if (imageBoundsRelativeToView != null) {
                            if (withinImageX(location.x)) {
                                if (magnifying) {
                                    selectorX = (int) location.x - selectorCenterX;
                                } else {
                                    selectorX = (int) location.x;
                                }
                                if (location.y < imageBoundsRelativeToView.top) {
                                    selectorY = imageBoundsRelativeToView.top;
                                } else {
                                    selectorY = imageBoundsRelativeToView.bottom;
                                }
                            } else if (withinImageY(location.y)) {
                                if (location.x < imageBoundsRelativeToView.left) {
                                    selectorX = imageBoundsRelativeToView.left;
                                } else {
                                    selectorX = imageBoundsRelativeToView.right;
                                }
                                if (magnifying) {
                                    selectorY = (int) location.y - selectorCenterY;
                                } else {
                                    selectorY = (int) location.y;
                                }
                            } else {
                                if (location.x < imageBoundsRelativeToView.left) {
                                    selectorX = imageBoundsRelativeToView.left;
                                } else {
                                    selectorX = imageBoundsRelativeToView.right;
                                }
                                if (location.y < imageBoundsRelativeToView.top) {
                                    selectorY = imageBoundsRelativeToView.top;
                                } else {
                                    selectorY = imageBoundsRelativeToView.bottom;
                                }
                            }
                            updateSelectorLocation();
                        }
                    }
                }
                return true;
            default:
                return false;
        }
    }

    boolean withinImageX(float point) {
        return imageBoundsRelativeToView != null && within(point, imageBoundsRelativeToView.left, imageBoundsRelativeToView.right);
    }

    boolean withinImageY(float point) {
        return imageBoundsRelativeToView != null && within(point, imageBoundsRelativeToView.top, imageBoundsRelativeToView.bottom);
    }

    boolean withinImage(PointF point) {
        return withinImageX(point.x) && withinImageY(point.y);
    }

    boolean within(float point, float start, float end) {
        return point >= start && point <= end;
    }

    /**
     * Returns the bitmap position inside an imageView.
     * @param imageView source ImageView
     * @return 0: left, 1: top, 2: right, 3: bottom
     */
    public static int[] getBitmapPositionInsideImageView(ImageView imageView) {
        if (imageView == null) return null;
        final Drawable d = imageView.getDrawable();
        if (d == null) return null;

        int[] ret = new int[4];

        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        int origW = d.getIntrinsicWidth();
        if (origW <= 0) {
            throw new RuntimeException("getIntrinsicWidth returned " + origW);
//            origW = 1;
        }
        int origH = d.getIntrinsicHeight();
        if (origH <= 0) {
            throw new RuntimeException("getIntrinsicHeight returned " + origH);
//            origH = 1;
        }

        // Calculate the actual dimensions
        int actW = Math.round(origW * scaleX);
        int actH = Math.round(origH * scaleY);

        ret[2] = actW;
        ret[3] = actH;

        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (imgViewH - actH)/2;
        int left = (imgViewW - actW)/2;

        ret[0] = left;
        ret[1] = top;

        return ret;
    }
}
