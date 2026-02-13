# Pull Request Template - AutoLogin

---

## PR Actual: Sprints 0-2 Completados

### Titulo
`feat: autenticacion MSAL con Shared Device Mode, SSO global y passwordless`

### Descripcion

## Summary
- Configuracion completa de Microsoft Entra ID con Shared Device Mode (sin Intune enrollment)
- App Android funcional con autenticacion MSAL usando `signIn()`/`signOut()` para SSO global
- SSO verificado en Samsung WAF Interactive Display 65" con Teams, M365 Copilot, Edge
- Autenticacion passwordless con Authenticator number matching
- Cierre de sesion global con revocacion de PRT y limpieza de procesos en background
- Documentacion completa actualizada

## Cambios incluidos

### Documentacion (Sprint 0)
- `README.md` - Descripcion del proyecto, arquitectura, setup rapido, compatibilidad SSO
- `docs/FEASIBILITY.md` - Estudio de viabilidad actualizado con resultados reales
- `docs/PROJECT_PLAN.md` - Plan de 7 fases con estado actualizado (Sprints 0-2 completados)
- `docs/ENTRA_ID_CONFIGURATION.md` - Guia completa: App Registration, passwordless, setup de pantallas, troubleshooting
- `docs/INTUNE_CONFIGURATION.md` - Marcado como OBSOLETO (Samsung WAF no soporta Work Profile)
- `docs/TECHNICAL_ARCHITECTURE.md` - Stack, modelo de datos, flujos de autenticacion, threading
- `docs/PROGRESS.md` - Estado actual y tracking de sprints
- `docs/LESSONS_LEARNED.md` - 22+ lecciones aprendidas
- `docs/SKILLS.md` - Competencias tecnicas incluyendo Shared Device Mode y passwordless
- `docs/PR.md` - Este template actualizado

### Codigo (Sprints 1-2)
- Proyecto Kotlin + Jetpack Compose con estructura MVVM + Clean Architecture
- `auth_config.json` con configuracion MSAL para Shared Device Mode
- `AndroidManifest.xml` con BrowserTabActivity (hash raw) y permisos necesarios
- `AuthRepository` interface + `MsalAuthRepository` implementacion
- `AuthViewModel` con estados: Idle, Loading, Authenticated, Unauthenticated, Error
- Todas las llamadas MSAL en `Dispatchers.IO` (evitar crash en main thread)
- `killBackgroundProcesses()` para limpieza de apps Microsoft en sign-out
- Modulos Hilt para inyeccion de dependencias

## Decisiones tecnicas

| Decision | Razon |
|---|---|
| Shared Device Mode (no Intune) | Samsung WAF no soporta Work Profile ni enrollment |
| `signIn()` en vez de `acquireToken()` | `signIn()` registra global sign-in para SSO device-wide |
| `Dispatchers.IO` para MSAL | `getCurrentAccount()` crashea en main thread |
| Hash raw en manifest, URL-encoded en config | Requisito de MSAL - formatos diferentes segun ubicacion |
| Sin Conditional Access | Samsung WAF no puede reportar compliance sin enrollment |
| Sin MAM policies | App Protection Policies bloquean SSO en Shared Device Mode |
| Passwordless (number matching) | Mas seguro y practico para pantallas compartidas |

## Problemas resueltos

1. Samsung WAF no soporta Work Profile > Shared Device Mode
2. SSO no propagaba con acquireToken() > Cambiar a signIn()
3. Crash en main thread con getCurrentAccount() > Dispatchers.IO
4. BrowserTabActivity no interceptaba redirect > Hash raw en android:path
5. Signature hash mismatch debug vs release > Usar hash del error de MSAL
6. MAM policies bloqueaban SSO > Excluir dispositivos compartidos
7. Passwordless no se configuraba > Reset nuclear de MFA + re-registro
8. Company Portal error "perfil de trabajo" > Ignorar (solo necesita estar instalado)

## SSO Verificado

| App | Tipo | Estado |
|---|---|---|
| Microsoft 365 Copilot | Completo | Verificado |
| Microsoft Teams | Completo | Verificado |
| Microsoft Edge | Completo | Verificado |
| Word, Excel, OneDrive, PowerPoint | Parcial | Verificado |
| SharePoint, To Do | Parcial | Verificado |

## Test plan
- [x] Login con passwordless funciona en Samsung WAF
- [x] SSO completo en Teams, M365 Copilot, Edge
- [x] SSO parcial en apps Office standalone
- [x] Logout revoca SSO en todas las apps
- [x] Logout mata procesos en background de apps Microsoft
- [ ] Unit tests (pendiente Sprint 5)
- [ ] Integration tests (pendiente Sprint 5)

---

## Template para PRs de Codigo (Sprints 3-6)

```markdown
### Titulo
`<type>: <descripcion corta>`

Tipos: feat, fix, refactor, test, docs, chore

## Summary
- <bullet points de cambios>

## Cambios
- `<archivo>`: <que cambio y por que>

## Testing
- [ ] Unit tests pasan
- [ ] Integration tests pasan
- [ ] Manual QA en Samsung WAF
- [ ] SSO verificado con Teams/M365 Copilot/Edge (si aplica)

## Screenshots
<si hay cambios de UI>

## Checklist
- [ ] Codigo sigue la arquitectura documentada (MVVM + Clean Architecture)
- [ ] No se introdujeron vulnerabilidades de seguridad
- [ ] No se modificaron archivos fuera del scope
- [ ] Llamadas MSAL en Dispatchers.IO
- [ ] ProGuard rules actualizadas si se anadieron dependencias
```
