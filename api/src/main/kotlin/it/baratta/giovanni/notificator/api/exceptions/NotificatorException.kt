package it.baratta.giovanni.notificator.api.exceptions

abstract class NotificatorException : RuntimeException {
    constructor(msg: String) : super(msg)
    constructor(throwable: Throwable) : super(throwable)
    constructor(msg: String, throwable: Throwable) : super(msg, throwable)
}

class ssShutdownException : NotificatorException {
    constructor(msg: String) : super(msg)
    constructor(throwable: Throwable) : super(throwable)
    constructor(msg: String, throwable: Throwable) : super(msg, throwable)
}