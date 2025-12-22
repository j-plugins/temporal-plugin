package com.github.xepozz.temporal.common.uiViewer

import com.intellij.openapi.util.IconLoader

// https://intellij-icons.jetbrains.design
// https://plugins.jetbrains.com/docs/intellij/icons.html#new-ui-tool-window-icons
// https://plugins.jetbrains.com/docs/intellij/icons-style.html
object BuggregatorIcons {
    @JvmField
    val BUGGREGATOR_FAILED = IconLoader.getIcon("/icons/buggregator/failed.svg", this::class.java)

    @JvmField
    val BUGGREGATOR_SUCCESS = IconLoader.getIcon("/icons/buggregator/success.svg", this::class.java)
}