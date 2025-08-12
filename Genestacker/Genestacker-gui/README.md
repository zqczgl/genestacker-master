# Gene Stacker GUI

This module provides a graphical user interface for the Gene Stacker application, built with JavaFX.

## Prerequisites

- Java 17 or higher
- Maven

## Building the GUI Module

To build the GUI module, run the following command from the `Genestacker` directory:

```bash
mvn clean package
```

This will create a JAR file in the `target` directory.

## Running the GUI

To run the GUI, use the following command:

```bash
mvn javafx:run -pl Genestacker-gui
```

Or, if you have built the JAR file:

```bash
java -jar Genestacker-gui/target/Genestacker-gui-1.9.jar
```

## Features

The GUI provides a user-friendly interface for:

- Selecting input and output files
- Setting Gene Stacker parameters:
  - Maximum number of generations
  - Overall success rate
  - Maximum linkage phase ambiguity (optional)
  - Population size (optional)
  - Require homozygous ideotype parents (checkbox)
- Running the Gene Stacker algorithm
- Viewing the output in a text area

## Implementation Details

The GUI is built using JavaFX and FXML. The main components are:

- `GeneStackerGUI.java`: The main application class
- `MainController.java`: The controller for the main FXML file
- `main.fxml`: The FXML layout file

The GUI calls the Gene Stacker CLI's `Main` class directly to execute the algorithm, capturing its output and displaying it in the GUI.