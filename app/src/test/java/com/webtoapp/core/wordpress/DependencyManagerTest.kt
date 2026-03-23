package com.webtoapp.core.wordpress

import android.content.Context
import com.google.common.truth.Truth.assertThat
import java.io.File
import java.util.Locale
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DependencyManagerTest {

    private lateinit var context: Context
    private var originalLocale: Locale = Locale.getDefault()

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        originalLocale = Locale.getDefault()
        DependencyManager.setMirrorRegion(null)
        DependencyManager.clearCache(context)
    }

    @After
    fun tearDown() {
        DependencyManager.setMirrorRegion(null)
        Locale.setDefault(originalLocale)
        DependencyManager.clearCache(context)
    }

    @Test
    fun `manual mirror region controls mirror config source list`() {
        DependencyManager.setMirrorRegion(DependencyManager.MirrorRegion.CN)
        val cn = DependencyManager.getMirrorConfig()
        assertThat(DependencyManager.getMirrorRegion()).isEqualTo(DependencyManager.MirrorRegion.CN)
        assertThat(cn.phpUrls.size).isGreaterThan(1)
        assertThat(cn.wordpressUrls.size).isGreaterThan(1)

        DependencyManager.setMirrorRegion(DependencyManager.MirrorRegion.GLOBAL)
        val global = DependencyManager.getMirrorConfig()
        assertThat(DependencyManager.getMirrorRegion()).isEqualTo(DependencyManager.MirrorRegion.GLOBAL)
        assertThat(global.phpUrls).hasSize(1)
    }

    @Test
    fun `auto mirror region follows locale language`() {
        DependencyManager.setMirrorRegion(null)

        Locale.setDefault(Locale.CHINESE)
        assertThat(DependencyManager.getMirrorRegion()).isEqualTo(DependencyManager.MirrorRegion.CN)

        Locale.setDefault(Locale.ENGLISH)
        assertThat(DependencyManager.getMirrorRegion()).isEqualTo(DependencyManager.MirrorRegion.GLOBAL)
    }

    @Test
    fun `deps directories are created and readiness checks follow files`() {
        val depsDir = DependencyManager.getDepsDir(context)
        val phpDir = DependencyManager.getPhpDir(context)
        val wpProjectsDir = DependencyManager.getWordPressProjectsDir(context)

        assertThat(depsDir.exists()).isTrue()
        assertThat(phpDir.exists()).isTrue()
        assertThat(wpProjectsDir.exists()).isTrue()

        assertThat(DependencyManager.isWordPressReady(context)).isFalse()
        assertThat(DependencyManager.isSqlitePluginReady(context)).isFalse()

        File(depsDir, "wordpress/wp-includes/version.php").apply {
            parentFile?.mkdirs()
            writeText("<?php")
        }
        File(depsDir, "sqlite-database-integration/load.php").apply {
            parentFile?.mkdirs()
            writeText("<?php")
        }

        assertThat(DependencyManager.isWordPressReady(context)).isTrue()
        assertThat(DependencyManager.isSqlitePluginReady(context)).isTrue()

        File(phpDir, "php").apply {
            writeText("#!/system/bin/sh")
            setExecutable(true)
        }

        assertThat(DependencyManager.isPhpReady(context)).isTrue()
        assertThat(DependencyManager.isAllReady(context)).isTrue()
        assertThat(DependencyManager.getPhpExecutablePath(context)).contains("/wordpress_deps/php/")
    }
}
