package utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Hunter on 1/14/2016.
 */
public class PictureUtils {
    public static Bitmap getScaledBitmap(String path, int destWidth, int destHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;

        int inSampleSize = 1;
        if (srcHeight > destHeight || srcWidth > destWidth) {
            inSampleSize = Math.round(srcHeight / destHeight);
        }
        else {
            inSampleSize = Math.round(srcWidth / destWidth);
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap getScaledBitmap(String path, View view) {
        Point size = new Point();
        size.set(view.getWidth(), view.getHeight());
        return getScaledBitmap(path, size.x, size.y);
    }

    public static Bitmap getScaledBitmap(String path, Activity activity) {
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        return getScaledBitmap(path, size.x, size.y);
    }

    public static Bitmap scaleBitmap(Bitmap source, View view) {
        float srcWidth = source.getWidth();
        float srcHeight = source.getHeight();
        int destWidth = view.getWidth();
        float widthRatio = destWidth / srcWidth;

        int scaleWidth = destWidth;
        int scaleHeight = (int) (widthRatio * srcHeight);

        return Bitmap.createScaledBitmap(source, scaleWidth, scaleHeight, false);
    }



    public static Bitmap toGreyscale(Context context, Bitmap source) {
        /**
         * Perform conversion to greyscale. Return greyscaled image.
         * File creation and saving Bitmap handled elsewhere.
         * Could be done in-line with other effects.
         */

        final float R_FRAC = 0.299f;
        final float G_FRAC = 0.587f;
        final float B_FRAC = 0.114f;

        final int width = source.getWidth();
        final int height = source.getHeight();

        Toast.makeText(context, "Processing Image. This may take several seconds.", Toast.LENGTH_LONG).show();

        Bitmap sourceGS = Bitmap.createBitmap(width, height, source.getConfig());

        int pixel, GR;

        for(int i = 0; i < width; i++) {
            if((i % 1000) == 0) {
                Toast.makeText(context, "Working...", Toast.LENGTH_SHORT).show();
            }
            for(int j = 0; j < height; j++) {
                pixel = source.getPixel(i, j);
                GR = (int)(Color.red(pixel)*R_FRAC + Color.green(pixel)*G_FRAC + Color.blue(pixel)*B_FRAC);
                sourceGS.setPixel(i, j, Color.argb(Color.alpha(pixel), GR, GR, GR));
            }
        }

        return sourceGS;
    }
}
