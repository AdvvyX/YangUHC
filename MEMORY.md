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
  - Rama base: `main`
  - Skills replicadas y activas: `context-compaction`, `memory`, `security-auditor-remediator`, `subagents`.
  - Flujo de trabajo definido: ramas de trabajo y merge hacia `main`/remoto en `AdvvyX/YangUHC`.

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
