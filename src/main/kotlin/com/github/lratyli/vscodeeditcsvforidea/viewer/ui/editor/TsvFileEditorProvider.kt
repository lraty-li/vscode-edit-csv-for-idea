package com.github.lratyli.vscodeeditcsvforidea.viewer.ui.editor

import com.github.lratyli.vscodeeditcsvforidea.viewer.lang.TsvFileType
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.AsyncFileEditorProvider
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class TsvFileEditorProvider : AsyncFileEditorProvider, DumbAware {
  override fun getEditorTypeId() = "TSV"

  override fun accept(project: Project, file: VirtualFile): Boolean {
    logger.debug("check accept, file: $file")
    return file.fileType == TsvFileType;
  }

  override fun createEditor(project: Project, file: VirtualFile): FileEditor {
    return createEditorAsync(project, file).build()
  }

  override fun getPolicy() = FileEditorPolicy.HIDE_DEFAULT_EDITOR

  override fun createEditorAsync(project: Project, file: VirtualFile): AsyncFileEditorProvider.Builder {
    return object: AsyncFileEditorProvider.Builder() {
      override fun build(): FileEditor = TsvFileEditor(project, file)
    }
  }

  companion object {
    private val logger = logger<TsvFileEditorProvider>()
  }
}
