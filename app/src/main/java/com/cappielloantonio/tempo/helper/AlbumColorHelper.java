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

import java.util.ArrayList;
import java.util.List;
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
     * 从Palette中获取主要颜色，通过加权平均计算
     * 主要颜色占比50%，其它颜色按比例分配剩余权重
     */
    private static int getDominantColor(@NonNull Palette palette) {
        List<Integer> colors = new ArrayList<>();
        List<Double> weights = new ArrayList<>();
        
        // 收集所有可用的颜色及其权重
        if (palette.getDominantSwatch() != null) {
            colors.add(palette.getDominantSwatch().getRgb());
            weights.add(0.5); // 主要颜色占比50%
        }
        if (palette.getVibrantSwatch() != null) {
            colors.add(palette.getVibrantSwatch().getRgb());
            weights.add(0.125); // 其它颜色各占12.5%
        }
        if (palette.getMutedSwatch() != null) {
            colors.add(palette.getMutedSwatch().getRgb());
            weights.add(0.125);
        }
        if (palette.getLightVibrantSwatch() != null) {
            colors.add(palette.getLightVibrantSwatch().getRgb());
            weights.add(0.125);
        }
        if (palette.getDarkMutedSwatch() != null) {
            colors.add(palette.getDarkMutedSwatch().getRgb());
            weights.add(0.125);
        }
        
        // 如果没有可用颜色，返回透明色
        if (colors.isEmpty()) {
            return Color.TRANSPARENT;
        }
        
        // 如果只有主要颜色，则直接返回
        if (colors.size() == 1) {
            return colors.get(0);
        }
        
        // 计算加权平均值
        double red = 0, green = 0, blue = 0;
        double totalWeight = 0;
        
        for (int i = 0; i < colors.size(); i++) {
            int color = colors.get(i);
            double weight = weights.get(i);
            
            red += Color.red(color) * weight;
            green += Color.green(color) * weight;
            blue += Color.blue(color) * weight;
            totalWeight += weight;
        }
        
        // 确保总权重为1（由于浮点数精度问题）
        if (totalWeight > 0) {
            return Color.rgb((int) (red / totalWeight), (int) (green / totalWeight), (int) (blue / totalWeight));
        } else {
            return Color.TRANSPARENT;
        }
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
        int darkened80Percent = darkenColor(topColor);
        return new int[]{topColor, darkened80Percent};
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