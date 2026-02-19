# Pull Request Template - AutoLogin

## Estado actual del proyecto

- Sprints 0-5: completados
- Sprint 6 (release/distribucion): en curso
- Pendientes de release: hash de firma release en Entra ID, build APK release, despliegue a displays

---

## Template de PR

### Titulo
`<type>: <descripcion corta>`

Tipos sugeridos: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `release`

### Summary
- <cambio 1>
- <cambio 2>

### Cambios por archivo
- `<ruta/archivo>`: <que cambio y por que>

### Testing
- [ ] Unit tests pasan (`./gradlew test`)
- [ ] Build debug pasa (`./gradlew assembleDebug`)
- [ ] Build release pasa (`./gradlew assembleRelease`) (si aplica)
- [ ] QA manual en Samsung WAF (si aplica)
- [ ] Verificacion de SSO (Teams/M365 Copilot/Edge) (si aplica)

### Checklist de calidad
- [ ] El cambio respeta MVVM + Clean Architecture
- [ ] No rompe flujo de login/logout con MSAL broker
- [ ] No introduce secretos en repo
- [ ] Documentacion actualizada si cambia comportamiento
- [ ] Sin cambios fuera de scope

### Checklist extra para PRs de release
- [ ] Hash release registrado en Entra ID
- [ ] APK release firmado generado
- [ ] Validado en dispositivo piloto
- [ ] Plan de rollback definido (APK anterior disponible)

