package it.baratta.giovanni.notificator.core

import it.baratta.giovanni.notificator.core.eventSourceImplementation.PingSource
import it.baratta.giovanni.notificator.core.eventSourceImplementation.SEPASource
import it.baratta.giovanni.notificator.core.network.tcp.RequestTCPThread
import it.baratta.giovanni.notificator.core.notificatorImplementation.FirebaseCloudMessagingNotificator
import it.baratta.giovanni.notificator.core.notificatorImplementation.MqttNotificator
import org.apache.logging.log4j.LogManager
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener

@WebListener
class WebServerInitializer : ServletContextListener {

    private var tcpThread : RequestTCPThread
            = RequestTCPThread(2000 /*Random().nextInt(50000)+1024*/)

    override fun contextInitialized(p0: ServletContextEvent?) {
        // Carico i moduli da supportare
        ServiceBinder.instance.bindEventSourceModule(PingSource.instance)
        ServiceBinder.instance.bindNotificatorModule(MqttNotificator.instance)
        ServiceBinder.instance.bindEventSourceModule(SEPASource.instance)
        ServiceBinder.instance.bindNotificatorModule(FirebaseCloudMessagingNotificator.instance)
        // Inizializzo la socket TCP
        tcpThread.setUncaughtExceptionHandler { t, e -> logger.error("Errore nella socket TCP") }
        // Avvio la socket
        tcpThread.start()
    }

    override fun contextDestroyed(p0: ServletContextEvent?) {
        tcpThread.shutdown()
        tcpThread.join()
    }

    companion object {
        private val logger = LogManager.getLogger(WebServerInitializer::class.java)
    }

}