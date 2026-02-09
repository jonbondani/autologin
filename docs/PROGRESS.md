# Progreso del Proyecto - AutoLogin

## Estado General: EN PLANIFICACIÓN

Última actualización: 2026-02-09

---

## Fase 0: Configuración del Entorno Microsoft
**Estado**: PENDIENTE

| Tarea | Estado | Fecha | Notas |
|---|---|---|---|
| Registro App en Entra ID | Pendiente | — | Requiere admin access |
| Configurar plataforma Android | Pendiente | — | Requiere package name + hash |
| Configurar permisos API | Pendiente | — | openid, profile, offline_access, User.Read |
| Habilitar public client flows | Pendiente | — | |
| Grant admin consent | Pendiente | — | Requiere Global Admin |
| Configurar Intune compliance | Pendiente | — | |
| Configurar App Protection | Pendiente | — | |
| Configurar Conditional Access | Pendiente | — | |
| Preparar dispositivo de pruebas | Pendiente | — | Android 7.0+ con Company Portal |

---

## Fase 1: Scaffold del Proyecto Android
**Estado**: PENDIENTE

| Tarea | Estado | Fecha | Notas |
|---|---|---|---|
| Crear proyecto Kotlin + Compose | Pendiente | — | |
| Configurar dependencias Gradle | Pendiente | — | MSAL, Room, Hilt, Compose |
| Crear auth_config.json | Pendiente | — | Requiere Client ID + Tenant ID |
| Configurar AndroidManifest.xml | Pendiente | — | |
| Crear estructura de paquetes | Pendiente | — | |
| Configurar Hilt Application | Pendiente | — | |
| Configurar Navigation | Pendiente | — | |

---

## Fase 2: Autenticación con MSAL + Broker
**Estado**: PENDIENTE

| Tarea | Estado | Fecha | Notas |
|---|---|---|---|
| Implementar AuthRepository | Pendiente | — | |
| Implementar MsalAuthRepository | Pendiente | — | signIn, signOut, getAccount |
| Crear AuthViewModel | Pendiente | — | |
| Verificación de broker instalado | Pendiente | — | |
| Test manual: SSO en Teams | Pendiente | — | |
| Test manual: SSO en Outlook | Pendiente | — | |

---

## Fase 3: Base de Datos e Historial
**Estado**: PENDIENTE

| Tarea | Estado | Fecha | Notas |
|---|---|---|---|
| Crear entity AuthEvent | Pendiente | — | |
| Crear AuthEventDao | Pendiente | — | |
| Crear AuthEventDatabase | Pendiente | — | |
| Implementar AppDetector | Pendiente | — | |
| Implementar HistoryRepository | Pendiente | — | |

---

## Fase 4: UI con Jetpack Compose
**Estado**: PENDIENTE

| Tarea | Estado | Fecha | Notas |
|---|---|---|---|
| Pantalla Login/Status | Pendiente | — | |
| Pantalla Historial | Pendiente | — | |
| Bottom Navigation | Pendiente | — | |
| Material 3 Theme | Pendiente | — | |

---

## Fase 5: Testing y QA
**Estado**: PENDIENTE

| Tarea | Estado | Fecha | Notas |
|---|---|---|---|
| Unit tests: AuthRepository | Pendiente | — | |
| Unit tests: HistoryRepository | Pendiente | — | |
| Unit tests: ViewModels | Pendiente | — | |
| Integration tests | Pendiente | — | |
| QA manual completo | Pendiente | — | |

---

## Fase 6: Release y Distribución
**Estado**: PENDIENTE

| Tarea | Estado | Fecha | Notas |
|---|---|---|---|
| Signing config release | Pendiente | — | |
| Hash de release en Entra ID | Pendiente | — | |
| Build APK/AAB release | Pendiente | — | |
| Proguard/R8 rules | Pendiente | — | |
| Subir a Intune | Pendiente | — | |
| Asignar a usuarios | Pendiente | — | |

---

## Registro de Decisiones

| Fecha | Decisión | Razón |
|---|---|---|
| 2026-02-09 | Usar MSAL broker mode (no standalone) | SSO device-wide requiere broker |
| 2026-02-09 | Kotlin + Compose (no XML) | Stack moderno, menos boilerplate |
| 2026-02-09 | Room para historial (no SharedPreferences) | Queries complejas, filtros por fecha |
| 2026-02-09 | Single account mode (no multiple) | Una cuenta corporativa por dispositivo |
| 2026-02-09 | minSdk 24 | Requerido por MSAL v7+ |

---

## Blockers Actuales

| Blocker | Impacto | Acción Requerida |
|---|---|---|
| Necesitamos Client ID + Tenant ID | Bloquea Fase 1-2 | Completar Fase 0 (Entra ID config) |
| Dispositivo de pruebas con Intune | Bloquea testing | Configurar dispositivo con Company Portal |
