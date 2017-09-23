package it.baratta.giovanni.habitat.notificator.core.eventSourceImplementation

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import it.baratta.giovanni.habitat.notificator.api.IEventSource
import it.baratta.giovanni.habitat.notificator.api.Message
import it.baratta.giovanni.habitat.notificator.api.request.ConfigurationParams
import it.unibo.arces.wot.sepa.api.INotificationHandler
import it.unibo.arces.wot.sepa.api.SPARQL11SEProperties
import it.unibo.arces.wot.sepa.api.SPARQL11SEProtocol
import it.unibo.arces.wot.sepa.commons.request.SubscribeRequest
import it.unibo.arces.wot.sepa.commons.request.UnsubscribeRequest
import it.unibo.arces.wot.sepa.commons.request.UpdateRequest
import it.unibo.arces.wot.sepa.commons.response.*
import org.apache.logging.log4j.LogManager
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.net.SocketException
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import javax.xml.bind.DatatypeConverter
import kotlin.collections.HashMap


class SEPASource private constructor(): IEventSource {

    companion object {
        val instance = SEPASource()
        private val logger = LogManager.getLogger(SEPASource::class.java)
    }

    override val sourceName: String = "sepa"

    private val lock = ReentrantLock()

    private val jaspConnection = HashMap<SPARQL11SEProperties, Pair<SepaConnectionHolder, Int>>()
    private val tokenConnection = HashMap<String, SepaConnectionHolder>()
    private val tokenJasp = HashMap<String, SPARQL11SEProperties>()

    override fun registerClient(clientToken: String, params: ConfigurationParams): Observable<Message> {
        logger.debug("Cliente in arrivo nella SEPA")
        // verifico che nei parametri sia il file jasp di configurazione e il file ssl serializzato
        val jasp = params.getParam("jasp")
        if(jasp == null)
            throw IllegalStateException("nei parametri non è presente il jasp")

        val sslKey = params.getParam("sslKey")
        if(sslKey == null)
            throw IllegalStateException("nei parametri non è presente la sslKey")

        val query = params.getParam("query")
        if(query == null)
            throw IllegalStateException("nei parametri non è presente la query")

        val tempJasp : File
        try {
            tempJasp = File.createTempFile(clientToken,"jasp")
            val fileWriter = FileWriter(tempJasp)
            fileWriter.write(jasp)
            fileWriter.close()
        }catch (exception : Exception){
            throw IllegalStateException("Non è stato possibile creare un file temporaneo")
        }

        val sparqlProp : SPARQL11SEProperties
        try {
            sparqlProp = SPARQL11SEProperties(tempJasp.absolutePath)
        }catch (exception : Exception){
            throw IllegalStateException("Il file jasp fornito non è valido.}",exception)
        }

        try{
            tempJasp.delete()
        }catch (exception : Exception){

        }

        val jksFile : File

        val temp = File("C:\\Users\\Gio\\Desktop\\SEPA\\SEPADocs-master\\prova.txt")
        try {
            logger.debug("DATA")
            val data = DatatypeConverter.parseBase64Binary(sslKey)
            jksFile = File.createTempFile(clientToken,".jks")
            val writer = DataOutputStream(FileOutputStream(jksFile))
            writer.write(DatatypeConverter.parseBase64Binary(sslKey))
            writer.close()
        }catch (exception : Exception){
            throw IllegalStateException("Il file sslkey fornito non è valido",exception)
        }

        val emitter = PublishSubject.create<Message>()
        val connHolder : SepaConnectionHolder
        try {
            connHolder = SepaConnectionHolder(this,sparqlProp,jksFile.absolutePath,emitter)
        }catch (exception : Exception){
            throw IllegalStateException("non è stato possibile stabilire una connessione con il server.",exception)
        }

        lock.lock()

        if(tokenConnection.contains(clientToken))
            throw IllegalArgumentException("Il token è già registrato")

        // verifico se esiste un file di configurazione compatibile con le richieste del cliente
        when(jaspConnection.containsKey(sparqlProp)) {
            true ->
                // elimino il mio handler perchè uno è già pronto
                connHolder.closeConnection()
            false ->
                // nessun jasp comptabile, creo un nuovo connection handler
                jaspConnection.put(sparqlProp, Pair(connHolder, 0))
        }

        try{
            connHolder.subscribe(clientToken, query)
        }catch (exception : Exception){
            throw IllegalStateException("non è stato possibible registrare la query",exception)
        }

        val prev = jaspConnection.getOrElse(sparqlProp,{throw IllegalStateException("jaspConnection non presente")})
        jaspConnection[sparqlProp] = Pair(prev.first,prev.second + 1)
        tokenJasp[clientToken] = sparqlProp
        tokenConnection[clientToken] = connHolder
        lock.unlock()
        return emitter.hide()
    }

    override fun unregisterClient(clientToken: String) {
        lock.lock()
        val conn = tokenConnection[clientToken]
        if(conn == null)
            throw IllegalArgumentException("il token non è registartoo")
        conn.unsubscribe(clientToken)

        val clientJasp = tokenJasp.getOrElse(clientToken,
                                                {throw IllegalStateException("client token non presente")})

        var connCount = jaspConnection.getOrElse(clientJasp
                                                ,{throw IllegalStateException("jasp non presente")})
                                        .second
        if(--connCount == 0){
            conn.closeConnection()
            jaspConnection.remove(clientJasp)
        }else{
            jaspConnection[clientJasp] = Pair(conn, connCount)
        }
        tokenJasp.remove(clientToken)
        tokenConnection.remove(clientToken)
        // elimina file jks
        lock.unlock()
    }

    override fun shutdown() {
        lock.lock()
        tokenConnection.forEach{
            it.component2().closeConnection()
        }
        tokenConnection.clear()
        jaspConnection.clear()
        tokenJasp.clear()
        lock.unlock()
    }

    private class SepaConnectionHolder(source: IEventSource,
                                       sparqL11SEProperties: SPARQL11SEProperties,
                                       jksFile : String,
                                       emitter: PublishSubject<Message>){


        private val sepaProtocol : SPARQL11SEProtocol
        private var timer : Timer = Timer()
        private val subscription = HashMap<String, SubscribeResponse>()

        init {
            logger.debug("INIZIO CONFIGURAZIONE INIT")
            sepaProtocol = SPARQL11SEProtocol(sparqL11SEProperties,
                                                NotificationHandler(source, emitter),
                                                jksFile,
                                                "sepa2017",
                                                "sepa2017")

            val response = sepaProtocol.register("SEPATest")
            when(response){
                is RegistrationResponse -> tokenRequest()
                is ErrorResponse -> throw IllegalStateException("Errore durante la registrazion. ${response.errorCode} - ${response.errorMessage}")
                else -> throw IllegalStateException("Risposta registration non valida")
            }

            logger.debug("REG -> ${response.asJsonObject}")
            val responseToken = sepaProtocol.requestToken()
            logger.debug("TOKEN -> ${responseToken.asJsonObject}")
        }

        private fun tokenRequest(){
            timer.cancel()
            val response = sepaProtocol.requestToken()
            when(response){
                is JWTResponse -> {
                    // scheduling per il rinnovo del token
                    val timerTask = object : TimerTask(){
                        override fun run() {
                            tokenRequest()
                        }
                    }
                    timer = Timer()
                    timer.schedule(timerTask, response.expiresIn)
                }
                is ErrorResponse -> {
                    if(response.errorMessage.compareTo("Token is not expired") != 0)
                        throw IllegalStateException("Errore durante la richiesta del token. ${response.errorCode} - ${response.errorMessage}")
                    else{
                        // scheduling per il rinnovo del token
                        val timerTask = object : TimerTask(){
                            override fun run() {
                                tokenRequest()
                            }
                        }
                        timer = Timer()
                        timer.schedule(timerTask, 100000L)
                    }
                }
                else -> throw IllegalStateException("Riposta token non valida")
            }
        }

        @Synchronized
        fun subscribe(clientToken: String, query : String){
            if(subscription.containsKey(clientToken))
                throw IllegalStateException("Il cliente possiede già una query registrata")
            val response = sepaProtocol.secureSubscribe(SubscribeRequest(query,clientToken))
            when(response){
                is SubscribeResponse -> {
                    logger.debug("Subscribe avvenuto correttamente")
                    subscription.put(clientToken,response)

                    timer.scheduleAtFixedRate(object : TimerTask(){
                        override fun run() {
                            logger.debug("Invio UPDATE")
                            val response = sepaProtocol.update(UpdateRequest(""" INSERT DATA { <http://myBookDomain${Random().nextInt()}.it> foaf:name "MyValue" } """))
                            logger.debug("FINE UPDATE")
                            when(response){
                                is UpdateResponse -> logger.debug("UPDATE successo ${response} ")
                                is ErrorResponse -> logger.debug("UPDATE ERRORE ${response.errorCode} - ${response.errorMessage}")
                                else -> logger.debug("RIsposta non supportata")
                            }
                        }
                    },2000L,20000L)
                }
                is ErrorResponse -> throw IllegalStateException("Errore nella sottoscrizione. ${response.errorCode} - ${response.errorMessage}")
                else -> throw IllegalStateException("Risposta non valida")
            }
        }

        @Synchronized
        fun unsubscribe(clientToken: String){
            val sub = subscription[clientToken]
            if(sub == null)
                return
            sepaProtocol.secureUnsubscribe(UnsubscribeRequest(sub.token, sub.spuid))
            subscription.remove(clientToken)
        }

        @Synchronized
        fun closeConnection(){
            timer.cancel()
            subscription.forEach{ unsubscribe(it.key) }
        }
    }
}

class NotificationHandler(private val source : IEventSource,
                 private val emitter : PublishSubject<Message>) : INotificationHandler{

    override fun onSemanticEvent(notify: Notification?) {
        if(notify == null)
            return
        emitter.onNext(Message.build(source, notify.sequence.toLong(),"SemanticEvent"))
    }

    override fun onPing() {
        emitter.onNext(Message.build(source, Random().nextLong(),"PingEvent"))
    }

    override fun onBrokenSocket() {
        emitter.onError(SocketException("Broken sepa socket"))
    }

    override fun onError(errorResponse: ErrorResponse?) {
    }
}