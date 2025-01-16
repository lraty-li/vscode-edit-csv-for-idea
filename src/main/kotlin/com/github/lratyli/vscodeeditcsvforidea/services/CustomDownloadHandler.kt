package com.github.lratyli.vscodeeditcsvforidea.services

import org.cef.browser.CefBrowser
import org.cef.callback.CefBeforeDownloadCallback
import org.cef.callback.CefDownloadItem
import org.cef.callback.CefDownloadItemCallback
import org.cef.handler.CefDownloadHandlerAdapter

class CustomDownloadHandler(virtualFilePath: String) : CefDownloadHandlerAdapter() {
    private val filePath = virtualFilePath
    override fun onBeforeDownload(
            browser: CefBrowser?,
            downloadItem: CefDownloadItem?,
            suggestedName: String?,
            callback: CefBeforeDownloadCallback?
    ) {
        callback?.Continue(filePath, true)
        println("File will be saved to: $filePath")
    }

    override fun onDownloadUpdated(
            browser: CefBrowser?,
            downloadItem: CefDownloadItem?,
            callback: CefDownloadItemCallback?
    ) {
        if (downloadItem != null) {}
    }
}
