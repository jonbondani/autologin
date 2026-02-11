# Progreso del Proyecto - AutoLogin

## Estado General: SPRINT 0 EN CURSO

Última actualización: 2026-02-10

---

## Datos del Proyecto

| Parámetro | Valor |
|---|---|
| Client ID | `678488cf-7a78-4487-bb96-76f479a4967a` |
| Tenant ID | `909c0d8a-dd01-4fe7-ac8a-f336e540fdfa` |
| Shared Device ID | `f3a76792-04eb-4ac8-a42b-aa431ccea198` |
| Dispositivo | Samsung WAF Interactive Display 65" |
| Android | 14 (API 34) |
| Android ID | `6a184d94879d45b6` |
| GMS | Habilitado (Play Store funcional) |
| Modo | Shared Device Mode via Authenticator |

---

## Fase 0: Configuración del Entorno Microsoft
**Estado**: EN CURSO

| Tarea | Estado | Fecha | Notas |
|---|---|---|---|
| Registro App en Entra ID | COMPLETADO | 2026-02-10 | Client ID: 678488cf... |
| Habilitar public client flows | COMPLETADO | 2026-02-10 | |
| Configurar permisos API | COMPLETADO | 2026-02-10 | openid, profile, offline_access, User.Read |
| Grant admin consent | COMPLETADO | 2026-02-10 | |
| Desactivar Security Defaults | COMPLETADO | 2026-02-10 | Necesario para Conditional Access |
| Configurar Conditional Access | COMPLETADO | 2026-02-10 | MFA + Block legacy auth + Device compliant |
| Crear grupo AutoLogin Users | COMPLETADO | 2026-02-10 | |
| Rol Cloud Device Admin asignado | COMPLETADO | 2026-02-10 | A adminprestige@prestige-expo.com |
| Shared Device Mode activado | COMPLETADO | 2026-02-10 | Device ID: f3a76792... |
| Configurar plataforma Android | PENDIENTE | — | Requiere signature hash del proyecto |
| Compliance Policy en Intune | PENDIENTE | — | Opcional con Shared Device Mode |
| App Protection Policy | PENDIENTE | — | Opcional con Shared Device Mode |

### Decisiones tomadas en Sprint 0:
- Dispositivos son Samsung WAF Interactive Displays (no phones) → Work Profile no soportado
- Se usa **Shared Device Mode** en vez de Intune enrollment → no requiere factory reset
- Security Defaults desactivados, reemplazados por Conditional Access policies
- Cuenta reuniones@prestige-expo.com es solo para Play Store, no interfiere con SSO

---

## Fase 1: Scaffold del Proyecto Android
**Estado**: PENDIENTE (desbloqueado — Client ID y Tenant ID disponibles)

| Tarea | Estado | Fecha | Notas |
|---|---|---|---|
| Crear proyecto Kotlin + Compose | Pendiente | — | |
| Configurar dependencias Gradle | Pendiente | — | MSAL, Room, Hilt, Compose |
| Crear auth_config.json | Pendiente | — | Client ID + Tenant ID listos, falta signature hash |
| Configurar AndroidManifest.xml | Pendiente | — | |
| Crear estructura de paquetes | Pendiente | — | |
| Configurar Hilt Application | Pendiente | — | |
| Configurar Navigation | Pendiente | — | |

---

## Fase 2: Autenticación con MSAL + Broker (Shared Device Mode)
**Estado**: PENDIENTE

| Tarea | Estado | Fecha | Notas |
|---|---|---|---|
| Implementar AuthRepository | Pendiente | — | Usar ISingleAccountPublicClientApplication |
| Implementar MsalAuthRepository | Pendiente | — | Verificar isSharedDevice, global signOut |
| Crear AuthViewModel | Pendiente | — | |
| Verificación de broker instalado | Pendiente | — | |
| Test manual: SSO en Teams | Pendiente | — | En Samsung WAF Display |
| Test manual: SSO en Outlook | Pendiente | — | En Samsung WAF Display |

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
| QA manual completo | Pendiente | — | En Samsung WAF Display |

---

## Fase 6: Release y Distribución
**Estado**: PENDIENTE

| Tarea | Estado | Fecha | Notas |
|---|---|---|---|
| Signing config release | Pendiente | — | |
| Hash de release en Entra ID | Pendiente | — | |
| Build APK/AAB release | Pendiente | — | |
| Proguard/R8 rules | Pendiente | — | |
| Distribuir a displays | Pendiente | — | Via Play Store privado o sideload |

---

## Registro de Decisiones

| Fecha | Decisión | Razón |
|---|---|---|
| 2026-02-09 | Usar MSAL broker mode (no standalone) | SSO device-wide requiere broker |
| 2026-02-09 | Kotlin + Compose (no XML) | Stack moderno, menos boilerplate |
| 2026-02-09 | Room para historial (no SharedPreferences) | Queries complejas, filtros por fecha |
| 2026-02-09 | minSdk 24 | Requerido por MSAL v7+ |
| 2026-02-10 | Shared Device Mode (no Intune enrollment) | Samsung WAF no soporta Work Profile; evita factory reset |
| 2026-02-10 | Desactivar Security Defaults | Requerido para usar Conditional Access |
| 2026-02-10 | Rol Cloud Device Admin (no Global Admin) | Mínimo privilegio necesario para registrar shared device |

---

## Blockers Actuales

| Blocker | Impacto | Acción Requerida |
|---|---|---|
| Signature hash del proyecto Android | Bloquea config plataforma Android en Entra ID | Crear proyecto en Sprint 1 y generar hash |
