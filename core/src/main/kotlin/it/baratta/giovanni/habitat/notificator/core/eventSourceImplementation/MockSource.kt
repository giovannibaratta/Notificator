package it.baratta.giovanni.habitat.notificator.core.eventSourceImplementation

import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import it.baratta.giovanni.habitat.notificator.api.request.ConfigurationParams
import it.baratta.giovanni.habitat.notificator.api.IEventSource
import it.baratta.giovanni.habitat.notificator.api.InitializationException
import it.baratta.giovanni.habitat.notificator.api.Message
import org.apache.logging.log4j.LogManager
import java.util.*

class MockSource private constructor(): IEventSource {

    private val subscribedClient = HashMap<String, Subject<Message>>()
    private var messageCounter = 0

    override val sourceName: String = "mock"

    override fun registerClient(clientToken: String, params: ConfigurationParams): Observable<Message> {
        if(subscribedClient.containsKey(clientToken))
            throw InitializationException("Il cliente è già registrato")
        val subject = PublishSubject.create<Message>()

        val temp = Thread {
            val className = String::class.qualifiedName
            if(className == null)
                throw IllegalStateException("String non ha un nome")

            Thread.sleep(1000)
            for(i in 0.until(10)){
                logger.debug("Messaggio generato ${i} - ${clientToken}")
                subject.onNext(Message(messageCounter, sourceName,
                                        gson.toJson("Message ${messageCounter++}"), className))
                Thread.sleep(Random().nextInt(500)+500L)
            }
            subject.onComplete()
        }

        temp.start()

        subscribedClient.put(clientToken, subject)
        return subject.hide()
    }

    override fun unregisterClient(clientToken: String) {
        subscribedClient[clientToken]?.onComplete()
        subscribedClient.remove(clientToken)
    }

    companion object {
        val instance = MockSource()
        private val logger = LogManager.getLogger(MockSource::class.java)
        private val gson = Gson()
    }
}