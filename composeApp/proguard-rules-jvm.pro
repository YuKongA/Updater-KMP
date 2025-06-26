-dontwarn org.slf4j.helpers.SubstituteLogger
-dontwarn okhttp3.internal.platform.**
-dontwarn io.ktor.network.sockets.SocketBase**
-dontwarn kotlinx.datetime.**

-keep class com.sun.jna.** { *; }
-keep class * implements com.sun.jna.** { *; }