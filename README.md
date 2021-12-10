# Algoritmo Evolutivo Paralelo para la Asignación de Rutas de un Servicio de Recolección de Residuos

## Instalación

Se utiliza la herramienta de gestión de proyectos Apache Maven para la Instalación de dependencias y compilación del proyecto. Para instalar el proyecto basta con ejecutar
el script [compile.sh](compile.sh) el cual generará un .jar portable en el directorio de trabajo.

## Uso

Se encuentra un ejemplo para ejecutarlo en el archivo [exe.sh](exe.sh). Los argumentos que el archivo opcionalmente espera son:

    Cantidad máxima de evaluaciones | Núcleos de CPU | Modo

Tipo de ejecución se utiliza para ejecutar el algoritmo ávido (Modo=greedy) o las pruebas experimentales (Modo=exp). Si no se especifica, se ejecuta el algoritmo evolutivo normal. 

**Importante:** El programa espera en el directorio de ejecución una carpeta llamada instance que contenga los archivos .csv que definen la instancia del problema. Puede ver ejemplos de las instancias [aquí](instancias/).

## Información 
Repositorio del proyecto final del curso de Algoritmos Evolutivos 2021 - Facultad de Ingeniería - UDELAR
Autores: Guillermo Toyos y Federico Vallcorba

