package de.jakobclass.transittracker.utilities

class BiMap<K,V> {
    private val map = mutableMapOf<K,V>()
    private val inverseMap = mutableMapOf<V,K>()

    operator fun get(key: K): V? {
        return map[key]
    }

    operator fun set(key: K, value: V) {
        map[key] = value
        inverseMap[value] = key
    }

    fun getKey(value: V): K? {
        return inverseMap[value]
    }

    fun remove(key: K): V? {
        val value = map.remove(key)
        value?.let { inverseMap.remove(it) }
        return value
    }
}