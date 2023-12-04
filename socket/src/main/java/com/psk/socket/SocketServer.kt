package com.psk.socket

import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.nio.ByteBuffer

class SocketServer(address: InetSocketAddress, private val listener: SocketListener? = null) : WebSocketServer(address) {
    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake) {
        conn?.send("Welcome to the server!") //This method sends a message to the new client
        broadcast("new connection: " + handshake.resourceDescriptor) //This method sends a message to all clients connected
        println("new connection to " + conn?.remoteSocketAddress)
        listener?.onOpen(conn?.remoteSocketAddress?.address?.hostAddress)
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String, remote: Boolean) {
        println("closed " + conn?.remoteSocketAddress + " with exit code " + code + " additional info: " + reason)
        listener?.onClose(code, reason)
    }

    override fun onMessage(conn: WebSocket?, message: String) {
        println("received message from " + conn?.remoteSocketAddress + ": " + message)
    }

    override fun onMessage(conn: WebSocket?, message: ByteBuffer?) {
        println("received ByteBuffer from " + conn?.remoteSocketAddress)
        message ?: return
        listener?.onMessage(message)
    }

    override fun onError(conn: WebSocket?, ex: Exception) {
        System.err.println("an error occurred on connection " + conn?.remoteSocketAddress + ":" + ex)
        listener?.onError(ex)
    }

    override fun onStart() {
        println("server started successfully")
    }

}