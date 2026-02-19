# Configuracion de Microsoft Entra ID - Guia Completa

> Esta guia asume que tienes permisos de **Global Administrator** o **Application Administrator** en el tenant de Microsoft 365.

---

## Datos de Configuracion

| Parametro | Valor |
|---|---|
| Application (Client) ID | `<ENTRA_CLIENT_ID>` |
| Directory (Tenant) ID | `<ENTRA_TENANT_ID>` |
| Redirect URI (auth_config.json) | `msauth://com.autologin.app/<SIGNATURE_HASH_URLENCODED>` |
| Signature Hash (portal Entra / manifest) | `<SIGNATURE_HASH_RAW>` |
| Package Name | `com.autologin.app` |
| Cuenta compartida | `shared-screen-account@example.com` |
| Cuenta Cloud Device Admin | `cloud-device-admin@example.com` |
| Rol del admin | Administrador de dispositivos en la nube |

> **IMPORTANTE**: El Redirect URI en `auth_config.json` usa el hash URL-encoded (`%2F`, `%2B`, `%3D`), pero en el portal de Entra ID y en `AndroidManifest.xml` (`android:path`) se usa el hash raw con caracteres `/`, `+`, `=`.

---

## Paso 1: Acceder al Portal de Entra ID

1. Abre el navegador y ve a: **https://entra.microsoft.com**
2. Inicia sesion con una cuenta de administrador del tenant
3. En el menu lateral izquierdo, navega a: **Identity** > **Applications** > **App registrations**

---

## Paso 2: Crear el App Registration

1. Haz clic en **"+ New registration"** (boton superior)
2. Rellena los campos:
   - **Name**: `AutoLogin SSO Manager`
   - **Supported account types**: Selecciona **"Accounts in this organizational directory only (Single tenant)"**
   - **Redirect URI**: Deja vacio por ahora (lo configuraremos en el paso 4)
3. Haz clic en **"Register"**

### Resultado:
Se crea la app y se muestra la pagina "Overview". Los valores generados son:
- **Application (client) ID**: `<ENTRA_CLIENT_ID>`
- **Directory (tenant) ID**: `<ENTRA_TENANT_ID>`

---

## Paso 3: Habilitar Public Client Flows

1. En la pagina de tu app registration, ve al menu lateral: **Authentication**
2. Scroll hasta la seccion **"Advanced settings"**
3. En **"Allow public client flows"**: Selecciona **"Yes"**
4. Haz clic en **"Save"** (boton superior)

> Esto es OBLIGATORIO para apps Android/iOS (public clients) que no pueden almacenar un client secret de forma segura.

---

## Paso 4: Configurar la Plataforma Android

### 4.1 Obtener el Signature Hash

En tu maquina de desarrollo, ejecuta en la terminal:

**Para debug keystore:**
```bash
keytool -exportcert -alias androiddebugkey \
  -keystore ~/.android/debug.keystore \
  | openssl sha1 -binary | openssl base64
```
- Password por defecto: `android`

**Para release keystore:**
```bash
keytool -exportcert -alias <TU_ALIAS> \
  -keystore <RUTA_A_TU_KEYSTORE> \
  | openssl sha1 -binary | openssl base64
```

**Resultado actual**: `<SIGNATURE_HASH_RAW>`

> **LECCION APRENDIDA**: El hash de debug y release son diferentes. Si usas el hash de debug en Entra ID y luego compilas en release (o viceversa), obtendras error de redirect_uri mismatch. MSAL muestra el hash correcto en su mensaje de error - usalo para corregir.

### 4.2 Registrar la Plataforma Android en Entra ID

1. En tu app registration, ve a **Authentication**
2. Haz clic en **"+ Add a platform"**
3. Selecciona **"Android"**
4. Rellena:
   - **Package name**: `com.autologin.app`
   - **Signature hash**: `<SIGNATURE_HASH_RAW>` (hash raw, NO URL-encoded)
5. Haz clic en **"Configure"**

### Resultado:
Se genera automaticamente el Redirect URI:
```
msauth://com.autologin.app/<SIGNATURE_HASH_URLENCODED>
```

---

## Paso 5: Configurar Permisos de API

1. En tu app registration, ve a **API permissions**
2. Haz clic en **"+ Add a permission"**
3. Selecciona **"Microsoft Graph"**
4. Selecciona **"Delegated permissions"**
5. Busca y selecciona los siguientes permisos:
   - `openid` (en la seccion "OpenId permissions")
   - `profile` (en la seccion "OpenId permissions")
   - `offline_access` (en la seccion "OpenId permissions")
   - `User.Read` (en la seccion "User")
6. Haz clic en **"Add permissions"**

### Resultado esperado en la lista de permisos:

| API | Permiso | Tipo | Estado |
|---|---|---|---|
| Microsoft Graph | `openid` | Delegated | Granted |
| Microsoft Graph | `profile` | Delegated | Granted |
| Microsoft Graph | `offline_access` | Delegated | Granted |
| Microsoft Graph | `User.Read` | Delegated | Granted |

### 5.1 Conceder Admin Consent

1. Haz clic en **"Grant admin consent for [Tu Organizacion]"**
2. Confirma en el dialogo

> Esto elimina la necesidad de que cada usuario individualmente consienta los permisos.

### Resultado:
La columna "Status" de todos los permisos debe mostrar un check verde: **"Granted for [Tu Organizacion]"**

---

## Paso 6: Desactivar Security Defaults

Security Defaults debe estar **desactivado** en el tenant. Es necesario para poder configurar los metodos de autenticacion (passwordless con Authenticator).

1. Ve a **Identity** > **Overview** > **Properties**
2. Haz clic en **"Manage security defaults"**
3. Selecciona **"Disabled"**
4. Indica un motivo (ej: "Usando Shared Device Mode para pantallas interactivas")
5. Haz clic en **"Save"**

> **NOTA**: Security Defaults fuerza MFA basico para todos los usuarios. Para usar autenticacion passwordless con Authenticator, necesitamos desactivarlo y configurar los metodos de autenticacion manualmente. NO se usan Conditional Access policies porque Samsung WAF no soporta enrollment ni compliance.

---

## Paso 7: Configurar Metodos de Autenticacion Passwordless

Esta seccion configura la autenticacion sin contrasena con Microsoft Authenticator para todo el tenant.

### 7.1 Habilitar Microsoft Authenticator Passwordless

1. Ve a **Entra ID** > **Proteccion** > **Metodos de autenticacion** > **Directivas**
2. Selecciona **Microsoft Authenticator**
3. Configurar:
   - **Habilitado**: **Si**
   - **Target**: **Todos los usuarios**
   - **Modo de autenticacion**: **Sin contrasena** (Passwordless)
   - **Permitir el uso de Microsoft Authenticator OTP**: **Si**
4. Haz clic en **Guardar**

> **IMPORTANTE**: El modo "Sin contrasena" habilita tanto notificaciones push como number matching. Number matching esta habilitado por defecto desde 2023 - no requiere configuracion adicional.

---

## Paso 8: Configurar Passwordless para Cuenta Compartida

Este es el proceso **probado y confirmado** para configurar autenticacion sin contrasena para una cuenta (ej: `shared-screen-account@example.com`).

### Requisitos previos por cuenta:
- La cuenta debe tener licencia Microsoft 365 Business Premium
- Se necesita un **telefono movil** con Microsoft Authenticator instalado (NO la pantalla Samsung WAF)
- Se necesita acceso a un **portatil/PC con navegador** para la configuracion inicial

### 8.1 Registrar Authenticator en el telefono movil

1. Desde un **portatil**, abre el navegador y ve a: **https://aka.ms/mysecurityinfo**
2. Inicia sesion con la cuenta: `shared-screen-account@example.com`
3. Haz clic en **"+ Agregar metodo de inicio de sesion"** (Add sign-in method)
4. Selecciona **"Microsoft Authenticator"**
5. En el telefono movil, abre **Microsoft Authenticator**
6. Pulsa **"+"** > **"Cuenta profesional o educativa"** > **"Escanear codigo QR"**
7. Escanea el codigo QR que aparece en el portatil
8. Verifica que la cuenta aparece con un **icono de maletin** (briefcase) en Authenticator

> **CRITICO**: Si la cuenta aparece con un icono **X** en vez de maletin, se registro como TOTP solamente, no como notificacion push. Hay que eliminarla y volver a registrarla como "Cuenta profesional" via QR.

### 8.2 Habilitar inicio de sesion sin contrasena

1. En Authenticator en el telefono, toca la cuenta con icono de maletin
2. Busca la opcion **"Configuracion de solicitudes de inicio de sesion sin contrasena"**
3. Activa la opcion
4. Cuando pida verificar identidad: **anota el codigo TOTP** que muestra Authenticator (6 digitos), cambia rapidamente al navegador del portatil e introducelo
5. Una vez habilitado, vuelve a **https://aka.ms/mysecurityinfo** en el portatil
6. Cambia el metodo de inicio de sesion predeterminado a **"Microsoft Authenticator - notificacion"**

### 8.3 Verificar que funciona

1. Abre una ventana de navegador en modo incognito
2. Ve a **https://portal.office.com**
3. Introduce el email: `shared-screen-account@example.com`
4. En vez de pedir contrasena, debe mostrar un **numero de 2 digitos**
5. En el telefono movil, Authenticator muestra una notificacion pidiendo ese numero
6. Introduce el numero en el telefono - la autenticacion se completa

---

## Troubleshooting de Autenticacion Passwordless

### La opcion "Sin contrasena" no aparece en Authenticator

- La cuenta se registro como TOTP solamente (icono X en vez de maletin)
- **Solucion**: Eliminar la cuenta de Authenticator, volver a registrarla desde https://aka.ms/mysecurityinfo como "Cuenta profesional" via QR

### No aparece "Configuracion de solicitudes de inicio de sesion sin contrasena"

- La cuenta no se registro correctamente como notificacion push
- **Solucion**: Eliminar y re-registrar la cuenta en Authenticator

### No puedo introducir el TOTP durante la verificacion

- El codigo TOTP cambia cada 30 segundos
- **Solucion**: Anota el codigo de 6 digitos de Authenticator, cambia rapidamente al navegador e introducelo antes de que expire

### OPCION NUCLEAR - Si nada funciona

Este proceso **siempre** resuelve los problemas de passwordless:

1. Como **ADMIN** en Entra ID, ve a **Usuarios** > `[usuario]` > **Metodos de autenticacion**
2. Haz clic en **"Requerir volver a registrar MFA"** (Require re-register MFA)
3. Esto **borra TODOS los metodos de autenticacion** del usuario
4. En el telefono movil, abre Authenticator y **elimina** todas las cuentas de ese usuario
5. Desde un portatil, ve a **https://aka.ms/mysecurityinfo** e inicia sesion con el usuario
6. El sistema **forzara** el registro de un nuevo metodo de autenticacion
7. Durante el re-registro, selecciona Microsoft Authenticator como "Cuenta profesional" via QR
8. Esta vez se registrara correctamente como notificacion push + passwordless
9. Despues de registrar, habilita "inicio de sesion sin contrasena" en la app Authenticator

> **NOTA**: Algunos usuarios obtienen number matching inmediatamente al registrarse. Otros necesitan el reset de MFA. Siempre intenta primero el flujo normal (pasos 8.1-8.2). Usa la opcion nuclear solo como ultimo recurso.

---

## Paso 9: Verificar la Configuracion Completa

### Checklist de Verificacion:

| Elemento | Valor | Verificado |
|---|---|---|
| Application (client) ID | `<ENTRA_CLIENT_ID>` | [x] |
| Directory (tenant) ID | `<ENTRA_TENANT_ID>` | [x] |
| Redirect URI | `msauth://com.autologin.app/<SIGNATURE_HASH_URLENCODED>` | [x] |
| Allow public client flows | Yes | [x] |
| Plataforma Android configurada | Si | [x] |
| Permisos: openid | Granted | [x] |
| Permisos: profile | Granted | [x] |
| Permisos: offline_access | Granted | [x] |
| Permisos: User.Read | Granted | [x] |
| Admin consent | Granted | [x] |
| Security Defaults | Desactivado | [x] |
| Authenticator passwordless | Habilitado, modo "Sin contrasena" | [x] |
| OTP habilitado | Si | [x] |
| Sin Conditional Access de compliance | Eliminada | [x] |
| Sin App Protection Policies (MAM) | Excluidas de dispositivos compartidos | [x] |

---

## Paso 10: Valores para auth_config.json

Con toda la configuracion completa, estos son los valores para el archivo `auth_config.json` de la app Android:

```json
{
  "client_id": "<ENTRA_CLIENT_ID>",
  "redirect_uri": "msauth://com.autologin.app/<SIGNATURE_HASH_URLENCODED>",
  "broker_redirect_uri_registered": true,
  "authorization_user_agent": "DEFAULT",
  "account_mode": "SINGLE",
  "shared_device_mode_supported": true,
  "authorities": [
    {
      "type": "AAD",
      "audience": {
        "type": "AzureADMyOrg",
        "tenant_id": "<ENTRA_TENANT_ID>"
      },
      "default": true
    }
  ]
}
```

Y en `AndroidManifest.xml`, el `android:path` del BrowserTabActivity usa el hash **raw** (NO URL-encoded):

```xml
<activity android:name="com.microsoft.identity.client.BrowserTabActivity">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:host="com.autologin.app"
            android:path="/<SIGNATURE_HASH_RAW>"
            android:scheme="msauth" />
    </intent-filter>
</activity>
```

---

## Setup de Nueva Pantalla Samsung WAF

Guia paso a paso para configurar una nueva pantalla Samsung WAF Interactive Display con Shared Device Mode y SSO.

### Prerequisitos

- Acceso a Google Play Store en la pantalla (GMS habilitado)
- Cuenta `cloud-device-admin@example.com` con rol "Administrador de dispositivos en la nube"
- APK de AutoLogin disponible (sideload o Play Store privado)
- Authenticator passwordless ya configurado para la cuenta compartida (ver Paso 8)

### Paso A: Instalar apps base

1. Abre **Google Play Store** en la pantalla Samsung WAF
2. Instala **Microsoft Authenticator**
3. Instala **Company Portal** (Portal de empresa de Intune)

> **IMPORTANTE**: Company Portal debe estar instalado junto con Authenticator para que el modo compartido se active correctamente. Ambas apps son OBLIGATORIAS.

### Paso B: Registrar como dispositivo compartido

1. Abre **Microsoft Authenticator** en la pantalla
2. En la **primera pantalla** (antes de agregar ninguna cuenta), busca la opcion **"Registrar como dispositivo compartido"**
3. Inicia sesion con la cuenta de Cloud Device Administrator: `cloud-device-admin@example.com`
4. Espera a que se complete el registro
5. El dispositivo queda registrado como dispositivo compartido y recibe un Device ID

> **CRITICO**: La opcion "Registrar como dispositivo compartido" SOLO aparece en la primera pantalla de Authenticator, antes de agregar cualquier cuenta. Si ya se agrego una cuenta, hay que **desinstalar y reinstalar** Authenticator para que aparezca la opcion. Company Portal tambien debe estar instalado.

### Paso C: Instalar AutoLogin

1. Instala el APK de AutoLogin mediante sideload (USB/descarga)
2. Abre AutoLogin para verificar que detecta el modo compartido

### Paso D: Instalar apps Microsoft

**Apps con SSO completo** (se autentican automaticamente sin intervencion):
1. **Microsoft 365 Copilot** - Google Play Store
2. **Microsoft Teams** - Google Play Store
3. **Microsoft Edge** - Google Play Store

**Apps con SSO parcial** (el usuario introduce su email, sin contrasena):
4. **Microsoft Word** - Google Play Store (opcional)
5. **Microsoft Excel** - Google Play Store (opcional)
6. **Microsoft OneDrive** - Google Play Store (opcional)
7. **Microsoft PowerPoint** - Google Play Store (opcional)

### Paso E: Verificar funcionamiento

1. Abre **AutoLogin**
2. Pulsa **"Iniciar Sesion"**
3. Autenticate con la cuenta compartida (`shared-screen-account@example.com`)
4. En el telefono movil aparece un numero en pantalla - introducelo en Authenticator del movil
5. Verifica que SSO funciona:
   - Abre **M365 Copilot** - debe iniciar sesion automaticamente
   - Abre **Teams** - debe iniciar sesion automaticamente
   - Abre **Edge** - debe iniciar sesion automaticamente
6. Verifica logout:
   - Vuelve a AutoLogin
   - Pulsa **"Cerrar Sesion"**
   - Verifica que Teams, M365 Copilot y Edge cierran sesion

---

## Compatibilidad SSO Confirmada en Samsung WAF

| App | Tipo SSO | Experiencia del usuario |
|-----|----------|------------------------|
| Microsoft 365 Copilot | COMPLETO (automatico) | No requiere accion - inicia sesion solo |
| Microsoft Teams | COMPLETO (automatico) | No requiere accion - inicia sesion solo |
| Microsoft Edge | COMPLETO (automatico) | No requiere accion - inicia sesion solo |
| Word (standalone) | PARCIAL | Usuario introduce email, sin contrasena |
| Excel (standalone) | PARCIAL | Usuario introduce email, sin contrasena |
| OneDrive (standalone) | PARCIAL | Usuario introduce email, sin contrasena |
| PowerPoint (standalone) | PARCIAL | Usuario introduce email, sin contrasena |
| SharePoint | PARCIAL | Usuario introduce email, sin contrasena |
| To Do | PARCIAL | Usuario introduce email, sin contrasena |

**SSO COMPLETO**: Apps "shared device mode aware" - se autentican y cierran sesion automaticamente.
**SSO PARCIAL**: Apps que usan el broker PRT pero no son SDM-aware. El usuario ve su email pre-rellenado y confirma sin contrasena.

---

## Notas sobre App Protection Policies (MAM)

> **ADVERTENCIA**: Las App Protection Policies (MAM) en Intune **NO deben aplicarse** a dispositivos compartidos. Las politicas MAM bloquean el SSO en modo compartido porque requieren que cada app individualmente gestione su propia sesion protegida (PIN, biometrics), lo cual entra en conflicto con el global sign-in/sign-out de Shared Device Mode.

Si tienes App Protection Policies configuradas en Intune:
1. Ve a **Intune** > **Apps** > **App protection policies**
2. Edita cada politica aplicable
3. Excluye el grupo de dispositivos compartidos o la cuenta compartida del scope de la politica

**Sintoma**: Teams pide PIN al abrirse, o SSO no propaga entre apps.
**Causa**: App Protection Policy activa para la cuenta/dispositivo.
**Solucion**: Excluir del scope de MAM.

---

## Problemas Comunes y Soluciones

### Error: "AADSTS700016: Application not found"
- Verifica que el Client ID es correcto
- Verifica que estas usando el tenant correcto
- Espera unos minutos - la propagacion puede tardar hasta 5 min

### Error: "redirect_uri does not match"
- Verifica que el package name en Entra ID coincide EXACTAMENTE con el de tu app
- Verifica que el signature hash corresponde al keystore con el que firmaste la app
- **Debug y release usan keystores diferentes** - el hash sera diferente para cada uno
- MSAL muestra el hash esperado en el mensaje de error - usalo para actualizar Entra ID
- En `auth_config.json` el hash va URL-encoded; en `AndroidManifest.xml` (`android:path`) va raw

### Error: "AADSTS65001: User has not consented"
- Asegurate de haber completado el paso 5.1 (Grant admin consent)

### Error: "Broker not installed"
- El dispositivo necesita Microsoft Authenticator Y Company Portal instalados
- Verifica que ambas apps estan actualizadas a la ultima version

### Error: "SharedDeviceMode not available"
- Authenticator no fue configurado en modo compartido
- Reinstala Authenticator y registra como dispositivo compartido ANTES de agregar cuentas
- Verifica que Company Portal esta instalado

### Error: "Cannot call getCurrentAccount on main thread"
- `getCurrentAccount()` de MSAL no puede ejecutarse en el hilo principal
- Usar `withContext(Dispatchers.IO)` para llamar a cualquier metodo de MSAL

### Error: "An account is already signed in"
- Ya hay una cuenta activa en el broker
- Verificar si hay una cuenta existente con `getCurrentAccount()` antes de llamar `signIn()`

### Error: "El soporte tecnico debe asignar licencia" en Company Portal
- La cuenta necesita licencia Microsoft 365 Business Premium
- Asignar la licencia desde el admin center de Microsoft 365

### Error: "No se puede anadir perfil de trabajo" en Company Portal
- Samsung WAF no soporta Work Profile
- No es necesario enrollar en Company Portal - solo necesita estar instalado
- El enrollment NO es requerido para Shared Device Mode

### SSO no funciona en apps Office standalone (Word, Excel, etc.)
- Las apps Office standalone no son "shared device mode aware"
- Solo proporcionan SSO parcial: el usuario ve su email pero debe confirmar, sin introducir contrasena
- Para SSO completo, usar Teams, Edge o M365 Copilot

### Teams pide PIN al abrirse
- App Protection Policy (MAM) activa para la cuenta
- Excluir dispositivos compartidos del scope de la politica MAM en Intune

### Authenticator muestra cuenta con icono X en vez de maletin
- La cuenta se registro como TOTP solamente, no como notificacion push
- Eliminar la cuenta de Authenticator y volver a registrarla como "Cuenta profesional" via QR

### Passwordless no se puede configurar
- Usar la opcion nuclear: Reset de MFA en el panel admin de Entra ID, re-registrar Authenticator desde cero (ver seccion Troubleshooting de Autenticacion Passwordless)

### La opcion "Registrar como dispositivo compartido" no aparece en Authenticator
- Debe estar en la PRIMERA pantalla antes de agregar cualquier cuenta
- Desinstalar y reinstalar Authenticator
- Verificar que Company Portal esta instalado

---

## Flujo de Cierre de Sesion

1. El usuario abre AutoLogin y pulsa **"Cerrar Sesion"**
2. MSAL ejecuta `signOut()` que revoca el PRT del broker (global sign-out)
3. La app mata los procesos en background de las siguientes apps Microsoft:
   - Microsoft Teams
   - Microsoft Edge
   - Microsoft 365 Copilot
   - Microsoft Word
   - Microsoft Excel
   - Microsoft OneDrive
   - Microsoft PowerPoint
   - Microsoft SharePoint
   - Microsoft To Do
   - Microsoft OneNote
4. Todas las apps vuelven a estado no autenticado
5. El sistema queda limpio para el siguiente usuario

---

## Referencias

- [Microsoft Entra ID App Registration](https://learn.microsoft.com/en-us/entra/identity-platform/quickstart-register-app)
- [MSAL Android Configuration](https://learn.microsoft.com/en-us/entra/msal/android/msal-configuration)
- [Shared Device Mode for Android](https://learn.microsoft.com/en-us/entra/identity-platform/msal-android-shared-devices)
- [Microsoft Authenticator Shared Device Mode](https://learn.microsoft.com/en-us/entra/identity-platform/concept-shared-device-mode)
- [Passwordless Authentication Methods](https://learn.microsoft.com/en-us/entra/identity/authentication/concept-authentication-passwordless)
