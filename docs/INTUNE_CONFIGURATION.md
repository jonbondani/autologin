# Configuración de Microsoft Intune - Guía Paso a Paso

> Esta guía asume licencias Microsoft 365 Business Premium y permisos de Intune Administrator.

---

## Prerrequisitos

- [ ] Tenant de Microsoft 365 con licencias Business Premium activas
- [ ] Microsoft Entra ID configurado según [ENTRA_ID_CONFIGURATION.md](ENTRA_ID_CONFIGURATION.md)
- [ ] Acceso al [Microsoft Intune Admin Center](https://intune.microsoft.com)

---

## Paso 1: Verificar Enrollment de Dispositivos Android

### 1.1 Configurar Android Enterprise

1. Accede a **https://intune.microsoft.com**
2. Ve a **Devices** → **Enrollment** → **Android**
3. En **Android Enterprise**, verifica que está vinculado a Managed Google Play
4. Si no está vinculado:
   - Haz clic en **"Managed Google Play"**
   - Sigue el wizard para vincular tu cuenta de Google administrada
   - Acepta los términos de servicio

### 1.2 Verificar Perfil de Enrollment

1. Ve a **Devices** → **Enrollment** → **Android** → **Enrollment profiles**
2. Verifica que existe un perfil para **"Android Enterprise - Work Profile"** o **"Fully Managed"**
3. Si no existe, créalo:
   - Haz clic en **"+ Create profile"**
   - **Profile type**: Corporate-owned, fully managed (o Work Profile según tu caso)
   - **Name**: `AutoLogin Device Profile`
   - **Token type**: Corporate-owned dedicated device (o el apropiado)
   - **Create**

### 1.3 Verificar que los Dispositivos están Enrollados

1. Ve a **Devices** → **All devices**
2. Filtra por **OS**: Android
3. Verifica que los dispositivos de destino aparecen con estado **"Compliant"** o **"Managed"**

> **Si los dispositivos NO están enrollados**: Los usuarios deben instalar Intune Company Portal desde Google Play Store e iniciar el enrollment siguiendo los pasos en pantalla.

---

## Paso 2: Crear Grupo de Dispositivos/Usuarios

> Los grupos se usan para asignar políticas y apps a subconjuntos de usuarios/dispositivos.

1. Ve a **Groups** → **All groups** → **"+ New group"**
2. Rellena:
   - **Group type**: Security
   - **Group name**: `AutoLogin Users`
   - **Group description**: `Usuarios que usan la app AutoLogin SSO`
   - **Membership type**: Assigned (o Dynamic si prefieres reglas automáticas)
3. **Members**: Añade los usuarios que usarán AutoLogin
4. **Create**

---

## Paso 3: Crear Compliance Policy para Android

> Las compliance policies definen los requisitos mínimos que un dispositivo debe cumplir.

1. Ve a **Devices** → **Compliance** → **Policies** → **"+ Create policy"**
2. **Platform**: Android Enterprise
3. **Profile type**: Work Profile (o Fully Managed, según tu enrollment)
4. **Name**: `AutoLogin Compliance Policy`

### 3.1 Configurar Requisitos

En la pestaña **Compliance settings**:

**Device Health:**
- Rooted devices: **Block**
- SafetyNet device attestation: **Check basic integrity** (mínimo)

**Device Properties:**
- Minimum OS version: **7.0** (para compatibilidad con MSAL minSdk 24)

**System Security:**
- Require a password to unlock mobile devices: **Require**
- Minimum password length: **6** (o el valor de tu política corporativa)
- Encryption of data storage on device: **Require**

**Actions for noncompliance:**
- Mark device noncompliant: **Immediately** (o con período de gracia)

5. **Assignments**: Asigna al grupo `AutoLogin Users`
6. **Create**

---

## Paso 4: Crear App Protection Policy

> Las App Protection Policies (MAM) protegen los datos corporativos dentro de las apps.

1. Ve a **Apps** → **App protection policies** → **"+ Create policy"** → **Android**
2. **Name**: `AutoLogin App Protection`

### 4.1 Apps Objetivo

En **Apps**:
- **Target to all apps**: No
- **Target policy to**: Select apps
- Haz clic en **"+ Select apps"**
- Si tu app AutoLogin ya está en Managed Google Play, selecciónala
- También selecciona las apps de Microsoft que quieres proteger:
  - Microsoft Teams
  - Microsoft Outlook
  - Microsoft OneDrive
  - (las que apliquen)

### 4.2 Data Protection

En **Data protection**:

| Configuración | Valor | Razón |
|---|---|---|
| Backup org data to Android backup services | Block | Prevenir fuga de datos |
| Send org data to other apps | Policy managed apps | Solo apps gestionadas |
| Receive data from other apps | Policy managed apps | Solo apps gestionadas |
| Save copies of org data | Allow | O Block según política |
| Allow user to save copies to selected services | OneDrive for Business, SharePoint | Servicios corporativos |
| Screen capture and Google Assistant | Block | Prevenir captura de datos sensibles |

### 4.3 Access Requirements

En **Access requirements**:

| Configuración | Valor |
|---|---|
| PIN for access | Require |
| PIN type | Numeric |
| Simple PIN | Block |
| Select Minimum PIN length | 4 |
| Fingerprint instead of PIN | Allow |
| Override biometrics with PIN after timeout | Require (after 30 min) |

### 4.4 Conditional Launch

En **Conditional launch**:

| Condición | Valor | Acción |
|---|---|---|
| Max PIN attempts | 5 | Reset PIN |
| Offline grace period | 720 min (12h) | Block access |
| Jailbroken/rooted devices | — | Block access |
| Min OS version | 7.0 | Block access |

3. **Assignments**: Asigna al grupo `AutoLogin Users`
4. **Create**

---

## Paso 5: Configurar Conditional Access

> Conditional Access une Entra ID + Intune para controlar quién accede, desde dónde y bajo qué condiciones.

### 5.1 Política: Requerir Dispositivo Compliant

1. Ve a **https://entra.microsoft.com**
2. Navega a **Identity** → **Protection** → **Conditional Access** → **Policies**
3. Haz clic en **"+ Create new policy"**
4. **Name**: `Require Compliant Device for M365 Apps`

**Users:**
- Include: Grupo `AutoLogin Users`
- Exclude: Cuentas de administrador de emergencia (break-glass accounts)

**Target resources:**
- Cloud apps: **Select apps**
- Selecciona: Office 365 (esto cubre Teams, Outlook, OneDrive, SharePoint, etc.)

**Conditions:**
- Device platforms: **Android**

**Grant:**
- **Grant access**
- Selecciona: **Require device to be marked as compliant**
- Selecciona: **Require approved client app**
- For multiple controls: **Require all the selected controls**

**Session:**
- Sign-in frequency: **8 hours** (ajusta según necesidad)

5. **Enable policy**: **On**
6. **Create**

### 5.2 Política: Requerir App Protection Policy (Opcional)

1. Crea otra política con nombre: `Require App Protection for M365`
2. Configuración similar, pero en **Grant**:
   - Selecciona: **Require app protection policy**
3. **Create**

> Esta política asegura que las apps están protegidas por MAM aunque el dispositivo no sea fully managed.

---

## Paso 6: Distribuir AutoLogin como Line-of-Business App

### 6.1 Subir la App a Intune

1. Ve a **Apps** → **All apps** → **"+ Add"**
2. **App type**: **Line-of-business app**
3. Haz clic en **"Select"**
4. **App package file**: Sube el APK de AutoLogin
5. **App information**:
   - Name: `AutoLogin SSO Manager`
   - Description: `App para gestión centralizada de SSO con Microsoft 365`
   - Publisher: Tu organización
   - Minimum operating system: **Android 7.0**
6. **Create**

### 6.2 Asignar la App

1. En la app recién creada, ve a **Properties** → **Assignments** → **Edit**
2. **Required**: Añade grupo `AutoLogin Users` (la app se instala automáticamente)
   - O **Available for enrolled devices**: Los usuarios la instalan desde Company Portal
3. **Save**

### Alternativa: Managed Google Play

Si prefieres distribuir via Managed Google Play:
1. Ve a **Apps** → **All apps** → **"+ Add"** → **Managed Google Play app**
2. Publica tu app en Google Play Console como app privada
3. Aprueba la app en Managed Google Play
4. Asigna al grupo de usuarios

---

## Paso 7: Verificación Final

### Checklist de Verificación Intune:

| Elemento | Estado |
|---|---|
| Android Enterprise vinculado a Managed Google Play | [ ] |
| Dispositivos de prueba enrollados en Intune | [ ] |
| Grupo `AutoLogin Users` creado con miembros | [ ] |
| Compliance Policy creada y asignada | [ ] |
| App Protection Policy creada y asignada | [ ] |
| Conditional Access: Dispositivo compliant activada | [ ] |
| AutoLogin app subida y asignada | [ ] |
| Company Portal instalado en dispositivos | [ ] |

### Test de Verificación:

1. **Desde un dispositivo enrollado**:
   - Abrir AutoLogin → Login → Verificar que se autentica correctamente
   - Abrir Teams → Verificar SSO (no pide credenciales)
   - Abrir Outlook → Verificar SSO (no pide credenciales)

2. **Desde un dispositivo NO enrollado**:
   - Abrir cualquier app M365 → Debe requerir enrollment o ser bloqueado (según policy)

3. **Desde un dispositivo non-compliant (ej: rooted)**:
   - Intentar acceder → Debe ser bloqueado

---

## Paso 8: Monitorización

### Logs de Sign-in

1. En **https://entra.microsoft.com** → **Identity** → **Monitoring & health** → **Sign-in logs**
2. Filtra por:
   - Application: `AutoLogin SSO Manager`
   - Status: All
3. Verifica que los sign-ins usan "Token broker" como authentication method

### Compliance Reports

1. En **https://intune.microsoft.com** → **Reports** → **Device compliance**
2. Verifica el porcentaje de dispositivos compliant
3. Investiga dispositivos non-compliant

---

## Troubleshooting

### Dispositivo no aparece como compliant
- Verifica que Company Portal está instalado y actualizado
- Abre Company Portal → Check status → Sync
- Espera hasta 15 minutos para la sincronización
- Verifica que el dispositivo cumple todos los requisitos de la Compliance Policy

### App Protection Policy no se aplica
- El usuario debe cerrar y reabrir la app después de que la policy se asigne
- Verifica que la app está en la lista de apps objetivo de la policy
- Espera hasta 4 horas para la propagación inicial

### SSO no funciona después del login en AutoLogin
- Verifica que Company Portal / Authenticator está instalado y es la versión más reciente
- Verifica que el dispositivo está registrado en Entra ID (Settings → Accounts → Work account)
- Limpia cache de la app broker y reintenta

---

## Referencias

- [Intune Android Enterprise Enrollment](https://learn.microsoft.com/en-us/mem/intune/enrollment/android-enroll)
- [App Protection Policies](https://learn.microsoft.com/en-us/mem/intune/apps/app-protection-policies)
- [Conditional Access](https://learn.microsoft.com/en-us/entra/identity/conditional-access/overview)
- [Deploy LOB Apps](https://learn.microsoft.com/en-us/mem/intune/apps/lob-apps-android)
