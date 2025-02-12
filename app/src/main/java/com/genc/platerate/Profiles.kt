import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.genc.platerate.BottomNavigationBar
import com.genc.platerate.CommentItemView
import com.genc.platerate.CommentViewModel
import com.genc.platerate.Screen
import com.google.firebase.auth.FirebaseUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    currentUser: FirebaseUser?,
    onBookmarkClick: () -> Unit,
    onNavigateToPlateRatingsScreen: (plateNumber: String) -> Unit,
    commentViewModel: CommentViewModel = viewModel(),
    onNavigateToCommentScreen: () -> Unit,
    onNavigateToHomeScreen: () -> Unit,
    onNavigateToSearchScreen: () -> Unit,
    currentScreen: Screen,
    onSignOutClick: () -> Unit,


    ) {

    LaunchedEffect(currentUser) {
        commentViewModel.fetchUserComments(currentUser)
    }


    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(top = 10.dp),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)

                    ) {



                        Spacer(modifier = Modifier.weight(1f)) // Pushes the bookmark icon to the right

                        // Bookmark Icon
                        IconButton(onClick = onBookmarkClick) {
                            Icon(Icons.Filled.Bookmark, contentDescription = "Bookmarks", tint = Color.LightGray)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Go back", tint = Color.LightGray)
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
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                items(commentViewModel.userComments) { commentItem ->
                    CommentItemView(
                        commentItem = commentItem,
                        onNavigateToPlateRatingsScreen = onNavigateToPlateRatingsScreen,
                        currentUser = currentUser,
                        commentViewModel = commentViewModel,
                        onDeleteClick = { commentId ->
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




