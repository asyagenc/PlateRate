package com.genc.platerate

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.Text

import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.genc.platerate.ui.theme.nunitosansMediumFontFamily
import com.genc.platerate.ui.theme.nunitosansregFontFamily
import com.google.firebase.auth.FirebaseUser

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    currentUser: FirebaseUser?,
    commentViewModel: CommentViewModel = viewModel(),
    currentScreen: Screen,
    onNavigateToCommentScreen: () -> Unit,
    onNavigateToHomeScreen: () -> Unit,
    onNavigateToSearchScreen: () -> Unit,
    onNavigateToPlateRatingsScreen: (plateNumber: String) -> Unit,
    onNavigateToLoginScreen: () -> Unit,
    previousSearches: MutableList<String>
) {
    val plateNumberState = remember { mutableStateOf("") }
    val previousSearches = remember { mutableStateListOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val configuration = LocalConfiguration.current

    val textSize = if (isTablet()) 22.sp else 16.sp
    val iconSize = if (isTablet()) 28.dp else 24.dp
    val spacing = if (isTablet()) 20.dp else 8.dp
    val padding = if (isTablet()) 32.dp else 20.dp
    val searchBarHeight = if (isTablet()) 150.dp else 85.dp
    val columnWidth = if (isTablet()) (configuration.screenWidthDp * 0.7).dp else Dp.Unspecified

    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { userId ->
            commentViewModel.fetchSearchHistory(userId) { searches ->
                previousSearches.clear()
                previousSearches.addAll(searches)
            }
        }
    }
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentScreen = currentScreen,
                onNavigateToHomeScreen = onNavigateToHomeScreen,
                onNavigateToSearchScreen = onNavigateToSearchScreen,
                onNavigateToCommentScreen = onNavigateToCommentScreen,
                onLogoutClick = onNavigateToLoginScreen
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
                        .padding(padding)
                        .width(columnWidth)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(searchBarHeight)
                            .padding(bottom = spacing),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        OutlinedTextField(
                            value = plateNumberState.value,
                            onValueChange = { plateNumberState.value = it },
                            label = { Text("Ara",
                                fontFamily = nunitosansMediumFontFamily ,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.size(40.dp)
                            ) },
                            leadingIcon = {
                                Icon(Icons.Filled.Search, contentDescription = "Search Icon",
                                    modifier = Modifier.size(iconSize))
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    val search = plateNumberState.value.trim()
                                    if (search.isNotEmpty()) {

                                        currentUser?.uid?.let { userId ->
                                            commentViewModel.addSearchToHistory(userId, search)
                                        }

                                        previousSearches.remove(search)
                                        previousSearches.add(0,search)
                                    }
                                    onNavigateToPlateRatingsScreen(search)
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    plateNumberState.value = ""
                                }
                            ),
                            textStyle = TextStyle(
                                fontSize = if (isTablet()) 24.sp else 16.sp,
                                fontFamily = nunitosansregFontFamily,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .background(MaterialTheme.colorScheme.background),
                            shape = RoundedCornerShape(20.dp),


                        )
                    }
                    Spacer(modifier = Modifier.height(spacing))
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(previousSearches) { search ->
                            Row(
                                modifier = Modifier
                                    .clickable { onNavigateToPlateRatingsScreen(search) }
                                    .fillMaxWidth()
                                    .padding(vertical = spacing),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    fontFamily = nunitosansregFontFamily ,
                                    fontWeight = FontWeight.SemiBold,
                                    text = search,
                                    fontSize = textSize,
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(
                                    onClick = {
                                        previousSearches.remove(search)
                                        currentUser?.uid?.let { userId->
                                            commentViewModel.removeSearchFromHistory(userId,search)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "Delete",

                                        modifier= Modifier.size(iconSize)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
