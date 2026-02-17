package com.autologin.app.ui.navigation

sealed class Screen(val route: String, val label: String) {
    data object Login : Screen("login", "Sesion")
    data object History : Screen("history", "Historial")
}
