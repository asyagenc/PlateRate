package com.genc.platerate

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genc.platerate.ui.theme.nunitosansMediumFontFamily
import com.genc.platerate.ui.theme.nunitosansregFontFamily
import com.genc.platerate.ui.theme.suseFontFamily
import com.genc.platerate.ui.theme.suseSemiBoldFontFamily

@Composable
fun LicensePlateView(
    licensePlate: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    borderColor: Color = Color.Gray,
    width: Dp = defaultWidth(),
    height: Dp = defaultHeight(),
    roundedCornerShape: RoundedCornerShape = RoundedCornerShape(8.dp)
) {
    val formattedPlate = formatTurkishLicensePlate(licensePlate)

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .background(backgroundColor, shape = roundedCornerShape)
            .border(2.dp, borderColor, shape = roundedCornerShape)
            .padding(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(if (isTablet()) 40.dp else 25.dp)
                    .background(Color(0xFF0033A0))
                    .padding(bottom = 4.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = "TR",
                    color = Color.White,
                    style = TextStyle(
                        fontSize = if (isTablet()) 14.sp else 9.sp,
                        fontWeight = FontWeight.Bold,
                    )
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = formattedPlate,
                style = TextStyle(
                    fontSize = if (isTablet()) 24.sp else 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = Color.DarkGray,
                    fontFamily = nunitosansregFontFamily

                ),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 7.dp)
            )
        }
    }
}

@Composable
fun defaultWidth(): Dp = if (isTablet()) 200.dp else 130.dp

@Composable
fun defaultHeight(): Dp = if (isTablet()) 50.dp else 34.dp


fun formatTurkishLicensePlate(input: String): String {
    val cleanedInput = input.replace("\\s".toRegex(), "").uppercase()
    val regex = "^(\\d{2})([A-Z]{1,3})(\\d{2,4})$".toRegex()

    return if (regex.matches(cleanedInput)) {
        val matchResult = regex.find(cleanedInput)
        val (digits, letters, remainingDigits) = matchResult!!.destructured
        "$digits $letters $remainingDigits"
    } else {
        input
    }
}
