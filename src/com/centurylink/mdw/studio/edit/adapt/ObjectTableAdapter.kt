package com.centurylink.mdw.studio.edit.adapt

import com.centurylink.mdw.model.asset.Pagelet.Widget
import com.centurylink.mdw.studio.edit.apply.WidgetApplier
import com.centurylink.mdw.studio.edit.default
import org.json.JSONArray
import org.json.JSONObject

/**
 * Converts workflowObj object values to table form for Configurator display.
 * The process Variables tab is one usage.
 */
@Suppress("unused")
class ObjectTableAdapter(applier: WidgetApplier) : TableAdapter(applier) {

    /**
     * Widget value is a JSONObject coming in (unlike every other adapter).
     * Convert value to a JSONArray of JSONArrays of strings.
     *
     */
    override fun didInit(widget: Widget) {
        if (widget.value == null) {
            widget.value = JSONArray()
            val row = createEmptyRow(widget)
            (widget.value as JSONArray).put(row)
        }
        else {
            widget.value = toTable(widget, widget.value as JSONObject)
        }
    }

    /**
     * Convert from JSONArray of JSONArrays to JSONObject
     */
    override fun willUpdate(widget: Widget) {
        widget.value?.let {
            widget.value = withoutEmptyRows(it as JSONArray)
            widget.value?.let {
                widget.value = toObject(widget, it as JSONArray)
            }
        }
    }

    private fun toTable(tableWidget: Widget, jsonObject: JSONObject): JSONArray {
        val jsonArray = JSONArray()
        for (name in jsonObject.keySet()) {
            val row = JSONArray()
            val obj = jsonObject.getJSONObject(name)
            for (columnWidget in tableWidget.widgets) {
                if (columnWidget.name == "_key") {
                    row.put(name)
                }
                else {
                    row.put(obj.optString(columnWidget.name))
                }
            }
            jsonArray.put(row)
        }

        return jsonArray
    }

    private fun toObject(tableWidget: Widget, rows: JSONArray): JSONObject {
        val jsonObject = JSONObject()
        for (i in 0 until rows.length()) {
            val row = rows.getJSONArray(i)
            val rowObj = JSONObject()
            for (j in tableWidget.widgets.indices) {
                val columnWidget = tableWidget.widgets[j]
                if (columnWidget.name == "_key") {
                    jsonObject.put(row.getString(j), rowObj)
                }
                else {
                    val value = row.getString(j)
                    if (value.isBlank()) {
                        columnWidget.default?.let {rowObj.put(columnWidget.name, it)}
                    }
                    else {
                        rowObj.put(columnWidget.name, value)
                    }
                }
            }
        }
        return jsonObject
    }
}