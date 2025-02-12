package com.genc.platerate
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.genc.platerate.ui.theme.nunitosansregFontFamily
import com.genc.platerate.ui.theme.opensansFontFamily
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkScreen(
    onBack: () -> Unit,
    currentUser: FirebaseUser?,
    onNavigateToPlateRatingsScreen: (String) -> Unit,
    commentViewModel: CommentViewModel = viewModel()
) {
    val db = Firebase.firestore
    val bookmarkedComments = remember { mutableStateListOf<CommentItem>() }
    val bookmarkedPlates = remember { mutableStateListOf<String>() }  // List of plate numbers
    var selectedTabIndex by remember { mutableStateOf(0) }



    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            // Fetch bookmarked comments
            getBookmarkedComments(user.uid) { bookmarkedCommentIds ->
                if (bookmarkedCommentIds.isNotEmpty()) {
                    db.collection("comments")
                        .whereIn(FieldPath.documentId(), bookmarkedCommentIds)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            bookmarkedComments.clear()
                            for (document in snapshot.documents) {
                                val commentItem = commentViewModel.getParsedComment(document.id, document.data, user)
                                bookmarkedComments.add(commentItem)
                            }
                        }
                        .addOnFailureListener { e ->
                            println("Error fetching comments: ${e.message}")
                        }
                } else {
                    // Handle the case where there are no bookmarked comments
                    bookmarkedComments.clear()
                    println("No bookmarked comments to fetch.")

                }

            }
        }
    }

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            // Fetch bookmarked plates (only plate numbers)
            commentViewModel.getBookmarkedPlates(user.uid) { bookmarkedPlateNumbers ->
                bookmarkedPlates.clear()
                bookmarkedPlates.addAll(bookmarkedPlateNumbers)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Filled.ArrowBackIosNew,
                            contentDescription = "Go back",
                            tint = Color.LightGray
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.comment2),
                            contentDescription = "Bookmarked Comments",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.driver_license),
                            contentDescription = "Bookmarked Plates",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
            }

            when (selectedTabIndex) {
                0 -> BookmarkedCommentsView(
                    bookmarkedComments = bookmarkedComments,
                    onNavigateToPlateRatingsScreen = onNavigateToPlateRatingsScreen,
                    currentUser = currentUser
                )
                1 -> BookmarkedPlatesView(
                    bookmarkedPlates = bookmarkedPlates,  // List of Strings
                    onNavigateToPlateRatingsScreen = onNavigateToPlateRatingsScreen
                )
            }
        }
    }
}

@Composable
fun BookmarkedCommentsView(
    bookmarkedComments: List<CommentItem>,
    onNavigateToPlateRatingsScreen: (String) -> Unit,
    currentUser: FirebaseUser?
) {
    val commentViewModel: CommentViewModel = viewModel()

    if (bookmarkedComments.isEmpty()) {
        Text(
            text = "Henüz kaydedilmiş yorum yok.",
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            fontSize = 16.sp,
            fontFamily = nunitosansregFontFamily,
            color = Color.Gray
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(bookmarkedComments.size) { index ->
                val commentItem = bookmarkedComments[index]
                CommentItemView(
                    commentItem = commentItem,
                    onNavigateToPlateRatingsScreen = onNavigateToPlateRatingsScreen,
                    currentUser = currentUser,
                    commentViewModel =commentViewModel,
                    onDeleteClick = { commentId ->
                        currentUser?.uid?.let { userId ->
                            commentViewModel.deleteComment(
                                currentUserId = userId,
                                commentId = commentId,
                                onDeleteSuccess = {
                                    println("Yorum başarıyla silindi!")
                                    bookmarkedComments.filter { it.commentId !== commentId }
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

@Composable
fun BookmarkedPlatesView(
    bookmarkedPlates: List<String>,  // List of plate numbers
    onNavigateToPlateRatingsScreen: (String) -> Unit
) {
    if (bookmarkedPlates.isEmpty())
        {

        Text(
            text = "Henüz kaydedilmiş plaka yok.",
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            fontSize = 16.sp,
            fontFamily = nunitosansregFontFamily,
            color = Color.Gray
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // 2 columns for two license plates side by side
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(bookmarkedPlates) { plateNumber ->
                LicensePlateView(
                    licensePlate = plateNumber,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            onNavigateToPlateRatingsScreen(plateNumber)
                        }
                )
            }
        }
    }
}
