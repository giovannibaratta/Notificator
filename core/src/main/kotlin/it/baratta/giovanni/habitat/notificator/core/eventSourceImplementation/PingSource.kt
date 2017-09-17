package it.baratta.giovanni.habitat.notificator.core.eventSourceImplementation

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import it.baratta.giovanni.habitat.notificator.api.IEventSource
import it.baratta.giovanni.habitat.notificator.api.Message
import it.baratta.giovanni.habitat.notificator.api.request.ConfigurationParams
import org.apache.logging.log4j.LogManager
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.Semaphore

class PingSource private constructor(): IEventSource {

    private val emitter : PublishSubject<Message> = PublishSubject.create()
    override val sourceName: String = "pingsource"
    private val timeEmitter : TimeEmitter
    private val registerClient = HashSet<String>()

    init {
        timeEmitter = TimeEmitter(this, emitter, 5000L)
        timeEmitter.start()
    }

    override fun registerClient(clientToken: String, params: ConfigurationParams): Observable<Message> {
        timeEmitter.resumeEmission()
        registerClient.add(clientToken)
        return emitter.hide()
    }

    override fun unregisterClient(clientToken: String) {
        registerClient.remove(clientToken)
        if(registerClient.size <= 0)
            timeEmitter.pauseEmission()
    }

    override fun shutdown(){
        registerClient.clear()
        timeEmitter.shutdown()
    }

    companion object {
        val instance = PingSource()
        private val logger = LogManager.getLogger(PingSource::class.java)
    }

    private class TimeEmitter(private val source : IEventSource,
                              private val emitter : PublishSubject<Message>,
                              private val timeout : Long,
                              start : Boolean = false) : Thread(){

        private val lock = Semaphore(0)
        private var end = false
        private var pause = true
        private val dateTimeFormat = DateTimeFormatter.ISO_LOCAL_DATE_TIME

        fun init(){
            resumeEmission()
        }

        override fun run() {
            while(!end){
                if(pause)
                    lock.acquire()
                val currentTime = LocalDateTime.now().atZone(ZoneOffset.UTC)
                if(!end) {
                    emitter.onNext(Message.build(source, currentTime.toInstant().toEpochMilli(), dateTimeFormat.format(currentTime)))
                    Thread.sleep(timeout)
                }else
                    emitter.onComplete()
            }
        }

        fun pauseEmission(){
            if(end)
                throw IllegalStateException("Hai chiesto di terminare")
            pause = true
        }

        fun resumeEmission(){
            if(end)
                throw IllegalStateException("Hai chiesto di terminare")
            pause = false
            if(lock.availablePermits() == 0)
                lock.release()
        }

        fun shutdown(){
            end = true
            lock.release()
        }
    }
}