package com.d4vram.threadsvault.ui.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d4vram.threadsvault.R
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector? = null,
    val iconRes: Int? = null,       // drawable/mipmap resource (overrides icon)
    val iconTint: Color,
    val iconBg: Color,
    val accentColor: Color,
    val gradientTop: Color,
    val gradientBottom: Color,
    val title: String,
    val description: String,
    val badge: String? = null
)

private val pages = listOf(
    OnboardingPage(
        iconRes = R.mipmap.ic_launcher_round,
        iconTint = Color.Unspecified,
        iconBg = Color.Transparent,
        accentColor = Color(0xFF7C4DFF),
        gradientTop = Color(0xFF0F0A1E),
        gradientBottom = Color(0xFF1A0F3D),
        title = "Bienvenido a\nThreadsVault",
        description = "Tu vault personal para guardar y organizar los mejores posts de Threads. Sin perder nada nunca más.",
        badge = "v1.0"
    ),
    OnboardingPage(
        icon = Icons.Rounded.Share,
        iconTint = Color(0xFF4DD0E1),
        iconBg = Color(0xFF003B42),
        accentColor = Color(0xFF00BCD4),
        gradientTop = Color(0xFF071820),
        gradientBottom = Color(0xFF0D2D35),
        title = "Guarda desde\nThreads",
        description = "Abre cualquier post en Threads, toca el botón Compartir y selecciona ThreadsVault. El contenido se extrae automáticamente.",
    ),
    OnboardingPage(
        icon = Icons.Rounded.Category,
        iconTint = Color(0xFF80CBC4),
        iconBg = Color(0xFF003631),
        accentColor = Color(0xFF26A69A),
        gradientTop = Color(0xFF061918),
        gradientBottom = Color(0xFF0E2B2A),
        title = "Organiza con\ncategorías",
        description = "Crea categorías personalizadas con emoji y color. Filtra tu vault al instante y encuentra lo que buscas en segundos.",
    ),
    OnboardingPage(
        icon = Icons.Rounded.Lock,
        iconTint = Color(0xFFFF80AB),
        iconBg = Color(0xFF3D001D),
        accentColor = Color(0xFFFF4081),
        gradientTop = Color(0xFF1A0010),
        gradientBottom = Color(0xFF2D0020),
        title = "100% privado\ny local",
        description = "Tus posts nunca salen de tu dispositivo. Sin nube, sin cuentas, sin rastreo. Solo tú y tu vault.",
        badge = "Privacy First"
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    val currentPage = pagerState.currentPage
    val page = pages[currentPage]

    val bgTop by animateColorAsState(
        targetValue = page.gradientTop,
        animationSpec = tween(600),
        label = "bgTop"
    )
    val bgBottom by animateColorAsState(
        targetValue = page.gradientBottom,
        animationSpec = tween(600),
        label = "bgBottom"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(bgTop, bgBottom)))
    ) {
        // Skip button
        if (currentPage < pages.lastIndex) {
            TextButton(
                onClick = onFinish,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(end = 8.dp, top = 8.dp)
            ) {
                Text(
                    text = "Omitir",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            }
        }

        // Pager content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 140.dp)
        ) { pageIndex ->
            PageContent(page = pages[pageIndex])
        }

        // Bottom bar: dots + button
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            PageIndicator(
                count = pages.size,
                current = currentPage,
                accentColor = page.accentColor
            )

            Button(
                onClick = {
                    if (currentPage < pages.lastIndex) {
                        scope.launch { pagerState.animateScrollToPage(currentPage + 1) }
                    } else {
                        onFinish()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = page.accentColor)
            ) {
                Text(
                    text = if (currentPage < pages.lastIndex) "Siguiente" else "¡Empezar!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun PageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(0.5f))

        // Icon illustration
        if (page.iconRes != null) {
            // App icon: use AsyncImage (Coil) — supports adaptive icons, painterResource does not
            coil.compose.AsyncImage(
                model = page.iconRes,
                contentDescription = null,
                modifier = Modifier.size(160.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(page.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(page.accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (page.icon != null) {
                        Icon(
                            imageVector = page.icon,
                            contentDescription = null,
                            modifier = Modifier.size(62.dp),
                            tint = page.iconTint
                        )
                    }
                }
            }
        }

        // Badge
        if (page.badge != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(page.accentColor.copy(alpha = 0.18f))
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(
                    text = page.badge,
                    color = page.accentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = page.title,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 38.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.68f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PageIndicator(count: Int, current: Int, accentColor: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            val isSelected = index == current
            val width by animateDpAsState(
                targetValue = if (isSelected) 28.dp else 8.dp,
                animationSpec = spring(),
                label = "dotWidth"
            )
            val color by animateColorAsState(
                targetValue = if (isSelected) accentColor else Color.White.copy(alpha = 0.25f),
                animationSpec = tween(300),
                label = "dotColor"
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
