package it.baratta.giovanni.notificator.api.response

data class RegistrationResponse(val token : String) : IResponse {
    override val error: Boolean = false

    override val className: String
            = RegistrationResponse::class.java.canonicalName
}