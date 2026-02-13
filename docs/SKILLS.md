# Skills y Competencias Tecnicas - AutoLogin

## Competencias Requeridas por el Proyecto

---

### Desarrollo Android

| Skill | Nivel Requerido | Descripcion |
|---|---|---|
| Kotlin | Avanzado | Coroutines, flows, sealed classes, withContext |
| Jetpack Compose | Intermedio-Avanzado | UI declarativa, state management, navigation |
| Material Design 3 | Intermedio | Theming, componentes, iconos (Warning, Lock, DateRange) |
| Room Database | Intermedio | Entities, DAOs, migrations, type converters |
| Hilt (Dagger) | Intermedio | Dependency injection, modules, scoping |
| MVVM Architecture | Avanzado | ViewModels, StateFlow, unidirectional data flow |
| Android PackageManager | Basico | Deteccion de apps instaladas |
| Android Lifecycle | Intermedio | Activity/Fragment lifecycle con Compose |
| ActivityManager | Basico | killBackgroundProcesses() para cierre de sesion |
| Android Resources | Basico | Vectores XML, mipmap fallbacks, resolucion de iconos |

---

### Microsoft Identity Platform

| Skill | Nivel Requerido | Descripcion |
|---|---|---|
| MSAL Android SDK | Avanzado | Broker auth, signIn()/signOut(), single account mode, shared device mode |
| OAuth 2.0 / OpenID Connect | Intermedio | Authorization code flow with PKCE |
| Microsoft Entra ID (Azure AD) | Intermedio | App registration, API permissions, admin consent |
| Primary Refresh Token (PRT) | Intermedio | Mecanismo de SSO device-wide, propagacion via broker |
| Shared Device Mode | Avanzado | Activacion, global sign-in/sign-out, compatibilidad de apps |
| Autenticacion Passwordless | Intermedio | Number matching, configuracion de Authenticator, troubleshooting |
| Metodos de Autenticacion Entra ID | Intermedio | Configuracion de directivas, modos passwordless, OTP, reset de MFA |

---

### Samsung WAF Interactive Display

| Skill | Nivel Requerido | Descripcion |
|---|---|---|
| Limitaciones del dispositivo | Intermedio | No soporta Work Profile, no soporta Intune enrollment |
| Shared Device Mode en displays | Avanzado | Activacion via Authenticator, requisito de Company Portal |
| GMS en dispositivos no-phone | Basico | Google Play Store funcional, limitaciones de Android Enterprise |
| Sideload de APKs | Basico | Instalacion via USB o descarga directa |

---

### Autenticacion y Seguridad

| Skill | Nivel Requerido | Descripcion |
|---|---|---|
| OAuth 2.0 Security | Intermedio | PKCE, token storage, redirect URI validation |
| Passwordless Authentication | Intermedio | Microsoft Authenticator, number matching, push notifications |
| Signature Hash Management | Intermedio | Debug vs release keystores, hash raw vs URL-encoded |
| Redirect URI Configuration | Intermedio | Formato msauth://, diferencias entre auth_config.json y AndroidManifest.xml |
| App Protection Policies (MAM) | Basico | Entender conflictos con Shared Device Mode, exclusion de scope |

---

### Infraestructura y DevOps

| Skill | Nivel Requerido | Descripcion |
|---|---|---|
| Android Keystore Management | Intermedio | Debug/release keystores, signature hashes, keytool + openssl |
| Gradle (Kotlin DSL) | Intermedio | Build configuration, dependencies, signing, Azure DevOps feed |
| ProGuard/R8 | Basico | Ofuscacion y shrinking para release, reglas para MSAL |
| Git | Intermedio | Versionado, branching, PRs |

---

### Testing

| Skill | Nivel Requerido | Descripcion |
|---|---|---|
| JUnit 4/5 | Intermedio | Unit tests |
| Mockito / MockK | Intermedio | Mocking de dependencias (MSAL, Room) |
| Kotlin Coroutines Test | Intermedio | Testing de codigo asincrono con Dispatchers.IO |
| Room In-Memory Testing | Basico | Tests de DAOs con DB en memoria |
| Manual QA en Samsung WAF | Intermedio | Testing en dispositivo real, verificacion de SSO, passwordless |

---

## Skills Aprendidos Durante el Proyecto

Competencias adquiridas durante el desarrollo que no estaban previstas inicialmente.

### SDM-001: Shared Device Mode es la solucion para dispositivos sin Work Profile
- **Situacion**: Samsung WAF no soporta Work Profile ni Intune enrollment
- **Aprendizaje**: Microsoft Shared Device Mode via Authenticator es una alternativa completa que no requiere MDM. Proporciona global sign-in/sign-out sin factory reset.
- **Aplicabilidad**: Cualquier dispositivo Android compartido que no soporte Android Enterprise

### SDM-002: signIn() vs acquireToken() - diferencia critica en SDM
- **Situacion**: SSO no propagaba a otras apps usando acquireToken()
- **Aprendizaje**: En Shared Device Mode, `signIn()` registra un global sign-in que propaga SSO a todas las apps. `acquireToken()` solo obtiene un token local. Esta distincion no es obvia en la documentacion.
- **Aplicabilidad**: Cualquier implementacion de MSAL con Shared Device Mode

### SDM-003: Company Portal es obligatorio aunque no se use Intune
- **Situacion**: Shared Device Mode no se activaba con solo Authenticator
- **Aprendizaje**: Aunque no se usa Intune enrollment, Company Portal debe estar instalado junto con Authenticator para que el registro de dispositivo compartido funcione.
- **Aplicabilidad**: Setup de cualquier dispositivo con Shared Device Mode

### AUTH-001: Configuracion de passwordless requiere flujo especifico
- **Situacion**: Authenticator no ofrecia opcion de passwordless
- **Aprendizaje**: La cuenta debe registrarse como "Cuenta profesional" via QR (icono maletin), NO como TOTP (icono X). Si se registra incorrectamente, hay que eliminar y re-registrar. El "reset nuclear" de MFA en Entra ID siempre resuelve el problema.
- **Aplicabilidad**: Configuracion de passwordless para cualquier usuario de Entra ID

### AUTH-002: Hash raw vs URL-encoded en dos ubicaciones diferentes
- **Situacion**: BrowserTabActivity no interceptaba el redirect de autenticacion
- **Aprendizaje**: `AndroidManifest.xml` usa hash raw (`/`, `+`, `=`); `auth_config.json` usa URL-encoded (`%2F`, `%2B`, `%3D`). Mezclarlos causa errores silenciosos.
- **Aplicabilidad**: Cualquier app Android con MSAL

### AUTH-003: Debug vs release keystore generan hashes diferentes
- **Situacion**: La app en debug no podia autenticarse con el hash registrado
- **Aprendizaje**: Cada keystore genera un signature hash unico. MSAL muestra el hash correcto en su mensaje de error - usarlo para actualizar Entra ID.
- **Aplicabilidad**: Cualquier app Android con autenticacion basada en signature hash

### THREAD-001: MSAL no puede ejecutarse en el hilo principal
- **Situacion**: Crash al llamar getCurrentAccount() desde el main thread
- **Aprendizaje**: Todos los metodos de MSAL que interactuan con el broker (`getCurrentAccount()`, `signIn()`, `signOut()`) deben ejecutarse en `Dispatchers.IO`. El crash es un `IllegalStateException` no documentado.
- **Aplicabilidad**: Cualquier app Android con MSAL en coroutines

### MAM-001: App Protection Policies bloquean SSO en dispositivos compartidos
- **Situacion**: SSO no funcionaba, Teams pedia PIN
- **Aprendizaje**: Las politicas MAM requieren sesiones protegidas individuales (PIN/biometrics) que conflictuan con el global sign-in/sign-out de Shared Device Mode. Hay que excluir dispositivos compartidos del scope.
- **Aplicabilidad**: Cualquier despliegue de Shared Device Mode con Intune MAM activo

### DEVICE-001: Samsung WAF no soporta Android Enterprise
- **Situacion**: Error "No se puede anadir perfil de trabajo"
- **Aprendizaje**: Las pantallas interactivas Samsung WAF, aunque ejecutan Android 14 con GMS, no implementan el stack de Android Enterprise. No es posible enrollar en Intune ni crear perfiles de trabajo.
- **Aplicabilidad**: Evaluacion de cualquier dispositivo no-phone para enterprise deployment

---

## Recursos de Aprendizaje

### MSAL Android
- [Microsoft MSAL Android Guide](https://learn.microsoft.com/en-us/entra/msal/android/)
- [MSAL Android GitHub (samples)](https://github.com/AzureAD/microsoft-authentication-library-for-android)
- [Android SSO with Brokers](https://learn.microsoft.com/en-us/entra/msal/android/single-sign-on)
- [Shared Device Mode for Android](https://learn.microsoft.com/en-us/entra/identity-platform/msal-android-shared-devices)

### Autenticacion Passwordless
- [Passwordless Authentication Methods](https://learn.microsoft.com/en-us/entra/identity/authentication/concept-authentication-passwordless)
- [Microsoft Authenticator - Passwordless](https://learn.microsoft.com/en-us/entra/identity/authentication/howto-authentication-passwordless-phone)

### Jetpack Compose
- [Compose Basics Codelab](https://developer.android.com/codelabs/jetpack-compose-basics)
- [Material 3 Compose](https://developer.android.com/jetpack/compose/designsystems/material3)

### Entra ID
- [App Registration Quickstart](https://learn.microsoft.com/en-us/entra/identity-platform/quickstart-register-app)
- [Authentication Methods Configuration](https://learn.microsoft.com/en-us/entra/identity/authentication/concept-authentication-methods-manage)
