package com.cappielloantonio.tempo.constants;

/**
 * 历史页面相关常量定义
 * 包含菜单项ID和其他常量
 */
public final class HistoryConstants {

    // 防止实例化
    private HistoryConstants() {
        throw new AssertionError("HistoryConstants class should not be instantiated");
    }

    /**
     * 历史页面菜单项ID
     */
    public static final class Menu {
        /** 下一首播放 */
        public static final int PLAY_NEXT = 0;

        /** 添加到队列 */
        public static final int ADD_TO_QUEUE = 1;

        /** 添加到播放列表 */
        public static final int ADD_TO_PLAYLIST = 2;

        /** 下载 */
        public static final int DOWNLOAD = 3;

        /** 从历史中移除 */
        public static final int REMOVE_FROM_HISTORY = 4;
    }

    /**
     * 历史页面菜单项标题
     */
    public static final class MenuTitle {
        /** 下一首播放 */
        public static final String PLAY_NEXT = "Play next";

        /** 添加到队列 */
        public static final String ADD_TO_QUEUE = "Add to queue";

        /** 添加到播放列表 */
        public static final String ADD_TO_PLAYLIST = "Add to playlist";

        /** 下载 */
        public static final String DOWNLOAD = "Download";

        /** 从历史中移除 */
        public static final String REMOVE_FROM_HISTORY = "Remove from history";
    }

    /**
     * Bundle键名常量
     */
    public static final class BundleKey {
        /** 媒体对象键名 */
        public static final String MEDIA = "media";
    }
}