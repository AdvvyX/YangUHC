---
name: Memoria Persistente
description: Permite al modelo recordar entre sesiones. Usar al INICIAR cualquier sesión (leer la memoria), cuando el usuario diga "recuerda que..." o "¿qué recuerdas?", al retomar trabajo posiblemente en curso, después de una decisión importante del proyecto, y antes de CERRAR una sesión con avances significativos. Guarda decisiones, preferencias, estado del trabajo y aprendizajes en MEMORY.md.
---

# Memoria Persistente

Las sesiones, la ventana de contexto y las compactaciones se olvidan; el archivo no. Esta skill convierte `MEMORY.md` (raíz del proyecto) en el cerebro externo del modelo: lo que se escribe ahí sobrevive entre sesiones, compactaciones y cambios de modelo.

**Regla de oro:** leer al empezar, escribir en el momento. Lo que no se escribe antes de que la sesión se corte o se compacte, se pierde.

## Cuándo leer la memoria

1. **Primer paso de toda sesión** — si `MEMORY.md` existe, léelo antes de responder o actuar.
2. **Al retomar** cualquier trabajo que pueda haber estado en curso.
3. **Antes de decidir** algo que pudo haberse decidido antes (arquitectura, nombres, convenciones).
4. **Cuando el usuario lo pida**: "recuerda que…", "acuérdate de…", "¿qué recuerdas?".

## Cuándo escribir en la memoria

Escribe en el momento, no al final:

- El usuario aprueba o pide una decisión (técnica o de producto).
- El usuario expresa una preferencia sobre cómo trabajar (idioma, estilo, formato, flujo).
- Se descubre un hecho no obvio del proyecto (por qué existe algo, un gotcha, un comando que costó encontrar).
- El usuario pide explícitamente recordar algo.
- Cierras una sesión con avances → actualiza el estado del trabajo.

## Qué registrar

| Sección | Contenido |
|---|---|
| `## Preferencias del usuario` | Cómo quiere que trabaje el modelo: idioma, nivel de detalle, convenciones, cosas que evitar. |
| `## Decisiones` | Qué se decidió + **por qué** + fecha. La razón es lo que el código no guarda. |
| `## Estado del trabajo` | Qué está en curso, qué queda pendiente, en qué se quedó la última sesión y cuál es el siguiente paso concreto. |
| `## Aprendizajes del proyecto` | Gotchas, comandos útiles, convenciones, atajos que costaron descubrir. |
| `## Contexto` | Historia relevante no derivable del repositorio. |

### Qué NO registrar

- **Secretos**: contraseñas, API keys, tokens. Nunca.
- **Lo que el código ya dice**: no dupliques código, nombres de archivos ni documentación existente.
- **Especulaciones**: solo hechos verificados. Una memoria mal escrita es peor que no tenerla.
- **Cosas volátiles**: errores puntuales ya resueltos, estados transitorios, logs.

## Formato

Archivo `MEMORY.md` en la raíz del proyecto (si no existe, créalo con este esqueleto):

```markdown
# Memoria del proyecto — <nombre>

## Preferencias del usuario
- (idioma, estilo de respuesta, convenciones de trabajo, ...)

## Decisiones
### [2026-10-24] <decisión>
- Por qué: ...
- Alternativas descartadas: ...

## Estado del trabajo
- **En curso:** ...
- **Pendiente:** ...
- **Última sesión (2026-10-24):** qué se hizo, en qué se quedó, siguiente paso.

## Aprendizajes del proyecto
- Comando útil: `...` hace ...
- Gotcha: ...

## Contexto
- ...
```

Reglas de formato:

- Entradas fechadas `[YYYY-MM-DD]`; lo más reciente arriba en cada sección.
- Frases cortas y concretas: este archivo se lee entero en cada sesión. **Meta: menos de 200 líneas.**
- Al superar ~250 líneas, consolida: fusiona lo obsoleto, elimina lo que ya no aplica y conserva en `## Decisiones` lo que siga vivo.
- Nunca borres una decisión sin sustituirla por la nueva: las decisiones se revocan, no se olvidan.

## Integridad

- La memoria es un **recordatorio, no una autoridad**. Antes de actuar sobre una entrada vieja, verifícala contra el código, `git log` o el usuario. El código y la rama actual mandan sobre el archivo.
- Si la memoria y la realidad discrepan, corrige la memoria y anótalo.
- Tras una compactación de contexto, confía en `MEMORY.md` y en el historial real, no en tu recuerdo de la conversación.

## Flujo completo

1. **Inicio de sesión**: leer `MEMORY.md` → retomar con ese contexto sin pedir al usuario que repita nada.
2. **Durante**: escribir en el momento cada hecho memorable.
3. **Fin de sesión**: actualizar `## Estado del trabajo` con lo hecho, lo pendiente y el siguiente paso concreto.
