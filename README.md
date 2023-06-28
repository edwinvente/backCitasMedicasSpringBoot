# Proyecto Base

Este es un proyecto base de Spring Boot que te proporciona una estructura inicial para desarrollar aplicaciones web. A continuación, se describen los pasos necesarios para ejecutar el proyecto en tu entorno local.

## Cómo correr el proyecto en local:

1. **Crear el build de la aplicación:**

    - Haz click en el icono de Gradle en tu entorno de desarrollo integrado (IDE).
    - Busca la carpeta "build" en el proyecto y haz doble click en el ejecutable de build.

2. **Crear el build de la imagen de Docker:**

   Ejecuta el siguiente comando en tu terminal:
   ```
   docker-compose build --no-cache
   ```

3. **Levantar el docker-compose con las dependencias necesarias:**

   Ejecuta el siguiente comando en tu terminal:
   ```
   docker-compose up -d
   ```

   Nota: Si encuentras algún error al intentar levantar el docker-compose con el comando `docker-compose up -d`, puedes intentar solucionarlo utilizando el siguiente comando:
   ```
   rm ~/.docker/config.json
   ```

4. **Configuración de la base de datos:**

   En la ruta `./docker/provision/mysql/init/database.sql`, encontrarás el archivo de conexión a la base de datos y podrás modificar la configuración inicial de la misma.
