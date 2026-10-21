package com.example.demo.matchmaking;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatchmakingServiceTest {

    @Test
    void onlineCountTracksUsersRatherThanTabs() {
        MatchmakingService service = new MatchmakingService();
        service.userConnected("a@vrsec.ac.in", "tab-1");
        service.userConnected("a@vrsec.ac.in", "tab-2");
        service.userConnected("b@vrsec.ac.in", "tab-1");

        assertEquals(2, service.getOnlineCount());

        assertFalse(service.userDisconnected("a@vrsec.ac.in", "tab-1"));
        assertEquals(2, service.getOnlineCount());

        assertTrue(service.userDisconnected("a@vrsec.ac.in", "tab-2"));
        assertEquals(1, service.getOnlineCount());
    }

    @Test
    void matchesTwoDifferentOnlineUsers() {
        MatchmakingService service = new MatchmakingService();
        service.userConnected("a@vrsec.ac.in", "a-session");
        service.userConnected("b@vrsec.ac.in", "b-session");

        assertNull(service.findMatch("a@vrsec.ac.in"));
        assertEquals("MATCHED", service.findMatch("b@vrsec.ac.in"));
        assertEquals("b@vrsec.ac.in", service.getPartner("a@vrsec.ac.in"));
        assertEquals("a@vrsec.ac.in", service.getPartner("b@vrsec.ac.in"));
    }

    @Test
    void nextDoesNotImmediatelyPairTheSameTwoUsersAgain() {
        MatchmakingService service = new MatchmakingService();
        service.userConnected("a@vrsec.ac.in", "a-session");
        service.userConnected("b@vrsec.ac.in", "b-session");

        service.findMatch("a@vrsec.ac.in");
        service.findMatch("b@vrsec.ac.in");
        service.skip("a@vrsec.ac.in");

        assertNull(service.findMatch("a@vrsec.ac.in"));
        assertNull(service.findMatch("b@vrsec.ac.in"));

        service.userConnected("c@vrsec.ac.in", "c-session");
        assertEquals("MATCHED", service.findMatch("c@vrsec.ac.in"));
        assertNotEquals("b@vrsec.ac.in", service.getPartner("c@vrsec.ac.in"));
    }

    @Test
    void disconnectedUsersAreNotMatched() {
        MatchmakingService service = new MatchmakingService();
        service.userConnected("a@vrsec.ac.in", "a-session");
        service.userConnected("b@vrsec.ac.in", "b-session");

        service.findMatch("a@vrsec.ac.in");
        assertTrue(service.userDisconnected("a@vrsec.ac.in", "a-session"));

        assertNull(service.findMatch("b@vrsec.ac.in"));
        assertNull(service.getPartner("b@vrsec.ac.in"));
    }
}