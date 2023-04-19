package com.example.mobilepaint.ui.canvas

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.example.mobilepaint.databinding.DialogStrokeBinding

class ImageNameDialog : DialogFragment() {

    private lateinit var binding: DialogStrokeBinding

    private val args by navArgs<ImageNameDialogArgs>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogStrokeBinding.inflate(layoutInflater)
        binding.etType.inputType = InputType.TYPE_CLASS_TEXT
        binding.etType.setText(args.name)
        binding.tlType.hint = args.hint

        return AlertDialog.Builder(requireContext())
            .setTitle(args.title)
            .setView(binding.root)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                setFragmentResult(RESULT_KEY, bundleOf(NAME to binding.etType.text?.toString()))
            }
            .create()
    }

    companion object {
        const val RESULT_KEY = "ImageNameDialog"
        const val NAME = "NAME"
    }
}