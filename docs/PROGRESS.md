# Progreso del Proyecto - AutoLogin

## Estado General: SPRINT 7 EN CURSO - HARDENING DE SEGURIDAD

Ultima actualizacion: 2026-02-20

---

## Datos del Proyecto

| Parametro | Valor |
|---|---|
| Client ID | `<ENTRA_CLIENT_ID>` |
| Tenant ID | `<ENTRA_TENANT_ID>` |
| Redirect URI | `msauth://com.autologin.app/<SIGNATURE_HASH_URLENCODED>` |
| Signature Hash (raw) | `<SIGNATURE_HASH_RAW>` |
| Shared Device ID | `<SHARED_DEVICE_ID>` |
| Dispositivo | Samsung WAF Interactive Display 65" |
| Android | 14 (API 34) |
| Android ID | `<ANDROID_DEVICE_ID>` |
| GMS | Habilitado (Play Store funcional) |
| Modo | Shared Device Mode via Authenticator (NO Intune) |
| Cuenta compartida | `shared-screen-account@example.com` |
| Cloud Device Admin | `cloud-device-admin@example.com` |
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
| Configurar plataforma Android | COMPLETADO | 2026-02-11 | Hash: <SIGNATURE_HASH_RAW> |
| Rol Cloud Device Admin asignado | COMPLETADO | 2026-02-10 | A cloud-device-admin@example.com |
| Shared Device Mode activado | COMPLETADO | 2026-02-10 | Device ID: <SHARED_DEVICE_ID> |
| Crear grupo AutoLogin Users | COMPLETADO | 2026-02-10 | |
| Configurar Authenticator passwordless | COMPLETADO | 2026-02-12 | Modo "Sin contrasena", OTP habilitado |
| Registrar passwordless para cuenta compartida | COMPLETADO | 2026-02-12 | Number matching verificado |

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
| Configurar passwordless para cuenta compartida | COMPLETADO | 2026-02-12 | Authenticator como "Cuenta profesional" via QR |
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
**Estado**: COMPLETADO (2026-02-20)

| Tarea | Estado | Fecha | Notas |
|---|---|---|---|
| Signing config release | COMPLETADO | 2026-02-16 | keystore.properties (gitignored) |
| Hash de release en Entra ID | COMPLETADO | 2026-02-20 | Registrado en App Registration |
| Build APK release | COMPLETADO | 2026-02-20 | AutoLogin-v1.0.21-release.apk |
| Proguard/R8 rules | COMPLETADO | 2026-02-16 | MSAL + Room rules |
| Versionado auto desde git | COMPLETADO | 2026-02-17 | versionCode=commits, versionName=1.0.N |
| Envio de logs a IT | COMPLETADO | 2026-02-17 | Boton en footer, via Intent.ACTION_SEND |
| Auto-update via GitHub Releases | COMPLETADO | 2026-02-19 | Comprueba al abrir, descarga con progreso, lanza instalador |
| Guia de instalacion | COMPLETADO | 2026-02-19 | docs/INSTALLATION.md |
| Distribuir a pantalla piloto | COMPLETADO | 2026-02-20 | Via ADB sideload, QA post-instalacion OK |
| Release en GitHub | COMPLETADO | 2026-02-20 | v21 publicado con APK |

---

## Sprint 7: Hardening de Seguridad
**Estado**: EN CURSO

Audit de seguridad realizado el 2026-02-19. Hallazgos:
- 3 CRITICAL (inherentes a MSAL Android, mitigables)
- 4 HIGH (BD sin cifrar, backup no controlado, componentes exportados)
- 8 MEDIUM (network config, APK sin verificar, PII en logs, ProGuard, dependencias)

| Tarea | Estado | Notas |
|---|---|---|
| Network security config | PENDIENTE | Bloquear cleartext, solo HTTPS |
| Cifrar Room DB con SQLCipher | PENDIENTE | Emails y timestamps en texto plano |
| Eliminar PII de logs | PENDIENTE | Emails expuestos en logcat |
| Verificar firma del APK descargado | PENDIENTE | Supply chain attack vector |
| Fijar versiones de dependencias | PENDIENTE | MSAL usa wildcard "8.+" |
| Mejorar reglas ProGuard | PENDIENTE | Rules demasiado amplias |
| Controlar backup (dataExtractionRules) | PENDIENTE | Android 12+ |
| Reemplazar Runtime.exec() | PENDIENTE | Usar ProcessBuilder |

---

## Sprint 8: Telemetria y Servidor de Monitorizacion (PLANIFICADO)
**Estado**: PENDIENTE (depende de Sprint 7)

| Tarea | Estado | Notas |
|---|---|---|
| Esquema BD PostgreSQL | PENDIENTE | devices, events, app_status, updates |
| API REST backend | PENDIENTE | Node.js o Go, endpoints heartbeat/events/devices |
| docker-compose.yml | PENDIENTE | PostgreSQL + API + Grafana |
| SDK telemetria en app Android | PENDIENTE | HeartbeatWorker + EventReporter, sin deps extra |
| Dashboards Grafana | PENDIENTE | Estado, timeline, errores, actualizaciones |
| Documentacion servidor | PENDIENTE | docs/MONITORING_SERVER.md |

---

## Sprint 9: Gestion Remota de Pantallas (PLANIFICADO)
**Estado**: PENDIENTE (depende de Sprint 8)

| Tarea | Estado | Notas |
|---|---|---|
| Endpoints comandos remotos | PENDIENTE | force_update, force_logout, restart_app |
| Actualizacion remota push | PENDIENTE | Deploy desde dashboard a pantallas |
| Cliente polling en app Android | PENDIENTE | WorkManager cada 2 min, pull-based |
| Panel administracion IT | PENDIENTE | Grafana o web custom |
| Alertas automaticas | PENDIENTE | Offline, errores, version desactualizada |
| Documentacion operativa | PENDIENTE | docs/REMOTE_MANAGEMENT.md |

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
| 2026-02-19 | Auto-update via GitHub Releases API | Sin dependencias extra (HttpURLConnection + org.json). Tag format: v{versionCode} |
| 2026-02-19 | APK renombrado a AutoLogin-v1.0.XX-release.apk | Nombre empresarial en vez de app-release.apk |
| 2026-02-19 | Telemetria pull-based (no Firebase) | Samsung WAF no tiene GCM. Polling cada 2-5 min en red local es aceptable |
| 2026-02-19 | Stack de monitorizacion: Docker + PostgreSQL + Grafana | Desplegable en cualquier maquina de la red local con Docker |

## Blockers Actuales

Hash de release pendiente de registrar en Entra ID antes de generar APK release.
