package smallville7123.zoomable;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.tabs.TabLayout;

import static com.google.android.material.tabs.TabLayout.*;

/**
 * TODO: document your custom view class.
 */
public class TabbedZoomImageView extends FrameLayout {
    private static final String TAG = "TabbedZoomImageView";
    public TabbedZoomImageView(Context context) {
        super(context);
        init();
    }

    public TabbedZoomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TabbedZoomImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public TabbedZoomImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    TabLayout tabLayout;

    Tab addTab(String text) {
        Tab tab = tabLayout.newTab();
        tab.setText(text);
        TextView textView = new TextView(getContext());
        textView.setText(text);
        tab.setCustomView(textView);
        return tab;
    }

    Button add, remove;

    FrameLayout container;

    ChainedZoomableImageView chainedZoomableImageView = new ChainedZoomableImageView();

    void addLayer() {
        chainedZoomableImageView.add(getContext());
        int size = chainedZoomableImageView.size();
        Tab tab = addTab("Layer " + size);
        tabLayout.addTab(tab);
        tabLayout.selectTab(tab, true);
        if (size > 1) {
            chainedZoomableImageView.get(size - 2).updateSelectorLocation();
        }
    }

    void removeLayer() {
        if (chainedZoomableImageView.size() > 1) {
            tabLayout.removeTabAt(chainedZoomableImageView.removeLast());
        }
    }

    private void init() {
        inflate(getContext(), R.layout.tabbed_zoomable_image_view_content, this);
        tabLayout = findViewById(R.id.tabLayout);
        add = findViewById(R.id.buttonAdd);
        remove = findViewById(R.id.buttonRemove);
        container = findViewById(R.id.container);

        add.setOnClickListener(v -> addLayer());
        remove.setOnClickListener(v -> removeLayer());

        tabLayout.addOnTabSelectedListener(new OnTabSelectedListener() {
            @Override
            public void onTabSelected(Tab tab) {
                if (container.getChildCount() != 0) {
                    container.removeViewAt(0);
                }
                container.addView(chainedZoomableImageView.get(tab.getPosition()));
            }

            @Override
            public void onTabUnselected(Tab tab) {

            }

            @Override
            public void onTabReselected(Tab tab) {

            }
        });

        addLayer();
    }

    Drawable imageDrawable;

    public void setImageDrawable(Drawable drawable) {
        imageDrawable = drawable;
        if (chainedZoomableImageView.size() != 0) {
            chainedZoomableImageView.get(0).setImageDrawable(drawable);
        }
    }

    public void setOnRequestStoragePermissionCallback(BoundedImageView.Request onRequestStoragePermissionCallback) {
        chainedZoomableImageView.setOnRequestStoragePermissionCallback(onRequestStoragePermissionCallback);
    }
}