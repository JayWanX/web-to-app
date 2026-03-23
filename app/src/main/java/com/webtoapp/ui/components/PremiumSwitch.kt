package com.webtoapp.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * iOS 风格 Premium Switch
 *
 * 特性:
 * - 弹簧物理动画（圆形滑块带弹性回弹）
 * - 按压时滑块轻微拉伸（iOS 特征性交互）
 * - 渐变轨道颜色过渡
 * - 柔和阴影 + 微光效果
 * - 触觉反馈
 */
@Composable
fun PremiumSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trackWidth: Dp = 51.dp,
    trackHeight: Dp = 31.dp,
    thumbSize: Dp = 27.dp,
    thumbPadding: Dp = 2.dp,
) {
    val view = LocalView.current
    val density = LocalDensity.current

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // ==================== 动画值 ====================

    // 滑块位置 — 弹簧回弹
    val thumbOffset by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.68f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "thumbOffset"
    )

    // 按压时滑块横向拉伸 — iOS 特征
    val thumbStretch by animateFloatAsState(
        targetValue = if (isPressed && enabled) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "thumbStretch"
    )

    // 轨道颜色过渡
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor by animateColorAsState(
        targetValue = if (checked) {
            if (enabled) primaryColor else primaryColor.copy(alpha = 0.4f)
        } else {
            if (enabled) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        animationSpec = tween(
            durationMillis = 250,
            easing = FastOutSlowInEasing
        ),
        label = "trackColor"
    )

    // ON 状态的渐变轨道终点色
    val trackEndColor by animateColorAsState(
        targetValue = if (checked) {
            if (enabled) primaryColor.copy(alpha = 0.85f) else primaryColor.copy(alpha = 0.35f)
        } else {
            if (enabled) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "trackEndColor"
    )

    // 滑块阴影 — 按压时缩小、松手时恢复
    val thumbElevation by animateFloatAsState(
        targetValue = if (isPressed) 1f else 3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "thumbElevation"
    )

    // 滑块缩放 — 按压微缩
    val thumbScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "thumbScale"
    )

    // ==================== 布局计算 ====================
    val thumbSizePx = with(density) { thumbSize.toPx() }
    val thumbPaddingPx = with(density) { thumbPadding.toPx() }
    val trackWidthPx = with(density) { trackWidth.toPx() }
    val trackHeightPx = with(density) { trackHeight.toPx() }

    // 滑块可移动范围
    val thumbTravel = trackWidthPx - thumbSizePx - thumbPaddingPx * 2

    // ==================== 绘制 ====================
    Box(
        modifier = modifier
            .width(trackWidth)
            .height(trackHeight)
            .clip(RoundedCornerShape(trackHeight / 2))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) {
                // 触觉反馈
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                }
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // ===== 轨道 =====
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 轨道渐变背景
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(trackColor, trackEndColor)
                ),
                cornerRadius = CornerRadius(size.height / 2),
                size = size
            )

            // ON 状态时的内发光
            if (checked && enabled) {
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.3f, size.height * 0.3f),
                        radius = size.width * 0.6f
                    ),
                    cornerRadius = CornerRadius(size.height / 2),
                    size = size
                )
            }
        }

        // ===== 滑块 =====
        val thumbX = thumbPaddingPx + thumbOffset * thumbTravel

        Box(
            modifier = Modifier
                .offset(x = with(density) { thumbX.toDp() })
                .graphicsLayer {
                    scaleX = thumbStretch * thumbScale
                    scaleY = (1f / thumbStretch.coerceAtLeast(1.001f)) * thumbScale
                    shadowElevation = thumbElevation * density.density
                }
                .size(thumbSize)
                .shadow(
                    elevation = thumbElevation.dp,
                    shape = CircleShape,
                    clip = false
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFF8F8F8)
                        )
                    )
                )
        )
    }
}
