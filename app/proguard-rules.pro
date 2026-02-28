# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Missing classes for R8
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
-dontwarn org.checkerframework.**
-dontwarn com.google.auto.value.**
-dontwarn com.google.crypto.tink.**
