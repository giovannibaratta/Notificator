package it.baratta.giovanni.notificator.api.request

/**
 * Classe di supporto per impostare i parametri di un [ModuleRequest].
 *
 * @param paramsMap dizionario contenente i parametri <chiave,valore>
 *     da utilizzare per inizializzare il dizionario interno.
 */
class ConfigurationParams(paramsMap : HashMap<String, String>? = null) {

    private val map = HashMap<String, String>()

    init {
        if(paramsMap != null)
            paramsMap.forEach{ map.put(it.key, it.value) }
    }

    /**
     * Imposta il valore [value] al parametro con chiave
     * [key]. Sovrascrive valori già presenti.
     */
    fun setParam(key : String, value : String){
        map.put(key, value)
    }

    /**
     * Recupera il parametro con chiave [key]. Se la chiave
     * [key] non è presente restituisce null.
     *
     * @param key chiave da utilizzare per recuperare il valore.
     * @return il valore associato a [key] oppure null se la chiave
     *          non è presente.
     */
    fun getParam(key : String) : String?{
        return map[key]
    }
}