package it.baratta.giovanni.habitat.notificator.api.request

import java.util.*

data class ConfigurationParams(private val params : HashMap<String, String>) {

    fun setParam(key : String, value : String){
        params.put(key, value)
    }

    fun getParam(key : String) : String?{
        return params[key]
    }
}