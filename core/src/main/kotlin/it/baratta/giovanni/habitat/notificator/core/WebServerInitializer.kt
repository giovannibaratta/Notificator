package it.baratta.giovanni.habitat.notificator.core

import it.baratta.giovanni.habitat.notificator.core.eventSourceImplementation.MockSourceAdapter
import it.baratta.giovanni.habitat.notificator.core.network.tcp.RequestTCPThread
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Logger
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener

@WebListener
class WebServerInitializer : ServletContextListener {

    private var tcpThread : RequestTCPThread
            = RequestTCPThread(2000 /*Random().nextInt(50000)+1024*/)

    override fun contextInitialized(p0: ServletContextEvent?) {
        // Carico i moduli da supportare
        NotificatorBinder.instance.bindEventSourceModule("mock",MockSourceAdapter::class)

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