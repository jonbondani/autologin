# Arquitectura Técnica - AutoLogin

## Visión General

App MVVM con Clean Architecture, autenticación via MSAL broker, y persistencia local con Room.

---

## Stack Tecnológico

```
┌─────────────────────────────────────────────┐
│                 UI Layer                     │
│  Jetpack Compose + Material 3               │
│  Navigation Compose                         │
├─────────────────────────────────────────────┤
│               ViewModel Layer               │
│  AuthViewModel │ HistoryViewModel           │
│  StateFlow → Compose State                  │
├─────────────────────────────────────────────┤
│              Domain Layer                    │
│  UseCases: SignIn, SignOut, GetHistory       │
│  Interfaces: AuthRepository, HistoryRepo    │
├─────────────────────────────────────────────┤
│               Data Layer                    │
│  MsalAuthRepository ← MSAL Android SDK     │
│  RoomHistoryRepository ← Room Database      │
│  AppDetector ← PackageManager              │
├─────────────────────────────────────────────┤
│              External                        │
│  MSAL Broker (Authenticator/Company Portal) │
│  Microsoft Entra ID                         │
└─────────────────────────────────────────────┘
```

---

## Dependencias Gradle

```kotlin
// build.gradle.kts (app)
dependencies {
    // MSAL
    implementation("com.microsoft.identity.client:msal:8.+")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    androidTestImplementation("androidx.room:room-testing:2.6.1")
}
```

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://pkgs.dev.azure.com/MicrosoftDeviceSDK/DuoSDK-Public/_packaging/Duo-SDK-Feed/maven/v1") }
    }
}
```

---

## Modelo de Datos

### AuthEvent (Room Entity)

```kotlin
@Entity(tableName = "auth_events")
data class AuthEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,         // "LOGIN" o "LOGOUT"
    val userEmail: String,
    val userName: String,
    val timestamp: Long,      // System.currentTimeMillis()
)
```

### DetectedApp (Data class, no persistido)

```kotlin
data class DetectedApp(
    val packageName: String,
    val appName: String,
    val isInstalled: Boolean,
)
```

---

## Interfaces de Repositorio

```kotlin
interface AuthRepository {
    val authState: StateFlow<AuthState>
    suspend fun signIn(activity: Activity): Result<AccountInfo>
    suspend fun signOut(): Result<Unit>
    fun getAccount(): AccountInfo?
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val account: AccountInfo) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

data class AccountInfo(
    val id: String,
    val name: String,
    val email: String,
)
```

```kotlin
interface HistoryRepository {
    fun getAllEvents(): Flow<List<AuthEvent>>
    fun getEventsByDateRange(startMillis: Long, endMillis: Long): Flow<List<AuthEvent>>
    suspend fun recordLogin(email: String, name: String)
    suspend fun recordLogout(email: String, name: String)
}
```

---

## MSAL Configuration

### res/raw/auth_config.json
```json
{
  "client_id": "<CLIENT_ID>",
  "redirect_uri": "msauth://com.autologin.app/<SIGNATURE_HASH>",
  "broker_redirect_uri_registered": true,
  "authorization_user_agent": "DEFAULT",
  "account_mode": "SINGLE",
  "authorities": [
    {
      "type": "AAD",
      "audience": {
        "type": "AzureADMyOrg",
        "tenant_id": "<TENANT_ID>"
      },
      "default": true
    }
  ]
}
```

### AndroidManifest.xml (permisos y config)
```xml
<manifest>
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:name=".AutoLoginApplication"
        ...>

        <activity
            android:name="com.microsoft.identity.client.BrowserTabActivity">
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data
                    android:host="com.autologin.app"
                    android:path="/<SIGNATURE_HASH>"
                    android:scheme="msauth" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

---

## Flujo de Autenticación

```
Usuario pulsa "Login"
        │
        ▼
AuthViewModel.signIn()
        │
        ▼
MsalAuthRepository.signIn(activity)
        │
        ▼
ISingleAccountPublicClientApplication.acquireToken(
    activity,
    scopes = ["User.Read"],
    callback
)
        │
        ▼
MSAL detecta broker instalado → delega al broker
        │
        ▼
Broker muestra UI de login de Microsoft (WebView/Chrome)
        │
        ▼
Usuario introduce credenciales + MFA (si aplica)
        │
        ▼
Broker recibe tokens de Entra ID → almacena PRT
        │
        ▼
Callback onSuccess → access token + account info
        │
        ▼
HistoryRepository.recordLogin(email, name)
        │
        ▼
AuthState → Authenticated(account)
        │
        ▼
UI muestra estado "SSO Activo"
        │
        ▼
Otras apps Microsoft → broker proporciona tokens silenciosamente → SSO
```

---

## Flujo de Logout

```
Usuario pulsa "Logout"
        │
        ▼
AuthViewModel.signOut()
        │
        ▼
MsalAuthRepository.signOut()
        │
        ▼
ISingleAccountPublicClientApplication.signOut(callback)
        │
        ▼
Broker revoca PRT → limpia sesión
        │
        ▼
HistoryRepository.recordLogout(email, name)
        │
        ▼
AuthState → Unauthenticated
        │
        ▼
Otras apps Microsoft → pierden SSO → requerirán re-auth
```

---

## Detección de Apps Microsoft

```kotlin
class AppDetector(private val packageManager: PackageManager) {

    private val microsoftApps = mapOf(
        "com.microsoft.teams" to "Microsoft Teams",
        "com.microsoft.office.outlook" to "Outlook",
        "com.microsoft.skydrive" to "OneDrive",
        "com.microsoft.office.word" to "Word",
        "com.microsoft.office.excel" to "Excel",
        "com.microsoft.office.powerpoint" to "PowerPoint",
        "com.microsoft.sharepoint" to "SharePoint",
        "com.microsoft.todos" to "To Do",
        "com.azure.authenticator" to "Authenticator",
        "com.microsoft.windowsintune.companyportal" to "Company Portal",
    )

    fun getDetectedApps(): List<DetectedApp> {
        return microsoftApps.map { (pkg, name) ->
            DetectedApp(
                packageName = pkg,
                appName = name,
                isInstalled = isAppInstalled(pkg),
            )
        }
    }

    private fun isAppInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}
```

---

## Pantallas UI

### 1. Pantalla Principal (LoginScreen)

**Estado: No autenticado**
- Logo AutoLogin (centrado)
- Texto: "Inicia sesión para activar SSO en todas tus apps Microsoft"
- Botón primario: "Iniciar Sesión con Microsoft" (icono M365)
- Sección inferior: Lista de apps Microsoft detectadas con badges instalado/no instalado

**Estado: Autenticado**
- Nombre del usuario + email
- Badge verde: "SSO Activo"
- Lista de apps con SSO (solo las instaladas, con check verde)
- Botón destructivo: "Cerrar Sesión"
- Texto de advertencia: "Cerrar sesión revocará el SSO en todas las apps"

**Estado: Cargando**
- CircularProgressIndicator centrado
- Texto: "Autenticando..."

**Estado: Error**
- Icono de error
- Mensaje de error
- Botón: "Reintentar"

### 2. Pantalla de Historial (HistoryScreen)

- Filtro por rango de fechas (botón con DateRangePicker)
- LazyColumn con items de AuthEvent:
  - Icono: flecha verde (login) o flecha roja (logout)
  - Texto primario: email del usuario
  - Texto secundario: fecha y hora formateada ("9 Feb 2026, 14:32")
- Empty state: "No hay eventos registrados"

### Navegación
- BottomNavigation con 2 items:
  - "SSO" (icono: shield/key)
  - "Historial" (icono: list/clock)

---

## Proguard Rules para MSAL

```proguard
# MSAL
-keep class com.microsoft.identity.** { *; }
-keep class com.microsoft.aad.** { *; }
-dontwarn com.microsoft.identity.**
-dontwarn com.microsoft.aad.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
```
