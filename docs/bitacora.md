# Bitácora de Práctica 2: Sabores

## Ejercicio 0: Clasificación por Capas

| Responsabilidad | ¿Qué capa? | Justificación |
| :--- | :--- | :--- |
| 1. Calcular el promedio de estrellas de un restaurante | Dominio | Es una regla aritmética del negocio; no cambia si mañana la app es web. |
| 2. Mostrar 4.333… como "4.3" | UI | Formato de presentación. El dominio produce el número; la UI decide los decimales. |
| 3. La lista de los restaurantes de muestra | Datos | Es la fuente. Hoy en memoria; el día que sea una base de datos, nadie arriba se entera. |
| 4. “El comentario debe tener al menos 15 caracteres” | Dominio | Regla. Va en ReviewValidator. |
| 5. El botón “Publicar” se pinta gris | UI | Es derivado de canSave. Quién decide si es válido es el dominio; quién lo pinta gris es la UI. |
| 6. Recordar cuántas estrellas lleva marcadas el usuario | ViewModel | Prueba de la rotación: debe sobrevivir. |
| 7. Decidir a qué pantalla ir después de guardar | UI | El ViewModel jamás conoce las pantallas. Expone estado y recibe eventos; el NavHost decide rutas. |
| 8. Convertir priceLevel = 2 en "$$" | Dominio | Etiqueta derivada del modelo y no necesita Android para existir (defendible también como UI). |

---

## Ejercicio B2: Romper la navegación a propósito

1. **Cambiar `type = NavType.IntType` por `StringType` sin tocar `getInt`:**
   - **Qué ocurre:** Llega como 0 o lanza un error de tipo (`ClassCastException`/`IllegalArgumentException`) al intentar obtener un entero mediante `getInt` sobre un argumento declarado como `StringType`. El tipo declarado en `navArgument` debe coincidir con el getter utilizado en el destino.

2. **Tocar la misma tarjeta 5 veces rápido y luego “atrás” 5 veces:**
   - **Qué ocurre:** En el back stack se apilan 5 copias consecutivas de la misma pantalla de detalle, por lo que es necesario presionar "atrás" 5 veces para volver a la pantalla principal.
   - **Solución:** Configurar `launchSingleTop = true` en la llamada a `navigate` para evitar apilar múltiples instancias del mismo destino en la cima del back stack.

---

## Ejercicio B4: Diagnóstico de ciclo de vida y alcance

1. **¿Cuál de las tres pruebas de la Parte 0 detecta este error?**
   - La prueba de navegación y rotación de pantalla (recreación de la composición). Al salir del Composable o rotar el dispositivo, todo lo guardado con `remember` dentro de la pantalla se destruye y se pierde.

2. **¿A quién le pertenecen las reseñas: al detalle, a la lista, o a ninguno de los dos? Justifica con lo que pasa cuando el promedio debe verse también en la tarjeta de la lista.**
   - A ninguno de los dos; pertenecen a un nivel superior, al ViewModel compartido (`SaboresViewModel`). Si pertenecieran únicamente a la pantalla de detalle, la lista no podría enterarse de los cambios ni actualizar el promedio de estrellas en las tarjetas.

3. **Si movieras las reseñas a un remember en `SaboresApp()` (el Composable que contiene al `NavHost`), ¿se arregla el Experimento 1? ¿Y el 2?**
   - El Experimento 1 sí se arregla, porque `SaboresApp` no sale de la composición durante la navegación entre destinos y preserva el estado en memoria.
   - El Experimento 2 no se arregla, ya que girar el teléfono destruye y recrea la `Activity` por completo, reiniciando todo el árbol de composición y perdiendo cualquier valor almacenado en un `remember`. Para sobrevivir a la destrucción y recreación de la Activity se requiere un `ViewModel`.
