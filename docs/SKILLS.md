# Skills y Competencias Técnicas - AutoLogin

## Competencias Requeridas por el Proyecto

---

### Desarrollo Android

| Skill | Nivel Requerido | Descripción |
|---|---|---|
| Kotlin | Avanzado | Lenguaje principal: coroutines, flows, sealed classes |
| Jetpack Compose | Intermedio-Avanzado | UI declarativa, state management, navigation |
| Material Design 3 | Intermedio | Theming, componentes, responsive layout |
| Room Database | Intermedio | Entities, DAOs, migrations, type converters |
| Hilt (Dagger) | Intermedio | Dependency injection, modules, scoping |
| MVVM Architecture | Avanzado | ViewModels, StateFlow, unidirectional data flow |
| Android PackageManager | Básico | Detección de apps instaladas |
| Android Lifecycle | Intermedio | Activity/Fragment lifecycle con Compose |

---

### Microsoft Identity Platform

| Skill | Nivel Requerido | Descripción |
|---|---|---|
| MSAL Android SDK | Avanzado | Broker auth, token management, single account mode |
| OAuth 2.0 / OpenID Connect | Intermedio | Authorization code flow with PKCE |
| Microsoft Entra ID (Azure AD) | Intermedio | App registration, API permissions, admin consent |
| Primary Refresh Token (PRT) | Conceptual | Entender el mecanismo de SSO device-wide |
| Conditional Access | Básico-Intermedio | Creación y configuración de policies |

---

### Microsoft Intune / MDM

| Skill | Nivel Requerido | Descripción |
|---|---|---|
| Device Enrollment | Básico | Android Enterprise work profile / fully managed |
| Compliance Policies | Intermedio | Definición de requisitos de dispositivo |
| App Protection Policies (MAM) | Intermedio | Protección de datos corporativos |
| App Distribution (LOB) | Básico | Subir y asignar apps via Intune |
| Managed Google Play | Básico | Publicación de apps privadas |

---

### Infraestructura y DevOps

| Skill | Nivel Requerido | Descripción |
|---|---|---|
| Android Keystore Management | Básico | Debug/release keystores, signature hashes |
| Gradle (Kotlin DSL) | Intermedio | Build configuration, dependencies, signing |
| ProGuard/R8 | Básico | Ofuscación y shrinking para release |
| Git | Intermedio | Versionado, branching, PRs |

---

### Testing

| Skill | Nivel Requerido | Descripción |
|---|---|---|
| JUnit 4/5 | Intermedio | Unit tests |
| Mockito / MockK | Intermedio | Mocking de dependencias (MSAL, Room) |
| Kotlin Coroutines Test | Intermedio | Testing de código asíncrono |
| Room In-Memory Testing | Básico | Tests de DAOs con DB en memoria |
| Manual QA Android | Básico | Testing en dispositivo real |

---

### Seguridad

| Skill | Nivel Requerido | Descripción |
|---|---|---|
| OAuth 2.0 Security | Intermedio | PKCE, token storage, redirect URI validation |
| Android App Security | Básico | Network security config, certificate pinning |
| Enterprise Data Protection | Conceptual | DLP policies, data boundaries |

---

## Recursos de Aprendizaje

### MSAL Android
- [Microsoft MSAL Android Guide](https://learn.microsoft.com/en-us/entra/msal/android/)
- [MSAL Android GitHub (samples)](https://github.com/AzureAD/microsoft-authentication-library-for-android)
- [Android SSO with Brokers](https://learn.microsoft.com/en-us/entra/msal/android/single-sign-on)

### Jetpack Compose
- [Compose Basics Codelab](https://developer.android.com/codelabs/jetpack-compose-basics)
- [Material 3 Compose](https://developer.android.com/jetpack/compose/designsystems/material3)

### Entra ID
- [App Registration Quickstart](https://learn.microsoft.com/en-us/entra/identity-platform/quickstart-register-app)
- [Conditional Access Documentation](https://learn.microsoft.com/en-us/entra/identity/conditional-access/overview)

### Intune
- [Android Enterprise Enrollment](https://learn.microsoft.com/en-us/mem/intune/enrollment/android-enroll)
- [App Protection Policies](https://learn.microsoft.com/en-us/mem/intune/apps/app-protection-policies)
