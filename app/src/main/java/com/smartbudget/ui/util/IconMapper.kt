package com.smartbudget.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object IconMapper {
    private val iconMap = mapOf(
        // Money & Finance
        "payments" to Icons.Filled.AttachMoney,
        "attach_money" to Icons.Filled.AttachMoney,
        "account_balance" to Icons.Filled.AccountBalance,
        "savings" to Icons.Filled.AccountBalance,
        "euro_symbol" to Icons.Filled.AttachMoney,
        "receipt" to Icons.Filled.Receipt,
        "card_giftcard" to Icons.Filled.Favorite,

        // Work & Education
        "work" to Icons.Filled.Work,
        "school" to Icons.Filled.School,
        "menu_book" to Icons.Filled.MenuBook,

        // Food & Drink
        "restaurant" to Icons.Filled.Restaurant,
        "local_cafe" to Icons.Filled.LocalCafe,
        "local_grocery_store" to Icons.Filled.LocalGroceryStore,
        "local_bar" to Icons.Filled.LocalBar,
        "local_pizza" to Icons.Filled.LocalPizza,
        "cake" to Icons.Filled.Cake,

        // Transport
        "directions_car" to Icons.Filled.DirectionsCar,
        "local_gas_station" to Icons.Filled.LocalGasStation,
        "flight" to Icons.Filled.Flight,
        "directions_bus" to Icons.Filled.DirectionsBus,
        "two_wheeler" to Icons.Filled.TwoWheeler,
        "local_parking" to Icons.Filled.LocalParking,
        "local_taxi" to Icons.Filled.LocalTaxi,

        // Home & Living
        "home" to Icons.Filled.Home,
        "electric_bolt" to Icons.Filled.FlashOn,
        "water_drop" to Icons.Filled.Opacity,
        "wifi" to Icons.Filled.Wifi,
        "build" to Icons.Filled.Build,
        "cleaning_services" to Icons.Filled.CleaningServices,

        // Shopping
        "shopping_bag" to Icons.Filled.ShoppingCart,
        "checkroom" to Icons.Filled.Store,
        "store" to Icons.Filled.Store,
        "local_mall" to Icons.Filled.LocalMall,

        // Health & Fitness
        "local_hospital" to Icons.Filled.LocalHospital,
        "fitness_center" to Icons.Filled.FitnessCenter,
        "spa" to Icons.Filled.Spa,

        // Entertainment
        "sports_esports" to Icons.Filled.Star,
        "movie" to Icons.Filled.Movie,
        "music_note" to Icons.Filled.MusicNote,
        "sports_soccer" to Icons.Filled.SportsSoccer,
        "sports_basketball" to Icons.Filled.SportsBasketball,
        "theater_comedy" to Icons.Filled.TheaterComedy,
        "park" to Icons.Filled.Park,
        "beach_access" to Icons.Filled.BeachAccess,

        // Tech & Communication
        "phone" to Icons.Filled.Phone,
        "laptop" to Icons.Filled.Laptop,
        "headphones" to Icons.Filled.Headphones,
        "photo_camera" to Icons.Filled.PhotoCamera,

        // Family & People
        "child_care" to Icons.Filled.Face,
        "pets" to Icons.Filled.Pets,
        "favorite" to Icons.Filled.Favorite,
        "volunteer_activism" to Icons.Filled.VolunteerActivism,

        // Other
        "more_horiz" to Icons.Filled.MoreHoriz,
        "label" to Icons.Filled.Label,
        "bookmark" to Icons.Filled.Bookmark,
        "flag" to Icons.Filled.Flag,
        "lightbulb" to Icons.Filled.Lightbulb,
    )

    fun getIcon(name: String): ImageVector = iconMap[name] ?: Icons.Filled.Label

    fun getAllIcons(): Map<String, ImageVector> = iconMap
}
