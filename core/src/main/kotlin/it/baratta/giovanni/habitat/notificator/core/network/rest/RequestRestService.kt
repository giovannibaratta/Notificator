package it.baratta.giovanni.habitat.notificator.core.network.rest

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import it.baratta.giovanni.habitat.notificator.api.request.RegistrationRequest
import it.baratta.giovanni.habitat.notificator.api.response.DeregistrationResponse
import it.baratta.giovanni.habitat.notificator.api.response.ErrorResponse
import it.baratta.giovanni.habitat.notificator.api.response.RegistrationResponse
import it.baratta.giovanni.habitat.notificator.api.response.StatusResponse
import it.baratta.giovanni.habitat.notificator.core.ClientManager
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
        if(token == null)
            return gson.toJson(ErrorResponse("token non presente"))
        val status = ClientManager.instance.registrationStatus(token)
        return gson.toJson(StatusResponse( !status.first.isEmpty() && !status.second.isEmpty(),
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

}