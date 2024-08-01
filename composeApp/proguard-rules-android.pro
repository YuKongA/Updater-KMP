-dontwarn org.slf4j.impl.StaticLoggerBinder

-keep class * implements android.os.Parcelable {
   public static final android.os.Parcelable$Creator *;
}