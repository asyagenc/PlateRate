package com.genc.platerate.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.genc.platerate.R


val provider = GoogleFont.Provider(
    providerAuthority ="com.google.android.gms.fonts",
    providerPackage ="com.google.android.gms.fonts",
    certificates = R.array.com_google_android_gms_fonts_certs


)

val fontName = GoogleFont("Montserrat")

val anekFontFamily= FontFamily(
    androidx.compose.ui.text.font.Font(
        R.font.anekdevanagari
    )
)

val robotoFontFamily= FontFamily(
    androidx.compose.ui.text.font.Font(
        R.font.robotoregular
    )
)

val opensansFontFamily= FontFamily(
    androidx.compose.ui.text.font.Font(
        R.font.opensansmedium

    )
)


val nunitosansFontFamily= FontFamily(
    androidx.compose.ui.text.font.Font(
        R.font.nunitosans
    )
)


val nunitosansLightFontFamily= FontFamily(
    androidx.compose.ui.text.font.Font(
        R.font.nunitosanslight
    )
)



val nunitosansMediumFontFamily= FontFamily(
    androidx.compose.ui.text.font.Font(
        R.font.nunitosansmed
    )
)


val nunitosansregFontFamily= FontFamily(
    androidx.compose.ui.text.font.Font(
        R.font.nunitosansreg
    )
)

val suseFontFamily= FontFamily(
    androidx.compose.ui.text.font.Font(
        R.font.suse
    )
)
val suseSemiBoldFontFamily= FontFamily(
    androidx.compose.ui.text.font.Font(
        R.font.susesemibold
    )
)


val montSerratFontFamily= FontFamily(
    androidx.compose.ui.text.font.Font(
        R.font.montserratvariablefont
    )
)


// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )

)