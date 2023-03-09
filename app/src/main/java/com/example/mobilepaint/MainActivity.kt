package com.example.mobilepaint

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import com.example.mobilepaint.databinding.ActivityMainBinding
import com.example.mobilepaint.drawing_view.GeometryType
import com.example.mobilepaint.drawing_view.ShapesView
import com.google.android.material.slider.Slider
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener


class MainActivity : AppCompatActivity(), ShapesView.OnShapeChanged, View.OnClickListener, ColorEnvelopeListener, AdapterView.OnItemClickListener,
    Slider.OnChangeListener {

    private lateinit var binding : ActivityMainBinding

    private lateinit var menu : Menu

    private val options by lazy {
        listOf(
            PenType(getString(R.string.cursor), R.drawable.ic_hand, GeometryType.HAND),
            PenType(getString(R.string.path), R.drawable.ic_curve, GeometryType.PATH),
            PenType(getString(R.string.line), R.drawable.ic_line, GeometryType.LINE),
            PenType(getString(R.string.ellipse), R.drawable.ic_ellipse, GeometryType.ELLIPSE),
            PenType(getString(R.string.rectangle), R.drawable.ic_rectangle, GeometryType.RECT),
            PenType(getString(R.string.arrow), R.drawable.ic_arrow, GeometryType.ARROW),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.shapesView.setOnShapeChangedListener(this)

        binding.etType.setAdapter(PenTypesAdapter(this, options))
        binding.etType.onItemClickListener = this
        binding.etType.setText(options.first().text, false)

        binding.colorView.setOnClickListener(this)
        binding.strokeWidthSlider.addOnChangeListener(this)
    }

    override fun onClick(p0: View?) {
        ColorPickerDialog.Builder(this)
            .setTitle("ColorPicker Dialog")
            .setPreferenceName("MyColorPickerDialog")
            .setPositiveButton(getString(android.R.string.ok), this)
            .attachAlphaSlideBar(true) // the default value is true.
            .attachBrightnessSlideBar(true) // the default value is true.
            .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        this.menu = menu!!
        menu.findItem(R.id.undo).isEnabled = false
        menu.findItem(R.id.redo).isEnabled = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.undo -> binding.shapesView.undo()
            R.id.redo -> binding.shapesView.redo()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStackSizesChanged(addedShapesSize: Int, removedShapesSize: Int) {
        menu.findItem(R.id.undo).isEnabled = addedShapesSize > 0
        menu.findItem(R.id.redo).isEnabled = removedShapesSize > 0
    }

    override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
        if (envelope == null) return
        binding.colorView.setBackgroundColor(envelope.color)
        binding.shapesView.color = envelope.color
    }

    override fun onItemClick(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
        val option = options[position]
        binding.etType.setText(option.text, false)
        binding.shapesView.geometryType = option.geometryType
    }

    override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
        binding.shapesView.strokeWidth = value
    }
}