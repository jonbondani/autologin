# Arquitectura Tecnica - AutoLogin

## Vision General

App MVVM con Clean Architecture, autenticacion via MSAL broker en **Shared Device Mode**, y persistencia local con Room.

**Dispositivo objetivo**: Samsung WAF Interactive Display 65", Android 14 API 34, GMS habilitado.
**Modo de operacion**: Dispositivo compartido - multiples usuarios hacen login/logout secuencialmente.
**Arquitectura de autenticacion**: MSAL Shared Device Mode (NO Intune MDM enrollment).

---

## Dispositivo

| Parametro | Valor |
|---|---|
| Modelo | Samsung WAF Interactive Display 65" |
| Android | 14 (API 34) |
| GMS | Habilitado |
| Work Profile | NO soportado |
| Intune enrollment | NO compatible |
| Modo | Shared Device Mode via Authenticator + Company Portal |
| Autenticacion | Passwordless con Authenticator number matching |

---

## Stack Tecnologico

```
+---------------------------------------------+
|                 UI Layer                     |
|  Jetpack Compose + Material 3               |
|  Navigation Compose                         |
+---------------------------------------------+
|               ViewModel Layer               |
|  AuthViewModel | HistoryViewModel           |
|  StateFlow -> Compose State                 |
+---------------------------------------------+
|              Domain Layer                    |
|  UseCases: SignIn, SignOut, GetHistory       |
|  Interfaces: AuthRepository, HistoryRepo    |
+---------------------------------------------+
|               Data Layer                    |
|  MsalAuthRepository <- MSAL Android SDK     |
|  RoomHistoryRepository <- Room Database      |
|  GithubUpdateRepository <- GitHub API       |
|  AppDetector <- PackageManager              |
+---------------------------------------------+
|              External                        |
|  MSAL Broker (Authenticator/Company Portal) |
|  Microsoft Entra ID                         |
|  GitHub Releases API (auto-update)          |
+---------------------------------------------+
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
  "client_id": "<ENTRA_CLIENT_ID>",
  "redirect_uri": "msauth://com.autologin.app/<SIGNATURE_HASH_URLENCODED>",
  "broker_redirect_uri_registered": true,
  "authorization_user_agent": "DEFAULT",
  "account_mode": "SINGLE",
  "shared_device_mode_supported": true,
  "authorities": [
    {
      "type": "AAD",
      "audience": {
        "type": "AzureADMyOrg",
        "tenant_id": "<ENTRA_TENANT_ID>"
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
    <uses-permission android:name="android.permission.KILL_BACKGROUND_PROCESSES" />
    <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />

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
                    android:path="/<SIGNATURE_HASH_RAW>"
                    android:scheme="msauth" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

> **IMPORTANTE**: `android:path` usa el hash **raw** (con `/`, `+`, `=`). El `redirect_uri` en `auth_config.json` usa la version **URL-encoded** (`%2F`, `%2B`, `%3D`). Mezclar formatos causa que el redirect no funcione.

---

## Threading

Todas las llamadas a MSAL deben ejecutarse fuera del hilo principal. Usar `Dispatchers.IO`:

```kotlin
// CORRECTO
suspend fun getCurrentAccount(): AccountInfo? = withContext(Dispatchers.IO) {
    msalApp.getCurrentAccount()?.currentAccount?.let { account ->
        AccountInfo(id = account.id, name = account.username, email = account.username)
    }
}

// INCORRECTO - crashea con IllegalStateException
fun getCurrentAccount(): AccountInfo? {
    return msalApp.getCurrentAccount()?.currentAccount?.let { ... }  // Main thread!
}
```

Metodos MSAL que requieren `Dispatchers.IO`:
- `getCurrentAccount()`
- `signIn()`
- `signOut()`
- `acquireTokenSilently()`

---

## Flujo de Autenticacion (Shared Device Mode)

```
Usuario pulsa "Login"
        |
        v
AuthViewModel.signIn()
        |
        v
MsalAuthRepository.signIn(activity)  [Dispatchers.IO]
        |
        v
ISingleAccountPublicClientApplication.signIn(
    activity,
    loginHint = null,
    scopes = ["User.Read"],
    callback
)
        |
        v
MSAL detecta broker instalado -> delega al broker
        |
        v
Broker muestra UI de login de Microsoft
        |
        v
Usuario introduce email -> Authenticator number matching (passwordless)
        |
        v
En el telefono movil, Authenticator muestra notificacion con numero
        |
        v
Usuario introduce el numero en la pantalla Samsung WAF
        |
        v
Broker recibe tokens de Entra ID -> almacena PRT -> global sign-in
        |
        v
Callback onSuccess -> access token + account info
        |
        v
HistoryRepository.recordLogin(email, name)
        |
        v
AuthState -> Authenticated(account)
        |
        v
UI muestra estado "SSO Activo"
        |
        v
Apps SDM-aware (Teams, Edge, M365 Copilot) -> SSO completo automatico
Apps no SDM-aware (Word, Excel, OneDrive, PPT, SharePoint, To Do) -> SSO parcial
```

> **CRITICO**: Se usa `signIn()` y NO `acquireToken()`. La diferencia es que `signIn()` registra un global sign-in en el broker, propagando SSO a todas las apps. `acquireToken()` solo obtiene un token local para la app que lo llama.

---

## Flujo de Logout

```
Usuario pulsa "Cerrar Sesion"
        |
        v
AuthViewModel.signOut()
        |
        v
MsalAuthRepository.signOut()  [Dispatchers.IO]
        |
        v
ISingleAccountPublicClientApplication.signOut(callback)
        |
        v
Broker revoca PRT -> limpia sesion global
        |
        v
killBackgroundProcesses() para apps Microsoft:
  - com.microsoft.teams (Teams)
  - com.microsoft.emmx (Edge)
  - com.microsoft.office.officehubrow (M365 Copilot)
  - com.microsoft.office.word (Word)
  - com.microsoft.office.excel (Excel)
  - com.microsoft.skydrive (OneDrive)
  - com.microsoft.office.powerpoint (PowerPoint)
  - com.microsoft.sharepoint (SharePoint)
  - com.microsoft.todos (To Do)
  - com.microsoft.office.onenote (OneNote)
        |
        v
HistoryRepository.recordLogout(email, name)
        |
        v
AuthState -> Unauthenticated
        |
        v
Otras apps Microsoft -> pierden SSO -> requeriran re-auth
```

> `killBackgroundProcesses()` solo funciona para apps en background. Cuando el usuario esta en AutoLogin haciendo logout, todas las apps Microsoft estan en background. Requiere el permiso `KILL_BACKGROUND_PROCESSES` en el manifest.

---

## Compatibilidad SSO

| App | Tipo SSO | Comportamiento |
|---|---|---|
| Microsoft 365 Copilot | COMPLETO | Login/logout automatico sin intervencion |
| Microsoft Teams | COMPLETO | Login/logout automatico sin intervencion |
| Microsoft Edge | COMPLETO | Login/logout automatico sin intervencion |
| Microsoft Word | PARCIAL | Email visible, usuario confirma, sin contrasena |
| Microsoft Excel | PARCIAL | Email visible, usuario confirma, sin contrasena |
| Microsoft OneDrive | PARCIAL | Email visible, usuario confirma, sin contrasena |
| Microsoft PowerPoint | PARCIAL | Email visible, usuario confirma, sin contrasena |
| Microsoft SharePoint | PARCIAL | Email visible, usuario confirma, sin contrasena |
| Microsoft To Do | PARCIAL | Email visible, usuario confirma, sin contrasena |

**SSO COMPLETO**: La app detecta automaticamente el global sign-in del broker y autentica al usuario sin ninguna intervencion. Son apps "shared device mode aware" que implementan el SDK de SDM.

**SSO PARCIAL**: La app detecta que hay una cuenta disponible via el broker PRT, muestra el email pre-rellenado y permite al usuario confirmar sin introducir contrasena. Son apps que no implementan completamente el SDK de Shared Device Mode.

---

## Autenticacion Passwordless

La cuenta compartida (`shared-screen-account@example.com`) utiliza autenticacion passwordless con Microsoft Authenticator number matching:

1. El usuario introduce el email en la pantalla Samsung WAF
2. Microsoft envia una notificacion push a la app Authenticator en un telefono movil registrado
3. La pantalla muestra un numero de 2 digitos
4. El usuario introduce ese numero en Authenticator del telefono movil
5. La autenticacion se completa

**Requisitos para que funcione**:
- Security Defaults desactivados en el tenant
- Microsoft Authenticator en modo "Sin contrasena" habilitado para todos los usuarios
- La cuenta debe estar registrada en Authenticator del movil como "Cuenta profesional" (icono maletin, NO icono X)
- El inicio de sesion sin contrasena debe estar habilitado en la configuracion de la cuenta en Authenticator

Esto elimina la necesidad de que los usuarios conozcan o introduzcan contrasenas en las pantallas compartidas.

---

## Deteccion de Apps Microsoft

```kotlin
class AppDetector(private val packageManager: PackageManager) {

    private val microsoftApps = mapOf(
        "com.microsoft.teams" to "Microsoft Teams",
        "com.microsoft.emmx" to "Microsoft Edge",
        "com.microsoft.office.officehubrow" to "Microsoft 365 Copilot",
        "com.microsoft.office.outlook" to "Outlook",
        "com.microsoft.skydrive" to "OneDrive",
        "com.microsoft.office.word" to "Word",
        "com.microsoft.office.excel" to "Excel",
        "com.microsoft.office.powerpoint" to "PowerPoint",
        "com.microsoft.sharepoint" to "SharePoint",
        "com.microsoft.todos" to "To Do",
        "com.microsoft.office.onenote" to "OneNote",
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
- Texto: "Inicia sesion para acceder a todas tus apps Microsoft"
- Boton primario: "Iniciar Sesion con Microsoft"
- Seccion inferior: Lista de apps Microsoft detectadas con badges instalado/no instalado

**Estado: Autenticado**
- Nombre del usuario + email
- Badge verde: "Sesion activa"
- Dos columnas de apps:
  - **Izquierda**: "Requiere identificacion" - apps con SSO parcial, boton "Identificate"
  - **Derecha**: "Acceso automatico" - apps con SSO completo, boton "Abrir"
- Cada boton abre la app directamente mediante launch intent
- Boton destructivo: "Cerrar Sesion"
- Texto de advertencia: "Cerrar sesion revocara el acceso en todas las apps"
- Footer: boton "Enviar log de errores a IT", version con hash del commit, copyright Prestige-Expo

**Estado: Cargando**
- CircularProgressIndicator centrado
- Texto: "Autenticando..."

**Estado: Error**
- Icono de advertencia (Warning)
- Mensaje de error
- Boton: "Reintentar"

### 2. Pantalla de Historial (HistoryScreen)

- Filtro por rango de fechas (boton con DateRangePicker)
- LazyColumn con items de AuthEvent:
  - Icono: flecha verde (login) o flecha roja (logout)
  - Texto primario: email del usuario
  - Texto secundario: fecha y hora formateada ("9 Feb 2026, 14:32")
- Empty state: "No hay eventos registrados"

### Navegacion
- BottomNavigation con 2 items:
  - "Sesion" (icono: Lock)
  - "Historial" (icono: DateRange)

### Versionado
- versionCode y versionName generados automaticamente desde el numero de commits git
- Hash corto del commit visible en el footer de la app
- BuildConfig fields: GIT_HASH, BUILD_NUMBER
- APK output: `AutoLogin-v1.0.XX-release.apk`

---

## Auto-Update via GitHub Releases

La app comprueba automaticamente si hay una version nueva al abrirse.

### Flujo
```
App se abre
    |
    v
AuthViewModel.init() -> checkForUpdate() [Dispatchers.IO]
    |
    v
GithubUpdateRepository.checkForUpdate()
    |
    v
GET https://api.github.com/repos/jonbondani/autologin/releases/latest
    |
    v
Parsea tag_name (v<versionCode>), body (release notes), assets[0].browser_download_url
    |
    v
Compara versionCode remoto > BuildConfig.VERSION_CODE
    |
    v
Si hay update -> UpdateState.Available(AppUpdate)
    |
    v
UI muestra boton "Actualizar a v1.0.XX" en AppFooter
    |
    v
Usuario pulsa -> downloadApk() con progreso -> UpdateState.Downloading(progress)
    |
    v
Descarga completa -> UpdateState.ReadyToInstall(file)
    |
    v
LaunchedEffect detecta ReadyToInstall -> installUpdate(context)
    |
    v
FileProvider genera URI -> Intent ACTION_VIEW con application/vnd.android.package-archive
    |
    v
Android Package Installer -> usuario confirma -> app se actualiza
```

### Modelo de datos
```kotlin
data class AppUpdate(
    val versionName: String,    // "AutoLogin v1.0.25"
    val versionCode: Int,       // 25
    val downloadUrl: String,    // URL del asset APK
    val releaseNotes: String,   // Body del release
)

sealed class UpdateState {
    data object NoUpdate : UpdateState()
    data class Available(val update: AppUpdate) : UpdateState()
    data class Downloading(val progress: Int) : UpdateState()
    data class ReadyToInstall(val file: File) : UpdateState()
    data class Error(val message: String) : UpdateState()
}
```

### Convencion para GitHub Releases
- Tag: `v<versionCode>` (ej: `v25`)
- Asset: APK adjunto (cualquier nombre)
- Body: notas de la version

### Dependencias
- Ninguna nueva. Usa `java.net.HttpURLConnection` + `org.json.JSONObject` (ambos incluidos en Android SDK)
- FileProvider de `androidx.core` (ya en el proyecto)

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
