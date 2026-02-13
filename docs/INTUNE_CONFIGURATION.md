# ~~Configuracion de Microsoft Intune - Guia Paso a Paso~~

> **OBSOLETO - NO USAR ESTA GUIA**
>
> Este documento esta **OBSOLETO** desde el 2026-02-10. Las pantallas Samsung WAF Interactive Display **NO soportan Android Enterprise Work Profile** ni enrollment en Intune. Al intentar enrollar, el dispositivo muestra el error: *"No se puede anadir un perfil de trabajo a este dispositivo"*.
>
> **Solucion adoptada**: Se utiliza **Shared Device Mode** via Microsoft Authenticator, que no requiere enrollment en Intune ni factory reset. Ver [ENTRA_ID_CONFIGURATION.md](ENTRA_ID_CONFIGURATION.md) para la guia actual de configuracion.
>
> Este documento se conserva unicamente como referencia historica de lo que se evaluo y descarto.

---

## Por que no funciona Intune en Samsung WAF

1. **Work Profile no soportado**: Las pantallas Samsung WAF Interactive Display, aunque ejecutan Android 14 con GMS, no soportan la creacion de perfiles de trabajo de Android Enterprise.
2. **Fully Managed requiere factory reset**: El modo "Fully Managed" de Android Enterprise requiere un factory reset y enrollment durante la configuracion inicial (OOBE), lo cual no es viable para pantallas ya desplegadas.
3. **Compliance Policies sin efecto**: Sin enrollment, las Compliance Policies de Intune no se aplican al dispositivo.
4. **Conditional Access incompatible**: Las politicas de Conditional Access que requieren "dispositivo compatible" no funcionan porque el dispositivo no puede reportar su estado de cumplimiento a Intune.

## Alternativa implementada

Se usa **Shared Device Mode** activado mediante Microsoft Authenticator:
- No requiere enrollment en Intune
- No requiere factory reset
- Proporciona global sign-in/sign-out via Primary Refresh Token (PRT)
- Compatible con SSO en apps Microsoft (Teams, Edge, M365 Copilot)

Para la guia completa de configuracion, ver:
- [Configuracion Entra ID](ENTRA_ID_CONFIGURATION.md) - Guia principal de setup
- [Arquitectura Tecnica](TECHNICAL_ARCHITECTURE.md) - Detalle de la arquitectura

---

## Contenido Original (solo referencia historica)

El contenido a continuacion describe el proceso de configuracion de Intune que **NO es aplicable** a Samsung WAF Interactive Displays. Se conserva como referencia de lo que se investigo.

<details>
<summary>Hacer clic para expandir contenido obsoleto</summary>

### Prerrequisitos (NO APLICABLE)

- Tenant de Microsoft 365 con licencias Business Premium activas
- Microsoft Entra ID configurado
- Acceso al Microsoft Intune Admin Center

### Enrollment de Dispositivos Android (NO FUNCIONA EN SAMSUNG WAF)

Samsung WAF muestra "No se puede anadir un perfil de trabajo a este dispositivo" al intentar Android Enterprise Work Profile.

### Compliance Policy (NO APLICABLE)

Sin enrollment, las Compliance Policies no tienen efecto.

### App Protection Policies (NO USAR EN DISPOSITIVOS COMPARTIDOS)

Las App Protection Policies (MAM) **bloquean el SSO** en Shared Device Mode porque requieren que cada app gestione su propia sesion protegida con PIN/biometrics, lo cual entra en conflicto con el global sign-in/sign-out.

### Conditional Access (ELIMINADA)

La politica de Conditional Access que requeria "dispositivo compatible" fue eliminada porque Samsung WAF no puede reportar compliance sin enrollment.

### Distribucion LOB (ALTERNATIVA: SIDELOAD)

Sin Intune enrollment, la distribucion de la app se realiza mediante sideload (APK via USB o descarga) o Google Play Store.

</details>

---

## Referencias

- [Shared Device Mode para Android](https://learn.microsoft.com/en-us/entra/identity-platform/msal-android-shared-devices) - Alternativa usada
- [Intune Android Enterprise Enrollment](https://learn.microsoft.com/en-us/mem/intune/enrollment/android-enroll) - Referencia (no aplicable a Samsung WAF)
