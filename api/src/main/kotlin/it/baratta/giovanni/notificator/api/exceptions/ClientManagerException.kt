package it.baratta.giovanni.notificator.api.exceptions

abstract class ClientManagerException : RuntimeException {
    constructor(msg: String) : super(msg)
    constructor(throwable: Throwable) : super(throwable)
    constructor(msg: String, throwable: Throwable) : super(msg, throwable)
}

class ClientRegistrationException : EventSourceException {
    constructor(msg: String) : super(msg)
    constructor(throwable: Throwable) : super(throwable)
    constructor(msg: String, throwable: Throwable) : super(msg, throwable)
}

class ClientNotFoundException : EventSourceException {
    constructor(msg: String) : super(msg)
    constructor(throwable: Throwable) : super(throwable)
    constructor(msg: String, throwable: Throwable) : super(msg, throwable)
}