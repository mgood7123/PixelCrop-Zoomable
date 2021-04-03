package smallville7123.example.photocrop;

import android.graphics.Canvas;
import android.graphics.Paint;

import smallville7123.circularbuffer.UndoRedoCircularBuffer;

public class ConnectionDrawer {

    private static final String TAG = "ConnectionDrawer";

    UndoRedoCircularBuffer undoRedoCircularBuffer = new UndoRedoCircularBuffer(50);

    private int tmpX, tmpY;

    private int[] saveTmp(int offsetX, int offsetY) {
        int[] next = new int[2];
        next[0] = Math.toIntExact(undoRedoCircularBuffer.pop_front());
        next[1] = Math.toIntExact(undoRedoCircularBuffer.pop_front());
        tmpX = next[0] + offsetX;
        tmpY = next[1] + offsetY;
        return next;
    }

    private void updateNext() {
        setNext(tmpX, tmpY);
    }

    public void setNext(int x, int y) {
        undoRedoCircularBuffer.push_front(x);
        undoRedoCircularBuffer.push_front(y);
    }

    public void drawLine(Canvas canvas, int offsetX, int offsetY, Paint paint) {
        int[] next = saveTmp(offsetX, offsetY);
        canvas.drawLine(next[0], next[1], tmpX, tmpY, paint);
        updateNext();
    }

    public void drawRect(Canvas canvas, int offsetX, int offsetY, Paint paint) {
        int[] next = saveTmp(offsetX, offsetY);
        canvas.drawRect(next[0], next[1], tmpX, tmpY, paint);
        updateNext();
    }

    public void drawCircle(Canvas canvas, int radius, Paint paint) {
        int[] next = saveTmp(0, 0);
        canvas.drawCircle(next[0], next[1], radius, paint);
        updateNext();
    }
}
