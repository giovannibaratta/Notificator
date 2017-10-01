package it.baratta.giovanni.notificator.api.response

class DeregistrationResponse : IResponse {
    override val error: Boolean = false

    override val className: String
            = DeregistrationResponse::class.java.canonicalName
}

