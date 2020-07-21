package org.edx.mobile.interfaces

/**
 * Provides callbacks for a date block in course to load its contents on webview.
 */
interface OnDateBlockListener {
    fun onClick(link: String)
}