package com.smartbudget.data

data class CurrencyInfo(
    val code: String,
    val symbol: String,
    val name: String,
    val country: String,
    val flag: String
)

object CurrencyData {
    val currencies = listOf(
        // Europe
        CurrencyInfo("EUR", "€", "Euro", "France", "🇫🇷"),
        CurrencyInfo("EUR", "€", "Euro", "Allemagne", "🇩🇪"),
        CurrencyInfo("EUR", "€", "Euro", "Espagne", "🇪🇸"),
        CurrencyInfo("EUR", "€", "Euro", "Italie", "🇮🇹"),
        CurrencyInfo("EUR", "€", "Euro", "Belgique", "🇧🇪"),
        CurrencyInfo("EUR", "€", "Euro", "Pays-Bas", "🇳🇱"),
        CurrencyInfo("EUR", "€", "Euro", "Portugal", "🇵🇹"),
        CurrencyInfo("EUR", "€", "Euro", "Irlande", "🇮🇪"),
        CurrencyInfo("EUR", "€", "Euro", "Autriche", "🇦🇹"),
        CurrencyInfo("EUR", "€", "Euro", "Finlande", "🇫🇮"),
        CurrencyInfo("EUR", "€", "Euro", "Grèce", "🇬🇷"),
        CurrencyInfo("GBP", "£", "Livre sterling", "Royaume-Uni", "🇬🇧"),
        CurrencyInfo("CHF", "CHF", "Franc suisse", "Suisse", "🇨🇭"),
        CurrencyInfo("SEK", "kr", "Couronne suédoise", "Suède", "🇸🇪"),
        CurrencyInfo("NOK", "kr", "Couronne norvégienne", "Norvège", "🇳🇴"),
        CurrencyInfo("DKK", "kr", "Couronne danoise", "Danemark", "🇩🇰"),
        CurrencyInfo("PLN", "zł", "Zloty", "Pologne", "🇵🇱"),
        CurrencyInfo("CZK", "Kč", "Couronne tchèque", "Tchéquie", "🇨🇿"),
        CurrencyInfo("HUF", "Ft", "Forint", "Hongrie", "🇭🇺"),
        CurrencyInfo("RON", "lei", "Leu roumain", "Roumanie", "🇷🇴"),
        CurrencyInfo("BGN", "лв", "Lev bulgare", "Bulgarie", "🇧🇬"),
        CurrencyInfo("HRK", "kn", "Kuna croate", "Croatie", "🇭🇷"),
        CurrencyInfo("RSD", "din.", "Dinar serbe", "Serbie", "🇷🇸"),
        CurrencyInfo("ISK", "kr", "Couronne islandaise", "Islande", "🇮🇸"),
        CurrencyInfo("UAH", "₴", "Hryvnia", "Ukraine", "🇺🇦"),
        CurrencyInfo("RUB", "₽", "Rouble", "Russie", "🇷🇺"),
        CurrencyInfo("TRY", "₺", "Livre turque", "Turquie", "🇹🇷"),
        CurrencyInfo("GEL", "₾", "Lari géorgien", "Géorgie", "🇬🇪"),

        // Amérique du Nord
        CurrencyInfo("USD", "\$", "Dollar américain", "États-Unis", "🇺🇸"),
        CurrencyInfo("CAD", "CA\$", "Dollar canadien", "Canada", "🇨🇦"),
        CurrencyInfo("MXN", "MX\$", "Peso mexicain", "Mexique", "🇲🇽"),

        // Amérique du Sud
        CurrencyInfo("BRL", "R\$", "Réal brésilien", "Brésil", "🇧🇷"),
        CurrencyInfo("ARS", "AR\$", "Peso argentin", "Argentine", "🇦🇷"),
        CurrencyInfo("CLP", "CL\$", "Peso chilien", "Chili", "🇨🇱"),
        CurrencyInfo("COP", "CO\$", "Peso colombien", "Colombie", "🇨🇴"),
        CurrencyInfo("PEN", "S/", "Sol péruvien", "Pérou", "🇵🇪"),
        CurrencyInfo("UYU", "\$U", "Peso uruguayen", "Uruguay", "🇺🇾"),
        CurrencyInfo("VES", "Bs.", "Bolivar vénézuélien", "Venezuela", "🇻🇪"),
        CurrencyInfo("BOB", "Bs", "Boliviano", "Bolivie", "🇧🇴"),

        // Caraïbes
        CurrencyInfo("DOP", "RD\$", "Peso dominicain", "République dominicaine", "🇩🇴"),
        CurrencyInfo("JMD", "J\$", "Dollar jamaïcain", "Jamaïque", "🇯🇲"),
        CurrencyInfo("HTG", "G", "Gourde haïtienne", "Haïti", "🇭🇹"),

        // Asie
        CurrencyInfo("JPY", "¥", "Yen", "Japon", "🇯🇵"),
        CurrencyInfo("CNY", "¥", "Yuan", "Chine", "🇨🇳"),
        CurrencyInfo("KRW", "₩", "Won sud-coréen", "Corée du Sud", "🇰🇷"),
        CurrencyInfo("INR", "₹", "Roupie indienne", "Inde", "🇮🇳"),
        CurrencyInfo("IDR", "Rp", "Roupie indonésienne", "Indonésie", "🇮🇩"),
        CurrencyInfo("THB", "฿", "Baht", "Thaïlande", "🇹🇭"),
        CurrencyInfo("VND", "₫", "Dong", "Vietnam", "🇻🇳"),
        CurrencyInfo("PHP", "₱", "Peso philippin", "Philippines", "🇵🇭"),
        CurrencyInfo("MYR", "RM", "Ringgit", "Malaisie", "🇲🇾"),
        CurrencyInfo("SGD", "S\$", "Dollar singapourien", "Singapour", "🇸🇬"),
        CurrencyInfo("TWD", "NT\$", "Dollar taïwanais", "Taïwan", "🇹🇼"),
        CurrencyInfo("HKD", "HK\$", "Dollar hongkongais", "Hong Kong", "🇭🇰"),
        CurrencyInfo("PKR", "₨", "Roupie pakistanaise", "Pakistan", "🇵🇰"),
        CurrencyInfo("BDT", "৳", "Taka", "Bangladesh", "🇧🇩"),
        CurrencyInfo("LKR", "Rs", "Roupie srilankaise", "Sri Lanka", "🇱🇰"),
        CurrencyInfo("MMK", "K", "Kyat", "Myanmar", "🇲🇲"),
        CurrencyInfo("KHR", "៛", "Riel", "Cambodge", "🇰🇭"),
        CurrencyInfo("LAK", "₭", "Kip", "Laos", "🇱🇦"),
        CurrencyInfo("MNT", "₮", "Tugrik", "Mongolie", "🇲🇳"),
        CurrencyInfo("KZT", "₸", "Tenge", "Kazakhstan", "🇰🇿"),
        CurrencyInfo("UZS", "soʻm", "Sum ouzbek", "Ouzbékistan", "🇺🇿"),

        // Moyen-Orient
        CurrencyInfo("AED", "د.إ", "Dirham émirati", "Émirats arabes unis", "🇦🇪"),
        CurrencyInfo("SAR", "﷼", "Riyal saoudien", "Arabie saoudite", "🇸🇦"),
        CurrencyInfo("QAR", "﷼", "Riyal qatari", "Qatar", "🇶🇦"),
        CurrencyInfo("KWD", "د.ك", "Dinar koweïtien", "Koweït", "🇰🇼"),
        CurrencyInfo("BHD", "BD", "Dinar bahreïni", "Bahreïn", "🇧🇭"),
        CurrencyInfo("OMR", "﷼", "Rial omanais", "Oman", "🇴🇲"),
        CurrencyInfo("JOD", "JD", "Dinar jordanien", "Jordanie", "🇯🇴"),
        CurrencyInfo("ILS", "₪", "Shekel", "Israël", "🇮🇱"),
        CurrencyInfo("LBP", "L£", "Livre libanaise", "Liban", "🇱🇧"),
        CurrencyInfo("IQD", "ع.د", "Dinar irakien", "Irak", "🇮🇶"),
        CurrencyInfo("IRR", "﷼", "Rial iranien", "Iran", "🇮🇷"),

        // Afrique
        CurrencyInfo("ZAR", "R", "Rand", "Afrique du Sud", "🇿🇦"),
        CurrencyInfo("NGN", "₦", "Naira", "Nigeria", "🇳🇬"),
        CurrencyInfo("EGP", "E£", "Livre égyptienne", "Égypte", "🇪🇬"),
        CurrencyInfo("MAD", "د.م.", "Dirham marocain", "Maroc", "🇲🇦"),
        CurrencyInfo("TND", "د.ت", "Dinar tunisien", "Tunisie", "🇹🇳"),
        CurrencyInfo("DZD", "د.ج", "Dinar algérien", "Algérie", "🇩🇿"),
        CurrencyInfo("KES", "KSh", "Shilling kényan", "Kenya", "🇰🇪"),
        CurrencyInfo("GHS", "₵", "Cedi", "Ghana", "🇬🇭"),
        CurrencyInfo("ETB", "Br", "Birr éthiopien", "Éthiopie", "🇪🇹"),
        CurrencyInfo("TZS", "TSh", "Shilling tanzanien", "Tanzanie", "🇹🇿"),
        CurrencyInfo("UGX", "USh", "Shilling ougandais", "Ouganda", "🇺🇬"),
        CurrencyInfo("XOF", "CFA", "Franc CFA (BCEAO)", "Sénégal", "🇸🇳"),
        CurrencyInfo("XOF", "CFA", "Franc CFA (BCEAO)", "Côte d'Ivoire", "🇨🇮"),
        CurrencyInfo("XOF", "CFA", "Franc CFA (BCEAO)", "Mali", "🇲🇱"),
        CurrencyInfo("XOF", "CFA", "Franc CFA (BCEAO)", "Burkina Faso", "🇧🇫"),
        CurrencyInfo("XOF", "CFA", "Franc CFA (BCEAO)", "Bénin", "🇧🇯"),
        CurrencyInfo("XOF", "CFA", "Franc CFA (BCEAO)", "Togo", "🇹🇬"),
        CurrencyInfo("XOF", "CFA", "Franc CFA (BCEAO)", "Niger", "🇳🇪"),
        CurrencyInfo("XAF", "FCFA", "Franc CFA (BEAC)", "Cameroun", "🇨🇲"),
        CurrencyInfo("XAF", "FCFA", "Franc CFA (BEAC)", "Gabon", "🇬🇦"),
        CurrencyInfo("XAF", "FCFA", "Franc CFA (BEAC)", "Congo", "🇨🇬"),
        CurrencyInfo("XAF", "FCFA", "Franc CFA (BEAC)", "Tchad", "🇹🇩"),
        CurrencyInfo("MGA", "Ar", "Ariary", "Madagascar", "🇲🇬"),
        CurrencyInfo("RWF", "FRw", "Franc rwandais", "Rwanda", "🇷🇼"),
        CurrencyInfo("CDF", "FC", "Franc congolais", "RD Congo", "🇨🇩"),
        CurrencyInfo("MUR", "₨", "Roupie mauricienne", "Maurice", "🇲🇺"),
        CurrencyInfo("LYD", "LD", "Dinar libyen", "Libye", "🇱🇾"),

        // Océanie
        CurrencyInfo("AUD", "A\$", "Dollar australien", "Australie", "🇦🇺"),
        CurrencyInfo("NZD", "NZ\$", "Dollar néo-zélandais", "Nouvelle-Zélande", "🇳🇿"),
        CurrencyInfo("FJD", "FJ\$", "Dollar fidjien", "Fidji", "🇫🇯"),
        CurrencyInfo("XPF", "₣", "Franc CFP", "Polynésie française", "🇵🇫"),
        CurrencyInfo("XPF", "₣", "Franc CFP", "Nouvelle-Calédonie", "🇳🇨"),
    )

    fun search(query: String): List<CurrencyInfo> {
        if (query.isBlank()) return currencies
        val q = query.lowercase().trim()
        return currencies.filter {
            it.country.lowercase().contains(q) ||
            it.name.lowercase().contains(q) ||
            it.code.lowercase().contains(q)
        }
    }

    fun getByCode(code: String): CurrencyInfo? {
        return currencies.firstOrNull { it.code == code }
    }
}
