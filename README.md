# OCR4all

[![Build Status](https://travis-ci.org/OCR4all/OCR4all.svg?branch=master)](https://travis-ci.org/OCR4all/OCR4all)

As suggested by the name one of the main goals of OCR4all is to allow basically any given user to independently perform OCR on a wide variety of historical printings and obtain high quality results with reasonable time expenditure. Therefore, **OCR4all is explicitly geared towards users with no technical background. If you are one of those users (or if you just want to use the tool and are not interested in the code), please go to the** [getting started project where you will find guides and test data](https://github.com/OCR4all/getting_started).

Please note that OCR4all main focus is a **semi-automatic workflow** allowing users to perform OCR even on the earliest printed books (for example cf. the [*Narragonien digital* project](http://kallimachos.de/kallimachos/index.php/Narragonien#.22) which kicked off its development), which is a very challenging task that often requires a significant amount of **manual interaction**, especially when almost perfect quality is desired. If you are looking for fully automatic mass digitization of historical material please check out the [OCR-D project](https://github.com/ocr-d).

This repository contains the code for the main interface and server of the OCR4all project, while the repositories [OCR4all/docker_image](https://github.com/OCR4all/docker_image) and [OCR4all/docker_base_image](https://github.com/OCR4all/docker_base_image) are about the creation of a preconfigurated docker image.

If you run into any problems or have any questions, please don't hesitate to open an issue or [contact us](#project-members) directly. When you use OCR4all we would appreciate a citation of the corresponding [paper](#citation).
In addition, we are very interested to learn about different projects/institutions/... OCR4all is utilized in and would welcome a short email with some information about the type of use, the material at hand, and the suitability of OCR4all for the given task.
Finally, consider visiting the OCR4all [homepage](https://www.uni-wuerzburg.de/en/zpd/ocr4all/) and subscribing to the [mailing list](#mailing-list).


## Current Developments

Plans for the near future:
* **Ensure full compatibility with the developments achieved during the [OCR-D project](https://github.com/ocr-d) (adjusting interfaces, integration of the various proposed tools and making them available to the user in a straight-forward manner, ...)**,
* Upgrade to a fullfeatured server application including resource management and user administration,
* Continuous user-driven improvement (many minor bug fixes, refinements, ...),
* Introducing a second way of distribution using Virtual Box.


## Included Projects

* [OCRopus](https://github.com/tmbdev/ocropy) - Collection of document analysis programs
* [Calamari](https://github.com/ChWick/calamari) - OCR Engine based on OCRopy and Kraken
* [LAREX](https://github.com/chreul/LAREX) - Layout analysis on early printed books

Additional projects used in earlier versions:
* [Kraken](https://github.com/mittagessen/kraken) - OCR engine for all the languages
* [nashi](https://github.com/andbue/nashi) - Some bits of javascript to transcribe scanned pages using PageXML


## Project Members

* **Christian Reul** (*project lead*) - Email: christian.reul@uni-wuerzburg.de
* **Maximilan Wehner** (*consultation, guides, and non-technical user support*) - Email: maximilian.wehner@uni-wuerzburg.de 
* **Dr. Herbert Baier-Saip** (*lead developer*)
* **Kevin Chadbourne** (*developer*)
* **Yannik Herbst** (*developer*)
* **Maximilian Nöth** (*developer*)
* **Christoph Wick** (*Calamari*)
* **Björn Eyselein** (*distribution via Docker*)
* **Jonathan Gaede** (*OCR4all Wiki*)
* **Raphaelle Jung** (guides and artwork)

Former:
* **Dennis Christ** and **Alexander Hartelt** (*OCR4all web app*) 
* **Nico Balbach** (*OCR4all web app and LAREX web GUI*)
* **Andreas Büttner** (*nashi*)
* **Dr. Uwe Springmann** and **Christine Grundig** (*ideas and feedback*)


## Funding

Developed during the BMBF project **"Kallimachos"** at the **Chair of Artificial Intelligence and Applied Computer Science (Prof. Dr. Frank Puppe)** in collaboration with the **Center for Philology and Digitality "Kallimachos"** at the University of Würzburg.


## Mailing List

OCR4all is under active development and consequently, frequent releases containing bug fixes and further functionality can be expected. In order to always be up to date, we highly recommend subscribing to our [mailing list](https://lists.uni-wuerzburg.de/mailman/listinfo/ocr4all) where we will always announce notable enhancements.


## Citation

**OCR4all - An Open-Source Tool Providing a (Semi-)Automatic OCR Workflow for Historical Printings.** Reul, Christian; Christ, Dennis; Hartelt, Alexander; Balbach, Nico; Wehner, Maximilian; Springmann, Uwe; Wick, Christoph; Grundig, Christine; Büttner, Andreas; Puppe, Frank in *Applied Sciences* (2019). 9(22). [Read Online](https://www.mdpi.com/2076-3417/9/22/4853/htm).
