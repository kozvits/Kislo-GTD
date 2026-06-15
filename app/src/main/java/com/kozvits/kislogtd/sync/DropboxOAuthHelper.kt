package com.kozvits.kislogtd.sync

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Helper for Dropbox OAuth2 PKCE flow.
 * User needs to create a Dropbox app at https://www.dropbox.com/developers/apps
 * and obtain App Key + App Secret.
 *
 * For quick testing, the manual token input in Settings can be used instead.
 */
object DropboxOAuthHelper {

    private const val REDIRECT_URI = "kislogtd://dropbox-auth"

    /**
     * Build the authorization URL for the user's browser.
     */
    fun getAuthorizationUrl(appKey: String): String {
        return "https://www.dropbox.com/oauth2/authorize" +
            "?client_id=$appKey" +
            "&response_type=code" +
            "&redirect_uri=$REDIRECT_URI" +
            "&token_access_type=offline"
    }

    /**
     * Open the system browser to start OAuth.
     */
    fun openAuthPage(context: Context, appKey: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getAuthorizationUrl(appKey)))
        context.startActivity(intent)
    }

    /**
     * Parse the authorization code from the redirect URI.
     */
    fun parseAuthCode(uri: Uri): String? {
        return uri.getQueryParameter("code")
    }
}
