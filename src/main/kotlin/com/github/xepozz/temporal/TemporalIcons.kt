package com.github.xepozz.temporal

import com.intellij.openapi.util.IconLoader

object TemporalIcons {
    @JvmStatic
    val TEMPORAL = IconLoader.getIcon("/icons/temporal/icon.svg", javaClass)

    @JvmField
    val CLEAR_OUTPUTS = IconLoader.getIcon("/icons/clearOutputs/icon.svg", this::class.java)
}
