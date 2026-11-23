# InventarioApp - CRUD Android con Jetpack Compose, Room y Firebase

Proyecto formativo desarrollado siguiendo la **Guía Técnica y Didáctica: CRUD Android profesional con Jetpack Compose, Firebase y Room**.

La aplicación implementa una arquitectura **MVVM limpia (Clean Architecture orientada a características)**, persistencia local reactiva con **Room**, sincronización remota con **Firebase Firestore** y autenticación anónima con **Firebase Auth**, usando **Navigation 3** para la navegación entre pantallas.

---

## 🏗️ Arquitectura y Estructura del Proyecto

El código está estructurado separando responsabilidades por capas y funcionalidades:

```
app/src/main/java/com/sena/inventarioapp/
├── InventoryApplication.kt              // Configuración de Hilt (@HiltAndroidApp)
├── MainActivity.kt                      // Entrada principal con @AndroidEntryPoint y Navigation 3
├── core/
│   ├── database/AppDatabase.kt          // Definición de base de datos Room
│   ├── di/
│   │   ├── DatabaseModule.kt            // Proveedor de AppDatabase y ProductDao
│   │   ├── FirebaseModule.kt            // Proveedor de FirebaseAuth y FirebaseFirestore
│   │   └── RepositoryModule.kt          // Enlace del contrato ProductRepository con su implementación
│   └── navigation/AppNavigation.kt      // Rutas tipadas NavKey y NavDisplay con Navigation 3
└── feature/product/
    ├── domain/
    │   ├── model/Product.kt             // Modelo de dominio independiente de frameworks
    │   ├── repository/ProductRepository.kt // Contrato del repositorio y SyncReport
    │   └── usecase/SaveProductUseCase.kt// Reglas de negocio y validaciones
    ├── data/
    │   ├── local/
    │   │   ├── ProductEntity.kt         // Entidad SQLite con marcas pending e isDeleted
    │   │   └── ProductDao.kt            // DAO reactivo con Flow y @Upsert
    │   ├── remote/
    │   │   ├── ProductDto.kt            // Representación del documento Firestore
    │   │   └── FirestoreProductDataSource.kt // Acceso a la colección users/{uid}/products
    │   ├── mapper/
    │   │   ├── ProductMapper.kt         // Conversión Entity <-> Dominio
    │   │   └── ProductRemoteMapper.kt   // Conversión DTO <-> Entity
    │   └── repository/ProductRepositoryImpl.kt // Coordinador de Room, Firebase y syncNow()
    └── presentation/
        ├── state/
        │   ├── ProductListUiState.kt    // Estado de la lista (productos, carga, avisos)
        │   └── ProductFormUiState.kt    // Estado inmutable del formulario
        ├── composables/
        │   ├── ProductCard.kt           // Tarjeta visual reutilizable (formato moneda es-CO)
        │   ├── ProductFormField.kt      // Campo de texto desacoplado
        │   └── ProductDeleteDialog.kt   // Diálogo modal de confirmación
        ├── section/
        │   ├── ProductListSection.kt    // Sección LazyColumn
        │   └── ProductFormSection.kt    // Sección estructurada del formulario con scroll
        ├── screen/
        │   ├── ProductListScreen.kt     // Pantalla contenedora de lista
        │   └── ProductFormScreen.kt     // Pantalla contenedora de edición/creación
        └── viewmodel/
            ├── ProductListViewModel.kt  // Estado de lista y disparador de sincronización
            └── ProductFormViewModel.kt  // Lógica de formulario y llamada al caso de uso
```

---

## 🚀 Flujo de Datos y Sincronización

1. **Modo Offline Primero (Offline-First)**:
   * Al crear, editar o eliminar un producto, el cambio se persiste inmediatamente en **Room** con la marca `pending = true`.
   * La interfaz de usuario observa los cambios de forma reactiva a través de `Flow` expuesto por el DAO y transformado en `StateFlow` por el ViewModel.
2. **Eliminación Lógica (Tombstone)**:
   * La acción de eliminar no destruye el registro físico local antes de sincronizar; actualiza `isDeleted = true` y `pending = true`.
3. **Sincronización Explícita (`syncNow`)**:
   * Al pulsar el botón **Sincronizar**:
     1. Se garantiza la sesión de usuario mediante autenticación anónima (`auth.signInAnonymously()`).
     2. Se leen los elementos pendientes locales y se suben a Firestore (`users/{uid}/products/{productId}`).
     3. Al subirse con éxito, se marcan como sincronizados (`pending = 0`).
     4. Se descargan los cambios del servidor (`fetchFromServer`) y se actualiza Room siempre que no haya ediciones locales pendientes más recientes.
   * Todo el proceso está protegido con un `Mutex` para evitar condiciones de carrera.

---

## 🔒 Reglas de Seguridad (`firestore.rules`)

Se definieron reglas de seguridad en Firestore para asegurar que:
* Cada usuario solo puede leer y escribir sus propios productos (`request.auth.uid == uid`).
* Los datos enviados cumplan estrictamente con los tipos y longitudes esperadas (nombre <= 80 chars, descripción <= 500 chars, precio >= 0, cantidad >= 0).

---

## 🧪 Matriz de Pruebas Funcionales (Capítulo 12)

| Caso | Acción | Resultado Esperado | Estado |
|---|---|---|---|
| **Crear sin Internet** | Guardar un nuevo producto en modo avión. | El producto aparece de inmediato en la lista a través de Room. | ✅ Verificado |
| **Persistencia** | Cerrar y volver a abrir la aplicación. | Los productos almacenados en SQLite se conservan intactos. | ✅ Verificado |
| **Editar** | Cambiar cantidad y precio de un producto. | La tarjeta en pantalla se actualiza en tiempo real. | ✅ Verificado |
| **Eliminar** | Confirmar eliminación en el diálogo modal. | El producto desaparece de la UI visual pero mantiene su tombstone para sincronizar. | ✅ Verificado |
| **Cancelar borrado** | Cerrar el diálogo de confirmación. | El producto se mantiene en la lista sin alteraciones. | ✅ Verificado |
| **Sincronización** | Reconectar a Internet y pulsar Sincronizar. | Los documentos se suben y reflejan en Firestore Console. | ✅ Verificado |
| **Validaciones** | Intentar guardar campos vacíos o precios negativos. | El formulario muestra el mensaje de error y bloquea el guardado. | ✅ Verificado |

---

## 📋 Checklist de la Práctica (Anexo A)

- [x] Configuración inicial y compilación con Compose y Material 3.
- [x] Configuración de dependencias: Room, KSP, Dagger Hilt y Navigation 3.
- [x] Creación del modelo de dominio `Product` y contrato `ProductRepository`.
- [x] Implementación del caso de uso `SaveProductUseCase` con validaciones de negocio.
- [x] Creación de `ProductEntity`, `ProductDao` y `AppDatabase`.
- [x] Módulos de inyección de dependencias con Hilt (`DatabaseModule`, `FirebaseModule`, `RepositoryModule`).
- [x] Construcción de pantallas en 3 niveles (Composables, Sections, Screens).
- [x] Gestión de estado con `UiState` y `StateFlow`.
- [x] Navegación tipada con Navigation 3 (`NavKey`, `NavDisplay`, `rememberNavBackStack`).
- [x] Operación CRUD local offline funcional.
- [x] Mapeadores desacoplados de dominio, entidad y DTO.
- [x] Conexión remota con Firestore y Auth anónimo.
- [x] Publicación y validación de reglas de seguridad (`firestore.rules`).
- [x] Auditoría de compilación y verificación de responsabilidades limpias.
