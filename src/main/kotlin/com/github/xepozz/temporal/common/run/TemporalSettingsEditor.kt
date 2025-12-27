package com.github.xepozz.temporal.common.run

import com.github.xepozz.temporal.TemporalBundle
import com.github.xepozz.temporal.common.configuration.TemporalExecutableManager
import com.github.xepozz.temporal.common.configuration.TemporalExecutablesConfigurable
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.ComboboxWithBrowseButton
import com.intellij.ui.JBIntSpinner
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent

open class TemporalSettingsEditor(protected val project: Project) : SettingsEditor<TemporalRunConfiguration>() {
    protected val temporalExecutableField = ComboboxWithBrowseButton(ComboBox<String>())
    protected val portField = JBIntSpinner(7233, 1, 65535)
    protected val uiPortField = JBIntSpinner(8233, 1, 65535)
    protected val metricsPortField = JBIntSpinner(57271, 1, 65535)
    protected val logLevelField = ComboBox(arrayOf("debug", "info", "warn", "error", "never"))
    protected val dynamicConfigValuesField = ExpandableTextField()
    protected val searchAttributesField = ExpandableTextField()
    protected val additionalArgsField = RawCommandLineEditor()

    init {
        temporalExecutableField.addActionListener {
            val configurable = TemporalExecutablesConfigurable(project)
            if (ShowSettingsUtil.getInstance().editConfigurable(project, configurable)) {
                updateExecutables()
            }
        }
        updateExecutables()
    }

    private fun updateExecutables() {
        val manager = TemporalExecutableManager.getInstance(project)
        val comboBox = temporalExecutableField.comboBox
        val selected = comboBox.selectedItem as String?
        comboBox.removeAllItems()
        for (executable in manager.executables) {
            executable.name.let { comboBox.addItem(it) }
        }
        comboBox.selectedItem = selected
    }

    override fun createEditor(): JComponent {
        return FormBuilder.createFormBuilder()
            .addLabeledComponent(TemporalBundle.message("run.configuration.common.temporal.executable.label"), temporalExecutableField)
            .addLabeledComponent(TemporalBundle.message("run.configuration.common.port.label"), portField)
            .addLabeledComponent(TemporalBundle.message("run.configuration.common.ui.port.label"), uiPortField)
            .addLabeledComponent(TemporalBundle.message("run.configuration.common.metrics.port.label"), metricsPortField)
            .addLabeledComponent(TemporalBundle.message("run.configuration.common.log.level.label"), logLevelField)
            .addLabeledComponent(TemporalBundle.message("run.configuration.common.dynamic.config.values.label"), dynamicConfigValuesField)
            .addLabeledComponent(TemporalBundle.message("run.configuration.common.search.attributes.label"), searchAttributesField)
            .addLabeledComponent(TemporalBundle.message("run.configuration.common.additional.args.label"), additionalArgsField)
            .panel
    }

    override fun resetEditorFrom(configuration: TemporalRunConfiguration) {
        updateExecutables()
        temporalExecutableField.comboBox.selectedItem = configuration.temporalExecutableName ?: ""
        portField.value = configuration.port
        uiPortField.value = configuration.uiPort
        metricsPortField.value = configuration.metricsPort
        logLevelField.selectedItem = configuration.logLevel
        dynamicConfigValuesField.text = configuration.dynamicConfigValues.entries.joinToString("\n") { "${it.key}=${it.value}" }
        searchAttributesField.text = configuration.searchAttributes.entries.joinToString("\n") { "${it.key}=${it.value}" }
        additionalArgsField.text = configuration.additionalArgs ?: ""
    }

    override fun applyEditorTo(configuration: TemporalRunConfiguration) {
        configuration.temporalExecutableName = temporalExecutableField.comboBox.selectedItem as String?
        configuration.port = portField.value as Int
        configuration.uiPort = uiPortField.value as Int
        configuration.metricsPort = metricsPortField.value as Int
        configuration.logLevel = logLevelField.selectedItem as String?
        configuration.dynamicConfigValues = parseMap(dynamicConfigValuesField.text)
        configuration.searchAttributes = parseMap(searchAttributesField.text)
        configuration.additionalArgs = additionalArgsField.text
    }

    private fun parseMap(text: String): MutableMap<String, String> {
        return text.split("\n")
            .filter { it.contains("=") }
            .associate { it.substringBefore("=").trim() to it.substringAfter("=").trim() }
            .toMutableMap()
    }
}
