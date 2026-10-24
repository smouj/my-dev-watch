<div align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="assets/branding/logo-white.png">
    <img src="assets/branding/logo-color.png" alt="My Dev Watch" width="168">
  </picture>

  <h1>My Dev Watch</h1>

  <p><strong>Tu reloj. Bajo tu control.</strong></p>

  <p>Compañero local, estudio de esferas y laboratorio BLE para relojes de la<br>
  familia GloryFit. Sin cuenta, sin anuncios, sin telemetría.</p>
</div>

<p align="center">
  <a href="README.md">English</a> · <strong>Español</strong>
</p>

---

## Qué es

Los relojes baratos traen hardware decente y una app que los secuestra: cuenta
obligatoria, nube, ficha que desaparece de la tienda y datos de salud que no puedes
exportar. My Dev Watch sustituye esa dependencia por etapas y dice exactamente hasta
dónde ha llegado.

- **Compañero** — Bluetooth, batería, actividad, salud, notificaciones y ajustes.
- **Estudio** — editor de esferas por capas, con códec real del contenedor.
- **Laboratorio** — catálogo de comandos, capturas, replay, análisis de firmware de
  solo lectura y una CLI que verifica todo sin reloj.

## Estado

> **Hito 0 completo, hito 1 en marcha.** El núcleo, la CLI, los códecs y el estudio
> web están construidos y probados. La app Android compila en CI y es un esqueleto
> alpha: todavía no se ha ejecutado contra un reloj físico y ningún modelo figura
> como verificado. No existe código para flashear firmware, y es deliberado.

## Qué funciona hoy

| Área | Estado | Detalle |
| --- | --- | --- |
| Identificación de protocolo | **Funciona** | Puntuación ponderada y explicable; por debajo del 60 % dice DESCONOCIDO |
| Batería | **Funciona** | Petición A2 y respuesta con porcentaje validado |
| Contenedor de esfera | **Funciona** | Cabecera, longitud, CRC32, lectura y escritura defensivas |
| Capturas .mdcap | **Funciona** | Parseo, serialización y replay por la pila real |
| Analizador de firmware | **Funciona** | SHA-256, entropía, cadenas, firmas; solo lectura |
| CLI | **Funciona** | hex, battery, commands, identify, watchface, firmware, capture |
| Estudio web | **Funciona** | Capas, lienzo, valores simulados, exportación, PWA |
| App Android | Alpha | Cinco destinos, escáner, matriz de capacidades, estados vacíos honestos |
| Sincronización real | Todavía no | Requiere un reloj verificado y una captura |
| Instalar esferas | Todavía no | Requiere probar la transferencia y el rollback |
| Firmware / sistema abierto | Todavía no | Solo investigación; ver hoja de ruta |

## Instalar

### En el móvil (Android o iPhone)

<p align="center">
  <a href="https://smouj.github.io/my-dev-watch/"><img src="assets/branding/install-qr.svg" alt="Abrir el estudio" width="150"></a>
</p>

Escanea el código o abre <https://smouj.github.io/my-dev-watch/> y elige **Añadir a
pantalla de inicio**. Funciona sin conexión tras la primera carga. Es la misma
aplicación en iPhone y Android, porque iOS no permite instalar un APK sin firma de
Apple.

Para guardar los proyectos en tu PC en lugar de en el navegador:

~~~bash
cd web && npm start
~~~

El servidor imprime una dirección de red local y la página de instalación muestra el
QR correspondiente.

### En el PC

El mismo estudio corre en el escritorio y puede hablar con el reloj por Web Bluetooth
en Chrome o Edge (lee la batería con el mismo comando 0xA2 que el núcleo Kotlin).
start-windows.cmd abre una ventana de aplicación; install-desktop-shortcut.ps1 crea
el acceso directo con el icono.

## Compilar

| Herramienta | Versión |
| --- | --- |
| JDK | 17 |
| Gradle | wrapper 8.10.2 |
| Android SDK | API 35, solo el módulo de app |
| Node | 20+, solo el estudio web |

~~~bash
./gradlew build
./gradlew :tools:cli:run --args="commands"
ANDROID_HOME=/ruta/al/sdk ./gradlew :android:app:assembleDebug
~~~

## La compatibilidad es una escalera

Ningún reloj se marca como "compatible". Cada uno sube niveles y cada nivel tiene un
criterio comprobable:

| Nivel | Nombre | Prueba exigida |
| --- | --- | --- |
| 0 | Detectado | Huella guardada y protocolo explicado |
| 1 | Comunicación | Batería e información sin la app del fabricante |
| 2 | Compañero | Un día completo con la app del fabricante desinstalada |
| 3 | Personalización | Esfera sustituida y restaurada byte a byte |
| 4 | Interfaz | Iconos, menús y temas, cada uno reversible |
| 5 | Firmware | Copia, escritura y recuperación ejecutada de verdad |
| 6 | Sistema abierto | Un firmware abierto arranca, da la hora y carga |

La matriz actual está vacía a propósito: [SUPPORTED_DEVICES.md](SUPPORTED_DEVICES.md).

## Evidencia, no optimismo

Cada comando del catálogo lleva nivel, cadena de evidencia y si está implementado.
Hoy solo la batería está implementada de extremo a extremo, porque es el único
payload corroborado por completo. Todo lo demás está documentado como investigación.

Lee [PROTOCOLS.md](PROTOCOLS.md) antes de proponer un comando y [AGENTS.md](AGENTS.md)
antes de escribir código.

## Privacidad

Sin cuenta, sin servidor, sin analítica, sin SDK de publicidad. Los datos de salud se
quedan en la base de datos del teléfono y las copias de seguridad están excluidas de
forma explícita. Ver [PRIVACY.md](PRIVACY.md).

## Licencia

Apache-2.0. El nombre y el logotipo no se conceden para distribuciones derivadas; ver
[NOTICE](NOTICE).
