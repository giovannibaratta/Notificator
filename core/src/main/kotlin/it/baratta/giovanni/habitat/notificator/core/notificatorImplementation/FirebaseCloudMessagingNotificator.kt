package it.baratta.giovanni.habitat.notificator.core.notificatorImplementation

import com.google.gson.Gson
import it.baratta.giovanni.habitat.notificator.api.INotificator
import it.baratta.giovanni.habitat.notificator.api.Message
import it.baratta.giovanni.habitat.notificator.api.request.ConfigurationParams
import org.apache.logging.log4j.LogManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class FirebaseCloudMessagingNotificator private constructor(): INotificator {

    companion object {
        private val logger= LogManager.getLogger(FirebaseCloudMessagingNotificator)
        val instance = FirebaseCloudMessagingNotificator()
        private val gson = Gson()
    }

    private val registeredClient = HashMap<String, Pair<String,FcmProxyService>>()

    override fun initNotifcator(clientToken: String, params: ConfigurationParams): Boolean {
        if(registeredClient.containsKey(clientToken))
            return false

        val fcmToken = params.getParam("fcmToken")
        if(fcmToken == null)
            return false

        val fcmProxy = params.getParam("fcmProxy")
        if(fcmProxy == null)
            return false

        val retrofit : Retrofit

        try {
            retrofit = Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(fcmProxy)
                    .build()
        }catch (exception : Exception){
            logger.error("Errore durante la creazione del retrofit per il token $clientToken con url $fcmProxy")
            return false
        }

        registeredClient.putIfAbsent(clientToken, Pair(fcmToken,retrofit.create(FcmProxyService::class.java)))
        return true
    }

    override fun destroyNotificator(clientToken: String) {
        registeredClient.remove(clientToken)
    }

    override fun notify(clientToken: String, message: Message) {
        val clientInfo = registeredClient[clientToken]
        if(clientInfo == null)
            return

        //val notificationInformation = gson.toJson(MessageWrapper(clientInfo.first, "\\ca\nio"))
        val notificationInformation = gson.toJson(MessageWrapper(clientInfo.first, message))
        logger.info("JSON -> ${notificationInformation}")
        val call = clientInfo.second.sendNotification("text/plain",notificationInformation)
        logger.info("TYPE -> ${call.request().header("Content-Type")}")
        try {
            call.execute()
        }catch (exception : Exception){

        }
    }

    override val notificatorName: String = "fcm"


    private data class MessageWrapper(val token : String,
                                      val data: Any)

}