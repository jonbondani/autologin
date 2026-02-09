# AutoLogin - SSO Manager para Microsoft 365 en Android

App Android empresarial que centraliza la autenticación Microsoft 365, habilitando Single Sign-On (SSO) automático en todas las apps Microsoft del dispositivo.

## Qué hace

1. El usuario abre AutoLogin y pulsa **Login**
2. Se autentica con sus credenciales Microsoft 365 (una sola vez)
3. El broker de Microsoft (Authenticator/Company Portal) almacena un Primary Refresh Token (PRT)
4. **Todas las apps Microsoft** del dispositivo (Teams, Outlook, OneDrive, etc.) inician sesión automáticamente sin requerir credenciales ni Authenticator
5. El usuario puede ver un **historial** de eventos de login/logout con fecha y hora
6. Al pulsar **Logout**, se cierra la sesión del broker y todas las apps pierden el SSO

## Arquitectura

```
┌─────────────────────────────────┐
│         AutoLogin App           │
│  ┌───────┐ ┌────────┐ ┌─────┐  │
│  │ Login │ │ Logout │ │ Log │  │
│  └───┬───┘ └───┬────┘ └──┬──┘  │
│      │         │         │      │
│      └────┬────┘         │      │
│           │              │      │
│     ┌─────▼─────┐  ┌────▼────┐ │
│     │   MSAL    │  │  Room   │ │
│     │  Android  │  │   DB    │ │
│     └─────┬─────┘  └─────────┘ │
└───────────┼─────────────────────┘
            │
    ┌───────▼────────┐
    │  Broker (PRT)  │
    │  Authenticator │
    │  / Company     │
    │    Portal      │
    └───────┬────────┘
            │
    ┌───────▼────────┐
    │  Microsoft     │
    │  Entra ID      │
    └───────┬────────┘
            │
    ┌───────▼────────┐
    │  SSO → Teams,  │
    │  Outlook, etc. │
    └────────────────┘
```

## Tech Stack

| Componente | Tecnología |
|---|---|
| Lenguaje | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Autenticación | MSAL Android v8.x |
| Base de datos local | Room |
| Inyección de dependencias | Hilt |
| Arquitectura | MVVM + Clean Architecture |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 35 |

## Requisitos Previos

### En el dispositivo
- Android 7.0+ (API 24)
- Microsoft Authenticator o Intune Company Portal instalado

### En el tenant Microsoft
- Microsoft 365 Business Premium (o superior)
- App registrada en Microsoft Entra ID
- Configuración de Intune completada

## Documentación

| Documento | Descripción |
|---|---|
| [Estudio de Viabilidad](docs/FEASIBILITY.md) | Análisis técnico de viabilidad |
| [Plan de Proyecto](docs/PROJECT_PLAN.md) | Fases, milestones, y tareas |
| [Configuración Entra ID](docs/ENTRA_ID_CONFIGURATION.md) | Guía paso a paso de Azure AD |
| [Configuración Intune](docs/INTUNE_CONFIGURATION.md) | Guía paso a paso de Intune |
| [Arquitectura Técnica](docs/TECHNICAL_ARCHITECTURE.md) | Diseño técnico detallado |
| [Progreso](docs/PROGRESS.md) | Estado actual del proyecto |
| [Lessons Learned](docs/LESSONS_LEARNED.md) | Lecciones aprendidas |
| [Skills](docs/SKILLS.md) | Competencias técnicas del proyecto |
| [PR Template](docs/PR.md) | Template de Pull Request |

## Estructura del Proyecto

```
autologin/
├── app/
│   ├── src/main/
│   │   ├── java/com/autologin/
│   │   │   ├── di/              # Hilt modules
│   │   │   ├── data/
│   │   │   │   ├── local/       # Room DB, DAOs
│   │   │   │   └── repository/  # Repository implementations
│   │   │   ├── domain/
│   │   │   │   ├── model/       # Domain entities
│   │   │   │   └── usecase/     # Business logic
│   │   │   └── ui/
│   │   │       ├── login/       # Login screen
│   │   │       ├── history/     # History screen
│   │   │       └── theme/       # Material 3 theme
│   │   ├── res/
│   │   │   └── raw/
│   │   │       └── auth_config.json  # MSAL config
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── docs/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Licencia

Uso interno empresarial.
