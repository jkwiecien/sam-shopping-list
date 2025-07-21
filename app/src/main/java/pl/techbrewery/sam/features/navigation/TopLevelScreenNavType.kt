package pl.techbrewery.sam.features.navigation

import android.os.Bundle
import android.net.Uri
import androidx.navigation.NavType
import com.google.gson.Gson

class TopLevelScreenNavType : NavType<TopLevelScreen>(isNullableAllowed = false) {
    private val gson = Gson()

    override fun get(bundle: Bundle, key: String): TopLevelScreen? {
        return bundle.getString(key)?.let { gson.fromJson(it, TopLevelScreen::class.java) }
    }

    override fun parseValue(value: String): TopLevelScreen {
        // Navigation Compose might URL-decode the string before passing it here,
        // but if it comes from a deep link, it might still be encoded.
        // It's generally safer to decode if you encoded it.
        return gson.fromJson(Uri.decode(value), TopLevelScreen::class.java)
    }

    override fun put(bundle: Bundle, key: String, value: TopLevelScreen) {
        // We encode the JSON string to make it URL-safe for the route path
        bundle.putString(key, Uri.encode(gson.toJson(value)))
    }

    override val name: String = "TopLevelScreen"
}