package com.prempk.personalai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactMatcherTest {
    @Test fun exactNameRanksAheadOfPartialMatches() {
        val contacts = listOf(
            ContactEntry("Rahul Sharma", "1111111111"),
            ContactEntry("Rahul", "2222222222"),
            ContactEntry("Rahul Verma", "3333333333")
        )
        assertEquals(
            listOf(ContactEntry("Rahul", "2222222222")),
            ContactMatcher.bestMatches(" rahul ", contacts)
        )
    }

    @Test fun returnsAllEquallyRankedMatchesInsteadOfGuessing() {
        val contacts = listOf(
            ContactEntry("Rahul Sharma", "1111111111"),
            ContactEntry("Rahul Verma", "2222222222"),
            ContactEntry("Rohan", "3333333333")
        )
        assertEquals(
            listOf(contacts[0], contacts[1]),
            ContactMatcher.bestMatches("rahul", contacts)
        )
    }

    @Test fun deduplicatesRepeatedPhoneNumbers() {
        val contacts = listOf(
            ContactEntry("Rahul Sharma", "+91 99999 00000"),
            ContactEntry("Rahul Sharma", "+919999900000")
        )
        assertEquals(1, ContactMatcher.bestMatches("Rahul Sharma", contacts).size)
    }

    @Test fun ignoresBlankAndUnmatchedQueries() {
        assertTrue(ContactMatcher.bestMatches(" ", listOf(ContactEntry("Rahul", "123"))).isEmpty())
        assertTrue(ContactMatcher.bestMatches("Amit", listOf(ContactEntry("Rahul", "123"))).isEmpty())
    }
}
