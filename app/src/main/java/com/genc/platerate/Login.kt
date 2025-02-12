package com.genc.platerate

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genc.platerate.ui.theme.nunitosansMediumFontFamily


@Composable
fun LoginScreen(modifier: Modifier=Modifier,
               onSignInClick:() -> Unit
){
 Surface(
     modifier = modifier
         .fillMaxSize()


 ) {
     Column(
         modifier= Modifier
             .fillMaxSize(),
         horizontalAlignment = Alignment.CenterHorizontally,
         verticalArrangement = Arrangement.Center
     ){
         val image: Painter = painterResource(id = R.drawable.google) // Ensure this matches your image file name

         Spacer(modifier = Modifier.size(32.dp))
         ElevatedButton(onClick = { onSignInClick() },
             colors = ButtonDefaults.buttonColors
                 (contentColor = Color.Black,
                         containerColor = Color.White )


         ) {
             Image(
                 painter = image,
                 contentDescription = null,
                 modifier = Modifier
                     .size(28.dp)
                     .padding(end = 8.dp)

             )
             Text(
                 text = "Continue with Google",
                 fontFamily = nunitosansMediumFontFamily,
                 fontWeight = FontWeight.W500,
                 fontSize = 20.sp
             )
         }
         }

     }

 }





@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    LoginScreen(onSignInClick = {})
}


