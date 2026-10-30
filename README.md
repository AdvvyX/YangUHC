# YangUHC

Plugin completo, modular y de alto rendimiento de **Ultra Hardcore (UHC)** para Minecraft **1.21+** (Paper / Purpur).

---

## 🚀 Características Principales

### 1. 📊 Scoreboard Dinámico y Sin Parpadeos (Flickerless)
- Sistema basado en buffers y prefijos por equipos nativos de Bukkit.
- Diseños dedicados por estado de la partida:
  - `LOBBY`: Jugadores, estado de equipo, escenarios activos, IP.
  - `SCATTERING`: Progreso de preparación y teletransporte.
  - `GRACE_PERIOD`: Tiempo de juego, temporizador de PvP, radio del borde, coordenadas.
  - `INGAME`: Kills personales, kills de equipo, borde actual y siguiente reducción.
  - `DEATHMATCH`: Borde final, supervivientes restantes.
  - `ENDED`: Ganador y estadísticas finales.
- Configurable en `scoreboard.yml` con soporte para **MiniMessage** (`<red>`, `<gradient>`, etc.) y colores legacy (`&a`).

### 2. 🌍 Loader World Custom & Border Controller
- Generación y carga del mundo UHC dedicado (`uhc_world`) y lobby (`world`).
- Aplicación estricta de GameRules UHC:
  - `naturalRegeneration = false`
  - `doImmediateRespawn = true`
  - `pvp = false` (hasta fin de gracia)
  - Dificultad difícil y ciclo de día/noche.
- **Controlador de Borde (WorldBorder)**:
  - Radio inicial personalizable (por defecto `±1000`, 2000x2000 bloques).
  - Reducciones programadas automáticas (`1000` -> `750` -> `500` -> `250` -> `100` -> `50` -> `25`).
  - Avisos sonoros y en pantalla.
- **Scatter Seguro (Solo & Equipos)**:
  - Distribución en espiral/círculo evitando océanos, lava y caídas al vacío.
  - Los compañeros de equipo aparecen juntos o próximos.
  - Efectos de resistencia y protección anti-caída mientras cargan chunks.

### 3. 🧩 Sistema Modular de Escenarios (15 Escenarios Incluidos)
Interfaz interactiva de 54 slots (`/scenarios`) y toggle en tiempo real:
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
- Kit inicial (`starter`) con armadura, herramientas, comida y materiales esenciales.
- **Entrega retardada (Delay)**: Kits que se entregan tras cierto tiempo de partida (ej. drops de suministros a los 10 minutos).
- Edición in-game: `/uhc kit create <nombre> [demora_seg]`, `/uhc kit preview <nombre>`, `/uhc kit setstarter <nombre>`.

### 5. 👥 Sistema de Equipos (Teams)
- Modos: Solo / FFA, To2, To3, To4 o tamaño personalizable.
- GUI interactiva `/team`.
- Fuego amigo (`friendly-fire`) configurable.
- Chat exclusivo de equipo con `/tc [mensaje]` o toggle.
- Colores distintivos y teletransporte coordinado en el scatter.

---

## 🛠️ Comandos y Permisos

| Comando | Descripción | Permiso |
|---|---|---|
| `/uhc start` | Inicia el scatter y la partida | `yanguhc.admin` |
| `/uhc stop` | Detiene la partida y restablece el lobby | `yanguhc.admin` |
| `/uhc scenarios` | Abre la GUI administrativa de escenarios | `yanguhc.admin` |
| `/uhc forcepvp` | Activa el PvP inmediatamente | `yanguhc.admin` |
| `/uhc border <shrink\|set>` | Controla el tamaño y reducción del borde | `yanguhc.admin` |
| `/uhc kit <create\|preview\|list>` | Administrador de kits | `yanguhc.admin` |
| `/uhc revive <jugador>` | Revive a un jugador eliminado | `yanguhc.admin` |
| `/uhc reload` | Recarga configuraciones | `yanguhc.admin` |
| `/team` | Abre la interfaz de gestión de equipos | Todos |
| `/tc [mensaje]` | Chat interno del equipo | Todos |
| `/scenarios` | Consulta los escenarios activos en la partida | Todos |

---

## 📦 Compilación
```bash
./gradlew build
```
El archivo JAR generado se encontrará en:
`build/libs/YangUHC-1.0.0-SNAPSHOT.jar`
