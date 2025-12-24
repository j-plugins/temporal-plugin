package com.github.xepozz.temporal.common.run

import com.github.xepozz.temporal.TemporalBundle
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.JBIntSpinner
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent

open class TemporalSettingsEditor(protected val project: Project) : SettingsEditor<TemporalRunConfiguration>() {
    protected val temporalExecutableField = TextFieldWithBrowseButton()
    protected val portField = JBIntSpinner(7233, 1, 65535)
    protected val logLevelField = JBTextField()
    protected val dynamicConfigValuesField = ExpandableTextField()
    protected val searchAttributesField = ExpandableTextField()

    init {
        temporalExecutableField.addBrowseFolderListener(
            project,
            FileChooserDescriptorFactory.singleFile()
                .withTitle(TemporalBundle.message("run.configuration.common.temporal.executable.browse.title"))
                .withDescription(null)
        )
    }

    override fun createEditor(): JComponent {
        return FormBuilder.createFormBuilder()
            .addLabeledComponent(TemporalBundle.message("run.configuration.common.temporal.executable.label"), temporalExecutableField)
            .addLabeledComponent(TemporalBundle.message("run.configuration.common.port.label"), portField)
            .addLabeledComponent(TemporalBundle.message("run.configuration.common.log.level.label"), logLevelField)
            .addLabeledComponent(TemporalBundle.message("run.configuration.common.dynamic.config.values.label"), dynamicConfigValuesField)
            .addLabeledComponent(TemporalBundle.message("run.configuration.common.search.attributes.label"), searchAttributesField)
            .panel
    }

    override fun resetEditorFrom(configuration: TemporalRunConfiguration) {
        temporalExecutableField.text = configuration.temporalExecutable ?: ""
        portField.value = configuration.port
        logLevelField.text = configuration.logLevel ?: ""
        dynamicConfigValuesField.text = configuration.dynamicConfigValues.entries.joinToString("\n") { "${it.key}=${it.value}" }
        searchAttributesField.text = configuration.searchAttributes.entries.joinToString("\n") { "${it.key}=${it.value}" }
    }

    override fun applyEditorTo(configuration: TemporalRunConfiguration) {
        configuration.temporalExecutable = temporalExecutableField.text
        configuration.port = portField.value as Int
        configuration.logLevel = logLevelField.text
        configuration.dynamicConfigValues = parseMap(dynamicConfigValuesField.text)
        configuration.searchAttributes = parseMap(searchAttributesField.text)
    }

    private fun parseMap(text: String): MutableMap<String, String> {
        return text.split("\n")
            .filter { it.contains("=") }
            .associate { it.substringBefore("=").trim() to it.substringAfter("=").trim() }
            .toMutableMap()
    }
}
