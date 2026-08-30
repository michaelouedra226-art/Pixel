package com.example.core.data.repository

data class FontPreset(
    val id: String,
    val name: String,
    val category: String
)

object FontLibrary {
    val fonts = listOf(
        FontPreset("Cinzel", "Cinzel (Romain Classique)", "Serif"),
        FontPreset("Montserrat", "Montserrat (Moderne Épuré)", "Sans"),
        FontPreset("Playfair", "Playfair Display (Élégant Luxe)", "Serif"),
        FontPreset("Oswald", "Oswald (Impact Condensé)", "Display"),
        FontPreset("Bebas", "Bebas Neue (Poster & Titre)", "Display"),
        FontPreset("Pacifico", "Pacifico (Calligraphie Rétro)", "Script"),
        FontPreset("Caveat", "Caveat (Écriture Manuscrite)", "Script"),
        FontPreset("Orbitron", "Orbitron (Futuriste & Sci-Fi)", "Tech"),
        FontPreset("Righteous", "Righteous (Synthwave & Arcade)", "Display"),
        FontPreset("GreatVibes", "Great Vibes (Signature Royale)", "Script"),
        FontPreset("Roboto", "Roboto (Standard Neutre)", "Sans"),
        FontPreset("Monospace", "Monospace (Code & Terminal)", "Tech")
    )
}
