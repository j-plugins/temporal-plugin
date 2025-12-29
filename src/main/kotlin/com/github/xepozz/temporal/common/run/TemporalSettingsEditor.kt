package com.github.xepozz.temporal.common.run

import com.github.xepozz.temporal.TemporalBundle
import com.github.xepozz.temporal.common.configuration.TemporalExecutablesConfigurable
import com.github.xepozz.temporal.common.configuration.TemporalExecutablesSettings
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.ComboboxWithBrowseButton
import com.intellij.ui.JBIntSpinner
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.bindIntValue
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toMutableProperty

open class TemporalSettingsEditor(
    private val project: Project,
    private val configuration: TemporalRunConfiguration,
) : SettingsEditor<TemporalRunConfiguration>() {
    protected val temporalExecutableField = ComboboxWithBrowseButton(ComboBox<String>())
    protected val portField = JBIntSpinner(7233, 1, 65535)
    protected val uiPortField = JBIntSpinner(8233, 1, 65535)
    protected val metricsPortField = JBIntSpinner(57271, 1, 65535)
    protected val logLevelField = ComboBox(arrayOf("debug", "info", "warn", "error", "never"))
    protected val dynamicConfigValuesField = ExpandableTextField()
    protected val searchAttributesField = ExpandableTextField()
    protected val additionalArgsField = RawCommandLineEditor()

    private val panel = panel {
        row(TemporalBundle.message("run.configuration.common.temporal.executable.label")) {
            cell(temporalExecutableField)
                .bind(
                    { it.comboBox.selectedItem as? String },
                    { _, value -> configuration.temporalExecutableName = value },
                    configuration::temporalExecutableName.toMutableProperty()
                )
                .align(AlignX.FILL)
        }.layout(RowLayout.PARENT_GRID)
        row(TemporalBundle.message("run.configuration.common.log.level.label")) {
            cell(logLevelField)
        }.layout(RowLayout.PARENT_GRID)
        row(TemporalBundle.message("run.configuration.common.dynamic.config.values.label")) {
            cell(dynamicConfigValuesField).bind(
                { parseMap(it.text) },
                { _, value -> configuration.dynamicConfigValues = value },
                configuration::dynamicConfigValues.toMutableProperty()
            )
                .align(AlignX.FILL)
        }.layout(RowLayout.PARENT_GRID)
        row(TemporalBundle.message("run.configuration.common.search.attributes.label")) {
            cell(searchAttributesField).bind(
                { parseMap(it.text) },
                { _, value -> configuration.searchAttributes = value },
                configuration::searchAttributes.toMutableProperty()
            )
                .align(AlignX.FILL)
        }.layout(RowLayout.PARENT_GRID)
        row(TemporalBundle.message("run.configuration.common.additional.args.label")) {
            cell(additionalArgsField)
                .onChanged { this@TemporalSettingsEditor.fireEditorStateChanged() }
                .bind(
                    { it.text },
                    { _, value -> configuration.additionalArgs = value },
                    configuration::additionalArgs.toMutableProperty()
                )
                .align(AlignX.FILL)
        }.layout(RowLayout.PARENT_GRID)
        group(TemporalBundle.message("run.configuration.common.ports.label"), false) {
            row {
                cell(portField)
                    .bindIntValue(configuration::port)
                    .label(TemporalBundle.message("run.configuration.common.server.label"))
                cell(uiPortField)
                    .bindIntValue(configuration::uiPort)
                    .label(TemporalBundle.message("run.configuration.common.ui.label"))
                cell(metricsPortField)
                    .bindIntValue(configuration::metricsPort)
                    .label(TemporalBundle.message("run.configuration.common.metrics.label"))
            }.layout(RowLayout.PARENT_GRID)
        }.layout(RowLayout.PARENT_GRID)
    }

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
        val manager = TemporalExecutablesSettings.getInstance(project)
        val comboBox = temporalExecutableField.comboBox
        val selected = comboBox.selectedItem as String?
        comboBox.removeAllItems()
        for (executable in manager.executables) {
            executable.name.let { comboBox.addItem(it) }
        }
        comboBox.selectedItem = selected
    }

    override fun createEditor() = panel

    override fun isSpecificallyModified() = panel.isModified().apply { println("isModified $this") }

    override fun resetEditorFrom(runConfiguration: TemporalRunConfiguration) {
        panel.reset()
    }

    override fun applyEditorTo(runConfiguration: TemporalRunConfiguration) {
        panel.apply()
    }

    private fun parseMap(text: String): MutableMap<String, String> {
        return text.split("\n")
            .filter { it.contains("=") }
            .associate { it.substringBefore("=").trim() to it.substringAfter("=").trim() }
            .toMutableMap()
    }
}
