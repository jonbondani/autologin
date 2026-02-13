# Estudio de Viabilidad Tecnica - AutoLogin Microsoft 365

## Veredicto: VIABLE - CONFIRMADO EN PRODUCCION

El proyecto es 100% realizable con la arquitectura de broker SSO de Microsoft para Android usando **Shared Device Mode**. Verificado y funcionando en Samsung WAF Interactive Display 65" con Android 14.

> **Nota**: Este documento fue creado como estudio previo al desarrollo (2026-02-09) y actualizado con los resultados reales de la implementacion (2026-02-12). Las secciones marcadas con "CONFIRMADO" reflejan resultados probados en el dispositivo real.

---

## 1. Mecanismo Tecnico: Primary Refresh Token (PRT) - CONFIRMADO

El SSO entre aplicaciones en Android funciona a traves del **Primary Refresh Token (PRT)**:

1. Nuestra app autentica al usuario via MSAL usando `signIn()` (NO `acquireToken()`)
2. MSAL delega al **broker** instalado en el dispositivo (Authenticator + Company Portal)
3. El broker obtiene un PRT de Microsoft Entra ID y lo almacena de forma segura
4. Cuando cualquier otra app compatible con MSAL solicita un token, el broker lo proporciona **silenciosamente** usando el PRT almacenado
5. No se requiere re-autenticacion del usuario

> **DESCUBRIMIENTO DURANTE IMPLEMENTACION**: Es obligatorio usar `signIn()` en vez de `acquireToken()` para que el SSO propague globalmente. `acquireToken()` solo obtiene un token local para la app que lo llama, sin efecto en las demas apps del dispositivo.

**Ref**: [Primary Refresh Token Concept](https://learn.microsoft.com/en-us/entra/identity/devices/concept-primary-refresh-token)

---

## 2. Broker Apps Soportadas en Android - CONFIRMADO

| App Broker | Package Name | Requerida |
|---|---|---|
| Microsoft Authenticator | `com.azure.authenticator` | SI - Obligatoria |
| Intune Company Portal | `com.microsoft.windowsintune.companyportal` | SI - Obligatoria (para Shared Device Mode) |

> **DESCUBRIMIENTO**: Ambas apps (Authenticator Y Company Portal) son **obligatorias** para que Shared Device Mode funcione correctamente. Company Portal no necesita enrollment, solo estar instalada.

---

## 3. Apps que se Benefician del SSO - CONFIRMADO

### SSO Completo (automatico, sin intervencion del usuario)

Apps "shared device mode aware" que implementan el SDK de SDM:

| App | Package Name | Verificado |
|---|---|---|
| Microsoft 365 Copilot | `com.microsoft.office.officehubrow` | Si |
| Microsoft Teams | `com.microsoft.teams` | Si |
| Microsoft Edge | `com.microsoft.emmx` | Si |

### SSO Parcial (email pre-rellenado, sin contrasena)

Apps que usan el broker PRT pero NO son shared device mode aware:

| App | Package Name | Verificado |
|---|---|---|
| Microsoft Word | `com.microsoft.office.word` | Si |
| Microsoft Excel | `com.microsoft.office.excel` | Si |
| Microsoft OneDrive | `com.microsoft.skydrive` | Si |
| Microsoft PowerPoint | `com.microsoft.office.powerpoint` | Si |
| Microsoft SharePoint | `com.microsoft.sharepoint` | Si |
| Microsoft To Do | `com.microsoft.todos` | Si |

> **CONCLUSION**: Solo las apps que implementan el SDK de Shared Device Mode reciben SSO completamente automatico. Las apps Office standalone muestran el email y permiten confirmar sin contrasena, pero requieren intervencion minima del usuario.

---

## 4. Opciones de Despliegue Evaluadas

### Opcion A: Intune MDM Enrollment - NO VIABLE

| Aspecto | Resultado |
|---|---|
| Work Profile | **NO soportado** en Samsung WAF - error "No se puede anadir perfil de trabajo" |
| Fully Managed | Requiere factory reset - no viable para pantallas ya desplegadas |
| Compliance Policies | No aplicables sin enrollment |
| Conditional Access | No funciona sin reporte de compliance |
| Veredicto | **DESCARTADO** |

### Opcion B: Shared Device Mode via Authenticator - VIABLE Y CONFIRMADO

| Aspecto | Resultado |
|---|---|
| Activacion | Via Authenticator con cuenta Cloud Device Admin |
| Factory reset | NO requerido |
| Global sign-in | SI - propaga SSO a todas las apps |
| Global sign-out | SI - revoca PRT y limpia sesiones |
| Intune enrollment | NO requerido |
| Veredicto | **ADOPTADO** |

---

## 5. Licencia Microsoft 365 Business Premium - CONFIRMADO

| Componente Necesario | Incluido | Detalle |
|---|---|---|
| Microsoft Entra ID P1 | Si | Broker auth, app registration |
| Microsoft Authenticator | Si | Shared Device Mode, passwordless |
| App Registration | Si | Disponible en todos los tiers |
| Broker SSO | Si | Sin licencia adicional |
| Passwordless Auth | Si | Number matching con Authenticator |

**Limitacion conocida**: Maximo 300 usuarios en planes Business (Basic + Standard + Premium combinados por tenant).

> **NOTA**: Intune Plan 1 esta incluido en Business Premium pero no se utiliza porque Samsung WAF no soporta enrollment.

---

## 6. Requisitos Tecnicos del SDK - CONFIRMADO

| Parametro | Valor |
|---|---|
| MSAL Android | v8.x |
| minSdk | 24 (Android 7.0 Nougat) |
| targetSdk | 35 |
| Lenguaje | Kotlin |
| Repositorio Maven | `mavenCentral()` + Azure DevOps feed |
| Dispositivo | Samsung WAF Interactive Display 65", Android 14 API 34 |

---

## 7. Limitacion de Tracking de Apps - CONFIRMADO

**En el dispositivo**: Android sandbox impide que nuestra app observe directamente cuando otras apps obtienen tokens del broker.

**Soluciones viables**:
1. **Entra ID Sign-in Logs** (server-side): Microsoft registra TODOS los sign-in events. Se pueden consultar via Microsoft Graph API (`GET /auditLogs/signIns`).
2. **Registro local**: Nuestra app registra sus propios eventos de login/logout con timestamps en Room DB.
3. **Lista de apps compatibles**: Detectamos apps Microsoft instaladas en el dispositivo y mostramos cuales se benefician del SSO.

---

## 8. Autenticacion Passwordless - CONFIRMADO

La autenticacion sin contrasena funciona en Samsung WAF mediante number matching:

| Aspecto | Detalle |
|---|---|
| Metodo | Microsoft Authenticator number matching |
| Configuracion | Authenticator en telefono movil, no en la pantalla |
| Flujo | Email en pantalla > numero aparece > introducir en Authenticator del movil |
| Requisitos Entra ID | Security Defaults desactivados, Authenticator en modo "Sin contrasena" |

> **DESCUBRIMIENTO**: La configuracion de passwordless puede requerir un "reset nuclear" de MFA si el registro inicial no configura correctamente las notificaciones push. Ver [ENTRA_ID_CONFIGURATION.md](ENTRA_ID_CONFIGURATION.md) para el procedimiento detallado.

---

## 9. Riesgos Identificados y Mitigaciones

| Riesgo | Severidad | Estado | Mitigacion |
|---|---|---|---|
| Samsung WAF no soporta Work Profile | Alta | RESUELTO | Usar Shared Device Mode en vez de Intune enrollment |
| Broker no instalado en dispositivo | Media | RESUELTO | Verificacion al iniciar app; guia de setup documentada |
| MAM policies bloquean SSO | Media | RESUELTO | Excluir dispositivos compartidos del scope de MAM |
| PRT no vinculado a hardware en Android | Baja | ACEPTADO | Global sign-out como medida de seguridad principal |
| Usuario desinstala broker | Media | DOCUMENTADO | SSO se pierde; reinstalar y re-registrar como dispositivo compartido |
| Apps Office standalone sin SSO completo | Baja | ACEPTADO | SSO parcial es el maximo posible; documentado como limitacion |
| Passwordless requiere reset de MFA | Baja | DOCUMENTADO | Procedimiento nuclear documentado como ultimo recurso |

---

## 10. Conclusion

El proyecto ha sido validado en produccion:

1. **Shared Device Mode** funciona correctamente en Samsung WAF Interactive Display con Android 14
2. **SSO completo** funciona en Teams, Edge y M365 Copilot
3. **SSO parcial** funciona en Word, Excel, OneDrive, PowerPoint, SharePoint y To Do
4. **Autenticacion passwordless** funciona con number matching de Authenticator
5. **Global sign-out** limpia correctamente todas las sesiones y mata procesos en background
6. **NO se requiere** Intune enrollment, Work Profile, Compliance Policies ni Conditional Access

La arquitectura broker-PRT con Shared Device Mode es la solucion correcta para dispositivos compartidos Samsung WAF.
