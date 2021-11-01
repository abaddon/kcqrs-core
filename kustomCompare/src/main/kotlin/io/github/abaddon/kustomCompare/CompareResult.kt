package io.github.abaddon.kustomCompare

data class CompareResult(
    private val result: Boolean,
    private val unMatchedProperties: Set<String>
){

    fun result():Boolean = this.result;

    fun unMatchedProperties():Set<String> = this.unMatchedProperties;

    override fun toString(): String{
        return StringBuilder("Result:")
            .appendLine()
            .append("test: $result").appendLine()
            .append("unmatched properties: ").appendLine()
            .append(unMatchedProperties.toString())
            .toString()
    }
}
