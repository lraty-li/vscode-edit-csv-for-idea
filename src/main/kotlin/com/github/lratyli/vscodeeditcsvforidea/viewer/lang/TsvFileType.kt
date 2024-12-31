package com.github.lratyli.vscodeeditcsvforidea.viewer.lang

import com.github.lratyli.vscodeeditcsvforidea.viewer.icons.TsvViewerIcons
import com.intellij.openapi.fileTypes.LanguageFileType

object TsvFileType : LanguageFileType(TsvLanguage) {
  override fun getIcon() = TsvViewerIcons.TSV_FILE

  override fun getName() = "TSV"

  override fun getDefaultExtension() = "tsv"

  //TODO: Add proper description
  override fun getDescription() = "TSV"
}
