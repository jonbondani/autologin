# AutoLogin - SSO Manager para Samsung WAF Interactive Displays

App Android empresarial que centraliza la autenticacion Microsoft 365 en pantallas interactivas Samsung WAF, habilitando Single Sign-On (SSO) automatico en apps Microsoft mediante Shared Device Mode.

## Que hace

1. El usuario abre AutoLogin en la pantalla Samsung WAF y pulsa **Login**
2. Se autentica con la cuenta compartida via **passwordless** (number matching con Authenticator en un movil)
3. El broker de Microsoft (Authenticator) registra un **global sign-in** y almacena un Primary Refresh Token (PRT)
4. La pantalla principal muestra las apps en dos columnas: **acceso automatico** y **requiere identificacion**, cada una con un boton para abrirla directamente
5. Al pulsar **Logout**, se ejecuta un **global sign-out** que revoca el PRT, cierra sesion en todas las apps y mata sus procesos en background
6. El usuario puede enviar un **log de errores** al equipo de IT directamente desde la app

## Compatibilidad SSO

| App | Tipo SSO | Experiencia del usuario |
|---|---|---|
| Microsoft 365 Copilot | Completo | Login/logout automatico, sin intervencion |
| Microsoft Teams | Completo | Login/logout automatico, sin intervencion |
| Microsoft Edge | Completo | Login/logout automatico, sin intervencion |
| Microsoft Word | Parcial | Email visible, confirmar sin contrasena |
| Microsoft Excel | Parcial | Email visible, confirmar sin contrasena |
| Microsoft OneDrive | Parcial | Email visible, confirmar sin contrasena |
| Microsoft PowerPoint | Parcial | Email visible, confirmar sin contrasena |
| Microsoft SharePoint | Parcial | Email visible, confirmar sin contrasena |
| Microsoft To Do | Parcial | Email visible, confirmar sin contrasena |

**SSO Completo**: Apps "shared device mode aware" - se autentican y cierran sesion automaticamente.
**SSO Parcial**: Apps que usan el broker PRT pero no son SDM-aware. El usuario ve su email pre-rellenado y confirma sin contrasena.

## Arquitectura

```
+-------------------------------+
|         AutoLogin App          |
|  +-------+ +--------+ +-----+ |
|  | Login | | Logout | | Log | |
|  +---+---+ +---+----+ +--+--+ |
|      |         |         |     |
|      +----+----+         |     |
|           |              |     |
|     +-----v-----+  +----v---+ |
|     |   MSAL    |  |  Room  | |
|     |  signIn() |  |   DB   | |
|     | signOut() |  +--------+ |
|     +-----+-----+             |
+-----------|--------------------+
            |
    +-------v--------+
    |  Broker (PRT)  |
    |  Authenticator |
    |  + Company     |
    |    Portal      |
    +-------+--------+
            |
    +-------v--------+
    |  Microsoft     |
    |  Entra ID      |
    +-------+--------+
            |
    +-------v--------+
    |  SSO -> Teams, |
    |  Edge, M365,   |
    |  Word, Excel.. |
    +----------------+
```

## Tech Stack

| Componente | Tecnologia |
|---|---|
| Lenguaje | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Autenticacion | MSAL Android v8.x (Shared Device Mode) |
| Base de datos local | Room |
| Inyeccion de dependencias | Hilt |
| Arquitectura | MVVM + Clean Architecture |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 35 |

## Dispositivo Objetivo

| Parametro | Valor |
|---|---|
| Modelo | Samsung WAF Interactive Display 65" |
| Android | 14 (API 34) |
| Modo | Shared Device Mode (NO Intune enrollment) |
| Autenticacion | Passwordless con Authenticator number matching |

---

## Setup Completo para Nueva Pantalla Samsung WAF

### Prerequisitos (configuracion unica, ya realizada)

**En Microsoft Entra ID:**
- App Registration "AutoLogin" con Client ID: `678488cf-7a78-4487-bb96-76f479a4967a`
- Tenant ID: `909c0d8a-dd01-4fe7-ac8a-f336e540fdfa`
- Plataforma Android configurada con signature hash
- Permisos: openid, profile, offline_access, User.Read (con admin consent)
- Security Defaults **desactivados**
- Authenticator passwordless habilitado en modo "Sin contrasena" para todos los usuarios
- OTP habilitado en Microsoft Authenticator
- Sin Conditional Access policies de compliance
- Sin App Protection Policies (MAM) para dispositivos compartidos
- Cuenta admin: `adminprestige@prestige-expo.com` con rol "Administrador de dispositivos en la nube"
- Cuenta compartida: `pantallas@prestige-expo.com` con licencia M365 Business Premium

**Passwordless configurado** para la cuenta compartida (ver guia detallada en [docs/ENTRA_ID_CONFIGURATION.md](docs/ENTRA_ID_CONFIGURATION.md)):
- Authenticator instalado en un telefono movil con la cuenta como "Cuenta profesional" (icono maletin)
- Inicio de sesion sin contrasena habilitado en Authenticator
- Number matching verificado y funcionando

### Pasos por pantalla (repetir para cada Samsung WAF)

1. Instalar **Microsoft Authenticator** desde Google Play
2. Instalar **Company Portal** desde Google Play (OBLIGATORIO - SDM no funciona sin ambas apps)
3. Abrir Authenticator > en la **primera pantalla** (antes de agregar cuentas) seleccionar **"Registrar como dispositivo compartido"**
4. Autenticarse con `adminprestige@prestige-expo.com` (Cloud Device Administrator)
5. Esperar a que se complete el registro (el dispositivo recibe un Device ID)
6. Instalar **AutoLogin** (APK via sideload)
7. Instalar **Microsoft 365 Copilot**, **Microsoft Teams**, **Microsoft Edge** desde Google Play
8. Opcional: instalar Word, Excel, OneDrive, PowerPoint (solo SSO parcial)
9. Verificar: Abrir AutoLogin > Iniciar Sesion > `pantallas@prestige-expo.com` > numero aparece en pantalla > aprobarlo en Authenticator del movil
10. Verificar SSO: abrir Teams, M365 Copilot, Edge - deben iniciar sesion automaticamente

> **CRITICO**: La opcion "Registrar como dispositivo compartido" SOLO aparece antes de agregar cualquier cuenta. Si no aparece, desinstalar y reinstalar Authenticator. Company Portal debe estar instalado previamente.

---

## Autenticacion Passwordless

Las pantallas compartidas usan autenticacion sin contrasena con number matching:

1. El usuario introduce el email `pantallas@prestige-expo.com` en la pantalla
2. La pantalla muestra un numero de 2 digitos
3. Authenticator en el telefono movil muestra una notificacion
4. El usuario introduce el numero en Authenticator del movil
5. Se completa el login y se activa SSO en todas las apps

**Si passwordless no funciona**, ver la seccion de troubleshooting en [docs/ENTRA_ID_CONFIGURATION.md](docs/ENTRA_ID_CONFIGURATION.md), incluyendo la "opcion nuclear" de reset de MFA.

---

## Flujo de Cierre de Sesion

1. El usuario abre AutoLogin y pulsa **"Cerrar Sesion"**
2. MSAL ejecuta global sign-out (revoca PRT del broker)
3. La app mata los procesos en background de todas las apps Microsoft: Teams, Edge, M365 Copilot, Word, Excel, OneDrive, PowerPoint, SharePoint, To Do, OneNote
4. Todas las apps vuelven a estado no autenticado
5. El sistema queda limpio para el siguiente usuario

---

## Requisitos Previos

### En el dispositivo
- Android 7.0+ (API 24) con GMS habilitado
- Microsoft Authenticator instalado (registrado como dispositivo compartido)
- Company Portal instalado (sin enrollment - solo presencia)

### En el tenant Microsoft
- Microsoft 365 Business Premium (o superior)
- App registrada en Microsoft Entra ID (Client ID: `678488cf-7a78-4487-bb96-76f479a4967a`)
- Security Defaults desactivados
- Authenticator passwordless habilitado en modo "Sin contrasena"
- Sin App Protection Policies (MAM) aplicadas a dispositivos compartidos
- Sin Conditional Access policies de compliance (Samsung WAF no soporta enrollment)

---

## Problemas Comunes y Soluciones

| # | Problema | Solucion |
|---|---|---|
| 1 | "No se puede anadir perfil de trabajo" | Samsung WAF no soporta Work Profile. Usar Shared Device Mode |
| 2 | "Registrar como dispositivo compartido" no aparece | Reinstalar Authenticator. Debe ser primera pantalla, sin cuentas previas. Company Portal debe estar instalado |
| 3 | "Security Defaults must be disabled" | Desactivar en Entra ID > Properties > Security Defaults |
| 4 | Redirect URI no coincide | Debug/release usan keystores diferentes. Usar hash del error de MSAL |
| 5 | Crash en getCurrentAccount | Envolver en withContext(Dispatchers.IO) |
| 6 | "An account is already signed in" | Verificar cuenta existente antes de signIn() |
| 7 | "El soporte tecnico debe asignar licencia" | Asignar licencia M365 Business Premium a la cuenta |
| 8 | SSO no funciona en Word/Excel | Son SSO parcial - usuario introduce email sin contrasena |
| 9 | Teams pide PIN | Excluir dispositivos compartidos de App Protection Policies (MAM) |
| 10 | Authenticator muestra icono X | Re-registrar como "Cuenta profesional" via QR |
| 11 | Passwordless no se puede configurar | Reset nuclear de MFA en Entra ID admin + re-registro |
| 12 | SSO no propaga a otras apps | Usar signIn() en vez de acquireToken() |

---

## Documentacion

| Documento | Descripcion |
|---|---|
| [Configuracion Entra ID](docs/ENTRA_ID_CONFIGURATION.md) | Guia completa: App Registration, passwordless, setup de pantallas, troubleshooting |
| [Arquitectura Tecnica](docs/TECHNICAL_ARCHITECTURE.md) | Diseno tecnico: stack, flujos, threading, SSO |
| [Estudio de Viabilidad](docs/FEASIBILITY.md) | Analisis tecnico con resultados confirmados |
| [Plan de Proyecto](docs/PROJECT_PLAN.md) | Fases, milestones y estado de sprints |
| [Progreso](docs/PROGRESS.md) | Estado actual del proyecto y decisiones |
| [Lecciones Aprendidas](docs/LESSONS_LEARNED.md) | 27 lecciones aprendidas durante el desarrollo |
| [Skills](docs/SKILLS.md) | Competencias tecnicas: SDM, passwordless, Samsung WAF |
| [PR Template](docs/PR.md) | Template de Pull Request con estado actual |
| ~~[Configuracion Intune](docs/INTUNE_CONFIGURATION.md)~~ | **OBSOLETO** - Samsung WAF no soporta Intune enrollment |

## Estructura del Proyecto

```
autologin/
+-- app/
|   +-- src/main/
|   |   +-- java/com/autologin/app/
|   |   |   +-- di/              # Hilt modules
|   |   |   +-- data/
|   |   |   |   +-- local/       # Room DB, DAOs
|   |   |   |   +-- repository/  # Repository implementations
|   |   |   +-- domain/
|   |   |   |   +-- model/       # Domain entities
|   |   |   |   +-- repository/  # Repository interfaces
|   |   |   +-- ui/              # Compose screens
|   |   +-- res/
|   |   |   +-- raw/
|   |   |       +-- auth_config.json  # MSAL config
|   |   +-- AndroidManifest.xml
|   +-- build.gradle.kts
+-- docs/
+-- build.gradle.kts
+-- settings.gradle.kts
+-- README.md
```

## Versionado

La version se genera automaticamente a partir de los commits de git:
- **versionCode**: numero total de commits
- **versionName**: `1.0.<commits>` (ej: `1.0.14`)
- **Build info**: hash corto del commit visible en la app

## Licencia

Uso interno empresarial. Desarrollado por el Departamento de IT de Prestige-Expo.
