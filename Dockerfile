# Imagen base con Java 17
FROM eclipse-temurin:17-jdk-alpine

  # Carpeta de trabajo dentro del contenedor
WORKDIR /app

  # Copiamos el JAR generado por Maven (ajusta el nombre)
COPY target/*.jar app.jar

  # Comando de arranque
ENTRYPOINT ["java","-jar","/app/app.jar"]