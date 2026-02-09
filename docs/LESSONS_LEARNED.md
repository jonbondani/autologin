# Lessons Learned - AutoLogin

Registro de lecciones aprendidas durante el desarrollo del proyecto.

---

## Fase de Planificación (2026-02-09)

### LL-001: Verificar viabilidad técnica antes de codificar
- **Contexto**: El mecanismo SSO device-wide en Android depende de un ecosistema específico (MSAL broker + PRT)
- **Lección**: Investigar la arquitectura de Microsoft antes de asumir que "simplemente funciona". El SSO en Android NO es inyectar tokens en otras apps — es dejar que el broker de Microsoft los distribuya.
- **Acción**: Se creó documento de viabilidad técnica antes de iniciar código.

### LL-002: El broker es OBLIGATORIO para SSO cross-app
- **Contexto**: Evaluamos alternativas para SSO en Android
- **Lección**: No hay forma de hacer SSO entre apps Microsoft sin usar el broker (Authenticator o Company Portal). Microsoft no soporta un mecanismo alternativo. La app custom no puede "empujar" tokens a otras apps.
- **Impacto**: El dispositivo DEBE tener Authenticator o Company Portal instalado. Esto es un requisito no negociable.

### LL-003: PRT no tiene binding por hardware en Android
- **Contexto**: En Windows, el PRT se vincula al TPM. En Android no hay TPM equivalente.
- **Lección**: La seguridad del PRT en Android depende de la encriptación por software. Esto hace más importante la compliance policy (dispositivo no rooteado, encriptación activada, etc.).
- **Impacto**: Se configuran Compliance Policies estrictas en Intune.

### LL-004: MSAL v7.0 cambió minSdk de 16 a 24
- **Contexto**: Versiones anteriores de MSAL soportaban Android 4.1+
- **Lección**: Desde agosto 2024, el mínimo es Android 7.0. Esto afecta a dispositivos muy antiguos (<2% del mercado).
- **Impacto**: Documentado como requisito. No es un problema real en entornos enterprise con dispositivos recientes.

### LL-005: No podemos monitorizar autenticación de otras apps desde el dispositivo
- **Contexto**: El requisito incluía "historial de aplicaciones accedidas"
- **Lección**: Android sandbox impide observar cuándo otras apps obtienen tokens del broker. Solo podemos: (a) registrar nuestros propios login/logout, (b) detectar qué apps Microsoft están instaladas, (c) consultar Entra ID Sign-in Logs server-side.
- **Impacto**: El historial de la app se centra en nuestros propios eventos. Para auditoría completa, se usan los Sign-in Logs de Entra ID.

### LL-006: La licencia Business Premium tiene límite de 300 usuarios
- **Contexto**: Evaluamos limitaciones de licenciamiento
- **Lección**: El límite de 300 usuarios es combinado entre todos los planes Business del tenant. Si la organización crece, necesitará migrar a Enterprise (E3/E5).
- **Impacto**: Documentado como limitación conocida. No afecta al desarrollo técnico.

---

## Formato para Nuevas Lecciones

```
### LL-XXX: [Título breve]
- **Contexto**: [Qué estaba pasando]
- **Lección**: [Qué aprendimos]
- **Impacto**: [Qué cambió como resultado]
```
