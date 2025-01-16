package com.github.lratyli.vscodeeditcsvforidea.services

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.jcef.JBCefBrowserBase
import com.intellij.ui.jcef.JBCefJSQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.network.CefRequest

class CustomLoadHandler(virtualFile: VirtualFile, myBrowser: JBCefBrowserBase) :
        CefLoadHandlerAdapter(), CefLoadHandler {
    private val virtualFile = virtualFile
    private val myBrowser = myBrowser
    override fun onLoadingStateChange(p0: CefBrowser?, p1: Boolean, p2: Boolean, p3: Boolean) {
        // do nothing
    }

    override fun onLoadStart(p0: CefBrowser?, p1: CefFrame?, p2: CefRequest.TransitionType?) {}

    override fun onLoadEnd(browser: CefBrowser, frame: CefFrame, httpStatusCode: Int) {
        CoroutineScope(Dispatchers.Default).launch {
            // cross field?
            println(virtualFile.path)
            browser.executeJavaScript(
                    "fetch('http://tsv-viewer/${virtualFile.path}')\n" +
                            "  .then(response => {\n" +
                            "    if (!response.ok) {\n" +
                            "      throw new Error(`HTTP error! status: \${response.status}`);\n" +
                            "    }\n" +
                            "    return response.text();\n" +
                            "  })\n" +
                            "  .then(data => {\n" +
                            "    openCsvText(data)\n" +
                            "  })\n" +
                            "  .catch(error => {\n" +
                            "    console.error('Error fetching the file:', error);\n" +
                            "    openCsvText(`\${error}`)\n" +
                            "  });",
                    null,
                    0
            )
            /*
            fetch('http://tsv-viewer/file.tsv')
              .then(response => {
                if (!response.ok) {
                  throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.text();
              })
              .then(data => {
                openCsvText(data)
              })
              .catch(error => {
                console.error('Error fetching the file:', error);
                openCsvText(`${error}`)
              });
            */

            registerFileSaveHandler(browser)
        }
    }

    private fun registerFileSaveHandler(browser: CefBrowser) {
        // https://plugins.jetbrains.com/docs/intellij/jcef.html#executing-plugin-code-from-javascript
        val openLinkQuery = JBCefJSQuery.create(myBrowser)
        openLinkQuery.addHandler { content: String? ->
            CoroutineScope(Dispatchers.Default).launch {
                if(content== null) return@launch
                writeTextToDocument({ virtualFile }, content.toByteArray())
            }
            null
        }
        browser.executeJavaScript(
                "function saveToIdeaFile(content) {" +
                        openLinkQuery.inject("content") +
                        "};",
                null,
                0
        )
    }

    override fun onLoadError(
            p0: CefBrowser?,
            p1: CefFrame?,
            p2: CefLoadHandler.ErrorCode?,
            p3: String?,
            p4: String?
    ) {}
}
