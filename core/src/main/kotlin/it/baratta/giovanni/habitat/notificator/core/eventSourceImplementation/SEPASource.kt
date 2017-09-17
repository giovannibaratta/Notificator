package it.baratta.giovanni.habitat.notificator.core.eventSourceImplementation

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import it.baratta.giovanni.habitat.notificator.api.IEventSource
import it.baratta.giovanni.habitat.notificator.api.Message
import it.baratta.giovanni.habitat.notificator.api.request.ConfigurationParams
import org.apache.logging.log4j.LogManager
import org.glassfish.tyrus.client.ClientManager
import java.net.URI
import javax.websocket.*

class SEPASource private constructor(): IEventSource {

    companion object {
        val instance = SEPASource()
        private val logger = LogManager.getLogger(SEPASource::class.java)
    }

    override val sourceName: String = "sepa"

    private val registeredClient = HashMap<String, Pair<SepaConnectionHolder,PublishSubject<Message>>>()

    override fun registerClient(clientToken: String, params: ConfigurationParams): Observable<Message> {
        if(registeredClient.containsKey(clientToken))
            throw IllegalStateException("Il token $clientToken è già registrato")

        val server = params.getParam("serverURI")
        if(server == null)
            throw IllegalStateException("nei parametri non è presente il server")

        val emitter = PublishSubject.create<Message>()
        val thread = SepaConnectionHolder(this, server, emitter)

        registeredClient.put(clientToken, Pair(thread, emitter))
        thread.start()
        return emitter.hide()
    }

    override fun unregisterClient(clientToken: String) {
        registeredClient[clientToken]?.first?.closeConnection()
        registeredClient[clientToken]?.second?.onComplete()
        registeredClient.remove(clientToken)
    }

    override fun shutdown() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @ClientEndpoint
    private class SepaConnectionHolder(private val source: IEventSource,
                                       server : String,
                                       private val emitter: PublishSubject<Message>) : Thread(){

        private val clientSession: Session
        private var messageCount = 0L

        init {
            logger.info("Tento l'apertura")
            // apro la web socket
            //clientSession = ClientManager.createClient().connectToServer(SepaConnectionHolder::class.java, URI(server))
            //clientSession = ClientManager.createClient().connectToServer(this, URI(server))
            val config : ClientEndpointConfig = ClientEndpointConfig.Builder.create().build()
            val endpoint = SEPAEndpoint(source, emitter)
            clientSession = ClientManager.createClient().connectToServer(endpoint,config,URI(server))
            logger.info("Open ? ${clientSession.isOpen}")
        }

        override fun run() {

            Thread.sleep(1000000L)
            // resto in attesa dei messaggi

            // chiudo la websocket
        }

        fun closeConnection()
            = clientSession.close()

    }
}

@ClientEndpoint
class SEPAEndpoint(private val source : IEventSource,
                           private val emitter : PublishSubject<Message>) : Endpoint(), MessageHandler.Whole<String>{

    private var messageCount = 0L

    @OnOpen
    override fun onOpen(session: Session?, config: EndpointConfig?) {
        if(session == null)
            return
        logger.info("Connesione apert ${session}")
        // creo la registrazione
        session.basicRemote.sendText(""" {"subscribe" : "select * where {?s ?p ?o}",
"authorization" : "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ",
"alias" : "All"} """)
        session.addMessageHandler(this)
    }

    override fun onMessage(message: String?) {
        logger.info("Messaggio dalla web socket , ${message}")
        emitter.onNext(Message.build(source,messageCount++,"Arrivato un messaggio dalla sepa"))
    }

    @OnClose
    private fun onCloseConnection(session: Session, closeReason: CloseReason){
    }

    companion object {
        private val logger = LogManager.getLogger(SEPAEndpoint)
    }
}