package ro.upb.factoriocompanion.model

import java.util.*
import kotlin.collections.HashMap

data class Stat(val name: String, val rate: Double, val count: Long, val date: Date) {
    fun getImageName(): String {
        return this.name.replace("-", "_")
    }

    constructor(name: String, rate: Double, count: Long) : this(name, rate, count, Stat.getCurrentDateTime())

    companion object {
        private const val KEY_RATE = "rate"
        private const val KEY_COUNT = "count"
        private const val KEY_NAME = "name"

        private fun getCurrentDateTime(): Date {
            return Calendar.getInstance().time
        }

        fun parseIncomingFirebaseHashSet(data: HashMap<*, *>): Array<Stat> {
            return data.entries
                .map { entry ->
                    val element = entry.value as HashMap<*, *>

                    val rate = element.get(KEY_RATE) as Number
                    val count = element.get(KEY_COUNT) as Number
                    val name = element.get(KEY_NAME) as String

                    val date = getCurrentDateTime()

                    Stat(name, rate.toDouble(), count.toLong(), date)
                }
                .sortedByDescending { item -> item.count }
                .toTypedArray()
        }
    }
}