# Add project specific ProGuard rules here.
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

-dontwarn javax.annotation.**
-keep class com.omnipaws.calendar.** { *; }
