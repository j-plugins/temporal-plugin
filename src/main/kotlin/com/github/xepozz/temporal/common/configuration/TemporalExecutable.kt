package com.github.xepozz.temporal.common.configuration

import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Tag

@Tag("executable")
data class TemporalExecutable(
    @Attribute("name")
    var name: String = "",
    @Attribute("path")
    var path: String = "",
)