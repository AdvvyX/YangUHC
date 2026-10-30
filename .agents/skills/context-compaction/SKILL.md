---
name: Compactación de Contexto
description: Prevenir y mitigar el context rot mediante compactación estructurada, resumen de sesión y preservación de estado crítico. Usar cuando la conversación sea larga, antes de un resumen forzado del harness, al notar degradación en las respuestas del modelo, o al cerrar hitos importantes de trabajo.
---

# Compactación de Contexto (Anti-Context Rot)

El **context rot** ocurre cuando una sesión acumula demasiados turnos, salidas de herramientas masivas, código repetido e intentos fallidos. El modelo pierde atención, olvida restricciones tempranas, alucina archivos y empieza a contradecirse.

Esta skill establece el protocolo para **compactar activamente** la memoria de trabajo antes de que el harness lo haga de forma destructiva o la ventana colapse.

---

## 1. Principio fundamental: El disco manda, el chat se purga

La memoria en el chat es volátil y cara. El sistema de archivos es persistente y barato.

```
Conversación larga (pesada, ruidosa)
       │
       ▼
[ Extracción de estado ] ──► Archivo de recuperación (ledger/memoria/resumen)
       │
       ▼
[ Poda deliberada / Nueva sesión limpia ]
```

Todo lo que deba sobrevivir a la compactación **debe estar escrito en un archivo** antes de compactar:
- Decisiones tomadas y su justificación.
- Archivos tocados y estado de git (`HEAD`, commits, diffs pendientes).
- Siguiente paso exacto y bloqueos activos.

---

## 2. Señales de "Context Rot" (Cuándo activar esta skill)

Activa la compactación inmediatamente si detectas **dos o más** de estas señales:

| Señal | Manifestación típica |
|---|---|
| **Pérdida de reglas** | El modelo olvida restricciones iniciales (idioma, no usar `any`, estilo). |
| **Bucle de reparación** | Mismo error intentado solucionar más de 2 veces sin leer logs nuevos. |
| **Alucinación de rutas** | Asume que existen archivos que nunca se crearon o confunde nombres. |
| **Respuestas infladas** | Respuestas largas que re-explican todo el historial en lugar de actuar. |
| **Volumen de tokens** | Más de 30-40 turnos o varias salidas grandes de terminal en el historial. |

---

## 3. Protocolo de Compactación en 3 Fases

### Fase A: Congelar el estado (Checkpointed State)

Antes de resumir o cambiar de sesión, vuelca el estado actual a un archivo de checkpoint. El archivo depende del proyecto:

1. Si existe un ledger (`plans/ledger.md`), actualízalo.
2. Si existe `MEMORY.md`, actualiza la sección `## Estado del trabajo`.
3. Si no existe ninguno, crea un archivo temporal `CONTEXT_CHECKPOINT.md` en la raíz.

Estructura obligatoria del checkpoint:

```markdown
# Checkpoint de Contexto — [YYYY-MM-DD HH:MM]

## Objetivo activo
- Qué se está intentando lograr exactamente en 1-2 frases.

## Estado técnico actual
- Rama git: <rama>
- Último commit estable: <sha> — <mensaje>
- Archivos modificados sin commit: [lista o "ninguno"]
- Pruebas/build: [VERDE | ROJO con error específico]

## Lo que FUNCIONA (no tocar)
- Componente/módulo X verificado.
- Endpoint Y probado con éxito.

## Lo que ESTÁ ROTO o PENDIENTE (foco inmediato)
- Problema concreto a resolver ahora mismo.
- Hipótesis o camino elegido.

## Lo que NO funcionó (para no repetir)
- Intento A descartado porque: <motivo>.
- Enfoque B falló por: <error>.

## Siguiente acción atómica
1. Paso inmediato único y concreto.
```

### Fase B: Poda de ruido (Evicción)

Reglas estrictas para limpiar la conversación:

- **No repetir código:** Nunca vuelques archivos enteros en el chat para "recordarlos". Si el archivo está en disco, basta con su ruta.
- **Podar logs:** Conserva solo la última línea de error relevante, no 200 líneas de stacktrace.
- **Eliminar debates resueltos:** Si se discutieron 3 opciones y se eligió una, el registro solo conserva la ganadora y una línea de por qué se descartaron las otras.

### Fase C: Hand-off para nueva sesión (Prompt de Rehidratación)

Cuando la sesión actual esté degradada, genera un **bloque de rehidratación** para pegarlo en una sesión limpia:

```text
ESTADO REHIDRATADO:
- Proyecto: <ruta absoluta>
- Tarea activa: <descripción breve>
- Lee el checkpoint en: <ruta/al/checkpoint.md o plans/ledger.md>
- Último commit: <sha>
- Siguiente paso inmediato: <acción única>
Restricciones clave: <1 a 3 reglas críticas que el modelo nuevo debe cumplir>.
```

---

## 4. Técnicas activas para evitar el context rot DURANTE el trabajo

No esperes a que el contexto se pudra para cuidarlo:

1. **Handoff por archivos:** En lugar de pegar el contenido de un archivo grande en el prompt, indica su ruta para que la herramienta lo lea de forma puntual.
2. **Una tarea a la vez:** Divide trabajos grandes en tareas atómicas. Termina una, commitea, anota en ledger y recién pasa a la siguiente.
3. **Commit frecuente:** Cada commit es un punto de guardado en el mundo real. Si el contexto colapsa, `git status` y `git log` son inmunes al olvido.
4. **Silencio entre herramientas:** No generes párrafos explicativos entre llamadas a herramientas. Una línea corta de intención basta. El registro lo llevan las herramientas y el disco.

---

## 5. Plantilla de comando rápido: `/compact`

Cuando el usuario pida compactar, responder siguiendo este formato exacto:

1. **Vuelco a disco:** Actualizar/crear el archivo de checkpoint en disco.
2. **Resumen ejecutivo:**
   - Hito alcanzado.
   - Estado de git y pruebas.
   - Siguiente paso único.
3. **Recomendación:** Indicar si se puede continuar en la misma sesión o si es momento de abrir una sesión limpia con el bloque de rehidratación listo para copiar.
