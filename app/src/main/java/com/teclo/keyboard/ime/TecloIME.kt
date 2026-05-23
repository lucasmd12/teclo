package com.teclo.keyboard.ime

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import com.teclo.keyboard.ui.TecloKeyboardView

class TecloIME : InputMethodService() {

    private lateinit var keyboardView: TecloKeyboardView

    override fun onCreateInputView(): View {
        keyboardView = TecloKeyboardView(this)
        keyboardView.layoutParams = android.widget.FrameLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            280.dpToPx(this)
        )
        return keyboardView
    }

    fun commitText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    fun deleteChar() {
        currentInputConnection?.deleteSurroundingText(1, 0)
    }

    private fun Int.dpToPx(context: android.content.Context): Int =
        (this * context.resources.displayMetrics.density).toInt()
}
