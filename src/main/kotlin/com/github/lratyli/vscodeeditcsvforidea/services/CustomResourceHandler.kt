package com.github.lratyli.vscodeeditcsvforidea.services


import org.cef.callback.CefCallback
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefResourceHandler
import org.cef.misc.IntRef
import org.cef.misc.StringRef
import org.cef.network.CefRequest
import org.cef.network.CefResponse
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLConnection

class CustomResourceHandler : CefResourceHandler {
    private var state: ResourceHandlerState = ClosedConnection
    //TODO gather match pattern
    val regex = Regex(""".*\.(tsv|csv)$""", RegexOption.IGNORE_CASE)
    override fun processRequest(
        cefRequest: CefRequest,
        cefCallback: CefCallback
    ): Boolean {
        val urlOption = cefRequest.url
        return if (urlOption != null) {
            val replacement = if (regex.containsMatchIn(urlOption)) "" else "vscode-edit-csv"
            val processedUrl = urlOption.replace("http://tsv-viewer", replacement)

            val newUrl: URL? = if (regex.containsMatchIn(urlOption)) {
                URL("file://$processedUrl")
            } else {
                this::class.java.classLoader.getResource(processedUrl)
            }
            if (newUrl != null) {
                state = OpenedConnection(newUrl.openConnection())
            }
            cefCallback.Continue()
            true
        } else {
            false
        }
    }


    override fun getResponseHeaders(cefResponse: CefResponse?, p1: IntRef?, p2: StringRef?) {
        if(cefResponse != null && p1 != null && p2!= null){
            state.getResponseHeaders(cefResponse, p1, p2)
        }
    }

    override fun readResponse(
        dataOut: ByteArray,
        designedBytesToRead: Int,
        bytesRead: IntRef,
        callback: CefCallback
    ): Boolean {
        return state.readResponse(dataOut, designedBytesToRead, bytesRead, callback)
    }

    override fun cancel() {
        state.close()
        state = ClosedConnection
    }
}

sealed interface ResourceHandlerState {
    fun getResponseHeaders(
        cefResponse: CefResponse,
        responseLength: IntRef,
        redirectUrl: StringRef
    )

    fun readResponse(
        dataOut: ByteArray,
        designedBytesToRead: Int,
        bytesRead: IntRef,
        callback: CefCallback
    ): Boolean

    fun close() {}
}


data class OpenedConnection(val connection: URLConnection) : ResourceHandlerState {
    val regex = Regex(""".*\.(tsv|csv)$""", RegexOption.IGNORE_CASE)
    private val inputStream: InputStream by lazy {
        if(regex.containsMatchIn(connection.url.toString())){
            File(connection.url.file).inputStream()
        }
        connection.inputStream }

    override fun getResponseHeaders(
        cefResponse: CefResponse,
        responseLength: IntRef,
        redirectUrl: StringRef
    ) {
        try {
            val url = connection.url.toString()
            when {
                url.contains(".css") -> cefResponse.mimeType = "text/css"
                url.contains(".js") -> cefResponse.mimeType = "text/javascript"
                url.contains(".html") -> cefResponse.mimeType = "text/html"
                url.contains(".tsv") -> cefResponse.mimeType = "text/tsv"
                else -> cefResponse.mimeType = connection.contentType // since 2021.1 all mime type must be set here, by hand
            }
            responseLength.set(inputStream.available())
            cefResponse.status = 200
        } catch (e: IOException) {
            cefResponse.error = CefLoadHandler.ErrorCode.ERR_FILE_NOT_FOUND
            cefResponse.statusText = e.localizedMessage
            cefResponse.status = 404
        }
    }

    override fun readResponse(
        dataOut: ByteArray,
        designedBytesToRead: Int,
        bytesRead: IntRef,
        callback: CefCallback
    ): Boolean {

        val availableSize = inputStream.available()
        return if (availableSize > 0) {
            val maxBytesToRead = minOf(availableSize, designedBytesToRead)
            val realNumberOfReadBytes = inputStream.read(dataOut, 0, maxBytesToRead)
            bytesRead.set(realNumberOfReadBytes)
            true
        } else {
            inputStream.close()
            false
        }
    }

    override fun close() {
        inputStream.close()
    }
}



object ClosedConnection : ResourceHandlerState {
    override fun getResponseHeaders(
        cefResponse: CefResponse,
        responseLength: IntRef,
        redirectUrl: StringRef
    ) {
        cefResponse.status = 404
    }

    override fun readResponse(
        dataOut: ByteArray,
        designedBytesToRead: Int,
        bytesRead: IntRef,
        callback: CefCallback
    ): Boolean {
        return false
    }
}



