package com.prempk.personalai

import java.util.Locale

data class ContactEntry(val name: String, val number: String)

/** Ranks contact matches without choosing arbitrarily between equally ranked people. */
object ContactMatcher {
    fun bestMatches(query: String, contacts: List<ContactEntry>): List<ContactEntry> {
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isBlank()) return emptyList()

        val ranked = contacts.mapNotNull { contact ->
            val normalizedName = normalize(contact.name)
            val rank = when {
                normalizedName == normalizedQuery -> 0
                normalizedName.startsWith(normalizedQuery) -> 1
                normalizedName.contains(normalizedQuery) -> 2
                else -> null
            }
            rank?.let { it to contact }
        }

        val bestRank = ranked.minOfOrNull { it.first } ?: return emptyList()
        return ranked.asSequence()
            .filter { it.first == bestRank }
            .map { it.second }
            .distinctBy { normalize(it.number) }
            .toList()
    }

    private fun normalize(value: String): String =
        value.trim().lowercase(Locale.ROOT).replace(Regex("\\s+"), " ")
}
