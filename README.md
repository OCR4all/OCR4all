# OCR4all

[![Build Status](https://travis-ci.org/OCR4all/OCR4all.svg?branch=master)](https://travis-ci.org/OCR4all/OCR4all)

As suggested by the name one of the main goals of OCR4all is to allow basically any given user to independently perform OCR on a wide variety of historical printings and obtain high quality results with reasonable time expenditure. Therefore, **OCR4all is explicitly geared towards users with no technical background. If you are one of those users (or if you just want to use the tool and are not interested in the code), please go to the** [getting started project where you will find guides and test data](https://github.com/OCR4all/getting_started).

Please note that OCR4all current main focus is a **semi-automatic workflow** allowing users to perform OCR even on the earliest printed books, which is a very challenging task that often requires a significant amount of **manual interaction**, especially when almost perfect quality is desired.
Nevertheless, we are working towards increasing robustness and the degree of automation of the tool.
An important cornerstone for this is the recently agreed cooperation with the [OCR-D project](https://github.com/ocr-d) which focuses on the mass full-text recognition of historical materials.

This repository contains the code for the main interface and server of the OCR4all project, 
while the repositories [OCR4all/docker_image](https://github.com/OCR4all/docker_image) and [OCR4all/docker_base_image](https://github.com/OCR4all/docker_base_image) are about the creation of a preconfigurated docker image.

For installing the complete project with a docker image, please follow the instructions [here](https://github.com/OCR4all/docker_image).

## Mailing List

OCR4all is under active development and consequently, frequent releases containing bug fixes and further functionality can be expected. In order to always be up to date, we highly recommend subscribing to our [mailing list](https://lists.uni-wuerzburg.de/mailman/listinfo/ocr4all) where we will always announce notable enhancements.

## Built With

* [Docker](https://www.docker.com) - Platform and Software Deployment
* [Maven](https://maven.apache.org/) - Dependency Management
* [Spring](https://spring.io/) - Java Framework
* [Materialize](http://materializecss.com/) - Front-end Framework
* [jQuery](https://jquery.com/) - JavaScript Library

## Included Projects

* [OCRopus](https://github.com/tmbdev/ocropy) - Collection of document analysis programs
* [Calamari](https://github.com/ChWick/calamari) - OCR Engine based on OCRopy and Kraken
* [LAREX](https://github.com/chreul/LAREX) - Layout analysis on early printed books

### Formerly included / inspired by

* [Kraken](https://github.com/mittagessen/kraken) - OCR engine for all the languages
* [nashi](https://github.com/andbue/nashi) - Some bits of javascript to transcribe scanned pages using PageXML


## Contact, Authors, and Helping Hands

* **Dr. Christian Reul** (*project lead*) - mail: christian.reul@uni-wuerzburg.de
* **Florian Langhanki** (*user support and guides*) - mail: florian.langhanki@uni-wuerzburg.de

### Developers

* **Dr. Herbert Baier Saip** (*lead*)
* **Maximilian Nöth** (*OCR4all, LAREX, and Calamari*)
* **Dr. Christoph Wick** (*Calamari*)
* **Andreas Büttner** (*Calamari and nashi*)
* **Kevin Chadbourne** (*LAREX*)
* **Yannik Herbst** (*distribution via VirtualBox*)
* **Björn Eyselein** (*Artifactory and distribution via Docker*)

### Miscellaneous

* **Raphaëlle Jung** (*guides and artwork*)
* **Dr. Uwe Springmann** (*ideas and feedback*)
* **Prof. Dr. Frank Puppe** (*ideas and feedback*)

### Former Project Members

* **Dennis Christ** (*OCR4all*)
* **Alexander Hartelt** (*OCR4all*)
* **Nico Balbach** (*OCR4all and LAREX*)
* **Christine Grundig** (*ideas and feedback*)
* **Maximilan Wehner** (*user support and guides*)
* ...


## Funding
* [DFG-funded Initiative "OCR-D" (Phase 2 and 3)](https://ocr-d.de/en/)
* [BMBF Project “Kallimachos“](http://www.kallimachos.de/)
* [Centre for Philology and Digitality (University of Würzburg)](https://www.uni-wuerzburg.de/zpd)
* [Chair of Artificial Intelligence (University of Würzburg)](https://www.informatik.uni-wuerzburg.de/en/is)

## Citing OCR4all

If you are using OCR4all please cite:

> Reul, C., Christ, D., Hartelt, A., Balbach, N., Wehner, M., Springmann, U., Wick, C., Grundig, Büttner, A., C., Puppe, F.: *OCR4all — An open-source tool providing a (semi-) automatic OCR workflow for historical printings* Applied Sciences **9**(22) (2019)

```
@article{reul2019ocr4all,
  title={OCR4all—An open-source tool providing a (semi-) automatic OCR workflow for historical printings},
  author={Reul, Christian and Christ, Dennis and Hartelt, Alexander and Balbach, Nico and Wehner, Maximilian and Springmann, Uwe and Wick, Christoph and Grundig, Christine and B{\"u}ttner, Andreas and Puppe, Frank},
  journal={Applied Sciences},
  volume={9},
  number={22},
  pages={4853},
  year={2019},
  publisher={Multidisciplinary Digital Publishing Institute}
}
```