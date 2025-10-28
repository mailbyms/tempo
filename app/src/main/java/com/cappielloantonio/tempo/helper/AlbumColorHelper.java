package com.cappielloantonio.tempo.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.concurrent.ExecutionException;

public class AlbumColorHelper {
    private static final String TAG = "AlbumColorHelper";
    private static final String DEFAULT_DARK_COLOR = "#FF121212";
    private static final String DEFAULT_LIGHT_COLOR = "#FFF5F5F5";

    public interface ColorExtractionCallback {
        void onColorExtracted(int dominantColor);
        void onColorExtractionFailed();
    }

    /**
     * 从专辑封面提取平均颜色
     * @param context 上下文
     * @param imageUrl 专辑封面URL
     * @param callback 颜色提取回调
     */
    public static void extractAlbumColor(@NonNull Context context, @NonNull String imageUrl, @NonNull ColorExtractionCallback callback) {
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .centerCrop();

        Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .apply(options)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        extractColorFromBitmap(resource, callback);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        callback.onColorExtractionFailed();
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        callback.onColorExtractionFailed();
                    }
                });
    }

    /**
     * 同步从专辑封面提取颜色（如果在主线程调用可能造成ANR，建议仅在后台线程使用）
     * @param context 上下文
     * @param imageUrl 专辑封面URL
     * @return 提取的颜色
     */
    public static int extractAlbumColorSync(@NonNull Context context, @NonNull String imageUrl) {
        try {
            Bitmap bitmap = Glide.with(context)
                    .asBitmap()
                    .load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .centerCrop()
                    .submit()
                    .get();

            return extractAverageColor(bitmap);
        } catch (ExecutionException | InterruptedException e) {
            return getDefaultColor(context);
        } catch (Exception e) {
            return getDefaultColor(context);
        }
    }

    /**
     * 从Bitmap中提取颜色
     */
    private static void extractColorFromBitmap(@NonNull Bitmap bitmap, @NonNull ColorExtractionCallback callback) {
        Palette.from(bitmap)
                .generate(palette -> {
                    if (palette != null) {
                        int dominantColor = getDominantColor(palette);
                        if (dominantColor != Color.TRANSPARENT) {
                            callback.onColorExtracted(dominantColor);
                            return;
                        }
                    }
                    callback.onColorExtractionFailed();
                });
    }

    /**
     * 从Bitmap中提取平均颜色
     */
    private static int extractAverageColor(@NonNull Bitmap bitmap) {
        Palette palette = Palette.from(bitmap).generate();
        if (palette != null) {
            return getDominantColor(palette);
        }
        return Color.parseColor(DEFAULT_DARK_COLOR);
    }

    /**
     * 从Palette中获取主要颜色
     */
    private static int getDominantColor(@NonNull Palette palette) {
        // 优先选择主要颜色，如果没有则选择鲜艳的颜色
        if (palette.getDominantSwatch() != null) {
            return palette.getDominantSwatch().getRgb();
        } else if (palette.getVibrantSwatch() != null) {
            return palette.getVibrantSwatch().getRgb();
        } else if (palette.getMutedSwatch() != null) {
            return palette.getMutedSwatch().getRgb();
        } else if (palette.getLightVibrantSwatch() != null) {
            return palette.getLightVibrantSwatch().getRgb();
        } else if (palette.getDarkMutedSwatch() != null) {
            return palette.getDarkMutedSwatch().getRgb();
        }
        return Color.TRANSPARENT;
    }

    /**
     * 将颜色黑化80%
     * @param color 原始颜色
     * @return 黑化后的颜色
     */
    public static int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = hsv[2] * 0.2f; // 将亮度降低80%
        return Color.HSVToColor(hsv);
    }

    /**
     * 将颜色黑化50%
     * @param color 原始颜色
     * @return 黑化50%后的颜色
     */
    public static int darkenColor50Percent(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = hsv[2] * 0.9f; // 将亮度降低50%
        return Color.HSVToColor(hsv);
    }

    /**
     * 根据当前主题获取默认颜色
     */
    private static int getDefaultColor(@NonNull Context context) {
        boolean isDarkTheme = isDarkTheme(context);
        if (isDarkTheme) {
            return Color.parseColor(DEFAULT_DARK_COLOR);
        } else {
            return Color.parseColor(DEFAULT_LIGHT_COLOR);
        }
    }

    /**
     * 检查是否为深色主题
     */
    private static boolean isDarkTheme(@NonNull Context context) {
        // 这里可以根据应用的主题设置来判断
        // 由于项目中有ThemeHelper，我们使用相应的逻辑
        String themePreference = com.cappielloantonio.tempo.util.Preferences.getTheme();

        if ("dark".equals(themePreference)) {
            return true;
        } else if ("light".equals(themePreference)) {
            return false;
        } else { // "default" - 跟随系统
            int nightModeFlags = context.getResources().getConfiguration().uiMode &
                               android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        }
    }

    /**
     * 创建渐变背景的颜色数组（起点是封面平均颜色黑化50%，终点是封面平均颜色黑化80%）
     * @param topColor 顶部颜色（专辑封面平均颜色）
     * @return 渐变颜色数组 [darkenedTopColor50%, darkenedTopColor80%]
     */
    public static int[] createGradientColors(int topColor) {
        int darkened50Percent = darkenColor50Percent(topColor);
        int darkened80Percent = darkenColor(topColor);
        return new int[]{darkened50Percent, darkened80Percent};
    }

    /**
     * 创建适配主题的渐变颜色数组（在颜色提取失败时使用）
     * @param context 上下文
     * @return 适配主题的渐变颜色数组
     */
    public static int[] createThemeBasedGradientColors(@NonNull Context context) {
        int defaultColor = getDefaultColor(context);
        return createGradientColors(defaultColor);
    }
}