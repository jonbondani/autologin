# Plan de Proyecto - AutoLogin

## Resumen

App Android que centraliza autenticacion Microsoft 365 y habilita SSO device-wide en Samsung WAF mediante broker MSAL + Shared Device Mode.

**Modo de operacion**: Shared Device Mode via Microsoft Authenticator (sin Intune enrollment).

---

## Estado General (alineado a 2026-02-19)

| Sprint | Estado | Fecha |
|---|---|---|
| Sprint 0: Configuracion Entra ID | COMPLETADO | 2026-02-10 |
| Sprint 1: Scaffold del Proyecto | COMPLETADO | 2026-02-11 |
| Sprint 2: Autenticacion MSAL + SSO | COMPLETADO | 2026-02-12 |
| Sprint 3: Base de Datos e Historial | COMPLETADO | 2026-02-13 |
| Sprint 4: UI con Jetpack Compose | COMPLETADO | 2026-02-13 |
| Sprint 5: Testing y QA | COMPLETADO | 2026-02-17 |
| Sprint 6: Release y Distribucion | EN CURSO | - |
| Sprint 7: Hardening de Seguridad | PLANIFICADO | - |
| Sprint 8: Telemetria y Monitorizacion | PLANIFICADO | - |
| Sprint 9: Gestion Remota | PLANIFICADO | - |

---

## Sprints completados (0-5)

- Configuracion de Entra ID y Shared Device Mode funcional en Samsung WAF.
- Login/logout con MSAL broker usando `signIn()` para SSO global.
- Historial persistente con Room (`AuthEvent`, DAO, repositorio).
- Deteccion y lanzamiento de apps Microsoft con clasificacion SSO completo/parcial.
- UI completa (sesion + historial + navegacion + footer con version/log).
- Suite de unit tests implementada:
  - AppDetector: 6 tests
  - HistoryRepository: 4 tests
  - AuthViewModel: 5 tests
- QA manual en dispositivo Samsung WAF completado.

---

## Sprint 6 (en curso): pendientes reales

### 6.1 Build release
- [x] Signing config de release
- [x] Reglas ProGuard/R8 para MSAL y Room
- [x] Versionado automatico desde git (`versionCode` y `versionName`)
- [ ] Registrar hash de keystore release en Entra ID (plataforma Android)
- [ ] Generar APK release firmado

### 6.2 Distribucion y Auto-update
- [x] Auto-update via GitHub Releases (comprueba al abrir, descarga con progreso, instala)
- [x] APK renombrado: `AutoLogin-v1.0.XX-release.apk`
- [x] FileProvider para instalacion de APK descargado
- [x] Permiso REQUEST_INSTALL_PACKAGES
- [x] Guia de instalacion (`docs/INSTALLATION.md`)
- [ ] Validar APK release en una Samsung WAF piloto
- [ ] Distribuir APK al resto de displays (sideload)

---

## Sprint 7 (planificado): Hardening de Seguridad

Audit de seguridad realizado el 2026-02-19. 20 hallazgos identificados.

### 7.1 Prioridad alta
- [ ] Cifrar Room DB con SQLCipher (emails en texto plano)
- [ ] Controlar backup con dataExtractionRules (Android 12+)
- [ ] Crear network_security_config.xml (bloquear cleartext)
- [ ] Verificar firma del APK descargado antes de instalar

### 7.2 Prioridad media
- [ ] Eliminar PII de logs (emails en logcat)
- [ ] Fijar versiones de dependencias (eliminar wildcards)
- [ ] Mejorar reglas ProGuard para ofuscacion efectiva
- [ ] Reemplazar Runtime.exec() por ProcessBuilder

---

## Sprint 8 (planificado): Telemetria y Servidor de Monitorizacion

Servidor Docker en red local para monitorizar estado de todas las pantallas.

### 8.1 Infraestructura servidor
- [ ] Esquema BD PostgreSQL (devices, events, app_status)
- [ ] API REST backend (heartbeat, events, devices)
- [ ] docker-compose.yml (PostgreSQL + API + Grafana)

### 8.2 Cliente Android
- [ ] SDK de telemetria (HeartbeatWorker + EventReporter)

### 8.3 Dashboard
- [ ] Dashboards Grafana (estado, timeline, errores, actualizaciones)
- [ ] Documentacion despliegue (`docs/MONITORING_SERVER.md`)

---

## Sprint 9 (planificado): Gestion Remota de Pantallas

Permitir a IT gestionar pantallas remotamente desde el dashboard.

### 9.1 Servidor
- [ ] Endpoints de comandos remotos (force_update, force_logout, restart_app)
- [ ] Actualizacion remota push desde dashboard

### 9.2 Cliente Android
- [ ] Cliente de polling (WorkManager cada 2 min, pull-based)

### 9.3 Dashboard IT
- [ ] Panel de administracion (acciones por dispositivo)
- [ ] Alertas automaticas (offline, errores, version desactualizada)
- [ ] Documentacion operativa (`docs/REMOTE_MANAGEMENT.md`)

---

## Riesgos y bloqueos actuales

- Bloqueo Sprint 6: falta registrar hash release en Entra ID antes del build final de distribucion.
- Riesgo operativo: version de Java local mal configurada puede bloquear ejecucion de tests/build en algunos entornos.
- Sprint 8/9: requiere maquina con Docker en la red local de las pantallas.

---

## Fuentes de verdad de estado

- Estado detallado y tracking por tareas: `docs/PROGRESS.md`
- Estado funcional del producto y setup operativo: `README.md`
- Tracking en Asana: subtareas de "Autologin en pantallas"

