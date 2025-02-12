package com.genc.platerate

import android.content.res.Configuration
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.material.Text
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.DropdownMenu

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cottage
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genc.platerate.ui.theme.nunitosansMediumFontFamily
import com.google.firebase.auth.FirebaseUser

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(

    modifier: Modifier = Modifier,
    currentUser: FirebaseUser?,
    currentScreen: Screen,
    onProfileClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onNavigateToCommentScreen: () -> Unit,
    onNavigateToHomeScreen: () -> Unit,
    onNavigateToPlateRatingsScreen: (plateNumber: String) -> Unit,
    onNavigateToSearchScreen: () -> Unit,
    commentViewModel: CommentViewModel = viewModel()

) {

    LaunchedEffect(Unit) {
        commentViewModel.fetchAllComments(currentUser)
    }

    var isMenuExpanded by remember { mutableStateOf(false) }
    val iconSize = if (isTablet()) 30.dp else 24.dp


    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth()
                ,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                            Box(
                                modifier = Modifier
                                    .size(if (isTablet())50.dp else 40.dp)
                                    .clip(CircleShape)
                                    .background(color = Color.White)
                                    .border(2.5.dp, Color.LightGray, CircleShape)
                                    .clickable { onProfileClick() }
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.user5),
                                    contentDescription = "Profile Icon",
                                    modifier = Modifier
                                        .size(if (isTablet()) 40.dp else 30.dp)
                                        .align(Alignment.BottomCenter)
                                )
                            }

                        Spacer(modifier = Modifier.weight(1f))
                        MinimalDropdownMenu(
                            onDeleteAccountClick = onDeleteAccountClick,
                            isExpanded = isMenuExpanded,
                            onToggle = {isMenuExpanded= !isMenuExpanded}
                        )


                        IconButton(onClick = onSignOutClick) {
                            Icon(Icons.Filled.Logout, contentDescription = "Logout", tint = Color.LightGray,modifier=Modifier.size(iconSize))
                        }
                        



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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    items(commentViewModel.commentsList) { commentItem ->
                        CommentItemView(
                            commentItem = commentItem,
                            onNavigateToPlateRatingsScreen = onNavigateToPlateRatingsScreen,
                            currentUser = currentUser,
                            commentViewModel = commentViewModel,
                            onDeleteClick ={
                                commentId ->
                                commentViewModel.deleteComment(
                                    currentUserId = currentUser?.uid?:"" ,
                                    commentId=commentId,
                                    onDeleteSuccess = {
                                        println("Yorum başarıyla silindi!")
                                    },
                                    onDeleteFailure = { errorMessage ->
                                        println("Yorum silinirken hata: $errorMessage")
                                    }

                                )
                            }


                        )
                    }
                }
            }
        }



    )


}


data class CommentItem(
    var commentId: String = "",
    val licensePlate: String = "",
    val comment: String = "",
    var likesCount: Int = 0,
    var dislikesCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val dislikedBy: List<String> = emptyList(),
    var isLiked: MutableState<Boolean> = mutableStateOf(false),
    var isDisliked: MutableState<Boolean> = mutableStateOf(false),
    var isBookmarked: MutableState<Boolean> = mutableStateOf(false),
    var isPlateBookmarked: MutableState<Boolean> = mutableStateOf(false),
    val userId:String





)


@Composable
fun HorizontalDivider(thickness: Dp = 1.dp, color: Color = Color.LightGray) {
    Divider(
        modifier = Modifier
            .fillMaxWidth()
            .height(thickness),
        color = color
    )
}

@Composable
fun calculateDividerThickness(commentLength: Int): Dp {
    return when {
        commentLength > 100 -> 2.dp
        commentLength > 50 -> 1.5.dp
        else -> 1.dp
    }
}

@Composable
fun MinimalDropdownMenu(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onDeleteAccountClick: () -> Unit)
{
    val iconSize = if (isTablet()) 30.dp else 24.dp

    Box {
        IconButton(onClick = { onToggle() }) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.LightGray,modifier=Modifier.size(iconSize))
        }
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { onToggle() }
        ) {
            DropdownMenuItem(
                text = { Text("Hesabı Sil", style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = Color.DarkGray,
                    fontFamily = nunitosansMediumFontFamily

                )) },

                onClick = {
                    onToggle()
                    onDeleteAccountClick()
                }
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentScreen: Screen,
    onNavigateToHomeScreen: () -> Unit,
    onNavigateToSearchScreen: () -> Unit,
    onNavigateToCommentScreen: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val selectedColor = Color(3, 57, 108)
        val defaultColor = Color.LightGray

        IconButton(
            onClick = onNavigateToHomeScreen,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.Cottage, contentDescription = "Home", tint = if (currentScreen == Screen.Home) selectedColor else defaultColor)
        }
        IconButton(
            onClick = onNavigateToSearchScreen,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.Search, contentDescription = "Search", tint = if (currentScreen == Screen.Search) selectedColor else defaultColor)
        }
        IconButton(
            onClick = onNavigateToCommentScreen,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Comment", tint = if (currentScreen == Screen.Comment) selectedColor else defaultColor)
        }
    }
}


@Composable
fun isTablet(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp >= 600
}

@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}







// Example usage
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    HomeScreen(
        currentUser = null,
        currentScreen = Screen.Home,  // Set the current screen to Home
        onSignOutClick = {},
        onNavigateToCommentScreen = {},
        onNavigateToPlateRatingsScreen = {},
        onNavigateToHomeScreen = {},
        onProfileClick={},
        onDeleteAccountClick = {},
        onNavigateToSearchScreen = {}
    )
}