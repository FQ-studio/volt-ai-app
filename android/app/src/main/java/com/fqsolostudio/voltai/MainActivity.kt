package com.fqsolostudio.voltai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isUser: Boolean)

class MainActivity : ComponentActivity() {
    private val voltBridge = VoltCoreBridge()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val dbPath = applicationContext.filesDir.absolutePath + "/volt_data.db"
        voltBridge.initVoltEngine(dbPath)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF131314),
                    surface = Color(0xFF1E1F20),
                    primary = Color(0xFFA8C7FA)
                )
            ) {
                VoltGeminiApp(voltBridge)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoltGeminiApp(bridge: VoltCoreBridge) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var isMalay by remember { mutableStateOf(true) }

    val welcomeText = if (isMalay) "Salam! Ada apa yang boleh Volt AI bantu hari ini?" else "Hello! How can Volt AI help you today?"
    val placeholderText = if (isMalay) "Tanya Volt AI..." else "Ask Volt AI..."

    val messages = remember { mutableStateListOf<ChatMessage>() }
    var inputText by remember { mutableStateOf("") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF1E1F20)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Volt AI",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
                Divider(color = Color.Gray.copy(alpha = 0.2f))
                
                NavigationDrawerItem(
                    label = { Text(if (isMalay) "Sesi Perbualan Baru" else "New Chat", color = Color.White) },
                    selected = false,
                    onClick = {
                        messages.clear()
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) }
                )

                NavigationDrawerItem(
                    label = { Text(if (isMalay) "Tukar Bahasa (BM / EN)" else "Switch Language (BM / EN)", color = Color.White) },
                    selected = false,
                    onClick = { isMalay = !isMalay },
                    icon = { Icon(Icons.Default.Share, contentDescription = null, tint = Color.White) }
                )

                NavigationDrawerItem(
                    label = { Text(if (isMalay) "Tetapan Volt AI" else "Settings", color = Color.White) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White) }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Volt AI (Offline)", color = Color.White, fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF131314))
                )
            },
            containerColor = Color(0xFF131314)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (messages.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = welcomeText,
                                color = Color.Gray,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            reverseLayout = false
                        ) {
                            items(messages) { msg ->
                                ChatBubble(msg)
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = Color(0xFF1E1F20)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { /* Lampiran Gambar / File */ }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = Color(0xFFA8C7FA)
                            )
                        }

                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text(placeholderText, color = Color.Gray) },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    val userQuery = inputText
                                    messages.add(ChatMessage(userQuery, isUser = true))
                                    inputText = ""
                                    
                                    val aiResponse = bridge.queryVolt(userQuery)
                                    messages.add(ChatMessage(aiResponse, isUser = false))
                                }
                            },
                            enabled = inputText.isNotBlank(),
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (inputText.isNotBlank()) Color(0xFFA8C7FA) else Color.Gray.copy(alpha = 0.3f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Send",
                                tint = if (inputText.isNotBlank()) Color.Black else Color.DarkGray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (message.isUser) 18.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 18.dp
            ),
            color = if (message.isUser) Color(0xFF2F2F32) else Color(0xFF1E1F20)
        ) {
            Text(
                text = message.text,
                color = Color.White,
                fontSize = 15.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}
