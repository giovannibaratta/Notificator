package it.baratta.giovanni.notificator.api.exceptions

class InitializationException : RuntimeException {
    constructor(msg: String) : super(msg)
    constructor(throwable: Throwable) : super(throwable)
    constructor(msg: String, throwable: Throwable) : super(msg, throwable)
}