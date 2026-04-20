This repository is about the paper "Drone Station Location and Routing Optimization for Infrastructure Inspection''.

Project Structure

This repository is organized into three main folders: code, data, and results. Their contents are briefly described below.

1. code/

This folder contains the source code of the solution methods implemented in this project.

ALNS/
Contains the implementation of the ALNS-based algorithm and its related components. More details about this algorithm can refer to the paper by Rave and Fontaine (EJOR, DOI: https://doi.org/10.1016/j.ejor.2024.11.040).

TSALNS/
Contains the implementation of the TSALNS algorithm and the corresponding supporting classes.

ALNS_Algorithm_Description.txt 
Provides a detailed overview of the ALNS framework, including the required environment, main class structure, supported benchmark formats, and basic instructions for running the code.

TSALNS_Algorithm_Description.txt
Provides a detailed description of our TSALNS framework, together with its environment requirements, code structure, and main functional modules.

The structure of these two algorithms are summarized as follows:
code/
├── ALNS/
│   ├── Main / BatchExperimentRunner
│   │   ├── Entry points for single-instance and batch experiments
│   │   └── Handle running, testing, and exporting results
│   ├── Instance readers
│   │   ├── InstanceLoader
│   │   ├── F1Reader / F2Reader / F3Reader / F4Reader / F5Reader
│   │   └── Convert benchmark files into the unified LRPInstance structure
│   ├── Core data classes
│   │   ├── Node
│   │   ├── Customer
│   │   ├── Facility
│   │   ├── Route
│   │   ├── Solution
│   │   └── LRPInstance
│   ├── Solver and operators
│   │   ├── ALNSSolver
│   │   ├── DestroyType / RepairType
│   │   ├── OperatorScore / InsertionMove / RemovalResult
│   │   └── Implement ALNS search, operator selection, and adaptive scoring
│   └── Utilities
│       ├── DistanceCalculator / DistanceMode
│       ├── SolutionEvaluator
│       └── SolutionPrinter
│
└── TSALNS/
    ├── Main
    │   └── Overall program entry and experiment orchestration
    ├── Node
    │   └── Stores node id, coordinates, service energy, and service time
    ├── Solution
    │   └── Stores station types and routes structure
    ├── FileReaderUtil
    │   └── Reads instance files and creates Node objects
    ├── Init
    │   ├── Builds the initial station plan
    │   ├── Allocates targets to stations
    │   └── Generates initial drone routes
    ├── LocationOperator
    │   └── Provides neighborhood moves for station opening/type decisions
    ├── RoutingOperator
    │   ├── Provides local search operators such as swap and 2-opt
    │   ├── Provides destroy operators
    │   └── Provides repair/insertion operators
    ├── Check
    │   ├── Evaluates path time, path cost, and total cost
    │   ├── Checks energy and operating-time feasibility
    │   └── Provides route-copying and reporting utilities
    ├── ALNSSolver
    │   └── Runs the adaptive large neighborhood search for route improvement
    └── Calculator
        └── Shared utility class for distance and basic cost-related calculations


2. data/

This folder contains instances used in the experiments, together with a short description of their formats.

Data_description.txt
Provides a brief explanation of the instance sets used in this project, including their formats and basic interpretation.

Instances_Alg/
Contains the LRPHSD instances used for the main algorithmic experiments.

Instances_Het_Case/
Contains the LRPHSD instances used for the heterogeneous experiments and case study.


3. results/

This folder contains the main computational results obtained in the experiments.

Results for drone deployment.xlsx
Stores the experimental results related to drone deployment settings.

Results for LRP instances.xlsx
Stores the computational results on the LRP instances.

Results for LPHSD Instances.xlsx
Stores the computational results on the LPHSD instances.



