package dev.contextnudge

import org.junit.Assert.*
import org.junit.Test

class NotificationFilterTest {
    @Test fun ordinaryAppointmentAllowed() { assertTrue(NotificationFilter.eligible("Amanhã às 15h vamos ao shopping")) }
    @Test fun otpBlocked() { assertFalse(NotificationFilter.eligible("Seu código de verificação é 123456")) }
    @Test fun financialBlocked() { assertFalse(NotificationFilter.eligible("Seu saldo no banco foi atualizado")) }
    @Test fun emptyBlocked() { assertFalse(NotificationFilter.eligible("")) }
}
