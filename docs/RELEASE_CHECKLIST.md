# Checklist de Release - AutoLogin

## 1. Preparacion tecnica

- [ ] Confirmar keystore release disponible localmente (`autologin-release.jks`)
- [ ] Confirmar `keystore.properties` correcto y fuera de git
- [ ] Corregir `JAVA_HOME` a una ruta valida del JDK antes de build/tests

## 2. Entra ID (bloqueante)

- [ ] Obtener hash SHA-1/SHA-256 de la keystore release
- [ ] Registrar hash release en App Registration de Entra ID (plataforma Android)
- [ ] Verificar redirect URI y package name sin cambios

## 3. Build y validacion local

- [ ] Ejecutar `./gradlew test`
- [ ] Ejecutar `./gradlew assembleRelease`
- [ ] Verificar que se genera `AutoLogin-v1.0.XX-release.apk`
- [ ] Validar `versionCode` y `versionName` esperados

## 4. QA en dispositivo piloto (Samsung WAF)

- [ ] Instalar APK release en 1 pantalla piloto
- [ ] Verificar login passwordless (number matching)
- [ ] Verificar SSO completo: Teams, M365 Copilot, Edge
- [ ] Verificar SSO parcial esperado: Word/Excel/OneDrive/PowerPoint/SharePoint/To Do
- [ ] Verificar logout global y limpieza de procesos
- [ ] Verificar boton de envio de logs a IT
- [ ] Verificar auto-update: crear release en GitHub con versionCode superior, abrir app, debe mostrar boton de actualizacion

## 5. Despliegue

- [ ] Definir ventana de despliegue y orden de pantallas
- [ ] Distribuir APK por sideload (ver `docs/INSTALLATION.md`)
- [ ] Confirmar version instalada en cada display
- [ ] Registrar incidencias por pantalla

## 6. Cierre

- [ ] Actualizar `docs/PROGRESS.md` con fecha y estado final de Sprint 6
- [ ] Crear PR de release con evidencias de QA
- [ ] Etiquetar release en GitHub y adjuntar APK final (ver `docs/INSTALLATION.md`)
- [ ] Guardar plan de rollback (APK anterior disponible)

