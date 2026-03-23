package com.webtoapp.ui.screens

import androidx.compose.animation.*
import com.webtoapp.ui.components.PremiumButton
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.ui.components.ThemedBackgroundBox
import com.webtoapp.ui.theme.*
import kotlinx.coroutines.launch
import com.webtoapp.ui.components.EnhancedElevatedCard

/**
 * 主题设置界面
 * 展示所有主题并支持切换
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val themeManager = remember { ThemeManager.getInstance(context) }
    
    // 状态
    val currentThemeType by themeManager.themeTypeFlow.collectAsState(initial = AppThemeType.KIMI_NO_NAWA)
    var showThemePreview by remember { mutableStateOf<AppThemeType?>(null) }
    
    val currentTheme = AppThemes.getTheme(currentThemeType)
    
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(Strings.themeSettings, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        Text(
                            currentTheme.type.getDisplayName(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, Strings.back)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                )
            )
        }
    ) { padding ->
        ThemedBackgroundBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ThemeSelectionTab(
                currentThemeType = currentThemeType,
                onThemeSelect = { type ->
                    scope.launch { themeManager.setThemeType(type) }
                },
                onPreviewTheme = { showThemePreview = it },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
    
    // Theme预览对话框
    showThemePreview?.let { themeType ->
        ThemePreviewDialog(
            theme = AppThemes.getTheme(themeType),
            onDismiss = { showThemePreview = null },
            onApply = {
                scope.launch {
                    themeManager.setThemeType(themeType)
                    showThemePreview = null
                }
            }
        )
    }
}

@Composable
private fun ThemeSelectionTab(
    currentThemeType: AppThemeType,
    onThemeSelect: (AppThemeType) -> Unit,
    onPreviewTheme: (AppThemeType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        items(AppThemes.allThemes) { theme ->
            ThemeCard(
                theme = theme,
                isSelected = theme.type == currentThemeType,
                onClick = { onThemeSelect(theme.type) },
                onLongClick = { onPreviewTheme(theme.type) }
            )
        }
    }
}

/**
 * 主题卡片
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ThemeCard(
    theme: AppTheme,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // iOS 26 spring 回弹
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else if (isSelected) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "themeCardScale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(theme.shapes.cardRadius),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
        ),
        border = if (isSelected) BorderStroke(
            2.dp,
            Brush.linearGradient(theme.gradients.accent.ifEmpty {
                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
            })
        ) else BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 渐变预览
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(weight = 1f, fill = true)
                    .background(
                        brush = Brush.linearGradient(theme.gradients.primary),
                        shape = RoundedCornerShape(
                            topStart = theme.shapes.cardRadius,
                            topEnd = theme.shapes.cardRadius
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Color圆点预览
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        theme.darkColors.primary,
                        theme.darkColors.secondary,
                        theme.darkColors.tertiary
                    ).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        )
                    }
                }
                
                // 选中标记
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            // Theme信息
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = theme.type.getDisplayName(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = theme.type.getDescription(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                )
                Spacer(modifier = Modifier.height(6.dp))
                // 特性标签
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FeatureChip(theme.animationStyle.getDisplayName())
                    if (theme.effects.enableGlow) {
                        FeatureChip(Strings.glow)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureChip(text: String) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

/**
 * 主题预览对话框
 */
@Composable
private fun ThemePreviewDialog(
    theme: AppTheme,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(theme.gradients.primary))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(theme.type.getDisplayName())
                    Text(
                        theme.type.getDescription(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Color预览
                Text(Strings.colorScheme, style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Strings.primaryColor to theme.darkColors.primary,
                        Strings.secondaryColor to theme.darkColors.secondary,
                        Strings.accentColor to theme.darkColors.tertiary
                    ).forEach { (name, color) ->
                        Column(
                            modifier = Modifier.weight(weight = 1f, fill = true),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Text(name, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                
                HorizontalDivider()
                
                // 特性
                Text(Strings.themeFeatures, style = MaterialTheme.typography.labelMedium)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    FeatureRow(Strings.animationStyle, theme.animationStyle.getDisplayName())
                    FeatureRow(Strings.interactionMethod, theme.interactionStyle.getDisplayName())
                    FeatureRow(Strings.cornerRadius, "${theme.shapes.cornerRadius}")
                    FeatureRow(Strings.glowEffect, if (theme.effects.enableGlow) Strings.yes else Strings.no)
                    FeatureRow(Strings.particleEffect, if (theme.effects.enableParticles) Strings.yes else Strings.no)
                    FeatureRow(Strings.glassmorphism, if (theme.effects.enableGlassmorphism) Strings.yes else Strings.no)
                }
            }
        },
        confirmButton = {
            PremiumButton(onClick = onApply) {
                Text(Strings.applyTheme)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.btnCancel)
            }
        }
    )
}

@Composable
private fun FeatureRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
