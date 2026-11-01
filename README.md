# BattleRoyaleX

Plugin completo, modular y de alto rendimiento de **Battle Royale / UHC** para Minecraft **1.21+** (Paper / Purpur).

---

## 🚀 Características Principales

### 1. 📊 Scoreboard Dinámico y Sin Parpadeos (Flickerless)
- Sistema basado en buffers y prefijos por equipos nativos de Bukkit.
- Soporte para animaciones de texto fluidas (`animations.yml`) estilo TAB.
- Diseños dedicados por estado de la partida:
  - `LOBBY`: Jugadores, estado de equipo, escenarios activos, IP.
  - `SCATTERING`: Progreso de preparación y teletransporte asíncrono.
  - `GRACE_PERIOD`: Tiempo de juego, temporizador de gracia/PvP, radio del borde, coordenadas.
  - `INGAME`: Kills personales, kills de equipo, borde actual y siguiente reducción.
  - `DEATHMATCH`: Borde final, supervivientes restantes.
  - `ENDED`: Ganador y estadísticas finales con invulnerabilidad completa.
- Configurable en `scoreboard.yml` con soporte para **MiniMessage** (`<red>`, `<gradient>`, etc.) y colores legacy (`&a`).

### 2. 🌍 Loader World Custom & Border Controller
- Generación y carga del mundo Battle Royale dedicado (`uhc_world`) y lobby (`world`).
- Regeneración automática y limpia del mundo al terminar o detener la partida.
- Filtro de biomas feos en el scatter (evita océanos profundos, ríos y pantanos).
- Aplicación estricta de GameRules:
  - `naturalRegeneration = false`
  - `doImmediateRespawn = true`
  - `pvp = false` (hasta fin de gracia)
  - Dificultad difícil y ciclo de día/noche.
- **Controlador de Borde (WorldBorder)**:
  - Radio inicial personalizable (por defecto `±1000`).
  - Reducciones programadas automáticas (`1000` -> `750` -> `500` -> `250` -> `100` -> `50` -> `25`).
  - Avisos sonoros y en pantalla.
- **Scatter Seguro y Precarga Asíncrona (Solo & Equipos)**:
  - Precarga asíncrona de terreno (chunks 3x3) para teletransporte instantáneo sin lag.
  - Cápsulas de cristal temporales con cuenta atrás de 5 segundos mientras carga el render.
  - Los compañeros de equipo aparecen juntos.

### 3. 🧩 Sistema Modular de Escenarios (15 Escenarios Incluidos)
Interfaz interactiva de 54 slots (`/scenarios` o `/br scenarios`) con toggle en tiempo real:
1. **CutClean**: Minerales y carnes caen cocinados; la grava siempre suelta pedernal.
2. **TimeBomb**: El loot de los jugadores muertos se almacena en cofres con holograma de 30s antes de explotar.
3. **Timber**: Talar un tronco rompe el árbol entero.
4. **Diamondless**: No se pueden picar diamantes; se consiguen únicamente eliminando jugadores.
5. **Goldless**: No se puede minar oro; se consiguen 8 lingotes de oro por kill.
6. **Fireless**: Inmunidad total al fuego y lava.
7. **Rodless**: Las cañas de pescar no empujan ni dañan jugadores.
8. **Bowless**: Desactiva el disparo de arcos y ballestas.
9. **Absorptionless**: Las manzanas doradas no otorgan corazones de absorción.
10. **GoldenRetriever**: Cada kill suelta una **Golden Head** (+4 corazones de regeneración y velocidad).
11. **HasteyBoys**: Las herramientas crafteadas reciben Eficiencia III y Durabilidad III.
12. **Horseless**: No se permite montar ni domesticar caballos.
13. **NoClean**: 20 segundos de invulnerabilidad tras conseguir una kill.
14. **BleedingSweets**: Cada kill suelta diamante, oro, libro y flechas.
15. **Soup**: Consumir sopa cura 3.5 corazones instantáneamente.

### 4. 🎒 Kits Predeterminados y Modificables con Delay
- Configuración en `kits.yml`.
- Kit inicial (`starter`) y selector visual in-game (`/br kits`).
- Entrega retardada (Delay) para drops o fases avanzadas.
- Editor in-game: `/br kit create <nombre> [demora_seg]`, `/br kit preview <nombre>`, `/br kit setstarter <nombre>`.

### 5. 👥 Sistema de Equipos Numerados y Balanceo Aleatorio
- Modos: Solos (`Equipo #1`, `Equipo #2`...), Dúos, Tríos o tamaño personalizable.
- Asignación 100% aleatoria al iniciar con balanceo equitativo.
- Tags visuales de equipo en Nametag (arriba de la cabeza), Tablist, Scoreboard y Chat.
- Fuego amigo (`friendly-fire`) configurable.
- Chat exclusivo de equipo con `/tc` o `/tc [mensaje]`.

---

## 🛠️ Comandos y Permisos

| Comando | Descripción | Permiso |
|---|---|---|
| `/br start` | Abre la GUI de configuración e inicio rápido (o `/br start now`) | `battleroyale.admin` / `battleroyale.host` |
| `/br stop` | Detiene la partida y restablece el lobby | `battleroyale.admin` |
| `/br scenarios` | Abre la GUI administrativa de escenarios | `battleroyale.admin` |
| `/br forcepvp` | Activa el PvP inmediatamente | `battleroyale.admin` |
| `/br border <shrink\|set>` | Controla el tamaño y reducción del borde | `battleroyale.admin` |
| `/br kits` | Abre el gestor visual de kits | `battleroyale.admin` |
| `/br revive <jugador>` | Revive a un jugador eliminado | `battleroyale.admin` |
| `/br setspawn` / `/br setlobby` | Establece el punto de aparición del Lobby | `battleroyale.admin` |
| `/br teamsize <número>` | Configura el tamaño máximo de los equipos | `battleroyale.admin` |
| `/br reload` | Recarga las configuraciones (`config.yml`, `messages.yml`, etc.) | `battleroyale.admin` |
| `/team` | Abre la interfaz de equipos o información | Todos |
| `/tc [mensaje]` | Alterna o envía mensajes al chat de equipo | Todos |
| `/scenarios` | Consulta los escenarios activos en la partida | Todos |

*Nota: `/uhc` sigue disponible como alias alternativo para los comandos principales.*

---

## 📦 Compilación y Requisitos

- **Java**: JDK 21 o superior.
- **Servidor**: Paper / Purpur 1.21 o superior.

### Compilar desde la terminal:
```bash
./gradlew build
```
El archivo JAR generado se encontrará en:
`build/libs/BattleRoyaleX-1.0.0-SNAPSHOT.jar`
