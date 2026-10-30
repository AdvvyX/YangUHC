---
name: Subagentes
description: Ejecutar tareas con subagentes de contexto aislado al estilo superpowers, con un subagente fresco por tarea, revisión tras cada tarea y revisión final amplia. Usar cuando hay un plan o varias tareas o investigaciones independientes que delegar, para preservar el contexto de la sesión principal, paraleizar lecturas o aislar riesgo. Funciona con cualquier modelo o harness con herramienta de subagentes tipo subagent, task o spawn, o con sesiones nuevas.
---

# Subagentes

Ejecuta un trabajo delegándolo a subagentes frescos con contexto aislado: **un subagente por tarea + una revisión por tarea (spec + calidad) + una revisión final amplia**.

**Por qué subagentes:** delegas en agentes especializados con contexto aislado. Construyendo a mano sus instrucciones, aseguras que se mantengan enfocados en su tarea: nunca heredan tu conversación, tú construyes exactamente lo que necesitan. Y conservas tu propio contexto para coordinar.

**Narración:** entre llamadas a herramientas, cuenta como máximo una línea corta; el ledger y los resultados de las herramientas llevan el registro.

**Ejecución continua:** no pares a preguntar "¿sigo?" entre tareas. Ejecuta todas las tareas del plan. Solo te detienes por estas cuatro razones: (1) una operación irreversible o destructiva; (2) una acción sensible de seguridad; (3) un efecto fuera de este trabajo que las normas exigen preguntar primero (merge, push a rama compartida, publicación); (4) un plan tan roto que todo avance es una suposición. Si no, sigue.

**Resoluciones, no bloqueos:** los conflictos, ambigüedades y defectos del plan se deciden, no se esperan. Registra cada decisión en el ledger como `Resolución: <qué decidí> — <por qué> — <qué cuesta si me equivoco>` y sigue. Un error de criterio cuesta retrabajo visible; una sesión detenida cuesta el día entero y no compra nada.

## Cuándo usarla

| Situación | Camino |
|---|---|
| Plan con tareas mayormente independientes y herramienta de subagentes disponible | **Esta skill** |
| Tareas acopladas (mismos archivos, dependencias cruzadas) | Ejecución en línea, en secuencia |
| Sin herramienta de subagentes | Ejecución en línea, o una sesión limpia nueva por tarea |
| Solo lectura o investigación independiente en paralelo | Despachos en paralelo (ver «Paralelismo») |

**Frente a ejecutarlo tú mismo (en línea):** subagente fresco por tarea = cero contaminación de contexto, con revisión tras cada tarea en vez de solo al final. A cambio, cuesta un contexto nuevo por tarea y por revisión; en línea cuesta un contexto más un revisor final.

## Adaptación al entorno (agnóstico de modelo y harness)

Antes de empezar, identifica en tu entorno:

1. **Herramienta de subagentes:** algo tipo `subagent`, `task`, `spawn_agent`. Si existe, úsala. **Si no existe:** ejecuta cada tarea en una sesión o ventana limpia nueva y vuelve con el informe — el patrón es idéntico: contexto cero + informe escrito.
2. **Selección de modelo por despacho:** si tu herramienta lo permite, elígelo siempre explícitamente (ver «Selección de modelo»). Un despacho sin modelo hereda el de la sesión, casi siempre el más capaz y caro, lo que anula esa decisión en silencio.
3. **Archivos como canal de transferencia:** todo lo que pase entre tú y un subagente va por archivos (brief, informe, paquete de diff). Lo que se pega en un prompt vive en tu contexto hasta el final de la sesión y se relee en cada turno posterior.

## Preparación

1. **Aísla el trabajo:** usa un worktree o rama aislada. Nunca implementes sobre main sin consentimiento explícito.
2. **Ledger:** la memoria conversacional no sobrevive a la compactación. Un controlador que pierde su lugar ha re-despachado secuencias de tareas enteras ya completas — el fallo más caro observado. Lleva el progreso en un archivo `ledger.md`, no solo en todos:
   - Primera línea: `# Ledger — plan: <ruta del plan>`.
   - Es tu mapa de recuperación: los commits que nombra existen en git aunque tu contexto ya no recuerde haberlos creado. Tras una compactación, confía en el ledger y en `git log` sobre tu propio recuerdo.
3. **Lee el plan una vez**, anota su contexto y sus restricciones globales, y crea un todo por tarea.
4. Antes de la tarea 1, haz un barrido de conflictos con lo ya existente y registra las resoluciones en el ledger.

## El ciclo por tarea

### 1. Despachar al implementador

Plantilla: [references/implementer-prompt.md](references/implementer-prompt.md)

- Antes del despacho registra `BASE = git rev-parse HEAD` si vas a revisar diffs. Nunca uses `HEAD~1`: descarta todos los commits menos el último de una tarea multi-commit.
- **Brief por tarea:** extrae el texto completo de la tarea a un archivo único (`task-N-brief.md`). El despacho lleva: (1) una línea de contexto; (2) la ruta del brief como fuente única de requisitos; (3) interfaces y decisiones de tareas previas que el brief no puede conocer; (4) tu resolución de cada ambigüedad; (5) la ruta del informe y su contrato. Los valores exactos viven solo en el brief. **Nunca hagas que un subagente lea el plan completo.**
- **Un despacho = una tarea.** No pegues resúmenes acumulados de tareas anteriores ("estado tras las tareas 1-3"): un despacho real llegó a 42k caracteres de los cuales 99% era historial pegado. Un subagente fresco necesita su tarea, las interfaces que toca y las restricciones globales. Nada más.
- El despacho incluye el contrato **no-subagentes**: el implementador nunca despacha subagentes — ni ayudantes, ni revisores. La revisión llega de ti, después del informe.
- **Nunca despaches dos implementadores en paralelo** (conflictos sobre los mismos archivos).
- Regresa la identidad del subagente despachado: las primeras rondas del bucle de corrección continúan con él.

### 2. Manejar el informe

Estados: `DONE`, `DONE_WITH_CONCERNS`, `NEEDS_CONTEXT`, `BLOCKED`.

- **DONE:** genera el paquete de revisión (commits + stat + diff del rango BASE..HEAD escrito a un archivo único) y despacha al revisor con su ruta.
- **DONE_WITH_CONCERNS / NEEDS_CONTEXT:** si es una pregunta, respóndale clara y completa con el contexto que haga falta; no la precipites a implementar.
- **BLOCKED:** resuelve el impedimento, o registra una resolución en el ledger y vuelve a despachar.
- Un informe sin ambos veredictos (spec ✅ **y** calidad aprobada) no es un informe aceptado.

### 3. Revisar la tarea

Plantilla: [references/reviewer-prompt.md](references/reviewer-prompt.md)

- El revisor recibe tres rutas — el brief, el informe y el paquete de diff — más las restricciones globales que ligan a esta tarea.
- El bloque de restricciones es su lente de atención: copia los requisitos vinculantes verbatim (valores exactos, formatos, relaciones entre componentes).
- La revisión por tarea es la puerta de cada tarea; la revisión amplia ocurre una sola vez, al final. Nunca la saltes.
- **No pre-juzgues los hallazgos:** nunca escribas en el prompt cosas como "no marques X", "a lo máximo Menor" o "el plan lo eligió así". Si crees que un hallazgo sería un falso positivo, deja que lo levante y adjudícalo en el bucle de corrección.
- No le pidas re-ejecutar pruebas que el implementador ya corrió en el mismo código; el informe lleva la evidencia.

### 4. Bucle de corrección

Se activa con spec ❌, cualquier hallazgo Crítico o Importante, o un ítem ⚠️ que confirmaste como gap real.

- **Tope de 3 rondas** por tarea. Si en la tercera sigue fallando, registra en el ledger `Resolución: tarea N estacionada con <K> hallazgos abiertos — <por qué> — <qué cuesta>` y pasa adelante (o escala el modelo una tier arriba para un último intento).
- Cada ronda termina con una **re-revisión acotada** de los fixes de esa ronda: nunca des un fix por revisado.
- La re-revisión acotada solo verifica los fixes. Hallazgos nuevos en código no tocado van al ledger, no al bucle.
- Nunca pases de tarea con hallazgos Críticos o Importantes que ni estén corregidos ni estacionados con resolución.

### 5. Completar la tarea

Cuando la revisión vuelva limpia (o cada hallazgo abierto esté estacionado con resolución), añade al ledger en el mismo mensaje que el resto de tu contabilidad:

- `Tarea N: completa (commits abc1234..def5678, revisión limpia)`
- `Tarea N: completa (commits abc1234..def5678, K estacionados)` tras activar el tope.

Marca el todo como completo y sigue.

## Selección de modelo

Usa el modelo menos potente que maneje cada rol. El costo y el tiempo escalan con el número de rondas, no solo con el precio por token: los modelos más baratos toman 2-3× más rondas en trabajo multi-paso y terminan costando más. La cantidad de rondas pesa más que el precio por token.

| Rol | Modelo |
|---|---|
| Implementación mecánica (1-2 archivos, spec completo, o el plan ya trae el código a escribir) | El más barato o rápido |
| Integración y juicio (multi-archivo, depuración, trabajo desde descripción en prosa) | Estándar — tier media, también suelo para revisores |
| Arquitectura, diseño y revisión final de todo el trabajo | El más potente disponible |
| Revisión | Escalado al tamaño y riesgo del diff: pequeño y mecánico no necesita el más potente; un cambio sutil de concurrencia sí |
| Corrección tardía (rondas 4-5 si escalaste) | Al menos una tier por encima del implementador que se atascó |

**Especifica el modelo siempre explícitamente en el despacho** cuando tu herramienta lo permita.

Señales de complejidad (tareas de implementación):

- Toca 1-2 archivos con spec completo → modelo barato.
- Toca varios archivos con problemas de integración → modelo estándar.
- Requiere juicio de diseño o entendimiento amplio del código → modelo más potente.

## Paralelismo

- **Prohibido en paralelo:** implementación, y cualquier tarea que toque los mismos archivos.
- **Permitido:** investigación y lectura independiente; revisores de tareas ya terminadas y no solapadas. El controlador sigue siendo quien lee e integra los resultados.
- **Esperando subagentes:** nunca hagas polling con timeouts cortos ni te quedes en un solo bloqueo silencioso e infinito. Sigue trabajando en local (ledger, empaquetar la siguiente revisión, leer informes) y reconcilia cada 5-10 minutos: lista tus hijos y persigue a los que terminaron sin reportar.

## Revisión final

Con todas las tareas completas, despacha **una revisión amplia de todo el trabajo** al modelo más potente disponible, entregándole por archivo el plan o spec, el rango completo de commits con su diff, y el ledger.

- Su foco es lo que la revisión por tarea no ve: integración entre tareas, deuda acumulada, coherencia del conjunto.
- Aplica un solo ciclo de corrección sobre sus hallazgos, con re-revisión acotada de ese fix.
- Si todo queda limpio, borra el directorio de trabajo de este plan (briefs, informes, paquetes) y termina.

## Errores comunes

| Excusa | Realidad |
|---|---|
| "Cumple más o menos con el spec" | El revisor encontró brechas = no está terminado. Corrige o estaciona con resolución. |
| "Lo arreglo yo, despachar es overhead" | Tus arreglos contaminan tu contexto y saltan la revisión. Reanuda al implementador. |
| "Una ronda más converge" | Pasado el tope, las rondas no convergen: el fallo es estructural. Adjudica y enruta. |
| "El hallazgo es obviamente falso, lo descarto" | Adjudicas solo en el tope, y cada resolución es una línea en el ledger. Descartes silenciosos, prohibidos. |
| "El fix fue pequeño, salto la re-revisión" | Los fixes sin revisar son cómo entran las regresiones. Toda ronda termina con re-revisión acotada. |
| "El ledger es overhead" | El ledger es lo que sobrevive a la compactación. Sin él, se re-despachan tareas enteras ya completas. |
| "El implementador creó su propio revisor, es garantía extra" | Es un asiento duplicado revisando el mismo diff; la puerta es la revisión de tarea. Un revisor creado por el trabajador es un defecto a señalar, no rigor. |
| "Revisar todo al final es suficiente" | La revisión por tarea es la puerta de cada tarea. La final ve el conjunto, no reemplaza las puertas. |
