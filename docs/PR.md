# Pull Request Template - AutoLogin

---

## PR para Fase 0: Documentación y Plan de Proyecto

### Título
`feat: project documentation, feasibility study, and configuration guides`

### Descripción

## Summary
- Estudio de viabilidad técnica completo: confirmada la posibilidad de SSO device-wide en Android via MSAL broker + PRT
- Plan de proyecto con 7 fases detalladas
- Guías paso a paso de configuración de Microsoft Entra ID e Intune
- Arquitectura técnica con stack, modelos de datos, y flujos de autenticación
- Documentación de skills, lessons learned, y tracking de progreso

## Cambios incluidos
- `README.md` — Descripción del proyecto, arquitectura, tech stack, estructura
- `docs/FEASIBILITY.md` — Análisis de viabilidad con verificación técnica de MSAL broker, PRT, licencias Business Premium, y limitaciones conocidas
- `docs/PROJECT_PLAN.md` — Plan de 7 fases con tareas detalladas, entregables, y dependencias
- `docs/ENTRA_ID_CONFIGURATION.md` — Guía paso a paso: App Registration, permisos API, plataforma Android, Conditional Access, troubleshooting
- `docs/INTUNE_CONFIGURATION.md` — Guía paso a paso: Enrollment, Compliance Policy, App Protection, Conditional Access, distribución LOB
- `docs/TECHNICAL_ARCHITECTURE.md` — Stack tecnológico, modelo de datos, interfaces, flujos, configuración MSAL, diseño de UI
- `docs/PROGRESS.md` — Estado actual y tracking de todas las fases
- `docs/LESSONS_LEARNED.md` — 6 lecciones aprendidas durante la planificación
- `docs/SKILLS.md` — Competencias técnicas requeridas y recursos de aprendizaje
- `docs/PR.md` — Este template de PR

## Decisiones técnicas documentadas
1. **MSAL broker mode** (no standalone) — SSO device-wide requiere broker obligatoriamente
2. **Kotlin + Jetpack Compose** — Stack moderno, menos boilerplate que XML
3. **Room Database** — Para historial con queries y filtros (no SharedPreferences)
4. **Single account mode** — Un usuario corporativo por dispositivo
5. **minSdk 24** — Requerido por MSAL v7+

## Limitaciones identificadas
- El historial en la app se limita a eventos propios (login/logout). Android sandbox impide observar autenticación de otras apps.
- Para auditoría completa de SSO: usar Entra ID Sign-in Logs (server-side)
- Licencia Business Premium: máximo 300 usuarios por tenant
- Broker obligatorio: Authenticator o Company Portal debe estar instalado

## Blockers para iniciar desarrollo
- [ ] Se necesita un administrador de Entra ID para crear el App Registration y obtener Client ID + Tenant ID
- [ ] Se necesita un dispositivo Android enrollado en Intune para testing

## Test plan
- [ ] Revisión de la viabilidad técnica por el equipo
- [ ] Validación de los pasos de Entra ID con un administrador del tenant
- [ ] Validación de los pasos de Intune con el equipo de IT
- [ ] Confirmación de disponibilidad de dispositivo de pruebas
- [ ] Aprobación del plan de proyecto antes de iniciar Fase 1

---

## Template para PRs de Código (Fases 1-6)

```markdown
### Título
`<type>: <descripción corta>`

Tipos: feat, fix, refactor, test, docs, chore

## Summary
- <bullet points de cambios>

## Cambios
- `<archivo>`: <qué cambió y por qué>

## Testing
- [ ] Unit tests pasan
- [ ] Integration tests pasan
- [ ] Manual QA en dispositivo
- [ ] SSO verificado con Teams/Outlook (si aplica)

## Screenshots
<si hay cambios de UI>

## Checklist
- [ ] Código sigue la arquitectura documentada
- [ ] No se introdujeron vulnerabilidades de seguridad
- [ ] No se modificaron archivos fuera del scope
- [ ] ProGuard rules actualizadas si se añadieron dependencias
```
