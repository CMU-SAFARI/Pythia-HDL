# Implementation of Pythia in Chisel

This project implements Pythia in Chisel.

## Setup

To setup Chisel3 in local machine, please follow the [setup instructions](https://github.com/chipsalliance/chisel3/blob/master/SETUP.md)

## Run

To run/test a module, use the following command:

```
sbt -v "test:runMain pythia.Launcher <Module name>"
sbt -v "test:runMain pythia.Launcher MasterModule"
```

The supported modules are:
| Module Name | Description |
|------------------|----------------|
|`IndexGen` | Generates a plane index from PC+Offset or Delta path feature. Implements [Robert Jenkin's 32-bit hash function](http://www.burtleburtle.net/bob/hash/doobs.html).|
|`Plane` | Implements a plane construct. |
|`Vault`| Implements a vault construct. Essentially a group of 3 planes. |
|`MaxN`| Given 3 sets of <id,value> pairs, this module returns the max value and the id with max value. |
|`QVCompare`| Sums up three partial Q-values (each read from individual planes) and returns the max of two overall Q-values. |
|`MasterModule`| The high-level module that implements Pythia in its entirity. |

## Contact
For any information, please contact [me](mailto:write2bera@gmail.com).
