# Manual de Usuario - AutoLogin

## Que es AutoLogin

AutoLogin permite iniciar sesion una sola vez y acceder automaticamente a todas las apps de Microsoft instaladas en la pantalla (Teams, Excel, Word, Edge, etc.).

---

## Iniciar Sesion

1. Abre la app **AutoLogin** en la pantalla
2. Pulsa **"Iniciar Sesion con Microsoft"**
3. Introduce el correo: `shared-screen-account@example.com`
4. La pantalla mostrara un **numero de 2 digitos**
5. Abre **Microsoft Authenticator** en el telefono movil del responsable
6. Introduce ese mismo numero en Authenticator
7. Listo: la sesion queda activa en todas las apps

---

## Abrir una app

Una vez iniciada la sesion, la pantalla principal muestra las apps en dos grupos:

### Acceso automatico (columna derecha)
- **Teams, Edge, Microsoft 365 Copilot**
- Pulsa **"Abrir"** y la app se abrira ya con la sesion iniciada
- No necesitas hacer nada mas

### Requiere identificacion (columna izquierda)
- **OneDrive, Word, Excel, OneNote**
- Pulsa **"Identificate"** y la app se abrira
- Escribe el correo (`usuario@example.com`) cuando la app lo pida
- **No te pedira contrasena**, solo confirmar el correo

---

## Cerrar Sesion

1. Vuelve a la app **AutoLogin**
2. Pulsa el boton rojo **"Cerrar Sesion"**
3. Se cerrara la sesion en **todas** las apps de Microsoft
4. La pantalla queda lista para el siguiente usuario

> **Importante**: Cierra siempre la sesion al terminar de usar la pantalla.

> **Nota para IT**: Para que el cierre de sesion sea global (cerrando tambien Teams, Edge, etc.), la pantalla debe estar registrada como **dispositivo compartido** en Microsoft Authenticator. Si al cerrar sesion las demas apps siguen con la cuenta activa, consultar la seccion "Configurar Shared Device Mode" en la [Guia de Instalacion](INSTALLATION.md).

---

## Enviar un informe de errores

Si algo no funciona correctamente:

1. En la parte inferior de la pantalla principal, pulsa **"Enviar log de errores a IT"**
2. Elige como enviarlo (correo, WhatsApp, etc.)
3. El informe se enviara automaticamente con la informacion tecnica necesaria

---

## Consultar el historial

1. Pulsa **"Historial"** en la barra inferior
2. Veras un registro de todos los inicios y cierres de sesion
3. Puedes filtrar por fechas pulsando el icono del calendario

---

## Actualizar la app

Si hay una version nueva disponible, veras un boton azul en la parte inferior de la pantalla:

1. Pulsa **"Actualizar a v1.0.XX"**
2. Se descargara la nueva version (veras una barra de progreso con el porcentaje)
3. Al terminar, aparecera el instalador de Android
4. Pulsa **"Instalar"** para confirmar
5. La app se reiniciara con la nueva version

> Si no ves el boton de actualizar, significa que ya tienes la ultima version.

---

## Preguntas frecuentes

**La app dice que falta Microsoft Authenticator**
> Pide al equipo de IT que instale Microsoft Authenticator y Company Portal en la pantalla.

**Una app no aparece en la lista**
> Solo aparecen las apps de Microsoft que estan instaladas. Pide al equipo de IT que instale la app que necesitas.

**No me llega la notificacion al movil**
> Asegurate de que el telefono tiene conexion a internet y que Authenticator tiene permisos de notificaciones.

**Una app pide contrasena**
> Esto no deberia ocurrir. Cierra sesion en AutoLogin, vuelve a iniciar sesion e intentalo de nuevo. Si persiste, envia un log de errores a IT.

**Cierro sesion pero Teams/Edge siguen con mi cuenta**
> La pantalla necesita estar configurada en "modo dispositivo compartido". Contacta con el equipo de IT para que registren la pantalla como dispositivo compartido en Microsoft Authenticator.

**La actualizacion falla o no se descarga**
> Comprueba que la pantalla tiene conexion a internet. Si el problema persiste, pide al equipo de IT que instale la nueva version manualmente.

---

*Desarrollado por el Departamento de IT de Prestige-Expo*
