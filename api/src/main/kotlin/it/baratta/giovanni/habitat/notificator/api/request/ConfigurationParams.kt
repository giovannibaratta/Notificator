package it.baratta.giovanni.habitat.notificator.api.request

import java.util.*

class ConfigurationParams(paramsMap : HashMap<String, String>) {

    companion object {
        val empty = ConfigurationParams(HashMap())
    }

    private val params = paramsMap

    fun setParam(key : String, value : String){
        params.put(key, value)
    }

    fun getParam(key : String) : String?{
        return params[key]
    }
}