# Gene Stacker

Gene Stacker is a flexible tool for marker-assisted gene pyramiding. It can be used
to construct efficient crossing schedules that gather desired alleles residing in
multiple individuals into a single, specific target genotype (the so-called ideotype).

## Running the GUI Version

To run the graphical user interface version of Gene Stacker, you can use one of the following methods:

### Method 1: Using Maven (Recommended)
```bash
mvn javafx:run -pl Genestacker-gui
```

### Method 2: Using the batch script
```bash
run-gui.bat
```

### Method 3: Direct execution (requires JavaFX)
```bash
java -jar bin/genestacker-gui.jar
```

Note: Method 3 requires JavaFX to be installed and configured in your Java environment.

## GUI Features

The Gene Stacker GUI now includes modern styling with:
- Clean, professional light theme (default)
- Dark theme option for reduced eye strain
- Improved visual hierarchy and spacing
- Enhanced form controls with better feedback
- Real-time logging during execution
- Completion notifications with execution time
- Responsive design elements

To switch between themes, you can modify the CSS reference in the GeneStackerGUI.java file or use the theme buttons in the interface.

## Running the Command Line Version

To run the command line version:
```bash
java -jar bin/genestacker.jar -help
```

## Building from Source

To build the project from source:
```bash
mvn package
```

This will create the executable JAR files in the `bin` directory.