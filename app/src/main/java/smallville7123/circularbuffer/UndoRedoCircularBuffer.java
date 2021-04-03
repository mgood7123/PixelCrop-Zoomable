package smallville7123.circularbuffer;

public class UndoRedoCircularBuffer {
    static {
        System.loadLibrary("UndoRedoCircularBuffer");
    }

    long instance;

    native long createNativeInstance1(long size);
    native long createNativeInstance2(long size, long undo_redo_size);
    native long createNativeInstance3(long size, long undo_size, long redo_size);
    native long size(long instance);
    native boolean empty(long instance);
    native long front(long instance);
    native long back(long instance);
    native void push_front(long instance, long value);
    native void push_back(long instance, long value);
    native long pop_front(long instance);
    native long pop_back(long instance);
    native void undo(long instance);
    native void redo(long instance);
    native String toString(long instance);

    public UndoRedoCircularBuffer(long size) {
        instance = createNativeInstance1(size);
    }

    public UndoRedoCircularBuffer(long size, long undo_redo_size) {
        instance = createNativeInstance2(size, undo_redo_size);
    }

    public UndoRedoCircularBuffer(long size, long undo_size, long redo_size) {
        instance = createNativeInstance3(size, undo_size, redo_size);
    }

    public boolean empty() {
        return empty(instance);
    }

    public long size() {
        return size(instance);
    }

    public long front() {
        return front(instance);
    }

    public long back() {
        return back(instance);
    }

    public void push_front(long value) {
        push_front(instance, value);
    }

    public void push_back(long value) {
        push_back(instance, value);
    }

    public long pop_front() {
        return pop_front(instance);
    }

    public long pop_back() {
        return pop_back(instance);
    }

    public void undo() {
        undo(instance);
    }

    public void redo() {
        redo(instance);
    }

    @Override
    public String toString() {
        return toString(instance);
    }
}