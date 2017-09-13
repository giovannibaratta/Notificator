package it.baratta.giovanni.habitat.notificator.core.network.rest

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import it.baratta.giovanni.habitat.notificator.api.request.ConfigurationParams
import it.baratta.giovanni.habitat.notificator.api.request.ModuleRequest
import it.baratta.giovanni.habitat.notificator.api.request.RegistrationRequest
import it.baratta.giovanni.habitat.notificator.api.response.ResponseBuilder
import it.baratta.giovanni.habitat.notificator.core.ClientManager
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("/rest")
class RequestRestService {

    private val gson = Gson()

    @Path("/registration")
    @POST @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun registration(data : String) : String{

        if(data.isNullOrEmpty())
            return gson.toJson(ResponseBuilder.errorResponse("request module non presenti"))

        val moduleRequest : RegistrationRequest
        try {
            moduleRequest = gson.fromJson(data,RegistrationRequest::class.java)
        }catch (exception : JsonSyntaxException){
            return gson.toJson(ResponseBuilder.errorResponse("formato moduli non valido"))
        }

        val token : String
        try {
            token= ClientManager.instance
                    .registerClient(moduleRequest.eventSource, moduleRequest.notificatorModule)
        }catch(exception : Exception){
            return gson.toJson(ResponseBuilder.errorResponse("Errore durante la registrazione. ${exception.message}"))
        }

        return  gson.toJson(ResponseBuilder.registrationResponse(token))
    }

    @DELETE @Path("/deregistration")
    @Produces(MediaType.APPLICATION_JSON)
    fun deregistration(@QueryParam("token") token : String?) : String{
        if(token == null)
            return gson.toJson(ResponseBuilder.errorResponse("token non presente"))
        ClientManager.instance.unregisterClient(token)
        return gson.toJson(ResponseBuilder.deregistrationResponse())
    }

}