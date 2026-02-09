# Plan de Proyecto - AutoLogin

## Resumen

App Android que actúa como punto único de autenticación Microsoft 365, habilitando SSO device-wide a través del mecanismo broker/PRT de Microsoft.

---

## Fase 0: Configuración del Entorno Microsoft (Pre-desarrollo)

> **Esta fase es BLOQUEANTE. No se puede escribir código funcional sin completarla.**

### 0.1 Registro de App en Entra ID
- [ ] Acceder a [Microsoft Entra Admin Center](https://entra.microsoft.com)
- [ ] Crear App Registration con nombre "AutoLogin"
- [ ] Configurar plataforma Android (package name + signature hash)
- [ ] Configurar permisos delegados: `openid`, `profile`, `offline_access`, `User.Read`
- [ ] Habilitar "Allow public client flows"
- [ ] Anotar: Client ID, Tenant ID, Redirect URI

**Guía detallada**: [docs/ENTRA_ID_CONFIGURATION.md](ENTRA_ID_CONFIGURATION.md)

### 0.2 Configuración de Intune
- [ ] Verificar que los dispositivos están enrollados en Intune
- [ ] Configurar Compliance Policy para Android Enterprise
- [ ] Crear App Protection Policy
- [ ] Configurar Conditional Access Policy que requiera dispositivo compliant

**Guía detallada**: [docs/INTUNE_CONFIGURATION.md](INTUNE_CONFIGURATION.md)

### 0.3 Dispositivo de Pruebas
- [ ] Dispositivo Android 7.0+ con Company Portal instalado
- [ ] Dispositivo enrollado en Intune
- [ ] Cuenta de usuario de prueba con licencia Business Premium
- [ ] Microsoft Teams y Outlook instalados (para verificar SSO)

**Entregable Fase 0**: Client ID, Tenant ID, Redirect URI documentados. Dispositivo de pruebas listo.

---

## Fase 1: Scaffold del Proyecto Android

### 1.1 Inicialización
- [ ] Crear proyecto Android con Android Studio (Kotlin + Compose)
- [ ] Configurar `build.gradle.kts` con dependencias:
  - MSAL Android v8.x
  - Jetpack Compose + Material 3
  - Room Database
  - Hilt
  - Navigation Compose
- [ ] Configurar `minSdk = 24`, `targetSdk = 35`

### 1.2 Configuración MSAL
- [ ] Crear `res/raw/auth_config.json` con Client ID, Tenant ID, Redirect URI
- [ ] Configurar `broker_redirect_uri_registered: true`
- [ ] Configurar `account_mode: "SINGLE"`
- [ ] Añadir permisos en `AndroidManifest.xml` (INTERNET, ACCESS_NETWORK_STATE)

### 1.3 Estructura Base
- [ ] Crear paquetes: `di/`, `data/`, `domain/`, `ui/`
- [ ] Configurar Hilt Application class
- [ ] Configurar Navigation graph con 2 pantallas: Login, History

**Entregable Fase 1**: Proyecto compila y se instala en dispositivo.

---

## Fase 2: Autenticación con MSAL + Broker

### 2.1 Capa de Autenticación
- [ ] Crear `AuthRepository` interface
- [ ] Implementar `MsalAuthRepository`:
  - `signIn()`: Llama `acquireToken()` via broker
  - `signOut()`: Llama `signOut()` + limpia PRT del broker
  - `getAccount()`: Obtiene cuenta actual del broker
  - `isLoggedIn()`: Verifica si hay sesión activa
- [ ] Crear Hilt module para proveer `AuthRepository`

### 2.2 ViewModel de Autenticación
- [ ] Crear `AuthViewModel` con estados: `Idle`, `Loading`, `LoggedIn(account)`, `LoggedOut`, `Error(message)`
- [ ] Manejar callback de MSAL (onSuccess, onError, onCancel)

### 2.3 Verificación de Broker
- [ ] Al iniciar app, verificar que hay un broker instalado
- [ ] Si no hay broker: mostrar mensaje + botón para instalar Company Portal / Authenticator
- [ ] Si hay broker: proceder normalmente

**Entregable Fase 2**: Login/logout funcional. Al hacer login, Teams/Outlook obtienen SSO automáticamente.

---

## Fase 3: Base de Datos e Historial

### 3.1 Room Database
- [ ] Crear entity `AuthEvent`:
  ```kotlin
  @Entity
  data class AuthEvent(
      @PrimaryKey(autoGenerate = true) val id: Long = 0,
      val type: AuthEventType,  // LOGIN, LOGOUT
      val userEmail: String,
      val userName: String,
      val timestamp: Long,      // epoch millis
  )
  ```
- [ ] Crear `AuthEventDao` con queries:
  - `getAll()`: Flow<List<AuthEvent>> ordenado por timestamp DESC
  - `insert(event)`
  - `getByDateRange(start, end)`: Filtrar por rango de fechas
- [ ] Crear `AuthEventDatabase`

### 3.2 Apps Microsoft Detectadas
- [ ] Crear entity `DetectedApp`:
  ```kotlin
  data class DetectedApp(
      val packageName: String,
      val appName: String,
      val isInstalled: Boolean,
      val supportsBrokerSSO: Boolean,
  )
  ```
- [ ] Implementar `AppDetector` que escanea apps Microsoft instaladas:
  - `com.microsoft.teams`
  - `com.microsoft.office.outlook`
  - `com.microsoft.skydrive` (OneDrive)
  - `com.microsoft.office.word`
  - `com.microsoft.office.excel`
  - `com.microsoft.office.powerpoint`
  - `com.microsoft.sharepoint`
  - `com.microsoft.todos`

### 3.3 Repository de Historial
- [ ] Crear `HistoryRepository` interface
- [ ] Implementar `RoomHistoryRepository`
- [ ] Registrar evento LOGIN al completar `signIn()` exitoso
- [ ] Registrar evento LOGOUT al completar `signOut()` exitoso

**Entregable Fase 3**: Historial persistente de login/logout con timestamps.

---

## Fase 4: UI con Jetpack Compose

### 4.1 Pantalla Principal (Login/Status)
- [ ] Estado no autenticado:
  - Logo de la app
  - Botón "Iniciar Sesión con Microsoft"
  - Lista de apps Microsoft detectadas en el dispositivo
- [ ] Estado autenticado:
  - Nombre y email del usuario
  - Badge "SSO Activo"
  - Lista de apps Microsoft con indicador de SSO disponible
  - Botón "Cerrar Sesión"
- [ ] Estado de carga (durante autenticación)
- [ ] Estado de error con mensaje y botón de reintento

### 4.2 Pantalla de Historial
- [ ] Lista de eventos login/logout con:
  - Icono (login verde, logout rojo)
  - Email del usuario
  - Fecha y hora formateada
- [ ] Filtro por rango de fechas (DateRangePicker)
- [ ] Estado vacío cuando no hay historial

### 4.3 Navegación
- [ ] Bottom navigation con 2 tabs: "SSO" y "Historial"
- [ ] Material 3 theming con colores corporativos

**Entregable Fase 4**: UI completa y funcional.

---

## Fase 5: Testing y QA

### 5.1 Unit Tests
- [ ] Tests de `AuthRepository` (mock MSAL)
- [ ] Tests de `HistoryRepository` (in-memory Room DB)
- [ ] Tests de ViewModels
- [ ] Tests de `AppDetector`

### 5.2 Integration Tests
- [ ] Test de flujo completo login → historial registrado
- [ ] Test de flujo completo logout → historial registrado
- [ ] Test de detección de apps Microsoft

### 5.3 Manual QA
- [ ] Verificar SSO en Teams después de login en AutoLogin
- [ ] Verificar SSO en Outlook después de login en AutoLogin
- [ ] Verificar que logout revoca SSO en todas las apps
- [ ] Verificar historial correcto con timestamps
- [ ] Verificar comportamiento sin broker instalado
- [ ] Verificar comportamiento sin conexión a internet
- [ ] Verificar en Android 7.0 (min) y Android 14+ (latest)

**Entregable Fase 5**: App testeada y validada.

---

## Fase 6: Preparación para Distribución

### 6.1 Build de Release
- [ ] Configurar signing config con keystore de release
- [ ] Generar signature hash de release para Entra ID
- [ ] Actualizar redirect URI en Entra ID con hash de release
- [ ] Generar APK/AAB de release
- [ ] Proguard/R8 rules para MSAL

### 6.2 Distribución via Intune
- [ ] Subir APK a Intune como Line-of-Business app
- [ ] Asignar a grupo de usuarios/dispositivos
- [ ] Configurar como app requerida o disponible

**Entregable Fase 6**: App distribuida y funcional en dispositivos empresariales.

---

## Dependencias entre Fases

```
Fase 0 (Entra ID + Intune) ──► Fase 1 (Scaffold) ──► Fase 2 (Auth)
                                                           │
                                                           ▼
                                    Fase 4 (UI) ◄── Fase 3 (DB/Historial)
                                         │
                                         ▼
                                    Fase 5 (Testing) ──► Fase 6 (Release)
```

---

## Criterios de Éxito

1. Usuario abre AutoLogin → pulsa Login → se autentica una vez
2. Usuario abre Teams → **entra directamente sin pedir credenciales**
3. Usuario abre Outlook → **entra directamente sin pedir credenciales**
4. Historial muestra fecha/hora exactas de login
5. Usuario pulsa Logout → SSO revocado en todas las apps
6. Historial muestra fecha/hora exactas de logout
