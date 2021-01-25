# Preliminary HERMESS SPU interface software
This software is able to connect to the [HERMESS](https://www.project-hermess.com/) Signal
Processing Unit and allows for superficial, visual analysis of measurements.

Keywords: _HERMESS_, _REXUS_, _REXUS/BEXUS_, _Kotlin_, _TornadoFx_, _MVVM_, _.herpro_,
_.herconf_, _.hermeas_


## Features
### Basic features
- [X] Organize in project files
- [ ] Store configuration files
- [ ] Visualize measurements data as line plot
- [ ] Visualize measurements data as center of force graphic for a specific time

### DAPI Features
Supports data-and-programming-interface (DAPI) protocol 0.0.1
- [ ] Connect via DAPI
- [ ] Configure SPU
- [ ] Read out and store measurements
- [ ] Read out and store metadata

### TM Features
Supports telemetry (TM) protocol 0.0.1
- [ ] Connect via TM
- [ ] Read and record status data
- [ ] Read and record measurements
- [ ] Life preview of measurements and status


## Usage
### Compilation and packaging
This software uses maven for compilation and packaging:  
`mvn clean package`

### Execution
Use the jre of [OpenJDK 14](https://openjdk.java.net/):  
`java -jar target/hermessGui-1.0-jar-with-dependencies.jar`


## Credits
- [Jerry Low](https://www.iconfinder.com/jerrylow) for the rocket-icon of this application
- [Wishforge Games](https://www.iconfinder.com/bitfreak86) for most of the icons used in this application