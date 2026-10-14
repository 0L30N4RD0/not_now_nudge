package dev.contextnudge

/** Pure, conservative filtering. No message text is persisted. */
object NotificationFilter {
    private val sensitive = Regex("""(?i)(?:\b(?:otp|2fa|verification code|codigo de verificacao|código de verificação|senha|password|token|c[oó]digo de acesso|autentica[cç][aã]o)\b|\b(?:pix|banco|bank|cart[aã]o|credit card|saldo|extrato|transa[cç][aã]o)\b)""")
    private val sixDigit = Regex("""\b\d{4,8}\b""")
    fun eligible(text: String): Boolean {
        val clean = text.trim()
        if (clean.length !in 12..500 || (clean.contains('\n') && clean.lines().size > 5)) return false
        if (sensitive.containsMatchIn(clean)) return false
        if (sixDigit.containsMatchIn(clean) && Regex("""(?i)\b(c[oó]digo|code|pin|login)\b""").containsMatchIn(clean)) return false
        return true
    }
}
