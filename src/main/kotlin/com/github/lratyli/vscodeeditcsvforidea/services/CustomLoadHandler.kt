package com.github.lratyli.vscodeeditcsvforidea.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.network.CefRequest

class CustomLoadHandler(virtualFilePath: String): CefLoadHandlerAdapter(), CefLoadHandler {
    private val filePath = virtualFilePath
    override fun onLoadingStateChange(p0: CefBrowser?, p1: Boolean, p2: Boolean, p3: Boolean) {
        //do nothing
    }

    override fun onLoadStart(p0: CefBrowser?, p1: CefFrame?, p2: CefRequest.TransitionType?) {

    }

    override fun onLoadEnd(browser: CefBrowser, frame: CefFrame, httpStatusCode: Int) {
        CoroutineScope(Dispatchers.Default).launch {
            //cross field?
            println(filePath)
            browser.executeJavaScript("fetch('http://tsv-viewer/${filePath}')\n" +
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
                    "  });",null,0)
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
        }
    }

    override fun onLoadError(
        p0: CefBrowser?,
        p1: CefFrame?,
        p2: CefLoadHandler.ErrorCode?,
        p3: String?,
        p4: String?
    ) {
    }
}