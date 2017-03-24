package com.gzzb.zbnameplate.utils;

import android.graphics.Bitmap;

/**
 * Created by Lam on 2017/2/15.
 */

public class BitmapToPixelUtil {
    public static byte[] convertBitmapToPixel(Bitmap bitmap) {
        byte[] bitmapPixels = null;
        int xStart = 0;
        int yStart = 0;
        bitmapPixels = new byte[bitmap.getWidth() * bitmap.getHeight()];
        for (int x1 = 0, i = xStart * yStart; x1 < bitmap.getWidth(); x1++) {
            for (int y1 = 0; y1 < bitmap.getHeight(); y1++) {
                int pixel = bitmap.getPixel(x1, y1);
                int blue = 0;
                int green = 0;
                int red = 0;
                String hexStr = Integer.toHexString(pixel);
                if (pixel == 0 && hexStr.length() < 6) {
                } else if (hexStr.length() >= 6) {
                    int length = hexStr.length();
                    hexStr = hexStr.substring(length - 6);//保证要有6位数字
                    blue= Integer.parseInt(hexStr.substring(0, 2), 16);//使用十六进制
                    green = Integer.parseInt(hexStr.substring(2, 4), 16);
                    red = Integer.parseInt(hexStr.substring(4, 6), 16);
                }
                red = red / 85;
                green = green / 85;
                blue = blue / 85;

                red = red * 16;
                green = green * 4;
                blue = blue * 1;
                bitmapPixels[i] = (byte) (red + green + blue);
                i++;
            }
        }
        return bitmapPixels;
    }
}
