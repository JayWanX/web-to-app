package com.webtoapp.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.webtoapp.ui.theme.*
import androidx.compose.animation.core.Spring

/**
 * 主题化组件
 * 根据当前主题应用不同的视觉效果
 */

// ==================== 主题化背景修饰符 ====================

/**
 * 为 Modifier 添加主题背景
 * 对于静态壁纸使用 painterResource
 * 对于 GIF 动态壁纸，请使用 ThemedBackgroundBox composable
 * 没有壁纸时使用渐变
 */
@Composable
fun Modifier.themedBackground(): Modifier {
    val theme = LocalAppTheme.current
    val isDark = LocalIsDarkTheme.current

    // 壁纸由 ThemedBackgroundBox 渲染（支持任意图片格式）
    // Modifier 版本只处理渐变背景
    if (theme.wallpaperResId != 0 || theme.animatedWallpaperResId != 0) {
        // 有壁纸时使用纯色底色（壁纸由 ThemedBackgroundBox 叠加）
        return if (isDark) {
            val bgColor = theme.gradients.background.firstOrNull() ?: Color(0xFF0C0A14)
            this.drawBehind { drawRect(color = bgColor) }
        } else {
            this.drawBehind { drawRect(color = Color(0xFFF5F1F8)) }
        }
    }

    // 没有壁纸时使用渐变背景
    return if (isDark) {
        val bgColors = theme.gradients.background.ifEmpty {
            listOf(Color(0xFF0C0A14), Color(0xFF1A1030), Color(0xFF261840))
        }
        this.drawBehind {
            drawRect(
                brush = Brush.verticalGradient(bgColors),
                size = size
            )
        }
    } else {
        val primary = theme.lightColors.primary
        val secondary = theme.lightColors.secondary
        val tertiary = theme.lightColors.tertiary
        this.drawBehind {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primary.copy(alpha = 0.15f).compositeOver(Color(0xFFF2EDF6)),
                        Color(0xFFF5F1F8),
                        tertiary.copy(alpha = 0.10f).compositeOver(Color(0xFFF4F0F5))
                    )
                ),
                size = size
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primary.copy(alpha = 0.12f),
                        Color.Transparent
                    )
                ),
                radius = size.width * 0.6f,
                center = Offset(size.width * 0.15f, size.height * 0.08f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        secondary.copy(alpha = 0.08f),
                        Color.Transparent
                    )
                ),
                radius = size.width * 0.5f,
                center = Offset(size.width * 0.85f, size.height * 0.7f)
            )
        }
    }
}

/**
 * 主题背景容器 — 支持所有壁纸格式（JPEG、WebP、PNG、GIF）
 * 通过 Coil 加载壁纸，自动识别图片格式，不依赖文件扩展名
 *
 * 用法：
 * ```
 * ThemedBackgroundBox(modifier = Modifier.fillMaxSize()) {
 *     // 你的页面内容
 * }
 * ```
 */
@Composable
fun ThemedBackgroundBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val theme = LocalAppTheme.current
    val isDark = LocalIsDarkTheme.current
    val context = LocalContext.current

    // 获取屏幕像素尺寸，确保壁纸以高清分辨率加载
    val displayMetrics = context.resources.displayMetrics
    val screenWidthPx = displayMetrics.widthPixels
    val screenHeightPx = displayMetrics.heightPixels

    // 构建支持 GIF 的 ImageLoader（也能自动处理 JPEG/WebP/PNG）
    val imageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .allowHardware(false)  // 禁用硬件位图，避免大图被裁切
            .build()
    }

    // 决定用哪个壁纸资源：优先动态壁纸（GIF），其次静态壁纸
    val wallpaperResId = when {
        theme.animatedWallpaperResId != 0 -> theme.animatedWallpaperResId
        theme.wallpaperResId != 0 -> theme.wallpaperResId
        else -> 0
    }


    Box(modifier = modifier.themedBackground()) {
        // 通过 Coil 渲染壁纸（支持所有格式）
        if (wallpaperResId != 0) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(wallpaperResId)
                    .size(screenWidthPx, screenHeightPx)  // 按屏幕像素尺寸加载
                    .allowHardware(false)
                    .crossfade(300)
                    .build(),
                contentDescription = null,
                imageLoader = imageLoader,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // ===== 柔和过渡遮罩层 =====
            // 多档位渐变：顶部透明 → 中部微透 → 底部半透明
            // 营造壁纸与内容之间平滑的高斯模糊式视觉过渡
            val surfaceColor = if (isDark) {
                Color.Black
            } else {
                MaterialTheme.colorScheme.surface
            }
            val baseAlpha = if (isDark) 0.35f else 0.15f

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.00f to surfaceColor.copy(alpha = baseAlpha * 0.3f),
                                0.15f to surfaceColor.copy(alpha = baseAlpha * 0.5f),
                                0.35f to surfaceColor.copy(alpha = baseAlpha * 0.85f),
                                0.55f to surfaceColor.copy(alpha = baseAlpha * 1.2f),
                                0.75f to surfaceColor.copy(alpha = baseAlpha * 1.6f),
                                1.00f to surfaceColor.copy(alpha = (baseAlpha * 2.2f).coerceAtMost(0.88f))
                            )
                        )
                    )
            )

            // 额外径向光晕：在顶部中心添加柔和的高光
            if (!isDark) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.72f),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width * 0.5f, size.height * 0.08f),
                                    radius = size.width * 0.8f
                                ),
                                center = Offset(size.width * 0.5f, size.height * 0.08f),
                                radius = size.width * 0.8f
                            )
                        }
                )
            }
        }

        content()
    }
}


/**
 * 获取当前主题的卡片圆角形状
 */
@Composable
fun themedCardShape(): RoundedCornerShape {
    val theme = LocalAppTheme.current
    return RoundedCornerShape(theme.shapes.cardRadius)
}

/**
 * 获取当前主题的按钮圆角形状
 */
@Composable
fun themedButtonShape(): RoundedCornerShape {
    val theme = LocalAppTheme.current
    return RoundedCornerShape(theme.shapes.buttonRadius)
}

// ==================== 主题化按钮 ====================

/**
 * 渐变按钮 - 根据主题动画风格应用不同效果
 */
@Composable
fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val theme = LocalAppTheme.current
    val animSettings = LocalAnimationSettings.current
    val view = LocalView.current
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // iOS 26 风格 spring 回弹缩放
    val scale by animateFloatAsState(
        targetValue = if (isPressed && animSettings.enabled) {
            when (theme.animationStyle) {
                AnimationStyle.BOUNCY -> 0.93f
                AnimationStyle.SNAPPY -> 0.96f
                AnimationStyle.PLAYFUL -> 0.91f
                AnimationStyle.DRAMATIC -> 0.94f
                else -> 0.96f
            }
        } else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "buttonScale"
    )
    
    // 发光动画
    val glowAlpha by animateFloatAsState(
        targetValue = if (isPressed && animSettings.enabled && theme.effects.enableGlow) 0.5f else 0.3f,
        animationSpec = tween(150),
        label = "glowAlpha"
    )
    
    Surface(
        onClick = {
            if (animSettings.hapticsEnabled) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            onClick()
        },
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (animSettings.enabled && theme.effects.enableGlow) {
                    Modifier.drawBehind {
                        drawCircle(
                            color = theme.effects.glowColor.copy(alpha = glowAlpha),
                            radius = size.maxDimension / 2 + theme.effects.glowRadius.toPx() * 0.5f,
                            center = center
                        )
                    }
                } else Modifier
            ),
        enabled = enabled,
        shape = RoundedCornerShape(theme.shapes.buttonRadius),
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(theme.gradients.primary),
                    shape = RoundedCornerShape(theme.shapes.buttonRadius)
                )
                .padding(horizontal = 24.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}

/**
 * 发光按钮
 */
@Composable
fun GlowingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glowColor: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val theme = LocalAppTheme.current
    val animSettings = LocalAnimationSettings.current
    
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    PremiumButton(
        onClick = onClick,
        modifier = modifier.then(
            if (animSettings.enabled && theme.effects.enableGlow) {
                Modifier.drawBehind {
                    drawCircle(
                        color = glowColor.copy(alpha = glowAlpha),
                        radius = size.maxDimension / 2 + 12.dp.toPx()
                    )
                }
            } else Modifier
        ),
        enabled = enabled,
        shape = RoundedCornerShape(theme.shapes.buttonRadius)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

// ==================== 主题化卡片 ====================

/**
 * 玻璃拟态卡片 — iOS 26 液态玻璃风格
 * 使用 liquidGlass Modifier 实现真正的高斯模糊毛玻璃效果
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White.copy(alpha = 0.1f),
    borderColor: Color = Color.White.copy(alpha = 0.2f),
    blurRadius: Dp = 10.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val theme = LocalAppTheme.current
    
    Surface(
        modifier = modifier.liquidGlass(
            cornerRadius = theme.shapes.cardRadius,
            blurRadius = blurRadius,
            tintAlpha = 0.08f,
            borderAlpha = 0.18f,
            shadowElevation = 6.dp
        ),
        shape = RoundedCornerShape(theme.shapes.cardRadius),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

/**
 * 渐变边框卡片
 */
@Composable
fun GradientBorderCard(
    modifier: Modifier = Modifier,
    gradientColors: List<Color>? = null,
    borderWidth: Dp = 2.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val theme = LocalAppTheme.current
    val colors = gradientColors ?: theme.gradients.accent
    
    Box(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(colors),
                shape = RoundedCornerShape(theme.shapes.cardRadius)
            )
            .padding(borderWidth)
    ) {
        Surface(
            shape = RoundedCornerShape(theme.shapes.cardRadius - borderWidth),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    }
}

/**
 * 悬浮卡片 — iOS 26 风格：spring 回弹缩放 + 柔和阴影
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val theme = LocalAppTheme.current
    val animSettings = LocalAnimationSettings.current
    val view = LocalView.current
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // iOS 26 spring 回弹
    val scale by animateFloatAsState(
        targetValue = if (isPressed && animSettings.enabled) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "floatingScale"
    )
    
    // 阴影随按压变化
    val elevation by animateFloatAsState(
        targetValue = if (isPressed) 2f else 8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "elevation"
    )
    
    Card(
        onClick = {
            if (animSettings.hapticsEnabled) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            onClick()
        },
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = elevation.dp.toPx()
            }
            .then(
                if (animSettings.enabled && theme.effects.enableGlow && isPressed) {
                    Modifier.drawBehind {
                        drawRoundRect(
                            color = theme.effects.glowColor.copy(alpha = 0.12f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                theme.shapes.cardRadius.toPx() + 4.dp.toPx()
                            ),
                            size = androidx.compose.ui.geometry.Size(
                                size.width + 8.dp.toPx(),
                                size.height + 8.dp.toPx()
                            ),
                            topLeft = androidx.compose.ui.geometry.Offset(-4.dp.toPx(), -4.dp.toPx())
                        )
                    }
                } else Modifier
            ),
        shape = RoundedCornerShape(theme.shapes.cardRadius),
        interactionSource = interactionSource
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

// ==================== 主题化背景 ====================

/**
 * 渐变背景
 */
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    colors: List<Color>? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val theme = LocalAppTheme.current
    val backgroundColors = colors ?: theme.gradients.background
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.linearGradient(backgroundColors)),
        content = content
    )
}

/**
 * 动态渐变背景
 */
@Composable
fun AnimatedGradientBackground(
    modifier: Modifier = Modifier,
    colors: List<Color>? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val theme = LocalAppTheme.current
    val animSettings = LocalAnimationSettings.current
    val backgroundColors = colors ?: theme.gradients.background
    
    val infiniteTransition = rememberInfiniteTransition(label = "gradientBg")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientOffset"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                if (animSettings.enabled) {
                    val start = Offset(size.width * offset, 0f)
                    val end = Offset(size.width * (1 - offset), size.height)
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = backgroundColors,
                            start = start,
                            end = end
                        )
                    )
                } else {
                    drawRect(brush = Brush.linearGradient(backgroundColors))
                }
            },
        content = content
    )
}

// ==================== 主题化指示器 ====================

/**
 * 主题化加载指示器
 */
@Composable
fun ThemedLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val theme = LocalAppTheme.current
    val animSettings = LocalAnimationSettings.current
    
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (1000 * animSettings.speedMultiplier).toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // 发光效果
        if (theme.effects.enableGlow && animSettings.enabled) {
            Box(
                modifier = Modifier
                    .size(size + 16.dp)
                    .graphicsLayer { rotationZ = rotation }
                    .drawBehind {
                        drawCircle(
                            brush = Brush.sweepGradient(
                                listOf(
                                    theme.effects.glowColor.copy(alpha = 0f),
                                    theme.effects.glowColor.copy(alpha = 0.5f),
                                    theme.effects.glowColor.copy(alpha = 0f)
                                )
                            ),
                            radius = this.size.minDimension / 2
                        )
                    }
            )
        }
        
        CircularProgressIndicator(
            modifier = Modifier
                .size(size)
                .graphicsLayer { rotationZ = rotation * 0.3f },
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
    }
}

/**
 * 脉冲点指示器
 */
@Composable
fun PulsingDotIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    dotCount: Int = 3,
    dotSize: Dp = 8.dp
) {
    val animSettings = LocalAnimationSettings.current
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(dotSize / 2)
    ) {
        repeat(dotCount) { index ->
            val infiniteTransition = rememberInfiniteTransition(label = "dot$index")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = (600 * animSettings.speedMultiplier).toInt(),
                        delayMillis = index * 150,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dotScale$index"
            )
            
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .scale(if (animSettings.enabled) scale else 1f)
                    .clip(CircleShape)
                    .background(color.copy(alpha = scale))
            )
        }
    }
}

// ==================== 主题化分隔线 ====================

/**
 * 渐变分隔线
 */
@Composable
fun GradientDivider(
    modifier: Modifier = Modifier,
    colors: List<Color>? = null,
    thickness: Dp = 1.dp
) {
    val theme = LocalAppTheme.current
    val dividerColors = colors ?: listOf(
        Color.Transparent,
        theme.gradients.accent.first(),
        theme.gradients.accent.last(),
        Color.Transparent
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(Brush.horizontalGradient(dividerColors))
    )
}

// ==================== 主题化徽章 ====================

/**
 * 发光徽章
 */
@Composable
fun GlowingBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    val theme = LocalAppTheme.current
    val animSettings = LocalAnimationSettings.current
    
    val infiniteTransition = rememberInfiniteTransition(label = "badge")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badgeGlow"
    )
    
    Surface(
        modifier = modifier.then(
            if (animSettings.enabled && theme.effects.enableGlow) {
                Modifier.drawBehind {
                    drawCircle(
                        color = backgroundColor.copy(alpha = glowAlpha),
                        radius = size.maxDimension / 2 + 4.dp.toPx()
                    )
                }
            } else Modifier
        ),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = contentColor,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
