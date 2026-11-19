# PROMPTS.md · Fase 2 (rama `mejora-ia`)

Mejoras de CampusGPS hechas con ayuda de **Gemini en Android Studio** (modelo gemini-3.5-flash-lite), sin romper MVVM + Repository.
La IA propone; yo reviso, pruebo y corrijo lo que no respeta la arquitectura.

| # | Prompt que escribí | Qué respondió Gemini | Qué cambié o descarté | Por qué |
|---|---|---|---|---|
| 1 | «Tengo una app Android en Kotlin con Jetpack Compose y MVVM. MapaViewModel expone lugares: StateFlow<List<LugarConDistancia>> (cada Lugar tiene una categoria: Estudio, Comida, Eventos o Servicios). Quiero agregar chips de filtro por categoría que muestren solo esos lugares en el mapa (MapaUbicacion) y en la lista (ListaLugares). El filtro debe estar en el ViewModel, no en el Composable. ¿Cómo lo hago?» | Puso el filtro en el ViewModel con `combine` y una función `filtrarPorCategoria(...)`; los chips en la pantalla llaman a esa función y `MapaUbicacion` y `ListaLugares` leen la lista ya filtrada. Además cambió la altura de la lista a 200.dp. | Usé `combine` y `filtrarPorCategoria`, pero **no filtré `lugares`**: creé un `StateFlow` aparte, `lugaresVisibles`. Dejé la lista en 220.dp. | Si se filtra `lugares`, `masCercano` (que se deriva de `lugares`) cambiaría con el filtro y el botón *Ir al más cercano* dejaría de llevar al lugar más cercano del campus. El cambio de altura no tenía relación con la mejora. |
