# Plan de Proyecto - AutoLogin

## Resumen

App Android que centraliza autenticacion Microsoft 365 y habilita SSO device-wide en Samsung WAF mediante broker MSAL + Shared Device Mode.

**Modo de operacion**: Shared Device Mode via Microsoft Authenticator (sin Intune enrollment).

---

## Estado General (alineado a 2026-02-17)

| Sprint | Estado | Fecha |
|---|---|---|
| Sprint 0: Configuracion Entra ID | COMPLETADO | 2026-02-10 |
| Sprint 1: Scaffold del Proyecto | COMPLETADO | 2026-02-11 |
| Sprint 2: Autenticacion MSAL + SSO | COMPLETADO | 2026-02-12 |
| Sprint 3: Base de Datos e Historial | COMPLETADO | 2026-02-13 |
| Sprint 4: UI con Jetpack Compose | COMPLETADO | 2026-02-13 |
| Sprint 5: Testing y QA | COMPLETADO | 2026-02-17 |
| Sprint 6: Release y Distribucion | EN CURSO | - |

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

### 6.2 Distribucion
- [ ] Validar APK release en una Samsung WAF piloto
- [ ] Distribuir APK al resto de displays (sideload)

---

## Riesgos y bloqueos actuales

- Bloqueo principal: falta registrar hash release en Entra ID antes del build final de distribucion.
- Riesgo operativo: version de Java local mal configurada puede bloquear ejecucion de tests/build en algunos entornos.

---

## Fuentes de verdad de estado

- Estado detallado y tracking por tareas: `docs/PROGRESS.md`
- Estado funcional del producto y setup operativo: `README.md`

