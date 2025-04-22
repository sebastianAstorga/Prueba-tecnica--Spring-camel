# 🐫 Servicio Spring Boot + Apache Camel

Este proyecto es una aplicación desarrollada con **Spring Boot** y **Apache Camel**, diseñada para:

- Procesar archivos en formato **JSON** o **XML**
- Validar la estructura y contenido de los archivos
- Enviar los datos a un servicio REST externo
- Manejar errores y registrar logs en una carpeta específica

---

## ⚙️ Configuración (`application.properties`)


Estas son las configuraciones utilizadas:
properties
# Puerto donde se ejecuta el servicio
server.port=8086

# Rutas de archivos utilizadas por Camel
#Ruta donde se procesas los archivos JSON/XML
camel.file.input=C:/Users/saastorga/Documents/doc/camel/input
#Ruta donde se dejan los archivos procesados
camel.file.output=C:/Users/saastorga/Documents/doc/camel/outPut
#Ruta donde se dejan los logs del proceso
camel.file.logs=C:/Users/saastorga/Documents/doc/camel/logs
