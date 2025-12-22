package com.github.xepozz.temporal.common.uiViewer.configuration

import com.github.xepozz.temporal.TemporalBundle
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bindIntText
import com.intellij.ui.dsl.builder.bindText

class TemporalSettingsConfigurable(project: Project) : UiDslUnnamedConfigurable.Simple(), Configurable {
    private val settings = TemporalSettings.getInstance(project)

    override fun Panel.createContent() {
        group("Configuration") {
//            row {
//                checkBox("Enabled").bindSelected(settings::enabled)
//            }

            separator()

            row {
                textFieldWithBrowseButton(project = settings.project)
                    .label("Binary:")
                    .align(Align.FILL)
                    .bindText(settings::binary)
            }

            row {
                textField()
                    .label("Command:")
                    .align(Align.FILL)
                    .bindText(settings::command)
                    .comment("Use %port% to substitute the port number. All special symbols will be escaped automatically.")
            }

            row {
                textField()
                    .label("Address:")
                    .bindText(settings::address)
            }
            row {
                intTextField(range = IntRange(1, 65535), keyboardStep = 1)
                    .label("Port:")
                    .bindIntText(settings::port)
            }

        }
    }

    override fun getDisplayName() = TemporalBundle.message("settings.configurable.title")
}