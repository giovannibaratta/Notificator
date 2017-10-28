package it.baratta.giovanni.notificator.utils

import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.message.SimpleMessage

fun Logger.errorAndThrow(throwable: Throwable) : Nothing{
    this.error{ SimpleMessage(throwable.message) }
    throw throwable
}

fun Logger.fatalAndThrow(throwable: Throwable) : Nothing{
    this.fatal{ SimpleMessage(throwable.message) }
    throw throwable
}