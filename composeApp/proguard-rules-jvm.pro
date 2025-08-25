-dontwarn org.slf4j.helpers.SubstituteLogger
-dontwarn okhttp3.internal.platform.**
-dontwarn io.ktor.network.sockets.SocketBase**
-dontwarn kotlinx.datetime.**
-dontwarn org.apache.commons.compress.**

-keep class com.sun.jna.** { *; }
-keep class * implements com.sun.jna.** { *; }
-keep class dev.whyoleg.cryptography.providers.jdk.JdkCryptographyProviderContainer

