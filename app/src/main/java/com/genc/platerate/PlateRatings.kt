package com.genc.platerate

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues

import androidx.compose.foundation.layout.Row
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genc.platerate.ui.theme.nunitosansregFontFamily
import com.genc.platerate.ui.theme.opensansFontFamily
import com.genc.platerate.ui.theme.suseSemiBoldFontFamily
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


fun normalizePlateNumber(plateNumber: String): String {
    return plateNumber.replace("\\s".toRegex(), "").uppercase()
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PlateRatingsScreen(
    modifier: Modifier = Modifier,
    onNavigateToCommentScreen: () -> Unit,
    onNavigateToHomeScreen: () -> Unit,
    onNavigateToSearchScreen: () -> Unit,
    plateNumber: String,
    onBack: () -> Unit,
    currentUser: FirebaseUser?,
    onSignOutClick: () -> Unit,
    currentScreen: Screen,
    commentViewModel: CommentViewModel = viewModel(),

    onNavigateToPlateRatingsScreen: (String) -> Unit
) {
    val normalizedPlateNumber = normalizePlateNumber(plateNumber)
    val isPlateBookmarked = remember { mutableStateOf(false) }

    LaunchedEffect(plateNumber) {
        commentViewModel.fetchPlateComments(normalizedPlateNumber, currentUser)

        currentUser?.let { user ->
            commentViewModel.ensureUserProfileExists(user.uid) {
                commentViewModel.isPlateBookmarked(normalizedPlateNumber, user.uid) { isBookmarked ->
                    isPlateBookmarked.value = isBookmarked
                }
            }
        }
    }


    val topBarHeight = if (isTablet()) 100.dp else 84.dp
    val topBarPadding = if (isTablet()) 25.dp else 15.dp


    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(topBarHeight)
                    .padding(top = topBarPadding),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = if (isTablet()) 24.dp else 0.dp)

                    ) {
                        Spacer(modifier = Modifier.weight(1f))

                        Box(
                            modifier = Modifier
                                .padding(end = 0.dp, top = 10.dp) // Sağa ve aşağıya kaydırma
                        ) {
                            LicensePlateView(
                                licensePlate = plateNumber,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }


                        Spacer(modifier = Modifier.width(0.dp))



                        IconButton(onClick = {
                            isPlateBookmarked.value = !isPlateBookmarked.value

                            currentUser?.let { user ->
                                commentViewModel.ensureUserProfileExists(user.uid) {
                                    commentViewModel.handlePlateBookmark(
                                        plateNumber = normalizedPlateNumber,
                                        currentUserId = user.uid,
                                        isBookmarked = isPlateBookmarked.value
                                    )
                                }
                            }
                        },
                                modifier = Modifier
                                .padding(top = 10.dp)) {
                            Icon(
                                imageVector = if (isPlateBookmarked.value) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                tint = if (isPlateBookmarked.value) Color(3, 57, 108) else Color.Gray,
                                contentDescription = "PlateBookmark",
                                modifier = Modifier
                                    .size(if (isTablet()) 28.dp else 24.dp)

                            )
                        }

                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Go back", tint = Color.LightGray,modifier = Modifier.size(if (isTablet()) 28.dp else 24.dp) // Simge boyutu
                        )
                    }
                }

                )
        },
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
                        .padding(horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.size(1.dp))


                    if (commentViewModel.plateComments.isEmpty()) {
                        Text("Henüz yorum yok.",
                            modifier = Modifier
                            .align(Alignment.CenterHorizontally),
                            fontFamily = nunitosansregFontFamily
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            items(commentViewModel.plateComments) { commentItem ->
                                CommentItemView(
                                    commentItem = commentItem,
                                    currentUser = currentUser,
                                    onNavigateToPlateRatingsScreen = onNavigateToPlateRatingsScreen,
                                    commentViewModel = commentViewModel,
                                    onDeleteClick = {
                                        commentId ->
                                        currentUser?.uid?.let { userId ->
                                            commentViewModel.deleteComment(
                                                currentUserId = userId,
                                                commentId = commentId,
                                                onDeleteSuccess = {
                                                    println("Yorum başarıyla silindi!")
                                                },
                                                onDeleteFailure = { errorMessage ->

                                                    println("Yorum silinirken hata: $errorMessage")
                                                }
                                            )
                                        } ?: run {
                                            // currentUser null ise
                                            println("Kullanıcı oturumu yok, silme işlemi yapılamadı.")
                                        }
                                    }

                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

