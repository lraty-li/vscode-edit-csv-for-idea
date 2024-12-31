package com.github.lratyli.vscodeeditcsvforidea.viewer.ui.editor

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.diff.util.FileEditorBase
import com.intellij.ide.structureView.StructureViewBuilder
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefBrowserBuilder
import org.cef.handler.CefRequestHandler
import javax.swing.JComponent

// TODO: Implement state persistence
class TsvFileEditor(project: Project, private val virtualFile: VirtualFile) : FileEditorBase(), DumbAware {
    private val messageBusConnection = project.messageBus.connect()
    private val fileChangedListener = FileChangedListener(true)
    private val myBrowser: JBCefBrowser = JBCefBrowserBuilder().setClient(ourCefClient).build()
    val viewComponent = myBrowser.component


    companion object {
        private val logger = logger<TsvFileEditor>()
        private const val NAME = "TsvEditor"

        private const val HOST_NAME = "localhost"
        private const val PROTOCOL = "http"
        private const val VIEWER_PATH = "/index.html"
        private const val VIEWER_URL = "$PROTOCOL://$HOST_NAME$VIEWER_PATH"

        private val ourCefClient = JBCefApp.getInstance().createClient()

        init {
            Disposer.register(ApplicationManager.getApplication(), ourCefClient)
        }
    }

    init {
        //Disposer.register(this, viewComponent)
        Disposer.register(this, messageBusConnection)
        //ourCefClient.addRequestHandler(myRequestHandler, myBrowser.cefBrowser)
        myBrowser.loadURL(VIEWER_URL)

        messageBusConnection.subscribe(VirtualFileManager.VFS_CHANGES, fileChangedListener)

    }

    override fun getName(): String = NAME

    override fun getFile(): VirtualFile = virtualFile

    override fun getComponent(): JComponent = viewComponent

    override fun getPreferredFocusedComponent(): JComponent = myBrowser.cefBrowser.uiComponent as JComponent

    private inner class FileChangedListener(var isEnabled: Boolean = true) : BulkFileListener {
        override fun after(events: MutableList<out VFileEvent>) {
            if (!isEnabled) {
                return
            }
            //TODO reload file if changed
            if (events.any { it.file == virtualFile }) {
                logger.debug("Target file ${virtualFile.path} changed. Reloading current view.")

            }
        }
    }

    override fun dispose() {
        //ourCefClient.removeRequestHandler(myRequestHandler, myBrowser.cefBrowser)
        //ourCefClient.removeLoadHandler(myLoadHandler, myBrowser.cefBrowser)
        super.dispose()
    }



}
