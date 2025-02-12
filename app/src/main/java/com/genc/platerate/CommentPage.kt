package com.genc.platerate

import android.graphics.drawable.Icon
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.Text

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import com.genc.platerate.ui.theme.nunitosansMediumFontFamily
import com.genc.platerate.ui.theme.opensansFontFamily
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

fun isValidTurkishLicensePlate(licensePlate: String): Boolean {
    val regex = "^(\\d{2})\\s([A-Z]{1,3})\\s(\\d{2,4})$".toRegex(RegexOption.IGNORE_CASE)
    return regex.matches(licensePlate)
}





@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CommentScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    currentUser:FirebaseUser?,
    onSubmitComment: (licensePlate: String, normalizedPlate: String, comment: String) -> Unit,
    currentScreen: Screen,
    onNavigateToCommentScreen: () -> Unit,
    onNavigateToHomeScreen: () -> Unit,
    onNavigateToSearchScreen: () -> Unit,
    onSignOutClick: () -> Unit,
    onLicensePlateChange: (String) -> Unit,
    onNavigateToPlateRatingsScreen: (plateNumber: String) -> Unit
) {
    val textStyle = TextStyle(
        fontFamily = nunitosansMediumFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp
    )

    val licensePlateState = remember { mutableStateOf("") }
    val commentState = remember { mutableStateOf("") }
    val errorMessage = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val showError = remember { mutableStateOf(false) }

    val horizontalPadding = if (isTablet()) 64.dp else 40.dp
    val fieldWidthFraction = if (isTablet()) 3f else 1f
    val licensePlateHeight = if (isTablet()) 80.dp else 65.dp
    val commentFieldHeight = if (isTablet()) 270.dp else 170.dp


    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentScreen = currentScreen,
                onNavigateToHomeScreen = onNavigateToHomeScreen,
                onNavigateToSearchScreen = onNavigateToSearchScreen,
                onNavigateToCommentScreen = onNavigateToCommentScreen,
                onLogoutClick = onSignOutClick
            )
        },
        content = { paddingValues ->
            Surface(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = horizontalPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.size(24.dp))

                    EditableLicensePlateView(
                        licensePlate = licensePlateState.value,
                        onLicensePlateChange = {
                            licensePlateState.value = it

                        },
                        modifier = Modifier
                            .fillMaxWidth(fieldWidthFraction)
                            .height(licensePlateHeight)
                            .padding(vertical = 8.dp)


                    )

                    OutlinedTextField(
                        value = commentState.value,
                        onValueChange = { commentState.value = it },
                        label = { Text("Yorum") },
                        textStyle =TextStyle(
                            fontFamily= nunitosansMediumFontFamily,
                            fontSize = if (isTablet()) 24.sp else 16.sp,
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth(fieldWidthFraction)
                            .height(commentFieldHeight),
                    )

                    Spacer(modifier = Modifier.size(16.dp))
                    Button(
                        onClick = {
                            if (isValidTurkishLicensePlate(licensePlateState.value) && commentState.value.isNotEmpty()) {
                                scope.launch {
                                    val normalizedPlate = normalizePlateNumber(licensePlateState.value)
                                    onSubmitComment(licensePlateState.value, normalizedPlate, commentState.value)
                                    commentState.value = ""
                                    licensePlateState.value = ""
                                    errorMessage.value = ""
                                    showError.value = false
                                }
                            } else {
                                errorMessage.value = "Geçerli bir plaka veya yorum giriniz. "
                                showError.value = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor =if (isValidTurkishLicensePlate(licensePlateState.value) && commentState.value.isNotEmpty()) Color(3, 57, 108) else Color.LightGray,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .padding(horizontal = if (isTablet()) 150.dp else 100.dp)
                            .fillMaxWidth(fieldWidthFraction)
                            .height(if (isTablet()) 60.dp else 50.dp)
                    ) {
                        Text("Paylaş", color = Color.White, fontSize = if (isTablet()) 20.sp else 16.sp)
                    }


                    Spacer(modifier = Modifier.size(10.dp))

                    AnimatedVisibility(visible = showError.value) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fieldWidthFraction)
                                .padding(top = 8.dp)
                                .background(
                                    Color(0xFFFFE0E0),
                                    shape = RoundedCornerShape(12.dp)
                                )  // Arka plan rengi ve köşelerin yumuşatılması
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                fontFamily = nunitosansMediumFontFamily,
                                fontWeight = FontWeight.Bold,
                                text = errorMessage.value,
                                color = Color(0xFFCC0000),
                                style = textStyle.copy(fontSize = if (isTablet()) 16.sp else 14.sp)

                            )
                        }
                        LaunchedEffect(showError.value) {
                            delay(4000)
                            showError.value = false
                            errorMessage.value = ""
                        }
                    }

                }
            }
        }
    )
}


private suspend fun hasComments(plateNumber: String): Boolean {
    val db = Firebase.firestore
    val result = db.collection("comments")
        .whereEqualTo("licensePlate", plateNumber)
        .get()
        .await()
    return !result.isEmpty
}

@Composable
fun EditableLicensePlateView(
    licensePlate: String,
    onLicensePlateChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)  // Adjusted to the specified height
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .border(2.dp, Color.Gray, shape = RoundedCornerShape(8.dp))
            .padding(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically  // Vertically centers both the TR box and the input text
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(if (isTablet())45.dp else 25.dp )
                    .background(Color(0xFF0033A0))
                    .paddingFromBaseline(bottom = 4.dp)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center  // Centers the "TR" text vertically within the box
            ) {
                Text(
                    text = "TR",
                    color = Color.White,
                    style = TextStyle(
                        fontSize = if (isTablet())18.sp else 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                )
            }
            Spacer(modifier = Modifier.width(4.dp))  // Adjust the width of this Spacer to change the gap between "TR" and the input text

            BasicTextField(
                value = licensePlate,
                onValueChange = onLicensePlateChange,
                textStyle = TextStyle(

                    fontSize = if (isTablet()) 24.sp else 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = Color.Black,
                    fontFamily = nunitosansMediumFontFamily

                ),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),  // Matches the height of the surrounding box
                singleLine = true,
                decorationBox = { innerTextField ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically // Ensures the text and placeholder are vertically centered
                    ){

                        innerTextField()
                    }
                }
            )
            if (licensePlate.isNotEmpty()) {
                IconButton(
                    onClick = { onLicensePlateChange("") },
                    modifier = Modifier.padding(end = 4.dp) // Add padding to position the icon correctly
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,  // Use a close icon
                        contentDescription = "Clear text",
                        tint = Color.Gray  // Set icon color
                    )
                }
            }

        }


    }
}

fun addUserComment(userId: String, commentId: String) {
    val db = Firebase.firestore
    val userProfileRef = db.collection("profiles").document(userId)

    db.runTransaction { transaction ->
        val snapshot = transaction.get(userProfileRef)
        val userComments = snapshot.get("userComments") as? MutableList<String> ?: mutableListOf()
        userComments.add(commentId)
        transaction.update(userProfileRef, "userComments", userComments)
    }.addOnSuccessListener {
        println("Comment ID successfully added to user profile!")
    }.addOnFailureListener { e ->
        println("Failed to update user profile with comment ID: ${e.message}")
    }
}


@Preview
@Composable
fun PreviewEditableLicensePlateView() {
    val licensePlate = remember { TextFieldValue("34 AB 1234") }
    EditableLicensePlateView(
        licensePlate = licensePlate.text,
        onLicensePlateChange = {}
    )
}