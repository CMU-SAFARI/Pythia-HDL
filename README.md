<p align="center">
  <a href="https://github.com/rahulbera/Pythia-HDL">
    <img src="logo.png" alt="Logo" width="354" height="100">
  </a>
  <h3 align="center">A Customizable Hardware Prefetching Framework Using Online Reinforcement Learning
  </h3>
  <h2 align="center">Chisel Implementation</h2>
</p>

<p align="center">
    <a href="https://github.com/rahulbera/Pythia-HDL/blob/master/LICENSE">
        <img alt="GitHub" src="https://img.shields.io/badge/License-MIT-yellow.svg">
    </a>
    <a href="https://github.com/rahulbera/Pythia-HDL/releases">
        <img alt="GitHub release" src="https://img.shields.io/github/release/rahulbera/Pythia-HDL">
    </a>
    <a href="https://github.com/rahulbera/Pythia-HDL">
        <img alt="Build" src="https://github.com/rahulbera/Pythia-HDL/actions/workflows/test.yml/badge.svg">
    </a>
    <a href="https://doi.org/10.5281/zenodo.5149410"><img src="https://zenodo.org/badge/DOI/10.5281/zenodo.5149410.svg" alt="DOI"></a>
</p>

<details open="open">
  <summary>Table of Contents</summary>
  <ol>
    <li><a href="#what-is-pythia">What is Pythia?</a></li>
    <li><a href="#setup">Setup</a></li>
    <li><a href="#Run">Run</a></li>
    <li><a href="#citation">Citation</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
    <li><a href="#acknowledgements">Acknowledgements</a></li>
  </ol>
</details>

## What is Pythia?

> Pythia is a hardware-realizable, light-weight data prefetcher that uses reinforcement learning to generate accurate, timely, and system-aware prefetch requests. 

Pythia formulates hardware prefeteching as a reinforcement learning task. For every demand request, Pythia observes multiple different types of program context information to take a prefetch decision. For every prefetch decision, Pythia receives a numerical reward that evaluates prefetch quality under the current memory bandwidth utilization. Pythia uses this reward to reinforce the correlation between program context information and prefetch decision to generate highly accurate, timely, and system-aware prefetch requests in the future.

Pythia is implemented in [ChampSim simulator](https://github.com/ChampSim/ChampSim). The code can be found here:
<p align="center">
<a href="https://github.com/rahulbera/Pythia">Pythia</a>
    <a href="https://github.com/rahulbera/Pythia/releases">
        <img alt="GitHub release" src="https://img.shields.io/github/release/rahulbera/Pythia">
    </a>
</p>

## Setup

To setup Chisel3 in local machine, please follow the [setup instructions](https://github.com/chipsalliance/chisel3/blob/master/SETUP.md)

## Run

To run/test a module, use the following command:

```bash
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

## Citation
If you use this framework, please cite the following paper:
```
@inproceedings{bera2021,
  author = {Bera, Rahul and Kanellopoulos, Konstantinos and Nori, Anant V. and Shahroodi, Taha and Subramoney, Sreenivas and Mutlu, Onur},
  title = {{Pythia: A Customizable Hardware Prefetching Framework Using Online Reinforcement Learning}},
  booktitle = {Proceedings of the 54th Annual IEEE/ACM International Symposium on Microarchitecture},
  year = {2021}
}
```

## License

Distributed under the MIT License. See `LICENSE` for more information.

## Contact

Rahul Bera - write2bera@gmail.com

## Acknowledgements
We acklowledge support from SAFARI Research Group's industrial partners.