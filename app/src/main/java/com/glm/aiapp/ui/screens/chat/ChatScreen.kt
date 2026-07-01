package com.glm.aiapp.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.glm.aiapp.domain.model.Message
import com.glm.aiapp.domain.model.Role
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun ChatScreen(vm: ChatViewModel = hiltViewModel()) {
    val conversations by vm.conversations.collectAsStateWithLifecycle()
    val activeConversation by vm.activeConversation.collectAsStateWithLifecycle()
    val streamingText by vm.streamingText.collectAsStateWithLifecycle()
    val streamingThinking by vm.streamingThinking.collectAsStateWithLifecycle()
    val isStreaming by vm.isStreaming.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()

    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var showHistory by remember { mutableStateOf(false) }

    // Auto-scroll on new messages or streaming updates
    LaunchedEffect(activeConversation?.messages?.size, streamingText, streamingThinking) {
        val total = (activeConversation?.messages?.size ?: 0) + if (streamingText.isNotBlank() || streamingThinking.isNotBlank()) 1 else 0
        if (total > 0) listState.animateScrollToItem(total - 1)
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Column(Modifier.fillMaxSize().background(Color.Black)) {
            // Top bar — minimal, just history button and new chat
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showHistory = true }) {
                    Icon(Icons.Filled.Menu, contentDescription = "History", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.weight(1f))
                Text("Pullarao 1", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { vm.newConversation() }) {
                    Icon(Icons.Filled.Add, contentDescription = "New chat", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            // Chat messages
            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (activeConversation == null && conversations.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Pullarao 1", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text("Ask anything. Build anything.", color = Color(0xFF666666), fontSize = 14.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        activeConversation?.messages?.let { msgs ->
                            items(msgs, key = { it.id }) { msg -> MessageBubble(msg) }
                        }
                        if (streamingText.isNotBlank() || streamingThinking.isNotBlank()) {
                            item { AssistantBubble(streamingText, streamingThinking) }
                        }
                        if (isStreaming && streamingText.isBlank() && streamingThinking.isBlank()) {
                            item {
                                Row(Modifier.padding(8.dp)) {
                                    Text("● ● ●", color = Color(0xFF444444), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Error
            error?.let { msg ->
                Text(msg, color = Color(0xFFFF6666), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            }

            // Input bar — stays above keyboard
            Row(
                Modifier.fillMaxWidth().padding(12.dp).imePadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message Pullarao 1…", color = Color(0xFF555555), fontSize = 14.sp) },
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedBorderColor = Color(0xFF333333),
                        unfocusedBorderColor = Color(0xFF222222),
                        focusedContainerColor = Color(0xFF111111),
                        unfocusedContainerColor = Color(0xFF111111)
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send)
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        val text = input.trim()
                        if (text.isNotBlank() && !isStreaming) { vm.sendMessage(text); input = "" }
                    },
                    enabled = input.isNotBlank() && !isStreaming,
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(50),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Icon(Icons.Filled.ArrowUpward, contentDescription = "Send", modifier = Modifier.size(20.dp))
                }
            }
        }

        // History drawer
        if (showHistory) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f))) {
                Surface(
                    modifier = Modifier.fillMaxHeight().width(280.dp),
                    color = Color(0xFF0A0A0A)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("History", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = { showHistory = false }) {
                                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        if (conversations.isEmpty()) {
                            Text("No conversations yet", color = Color(0xFF555555), fontSize = 14.sp)
                        } else {
                            conversations.forEach { conv ->
                                Surface(
                                    onClick = {
                                        vm.selectConversation(conv.id)
                                        showHistory = false
                                    },
                                    color = Color.Transparent,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(Modifier.padding(vertical = 12.dp)) {
                                        Text(conv.title, color = Color.White, fontSize = 14.sp, maxLines = 1)
                                        Text(conv.model, color = Color(0xFF555555), fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: Message) {
    val isUser = message.role == Role.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // AI avatar
            Box(
                Modifier.size(28.dp).clip(RoundedCornerShape(50)).background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text("P1", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
        }
        Column(modifier = Modifier.widthIn(max = 320.dp)) {
            message.thinking?.takeIf { it.isNotBlank() }?.let {
                Surface(
                    color = Color(0xFF111111),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                ) {
                    Column(Modifier.padding(10.dp)) {
                        Text("Thinking", color = Color(0xFF666666), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        Text(it, color = Color(0xFF888888), fontSize = 11.sp, fontFamily = FontFamily.Monospace, lineHeight = 16.sp)
                    }
                }
            }
            Surface(
                color = if (isUser) Color.White else Color(0xFF1A1A1A),
                shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, if (isUser) 4.dp else 16.dp)
            ) {
                if (isUser) {
                    Text(message.content, color = Color.Black, fontSize = 14.sp, modifier = Modifier.padding(12.dp))
                } else {
                    Box(Modifier.padding(12.dp)) {
                        MessageContent(message.content, Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun AssistantBubble(content: String, thinking: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Box(
            Modifier.size(28.dp).clip(RoundedCornerShape(50)).background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text("P1", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.widthIn(max = 320.dp)) {
            if (thinking.isNotBlank()) {
                Surface(color = Color(0xFF111111), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                    Column(Modifier.padding(10.dp)) {
                        Text("Thinking", color = Color(0xFF666666), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        Text(thinking, color = Color(0xFF888888), fontSize = 11.sp, fontFamily = FontFamily.Monospace, lineHeight = 16.sp)
                    }
                }
            }
            if (content.isNotBlank()) {
                Surface(color = Color(0xFF1A1A1A), shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)) {
                    Box(Modifier.padding(12.dp)) {
                        MessageContent(content, Color.White)
                    }
                }
            }
        }
    }
}

// ---- Code-aware content rendering ----
//
// MarkdownText (dev.jeziellago:compose-markdown) wraps everything — prose
// AND code — to the container width with no horizontal scroll for long
// lines. Inside a narrow chat bubble, that turns any real code block into
// an unreadable wall of character-by-character wrapping (exactly what a
// generated AndroidManifest/XML snippet looked like before this fix).
//
// Fix: split fenced code blocks out ourselves before handing content to
// MarkdownText — prose still renders through the library as before, but
// code blocks get their own composable with a monospace font and
// horizontal scroll, so long lines scroll instead of mangling into
// fragments. This only uses stock Jetpack Compose APIs (Regex,
// horizontalScroll, FontFamily.Monospace) — nothing from the third-party
// library's internals, which aren't customizable this deeply anyway.

private val CODE_FENCE_REGEX = Regex("```([a-zA-Z0-9_+-]*)\\n([\\s\\S]*?)```")

private sealed class ContentSegment {
    data class Prose(val markdown: String) : ContentSegment()
    data class Code(val language: String, val code: String) : ContentSegment()
}

private fun splitContentSegments(content: String): List<ContentSegment> {
    val segments = mutableListOf<ContentSegment>()
    var lastEnd = 0
    for (match in CODE_FENCE_REGEX.findAll(content)) {
        if (match.range.first > lastEnd) {
            val prose = content.substring(lastEnd, match.range.first)
            if (prose.isNotBlank()) segments.add(ContentSegment.Prose(prose))
        }
        segments.add(ContentSegment.Code(match.groupValues[1], match.groupValues[2].trimEnd('\n')))
        lastEnd = match.range.last + 1
    }
    if (lastEnd < content.length) {
        val prose = content.substring(lastEnd)
        if (prose.isNotBlank()) segments.add(ContentSegment.Prose(prose))
    }
    if (segments.isEmpty() && content.isNotBlank()) segments.add(ContentSegment.Prose(content))
    return segments
}

@Composable
private fun MessageContent(content: String, textColor: Color) {
    val segments = remember(content) { splitContentSegments(content) }
    Column {
        segments.forEachIndexed { index, segment ->
            if (index > 0) Spacer(Modifier.height(6.dp))
            when (segment) {
                is ContentSegment.Prose -> MarkdownText(
                    markdown = segment.markdown,
                    style = MaterialTheme.typography.bodyMedium.copy(color = textColor)
                )
                is ContentSegment.Code -> CodeBlock(segment.code, segment.language)
            }
        }
    }
}

@Composable
private fun CodeBlock(code: String, language: String) {
    Surface(
        color = Color(0xFF0D0D0D),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            if (language.isNotBlank()) {
                Text(
                    language,
                    color = Color(0xFF666666),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, top = 6.dp)
                )
            }
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(10.dp)) {
                Text(
                    code,
                    color = Color(0xFFDDDDDD),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
