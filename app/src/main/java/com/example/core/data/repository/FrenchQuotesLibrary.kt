package com.example.core.data.repository

data class FrenchQuote(
    val quote: String,
    val author: String,
    val category: String
)

object FrenchQuotesLibrary {
    val categories = listOf("Motivation", "Succès", "Créativité", "Sagesse", "Design", "Amour")

    val quotes = listOf(
        // Motivation
        FrenchQuote("Le succès, c'est tomber sept fois et se relever huit.", "Proverbe", "Motivation"),
        FrenchQuote("Crois en toi et en tout ce que tu es.", "Christian D. Larson", "Motivation"),
        FrenchQuote("Il n'y a pas de limites à ce que vous pouvez accomplir.", "Michelle Obama", "Motivation"),
        FrenchQuote("L'avenir appartient à ceux qui croient en la beauté de leurs rêves.", "Eleanor Roosevelt", "Motivation"),
        FrenchQuote("Fais de ta vie un rêve, et d'un rêve, une réalité.", "Antoine de Saint-Exupéry", "Motivation"),
        FrenchQuote("Ne rêve pas ta vie, vis tes rêves.", "Anonyme", "Motivation"),
        
        // Succès
        FrenchQuote("Le succès n'est pas la clé du bonheur. Le bonheur est la clé du succès.", "Albert Schweitzer", "Succès"),
        FrenchQuote("La seule façon de faire du bon travail est d'aimer ce que vous faites.", "Steve Jobs", "Succès"),
        FrenchQuote("Ceux qui pensent qu'il est impossible d'agir ne devraient pas déranger ceux qui le font.", "Confucius", "Succès"),
        FrenchQuote("Tout ce dont vous avez besoin pour réussir est déjà en vous.", "Anonyme", "Succès"),
        FrenchQuote("La discipline est le pont entre les objectifs et l'accomplissement.", "Jim Rohn", "Succès"),

        // Créativité
        FrenchQuote("La créativité, c'est l'intelligence qui s'amuse.", "Albert Einstein", "Créativité"),
        FrenchQuote("Chaque artiste a d'abord été un amateur.", "Ralph Waldo Emerson", "Créativité"),
        FrenchQuote("Créer, c'est vivre deux fois.", "Albert Camus", "Créativité"),
        FrenchQuote("L'imagination est plus importante que le savoir.", "Albert Einstein", "Créativité"),
        FrenchQuote("L'art lave notre âme de la poussière du quotidien.", "Pablo Picasso", "Créativité"),

        // Sagesse
        FrenchQuote("La simplicité est la sophistication suprême.", "Léonard de Vinci", "Sagesse"),
        FrenchQuote("Connais-toi toi-même.", "Socrate", "Sagesse"),
        FrenchQuote("Rien de grand ne s'est accompli sans passion.", "Hegel", "Sagesse"),
        FrenchQuote("La paix commence par un sourire.", "Mère Teresa", "Sagesse"),
        FrenchQuote("Soyez le changement que vous voulez voir dans le monde.", "Gandhi", "Sagesse"),

        // Design
        FrenchQuote("Le design n'est pas seulement ce à quoi il ressemble, mais comment il fonctionne.", "Steve Jobs", "Design"),
        FrenchQuote("Moins, c'est plus.", "Ludwig Mies van der Rohe", "Design"),
        FrenchQuote("Le bon design est aussi peu de design que possible.", "Dieter Rams", "Design"),
        FrenchQuote("La perfection est atteinte non pas lorsqu'il n'y a plus rien à ajouter, mais lorsqu'il n'y a plus rien à retrancher.", "Antoine de Saint-Exupéry", "Design"),

        // Amour
        FrenchQuote("Aimer, ce n'est pas se regarder l'un l'autre, c'est regarder ensemble dans la même direction.", "Antoine de Saint-Exupéry", "Amour"),
        FrenchQuote("Il n'y a qu'un bonheur dans la vie, c'est d'aimer et d'être aimé.", "George Sand", "Amour"),
        FrenchQuote("Le cœur a ses raisons que la raison ne connaît point.", "Blaise Pascal", "Amour")
    )
}
