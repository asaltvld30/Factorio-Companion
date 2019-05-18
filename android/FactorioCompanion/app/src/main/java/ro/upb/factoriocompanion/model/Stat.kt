package ro.upb.factoriocompanion.model

data class Stat(val name: String, val rate: Double, val count: Long) {

    fun getImageName(): String {
        return this.name.replace("-", "_")
    }

    companion object {
        private const val KEY_RATE = "rate"
        private const val KEY_COUNT = "count"
        private const val KEY_NAME = "name"

        fun parseIncomingFirebaseHashSet(data: HashMap<*, *>): Array<Stat> {
            return data.entries
                .map { entry ->
                    val element = entry.value as HashMap<*, *>

                    val rate = element.get(KEY_RATE) as Number
                    val count = element.get(KEY_COUNT) as Number
                    val name = element.get(KEY_NAME) as String

                    Stat(name, rate.toDouble(), count.toLong())
                }
                .sortedByDescending { item -> item.count }
                .toTypedArray()
        }
    }
}