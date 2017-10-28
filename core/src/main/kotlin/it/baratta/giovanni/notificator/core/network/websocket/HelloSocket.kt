package it.baratta.giovanni.notificator.core.network.websocket

import javax.websocket.OnClose
import javax.websocket.OnMessage
import javax.websocket.OnOpen
import javax.websocket.Session
import javax.websocket.server.ServerEndpoint


@ServerEndpoint("/ws")
class HelloSocket{

    @OnOpen
    fun open(session : Session){
        session.basicRemote.sendText("Hello")
    }

    @OnClose
    fun close(session : Session){
        session.basicRemote.sendText("Bye")
    }

    @OnMessage
    fun message(message: String, session: Session){
        session.basicRemote.sendText("HO ricevutoi l msg")
    }
}