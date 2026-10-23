# Reglas de ProGuard/R8 para CocinaGT.

# Mantener los modelos serializables de kotlinx.serialization.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class gt.cocina.cocinagt.model.** {
    *** Companion;
}
-keepclasseswithmembers class gt.cocina.cocinagt.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
