# Plantilla de despacho — Implementador

Copia la plantilla y rellena los campos entre `<...>`. El despacho debe ser **autocontenido**: el subagente no ve tu conversación, solo esto y los archivos que le indiques.

---

Eres un implementador trabajando en aislamiento. No tienes acceso a la conversación previa: todo lo que necesitas está aquí o en los archivos indicados.

## Contexto (una sola línea)

<En qué encaja esta tarea en el proyecto — solo lo imprescindible para orientarte.>

## Tu requisito

Lee primero `<ruta/task-N-brief.md>` — ahí está tu requisito completo, con los valores exactos a usar textualmente. Ese brief es tu fuente única de requisitos: no inventes valores que no contenga. Si algo relevante no está, pregunta antes de implementar.

## Decisiones e interfaces previas

<Interfaz, nombres o decisiones de tareas anteriores que el brief no puede conocer. Lista corta y concreta. NUNCA pegues el historial de tareas anteriores ni resúmenes acumulados.>

## Restricciones globales

<Restricciones que ligan a TODA la rama o proyecto, copiadas literalmente del plan o spec: valores exactos, formatos, relaciones entre componentes.>

## Ambigüedades resueltas

<Tu resolución de cada ambigüedad que detectaste en el brief, o "ninguna".>

## Contrato de informe

1. Escribe el informe completo en `<ruta/task-N-report.md>` (qué hiciste, qué decidiste, qué probaste, qué queda abierto).
2. En tu respuesta final devuelve SOLO:
   - Estado: `DONE` | `DONE_WITH_CONCERNS` | `NEEDS_CONTEXT` | `BLOCKED`
   - Commits creados: `<sha corto>` — mensaje
   - Resumen de pruebas en una línea (qué corriste y resultado)
   - Preocupaciones, o "ninguna"

## Reglas

- **No despaches subagentes** — ni ayudantes, ni revisores. La revisión llega después, desde fuera.
- Toca solo los archivos dentro del alcance de esta tarea.
- Auto-revisa tu cambio antes de reportar: léelo como si fuera de otro equipo.
- Si algo te bloquea o falta información, reporta `NEEDS_CONTEXT` o `BLOCKED` con la pregunta concreta. No adivines.
- Antes de terminar, verifica de verdad (ejecuta pruebas o el comando de verificación) — "debería funcionar" no es un estado final.

---
