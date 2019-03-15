# OCR4all_Web

[![Build Status](https://travis-ci.org/OCR4all/OCR4all.svg?branch=master)](https://travis-ci.org/OCR4all/OCR4all)

As suggested by the name one of the main goals of OCR4all is to allow basically any given user to independently perform OCR on a wide variety of historical printings and obtain high quality results with reasonable time expenditure. Therefore, OCR4all is explicitly planned even for users with no technical background.

If you are one of those users (or if you just want to use the tool and are not interested in the code), please go to the [getting started section](https://github.com/OCR4all/getting_started).


## Getting Started

These instructions will get you a [Docker container](https://www.docker.com/what-container) that runs the project

### Prerequisites

[Docker](https://www.docker.com) (for installation instructions see the [Official Installation Guide](https://docs.docker.com/install/))

### Installing

Download the [Dockerfile](Dockerfile) first and enter the directory that contains it with a command line tool.

The Docker image can be built with the following command:
```
docker build -t <IMAGE_NAME> .
```

With the help of the image a container can now be created with the following command:
```
docker run \
    -p 5000:5000 \
    -p 8080:8080 \
    -u `id -u root`:`id -g $USER` \
    --name ocr4all \
    -v <OCR_DATA_DIR>:/var/ocr4all/data \
    -v <OCR_MODEL_DIR>:/var/ocr4all/models/custom \
    -it <IMAGE_NAME>
```

Explanation of variables used above:
* `<IMAGE_NAME>` - Name of the Docker image
* `<OCR_DATA_DIR>` - Directory in which the OCR data is located on your local machine
* `<OCR_MODEL_DIR>` - Directory in which the OCR models are located on your local machine

The container will be started by default after executing the `docker run` command.

If you want to start the container again later use `docker ps -a` to list all available containers with their Container IDs and then use `docker start -ia ocr4all` to start the desired container.

You can now access the project via following URL: http://localhost:8080/OCR4all_Web/

### Updating

To update the source code of the project you currently need to reinstall the image.

This can be achieved with executing the following command first:
```
docker image rm <IMAGE_NAME>
```
Afterwards you can follow the installation guide above as it is a new clean installation.

## Development

In case you want shell access on your Docker container for development or testing purposes the container needs to be created with the following command (including the `--entrypoint` option):
```
docker run \
    -p 5000:5000 \
    -p 8080:8080 \
    --entrypoint /bin/bash \
    -v <OCR_DATA_DIR>:/var/ocr4all/data \
    -v <OCR_MODEL_DIR>:/var/ocr4all/models/custom \
    -it <IMAGE_NAME>
```

The container will be started by default after executing the `docker run` command.

If you want to start the container again later use `docker ps -a` to list all available containers with their Container IDs and then use `docker start <CONTAINER_ID>` to start the desired container. To gain shell access again use `docker attach <CONTAINER_ID>`.

Because the entrypoint has changed, processes will not start automatically and the following command needs to be executed after the container startup:
```
/usr/bin/supervisord
```

For information on how to update the project take a look into the commands within the [Dockerfile](Dockerfile).

### Updated Dockerfile

The Dockerfile in branch `update-dockerfile` does not build OCR4all_Web, LAREX and GTC-Web anymore but downloads them 
from the [artifactory of LS 6 of Uni Würzburg](http://artifactory-ls6.informatik.uni-wuerzburg.de/artifactory/webapp/#/home).
To update them, you have to publish a new version to the artifactory and update the download link in the Dockerfile.

**Caution:** Larex needs to be (mvn) packaged with the option `directrequest` in `larex.config` enabled (uncommented).

## Built With

* [Docker](https://www.docker.com) - Platform and Software Deployment
* [Maven](https://maven.apache.org/) - Dependency Management
* [Spring](https://spring.io/) - Java Framework
* [Materialize](http://materializecss.com/) - Front-end Framework
* [jQuery](https://jquery.com/) - JavaScript Library

## Included Projects

* [LAREX](https://github.com/chreul/LAREX) - Layout analysis on early printed books
* [OCRopus](https://github.com/tmbdev/ocropy) - Collection of document analysis programs
* [calamari](https://github.com/ChWick/calamari) - OCR Engine based on OCRopy and Kraken
* [nashi](https://github.com/andbue/nashi) - Some bits of javascript to transcribe scanned pages using PageXML

## Authors and Helping Hands

* **Christian Reul** (*Project Lead*) - Email: christian.reul@uni-wuerzburg.de
* **Dennis Christ** and **Alexander Hartelt** (*OCR4all Web Development*) 
* **Christoph Wick** (Calamari)
* **Nico Balbach** (LAREX Web GUI)
* **Andreas Büttner** (nashi)
* **Björn Eyeselein** (distribution via Docker)
* **Maximilan Wehner** (tireless testing, guides, and non-technical user support)
* **Christine Grundig, Frank Puppe, and Uwe Springmann** (Ideas and Feedback)
* ...
