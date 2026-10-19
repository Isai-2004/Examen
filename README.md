# Agenda de Contactos — Examen PM01 (Versión A)

Aplicación Android en **Java** para el primer parcial de Programación Móvil I (PMO-0602), UTH.
Arquitectura **MVC + DAO** con base de datos **SQLite**.

## Pantallas
1. **Registro** (`MainActivity`): foto (cámara/selfie), país, nombre, teléfono y nota.
   - Alertas si falta un campo obligatorio.
   - Validación con expresiones regulares: nombre solo letras, teléfono de 8 dígitos.
   - Agregar países desde la app (punto extra).
2. **Lista** (`ListaContactosActivity`): contactos guardados, búsqueda, compartir (`Intent.ACTION_SEND`), ver imagen, eliminar y actualizar.
3. **Llamar** (`LlamarActivity`): confirmación con `AlertDialog.Builder` y llamada con `ACTION_CALL`, solicitando `CALL_PHONE` en tiempo de ejecución.

## Estructura
```
app/src/main/java/com/uth/examen/
├── models/       Contacto, Pais
├── dao/          ContactoDAO, PaisDAO
├── database/     DBConfig, DBHelper (SQLite)
├── controllers/  MainActivity, ListaContactosActivity, LlamarActivity
└── utils/        Utilidades
```
