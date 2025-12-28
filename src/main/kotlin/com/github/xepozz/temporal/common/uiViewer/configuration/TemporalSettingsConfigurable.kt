package com.github.xepozz.temporal.common.uiViewer.configuration

import com.github.xepozz.temporal.TemporalBundle
import com.github.xepozz.temporal.common.run.TemporalConfigurationType
import com.intellij.execution.RunManagerEx
import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.DslConfigurableBase
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.ComboboxWithBrowseButton
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toMutableProperty

class TemporalSettingsConfigurable(val myProject: Project) : DslConfigurableBase(), Configurable {
    private val settings = myProject.service<TemporalSettings>()
    private val temporalConfigurationsField = ComboboxWithBrowseButton(ComboBox<String>())

    private val panel = panel {
        group("Configuration") {
//            row {
//                checkBox("Enabled").bindSelected(settings::enabled)
//            }

//            separator()

            row(TemporalBundle.message("run.configuration.common.temporal.executable.label")) {
                cell(temporalConfigurationsField)
                    .bind(
                        { it.comboBox.selectedItem as? String },
                        { _, value -> settings.uiViewerConfiguration = value },
                        settings::uiViewerConfiguration.toMutableProperty()
                    )
                    .align(AlignX.FILL)
            }.layout(RowLayout.PARENT_GRID)
        }
    }

    init {
        temporalConfigurationsField.addActionListener {
            val runManager = RunManagerEx.getInstanceEx(myProject)
            runManager.getConfigurationSettingsList(TemporalConfigurationType.getInstance())
//            val configurable = TemporalExecutablesConfigurable(myProject)
//            if (ShowSettingsUtil.getInstance().editConfigurable(myProject, configurable)) {
//                updateExecutables()
//            }
        }
        updateExecutables()
    }

    private fun updateExecutables() {
        val comboBox = temporalConfigurationsField.comboBox
        val selected = comboBox.selectedItem as String?
        comboBox.removeAllItems()

        val runManager = RunManagerEx.getInstanceEx(myProject)
        runManager
            .getConfigurationSettingsList(TemporalConfigurationType.getInstance())
            .forEach {
                comboBox.addItem(it.name)
            }


//        val manager = TemporalExecutablesSettings.getInstance(myProject)
//        for (executable in manager.executables) {
//            executable.name.let { comboBox.addItem(it) }
//        }
        comboBox.selectedItem = selected
    }

    override fun getDisplayName() = TemporalBundle.message("settings.configurable.title")
    override fun createPanel() = panel
}