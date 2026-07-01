# SwiftCore — Mod de optimización (Forge 1.20.1)

## ⚠️ Importante: cómo compilarlo

Este entorno donde escribí el código **no tiene acceso a internet**, así que no
pude ejecutar `gradlew build` para generar el `.jar` final ni descargar las
dependencias de Forge. Te dejo el proyecto fuente completo y listo — solo
te faltan estos pasos en tu propia PC (necesitas internet la primera vez):

### Opción A — Con IntelliJ IDEA (recomendado)
1. Instala **Java 17** (JDK) y **IntelliJ IDEA** (Community sirve).
2. Abre esta carpeta como proyecto Gradle (`File > Open`, selecciona `build.gradle`).
3. IntelliJ detectará el `gradle-wrapper` automáticamente y descargará todo
   (Forge 1.20.1-47.2.0, mappings, etc.) — puede tardar varios minutos.
4. Una vez sincronizado, ejecuta la tarea Gradle `build` (panel Gradle → Tasks → build → build).
5. El `.jar` final aparece en `build/libs/swiftcore-1.0.0.jar`.

### Opción B — Por línea de comandos
```bash
# Dentro de la carpeta swiftcore-mod
gradle wrapper --gradle-version 8.1   # genera el wrapper si no lo tienes
./gradlew build                        # en Windows: gradlew.bat build
```
El resultado queda en `build/libs/swiftcore-1.0.0.jar`.

## Instalación
1. Copia `swiftcore-1.0.0.jar` a la carpeta `mods/` de tu instalación de
   Forge 1.20.1 (cliente o servidor).
2. Embeddium (y cualquier otro mod de renderizado como Oculus/Iris) puede
   convivir sin problema, ya que SwiftCore no toca nada de rendering.

## Qué hace
- **Distancia adaptativa**: si el TPS del servidor cae por debajo de 18,
  reduce la view-distance/simulation-distance paso a paso hasta un mínimo
  configurable, y la restaura automáticamente cuando el servidor se
  estabiliza por encima de 19.5 TPS.
- **randomTickSpeed adaptativo**: baja temporalmente la velocidad de
  crecimiento de cultivos/fuego/hojas cuando hay lag, sin desactivarla
  del todo, y la restaura al recuperarse.
- Comando **`/swiftcore status`** (requiere permisos de operador) para
  ver el TPS estimado y los valores actuales en vivo.

## Configuración
Al arrancar por primera vez se genera `config/swiftcore-common.toml` con
todos los umbrales editables (TPS mínimo, distancias mínimas, intervalo
de revisión, etc.), con comentarios en cada opción.

## Por qué es compatible con Embeddium
Embeddium reemplaza el motor de renderizado de chunks del **cliente**
(mallas, culling visual, shaders). SwiftCore vive enteramente en la
lógica del **servidor** (o servidor integrado en singleplayer): ticks,
distancias de simulación y gamerules. Ninguno de los dos toca el código
del otro, así que no hay superposición ni riesgo de crash por conflicto.
