package com.github.lratyli.vscodeeditcsvforidea.services

import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.readAndWriteAction
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findDocument
import java.io.IOException

// https://plugins.jetbrains.com/docs/intellij/jcef.html#executing-plugin-code-from-javascript
// invoked from js, save text into file opened

suspend fun writeBytesToFile(destination: () -> VirtualFile, byteArrayPayload: ByteArray) {
    readAndWriteAction {
        writeAction {
            val file = destination.invoke()
            try {
                file.getOutputStream(file).use { stream ->
                    with(stream) { write(byteArrayPayload) }
                }
            } catch (e: IOException) {} catch (e: IllegalArgumentException) {}
        }
    }
}

suspend fun writeTextToDocument(destination: () -> VirtualFile, bytes: ByteArray) {
    val file = destination()
    readAndWriteAction {
        writeAction {
            file.getOutputStream(file).use { stream ->
                with(stream) {
                    write(bytes)
                }
            }
        }
    }
}
