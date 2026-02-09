# Estudio de Viabilidad Técnica - AutoLogin Microsoft 365

## Veredicto: VIABLE

El proyecto es 100% realizable con la arquitectura de broker SSO de Microsoft para Android.

---

## 1. Mecanismo Técnico: Primary Refresh Token (PRT)

El SSO entre aplicaciones en Android funciona a través del **Primary Refresh Token (PRT)**:

1. Nuestra app autentica al usuario vía MSAL (Microsoft Authentication Library)
2. MSAL delega al **broker** instalado en el dispositivo (Authenticator o Company Portal)
3. El broker obtiene un PRT de Microsoft Entra ID y lo almacena de forma segura
4. Cuando cualquier otra app compatible con MSAL solicita un token, el broker lo proporciona **silenciosamente** usando el PRT almacenado
5. No se requiere re-autenticación del usuario

**Ref**: [Primary Refresh Token Concept](https://learn.microsoft.com/en-us/entra/identity/devices/concept-primary-refresh-token)

---

## 2. Broker Apps Soportadas en Android

| App Broker | Package Name | Notas |
|---|---|---|
| Microsoft Authenticator | `com.azure.authenticator` | Broker más común |
| Intune Company Portal | `com.microsoft.windowsintune.companyportal` | Requerido para dispositivos gestionados por Intune |
| Link to Windows | `com.microsoft.appmanager` | Tercera opción, menos común |

**Nota**: Al menos uno de estos brokers DEBE estar instalado en el dispositivo para que el SSO funcione. En un entorno empresarial con Intune, Company Portal ya estará presente.

---

## 3. Apps que se Benefician del SSO

El SSO a través del broker funciona **únicamente** con apps que usan MSAL y el broker. Esto incluye todas las apps de Microsoft 365:

- Microsoft Teams
- Microsoft Outlook
- Microsoft OneDrive
- Microsoft Word, Excel, PowerPoint
- Microsoft SharePoint
- Microsoft To Do
- Cualquier app de terceros que implemente MSAL con broker

**Limitación**: Apps que NO usan MSAL no recibirán SSO automáticamente. Esto NO es una limitación de nuestro proyecto, sino de la arquitectura de Microsoft.

---

## 4. Licencia Microsoft 365 Business Premium

| Componente Necesario | Incluido en Business Premium | Detalle |
|---|---|---|
| Microsoft Entra ID P1 | Si | Conditional Access, broker auth |
| Microsoft Intune Plan 1 | Si | MDM/MAM, gestión de dispositivos |
| Conditional Access | Si | Incluido con Entra ID P1 |
| App Registration | Si | Disponible en todos los tiers de Entra ID |
| Broker SSO | Si | Sin licencia adicional |

**Limitación conocida**: Máximo 300 usuarios en planes Business (Basic + Standard + Premium combinados por tenant).

---

## 5. Requisitos Técnicos del SDK

| Parámetro | Valor |
|---|---|
| MSAL Android (última versión) | v8.2.1 (Feb 2025) |
| minSdk | 24 (Android 7.0 Nougat) |
| targetSdk | 35 |
| Lenguaje | Kotlin |
| Repositorio Maven | `mavenCentral()` + Azure DevOps feed |

---

## 6. Limitación de Tracking de Apps

**En el dispositivo**: Android sandbox impide que nuestra app observe directamente cuándo otras apps obtienen tokens del broker.

**Soluciones viables**:
1. **Entra ID Sign-in Logs** (server-side): Microsoft registra TODOS los sign-in events. Se pueden consultar via Microsoft Graph API (`GET /auditLogs/signIns`).
2. **Registro local**: Nuestra app registra sus propios eventos de login/logout con timestamps.
3. **Lista de apps compatibles**: Detectamos apps Microsoft instaladas en el dispositivo y mostramos cuáles se benefician del SSO.

---

## 7. Riesgos Identificados

| Riesgo | Severidad | Mitigación |
|---|---|---|
| Broker no instalado en dispositivo | Media | Verificar al iniciar app; solicitar instalación |
| `BROKER_BIND_FAILURE` en dispositivos con ahorro de batería | Baja | Solicitar excepción de optimización de batería |
| PRT no vinculado a hardware en Android (sin TPM) | Baja | Protección por software; enforce compliance via Conditional Access |
| Usuario desinstala broker | Media | SSO se pierde; manejar re-autenticación gracefully |
| B2C tenants | Bloqueante | Broker NO funciona con B2C. Solo Entra ID. Confirmado que los usuarios usan Entra ID |

---

## 8. Configuración MSAL Requerida

### Redirect URI Format:
```
msauth://<PACKAGE_NAME>/<BASE64_URL_ENCODED_SIGNATURE>
```

### auth_config.json mínimo:
```json
{
  "client_id": "<CLIENT_ID>",
  "redirect_uri": "msauth://<PACKAGE_NAME>/<SIGNATURE>",
  "broker_redirect_uri_registered": true,
  "account_mode": "SINGLE",
  "authorities": [
    {
      "type": "AAD",
      "audience": {
        "type": "AzureADMyOrg",
        "tenant_id": "<TENANT_ID>"
      }
    }
  ]
}
```

---

## 9. Conclusión

No hay impedimentos técnicos ni de licenciamiento. La arquitectura broker-PRT de Microsoft está diseñada exactamente para este caso de uso. El desarrollo puede proceder.
