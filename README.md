
# OCR4all_Web

Provides OCR (optical character recognition) services through web applications

## Getting Started

These instructions will get you a [Docker container](https://www.docker.com/what-container) that runs the project

### Prerequisites

[Docker](https://www.docker.com)
For installation instructions see the [Official Installation Guide](https://docs.docker.com/install/)

### Installing

Download the [Dockerfile](Dockerfile) first and enter the directory that contains it with a command line tool.

The Docker image can be built with the following command:
```
docker build -t <IMAGE_NAME> .
```

With the help of the image a container can now be created with the following command:
```
docker run -p 8080:8080 \
    -v <OCR_DATA_DIR>:/var/ocr4all/data \
    -v <OCR_MODEL_DIR>:/var/ocr4all/models \
    -it <IMAGE_NAME>
```

Explanation of variables used above:
* `<IMAGE_NAME>` - Name of the Docker image
* `<OCR_DATA_DIR>` - Directory in which the OCR data is located on your local machine 
* `<OCR_MODEL_DIR>` - Directory in which the OCR models are located on your local machine 

The container will be started by default after executing the `docker run` command. If you want to start the container again later use `docker ps -a` to list all available containers with their Container IDs and then use `docker start <CONTAINER_ID>` to start the desired container.

You can now access the project via following URL: http://localhost:8080/OCR4all_Web/

### Updating
To update the source code of the project you currently need to reinstall the image.

This can be achieved with executing the following command first:
```
docker image rm <IMAGE_NAME>
```
Afterwards you can follow the installation guide above as it is a new clean installation.

## Built With

* [Docker](https://www.docker.com) - Platform and Software Deployment
* [Maven](https://maven.apache.org/) - Dependency Management
* [Spring](https://spring.io/) - Java Framework
* [Materialize](http://materializecss.com/) - Front-end Framework

## Authors

* **Christian Reul** - *Responsible for OCR processes* - Email: christian.reul@uni-wuerzburg.de
* **Dennis Christ** - *Developer* - Email: dennis.christ@stud-mail.uni-wuerzburg.de
* **Alexander Hartelt** - *Developer* - Email: alexander.hartelt@stud-mail.uni-wuerzburg.de