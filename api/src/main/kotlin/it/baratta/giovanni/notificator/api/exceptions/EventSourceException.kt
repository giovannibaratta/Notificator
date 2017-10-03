package it.baratta.giovanni.notificator.api.exceptions

abstract class EventSourceException : RuntimeException {
    constructor(msg: String) : super(msg)
    constructor(throwable: Throwable) : super(throwable)
    constructor(msg: String, throwable: Throwable) : super(msg, throwable)
}

class ShutdownException : EventSourceException {
    constructor(msg: String) : super(msg)
    constructor(throwable: Throwable) : super(throwable)
    constructor(msg: String, throwable: Throwable) : super(msg, throwable)
}