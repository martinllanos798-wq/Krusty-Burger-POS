# &#x20;Krusty Burger POS

**Sistema de gestión de ventas e inventario para un restaurante de comida rápida.**

Krusty Burger POS es una aplicación de escritorio desarrollada en Java que permite gestionar las operaciones principales de un restaurante: clientes, empleados, productos, ingredientes, pedidos y ventas.

El proyecto está diseñado para centralizar la gestión del negocio mediante una interfaz gráfica intuitiva y una base de datos MySQL.

---

## &#x20;Características principales

- **Gestión de clientes:** registro y administración de clientes.
- **Gestión de empleados:** administración de los empleados del restaurante.
- **Gestión de productos:** control de los productos disponibles.
- **Gestión de inventario:** administración de ingredientes e insumos.
- **Gestión de pedidos:** creación y seguimiento de pedidos.
- **Sistema de ventas:** procesamiento de ventas y control de stock.
- **Autenticación:** inicio de sesión y almacenamiento seguro de contraseñas mediante BCrypt.
- **Facturación:** generación de documentos PDF.

---

## &#x20;Tecnologías utilizadas

| Tecnología     | Uso                                  |   
| -------------  | ------------------------------------ | 
| Java           | Lenguaje de programación             |  
| MySQL          | Sistema de gestión de bases de datos |  
| JDBC           | Conexión con la base de datos        |  
| Swing          | Interfaz gráfica                     |   
| FlatLaf        | Personalización visual               |   
| Apache PDFBox  | Generación de documentos PDF         |   
| jBCrypt        | Cifrado de contraseñas               |   

---

## &#x20;Estructura del proyecto

```text
krusty-burger-pos/
│
├── lib/                    # Librerías externas
├── database.sql            # Script de creación de la BD
├── src/
│   ├── META-INF/
│   └── krustyburger/
│       ├── Main.java
│       ├── daos/            # Acceso a datos
│       ├── database/        # Conexión con MySQL
│       ├── exceptions/      # Excepciones personalizadas
│       ├── models/          # Modelos de datos
│       ├── services/        # Lógica de negocio
│       └── views/           # Interfaces gráficas
│
└── .gitignore
```

---

## &#x20;Requisitos

Antes de ejecutar el proyecto necesitas:

- Java JDK instalado.
- MySQL Server.
- IntelliJ IDEA u otro IDE compatible con Java.

## &#x20;Instalación y configuración

### 1. Clonar el repositorio

```bash
git clone https://github.com/martinllanos798-wq/Krusty-Burger-POS.git
```

### 2. Crear la base de datos

Abre MySQL Workbench o tu herramienta habitual de MySQL e importa el archivo:

```text
database.sql
```

Este script contiene la estructura y los datos de demostración necesarios para iniciar el proyecto.

### 3. Configurar la conexión a MySQL

En la carpeta raíz del proyecto, crea un archivo llamado `db.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/empresa?useSSL=false&serverTimezone=UTC
db.user=root
db.password=TU_CONTRASEÑA
```

Sustituye `TU_CONTRASEÑA` por la contraseña de tu instalación local de MySQL.

**Importante:** Este archivo contiene credenciales privadas y está excluido del repositorio mediante `.gitignore`. No lo compartas ni lo publiques.

### 4. Configurar las dependencias

Las librerías necesarias se encuentran dentro de la carpeta `lib/`.

Asegúrate de añadirlas al classpath del proyecto desde tu IDE.

### 5. Ejecutar la aplicación

Abre el proyecto en IntelliJ IDEA y ejecuta la clase principal:

```text
src/krustyburger/Main.java
```

Comprueba que la base de datos está iniciada y que el archivo `db.properties` está en la carpeta raíz del proyecto.

---

## &#x20;Seguridad

- Las contraseñas de los usuarios se almacenan mediante BCrypt.
- Las credenciales de conexión a MySQL se mantienen fuera del repositorio.
- El archivo `db.properties` está excluido mediante `.gitignore`.

---

## &#x20;Autor

**Martin Llanos**

Proyecto desarrollado como parte de los estudios de Desarrollo de Aplicaciones Multiplataforma (DAM).

---

## &#x20;Licencia

Proyecto desarrollado con fines educativos y de aprendizaje.

---

&#x20;Si te resulta interesante el proyecto, ¡no dudes en echarle un vistazo!
