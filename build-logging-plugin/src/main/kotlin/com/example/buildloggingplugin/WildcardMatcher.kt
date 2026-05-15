package com.example.buildloggingplugin

object WildcardMatcher {

    fun match(pattern: String, input: String): Boolean {
        return when {
            pattern == input -> true
            pattern == "**" -> true
            pattern == "*" -> !input.contains('.')
            pattern.contains("**") -> matchDoubleStar(pattern, input)
            pattern.contains("*") -> matchSingleStar(pattern, input)
            else -> pattern == input
        }
    }

    private fun matchDoubleStar(pattern: String, input: String): Boolean {
        val parts = pattern.split("**")
        if (parts.size == 2) {
            val prefix = parts[0]
            val suffix = parts[1]
            return (prefix.isEmpty() || input.startsWith(prefix)) &&
                    (suffix.isEmpty() || input.endsWith(suffix))
        }
        return false
    }

    private fun matchSingleStar(pattern: String, input: String): Boolean {
        val inputParts = input.split('.')
        val patternParts = pattern.split('.')
        
        if (inputParts.size != patternParts.size) {
            return false
        }
        
        for (i in inputParts.indices) {
            val patternPart = patternParts[i]
            val inputPart = inputParts[i]
            
            if (patternPart == "*") {
                continue
            } else if (!patternPart.contains("*")) {
                if (patternPart != inputPart) {
                    return false
                }
            } else {
                val regex = patternPart.replace("*", ".*")
                if (!inputPart.matches(Regex(regex))) {
                    return false
                }
            }
        }
        return true
    }

    fun matchesAny(patterns: List<String>, input: String): Boolean {
        return patterns.any { pattern ->
            match(pattern, input)
        }
    }
}
