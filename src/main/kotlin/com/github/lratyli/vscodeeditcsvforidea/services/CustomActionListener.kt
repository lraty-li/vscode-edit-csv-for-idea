package com.github.lratyli.vscodeeditcsvforidea.services

import com.intellij.ide.actions.SaveAllAction
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.AnActionResult
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.ex.AnActionListener

class CustomActionListener(onSave: () -> Unit) : AnActionListener {
    private val onSave: () -> Unit = onSave
    override fun beforeActionPerformed(action: AnAction, event: AnActionEvent) {
        if (action is SaveAllAction) {
            onSave()
        }
    }

    override fun afterActionPerformed(
            action: AnAction,
            event: AnActionEvent,
            result: AnActionResult
    ) {}

    override fun beforeEditorTyping(c: Char, dataContext: DataContext) {}
}
