package it.baratta.giovanni.notificator.core.network.rest

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import it.baratta.giovanni.notificator.api.exceptions.ClientNotFoundException
import it.baratta.giovanni.notificator.api.request.ModuleRequest
import it.baratta.giovanni.notificator.api.request.RegistrationRequest
import it.baratta.giovanni.notificator.api.response.DeregistrationResponse
import it.baratta.giovanni.notificator.api.response.ErrorResponse
import it.baratta.giovanni.notificator.api.response.RegistrationResponse
import it.baratta.giovanni.notificator.api.response.StatusResponse
import it.baratta.giovanni.notificator.core.ClientManager
import org.apache.logging.log4j.LogManager
import javax.ws.rs.*
import javax.ws.rs.core.MediaType


@Path("/rest")
class RequestRestService {

    private val gson = Gson()

    @Path("/registration") @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun registration(data : String) : String{

        if(data.isNullOrEmpty())
            return gson.toJson(ErrorResponse("request module non presenti"))

        val moduleRequest : RegistrationRequest
        try {
            moduleRequest = gson.fromJson(data,RegistrationRequest::class.java)
        }catch (exception : JsonSyntaxException){
            return gson.toJson(ErrorResponse("formato moduli non valido"))
        }

        val token : String
        try {
            token= ClientManager.instance
                    .registerClient(moduleRequest.eventSource, moduleRequest.notificatorModule)
        }catch(exception : Exception){
            return gson.toJson(ErrorResponse("Errore durante la registrazione. ${exception.message}"))
        }

        return  gson.toJson(RegistrationResponse(token))
    }

    @GET @Path("/registrationStatus")
    @Produces(MediaType.APPLICATION_JSON)
    fun registrationStatus(@QueryParam("token") token : String?) : String{
        logger.info("Richiesta di stato per il token ${token}")
        if(token == null)
            return gson.toJson(ErrorResponse("token non presente"))
        val status: Pair<Set<ModuleRequest>, Set<ModuleRequest>>

        try {
            status = ClientManager.instance.registrationStatus(token)
        } catch (ex: ClientNotFoundException) {
            return gson.toJson(StatusResponse(token, false, emptySet(), emptySet()))
        }

        return gson.toJson(StatusResponse(token, true,
                                    status.first, status.second))
    }

    @DELETE @Path("/deregistration")
    @Produces(MediaType.APPLICATION_JSON)
    fun deregistration(@QueryParam("token") token : String?) : String{
        if(token == null)
            return gson.toJson(ErrorResponse("token non presente"))
        ClientManager.instance.unregisterClient(token)
        return gson.toJson(DeregistrationResponse())
    }

    companion object {
        private val logger = LogManager.getLogger(RequestRestService::class.java)
    }

}