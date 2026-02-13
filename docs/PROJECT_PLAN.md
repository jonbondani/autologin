# Plan de Proyecto - AutoLogin

## Resumen

App Android que actua como punto unico de autenticacion Microsoft 365, habilitando SSO device-wide a traves del mecanismo broker/PRT de Microsoft en pantallas compartidas Samsung WAF Interactive Display.

**Modo de operacion**: Shared Device Mode via Microsoft Authenticator (NO Intune enrollment).

---

## Estado General

| Sprint | Estado | Fecha |
|---|---|---|
| Sprint 0: Configuracion Entra ID | COMPLETADO | 2026-02-10 |
| Sprint 1: Scaffold del Proyecto | COMPLETADO | 2026-02-11 |
| Sprint 2: Autenticacion MSAL + SSO | COMPLETADO | 2026-02-12 |
| Sprint 3: Base de Datos e Historial | PENDIENTE | - |
| Sprint 4: UI con Jetpack Compose | PENDIENTE | - |
| Sprint 5: Testing y QA | PENDIENTE | - |
| Sprint 6: Release y Distribucion | PENDIENTE | - |

---

## Fase 0: Configuracion del Entorno Microsoft (Pre-desarrollo) - COMPLETADO

> **Esta fase es BLOQUEANTE. No se puede escribir codigo funcional sin completarla.**

### 0.1 Registro de App en Entra ID - COMPLETADO
- [x] Acceder a [Microsoft Entra Admin Center](https://entra.microsoft.com)
- [x] Crear App Registration con nombre "AutoLogin"
- [x] Configurar plataforma Android (package name + signature hash)
- [x] Configurar permisos delegados: `openid`, `profile`, `offline_access`, `User.Read`
- [x] Habilitar "Allow public client flows"
- [x] Anotar: Client ID, Tenant ID, Redirect URI

**Guia detallada**: [docs/ENTRA_ID_CONFIGURATION.md](ENTRA_ID_CONFIGURATION.md)

### ~~0.2 Configuracion de Intune~~ - DESCARTADO
> **DESCARTADO**: Samsung WAF Interactive Display no soporta Work Profile ni enrollment en Intune. Se sustituyo por Shared Device Mode via Authenticator. Ver [docs/INTUNE_CONFIGURATION.md](INTUNE_CONFIGURATION.md) (OBSOLETO).

- ~~Verificar que los dispositivos estan enrollados en Intune~~
- ~~Configurar Compliance Policy para Android Enterprise~~
- ~~Crear App Protection Policy~~
- ~~Configurar Conditional Access Policy que requiera dispositivo compliant~~

**Cambio**: Se implemento Shared Device Mode en su lugar:
- [x] Instalar Authenticator + Company Portal en Samsung WAF
- [x] Registrar como dispositivo compartido con cuenta Cloud Device Admin
- [x] Desactivar Security Defaults
- [x] Eliminar Conditional Access policy de compliance
- [x] Excluir App Protection Policies (MAM) de dispositivos compartidos

### 0.3 Dispositivo de Pruebas - COMPLETADO
- [x] Samsung WAF Interactive Display 65", Android 14 API 34 con GMS
- [x] Authenticator registrado como dispositivo compartido (Device ID: f3a76792-04eb-4ac8-a42b-aa431ccea198)
- [x] Company Portal instalado (sin enrollment - solo presencia)
- [x] Cuenta compartida de prueba con licencia Business Premium: `pantallas@prestige-expo.com`
- [x] Teams, M365 Copilot y Edge instalados para verificar SSO

### 0.4 Autenticacion Passwordless - COMPLETADO
- [x] Configurar Authenticator en modo "Sin contrasena" en el tenant
- [x] Registrar Authenticator en telefono movil para cuenta `pantallas@prestige-expo.com`
- [x] Habilitar inicio de sesion sin contrasena (number matching)
- [x] Verificar flujo completo de passwordless en la pantalla Samsung WAF

**Entregable Fase 0**: Client ID, Tenant ID, Redirect URI documentados. Dispositivo de pruebas configurado con Shared Device Mode y passwordless.

---

## Fase 1: Scaffold del Proyecto Android - COMPLETADO

### 1.1 Inicializacion - COMPLETADO
- [x] Crear proyecto Android con Android Studio (Kotlin + Compose)
- [x] Configurar `build.gradle.kts` con dependencias:
  - MSAL Android v8.x
  - Jetpack Compose + Material 3
  - Room Database
  - Hilt
  - Navigation Compose
- [x] Configurar `minSdk = 24`, `targetSdk = 35`

### 1.2 Configuracion MSAL - COMPLETADO
- [x] Crear `res/raw/auth_config.json` con Client ID, Tenant ID, Redirect URI
- [x] Configurar `broker_redirect_uri_registered: true`
- [x] Configurar `account_mode: "SINGLE"`, `shared_device_mode_supported: true`
- [x] Anadir permisos en `AndroidManifest.xml` (INTERNET, ACCESS_NETWORK_STATE, KILL_BACKGROUND_PROCESSES)
- [x] Configurar BrowserTabActivity con hash raw en android:path

### 1.3 Estructura Base - COMPLETADO
- [x] Crear paquetes: `di/`, `data/local/`, `data/repository/`, `domain/model/`, `domain/repository/`, `ui/`
- [x] Configurar Hilt Application class
- [x] Configurar Navigation graph con 2 pantallas: Login, History

**Entregable Fase 1**: Proyecto compila y se instala en dispositivo.

---

## Fase 2: Autenticacion con MSAL + Broker - COMPLETADO

### 2.1 Capa de Autenticacion - COMPLETADO
- [x] Crear `AuthRepository` interface
- [x] Implementar `MsalAuthRepository`:
  - `signIn()`: Usa `signIn()` de MSAL (NO `acquireToken()`) para global SSO
  - `signOut()`: Usa `signOut()` + `killBackgroundProcesses()` para limpiar apps Microsoft
  - `getAccount()`: Obtiene cuenta actual del broker (en Dispatchers.IO)
- [x] Crear Hilt module para proveer `AuthRepository`

> **CAMBIO vs PLAN ORIGINAL**: Se cambio de `acquireToken()` a `signIn()`. `acquireToken()` solo obtiene un token local; `signIn()` registra un global sign-in que propaga SSO a todas las apps.

### 2.2 ViewModel de Autenticacion - COMPLETADO
- [x] Crear `AuthViewModel` con estados: `Idle`, `Loading`, `Authenticated(account)`, `Unauthenticated`, `Error(message)`
- [x] Manejar callback de MSAL (onSuccess, onError, onCancel)
- [x] Todas las llamadas MSAL en `Dispatchers.IO` (crash si se ejecutan en main thread)

### 2.3 Verificacion de Broker - COMPLETADO
- [x] Al iniciar app, verificar que hay un broker instalado
- [x] Si no hay broker: mostrar mensaje + instrucciones
- [x] Si hay broker: proceder normalmente

### 2.4 Verificacion SSO - COMPLETADO
- [x] Test manual: SSO completo en Teams (automatico)
- [x] Test manual: SSO completo en M365 Copilot (automatico)
- [x] Test manual: SSO completo en Edge (automatico)
- [x] Test manual: SSO parcial en Word, Excel, OneDrive, PowerPoint (email visible, sin contrasena)

**Entregable Fase 2**: Login/logout funcional con SSO global. Passwordless verificado con number matching.

---

## Fase 3: Base de Datos e Historial - PENDIENTE

### 3.1 Room Database
- [ ] Crear entity `AuthEvent` (id, type, userEmail, userName, timestamp)
- [ ] Crear `AuthEventDao` con queries: getAll(), insert(), getByDateRange()
- [ ] Crear `AuthEventDatabase`

### 3.2 Apps Microsoft Detectadas
- [ ] Implementar `AppDetector` que escanea apps Microsoft instaladas via PackageManager
- [ ] Mostrar estado de SSO por app (completo/parcial/no instalada)

### 3.3 Repository de Historial
- [ ] Crear `HistoryRepository` interface
- [ ] Implementar `RoomHistoryRepository`
- [ ] Registrar evento LOGIN al completar `signIn()` exitoso
- [ ] Registrar evento LOGOUT al completar `signOut()` exitoso

**Entregable Fase 3**: Historial persistente de login/logout con timestamps.

---

## Fase 4: UI con Jetpack Compose - PENDIENTE

### 4.1 Pantalla Principal (Login/Status)
- [ ] Estado no autenticado: Logo + boton "Iniciar Sesion"
- [ ] Estado autenticado: Nombre, email, badge "SSO Activo", lista de apps
- [ ] Estado de carga: CircularProgressIndicator
- [ ] Estado de error: Mensaje + boton reintento

### 4.2 Pantalla de Historial
- [ ] Lista de eventos login/logout con iconos, email, fecha/hora
- [ ] Filtro por rango de fechas (DateRangePicker)
- [ ] Estado vacio

### 4.3 Navegacion
- [ ] Bottom navigation con 2 tabs: "SSO" y "Historial"
- [ ] Material 3 theming

**Entregable Fase 4**: UI completa y funcional.

---

## Fase 5: Testing y QA - PENDIENTE

### 5.1 Unit Tests
- [ ] Tests de `AuthRepository` (mock MSAL)
- [ ] Tests de `HistoryRepository` (in-memory Room DB)
- [ ] Tests de ViewModels
- [ ] Tests de `AppDetector`

### 5.2 Integration Tests
- [ ] Test de flujo completo login > historial registrado
- [ ] Test de flujo completo logout > historial registrado
- [ ] Test de deteccion de apps Microsoft

### 5.3 Manual QA en Samsung WAF
- [ ] Verificar SSO completo en Teams, M365 Copilot, Edge
- [ ] Verificar SSO parcial en Word, Excel, OneDrive, PowerPoint
- [ ] Verificar que logout revoca SSO y mata procesos en background
- [ ] Verificar historial correcto con timestamps
- [ ] Verificar comportamiento sin broker instalado
- [ ] Verificar comportamiento sin conexion a internet
- [ ] Verificar autenticacion passwordless con number matching

**Entregable Fase 5**: App testeada y validada en Samsung WAF.

---

## Fase 6: Release y Distribucion - PENDIENTE

### 6.1 Build de Release
- [ ] Configurar signing config con keystore de release
- [ ] Generar signature hash de release
- [ ] Actualizar signature hash en Entra ID (portal > plataforma Android)
- [ ] Generar APK de release
- [ ] Proguard/R8 rules para MSAL y Room

### 6.2 Distribucion
- [ ] Distribuir APK via sideload (USB/descarga) a cada pantalla Samsung WAF
- [ ] Alternativa: Google Play Store privado si se publica

> **CAMBIO vs PLAN ORIGINAL**: La distribucion se realiza via sideload en vez de Intune LOB, porque Samsung WAF no soporta enrollment en Intune.

**Entregable Fase 6**: App distribuida y funcional en pantallas Samsung WAF.

---

## Dependencias entre Fases

```
Fase 0 (Entra ID + SDM) --> Fase 1 (Scaffold) --> Fase 2 (Auth + SSO)
                                                         |
                                                         v
                                  Fase 4 (UI) <-- Fase 3 (DB/Historial)
                                       |
                                       v
                                  Fase 5 (Testing) --> Fase 6 (Release)
```

> **NOTA**: Fase 0 ya no incluye Intune. Se sustituyo por configuracion de Shared Device Mode.

---

## Cambios vs Plan Original

| Aspecto | Plan Original | Implementacion Real | Razon |
|---|---|---|---|
| Enrollment | Intune MDM + Work Profile | Shared Device Mode via Authenticator | Samsung WAF no soporta Work Profile |
| Compliance | Compliance Policy en Intune | Sin compliance (no necesaria) | Sin enrollment, no hay compliance |
| Conditional Access | Requerir dispositivo compatible | Eliminada | Sin compliance, no funciona |
| App Protection (MAM) | Configurada para proteccion | Excluida para dispositivos compartidos | MAM bloquea SSO en SDM |
| Metodo de login | `acquireToken()` | `signIn()` | `signIn()` es obligatorio para SSO global en SDM |
| Distribucion | Intune LOB app | Sideload (APK via USB) | Sin enrollment, no se puede distribuir via Intune |
| Security Defaults | Activados | Desactivados | Necesario para configurar autenticacion passwordless |
| Autenticacion | Password + MFA | Passwordless (number matching) | Mas seguro y practico para pantallas compartidas |

---

## Criterios de Exito - PARCIALMENTE VERIFICADO

1. [x] Usuario abre AutoLogin > pulsa Login > se autentica con passwordless (number matching)
2. [x] Usuario abre Teams > **entra directamente sin pedir credenciales** (SSO completo)
3. [x] Usuario abre M365 Copilot > **entra directamente sin pedir credenciales** (SSO completo)
4. [x] Usuario abre Edge > **entra directamente sin pedir credenciales** (SSO completo)
5. [ ] Historial muestra fecha/hora exactas de login (pendiente Sprint 3)
6. [x] Usuario pulsa Logout > SSO revocado en todas las apps
7. [ ] Historial muestra fecha/hora exactas de logout (pendiente Sprint 3)
