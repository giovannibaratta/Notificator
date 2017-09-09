package it.baratta.giovanni.habitat.notificator.core.eventSourceImplementation

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import it.baratta.giovanni.habitat.notificator.api.ConfigurationParams
import it.baratta.giovanni.habitat.notificator.api.IEventSource
import org.apache.logging.log4j.LogManager
import java.io.Serializable
import java.util.*
import kotlin.concurrent.thread

class MockSource private constructor(): IEventSource{

    private val subscribedClient = HashMap<String, Subject<Serializable>>()

    override fun registerClient(clientToken: String, params: ConfigurationParams): Observable<Serializable> {
        if(subscribedClient.containsKey(clientToken))
            throw IllegalStateException("Il cliente è già registrato")
        val subject = PublishSubject.create<Serializable>()

        val temp = Thread {
            Thread.sleep(1000)
            for(i in 0.until(10)){
                logger.debug("Messaggio generato ${i} - ${clientToken}")
                subject.onNext("Message ${i}")
                Thread.sleep(Random().nextInt(500)+500L)
            }
        }

        temp .start()

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
    }
}