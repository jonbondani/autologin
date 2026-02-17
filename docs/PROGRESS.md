# Progreso del Proyecto - AutoLogin

## Estado General: SPRINT 6 EN CURSO - APP EN PRODUCCION

Ultima actualizacion: 2026-02-17

---

## Datos del Proyecto

| Parametro | Valor |
|---|---|
| Client ID | `678488cf-7a78-4487-bb96-76f479a4967a` |
| Tenant ID | `909c0d8a-dd01-4fe7-ac8a-f336e540fdfa` |
| Redirect URI | `msauth://com.autologin.app/TDPWfC9supht4%2Fc0hKDPvlzj%2BO8%3D` |
| Signature Hash (raw) | `TDPWfC9supht4/c0hKDPvlzj+O8=` |
| Shared Device ID | `f3a76792-04eb-4ac8-a42b-aa431ccea198` |
| Dispositivo | Samsung WAF Interactive Display 65" |
| Android | 14 (API 34) |
| Android ID | `6a184d94879d45b6` |
| GMS | Habilitado (Play Store funcional) |
| Modo | Shared Device Mode via Authenticator (NO Intune) |
| Cuenta compartida | `pantallas@prestige-expo.com` |
| Cloud Device Admin | `adminprestige@prestige-expo.com` |
| Rol del admin | Administrador de dispositivos en la nube |
| Autenticacion | Passwordless con Authenticator number matching |

---

## Sprint 0: Configuracion del Entorno Microsoft
**Estado**: COMPLETADO

| Tarea | Estado | Fecha | Notas |
|---|---|---|---|
| Registro App en Entra ID | COMPLETADO | 2026-02-10 | Client ID: 678488cf... |
| Habilitar public client flows | COMPLETADO | 2026-02-10 | |
| Configurar permisos API | COMPLETADO | 2026-02-10 | openid, profile, offline_access, User.Read |
| Grant admin consent | COMPLETADO | 2026-02-10 | |
| Desactivar Security Defaults | COMPLETADO | 2026-02-10 | Necesario para auth passwordless |
| Configurar plataforma Android | COMPLETADO | 2026-02-11 | Hash: TDPWfC9supht4/c0hKDPvlzj+O8= |
| Rol Cloud Device Admin asignado | COMPLETADO | 2026-02-10 | A adminprestige@prestige-expo.com |
| Shared Device Mode activado | COMPLETADO | 2026-02-10 | Device ID: f3a76792... |
| Crear grupo AutoLogin Users | COMPLETADO | 2026-02-10 | |
| Configurar Authenticator passwordless | COMPLETADO | 2026-02-12 | Modo "Sin contrasena", OTP habilitado |
| Registrar passwordless para pantallas@ | COMPLETADO | 2026-02-12 | Number matching verificado |

### Decisiones tomadas en Sprint 0:
- Dispositivos son Samsung WAF Interactive Displays (no phones) - **Work Profile no soportado**
- Se usa **Shared Device Mode** en vez de Intune enrollment - no requiere factory reset
- Security Defaults desactivados (requerido para configurar metodos de autenticacion passwordless)
- ~~Conditional Access policy~~ eliminada - Samsung WAF no soporta enrollment
- App Protection Policies (MAM) excluidas de dispositivos compartidos - bloquean SSO
- Autenticacion passwordless configurada con Authenticator number matching
- INTUNE_CONFIGURATION.md marcado como OBSOLETO

---

## Sprint 1: Scaffold del Proyecto Android
**Estado**: COMPLETADO

| Tarea | Estado | Fecha | Notas |
|---|---|---|---|
| Crear proyecto Kotlin + Compose | COMPLETADO | 2026-02-11 | |
| Configurar dependencias Gradle | COMPLETADO | 2026-02-11 | MSAL, Room, Hilt, Compose |
| Crear auth_config.json | COMPLETADO | 2026-02-11 | Con redirect URI real, shared_device_mode_supported: true |
| Configurar AndroidManifest.xml | COMPLETADO | 2026-02-11 | BrowserTabActivity con hash raw, KILL_BACKGROUND_PROCESSES |
| Crear estructura de paquetes | COMPLETADO | 2026-02-11 | di/, data/, domain/, ui/ |
| Configurar Hilt Application | COMPLETADO | 2026-02-11 | |
| Configurar Navigation | COMPLETADO | 2026-02-11 | |
| Resolver: fillColor duplicado en vector XML | COMPLETADO | 2026-02-11 | Atributo duplicado eliminado |
| Resolver: mipmap fallback para API < 26 | COMPLETADO | 2026-02-11 | Icono adaptativo con fallback |
| Resolver: iconos Material no encontrados | COMPLETADO | 2026-02-11 | Warning/Lock/DateRange en vez de Error/Shield/History |
| Build exitoso | COMPLETADO | 2026-02-11 | Compila sin errores |

---

## Sprint 2: Autenticacion con MSAL + Broker (Shared Device Mode)
**Estado**: COMPLETADO

| Tarea | Estado | Fecha | Notas |
|---|---|---|---|
| Implementar AuthRepository | COMPLETADO | 2026-02-12 | ISingleAccountPublicClientApplication |
| Implementar MsalAuthRepository | COMPLETADO | 2026-02-12 | signIn() para SSO global, signOut() + killBackgroundProcesses |
| Crear AuthViewModel | COMPLETADO | 2026-02-12 | |
| Verificacion de broker instalado | COMPLETADO | 2026-02-12 | Authenticator + Company Portal |
| Resolver: signature hash mismatch | COMPLETADO | 2026-02-12 | Debug keystore usa hash diferente |
| Resolver: BrowserTabActivity path | COMPLETADO | 2026-02-12 | android:path raw, no URL-encoded |
| Resolver: getCurrentAccount main thread | COMPLETADO | 2026-02-12 | Movido a withContext(Dispatchers.IO) |
| Resolver: signIn vs acquireToken | COMPLETADO | 2026-02-12 | signIn() obligatorio para SSO global en SDM |
| Resolver: account already signed in | COMPLETADO | 2026-02-12 | Verificar cuenta existente antes de signIn() |
| Configurar passwordless para pantallas@ | COMPLETADO | 2026-02-12 | Authenticator como "Cuenta profesional" via QR |
| Resolver: passwordless no se configuraba | COMPLETADO | 2026-02-12 | Reset nuclear de MFA + re-registro |
| Excluir MAM de dispositivos compartidos | COMPLETADO | 2026-02-12 | MAM bloqueaba SSO |
| Test manual: SSO en M365 Copilot | COMPLETADO | 2026-02-12 | SSO completo automatico |
| Test manual: SSO en Teams | COMPLETADO | 2026-02-12 | SSO completo automatico |
| Test manual: SSO en Edge | COMPLETADO | 2026-02-12 | SSO completo automatico |
| Test manual: Apps Office standalone | COMPLETADO | 2026-02-12 | SSO parcial (email visible, sin contrasena) |
| Test manual: Logout global | COMPLETADO | 2026-02-12 | Revoca PRT, mata procesos, limpia sesiones |

### Problemas resueltos en Sprint 2:

| # | Problema | Causa | Solucion |
|---|---|---|---|
| 1 | Signature hash mismatch | Debug keystore genera hash diferente | Usar hash del error de MSAL |
| 2 | BrowserTabActivity no intercepta redirect | android:path URL-encoded | Usar hash raw con `/`, `+`, `=` |
| 3 | Crash en getCurrentAccount | Main thread | withContext(Dispatchers.IO) |
| 4 | SSO no propagaba | acquireToken() en vez de signIn() | Cambiar a signIn() |
| 5 | Apps Office sin SSO completo | No son SDM-aware | Documentado como limitacion |
| 6 | MAM bloqueaba SSO | Conflicto con Shared Device Mode | Excluir de scope MAM |
| 7 | Passwordless no se configuraba | Registro incorrecto en Authenticator | Reset nuclear de MFA + re-registro como "Cuenta profesional" |
| 8 | Account already signed in | No se verificaba cuenta existente | Check getCurrentAccount() antes de signIn() |
| 9 | Teams pedia PIN | App Protection Policy activa | Excluir dispositivos compartidos de MAM |
| 10 | Authenticator con icono X | Registrado como TOTP, no push | Re-registrar como "Cuenta profesional" via QR |
| 11 | "No se puede anadir perfil de trabajo" | Samsung WAF no soporta Work Profile | Usar Shared Device Mode (ignorar error en Company Portal) |
| 12 | "El soporte tecnico debe asignar licencia" | Falta licencia M365 Business Premium | Asignar licencia desde admin center |

---

## Sprint 3: Base de Datos e Historial
**Estado**: COMPLETADO

| Tarea | Estado | Fecha | Notas |
|---|---|---|---|
| Crear entity AuthEvent | COMPLETADO | 2026-02-13 | Room entity con tipo, email, nombre, timestamp |
| Crear AuthEventDao | COMPLETADO | 2026-02-13 | Queries por fecha |
| Crear AuthEventDatabase | COMPLETADO | 2026-02-13 | autologin.db |
| Implementar AppDetector | COMPLETADO | 2026-02-13 | Deteccion de apps + kill + launch intent |
| Implementar HistoryRepository | COMPLETADO | 2026-02-13 | RoomHistoryRepository |

---

## Sprint 4: UI con Jetpack Compose
**Estado**: COMPLETADO

| Tarea | Estado | Fecha | Notas |
|---|---|---|---|
| Pantalla Login/Status | COMPLETADO | 2026-02-13 | Dos columnas: acceso automatico / requiere identificacion |
| Pantalla Historial | COMPLETADO | 2026-02-13 | Filtro por fechas, LazyColumn |
| Bottom Navigation | COMPLETADO | 2026-02-13 | Sesion + Historial |
| Material 3 Theme | COMPLETADO | 2026-02-13 | |
| Botones "Abrir" / "Identificate" | COMPLETADO | 2026-02-17 | Lanzamiento directo de apps Microsoft |
| Footer con version, copyright, log | COMPLETADO | 2026-02-17 | Version auto desde commits git |

---

## Sprint 5: Testing y QA
**Estado**: COMPLETADO

| Tarea | Estado | Fecha | Notas |
|---|---|---|---|
| Unit tests: AppDetector | COMPLETADO | 2026-02-15 | 6 tests |
| Unit tests: HistoryRepository | COMPLETADO | 2026-02-15 | 4 tests |
| Unit tests: AuthViewModel | COMPLETADO | 2026-02-15 | 5 tests |
| QA manual completo | COMPLETADO | 2026-02-17 | En Samsung WAF Display |

---

## Sprint 6: Release y Distribucion
**Estado**: EN CURSO

| Tarea | Estado | Fecha | Notas |
|---|---|---|---|
| Signing config release | COMPLETADO | 2026-02-16 | keystore.properties (gitignored) |
| Hash de release en Entra ID | PENDIENTE | - | Necesario para APK release |
| Build APK release | PENDIENTE | - | |
| Proguard/R8 rules | COMPLETADO | 2026-02-16 | MSAL + Room rules |
| Versionado auto desde git | COMPLETADO | 2026-02-17 | versionCode=commits, versionName=1.0.N |
| Envio de logs a IT | COMPLETADO | 2026-02-17 | Boton en footer, via Intent.ACTION_SEND |
| Distribuir a displays | PENDIENTE | - | Via sideload (APK) |

---

## Registro de Decisiones

| Fecha | Decision | Razon |
|---|---|---|
| 2026-02-09 | Usar MSAL broker mode (no standalone) | SSO device-wide requiere broker |
| 2026-02-09 | Kotlin + Compose (no XML) | Stack moderno, menos boilerplate |
| 2026-02-09 | Room para historial (no SharedPreferences) | Queries complejas, filtros por fecha |
| 2026-02-09 | minSdk 24 | Requerido por MSAL v7+ |
| 2026-02-10 | Shared Device Mode (no Intune enrollment) | Samsung WAF no soporta Work Profile; evita factory reset |
| 2026-02-10 | Desactivar Security Defaults | Requerido para configurar metodos de autenticacion passwordless |
| 2026-02-10 | Rol Cloud Device Admin (no Global Admin) | Minimo privilegio para registrar shared device |
| 2026-02-11 | Eliminar Conditional Access policy | Samsung WAF no soporta enrollment como dispositivo compatible |
| 2026-02-12 | Usar signIn() en vez de acquireToken() | signIn() es obligatorio para global SSO en SDM |
| 2026-02-12 | Excluir MAM de dispositivos compartidos | App Protection Policies bloquean SSO en SDM |
| 2026-02-12 | Documentar SSO parcial en apps Office | Word, Excel, OneDrive, PPT, SharePoint, To Do no son SDM-aware |
| 2026-02-12 | Autenticacion passwordless con number matching | Mas seguro y practico para pantallas compartidas |
| 2026-02-12 | killBackgroundProcesses en sign-out | Limpieza de apps Microsoft en background |
| 2026-02-13 | Marcar INTUNE_CONFIGURATION.md como OBSOLETO | Samsung WAF no soporta Work Profile ni Intune enrollment |

---

## Compatibilidad SSO Confirmada

| App | Tipo SSO | Estado |
|-----|----------|--------|
| Microsoft 365 Copilot | COMPLETO (automatico) | Verificado |
| Microsoft Teams | COMPLETO (automatico) | Verificado |
| Microsoft Edge | COMPLETO (automatico) | Verificado |
| Word (standalone) | PARCIAL (email, sin contrasena) | Verificado |
| Excel (standalone) | PARCIAL (email, sin contrasena) | Verificado |
| OneDrive (standalone) | PARCIAL (email, sin contrasena) | Verificado |
| PowerPoint (standalone) | PARCIAL (email, sin contrasena) | Verificado |
| SharePoint | PARCIAL (email, sin contrasena) | Verificado |
| To Do | PARCIAL (email, sin contrasena) | Verificado |

---

## Registro de Decisiones (continuacion)

| Fecha | Decision | Razon |
|---|---|---|
| 2026-02-17 | No usar killBackgroundProcesses al abrir apps | Rompe el estado de auth de Excel/Office, causa bucle SyncPlaces |
| 2026-02-17 | Launch intent directo con FLAG_ACTIVITY_CLEAR_TOP | Suficiente para SSO, el broker inyecta credenciales en cold start |
| 2026-02-17 | Versionado automatico desde git commits | Identificacion inequivoca de builds en dispositivos |
| 2026-02-17 | Envio de logs via Intent.ACTION_SEND | No requiere permisos extra, captura logs del proceso (incluyendo MSAL) |
| 2026-02-17 | pm clear no disponible desde la app | Requiere CLEAR_APP_USER_DATA (solo device owner). Cloud DPC es el device owner |

## Blockers Actuales

Hash de release pendiente de registrar en Entra ID antes de generar APK release.
