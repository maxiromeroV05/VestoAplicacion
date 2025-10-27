package com.example.vest0.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.vest0.R

@Composable
fun BottomNavigationBar(onNavigate: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomBarButton("Ropa", R.drawable.ic_ropa_playstore) { onNavigate("ropa") }
        BottomBarButton("Ubicación", R.drawable.ic_location_playstore) { onNavigate("ubicacion") }
        BottomBarButton("Perfil", R.drawable.ic_profile_playstore) { onNavigate("perfil") }
        BottomBarButton("Inicio", R.drawable.ic_home_playstore) { onNavigate("menu") }
    }
}