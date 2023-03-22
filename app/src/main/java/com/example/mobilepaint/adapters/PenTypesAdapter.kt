package com.example.mobilepaint.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.mobilepaint.Utils.toPx
import com.example.mobilepaint.models.PenType

class PenTypesAdapter(
    context: Context,
    items: List<PenType>
): ArrayAdapter<PenType>(context, android.R.layout.simple_dropdown_item_1line, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_dropdown_item_1line, parent, false)
        val textView = view as TextView
        val item = getItem(position)
        textView.text = item?.text
        val startDrawable = if (item == null) null else ContextCompat.getDrawable(context, item.iconRes)
        textView.compoundDrawablePadding = 4.toPx.toInt()
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(startDrawable, null, null, null)
        return view
    }

}