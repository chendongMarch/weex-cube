package com.march.wxcube.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.march.common.utils.RecycleUtils;

/**
 * CreateAt : 2018/6/15
 * Describe :
 *
 * @author chendong
 */
public class Utils {

    public static Drawable createRepeatDrawable(Context context, Bitmap bitmap, int width, float widthScale, float aspectRatio) {
        int realWidth = (int) (width * widthScale);
        int realHeight = (int) (realWidth * aspectRatio);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, realWidth, realHeight, true);
        // 设置内容区域平铺的小圆角背景
        BitmapDrawable drawable = new BitmapDrawable(context.getResources(), scaledBitmap);
        drawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        if(!bitmap.equals(scaledBitmap)){
            RecycleUtils.recycle(bitmap);
        }
        return drawable;
    }

}
