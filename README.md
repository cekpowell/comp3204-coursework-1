# OpenIMAJ Tutorial Walkthrough
## COMP3204: Computer Vision
---

## Contents

- **[Introduction](#introduction)**
  * **[Background](#background)**
  * **[Project Description](#project-description)**
- **[Usage](#usage)**

---

## Introduction

### Background

- OpenIMAJ is an [award-winning](http://www.acmmm11.org/content-awards-recognitions.html) set of libraries and tools for multimedia content analysis and content generation. 
- OpenIMAJ is very broad and contains everything from state-of-the-art computer vision (e.g. SIFT descriptors, salient region detection, face detection, etc.) and advanced data clustering, through to software that performs analysis on the content, layout and structure of webpages.

### Task Description

- Go through and complete the OpenIMAJ tutorial, making notes on the chapter contents and completing the tutorial exercises.
- This project contains a commented-walkthrough of each chapter's tutorial as well as the solutions to the exercises.

> <u>*Note:*</u> *Chapters 8,9,10 and 11 are not contained within the project as they were not required for the coursework.*

---
## Usage

- The project is developed with `Maven` and includes a `pom.xml` for executing the programs.

- Each chapter of the OpenIMAJ tutorial is contained within it's own package within the codebase - e.g., `ch1`

- Each chapter's package is structured as follows:

  - ```
    ch<chapter-number>/
    	|- Chapter<chapter-number>Tutorial.java
    	|- Chapter<chapter-number>Exercises.java
    ```

- Where:

  - `Chapter<chapter-number>Tutorial.java` : A commented walkthrough of the chapter.
  - `Chapter<chapter-number>Exercises.java` : Solutions to the chapter's exercises. 


- To run a chapter's tutorial/exercises, simply execute the file within the IDE in use.

> <u>*Note:*</u> *Some tutorials/exercises require certain pieces of code to be commented/uncommented for all of the code to be tested.*

---
