package com.github.lratyli.vscodeeditcsvforidea.viewer.ui.editor

import com.github.lratyli.vscodeeditcsvforidea.services.*
import com.intellij.diff.util.FileEditorBase
import com.intellij.openapi.actionSystem.ex.AnActionListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.ui.jcef.*
import javax.swing.JComponent
import org.cef.CefApp
import org.cef.handler.CefLoadHandler

// TODO: Implement state persistence
class TsvFileEditor(project: Project, private val virtualFile: VirtualFile) :
        FileEditorBase(), DumbAware {
    private val messageBusConnection = project.messageBus.connect()
    private val fileChangedListener = FileChangedListener(true)
    private val myBrowser: JBCefBrowser = JBCefBrowserBuilder().setClient(ourCefClient).build()
    private val viewComponent = myBrowser.component
    private val myLoadHandler: CefLoadHandler
    // private val myDownloadHandler: CefDownloadHandler
    companion object {
        private val logger = logger<TsvFileEditor>()
        private const val NAME = "TsvEditor"

        private const val HOST_NAME = "tsv-viewer"
        private const val PROTOCOL = "http"
        private const val VIEWER_PATH = "/csvEditorHtml/browser/indexBrowser.html"
        private const val VIEWER_URL = "$PROTOCOL://$HOST_NAME$VIEWER_PATH"

        private val ourCefClient = JBCefApp.getInstance().createClient()

        init {
            Disposer.register(ApplicationManager.getApplication(), ourCefClient)
        }
    }

    init {
        Disposer.register(this, messageBusConnection)
        myBrowser.loadURL(VIEWER_URL)
        messageBusConnection.subscribe(VirtualFileManager.VFS_CHANGES, fileChangedListener)
        messageBusConnection.subscribe(
                AnActionListener.TOPIC,
                CustomActionListener(
                        onSave = {
                            myBrowser.cefBrowser.executeJavaScript(
                                    "saveToIdeaFile(String(getDataAsCsv(defaultCsvReadOptions, defaultCsvWriteOptions)))",
                                    null,
                                    0
                            )
                        }
                )
        )

        registerAppSchemeHandler()
        // Set the property JBCefClient.Properties.JS_QUERY_POOL_SIZE to use JBCefJSQuery after the
        // browser has been created
        myBrowser.jbCefClient.setProperty(JBCefClient.Properties.JS_QUERY_POOL_SIZE, 10)
        myLoadHandler = CustomLoadHandler(virtualFile, myBrowser)
        // myDownloadHandler = CustomDownloadHandler(virtualFile.path)
        ourCefClient.addLoadHandler(myLoadHandler, myBrowser.cefBrowser)
        // ourCefClient.addDownloadHandler(myDownloadHandler, myBrowser.cefBrowser)
        myBrowser.cefBrowser.setFocus(true);
    }

    override fun getName(): String = NAME

    override fun getFile(): VirtualFile = virtualFile

    override fun getComponent(): JComponent = viewComponent

    override fun getPreferredFocusedComponent(): JComponent = viewComponent

    private inner class FileChangedListener(var isEnabled: Boolean = true) : BulkFileListener {
        override fun after(events: MutableList<out VFileEvent>) {
            if (!isEnabled) {
                return
            }
            // TODO reload file if changed
            //            if (events.any { it.file == virtualFile }) {
            //                logger.debug("Target file ${virtualFile.path} changed. Reloading
            // current view.")
            //            }
        }
    }

    override fun dispose() {
        ourCefClient.removeLoadHandler(myLoadHandler, myBrowser.cefBrowser)
        super.dispose()
    }

    private fun registerAppSchemeHandler() {
        CefApp.getInstance()
                .registerSchemeHandlerFactory(PROTOCOL, HOST_NAME, CustomSchemeHandlerFactory())
    }
}
