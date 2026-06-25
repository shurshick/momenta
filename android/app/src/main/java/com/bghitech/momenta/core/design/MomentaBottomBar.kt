package com.bghitech.momenta.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class BottomNavItem(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String
) {
    HOME("today", Icons.Filled.Home, Icons.Outlined.Home, "Главная"),
    SEARCH("search", Icons.Filled.Search, Icons.Outlined.Search, "Поиск"),
    CREATE("camera", Icons.Filled.Home, Icons.Outlined.Home, "Создать"),
    FEED("feed", Icons.Filled.Home, Icons.Outlined.Home, "Моменты"),
    PROFILE("profile", Icons.Filled.Person, Icons.Outlined.Person, "Профиль")
}

@Composable
fun MomentaBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    Surface(
        color = MomentaSurface,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem.entries.forEach { item ->
                if (item == BottomNavItem.CREATE) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MomentaGreen)
                            .clickable { onNavigate(item.route) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            color = MomentaBackground,
                            fontSize = 28.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                } else {
                    val selected = currentRoute == item.route
                    val icon = if (selected) item.selectedIcon else item.unselectedIcon
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onNavigate(item.route) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = item.label,
                            tint = if (selected) MomentaGreen else MomentaTextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.label,
                            color = if (selected) MomentaGreen else MomentaTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
