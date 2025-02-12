package com.genc.platerate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.DropdownMenu
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genc.platerate.ui.theme.nunitosansMediumFontFamily
import com.genc.platerate.ui.theme.nunitosansregFontFamily
import com.google.firebase.auth.FirebaseUser


@Composable
fun CommentItemView(
    commentItem: CommentItem,
    currentUser: FirebaseUser?,
    commentViewModel: CommentViewModel,
    onDeleteClick: (String) -> Unit,
    onNavigateToPlateRatingsScreen: (String) -> Unit

    ) {

    val textSize = if (isTablet()) 22.sp else 15.sp
    val iconSize = if (isTablet()) 28.dp else 24.dp
    val spacing = if (isTablet()) 13.dp else 4.dp

    var showMenu by remember { mutableStateOf(false) }
    var menuAnchor by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }





        Column(
            verticalArrangement = Arrangement.spacedBy(7.dp),
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = spacing, top = spacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // **License Plate**
                LicensePlateView(
                    licensePlate = commentItem.licensePlate,
                    modifier = Modifier
                        .clickable { onNavigateToPlateRatingsScreen(commentItem.licensePlate) }
                )

                Spacer(modifier = Modifier.weight(1f)) // 🔥 **MoreVert sağa kayacak!**

                // **Menü Butonu (Sadece Kullanıcıya Aitse)**
                if (currentUser?.uid == commentItem.userId) {
                    Box {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Yorumu Seçenekleri",
                                tint = Color.Gray,
                                modifier = Modifier.size(iconSize)
                            )
                        }

                        // **Dropdown Menü**
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Yorumu Sil",
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp,
                                            color = Color.DarkGray,
                                            fontFamily = nunitosansMediumFontFamily
                                        )
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onDeleteClick(commentItem.commentId)
                                }
                            )
                        }
                    }
                }
            }



            // Comment text
            Text(
                text = commentItem.comment,
                color = Color(22, 50, 22),
                style = TextStyle(
                    fontSize = textSize,
                    fontFamily = nunitosansregFontFamily,
                    fontWeight = FontWeight.SemiBold,
                ),
                modifier = Modifier.padding(start = spacing, end = spacing)
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = spacing / 8)
            ) {
                Spacer(modifier = Modifier.weight(1f))

                // Like Button
                IconButton(onClick = {
                    if (!commentItem.isDisliked.value) {
                        commentItem.isLiked.value = !commentItem.isLiked.value
                        if (currentUser != null) {
                            commentViewModel.updateCommentState(
                                commentId = commentItem.commentId,
                                currentUserId = currentUser.uid,
                                isLiked = commentItem.isLiked.value
                            )
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        tint = if (commentItem.isLiked.value) Color(3, 57, 108) else Color.Gray,
                        contentDescription = "Like",
                        modifier = Modifier.size(iconSize)
                    )
                }

                Text(
                    text = commentItem.likesCount.toString(),
                    color = Color.LightGray,
                    fontSize = textSize,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                Spacer(modifier = Modifier.width(if (isTablet()) 150.dp else 75.dp))

                // Dislike Button
                IconButton(onClick = {
                    if (!commentItem.isLiked.value) {
                        commentItem.isDisliked.value = !commentItem.isDisliked.value
                        if (currentUser != null) {
                            commentViewModel.updateCommentState(
                                commentId = commentItem.commentId,
                                currentUserId = currentUser.uid,
                                isDisliked = commentItem.isDisliked.value
                            )
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.ThumbDown,
                        tint = if (commentItem.isDisliked.value) Color(3, 57, 108) else Color.Gray,
                        contentDescription = "Dislike",
                        modifier = Modifier.size(iconSize)
                    )
                }

                Text(
                    text = commentItem.dislikesCount.toString(),
                    color = Color.LightGray,
                    fontSize = textSize,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                Spacer(modifier = Modifier.width(if (isTablet()) 150.dp else 75.dp))

                // Bookmark Button
                IconButton(onClick = {
                    commentItem.isBookmarked.value = !commentItem.isBookmarked.value
                    if (currentUser != null) {
                        commentViewModel.updateCommentState(
                            commentId = commentItem.commentId,
                            currentUserId = currentUser.uid,
                            isBookmarked = commentItem.isBookmarked.value
                        )
                    }
                }) {
                    Icon(
                        imageVector = if (commentItem.isBookmarked.value) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        tint = if (commentItem.isBookmarked.value) Color(3, 57, 108) else Color.Gray,
                        contentDescription = "Bookmark",
                        modifier = Modifier.size(iconSize)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }

            HorizontalDivider(thickness = calculateDividerThickness(commentItem.comment.length))
        }
    }

