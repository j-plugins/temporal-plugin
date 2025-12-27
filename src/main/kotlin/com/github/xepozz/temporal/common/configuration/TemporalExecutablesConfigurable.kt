package com.github.xepozz.temporal.common.configuration

import com.github.xepozz.temporal.TemporalBundle
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.CollectionListModel
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignY
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.event.DocumentEvent
import javax.swing.event.ListSelectionEvent

class TemporalExecutablesConfigurable(private val project: Project) : SearchableConfigurable, Configurable {
    private val manager = TemporalExecutableManager.getInstance(project)
    private val listModel = CollectionListModel<TemporalExecutable>()
    private val list = JBList(listModel)

    private val nameField = com.intellij.ui.components.JBTextField()
    private val pathField = TextFieldWithBrowseButton()

    private var selectedExecutable: TemporalExecutable? = null

    override fun getDisplayName(): String = TemporalBundle.message("settings.executables.title")
    override fun getId(): String = "settings.temporal.executables"

    override fun createComponent(): JComponent {
        list.selectionModel.addListSelectionListener { e: ListSelectionEvent ->
            if (!e.valueIsAdjusting) {
                updateDetails()
            }
        }

        list.cellRenderer = object : com.intellij.ui.ColoredListCellRenderer<TemporalExecutable>() {
            override fun customizeCellRenderer(
                list: javax.swing.JList<out TemporalExecutable>,
                value: TemporalExecutable?,
                index: Int,
                selected: Boolean,
                hasFocus: Boolean
            ) {
                if (value != null) {
                    append(value.name)
                }
            }
        }

        pathField.addBrowseFolderListener(
            project,
            FileChooserDescriptorFactory.singleFile()
                .withTitle(TemporalBundle.message("run.configuration.common.temporal.executable.browse.title"))
        )

        val decorator = ToolbarDecorator.createDecorator(list)
            .setAddAction {
                val newExec = TemporalExecutable(name = "New Configuration", path = "" )
                listModel.add(newExec)
                list.setSelectedValue(newExec, true)
            }
            .setRemoveAction {
                val index = list.selectedIndex
                if (index != -1) {
                    listModel.remove(index)
                    if (listModel.size > 0) {
                        list.selectedIndex = Math.min(index, listModel.size - 1)
                    }
                }
            }
            .disableUpDownActions()

        val listPanel = decorator.createPanel()

        val checkVersionButton = javax.swing.JButton(TemporalBundle.message("settings.executables.check.version.button")).apply {
            addActionListener {
                val version = TemporalVersionChecker.checkVersion(pathField.text)
                Messages.showInfoMessage(project, version, TemporalBundle.message("settings.executables.check.version.title"))
            }
        }

        val detailsPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(TemporalBundle.message("settings.executables.name.label"), nameField)
            .addLabeledComponent(TemporalBundle.message("settings.executables.path.label"), pathField)
            .addComponent(checkVersionButton)
            .panel

        nameField.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                selectedExecutable?.name = nameField.text
                list.repaint()
            }
        })

        pathField.textField.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                selectedExecutable?.path = pathField.text
            }
        })

        return panel {
            row {
                cell(listPanel).align(Align.FILL)
                cell(detailsPanel).align(Align.FILL).align(AlignY.TOP)
            }.resizableRow()
        }
    }

    private fun updateDetails() {
        selectedExecutable = list.selectedValue
        if (selectedExecutable != null) {
            nameField.isEnabled = true
            pathField.isEnabled = true
            nameField.text = selectedExecutable?.name ?: ""
            pathField.text = selectedExecutable?.path ?: ""
        } else {
            nameField.isEnabled = false
            pathField.isEnabled = false
            nameField.text = ""
            pathField.text = ""
        }
    }

    override fun isModified(): Boolean {
        val currentExecutables = manager.executables
        if (listModel.items.size != currentExecutables.size) return true
        for (i in listModel.items.indices) {
            if (listModel.items[i] != currentExecutables[i]) return true
        }
        return false
    }

    override fun apply() {
        manager.executables.clear()
        manager.executables.addAll(listModel.items.map { it.copy() })
    }

    override fun reset() {
        listModel.removeAll()
        listModel.addAll(0, manager.executables.map { it.copy() })
        if (listModel.size > 0) {
            list.selectedIndex = 0
        } else {
            updateDetails()
        }
    }
}
