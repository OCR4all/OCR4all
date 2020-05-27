# OCR4all

[![Build Status](https://travis-ci.org/OCR4all/OCR4all.svg?branch=master)](https://travis-ci.org/OCR4all/OCR4all)

As suggested by the name one of the main goals of OCR4all is to allow basically any given user to independently perform OCR on a wide variety of historical printings and obtain high quality results with reasonable time expenditure. Therefore, **OCR4all is explicitly geared towards users with no technical background. If you are one of those users (or if you just want to use the tool and are not interested in the code), please go to the** [getting started project where you will find guides and test data](https://github.com/OCR4all/getting_started).

Please note that OCR4all main focus is a **semi-automatic workflow** allowing users to perform OCR even on the earliest printed books, which is a very challenging task that often requires a significant amount of **manual interaction**, especially when almost perfect quality is desired.
If you are looking for mass digitization of historical material the [OCR-D project](https://github.com/ocr-d) (in progress) might be worth a look.

This repository contains the code for the main interface and server of the OCR4all project, 
while the repositories [OCR4all/docker_image](https://github.com/OCR4all/docker_image) and [OCR4all/docker_base_image](https://github.com/OCR4all/docker_base_image) are about the creation of a preconfigurated docker image.

For installing the complete project with a docker image, please follow the instructions [here](https://github.com/OCR4all/docker_image).

## Mailing List

OCR4all is under active development and consequently, frequent releases containing bug fixes and further functionality can be expected. In order to always be up to date, we highly recommend subscribing to our [mailing list](https://lists.uni-wuerzburg.de/mailman/listinfo/ocr4all) where we will always announce notable enhancements.

## Current Developments

Plans for the (very) near future:
* Enabling a second project management approach solely based on PageXML allowing for a more flexible workflow.
* Integrating Tesseract for recognition.
* Many minor bug fixes and improvements.

## Built With

* [Docker](https://www.docker.com) - Platform and Software Deployment
* [Maven](https://maven.apache.org/) - Dependency Management
* [Spring](https://spring.io/) - Java Framework
* [Materialize](http://materializecss.com/) - Front-end Framework
* [jQuery](https://jquery.com/) - JavaScript Library

## Included Projects

* [OCRopus](https://github.com/tmbdev/ocropy) - Collection of document analysis programs
* [calamari](https://github.com/ChWick/calamari) - OCR Engine based on OCRopy and Kraken
* [LAREX](https://github.com/chreul/LAREX) - Layout analysis on early printed books
* [Kraken](https://github.com/mittagessen/kraken) - OCR engine for all the languages
* [nashi](https://github.com/andbue/nashi) - Some bits of javascript to transcribe scanned pages using PageXML

## Authors and Helping Hands

* **Christian Reul** (*project lead*) - Email: christian.reul@uni-wuerzburg.de
* **Dennis Christ** and **Alexander Hartelt** (*OCR4all web development*) 
* **Christoph Wick** (*calamari*)
* **Nico Balbach** (*LAREX web GUI*)
* **Andreas Büttner** (*nashi*)
* **Björn Eyselein** (*distribution via Docker*)
* **Maximilan Wehner** (*tireless testing, guides, and non-technical user support*)
* **Christine Grundig, Frank Puppe, and Uwe Springmann** (*ideas and feedback*)
* ...
