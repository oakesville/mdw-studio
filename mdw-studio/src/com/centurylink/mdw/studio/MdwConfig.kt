package com.centurylink.mdw.studio

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.PathChooserDialog
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.components.CheckBox
import java.awt.*
import javax.swing.*
import javax.swing.event.DocumentEvent

class MdwConfig : SearchableConfigurable {

    var modified = false

    private val settingsPanel = JPanel(BorderLayout())
    private val mdwHomeText = object: TextFieldWithBrowseButton() {
        override fun getPreferredSize(): Dimension {
            return Dimension(500, super.getPreferredSize().height)
        }
    }

    private val serverPollingCheckbox = CheckBox("Poll to detect running server (requires restart)")

    private val gridLinesCheckbox = CheckBox("Show grid lines when editable")
    private val zoomSlider = object : JSlider(20, 200, 100) {
        override fun getPreferredSize(): Dimension {
            return Dimension(500, super.getPreferredSize().height)
        }
    }

    private val syncDynamicJavaCheckbox = CheckBox("Sync dynamic Java class name")
    private val attributeContentInEditorTabCheckbox = CheckBox("Open dynamic Java and script activity content in editor tab")
    private val createAndAssociateTaskCheckbox = CheckBox("Create and associate task template")

    init {
        settingsPanel.layout = GridBagLayout()

        val gridConstraints = GridBagConstraints()
        gridConstraints.anchor = GridBagConstraints.NORTH
        gridConstraints.gridx = 0
        gridConstraints.gridy = 0
        gridConstraints.fill = GridBagConstraints.HORIZONTAL
        gridConstraints.weightx = 1.0
        gridConstraints.weighty = 1.0

        // environment
        val envPanel = JPanel()
        envPanel.layout = BoxLayout(envPanel, BoxLayout.Y_AXIS)
        envPanel.border = IdeBorderFactory.createTitledBorder("Environment")
        settingsPanel.add(envPanel, gridConstraints)

        // mdw home
        val mdwHomePanel = JPanel(FlowLayout(FlowLayout.LEFT, 5, 5))
        mdwHomePanel.alignmentX = Component.LEFT_ALIGNMENT
        envPanel.add(mdwHomePanel)
        val mdwHomeLabel = JLabel("MDW Home:")
        mdwHomePanel.add(mdwHomeLabel)
        mdwHomeText.text = MdwSettings.instance.mdwHome
        val descriptor = FileChooserDescriptor(false, true, false, false, false, false)
        descriptor.putUserData(PathChooserDialog.PREFER_LAST_OVER_EXPLICIT, false)
        mdwHomeText.addBrowseFolderListener(null, null, null, descriptor)
        mdwHomeText.textField.document.addDocumentListener(object: DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                modified = true
            }
        })
        mdwHomePanel.add(mdwHomeText)
        val mdwHomeHelp = JLabel("MDW CLI installation directory (requires restart)")
        mdwHomeHelp.alignmentX = Component.LEFT_ALIGNMENT
        mdwHomeHelp.foreground = Color.GRAY
        mdwHomeHelp.border = BorderFactory.createEmptyBorder(0, 90, 0, 0)
        envPanel.add(mdwHomeHelp)

        // server polling
        serverPollingCheckbox.alignmentX = Component.LEFT_ALIGNMENT
        serverPollingCheckbox.border = BorderFactory.createEmptyBorder(10, 0, 5, 0)
        serverPollingCheckbox.isSelected = !MdwSettings.instance.isSuppressServerPolling
        serverPollingCheckbox.addActionListener {
            modified = true
        }
        envPanel.add(serverPollingCheckbox)

        // canvas
        gridConstraints.gridy = 1
        val canvasPanel = JPanel()
        canvasPanel.layout = BoxLayout(canvasPanel, BoxLayout.Y_AXIS)
        canvasPanel.border = IdeBorderFactory.createTitledBorder("Canvas")
        settingsPanel.add(canvasPanel, gridConstraints)

        // grid lines
        gridLinesCheckbox.alignmentX = Component.LEFT_ALIGNMENT
        gridLinesCheckbox.border = BorderFactory.createEmptyBorder(0, 0, 5, 0)
        gridLinesCheckbox.isSelected = !MdwSettings.instance.isHideCanvasGridLines
        gridLinesCheckbox.addActionListener {
            modified = true
        }
        canvasPanel.add(gridLinesCheckbox)

        // canvas zoom
        val zoomPanel = JPanel(FlowLayout(FlowLayout.LEFT, 5, 5))
        zoomPanel.alignmentX = Component.LEFT_ALIGNMENT
        canvasPanel.add(zoomPanel)
        val zoomLabel = JLabel("Canvas Zoom:")
        zoomPanel.add(zoomLabel)

        zoomSlider.alignmentX = Component.LEFT_ALIGNMENT
        zoomSlider.value = MdwSettings.instance.canvasZoom
        zoomSlider.minorTickSpacing = 10
        zoomSlider.majorTickSpacing = 20
        zoomSlider.paintTicks = true
        zoomSlider.paintLabels = true
        zoomSlider.addChangeListener {
            modified = true
        }
        zoomPanel.add(zoomSlider)

        // editing
        gridConstraints.gridy = 2
        val editPanel = JPanel()
        editPanel.layout = BoxLayout(editPanel, BoxLayout.Y_AXIS)
        editPanel.border = IdeBorderFactory.createTitledBorder("Editing")
        settingsPanel.add(editPanel, gridConstraints)

        // sync dynamic java classname
        syncDynamicJavaCheckbox.alignmentX = Component.LEFT_ALIGNMENT
        syncDynamicJavaCheckbox.border = BorderFactory.createEmptyBorder(0, 0, 5, 0)
        syncDynamicJavaCheckbox.isSelected = MdwSettings.instance.isSyncDynamicJavaClassName
        syncDynamicJavaCheckbox.addActionListener {
            modified = true
        }
        editPanel.add(syncDynamicJavaCheckbox)

        // open attribute content in editor tab
        attributeContentInEditorTabCheckbox.alignmentX = Component.LEFT_ALIGNMENT
        attributeContentInEditorTabCheckbox.border = BorderFactory.createEmptyBorder(0, 0, 5, 0)
        attributeContentInEditorTabCheckbox.isSelected = MdwSettings.instance.isOpenAttributeContentInEditorTab
        attributeContentInEditorTabCheckbox.addActionListener {
            modified = true
        }
        editPanel.add(attributeContentInEditorTabCheckbox)

        // create and associate task template
        createAndAssociateTaskCheckbox.alignmentX = Component.LEFT_ALIGNMENT
        createAndAssociateTaskCheckbox.border = BorderFactory.createEmptyBorder(0, 0, 5, 0)
        createAndAssociateTaskCheckbox.isSelected = MdwSettings.instance.isCreateAndAssociateTaskTemplate
        createAndAssociateTaskCheckbox.addActionListener {
            modified = true
        }
        editPanel.add(createAndAssociateTaskCheckbox)


        // leftover vertical space
        gridConstraints.gridy = 3
        gridConstraints.fill = GridBagConstraints.VERTICAL
        gridConstraints.gridheight = GridBagConstraints.REMAINDER
        gridConstraints.weighty = 100.0
        val glue = Box.createVerticalGlue()
        settingsPanel.add(glue, gridConstraints)
    }

    override fun getId(): String {
        return "com.centurylink.mdw.studio"
    }

    override fun getDisplayName(): String {
        return "MDW"
    }

    override fun createComponent(): JComponent {
        return settingsPanel
    }
    override fun isModified(): Boolean {
        return modified
    }

    override fun apply() {
        val mdwSettings = MdwSettings.instance

        mdwSettings.isSuppressServerPolling = !serverPollingCheckbox.isSelected

        mdwSettings.isHideCanvasGridLines = !gridLinesCheckbox.isSelected
        mdwSettings.canvasZoom = zoomSlider.value

        mdwSettings.isSyncDynamicJavaClassName = syncDynamicJavaCheckbox.isSelected
        mdwSettings.isOpenAttributeContentInEditorTab = attributeContentInEditorTabCheckbox.isSelected
        mdwSettings.isCreateAndAssociateTaskTemplate = createAndAssociateTaskCheckbox.isSelected
    }
}