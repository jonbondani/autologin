# Lecciones Aprendidas - AutoLogin

Registro de lecciones aprendidas durante el desarrollo del proyecto.

---

## Fase de Planificacion (2026-02-09)

### LL-001: Verificar viabilidad tecnica antes de codificar
- **Contexto**: El mecanismo SSO device-wide en Android depende de un ecosistema especifico (MSAL broker + PRT)
- **Leccion**: Investigar la arquitectura de Microsoft antes de asumir que "simplemente funciona". El SSO en Android NO es inyectar tokens en otras apps - es dejar que el broker de Microsoft los distribuya.
- **Accion**: Se creo documento de viabilidad tecnica antes de iniciar codigo.

### LL-002: El broker es OBLIGATORIO para SSO cross-app
- **Contexto**: Evaluamos alternativas para SSO en Android
- **Leccion**: No hay forma de hacer SSO entre apps Microsoft sin usar el broker (Authenticator o Company Portal). Microsoft no soporta un mecanismo alternativo. La app custom no puede "empujar" tokens a otras apps.
- **Impacto**: El dispositivo DEBE tener Authenticator Y Company Portal instalados. Esto es un requisito no negociable.

### LL-003: PRT no tiene binding por hardware en Android
- **Contexto**: En Windows, el PRT se vincula al TPM. En Android no hay TPM equivalente.
- **Leccion**: La seguridad del PRT en Android depende de la encriptacion por software. En Shared Device Mode, la seguridad se gestiona a nivel de global sign-out que limpia todos los tokens.
- **Impacto**: El global sign-out de Shared Device Mode es la medida de seguridad principal.

### LL-004: MSAL v7.0 cambio minSdk de 16 a 24
- **Contexto**: Versiones anteriores de MSAL soportaban Android 4.1+
- **Leccion**: Desde agosto 2024, el minimo es Android 7.0. Esto afecta a dispositivos muy antiguos (<2% del mercado).
- **Impacto**: Documentado como requisito. No es un problema real en entornos enterprise con dispositivos recientes.

### LL-005: No podemos monitorizar autenticacion de otras apps desde el dispositivo
- **Contexto**: El requisito incluia "historial de aplicaciones accedidas"
- **Leccion**: Android sandbox impide observar cuando otras apps obtienen tokens del broker. Solo podemos: (a) registrar nuestros propios login/logout, (b) detectar que apps Microsoft estan instaladas, (c) consultar Entra ID Sign-in Logs server-side.
- **Impacto**: El historial de la app se centra en nuestros propios eventos. Para auditoria completa, se usan los Sign-in Logs de Entra ID.

### LL-006: La licencia Business Premium tiene limite de 300 usuarios
- **Contexto**: Evaluamos limitaciones de licenciamiento
- **Leccion**: El limite de 300 usuarios es combinado entre todos los planes Business del tenant. Si la organizacion crece, necesitara migrar a Enterprise (E3/E5).
- **Impacto**: Documentado como limitacion conocida. No afecta al desarrollo tecnico.

---

## Sprint 0 - Configuracion (2026-02-10)

### LL-007: Samsung WAF Interactive Displays no soportan Android Enterprise Work Profile
- **Contexto**: Intentamos enrollar Samsung WAF 65" en Intune con Work Profile
- **Leccion**: Las pantallas interactivas Samsung WAF, aunque ejecutan Android 14 con GMS, NO soportan Work Profile. El error "No se puede anadir un perfil de trabajo a este dispositivo" es definitivo para este tipo de dispositivo. No se puede usar Intune device enrollment.
- **Impacto**: Se cambio de Intune enrollment a Shared Device Mode via Authenticator. Sin factory reset necesario. Toda la guia de Intune (INTUNE_CONFIGURATION.md) quedo OBSOLETA.

### LL-008: Security Defaults y Conditional Access son mutuamente excluyentes
- **Contexto**: Al crear una Conditional Access policy, Entra ID bloqueo con "deben deshabilitarse los valores predeterminados de seguridad"
- **Leccion**: No se pueden usar ambos simultaneamente. Hay que desactivar Security Defaults primero. Ademas, Security Defaults deben estar desactivados para poder configurar metodos de autenticacion passwordless.
- **Impacto**: Security Defaults desactivados permanentemente. CA policy eliminada por incompatibilidad con Samsung WAF.

### LL-009: Shared Device Mode es la solucion correcta para dispositivos compartidos
- **Contexto**: Las pantallas Samsung WAF son dispositivos compartidos donde multiples usuarios necesitan hacer login/logout
- **Leccion**: Microsoft tiene Shared Device Mode disenado exactamente para esto. Se activa via Authenticator con rol Cloud Device Administrator. Proporciona login global + sign-out global (limpia tokens de TODAS las apps). No requiere Intune enrollment ni factory reset.
- **Impacto**: Simplifico enormemente la arquitectura. No necesitamos Intune enrollment, Compliance Policies ni Conditional Access obligatorias.

### LL-010: La opcion "Registrar como dispositivo compartido" en Authenticator solo aparece con la app limpia
- **Contexto**: Se registro el dispositivo en Authenticator sin activar modo compartido
- **Leccion**: La opcion de Shared Device Mode en Authenticator aparece SOLO en la primera pantalla, antes de agregar cualquier cuenta. Si ya hay una cuenta registrada, hay que **desinstalar y reinstalar** Authenticator para ver la opcion. Ademas, Company Portal debe estar instalado previamente.
- **Impacto**: Documentado para futuras configuraciones. Es critico seguir este orden exacto.

### LL-011: El rol en espanol se llama "Administrador de dispositivos en la nube"
- **Contexto**: Buscabamos "Cloud Device Administrator" en Entra ID en espanol
- **Leccion**: Los nombres de roles en Entra ID estan traducidos. "Cloud Device Administrator" = "Administrador de dispositivos en la nube". No confundir con "Administrador de Cloud App Security" que es un rol diferente.
- **Impacto**: Documentado para referencia.

### LL-012: Company Portal debe estar instalado junto con Authenticator
- **Contexto**: Shared Device Mode no se activaba con solo Authenticator
- **Leccion**: Para que el modo dispositivo compartido funcione correctamente, **Company Portal debe estar instalado** en el dispositivo ademas de Authenticator. Ambas apps trabajan juntas para gestionar el registro del dispositivo compartido. Company Portal NO necesita enrollment - solo estar presente.
- **Impacto**: Agregado a la guia de setup como requisito obligatorio.

---

## Sprint 1 - Scaffold (2026-02-11)

### LL-013: Signature hash de debug keystore difiere de release
- **Contexto**: La app compilada en debug no podia autenticarse con el hash registrado en Entra ID
- **Leccion**: El debug keystore (`~/.android/debug.keystore`) genera un signature hash diferente al release keystore. Hay que registrar el hash correcto en Entra ID segun el tipo de build. MSAL muestra el hash esperado en su mensaje de error - usar ese hash para corregir la configuracion.
- **Impacto**: Se actualizo Entra ID con el hash correcto. Documentado como paso critico.

### LL-014: BrowserTabActivity android:path debe usar hash raw, no URL-encoded
- **Contexto**: El redirect despues de la autenticacion en el navegador no volvia a la app
- **Leccion**: En `AndroidManifest.xml`, el atributo `android:path` del `BrowserTabActivity` debe usar el signature hash en formato **raw** con los caracteres `/`, `+`, `=` tal cual. NO usar la version URL-encoded (`%2F`, `%2B`, `%3D`). Sin embargo, en `auth_config.json` SI se usa la version URL-encoded.
- **Impacto**: Dos formatos diferentes para el mismo hash segun donde se use. Documentado para evitar confusion.

### LL-015: fillColor duplicado en vector XML causa error de compilacion
- **Contexto**: Error AAPT al compilar vectores XML de iconos
- **Leccion**: Los archivos XML de vectores no pueden tener atributos duplicados. Si un vector tiene `android:fillColor` repetido, el build falla con error de AAPT.
- **Impacto**: Limpiar XMLs de vectores al importarlos.

### LL-016: mipmap-anydpi requiere fallback para minSdk < 26
- **Contexto**: Error "mipmap not found" en dispositivos con API < 26
- **Leccion**: Los adaptive icons (mipmap-anydpi) solo funcionan en API 26+. Si minSdk es 24, se necesita un icono fallback en `mipmap-anydpi/` que apunte a un recurso compatible.
- **Impacto**: Agregado fallback de iconos.

### LL-017: Iconos Icons.Default limitados en core Material
- **Contexto**: `Icons.Default.Error`, `Icons.Default.Shield`, `Icons.Default.History` no existen
- **Leccion**: Solo un subconjunto de iconos esta disponible en `androidx.compose.material.icons.Icons.Default`. Para alternativas: `Warning` en vez de `Error`, `Lock` en vez de `Shield`, `DateRange` en vez de `History`.
- **Impacto**: Usar iconos del core set para evitar dependencias adicionales.

---

## Sprint 2 - Autenticacion MSAL (2026-02-12)

### LL-018: MSAL getCurrentAccount() no puede llamarse desde el hilo principal
- **Contexto**: La app crasheaba al intentar verificar si habia una cuenta activa
- **Leccion**: `getCurrentAccount()` de MSAL realiza operaciones de I/O y no puede ejecutarse en el main thread. Debe envolverse en `withContext(Dispatchers.IO)`. Aplica a todos los metodos de MSAL que interactuan con el broker: `getCurrentAccount()`, `signIn()`, `signOut()`, `acquireTokenSilently()`.
- **Impacto**: Todas las llamadas MSAL se ejecutan en `Dispatchers.IO`.

### LL-019: signIn() es obligatorio para SSO global en Shared Device Mode
- **Contexto**: Usamos `acquireToken()` pero otras apps no recibian SSO
- **Leccion**: En Shared Device Mode, hay que usar `signIn()` (no `acquireToken()`) para que el broker registre un global sign-in y propague SSO a todas las apps del dispositivo. `acquireToken()` solo obtiene un token para la app que lo llama, sin efecto global. Analogamente, para logout se usa `signOut()` que hace global sign-out.
- **Impacto**: Se cambio la implementacion de acquireToken a signIn. El SSO funciona correctamente.

### LL-020: Solo apps "shared device mode aware" reciben SSO completo
- **Contexto**: Probamos SSO en todas las apps Microsoft instaladas
- **Leccion**: Solo las apps que implementan el SDK de Shared Device Mode reciben SSO automatico completo:
  - **SSO completo**: Teams, Edge, M365 Copilot (se autentican sin ninguna intervencion)
  - **SSO parcial**: Word, Excel, OneDrive, PowerPoint, SharePoint, To Do (email visible, confirmar sin contrasena)
- **Impacto**: Documentado como comportamiento esperado. No es un bug, es una limitacion de las apps que no implementan SDM.

### LL-021: App Protection Policies (MAM) bloquean SSO en dispositivos compartidos
- **Contexto**: SSO no funcionaba en algunas apps, Teams pedia PIN
- **Leccion**: Las App Protection Policies (MAM) de Intune entran en conflicto con Shared Device Mode. MAM requiere que cada app gestione su propia sesion protegida con PIN/biometrics, lo cual impide que el broker haga global sign-in/sign-out. Hay que excluir los dispositivos compartidos del scope de las politicas MAM.
- **Impacto**: Se removieron/excluyeron las MAM policies del grupo de dispositivos compartidos.

### LL-022: killBackgroundProcesses solo funciona con apps en background
- **Contexto**: Al hacer sign-out, queriamos forzar el cierre de todas las apps Microsoft
- **Leccion**: `ActivityManager.killBackgroundProcesses()` solo mata procesos que estan en background, no apps en foreground. Para el flujo de AutoLogin esto funciona correctamente: cuando el usuario esta en AutoLogin haciendo logout, todas las demas apps Microsoft estan en background.
- **Impacto**: El flujo de logout funciona correctamente. Se matan: Teams, Edge, M365 Copilot, Word, Excel, OneDrive, PowerPoint, SharePoint, To Do, OneNote.

### LL-023: "An account is already signed in" al llamar signIn()
- **Contexto**: Crash al intentar hacer login cuando ya habia una sesion activa
- **Leccion**: Antes de llamar `signIn()`, hay que verificar si ya hay una cuenta activa con `getCurrentAccount()`. Si ya hay una cuenta, mostrar el estado autenticado en vez de intentar un nuevo login.
- **Impacto**: Se agrego verificacion de cuenta existente al iniciar la app.

### LL-024: Autenticacion passwordless requiere registro especifico en Authenticator
- **Contexto**: Passwordless no funcionaba, la opcion no aparecia en Authenticator
- **Leccion**: Para que passwordless funcione, la cuenta debe registrarse en Authenticator como **"Cuenta profesional o educativa"** via escaneo de codigo QR desde https://aka.ms/mysecurityinfo. El resultado debe mostrar un **icono de maletin** (briefcase). Si aparece con icono **X**, se registro como TOTP solamente y no soportara push notifications ni number matching.
- **Impacto**: Documentado el flujo exacto de registro. Incluye procedimiento de troubleshooting.

### LL-025: El "reset nuclear" de MFA siempre resuelve problemas de passwordless
- **Contexto**: Despues de multiples intentos, passwordless no se podia configurar para un usuario
- **Leccion**: Si nada funciona para configurar passwordless:
  1. Admin en Entra ID > Usuarios > [usuario] > Metodos de autenticacion > "Requerir volver a registrar MFA"
  2. Eliminar todas las cuentas del usuario en Authenticator
  3. Iniciar sesion de nuevo en https://aka.ms/mysecurityinfo - el sistema fuerza re-registro
  4. Durante el re-registro, agregar Authenticator como "Cuenta profesional" via QR
  5. Esta vez siempre se registra correctamente como push + passwordless
- **Impacto**: Documentado como procedimiento de ultimo recurso. Algunos usuarios lo necesitan, otros no.

### LL-026: Number matching esta habilitado por defecto desde 2023
- **Contexto**: Buscabamos como habilitar number matching en Entra ID
- **Leccion**: Desde mayo 2023, Microsoft habilito number matching por defecto para todas las notificaciones push de Authenticator. No requiere configuracion adicional.
- **Impacto**: Menos configuracion necesaria en el tenant.

### LL-027: Company Portal muestra error de "perfil de trabajo" pero no afecta
- **Contexto**: Al abrir Company Portal en Samsung WAF aparece "No se puede anadir perfil de trabajo"
- **Leccion**: Este error es esperado en Samsung WAF. Company Portal no necesita enrollment ni perfil de trabajo para funcionar como parte del stack de Shared Device Mode. Solo necesita estar instalado. El error se puede ignorar.
- **Impacto**: Documentado para evitar que los administradores pierdan tiempo intentando resolver este error.

---

## Formato para Nuevas Lecciones

```
### LL-XXX: [Titulo breve]
- **Contexto**: [Que estaba pasando]
- **Leccion**: [Que aprendimos]
- **Impacto**: [Que cambio como resultado]
```
