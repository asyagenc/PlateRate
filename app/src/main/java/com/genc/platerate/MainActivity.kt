package com.genc.platerate

import ProfileScreen
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.genc.platerate.ui.theme.PlateRateTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch



const val WEB_CLIENT_ID = "10287691161-1mkhjroi1gvsdltg15dlvbjjhggg7pnh.apps.googleusercontent.com"

enum class Screen {
    PlateRatings,
    Login,
    Home,
    Comment,
    Search,
    Profile,
    Bookmarks
}

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore




    @OptIn(ExperimentalMaterial3Api::class)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        setContent {

            var isDarkMode by remember { mutableStateOf(false) }
            val previousSearches = remember { mutableStateListOf<String>() }
            val licensePlateState = remember { mutableStateOf("") }
            val onLicensePlateChange = {newPlate: String ->
                licensePlateState.value=newPlate

            }
            var expanded by remember { mutableStateOf(false) }
            val commentViewModel: CommentViewModel = viewModel()




            PlateRateTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val credentialManager = CredentialManager.create(context)

                val startDestination = if (auth.currentUser == null) Screen.Login.name else Screen.Home.name

                val commentViewModel: CommentViewModel = viewModel()  // Shared view model instance



                NavHost(navController = navController, startDestination = startDestination) {
                    composable(Screen.Login.name) {
                        LoginScreen(onSignInClick = {
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(WEB_CLIENT_ID)
                                .build()

                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()

                            scope.launch {
                                try {
                                    val result = credentialManager.getCredential(
                                        context = context,
                                        request = request
                                    )
                                    val credential = result.credential
                                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                    val googleIdToken = googleIdTokenCredential.idToken

                                    val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                                    auth.signInWithCredential(firebaseCredential)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                navController.popBackStack()
                                                navController.navigate(Screen.Home.name)
                                            }
                                        }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    e.printStackTrace()
                                }
                            }
                        })
                    }
                    composable(Screen.Home.name) {
                        HomeScreen(
                            currentUser = auth.currentUser,
                            commentViewModel = commentViewModel,
                            currentScreen = Screen.Home,
                            onSignOutClick = {
                                auth.signOut()
                                scope.launch {
                                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                                }
                                navController.popBackStack()
                                navController.navigate(Screen.Login.name)
                            },
                            onProfileClick = {
                                navController.navigate(Screen.Profile.name)
                            },
                            onNavigateToCommentScreen = {
                                navController.navigate(Screen.Comment.name)
                            },
                            onNavigateToPlateRatingsScreen = { plateNumber ->
                                navController.navigate("${Screen.PlateRatings.name}/$plateNumber")
                            },
                            onNavigateToSearchScreen = {
                                navController.navigate(Screen.Search.name)
                            },
                            onNavigateToHomeScreen = {
                                navController.navigate(Screen.Home.name)
                            },

                            onDeleteAccountClick = {
                                auth.currentUser?.delete()?.addOnCompleteListener { task ->
                                    if (task.isSuccessful){
                                        Toast.makeText(this@MainActivity,"Account deleted successfullt.",Toast.LENGTH_SHORT).show()
                                        navController.navigate(Screen.Login.name)
                                    }else{
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Failed to delete account: ${task.exception?.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                            }




                        )
                    }

                    composable(Screen.Profile.name){
                        ProfileScreen(
                            onBack = {
                                navController.popBackStack()
                            },
                            onBookmarkClick = {
                                              navController.navigate(Screen.Bookmarks.name)
                            },
                            currentUser = auth.currentUser,
                            onNavigateToPlateRatingsScreen = { plateNumber ->
                                navController.navigate("${Screen.PlateRatings.name}/$plateNumber")
                            },
                            onNavigateToHomeScreen = {
                                navController.navigate(Screen.Home.name)
                            },
                            onNavigateToSearchScreen = {
                                navController.navigate(Screen.Search.name)
                            },
                            onNavigateToCommentScreen = {
                                navController.navigate(Screen.Comment.name)
                            },
                            onSignOutClick = {
                                auth.signOut()
                                navController.navigate(Screen.Login.name)
                            },
                            currentScreen = Screen.Profile


                        )
                    }

                    composable(Screen.Comment.name) {
                        CommentScreen(
                            currentUser = auth.currentUser,
                            onBack = { navController.popBackStack() },
                            onSubmitComment = { licensePlate, normalizedPlate, comment ->
                                val commentData = hashMapOf(
                                    "licensePlate" to licensePlate,
                                    "normalizedPlate" to normalizedPlate,
                                    "comment" to comment,
                                    "timestamp" to System.currentTimeMillis(),
                                    "likesCount" to 0,
                                    "dislikesCount" to 0,
                                    "likedBy" to emptyList<String>(),
                                    "dislikedBy" to emptyList<String>(),
                                    "bookmarkedBy" to emptyList<String>(),
                                    "plateBookmarkedBy" to emptyList<String>(),
                                    "userId" to auth.currentUser?.uid.orEmpty()  // Ensure the user ID is stored
                                )

                                firestore.collection("comments")
                                    .add(commentData)
                                    .addOnSuccessListener { documentReference ->
                                        val commentId = documentReference.id
                                        auth.currentUser?.uid?.let { userId ->
                                            addUserComment(userId, commentId)
                                        }
                                        Toast.makeText(context, "Comment added", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Error adding comment: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            },
                            currentScreen = Screen.Comment,
                            onNavigateToHomeScreen = { navController.navigate(Screen.Home.name) },
                            onNavigateToSearchScreen = { navController.navigate(Screen.Search.name) },
                            onNavigateToCommentScreen = { navController.navigate(Screen.Comment.name) },
                            onSignOutClick = {
                                auth.signOut()
                                navController.navigate(Screen.Login.name)
                            },
                            onNavigateToPlateRatingsScreen = { plateNumber ->
                                navController.navigate("${Screen.PlateRatings.name}/$plateNumber")
                            },
                            onLicensePlateChange = onLicensePlateChange
                        )
                    }



                    composable(Screen.Search.name) {
                        SearchScreen(
                            currentScreen = Screen.Search,
                            commentViewModel = commentViewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onNavigateToPlateRatingsScreen = { plateNumber ->
                                navController.navigate("${Screen.PlateRatings.name}/$plateNumber")
                            },
                            onNavigateToHomeScreen = {
                                navController.navigate(Screen.Home.name)
                            },
                            onNavigateToSearchScreen = {
                                navController.navigate(Screen.Search.name)
                            },
                            onNavigateToCommentScreen = {
                                navController.navigate(Screen.Comment.name)
                            },
                            onNavigateToLoginScreen = {
                                navController.navigate(Screen.Login.name)
                            },
                            previousSearches = previousSearches,
                            currentUser = auth.currentUser
                        )
                    }
                    composable("${Screen.PlateRatings.name}/{plateNumber}") { backStackEntry ->
                        val plateNumber = backStackEntry.arguments?.getString("plateNumber") ?: ""
                        PlateRatingsScreen(
                            plateNumber = plateNumber,
                            onBack = {
                                navController.popBackStack()
                            },
                            onNavigateToPlateRatingsScreen = {
                                navController.navigate("${Screen.PlateRatings.name}/$plateNumber")

                            },

                            onNavigateToHomeScreen = {
                                navController.navigate(Screen.Home.name)
                            },
                            onNavigateToSearchScreen = {
                                navController.navigate(Screen.Search.name)
                            },
                            onNavigateToCommentScreen = {
                                navController.navigate(Screen.Comment.name)
                            },
                            currentScreen = Screen.PlateRatings,
                            onSignOutClick = {
                                auth.signOut()
                                navController.navigate(Screen.Login.name)
                            },
                            currentUser = auth.currentUser,

                        )
                    }
                    composable(Screen.Bookmarks.name){

                        BookmarkScreen(
                            commentViewModel=commentViewModel,
                            onBack = {
                                navController.popBackStack()
                            },
                            currentUser = auth.currentUser,
                            onNavigateToPlateRatingsScreen = {plateNumber->
                                navController.navigate("${Screen.PlateRatings.name}/$plateNumber")

                            },



                        )
                    }
                }
            }
        }
    }

}





fun getBookmarkedComments(userId: String, onResult: (List<String>) -> Unit) {
    val db = Firebase.firestore
    db.collection("comments")
        .whereArrayContains("bookmarkedBy", userId)
        .get()
        .addOnSuccessListener { snapshot ->
            val bookmarkedComments = snapshot.documents.map { it.id }
            onResult(bookmarkedComments)
        }
        .addOnFailureListener { e ->
            println("Error getting bookmarked comments: ${e.message}")
            onResult(emptyList())
        }
}






