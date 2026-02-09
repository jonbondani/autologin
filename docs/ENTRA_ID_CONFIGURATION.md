# Configuración de Microsoft Entra ID (Azure AD) - Guía Paso a Paso

> Esta guía asume que tienes permisos de **Global Administrator** o **Application Administrator** en el tenant de Microsoft 365.

---

## Paso 1: Acceder al Portal de Entra ID

1. Abre el navegador y ve a: **https://entra.microsoft.com**
2. Inicia sesión con una cuenta de administrador del tenant
3. En el menú lateral izquierdo, navega a: **Identity** → **Applications** → **App registrations**

---

## Paso 2: Crear el App Registration

1. Haz clic en **"+ New registration"** (botón superior)
2. Rellena los campos:
   - **Name**: `AutoLogin SSO Manager`
   - **Supported account types**: Selecciona **"Accounts in this organizational directory only (Single tenant)"**
   - **Redirect URI**: Deja vacío por ahora (lo configuraremos en el paso 4)
3. Haz clic en **"Register"**

### Resultado:
Se crea la app y se muestra la página "Overview". **Anota estos valores**:
- **Application (client) ID**: `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`
- **Directory (tenant) ID**: `yyyyyyyy-yyyy-yyyy-yyyy-yyyyyyyyyyyy`

---

## Paso 3: Habilitar Public Client Flows

1. En la página de tu app registration, ve al menú lateral: **Authentication**
2. Scroll hasta la sección **"Advanced settings"**
3. En **"Allow public client flows"**: Selecciona **"Yes"**
4. Haz clic en **"Save"** (botón superior)

> Esto es OBLIGATORIO para apps Android/iOS (public clients) que no pueden almacenar un client secret de forma segura.

---

## Paso 4: Configurar la Plataforma Android

### 4.1 Obtener el Signature Hash

En tu máquina de desarrollo, ejecuta en la terminal:

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

**Resultado**: Un hash Base64 como `1wIqXSqBj7w+h11ZifsnqwgyKrY=`

### 4.2 Registrar la Plataforma Android en Entra ID

1. En tu app registration, ve a **Authentication**
2. Haz clic en **"+ Add a platform"**
3. Selecciona **"Android"**
4. Rellena:
   - **Package name**: `com.autologin.app` (o el package name que uses)
   - **Signature hash**: El hash Base64 del paso 4.1
5. Haz clic en **"Configure"**

### Resultado:
Se genera automáticamente el **Redirect URI** con formato:
```
msauth://com.autologin.app/<URL_ENCODED_HASH>
```

**Anota este Redirect URI** — lo necesitarás en `auth_config.json`.

---

## Paso 5: Configurar Permisos de API

1. En tu app registration, ve a **API permissions**
2. Haz clic en **"+ Add a permission"**
3. Selecciona **"Microsoft Graph"**
4. Selecciona **"Delegated permissions"**
5. Busca y selecciona los siguientes permisos:
   - `openid` (en la sección "OpenId permissions")
   - `profile` (en la sección "OpenId permissions")
   - `offline_access` (en la sección "OpenId permissions")
   - `User.Read` (en la sección "User")
6. Haz clic en **"Add permissions"**

### Resultado esperado en la lista de permisos:

| API | Permiso | Tipo | Estado |
|---|---|---|---|
| Microsoft Graph | `openid` | Delegated | Granted |
| Microsoft Graph | `profile` | Delegated | Granted |
| Microsoft Graph | `offline_access` | Delegated | Granted |
| Microsoft Graph | `User.Read` | Delegated | Granted |

### 5.1 Conceder Admin Consent

1. Haz clic en **"Grant admin consent for [Tu Organización]"**
2. Confirma en el diálogo

> Esto elimina la necesidad de que cada usuario individualmente consienta los permisos.

### Resultado:
La columna "Status" de todos los permisos debe mostrar un check verde: **"Granted for [Tu Organización]"**

---

## Paso 6: Verificar la Configuración

### Checklist de Verificación:

| Elemento | Valor | Verificado |
|---|---|---|
| Application (client) ID | `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` | [ ] |
| Directory (tenant) ID | `yyyyyyyy-yyyy-yyyy-yyyy-yyyyyyyyyyyy` | [ ] |
| Redirect URI | `msauth://com.autologin.app/<HASH>` | [ ] |
| Allow public client flows | Yes | [ ] |
| Platform Android configurada | Si | [ ] |
| Permisos: openid | Granted | [ ] |
| Permisos: profile | Granted | [ ] |
| Permisos: offline_access | Granted | [ ] |
| Permisos: User.Read | Granted | [ ] |
| Admin consent | Granted | [ ] |

---

## Paso 7: Configurar Token Lifetime (Opcional pero Recomendado)

Para controlar cuánto dura la sesión SSO:

1. Ve a **Identity** → **Protection** → **Conditional Access** → **Session lifetime**
2. O configura via PowerShell con `New-AzureADPolicy`:

```powershell
# Ejemplo: Token lifetime de 8 horas (jornada laboral)
New-AzureADPolicy -Definition @(
  '{"TokenLifetimePolicy":{"Version":1,"AccessTokenLifetime":"08:00:00"}}'
) -DisplayName "AutoLogin Token Policy" -Type "TokenLifetimePolicy"
```

> **Nota**: Microsoft está deprecando token lifetime policies en favor de Conditional Access Session Controls. Usa Conditional Access para nuevas configuraciones.

### Configuración via Conditional Access (Recomendado):

1. Ve a **Identity** → **Protection** → **Conditional Access** → **Policies**
2. Haz clic en **"+ Create new policy"**
3. Nombre: `AutoLogin Session Control`
4. **Assignments**:
   - Users: Todos o grupo específico
   - Cloud apps: Selecciona "AutoLogin SSO Manager"
5. **Session**:
   - Sign-in frequency: `8 hours` (o el valor deseado)
   - Persistent browser session: `Always persistent`
6. **Enable policy**: On
7. **Create**

---

## Paso 8: Valores para auth_config.json

Con toda la configuración completa, estos son los valores que necesitas para el archivo `auth_config.json` de la app Android:

```json
{
  "client_id": "<TU_CLIENT_ID_DEL_PASO_2>",
  "redirect_uri": "msauth://com.autologin.app/<TU_HASH_URL_ENCODED>",
  "broker_redirect_uri_registered": true,
  "authorization_user_agent": "DEFAULT",
  "account_mode": "SINGLE",
  "authorities": [
    {
      "type": "AAD",
      "audience": {
        "type": "AzureADMyOrg",
        "tenant_id": "<TU_TENANT_ID_DEL_PASO_2>"
      },
      "default": true
    }
  ]
}
```

---

## Troubleshooting

### Error: "AADSTS700016: Application not found"
- Verifica que el Client ID es correcto
- Verifica que estás usando el tenant correcto
- Espera unos minutos — la propagación puede tardar hasta 5 min

### Error: "redirect_uri does not match"
- Verifica que el package name en Entra ID coincide EXACTAMENTE con el de tu app
- Verifica que el signature hash corresponde al keystore con el que firmaste la app
- Para debug builds usa el debug keystore hash; para release usa el release keystore hash

### Error: "AADSTS65001: User has not consented"
- Asegúrate de haber completado el paso 5.1 (Grant admin consent)

### Error: "Broker not installed"
- El dispositivo necesita Microsoft Authenticator o Intune Company Portal instalado
- Verifica que la app broker está actualizada a la última versión

---

## Referencias

- [Microsoft Entra ID App Registration](https://learn.microsoft.com/en-us/entra/identity-platform/quickstart-register-app)
- [MSAL Android Configuration](https://learn.microsoft.com/en-us/entra/msal/android/msal-configuration)
- [Conditional Access Policies](https://learn.microsoft.com/en-us/entra/identity/conditional-access/overview)
