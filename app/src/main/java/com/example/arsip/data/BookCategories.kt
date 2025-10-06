package com.example.arsip.data

object BookCategories {
    // ✅ SEMUA KATEGORI BUKU LENGKAP
    const val SELF_DEVELOPMENT = "Self Development"
    const val NOVEL_ROMANCE = "Novel - Romance (Cinta)"
    const val NOVEL_MYSTERY = "Novel - Mystery/Thriller"
    const val NOVEL_FANTASY = "Novel - Fantasy/Sci-Fi"
    const val NOVEL_HISTORICAL = "Novel - Sejarah"
    const val BUSINESS = "Bisnis & Ekonomi"
    const val TECHNOLOGY = "Teknologi & Komputer"
    const val HEALTH = "Kesehatan & Medis"
    const val EDUCATION = "Pendidikan"
    const val RELIGION = "Agama & Spiritualitas"
    const val BIOGRAPHY = "Biografi & Autobiografi"
    const val HISTORY = "Sejarah"
    const val SCIENCE = "Sains & Penelitian"
    const val PSYCHOLOGY = "Psikologi"
    const val ART = "Seni & Kreativitas"
    const val COOKING = "Masak & Kuliner"
    const val TRAVEL = "Travel & Adventure"
    const val CHILDREN = "Buku Anak-anak"
    const val COMIC = "Komik & Manga"
    const val ACADEMIC = "Buku Akademis/Kuliah"
    const val OTHER = "Lainnya"

    // ✅ DAFTAR SEMUA KATEGORI UNTUK DROPDOWN
    val ALL_CATEGORIES = listOf(
        SELF_DEVELOPMENT,
        NOVEL_ROMANCE,
        NOVEL_MYSTERY,
        NOVEL_FANTASY,
        NOVEL_HISTORICAL,
        BUSINESS,
        TECHNOLOGY,
        HEALTH,
        EDUCATION,
        RELIGION,
        BIOGRAPHY,
        HISTORY,
        SCIENCE,
        PSYCHOLOGY,
        ART,
        COOKING,
        TRAVEL,
        CHILDREN,
        COMIC,
        ACADEMIC,
        OTHER
    )

    // ✅ HELPER FUNCTION UNTUK VALIDASI KATEGORI
    fun isValidCategory(category: String): Boolean {
        return ALL_CATEGORIES.contains(category)
    }
}
