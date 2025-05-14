package com.example.memosapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.* // For Material Design components
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.memosapp.model.Memo
import com.example.memosapp.viewmodel.MemoViewModel

// Dummy Memo data for preview
val previewMemo = Memo(
    id = "1", creatorId = "user1", createdTs = System.currentTimeMillis(), updatedTs = System.currentTimeMillis(),
    content = "This is a preview memo content. It can be a bit long to see how it wraps.",
    visibility = "PUBLIC", pinned = false, resourceList = emptyList(), relationList = emptyList()
)
val previewMemoList = listOf(previewMemo, previewMemo.copy(id = "2", content = "Another memo for preview."))

@Composable
fun MemosListScreen(
    memoViewModel: MemoViewModel, // Pass the ViewModel
    onMemoClick: (Memo) -> Unit, // Callback for when a memo is clicked
    onFabClick: () -> Unit // Callback for FloatingActionButton click
) {
    val memos by memoViewModel.memosList.observeAsState(initial = null)
    val isLoading by memoViewModel.isLoading.observeAsState(initial = false)
    val errorMessage by memoViewModel.errorMessage.observeAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Memos") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onFabClick) {
                Icon(Icons.Filled.Add, contentDescription = "Create Memo")
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(it)) {
            if (isLoading && memos == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colors.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            }

            if (memos == null && !isLoading && errorMessage == null) {
                 // You might want to fetch memos if the list is null and not loading
                 // memoViewModel.fetchMemos() // Be careful with calling this directly in Composable
                 Text("No memos yet. Tap the + button to create one!", modifier = Modifier.align(Alignment.Center))
            }

            memos?.let {
                if (it.isEmpty() && !isLoading) {
                    Text("No memos found.", modifier = Modifier.align(Alignment.Center).padding(16.dp))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(it) { memo ->
                            MemoListItem(memo = memo, onClick = { onMemoClick(memo) })
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemoListItem(memo: Memo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = memo.content, style = MaterialTheme.typography.body1, maxLines = 3)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Visibility: ${memo.visibility}",
                    style = MaterialTheme.typography.caption
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(memo.createdTs * 1000L)), // Assuming createdTs is in seconds
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMemosListScreen() {
    // This preview won't have a real ViewModel, so it's limited
    // For a more complete preview, you'd use a fake ViewModel or pass dummy data directly
    Scaffold(
        topBar = { TopAppBar(title = { Text("My Memos Preview") }) },
        floatingActionButton = { FloatingActionButton(onClick = {}) { Icon(Icons.Filled.Add, "") } }
    ) {
        LazyColumn(modifier = Modifier.padding(it)) {
            items(previewMemoList) { memo ->
                MemoListItem(memo = memo, onClick = {})
                Divider()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMemoListItem() {
    MemoListItem(memo = previewMemo, onClick = {})
}

