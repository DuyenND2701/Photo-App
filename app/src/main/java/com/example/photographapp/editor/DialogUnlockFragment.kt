package com.example.photographapp.editor

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.photographapp.R
import com.example.photographapp.databinding.FragmentDialogUnlockBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DialogUnlockFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DialogUnlockFragment(
    private val onWatchAds: () -> Unit,
    private val onBuyPremium: ()-> Unit
) : DialogFragment() {
    private var _binding: FragmentDialogUnlockBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentDialogUnlockBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
        binding.btnWatchAds.setOnClickListener {
            onWatchAds()
            dismiss()
        }
        binding.btnBuyPremium.setOnClickListener {
            onBuyPremium()
            dismiss()
        }
        binding.btnCancel.setOnClickListener{
            dismiss()
        }
        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}