# Memoria del proyecto — SKILLS-AGENTS (workspace) + tienda-digital

## Preferencias del usuario

- Idioma: **español** en respuestas, UI y commits convencionales.
- **Evitar `<textarea>` en formularios** como buena práctica → preferir `<input>` de una línea (decisión del usuario 2026-10-27). Excepción confirmada: campos multilinea esenciales en admin (pegar listas de claves en `SerialStockManager`, descripción de producto en `ProductForm`).
- Para evitar comentarios/spam saturado en formularios públicos: **exigir cuenta creada primero** (nombre tomado de la sesión, nunca campo libre de apodo).
- Trabajar directamente cuando los subagentes están bloqueados ("Free usage limit"): el controlador implementa y revisa.

## Decisiones

### [2026-10-27] Reseñas solo con cuenta de cliente
- Por qué: evitar comentarios saturados/anónimos (petición explícita del usuario).
- Cambios: `POST /api/products/[id]/reviews` → **401 sin sesión**, **409 si ya reseñó ese producto**, `authorName` solo desde la sesión (eliminado del schema Zod); UI sin campo "Tu Nombre o Apodo", CTA "Inicia sesión para opinar" + "Crear cuenta" para invitados, comentario en `<input>` (no textarea).
- Alternativas descartadas: reseñas anónimas con apodo libre (ya existían y permitían spam).

### [2026-10-27] Remediación de lint post-plan (regla `react-hooks/set-state-in-effect`)
- eslint-plugin-react-hooks **v7** marca cualquier función que contenga `setState` invocada directamente desde un efecto, **incluso tras `await`**. Soluciones válidas: `useSyncExternalStore` (montaje/portales), actualización en render para props ("adjusting state when props change"), fetch en el efecto con `setState` en callbacks `.then/.catch/.finally`.
- Ojo: funciones `async` **anidadas dentro del efecto** no se marcan; las definidas a nivel de componente sí (depende del registro de la regla).

## Estado del trabajo

- **Nuevo Proyecto — YangUHC:**
  - Directorio: `C:\Users\DevAdvvy\Desktop\Proyectos\YangUHC`
  - Repositorio: `https://github.com/AdvvyX/YangUHC.git`
  - Rama base: `main` (rama de trabajo: `feature/uhc-system`, mergeada y sincronizada a GitHub).
  - Estado: COMPLETADO y compilado exitosamente con Gradle 9.7 (Java 21). JAR en `build/libs/YangUHC-1.0.0-SNAPSHOT.jar`.
  - Módulos incluidos:
    1. Scoreboard (flickerless por prefijos de equipo, diseños por estado, configurable en `scoreboard.yml`).
    2. Loader World Custom (creación de mundos, gamerules UHC, controlador de WorldBorder con etapas programadas y scatter seguro en espiral para solo y equipos).
    3. 15 Escenarios (CutClean, TimeBomb, Timber, Diamondless, Goldless, Fireless, Rodless, Bowless, Absorptionless, GoldenRetriever, HasteyBoys, Horseless, NoClean, BleedingSweets, Soup) con GUI interactiva `/scenarios`.
    4. Kits con soporte de entrega retardada (delay seconds), editor in-game (`/uhc kit create`, `/uhc kit preview`, `/uhc kit setstarter`) y archivo `kits.yml`.
    5. Teams (Solo, To2, ToX, friendly fire toggle, chat de equipo `/tc`, GUI `/team` y scatter coordinado).
    6. Flujo de partida completo (Lobby -> Scatter -> Grace Period -> PvP -> Deathmatch -> Winner celebration & auto-reset).
    7. Sistema de Lobby Spawn (`/uhc setlobby` / `/uhc setspawn`): limpieza absoluta de inventario (jugadores entran con inventario 100% vacío sin items en barra), hambre cancelada, daño cancelado, anti-caída al vacío.
    8. Menú de Escenarios (`/uhc scenarios` y `/scenarios`): corregido registro de clicks mediante PersistentDataContainer e identificación estricta para operadores (`isOp()`) y permisos `yanguhc.admin`/`yanguhc.host`, con actualización visual en tiempo real de encantamiento y estado (✔ ACTIVADO / ✖ DESACTIVADO). Corregido cierre huérfano de etiqueta `</bold>` en escenarios desactivados y simplificado el lore de ayuda a exactamente `Click para alternar` (ocultando atributos vanilla tipo Attack Damage con `HIDE_ATTRIBUTES`).
    9. Títulos y Scoreboard: `sendTitle` migrado a la API nativa de Adventure `player.showTitle(Title.title(...))` (soluciona códigos crudos `&c&l` en pantalla); números rojos de la derecha del Scoreboard ocultados vía `objective.numberFormat(NumberFormat.blank())` en Paper 1.21+.
    10. Menú Visual de Kits (`/uhc kits` o `/uhc kit`): panel completo con `KitManagerGUI` y `KitEditorGUI`. Cancelación estricta de tareas asíncronas de kits diferidos (`cancelPendingTasks`) al finalizar (`endGame`), reiniciar (`resetGame`) o detener (`stopGame`) la partida, y validación de jugadores vivos (`isAlive`) para evitar entrega de kits en el lobby o tras victoria.
    11. Centralización de Mensajes (`messages.yml`): soporte completo para MiniMessage y formato legacy (`&`), variables/placeholders dinámicos, recarga en vivo con `/uhc reload`. Todos los broadcasts, avisos, títulos, mensajes de kits, teams, escenarios y bordes centralizados y editables.
    12. Selección y Activación Global de Kits por el Host: el host/admin puede elegir exactamente qué kits se entregarán y cuáles no en la partida a través del menú `/uhc kits` o comandos `/uhc kit enable|disable|toggle`. Soporte para activar o desactivar kits individuales (click izquierdo) con brillo encantado dinámico, botones masivos de 'Activar Todos' y 'Desactivar Todos', contador en vivo de kits activos al inicio (0s) y diferidos, persistencia en `kits.yml` (`enabled: true/false`).
    13. Auto-asignación de Equipos Individuales al Iniciar: si la partida tiene modalidad de equipos activa (`max-size > 1`) y algún jugador no llegó a crear o unirse a un equipo antes de `/uhc start`, el sistema le crea automáticamente su propio equipo individual (`Equipo <PlayerName>`) con color único, mensaje informativo `teams.auto-created`, registro en Scoreboard, teleportación segura y detección formal de victoria.
    14. Menú de Ayuda Estructurado en `/team`: reemplazado el texto simple plano por una lista completa y elegante de comandos con descripción para `/team help`, `/team info [jugador]`, `/team list`, `/team invite <jugador>`, etc., con autocompletado inteligente de nombres de jugadores.
    15. Transición Estética de Muerte a Espectador: al morir un jugador en partida activa, se evita la pantalla de muerte y el teleport al spawn/lobby; el jugador respawnea instantáneamente en el mismo lugar de su muerte en modo espectador (`SPECTATOR`), recibe un suave impulso/empuje hacia atrás (alejándolo del asesino con elevación), se genera un rayo cosmético sin daño en el punto de impacto, sonido de muerte del Wither y el título `titles.eliminated` en pantalla. Permanece en el UHC viendo el combate en vivo.
    16. Motor de Animaciones de Texto Estilo TAB (`animations.yml`): sistema con soporte para animaciones configurables (`change-interval` en ms y lista de frames `texts`), placeholders dinámicos `%animation:<nombre>%`, `{animation:<nombre>}` o `<animation:<nombre>>`, sincronización matemática milisegundo a milisegundo por reloj `System.currentTimeMillis()` (sin timers redundantes), integración directa en Scoreboard (títulos y líneas animadas a 2 ticks de actualización sin flicker) y mensajes/títulos, recargable en caliente con `/uhc reload`.
    17. Soporte Nativo de Mensajes Multilínea en `messages.yml`: cualquier clave de mensaje puede ser ahora un texto simple o una lista multilínea (`- "línea 1"`). El gestor `Messages.java` emite automáticamente cada línea en orden para broadcasts y envíos a jugadores. Unificado el anuncio de fin de partida en una sola clave multilínea `game.game-ended` con soporte total para placeholders y animaciones.
    18. Soporte Total de Formatos de Color #RGB y MiniMessage en `TextUtil.java`: parser universal que admite colores hex en todas sus variantes (`#RRGGBB`, `&#RRGGBB`, `{#RRGGBB}`, abreviado `#RGB`), tags modernos de MiniMessage (`<#RRGGBB>`, `<gradient:#c1:#c2>`, `<rainbow>`, `<click:...>`, `<hover:...>`) y códigos legacy tradicionales (`&0-9`, `&a-f`, `&l`, `&k-o`, `&r`, `§`), combinables entre sí sin conflictos ni roturas en todo el servidor (Scoreboard, Actionbars, Títulos, Chat y GUIs).
    19. Validaciones Estrictas de Estado en Comandos (`UHCCommand.java` y `GameManager.java`): protegidos comandos críticos contra ejecuciones en estados inválidos: `/uhc start` bloqueado si ya arrancó la partida o no hay jugadores conectados; `/uhc forcepvp` bloqueado si la partida está en lobby, si el PvP ya inició o si no está en periodo de gracia; `/uhc stop` bloqueado si ya está en lobby; `/uhc revive` bloqueado si la partida no está activa o el jugador ya está con vida; `/uhc border shrink` bloqueado si no hay partida en curso. Mensajes correspondientes centralizados y configurables en `messages.yml`.
    20. Configuración Centralizada de Scoreboard (`scoreboard.yml`): creado archivo de configuración independiente empaquetado en recursos y extraído automáticamente. Permite configurar el título dinámico (`%animation:titleAnimation%`), frecuencia de actualización (2 ticks = 100ms), IP y diseños personalizados por estado (`LOBBY`, `SCATTERING`, `GRACE_PERIOD`, `INGAME`, `DEATHMATCH`, `ENDED`) con placeholders completos (`{alive}`, `{total_players}`, `{team}`, `{scenarios_count}`, `{timer}`, `{pvp_timer}`, `{border}`, `{kills}`, `{coords}`, etc.) y animación fluida `glowIp`.
    21. Regeneración Limpia y Automática de Mundo (`WorldManager.java` y `GameManager.java`): al finalizar cada UHC o ejecutarse `/uhc stop`, todos los jugadores son evacuados al lobby, el mundo anterior se descarga sin guardar (`unloadWorld(false)`), su carpeta se elimina del disco y se genera automáticamente un mundo UHC 100% virgen con una nueva semilla aleatoria (`Random().nextLong()`) y gamerules UHC aplicadas.
    22. Spot Pre-generation Asíncrono y Teletransporte Ultrarrápido (`ScatterManager.java`): al dar `/uhc start`, se calculan de inmediato coordenadas espaciadas para todos los grupos/equipos y se precargan de forma asíncrona en memoria los chunks circundantes (área 3x3 por spawn point) usando `world.getChunkAtAsync(cx, cz, true)`. Se muestra una barra de acción en tiempo real a los jugadores (`Precargando terreno seguro: X%`), y cuando todos los chunks están en RAM, se ejecuta `player.teleportAsync(loc)`. Teletransporte instantáneo en 1-2 segundos sin caída al vacío ni bajadas de TPS.
    23. Cápsulas de Cristal Temporales y Cuenta Regresiva de 5s (`ScatterManager.java`): al teletransportar a los jugadores a sus puntos de scatter, se genera automáticamente una cápsula temporal de cristal 3x3 (suelo, techo y paredes) con parálisis/resistencia temporal y se inicia una cuenta regresiva visual y sonora de 5 a 1 segundo (`BLOCK_NOTE_BLOCK_PLING`). Esto permite que la tarjeta gráfica y la red del cliente rendericen el 100% de los chunks. Al llegar a 0, las cápsulas de cristal se eliminan automáticamente restaurando el bloque original (aire/terreno), se desbloquean los jugadores y el UHC inicia simultáneamente sin pantalla de vacío.
    24. Cancelación Estricta de Tareas de Reinicio y Scatter al Detener Partida (`GameManager.java` y `ScatterManager.java`): la tarea de cuenta regresiva de fin de partida (`endGameTask` / `game.reboot-countdown`) y la tarea de dispersión (`scatterTask`) ahora se almacenan y se cancelan inmediatamente si un administrador ejecuta `/uhc stop` o se reinicia la partida (`resetGame()`), evitando que el mensaje 'El servidor se reiniciará en X segundos' se emita en el chat o se dispare un reinicio duplicado.

- **Plan tienda-digital T1–T9: COMPLETADO y verificado** (informes + suites + auditoría R1–R9 100 %).
- **Commits post-revisión:** `9de51dc` fix(lint: 0 errores/0 avisos), `1385c32` fix(metadata títulos duplicados), `9433aef` feat(reseñas solo con cuenta + comentario en input), **todos push a `origin/main`** (`AdvvyX/Store-DevAdvvy`, sincronizado).
- **Reseñas solo con cuenta: COMPLETADO (2026-10-27)** — 401/403/201/409 verificados por API, E2E en navegador con cuenta QA, datos QA limpiados (`plans/qa-cleanup.ts`), ledger actualizado, lint/build/audit verdes.
- **Siguiente paso:** nada pendiente; pedir al usuario siguiente dirección.

## Aprendizajes del proyecto

- `tienda-digital` NO es el cwd: está en `C:\Users\DevAdvvy\Desktop\Proyectos\tienda-digital` (cwd `SKILLS-AGENTS`); `plans/` va gitignored allí.
- Auditoría pre-commit obligatoria: `SKILLS-AGENTS\.agents\skills\security-auditor-remediator\scripts\audit-security.ps1 -ProjectPath <ruta>` (exit 0 = 100 %).
- PS 5.1: `.Trim()` antes de comparar códigos HTTP de curl (el `\r` rompe `$` en `-match`); salida UTF-8 se lee mal por `Get-Content` (usar la herramienta `read` para editar texto con acentos); git escribe progreso por stderr y PS lo muestra como NativeCommandError (no es error del push).
- Servidor prod en :3000: matar `Get-NetTCPConnection -LocalPort 3000` y re-lanzar `npm start` tras cada `npm run build`.
- Credenciales admin en `.env` de tienda-digital (`ADMIN_USER`/`ADMIN_PASSWORD`), nunca en git ni en memoria.

## Contexto

- El usuario gestiona la tienda desde el panel `/admin` y prueba los flujos en navegador; feedback en ráfagas cortas y en español, con capturas de pantalla.
- GitHub: `https://github.com/AdvvyX/Store-DevAdvvy.git` (rama `main`); el usuario pide sincronizar explícitamente cuando quiere el push.
