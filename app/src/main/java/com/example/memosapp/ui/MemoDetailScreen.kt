package com.example.memosapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.memosapp.model.Memo
import com.example.memosapp.viewmodel.MemoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MemoDetailScreen(
    memoViewModel: MemoViewModel,
    memoId: String, // UID of the memo to display
    onNavigateBack: () -> Unit,
    onEditMemo: (Memo) -> Unit, // Callback to navigate to edit screen
    // onDeleteMemo: (String) -> Unit // Callback for delete, memoName (memos/UID) needed
) {
    val selectedMemo by memoViewModel.selectedMemo.observeAsState(initial = null)
    val isLoading by memoViewModel.isLoading.observeAsState(initial = false)
    val errorMessage by memoViewModel.errorMessage.observeAsState(initial = null)

    LaunchedEffect(memoId) {
        if (selectedMemo?.id != memoId) { // Fetch only if not already loaded or different memo
            memoViewModel.fetchMemoDetails(memoId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (selectedMemo != null) "Memo Details" else "Loading...") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    selectedMemo?.let {
                        IconButton(onClick = { onEditMemo(it) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit Memo")
                        }
                        // IconButton(onClick = { onDeleteMemo("memos/${it.id}") }) { // Construct memoName for deletion
                        //     Icon(Icons.Filled.Delete, contentDescription = "Delete Memo")
                        // }
                    }
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(it).padding(16.dp)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colors.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            selectedMemo?.let { memo ->
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    Text(text = memo.content, style = MaterialTheme.typography.body1)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Visibility: ${memo.visibility}",
                        style = MaterialTheme.typography.caption
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Created: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(memo.createdTs * 1000L))}",
                        style = MaterialTheme.typography.caption
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Updated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(memo.updatedTs * 1000L))}",
                        style = MaterialTheme.typography.caption
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pinned: ${memo.pinned}",
                        style = MaterialTheme.typography.caption
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ID: ${memo.id}",
                        style = MaterialTheme.typography.caption
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Creator ID: ${memo.creatorId}",
                        style = MaterialTheme.typography.caption
                    )

                    if (!memo.resourceList.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Resources:", style = MaterialTheme.typography.h6)
                        memo.resourceList.forEach { resource ->
                            Text("- ${resource.filename} (${resource.type})", style = MaterialTheme.typography.body2)
                            // Potentially display images or links to resources here
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMemoDetailScreen() {
    // This preview is static and won't interact with a ViewModel
    val previewMemo = Memo(
        id = "preview-123",
        creatorId = "user-prev",
        createdTs = System.currentTimeMillis() / 1000 - 3600,
        updatedTs = System.currentTimeMillis() / 1000,
        content = "This is a detailed preview of a memo. It contains multiple lines of text to demonstrate how the content would be displayed. \n\nIt can also include **Markdown** (though rendering is not part of this basic preview) and other formatting.",
        visibility = "PUBLIC",
        pinned = true,
        resourceList = listOf(
            com.example.memosapp.model.MemoResource(id="res1", name="res_name_1", filename="image.jpg", type="image/jpeg", size=1024, createdTs=0, updatedTs=0, publicId = "pubid1"),
            com.example.memosapp.model.MemoResource(id="res2", name="res_name_2", filename="document.pdf", type="application/pdf", size=2048, createdTs=0, updatedTs=0, publicId = "pubid2")
        ),
        relationList = emptyList()
    )
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Memo Details Preview") },
                    navigationIcon = { IconButton(onClick = {}) { Icon(Icons.Filled.ArrowBack, "") } },
                    actions = {
                        IconButton(onClick = {}) { Icon(Icons.Filled.Edit, "") }
                        IconButton(onClick = {}) { Icon(Icons.Filled.Delete, "") }
                    }
                )
            }
        ) {
            Column(modifier = Modifier.padding(it).padding(16.dp).verticalScroll(rememberScrollState())) {
                Text(text = previewMemo.content, style = MaterialTheme.typography.body1)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Visibility: ${previewMemo.visibility}", style = MaterialTheme.typography.caption)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Created: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(previewMemo.createdTs * 1000L))}", style = MaterialTheme.typography.caption)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Updated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(previewMemo.updatedTs * 1000L))}", style = MaterialTheme.typography.caption)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Pinned: ${previewMemo.pinned}", style = MaterialTheme.typography.caption)
                Spacer(modifier = Modifier.height(4.dp))
                Text("ID: ${previewMemo.id}", style = MaterialTheme.typography.caption)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Creator ID: ${previewMemo.creatorId}", style = MaterialTheme.typography.caption)
                if (!previewMemo.resourceList.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Resources:", style = MaterialTheme.typography.h6)
                    previewMemo.resourceList.forEach { resource ->
                        Text("- ${resource.filename} (${resource.type})", style = MaterialTheme.typography.body2)
                    }
                }
            }
        }
    }
}

