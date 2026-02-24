# Guia de Instalacion - AutoLogin

## Requisitos previos

- Pantalla Samsung WAF Interactive Display (65", Android 14)
- Microsoft Authenticator o Company Portal instalado en la pantalla
- Conexion a internet (Wi-Fi o Ethernet)
- APK firmado: `AutoLogin-v1.0.XX-release.apk`

## Generar el APK

Desde Android Studio:

1. **Build → Generate Signed Bundle / APK → APK**
2. Seleccionar keystore release (`autologin-release.jks`)
3. Build variant: `release`
4. El APK se genera en `app/build/outputs/apk/release/AutoLogin-v1.0.XX-release.apk`

O por terminal:

```bash
./gradlew assembleRelease
```

## Configurar Shared Device Mode (obligatorio)

Para que el cierre de sesion funcione de forma global (cerrando la sesion en Teams, Edge y todas las apps de Microsoft), la pantalla debe estar registrada como **dispositivo compartido** en Azure AD.

**Sin este paso, el cierre de sesion solo elimina la cuenta local de AutoLogin, pero las demas apps mantienen la sesion activa.**

### Pasos

**IMPORTANTE**: Authenticator debe configurarse desde una instalacion limpia. Si ya tiene cuentas añadidas, la opcion de Shared Device Mode no aparece.

1. **Desinstalar** Microsoft Authenticator de la pantalla (si ya estaba instalado)
2. **Reinstalar** Microsoft Authenticator desde cero
3. Al abrir Authenticator por primera vez, pulsar **"Omitir este paso"** (Skip) — NO iniciar sesion ni agregar cuenta
4. Una vez en la pantalla principal de Authenticator, ir a **Configuracion** (icono de engranaje o menu de tres puntos)
5. Pulsar **"Registro de dispositivos"** (Device Registration)
6. Seleccionar **"Registrar como dispositivo compartido"** (Register as shared device)
7. Iniciar sesion con la cuenta **Cloud Device Administrator** (ej: `administrador365@tudominio.com`)
8. Esperar a que el registro se complete (puede tardar unos minutos)
9. Verificar que Authenticator muestra **"Shared Device Mode: Enabled"**

### Verificacion desde AutoLogin

Al abrir AutoLogin despues de registrar el dispositivo, los logs de depuracion (`adb logcat -s AutoLogin`) deben mostrar:

```
AutoLogin: MSAL init OK
AutoLogin:   isSharedDevice = true
```

Si muestra `isSharedDevice = false`, el dispositivo no esta correctamente registrado. Revisar:

- Que Microsoft Authenticator este actualizado a la ultima version
- Que la cuenta usada para registrar tenga el rol **Cloud Device Administrator**
- Que el dispositivo tenga conexion a internet durante el registro
- Reiniciar la pantalla tras el registro

### Requisitos en Azure AD (Entra ID)

- La cuenta de registro necesita el rol **Cloud Device Administrator** o **Global Administrator**
- El tenant debe tener habilitado el registro de dispositivos compartidos
- El `client_id` de la app (`678488cf-7a78-4487-bb96-76f479a4967a`) debe estar registrado en Entra ID con el permiso `User.Read`

---

## Instalacion inicial (primera vez)

### Opcion A: ADB por red (recomendado para IT)

```bash
# 1. Activar depuracion en la pantalla:
#    Ajustes → Acerca del dispositivo → pulsar 7 veces en "Numero de compilacion"
#    Ajustes → Opciones de desarrollador → Depuracion por red (ON)

# 2. Conectar desde el PC (misma red)
adb connect <IP_PANTALLA>:5555

# 3. Instalar
adb install -r app/build/outputs/apk/release/AutoLogin-v1.0.XX-release.apk

# 4. Verificar instalacion
adb shell pm list packages | grep autologin
```

### Opcion B: USB

1. Copiar el APK a un pendrive USB
2. Conectar el USB a la pantalla Samsung WAF
3. Abrir **Mis archivos** desde el menu de apps
4. Navegar al USB → pulsar sobre el APK
5. Cuando pregunte, activar **"Permitir instalar apps de esta fuente"**
6. Confirmar instalacion

## Actualizaciones (versiones posteriores)

La app incluye auto-update via GitHub Releases. Una vez instalada la primera version:

1. La app comprueba automaticamente si hay una version nueva al abrirse
2. Si hay actualizacion, muestra un boton **"Actualizar a v1.0.XX"** en el pie de pantalla
3. El usuario pulsa el boton → se descarga el APK con progreso visual
4. Al completar, se abre el instalador de Android → confirmar instalacion

### Publicar una nueva version en GitHub

```bash
# Desde la raiz del proyecto, tras generar el APK release:
gh release create v$(git rev-list --count HEAD) \
  app/build/outputs/apk/release/AutoLogin-v1.0.$(git rev-list --count HEAD)-release.apk \
  --title "AutoLogin v1.0.$(git rev-list --count HEAD)" \
  --notes "Descripcion de los cambios"
```

**Importante:** El asset adjunto al release puede tener cualquier nombre. El tag debe seguir el formato `v<numero>` donde el numero corresponde al `versionCode` (numero de commits).

## Verificacion post-instalacion

- [ ] La app se abre correctamente
- [ ] Se muestra la pantalla de login con el boton "Iniciar Sesion con Microsoft"
- [ ] Login passwordless funciona (number matching)
- [ ] SSO automatico en Teams, Edge, M365 Copilot
- [ ] SSO parcial en Word, Excel, OneDrive (pide correo, no password)
- [ ] Logout cierra sesion en todas las apps
- [ ] La version se muestra correctamente en el pie de pantalla

## Solucion de problemas

| Problema | Solucion |
|----------|----------|
| "App no instalada" | Verificar que el APK esta firmado con la keystore correcta |
| No aparece boton de actualizar | Comprobar conexion a internet y que existe un release en GitHub con versionCode superior |
| "Instalar apps desconocidas" bloqueado | Ajustes → Apps → Permisos especiales → Instalar apps desconocidas → activar para la fuente |
| ADB no conecta | Verificar que la pantalla y el PC estan en la misma red, y que la depuracion por red esta activa |
| Login falla | Verificar que Microsoft Authenticator esta instalado y que el hash del APK esta registrado en Entra ID |
| Cerrar sesion no cierra Teams/Edge | El dispositivo no esta en Shared Device Mode. Seguir la seccion "Configurar Shared Device Mode" de esta guia |
| `isSharedDevice = false` en logs | Microsoft Authenticator no tiene el dispositivo registrado como compartido. Registrarlo con una cuenta Cloud Device Administrator |
