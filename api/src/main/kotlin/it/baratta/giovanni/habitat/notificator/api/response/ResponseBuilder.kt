package it.baratta.giovanni.habitat.notificator.api.response

class ResponseBuilder(){
    companion object {
        fun errorResponse(errorMsg : String) : ResponseHolder
            = ResponseHolder(ErrorResponse::class.qualifiedName ?: throw IllegalStateException("La classe non ha un nome"),
                                ErrorResponse(errorMsg))

        fun registrationResponse(token : String) : ResponseHolder
                = ResponseHolder(RegistrationResponse::class.qualifiedName ?: throw IllegalStateException("La classe non ha un nome"),
                                RegistrationResponse(token))

        fun deregistrationResponse() : ResponseHolder
                = ResponseHolder(DeregistrationResponse::class.qualifiedName ?: throw IllegalStateException("La classe non ha un nome"),
                DeregistrationResponse())
    }
}