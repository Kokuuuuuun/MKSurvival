# MKSurvival Plugin Documentation

## Descripción General
MKSurvival es un plugin de Minecraft Bukkit/Spigot que transforma un servidor de supervivencia en una experiencia RPG completa. Incluye sistemas de clases, habilidades, misiones, dungeons, minas, economía, y mucho más.

## Características Implementadas

### 1. Hub Central
- **Descripción**: Un área central donde los jugadores spawnearán al entrar al servidor.
- **Funcionalidades**:
    - NPCs informativos y de acceso a funciones
    - Portales a diferentes áreas del servidor
    - Menú principal con acceso a todas las funciones
    - Perfil del jugador con estadísticas
    - Efectos visuales y partículas
    - Protección contra daño y modificaciones del mundo
- **Comandos**:
    - `/hub` - Teletransportarse al hub

### 2. Sistema de Clases
- **Descripción**: Permite a los jugadores elegir una clase con habilidades y efectos únicos.
- **Clases Disponibles**:
    - Guerrero: Bonificaciones de combate
    - Arquero: Habilidades a distancia
    - Mago: Hechizos y efectos mágicos
    - Aventurero: Clase equilibrada para principiantes
- **Funcionalidades**:
    - GUI de selección de clases
    - Efectos permanentes según la clase
    - Habilidades especiales
- **Comandos**:
    - `/class` - Abrir GUI de clases
    - `/class choose <clase>` - Elegir una clase
    - `/class info <clase>` - Ver información de una clase

### 3. Sistema de Habilidades
- **Descripción**: Sistema de habilidades que se mejoran al realizar acciones específicas.
- **Habilidades Disponibles**:
    - Minería: Mejora al minar minerales
    - Combate: Mejora al derrotar mobs
    - Tala: Mejora al talar árboles
    - Pesca: Mejora al pescar
    - Agricultura: Mejora al cosechar cultivos
    - Excavación: Mejora al cavar
    - Alquimia: Mejora al usar pociones
    - Encantamiento: Mejora al encantar items
- **Funcionalidades**:
    - Experiencia y niveles por cada habilidad
    - Bonificaciones al subir de nivel
    - GUI de visualización de habilidades
    - Efectos visuales al ganar experiencia
- **Comandos**:
    - `/skills` - Ver GUI de habilidades

### 4. Sistema de Misiones
- **Descripción**: Sistema de misiones que los jugadores pueden completar para obtener recompensas.
- **Tipos de Misiones**:
    - Misiones de combate (derrotar mobs)
    - Misiones de recolección (obtener items)
    - Misiones de minería (minar bloques)
    - Misiones especiales
- **Funcionalidades**:
    - GUI de misiones activas y disponibles
    - Progreso de misiones
    - Recompensas al completar misiones
- **Comandos**:
    - `/quests` - Abrir GUI de misiones
    - `/quests accept <id>` - Aceptar una misión
    - `/quests abandon <id>` - Abandonar una misión

### 5. Sistema de Tiendas
- **Descripción**: Sistema de tiendas donde los jugadores pueden comprar y vender items.
- **Funcionalidades**:
    - Tienda principal del servidor
    - Tiendas de jugadores
    - Categorías de items
    - Sistema de compra/venta
    - GUI interactiva
- **Comandos**:
    - `/shop` - Abrir tienda principal
    - `/shop create <nombre>` - Crear una tienda
    - `/shop additem <precio_compra> <precio_venta>` - Añadir item a la tienda

### 6. Sistema de Tierras
- **Descripción**: Permite a los jugadores reclamar y proteger terrenos.
- **Funcionalidades**:
    - Selección visual de terrenos
    - Reclamación de tierras
    - Protección contra construcción no autorizada
    - Sistema de confianza para otros jugadores
    - Límites de bloques por jugador
- **Comandos**:
    - `/land` - Abrir GUI de tierras
    - `/land claim` - Reclamar terreno seleccionado
    - `/land unclaim` - Abandonar terreno actual
    - `/land info` - Ver información del terreno
    - `/land trust <jugador>` - Dar acceso a otro jugador
    - `/land untrust <jugador>` - Quitar acceso a otro jugador
    - `/land selection` - Iniciar selección de terreno

### 7. Sistema de Dungeons
- **Descripción**: Áreas desafiantes con mobs de alto nivel y recompensas especiales.
- **Dungeons Disponibles**:
    - Mazmorra de Zombis (Nivel 10)
    - Mazmorra de Esqueletos (Nivel 15)
    - Mazmorra de Creepers (Nivel 20)
    - Mazmorra del Nether (Nivel 25)
    - Mazmorra del End (Nivel 30)
- **Funcionalidades**:
    - Niveles mínimos requeridos
    - Cooldowns entre entradas
    - Recompensas especiales
    - Comandos de administración
- **Comandos**:
    - `/dungeon` - Listar dungeons disponibles
    - `/dungeon <nombre>` - Ver información de un dungeon
    - `/dungeon tp <nombre>` - Teletransportarse a un dungeon
    - `/dungeon reset <nombre>` - Resetea un dungeon (admin)

### 8. Sistema de Minas
- **Descripción**: Áreas de minería con minerales especiales y recompensas.
- **Minas Disponibles**:
    - Mina de Carbón (Nivel 1)
    - Mina de Hierro (Nivel 5)
    - Mina de Oro (Nivel 10)
    - Mina de Diamante (Nivel 15)
    - Mina de Esmeralda (Nivel 20)
    - Mina de Netherita (Nivel 25)
- **Funcionalidades**:
    - Niveles mínimos requeridos
    - Cooldowns entre entradas
    - Herramientas proporcionadas
    - Recompensas especiales
    - Comandos de administración
- **Comandos**:
    - `/mine` - Listar minas disponibles
    - `/mine <nombre>` - Ver información de una mina
    - `/mine tp <nombre>` - Teletransportarse a una mina
    - `/mine reset <nombre>` - Resetea una mina (admin)

### 9. Sistema de Economía
- **Descripción**: Sistema económico que permite a los jugadores gestionar su dinero.
- **Funcionalidades**:
    - Balance de jugadores
    - Transacciones entre jugadores
    - Formato de moneda personalizable
    - Integración con otros sistemas
- **Comandos**:
    - `/balance` - Ver tu dinero
    - `/pay <jugador> <cantidad>` - Pagar a otro jugador
    - `/balance <jugador>` - Ver dinero de otro jugador

### 10. Sistema de Jefes (Bosses)
- **Descripción**: Jefes poderosos que spawnean periódicamente con recompensas valiosas.
- **Jefes Disponibles**:
    - Rey Zombi
    - Señor Esqueleto
- **Funcionalidades**:
    - Spawn automático cada 2 horas
    - Atributos mejorados
    - Recompensas especiales
    - Anuncios de spawn y derrota

### 11. Sistema de Niveles en Mobs
- **Descripción**: Los mobs tienen niveles que afectan sus atributos y recompensas.
- **Funcionalidades**:
    - Niveles aleatorios para cada mob
    - Atributos escalados según el nivel
    - Drops especiales para mobs de alto nivel
    - Efectos visuales al morir

### 12. Sistema de Logros
- **Descripción**: Logros desbloqueables que otorgan recompensas.
- **Logros Disponibles**:
    - Logros de combate (matar mobs)
    - Logros de minería (minar bloques)
    - Logros de habilidades (alcanzar niveles)
    - Logros de dinero (acumular riqueza)
    - Logros especiales (completar dungeons, misiones)
- **Funcionalidades**:
    - GUI de logros desbloqueados
    - Progreso de logros
    - Recompensas al desbloquear
    - Efectos visuales
- **Comandos**:
    - `/achievement` - Ver tus logros
    - `/achievement list` - Ver todos los logros
    - `/achievement check` - Verificar logros desbloqueables
    - `/achievement progress` - Ver progreso de logros

### 13. Sistema de Partículas
- **Descripción**: Efectos visuales para mejorar la experiencia del jugador.
- **Efectos Disponibles**:
    - Efectos al subir de nivel
    - Efectos al completar misiones
    - Efectos al obtener recompensas
    - Efectos en entradas de dungeons y minas
    - Efectos al morir mobs

### 14. Sistema de Cooldowns
- **Descripción**: Sistema de tiempos de espera para evitar abuso de ciertas funciones.
- **Funcionalidades**:
    - Cooldowns para dungeons y minas
    - Cooldowns para NPCs y portales
    - Notificaciones cuando los cooldowns terminan

### 15. Sistema de Configuración
- **Descripción**: Sistema de archivos de configuración para personalizar el plugin.
- **Archivos de Configuración**:
    - `config.yml` - Configuración principal
    - `shops.yml` - Configuración de tiendas
    - `quests.yml` - Configuración de misiones
    - `classes.yml` - Configuración de clases
    - `skills.yml` - Configuración de habilidades
    - `lands.yml` - Configuración de tierras
    - `players.yml` - Datos de jugadores

## Comandos del Plugin

### Comandos Generales
- `/help` - Muestra ayuda sobre comandos
- `/hub` - Teletransportarse al hub

### Comandos de Tierras
- `/land` - Abrir GUI de tierras
- `/land claim` - Reclamar terreno seleccionado
- `/land unclaim` - Abandonar terreno actual
- `/land info` - Ver información del terreno
- `/land trust <jugador>` - Dar acceso a otro jugador
- `/land untrust <jugador>` - Quitar acceso a otro jugador
- `/land selection` - Iniciar selección de terreno

### Comandos de Tiendas
- `/shop` - Abrir tienda principal
- `/shop create <nombre>` - Crear una tienda
- `/shop additem <precio_compra> <precio_venta>` - Añadir item a la tienda

### Comandos de Clases
- `/class` - Abrir GUI de clases
- `/class choose <clase>` - Elegir una clase
- `/class info <clase>` - Ver información de una clase

### Comandos de Misiones
- `/quests` - Abrir GUI de misiones
- `/quests accept <id>` - Aceptar una misión
- `/quests abandon <id>` - Abandonar una misión

### Comandos de Habilidades
- `/skills` - Ver GUI de habilidades

### Comandos de Dungeons
- `/dungeon` - Listar dungeons disponibles
- `/dungeon <nombre>` - Ver información de un dungeon
- `/dungeon tp <nombre>` - Teletransportarse a un dungeon
- `/dungeon reset <nombre>` - Resetea un dungeon (admin)

### Comandos de Minas
- `/mine` - Listar minas disponibles
- `/mine <nombre>` - Ver información de una mina
- `/mine tp <nombre>` - Teletransportarse a una mina
- `/mine reset <nombre>` - Resetea una mina (admin)

### Comandos de Economía
- `/balance` - Ver tu dinero
- `/pay <jugador> <cantidad>` - Pagar a otro jugador
- `/balance <jugador>` - Ver dinero de otro jugador

### Comandos de Logros
- `/achievement` - Ver tus logros
- `/achievement list` - Ver todos los logros
- `/achievement check` - Verificar logros desbloqueables
- `/achievement progress` - Ver progreso de logros

## Permisos

### Permisos Generales
- `mksurvival.*` - Acceso a todos los comandos
- `mksurvival.hub` - Acceso al comando /hub

### Permisos de Tierras
- `mksurvival.land.*` - Acceso a todos los comandos de tierras
- `mksurvival.land.claim` - Reclamar tierras
- `mksurvival.land.unclaim` - Abandonar tierras
- `mksurvival.land.trust` - Confiar jugadores

### Permisos de Tiendas
- `mksurvival.shop.*` - Acceso a todos los comandos de tiendas
- `mksurvival.shop.create` - Crear tiendas
- `mksurvival.shop.additem` - Añadir items a tiendas

### Permisos de Clases
- `mksurvival.class.*` - Acceso a todos los comandos de clases
- `mksurvival.class.choose` - Elegir clases
- `mksurvival.class.info` - Ver información de clases

### Permisos de Misiones
- `mksurvival.quests.*` - Acceso a todos los comandos de misiones
- `mksurvival.quests.accept` - Aceptar misiones
- `mksurvival.quests.abandon` - Abandonar misiones

### Permisos de Habilidades
- `mksurvival.skills.*` - Acceso a todos los comandos de habilidades

### Permisos de Dungeons
- `mksurvival.dungeon.*` - Acceso a todos los comandos de dungeons
- `mksurvival.dungeon.tp` - Teletransportarse a dungeons
- `mksurvival.dungeon.reset` - Resetea dungeons (admin)

### Permisos de Minas
- `mksurvival.mine.*` - Acceso a todos los comandos de minas
- `mksurvival.mine.tp` - Teletransportarse a minas
- `mksurvival.mine.reset` - Resetea minas (admin)

### Permisos de Economía
- `mksurvival.economy.*` - Acceso a todos los comandos de economía
- `mksurvival.economy.balance` - Ver balance
- `mksurvival.economy.pay` - Pagar a otros jugadores

### Permisos de Logros
- `mksurvival.achievement.*` - Acceso a todos los comandos de logros

### Permisos de Administrador
- `mksurvival.admin.*` - Comandos de administrador
- `mksurvival.admin.dungeon.reset` - Resetea dungeons
- `mksurvival.admin.mine.reset` - Resetea minas

## Instalación

1. Descarga el archivo JAR del plugin.
2. Coloca el archivo en la carpeta `plugins` de tu servidor.
3. Reinicia el servidor.
4. El plugin generará automáticamente los archivos de configuración necesarios.

## Configuración

El plugin genera varios archivos de configuración en la carpeta `plugins/MKSurvival/`:

- `config.yml` - Configuración principal del plugin
- `shops.yml` - Configuración de tiendas
- `quests.yml` - Configuración de misiones
- `classes.yml` - Configuración de clases
- `skills.yml` - Configuración de habilidades
- `lands.yml` - Configuración de tierras
- `players.yml` - Datos de jugadores

Puedes editar estos archivos para personalizar el funcionamiento del plugin según tus necesidades.

## Dependencias

El plugin requiere las siguientes dependencias:

- Minecraft versión 1.16 o superior
- Spigot o Paper
- Vault (para economía)

## API

El plugin incluye una API que permite a otros plugins interactuar con sus sistemas:

```java
// Obtener instancia del plugin
MKSurvival plugin = MKSurvival.getInstance();

// Acceder a los managers
LandManager landManager = plugin.getLandManager();
ShopManager shopManager = plugin.getShopManager();
SkillManager skillManager = plugin.getSkillManager();
QuestManager questManager = plugin.getQuestManager();
ClassManager classManager = plugin.getClassManager();
BossManager bossManager = plugin.getBossManager();
EnemyLevelManager enemyLevelManager = plugin.getEnemyLevelManager();
DungeonManager dungeonManager = plugin.getDungeonManager();
MineManager mineManager = plugin.getMineManager();
EconomyManager economyManager = plugin.getEconomyManager();
RewardManager rewardManager = plugin.getRewardManager();
CooldownManager cooldownManager = plugin.getCooldownManager();
ParticleManager particleManager = plugin.getParticleManager();
AchievementManager achievementManager = plugin.getAchievementManager();
NotificationManager notificationManager = plugin.getNotificationManager();
HubManager hubManager = plugin.getHubManager();