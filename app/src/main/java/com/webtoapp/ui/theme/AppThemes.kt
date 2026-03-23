package com.webtoapp.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.webtoapp.core.i18n.Strings
import com.webtoapp.R

/**
 * 应用主题系统
 * 每个主题都有独特的配色、动画和交互风格
 */

// ==================== 主题定义 ====================

/**
 * 主题类型枚举
 */
enum class AppThemeType(val icon: String) {
    KIMI_NO_NAWA("WbTwilight"),
    DEATH_NOTE("MenuBook"),
    NARUTO("Whatshot"),
    ONE_PIECE("Sailing");
    
    fun getDisplayName(): String = when (this) {
        KIMI_NO_NAWA -> Strings.themeKimiNoNawa
        DEATH_NOTE -> Strings.themeDeathNote
        NARUTO -> Strings.themeNaruto
        ONE_PIECE -> Strings.themeOnePiece
    }
    
    fun getDescription(): String = when (this) {
        KIMI_NO_NAWA -> Strings.themeKimiNoNawaDesc
        DEATH_NOTE -> Strings.themeDeathNoteDesc
        NARUTO -> Strings.themeNarutoDesc
        ONE_PIECE -> Strings.themeOnePieceDesc
    }
}

/**
 * 动画风格类型
 */
enum class AnimationStyle {
    SMOOTH,
    BOUNCY,
    SNAPPY,
    ELEGANT,
    PLAYFUL,
    DRAMATIC;
    
    fun getDisplayName(): String = when (this) {
        SMOOTH -> Strings.animSmooth
        BOUNCY -> Strings.animBouncy
        SNAPPY -> Strings.animSnappy
        ELEGANT -> Strings.animElegant
        PLAYFUL -> Strings.animPlayful
        DRAMATIC -> Strings.animDramatic
    }
}

/**
 * 交互反馈风格
 */
enum class InteractionStyle {
    RIPPLE,
    GLOW,
    SCALE,
    SHAKE,
    MORPH,
    PARTICLE;
    
    fun getDisplayName(): String = when (this) {
        RIPPLE -> Strings.interRipple
        GLOW -> Strings.interGlow
        SCALE -> Strings.interScale
        SHAKE -> Strings.interShake
        MORPH -> Strings.interMorph
        PARTICLE -> Strings.interParticle
    }
}

/**
 * 主题配置
 */
@Stable
data class AppTheme(
    val type: AppThemeType,
    val lightColors: ColorScheme,
    val darkColors: ColorScheme,
    val animationStyle: AnimationStyle,
    val interactionStyle: InteractionStyle,
    val gradients: ThemeGradients,
    val effects: ThemeEffects,
    val shapes: ThemeShapes,
    val wallpaperResId: Int = 0,           // 壁纸 drawable 资源 ID，0 = 使用渐变背景
    val animatedWallpaperResId: Int = 0    // GIF 动态壁纸资源 ID，0 = 无动态壁纸
)

/**
 * 主题渐变色
 */
@Stable
data class ThemeGradients(
    val primary: List<Color>,           // 主渐变
    val secondary: List<Color>,         // 次渐变
    val background: List<Color>,        // 背景渐变
    val accent: List<Color>,            // 强调渐变
    val shimmer: List<Color>            // 闪烁渐变
) {
    val primaryBrush: Brush get() = Brush.linearGradient(primary)
    val secondaryBrush: Brush get() = Brush.linearGradient(secondary)
    val backgroundBrush: Brush get() = Brush.linearGradient(background)
    val accentBrush: Brush get() = Brush.linearGradient(accent)
}

/**
 * 主题特效
 */
@Stable
data class ThemeEffects(
    val glowColor: Color,               // 发光颜色
    val glowRadius: Dp,                 // 发光半径
    val shadowColor: Color,             // 阴影颜色
    val shadowElevation: Dp,            // 阴影高度
    val blurRadius: Dp,                 // 模糊半径
    val particleColor: Color,           // 粒子颜色
    val enableParticles: Boolean,       // Yes否启用粒子
    val enableGlow: Boolean,            // Yes否启用发光
    val enableGlassmorphism: Boolean    // Yes否启用玻璃拟态
)

/**
 * 主题形状
 */
@Stable
data class ThemeShapes(
    val cornerRadius: Dp,               // 圆角大小
    val buttonRadius: Dp,               // 按钮圆角
    val cardRadius: Dp,                 // 卡片圆角
    val dialogRadius: Dp,               // 对话框圆角
    val useRoundedButtons: Boolean,     // Yes否使用圆形按钮
    val useSoftShadows: Boolean         // Yes否使用柔和阴影
)

// ==================== 主题定义 ====================

/**
 * 所有预置主题
 */
object AppThemes {
    
    
    
    // ========== 2. 你的名字 (黄昏之境) ==========
    val KimiNoNawa = AppTheme(
        type = AppThemeType.KIMI_NO_NAWA,
        lightColors = lightColorScheme(
            primary = Color(0xFFE85D3A),           // 彗星橙
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFFE0D6),
            onPrimaryContainer = Color(0xFF3D1200),
            secondary = Color(0xFF2E4482),         // 黄昏蓝
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFD6DFFF),
            onSecondaryContainer = Color(0xFF0A1842),
            tertiary = Color(0xFFF5A623),           // 夕阳金
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFFFE5B4),
            onTertiaryContainer = Color(0xFF3D2800),
            background = Color(0xFFFFF8F5),
            onBackground = Color(0xFF1A1520),
            surface = Color(0xFFFFFBF9),
            onSurface = Color(0xFF1A1520),
            surfaceVariant = Color(0xFFF5EDE8),
            onSurfaceVariant = Color(0xFF524640),
            outline = Color(0xFFD4A58C)
        ),
        darkColors = darkColorScheme(
            primary = Color(0xFFFF8A65),            // 暖橙
            onPrimary = Color(0xFF4A1800),
            primaryContainer = Color(0xFF8B3A1A),
            onPrimaryContainer = Color(0xFFFFE0D6),
            secondary = Color(0xFF8EAAFF),          // 夜空蓝
            onSecondary = Color(0xFF0A1842),
            secondaryContainer = Color(0xFF2A3F7A),
            onSecondaryContainer = Color(0xFFD6DFFF),
            tertiary = Color(0xFFFFD180),            // 星光金
            onTertiary = Color(0xFF3D2800),
            tertiaryContainer = Color(0xFF6B4A00),
            onTertiaryContainer = Color(0xFFFFE5B4),
            background = Color(0xFF0C0A14),         // 深夜靛蓝
            onBackground = Color(0xFFEDE6F0),
            surface = Color(0xFF14101E),
            onSurface = Color(0xFFEDE6F0),
            surfaceVariant = Color(0xFF221A30),
            onSurfaceVariant = Color(0xFFCCC0D8),
            outline = Color(0xFF6B5A8A)
        ),
        animationStyle = AnimationStyle.DRAMATIC,
        interactionStyle = InteractionStyle.GLOW,
        gradients = ThemeGradients(
            // 经典黄昏天空渐变: 深靛 → 紫 → 橙 → 粉
            primary = listOf(Color(0xFF0C0A14), Color(0xFF1E1240), Color(0xFF4A2060), Color(0xFFE85D3A), Color(0xFFF5A623)),
            secondary = listOf(Color(0xFF2E4482), Color(0xFF8B5E9A), Color(0xFFE85D3A)),
            background = listOf(Color(0xFF0C0A14), Color(0xFF1A1030), Color(0xFF261840)),
            accent = listOf(Color(0xFFFF8A65), Color(0xFFF5A623), Color(0xFFFFD700)),
            shimmer = listOf(Color(0x30FF8A65), Color(0x60F5A623), Color(0x30FFD700))
        ),
        effects = ThemeEffects(
            glowColor = Color(0xFFE85D3A),       // 彗星尾迹光
            glowRadius = 22.dp,
            shadowColor = Color(0x60E85D3A),
            shadowElevation = 14.dp,
            blurRadius = 20.dp,
            particleColor = Color(0xFFFFD700),    // 流星金色粒子
            enableParticles = false,
            enableGlow = true,
            enableGlassmorphism = true
        ),
        shapes = ThemeShapes(
            cornerRadius = 18.dp,
            buttonRadius = 14.dp,
            cardRadius = 22.dp,
            dialogRadius = 26.dp,
            useRoundedButtons = true,
            useSoftShadows = true
        ),
        wallpaperResId = R.drawable.wallpaper_kimi_no_nawa
    )
    
    
    // ========== 4. 死亡笔记 (死神之眉) ==========
    val DeathNote = AppTheme(
        type = AppThemeType.DEATH_NOTE,
        lightColors = lightColorScheme(
            primary = Color(0xFF8B0000),           // 死神纯红
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFFDAD6),
            onPrimaryContainer = Color(0xFF410002),
            secondary = Color(0xFF37474F),          // 铅灰
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFCFD8DC),
            onSecondaryContainer = Color(0xFF1A2327),
            tertiary = Color(0xFF4A148C),            // 深紫
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFE1BEE7),
            onTertiaryContainer = Color(0xFF1A0033),
            background = Color(0xFFFAF5F5),
            onBackground = Color(0xFF1C1B1B),
            surface = Color(0xFFFFFBFF),
            onSurface = Color(0xFF1C1B1B),
            surfaceVariant = Color(0xFFF0E6E6),
            onSurfaceVariant = Color(0xFF534343),
            outline = Color(0xFF857373)
        ),
        darkColors = darkColorScheme(
            primary = Color(0xFFFF6659),            // 血红
            onPrimary = Color(0xFF690005),
            primaryContainer = Color(0xFF6B0000),
            onPrimaryContainer = Color(0xFFFFDAD6),
            secondary = Color(0xFF90A4AE),          // 银灰
            onSecondary = Color(0xFF1A2327),
            secondaryContainer = Color(0xFF37474F),
            onSecondaryContainer = Color(0xFFCFD8DC),
            tertiary = Color(0xFFCE93D8),            // 淡紫
            onTertiary = Color(0xFF1A0033),
            tertiaryContainer = Color(0xFF4A148C),
            onTertiaryContainer = Color(0xFFE1BEE7),
            background = Color(0xFF0A0808),         // 深漆黑
            onBackground = Color(0xFFE6E1E1),
            surface = Color(0xFF120F0F),
            onSurface = Color(0xFFE6E1E1),
            surfaceVariant = Color(0xFF1E1A1A),
            onSurfaceVariant = Color(0xFFD4C4C4),
            outline = Color(0xFF857373)
        ),
        animationStyle = AnimationStyle.DRAMATIC,
        interactionStyle = InteractionStyle.GLOW,
        gradients = ThemeGradients(
            primary = listOf(Color(0xFF0A0808), Color(0xFF1A0000), Color(0xFF8B0000)),
            secondary = listOf(Color(0xFF37474F), Color(0xFF263238)),
            background = listOf(Color(0xFF0A0808), Color(0xFF120F0F), Color(0xFF1A0000)),
            accent = listOf(Color(0xFFFF6659), Color(0xFF8B0000)),
            shimmer = listOf(Color(0x30FF6659), Color(0x608B0000), Color(0x304A148C))
        ),
        effects = ThemeEffects(
            glowColor = Color(0xFF8B0000),
            glowRadius = 16.dp,
            shadowColor = Color(0x80FF6659),
            shadowElevation = 12.dp,
            blurRadius = 14.dp,
            particleColor = Color(0xFFFF6659),
            enableParticles = false,
            enableGlow = true,
            enableGlassmorphism = false
        ),
        shapes = ThemeShapes(
            cornerRadius = 4.dp,
            buttonRadius = 4.dp,
            cardRadius = 8.dp,
            dialogRadius = 12.dp,
            useRoundedButtons = false,
            useSoftShadows = false
        ),
        wallpaperResId = 0,  // 仅有 GIF，无静态 PNG，由 ThemedBackgroundBox 渲染
        animatedWallpaperResId = R.drawable.wallpaper_death_note
    )
    
    // ========== 5. 火影忍者 (火之意志) ==========
    val Naruto = AppTheme(
        type = AppThemeType.NARUTO,
        lightColors = lightColorScheme(
            primary = Color(0xFFEF6C00),           // 鸣人橙
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFFE0B2),
            onPrimaryContainer = Color(0xFF3E1C00),
            secondary = Color(0xFF2E7D32),          // 木叶绿
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFC8E6C9),
            onSecondaryContainer = Color(0xFF002106),
            tertiary = Color(0xFF1565C0),            // 查克拉蓝
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFBBDEFB),
            onTertiaryContainer = Color(0xFF001D36),
            background = Color(0xFFFFF8F0),
            onBackground = Color(0xFF2D1F0E),
            surface = Color(0xFFFFFBF5),
            onSurface = Color(0xFF2D1F0E),
            surfaceVariant = Color(0xFFF5EDE0),
            onSurfaceVariant = Color(0xFF524638),
            outline = Color(0xFFC4A882)
        ),
        darkColors = darkColorScheme(
            primary = Color(0xFFFFB74D),            // 暖橙
            onPrimary = Color(0xFF4A2800),
            primaryContainer = Color(0xFF7A4400),
            onPrimaryContainer = Color(0xFFFFE0B2),
            secondary = Color(0xFF81C784),          // 柔和绿
            onSecondary = Color(0xFF002106),
            secondaryContainer = Color(0xFF1B5E20),
            onSecondaryContainer = Color(0xFFC8E6C9),
            tertiary = Color(0xFF64B5F6),            // 柔和蓝
            onTertiary = Color(0xFF001D36),
            tertiaryContainer = Color(0xFF0D47A1),
            onTertiaryContainer = Color(0xFFBBDEFB),
            background = Color(0xFF14100A),         // 深暖棕
            onBackground = Color(0xFFF0E8D8),
            surface = Color(0xFF1C1610),
            onSurface = Color(0xFFF0E8D8),
            surfaceVariant = Color(0xFF2A2218),
            onSurfaceVariant = Color(0xFFD4C4A8),
            outline = Color(0xFF7A6B52)
        ),
        animationStyle = AnimationStyle.BOUNCY,
        interactionStyle = InteractionStyle.SCALE,
        gradients = ThemeGradients(
            primary = listOf(Color(0xFFEF6C00), Color(0xFFFF9800), Color(0xFFFFB74D)),
            secondary = listOf(Color(0xFF2E7D32), Color(0xFF66BB6A)),
            background = listOf(Color(0xFF14100A), Color(0xFF1C1610), Color(0xFF2A2218)),
            accent = listOf(Color(0xFFFFB74D), Color(0xFFEF6C00)),
            shimmer = listOf(Color(0x40FFB74D), Color(0x80EF6C00), Color(0x40FF9800))
        ),
        effects = ThemeEffects(
            glowColor = Color(0xFFEF6C00),
            glowRadius = 18.dp,
            shadowColor = Color(0x60EF6C00),
            shadowElevation = 12.dp,
            blurRadius = 16.dp,
            particleColor = Color(0xFFFFB74D),
            enableParticles = false,
            enableGlow = true,
            enableGlassmorphism = false
        ),
        shapes = ThemeShapes(
            cornerRadius = 10.dp,
            buttonRadius = 8.dp,
            cardRadius = 14.dp,
            dialogRadius = 18.dp,
            useRoundedButtons = false,
            useSoftShadows = true
        ),
        wallpaperResId = R.drawable.wallpaper_naruto
    )
    
    // ========== 6. 海贼王 (伟大航路) ==========
    val OnePiece = AppTheme(
        type = AppThemeType.ONE_PIECE,
        lightColors = lightColorScheme(
            primary = Color(0xFF0277BD),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFB3E5FC),
            onPrimaryContainer = Color(0xFF001F30),
            secondary = Color(0xFFC62828),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFFFCDD2),
            onSecondaryContainer = Color(0xFF410002),
            tertiary = Color(0xFFF9A825),
            onTertiary = Color(0xFF3E2700),
            tertiaryContainer = Color(0xFFFFF8E1),
            onTertiaryContainer = Color(0xFF261900),
            background = Color(0xFFF0F9FF),
            onBackground = Color(0xFF0A1929),
            surface = Color(0xFFF8FCFF),
            onSurface = Color(0xFF0A1929),
            surfaceVariant = Color(0xFFE0F0FA),
            onSurfaceVariant = Color(0xFF3A4C5A),
            outline = Color(0xFF6E8A9C)
        ),
        darkColors = darkColorScheme(
            primary = Color(0xFF4FC3F7),
            onPrimary = Color(0xFF001F30),
            primaryContainer = Color(0xFF01579B),
            onPrimaryContainer = Color(0xFFB3E5FC),
            secondary = Color(0xFFEF5350),
            onSecondary = Color(0xFF410002),
            secondaryContainer = Color(0xFF8E0000),
            onSecondaryContainer = Color(0xFFFFCDD2),
            tertiary = Color(0xFFFFD54F),
            onTertiary = Color(0xFF261900),
            tertiaryContainer = Color(0xFF7A5900),
            onTertiaryContainer = Color(0xFFFFF8E1),
            background = Color(0xFF051425),
            onBackground = Color(0xFFD8EEFF),
            surface = Color(0xFF0A1C30),
            onSurface = Color(0xFFD8EEFF),
            surfaceVariant = Color(0xFF12283E),
            onSurfaceVariant = Color(0xFFB0CCE0),
            outline = Color(0xFF5A7A90)
        ),
        animationStyle = AnimationStyle.BOUNCY,
        interactionStyle = InteractionStyle.RIPPLE,
        gradients = ThemeGradients(
            primary = listOf(Color(0xFF051425), Color(0xFF01579B), Color(0xFF0277BD)),
            secondary = listOf(Color(0xFFC62828), Color(0xFFF9A825)),
            background = listOf(Color(0xFF051425), Color(0xFF0A1C30)),
            accent = listOf(Color(0xFF4FC3F7), Color(0xFFFFD54F)),
            shimmer = listOf(Color(0x304FC3F7), Color(0x60FFD54F), Color(0x304FC3F7))
        ),
        effects = ThemeEffects(
            glowColor = Color(0xFF0277BD),
            glowRadius = 16.dp,
            shadowColor = Color(0x500277BD),
            shadowElevation = 10.dp,
            blurRadius = 14.dp,
            particleColor = Color(0xFFFFD54F),
            enableParticles = false,
            enableGlow = true,
            enableGlassmorphism = true
        ),
        shapes = ThemeShapes(
            cornerRadius = 14.dp,
            buttonRadius = 10.dp,
            cardRadius = 18.dp,
            dialogRadius = 22.dp,
            useRoundedButtons = true,
            useSoftShadows = true
        ),
        wallpaperResId = R.drawable.wallpaper_one_piece
    )
    
    /**
     * 获取所有主题
     */
    val allThemes = listOf(
        KimiNoNawa, DeathNote, Naruto, OnePiece
    )
    
    /**
     * 通过类型获取主题
     */
    fun getTheme(type: AppThemeType): AppTheme = allThemes.first { it.type == type }
    
    /**
     * 默认主题
     */
    val Default = KimiNoNawa
}

/**
 * 获取本地化的主题显示名称
 */
fun AppThemeType.getLocalizedDisplayName(): String {
    return when (this) {
        AppThemeType.KIMI_NO_NAWA -> com.webtoapp.core.i18n.Strings.themeKimiNoNawa
        AppThemeType.DEATH_NOTE -> com.webtoapp.core.i18n.Strings.themeDeathNote
        AppThemeType.NARUTO -> com.webtoapp.core.i18n.Strings.themeNaruto
        AppThemeType.ONE_PIECE -> com.webtoapp.core.i18n.Strings.themeOnePiece
    }
}

/**
 * 获取本地化的主题描述
 */
fun AppThemeType.getLocalizedDescription(): String {
    return when (this) {
        AppThemeType.KIMI_NO_NAWA -> com.webtoapp.core.i18n.Strings.themeKimiNoNawaDesc
        AppThemeType.DEATH_NOTE -> com.webtoapp.core.i18n.Strings.themeDeathNoteDesc
        AppThemeType.NARUTO -> com.webtoapp.core.i18n.Strings.themeNarutoDesc
        AppThemeType.ONE_PIECE -> com.webtoapp.core.i18n.Strings.themeOnePieceDesc
    }
}

/**
 * 获取本地化的动画风格显示名称
 */
fun AnimationStyle.getLocalizedDisplayName(): String {
    return when (this) {
        AnimationStyle.SMOOTH -> com.webtoapp.core.i18n.Strings.animSmooth
        AnimationStyle.BOUNCY -> com.webtoapp.core.i18n.Strings.animBouncy
        AnimationStyle.SNAPPY -> com.webtoapp.core.i18n.Strings.animSnappy
        AnimationStyle.ELEGANT -> com.webtoapp.core.i18n.Strings.animElegant
        AnimationStyle.PLAYFUL -> com.webtoapp.core.i18n.Strings.animPlayful
        AnimationStyle.DRAMATIC -> com.webtoapp.core.i18n.Strings.animDramatic
    }
}

/**
 * 获取本地化的交互风格显示名称
 */
fun InteractionStyle.getLocalizedDisplayName(): String {
    return when (this) {
        InteractionStyle.RIPPLE -> com.webtoapp.core.i18n.Strings.interRipple
        InteractionStyle.GLOW -> com.webtoapp.core.i18n.Strings.interGlow
        InteractionStyle.SCALE -> com.webtoapp.core.i18n.Strings.interScale
        InteractionStyle.SHAKE -> com.webtoapp.core.i18n.Strings.interShake
        InteractionStyle.MORPH -> com.webtoapp.core.i18n.Strings.interMorph
        InteractionStyle.PARTICLE -> com.webtoapp.core.i18n.Strings.interParticle
    }
}
