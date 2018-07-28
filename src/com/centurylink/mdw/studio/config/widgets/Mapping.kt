package com.centurylink.mdw.studio.config.widgets

import com.centurylink.mdw.model.asset.Pagelet
import com.centurylink.mdw.model.variable.Variable
import com.centurylink.mdw.model.workflow.Process
import com.centurylink.mdw.studio.edit.init
import com.centurylink.mdw.studio.edit.isReadonly
import com.centurylink.mdw.studio.edit.source
import org.json.JSONObject
import java.io.IOException
import java.lang.IllegalArgumentException

/**
 * Map will have been converted to a table by its adapter.
 */
@Suppress("unused")
class Mapping(widget: Pagelet.Widget) : Table(widget, false, false) {

    override fun initialColumnWidgets(): List<Pagelet.Widget> {
        val colWidgs = mutableListOf<Pagelet.Widget>()
        val pagelet = Pagelet(JSONObject("{ \"widgets\": [] }"))

        val label = if (widget.source == null) "Input Variable" else "${widget.source} Variable"
        val varWidget = pagelet.Widget(label, "text")
        varWidget.init("table", workflowObj)
        varWidget.isReadonly = true
        colWidgs.add(varWidget)

        val typeWidget = pagelet.Widget("Type", "text")
        typeWidget.init("table", workflowObj)
        typeWidget.isReadonly = true
        colWidgs.add(typeWidget)

        val modeWidget = pagelet.Widget("Mode", "text")
        modeWidget.init("table", workflowObj)
        modeWidget.isReadonly = true
        colWidgs.add(modeWidget)

        val exprWidget = pagelet.Widget("Binding Expression", "text")
        exprWidget.init("table", workflowObj)
        colWidgs.add(exprWidget)

        return colWidgs
    }

    /**
     * TODO: versioning
     */
    private val bindingVariables: List<Variable> by lazy {
        if (widget.source == "Subprocess") {
            val procName = workflowObj.getAttribute("processname")
            procName ?: throw IllegalArgumentException("Missing processname attribute")
            var file = projectSetup.getAssetFile(procName)
            if (file == null) {
                file = projectSetup.getAssetFile(procName + ".proc")
            }
            file ?: throw IOException("Missing subprocess asset: " + procName)
            val process = Process(JSONObject(String(file.contentsToByteArray())))
            getBindingVars(process, true)
        }
        else {
            getBindingVars(workflowObj.process, false)
        }
    }

    override fun initialRows(): MutableList<Array<String>> {
        val rows = mutableListOf<Array<String>>()
        // initialize rows from widget value
        widget.value?.let {
            val mappingJson = it as JSONObject
            for (bindingVariable in bindingVariables) {
                rows.add(arrayOf(bindingVariable.name, bindingVariable.type, bindingVariable.category,
                        mappingJson.optString(bindingVariable.name)))
            }
        }
        return rows
    }

    private fun getBindingVars(process: Process, includeOuts: Boolean): List<Variable> {
        val bindingVars = mutableListOf<Variable>()
        process.variables?.let {
            for (variable in it) {
                if (variable.category == "INPUT" || variable.category == "INOUT" ||
                        (includeOuts && variable.category == "OUTPUT")) {
                    bindingVars.add(variable)
                }
            }
        }

        bindingVars.sortBy { it.name }
        return bindingVars
    }

    override fun widgetValueFromRows(rows: List<Array<String>>): Any {
        val updatedMapping = JSONObject()
        for (row in rows) {
            val bindingExpr = row[3]
            if (!bindingExpr.isNullOrBlank()) {
                updatedMapping.put(row[0], bindingExpr)
            }
        }
        return updatedMapping
    }
}