# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keepattributes SourceFile, LineNumberTable
-keepattributes Signature, InnerClasses, EnclosingMethod
-keep public class * extends java.lang.Exception

# Retrofit
-keep class retrofit2.** { *; }
-keep class **.reflect.TypeToken { *; }
-keep class * extends **.reflect.TypeToken

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# 保持所有模型类的字段不被混淆（使用Gson注解的类）
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# 保持具体的评论相关模型类
-keep class com.cappielloantonio.tempo.subsonic.models.SongComment { *; }
-keep class com.cappielloantonio.tempo.subsonic.models.SongComments { *; }
-keep class com.cappielloantonio.tempo.subsonic.base.ApiResponse { *; }
-keep class com.cappielloantonio.tempo.subsonic.base.ApiResponse$* { *; }

# 保持接口实现
-keep class com.cappielloantonio.tempo.subsonic.api.comments.CommentsService { *; }
-keep class com.cappielloantonio.tempo.subsonic.api.comments.CommentsClient { *; }