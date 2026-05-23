package com.teclo.keyboard.ime

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import com.teclo.keyboard.ui.TecloKeyboardView

class TecloIME : InputMethodService() {

    private lateinit var keyboardView: TecloKeyboardView

    override fun onCreateInputView(): View {
        keyboardView = TecloKeyboardView(this)
        return keyboardView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
    }

    fun commitText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    fun deleteChar() {
        currentInputConnection?.deleteSurroundingText(1, 0)
    }
}
