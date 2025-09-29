package org.example.project

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * JS (Browser): IndexedDB-basiert, Fallback auf localStorage.
 */
actual class WebFileHandler : FileHandler {
    private val dbName = "openstash-db"
    private val storeName = "kv"
    private val key = "openstash.json"

    private val win: dynamic get() = js("window")

    private suspend fun openDb(): dynamic = suspendCancellableCoroutine { cont ->
        val idb = win.indexedDB
        if (idb == null || js("typeof idb === 'undefined'") as Boolean) {
            cont.resumeWithException(IllegalStateException("IndexedDB not supported"))
            return@suspendCancellableCoroutine
        }
        val req = idb.open(dbName, 1)
        req.onupgradeneeded = { ev: dynamic ->
            val db = ev.target.result
            if (!db.objectStoreNames.contains(storeName)) {
                db.createObjectStore(storeName)
            }
        }
        req.onsuccess = { cont.resume(req.result) }
        req.onerror = {
            val msg = (req.error?.message as? String) ?: "Failed to open IndexedDB"
            cont.resumeWithException(IllegalStateException(msg))
        }
    }

    override suspend fun readFile(): String {
        val db = try { openDb() } catch (_: Throwable) {
            return (win.localStorage?.getItem(key) as String?) ?: ""
        }
        return suspendCancellableCoroutine { cont ->
            val tx = db.transaction(storeName, "readonly")
            val store = tx.objectStore(storeName)
            val getReq = store.get(key)
            getReq.onsuccess = {
                cont.resume(getReq.result as? String ?: "")
            }
            getReq.onerror = {
                val msg = (getReq.error?.message as? String) ?: "IndexedDB get error"
                cont.resumeWithException(IllegalStateException(msg))
            }
        }
    }

    override suspend fun writeFile(content: String) {
        val db = try { openDb() } catch (_: Throwable) {
            win.localStorage?.setItem(key, content)
            return
        }
        suspendCancellableCoroutine<Unit> { cont ->
            val tx = db.transaction(storeName, "readwrite")
            val store = tx.objectStore(storeName)
            val putReq = store.put(content, key)
            putReq.onsuccess = { cont.resume(Unit) }
            putReq.onerror = {
                val msg = (putReq.error?.message as? String) ?: "IndexedDB put error"
                cont.resumeWithException(IllegalStateException(msg))
            }
        }
    }
}