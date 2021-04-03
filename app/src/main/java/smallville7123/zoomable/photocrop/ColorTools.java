package smallville7123.zoomable.photocrop;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.annotation.NonNull;

public class ColorTools {
    public static String padLeft(String input, int length, String what) {
        int inputLength = input.length();
        if (inputLength >= length) return input;
        StringBuilder builder = new StringBuilder(length);
        int whatLength = what.length();
        int currentLength = 0;

        builder.append(what);
        currentLength += whatLength;

        while (currentLength + inputLength < length) {
            builder.append(what);
            currentLength += whatLength;
        }

        builder.append(input);
        currentLength += inputLength;

        return currentLength > length ?
                builder.substring(currentLength - length, length) :
                builder.toString();
    }

    private static String pad(String input, int radix) {
        return padLeft(input, radix == 10 ? 3 : 2, "0");
    }

    public static class Split {
        private int alpha;
        private int red;
        private int green;
        private int blue;

        Split(int color) {
            this(Color.alpha(color), Color.red(color), Color.green(color), Color.blue(color));
        }

        Split(int alpha, int red, int green, int blue) {
            this.alpha = alpha;
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        public int getAlpha() {
            return alpha;
        }

        public Split setAlpha(int alpha) {
            this.alpha = alpha;
            return this;
        }

        public int getRed() {
            return red;
        }

        public Split setRed(int red) {
            this.red = red;
            return this;
        }

        public int getGreen() {
            return green;
        }

        public Split setGreen(int green) {
            this.green = green;
            return this;
        }

        public int getBlue() {
            return blue;
        }

        public Split setBlue(int blue) {
            this.blue = blue;
            return this;
        }

        public int combine() {
            return Color.argb(alpha, red, green, blue);
        }
    }

    public static Split splitColor(int color) {
        return new Split(color);
    }


    public static int setAlpha(int value, int color) {
        return splitColor(color).setAlpha(value).combine();
    }

    public static int setRed(int value, int color) {
        return splitColor(color).setRed(value).combine();
    }

    public static int setGreen(int value, int color) {
        return splitColor(color).setGreen(value).combine();
    }

    public static int setBlue(int value, int color) {
        return splitColor(color).setBlue(value).combine();
    }

    enum Channel {
        Alpha, Red, Green, Blue
    }

    public static String parseColor(int color, Channel channel, int radix) {
        int extracted;
        if (channel == Channel.Alpha) extracted = Color.alpha(color);
        else if (channel == Channel.Red) extracted = Color.red(color);
        else if (channel == Channel.Green) extracted = Color.green(color);
        else extracted = Color.blue(color);
        return pad(Integer.toString(extracted, radix) , radix);
    }
    
    public static String parseIntColor(int color, Channel channel) {
        return parseColor(color, channel, 10);
    }

    public static String parseHexColor(int color, Channel channel) {
        return parseColor(color, channel, 16);
    }

    public static String parseColorAlpha(int color, int radix) {
        return parseColor(color, Channel.Alpha, radix);
    }

    public static String parseColorRed(int color, int radix) {
        return parseColor(color, Channel.Red, radix);
    }

    public static String parseColorGreen(int color, int radix) {
        return parseColor(color, Channel.Green, radix);
    }

    public static String parseColorBlue(int color, int radix) {
        return parseColor(color, Channel.Blue, radix);
    }

    public static String parseIntColorAlpha(int color) {
        return parseColorAlpha(color, 10);
    }

    public static String parseIntColorRed(int color) {
        return parseColorRed(color, 10);
    }

    public static String parseIntColorGreen(int color) {
        return parseColorGreen(color, 10);
    }

    public static String parseIntColorBlue(int color) {
        return parseColorBlue(color, 10);
    }

    public static String parseHexColorAlpha(int color) {
        return parseColorAlpha(color, 16);
    }

    public static String parseHexColorRed(int color) {
        return parseColorRed(color, 16);
    }

    public static String parseHexColorGreen(int color) {
        return parseColorGreen(color, 16);
    }

    public static String parseHexColorBlue(int color) {
        return parseColorBlue(color, 16);
    }

    public static class Components {
        public final String alpha;
        public final String red;
        public final String green;
        public final String blue;
        public final boolean isHex;

        Components(int color, int radix) {
            this(
                    parseColorAlpha(color, radix),
                    parseColorRed(color, radix),
                    parseColorGreen(color, radix),
                    parseColorBlue(color, radix),
                    radix == 16
            );
        }

        Components(String alpha, String red, String green, String blue, boolean isHex) {
            this.alpha = alpha;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.isHex = isHex;
        }

        public static String toString(int color, int radix) {
            return getComponents(color, radix).toString();
        }

        public static String toIntString(int color) {
            return toString(color, 10);
        }

        public static String toHexString(int color) {
            return toString(color, 16);
        }

        @NonNull
        @Override
        public String toString() {
            if (!isHex) {
                return "[" + alpha + ", " + red + ", " + green + ", " + blue + "]";
            } else {
                return alpha + red + green + blue;
            }
        }

        public String[] toArray() {
            return new String[] {alpha, red, green, blue};
        }
    }

    public static Components getComponents(int color, int radix) {
        return new Components(color, radix);
    }

    public static Components getIntComponents(int color) {
        return getComponents(color, 10);
    }

    public static Components getHexComponents(int color) {
        return getComponents(color, 16);
    }

    public static int getColor(Bitmap bitmap, int x, int y) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        return bitmap.getPixel(Math.min(x, w-1), Math.min(y, h-1));
    }

    public static String toIntString(int color) {
        return Components.toIntString(color);
    }

    public static String toIntString(Bitmap bitmap, int x, int y) {
        return Components.toIntString(getColor(bitmap, x, y));
    }

    public static String toHexString(int color) {
        return "#" + Components.toHexString(color);
    }

    public static String toHexString(Bitmap bitmap, int x, int y) {
        return "#" + Components.toHexString(getColor(bitmap, x, y));
    }
}
