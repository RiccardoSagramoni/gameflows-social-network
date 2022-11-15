# gameflows-social-network
Project for the *Large-Scale and Multi-Structured Databases* course by [Riccardo Sagramoni](https://github.com/RiccardoSagramoni), [Luca Tartaglia](https://github.com/LucT3) and [Fabrizio Lanzillo](https://github.com/FabrizioLanzillo).

The [*Release*](https://github.com/RiccardoSagramoni/gameflows-social-network/releases) page contains both the source code and the dumps of the databases.
*Note:* The Java source code is inside the **gameflows** folder.

## Description
**GameFlows** is a **social networking application** that aims to put together people interested in the same videogames. 

Users registered to the service can discover new videogames and follow the related community. Inside a community a user can interact with the other user by writing posts or reply to other users' posts. 

Moreover, user can read posts written by special users, called *influencers*. Influencers are users who have proven to be engaging inside the community in respect to the other users. The level of "ability to engage" is defined the number of recent likes and comments on its posts. In this perspective, their posts can be filtered out from the others while browsing the post of a videogame community.

## Structure of the repository
- **cluster**: scripts for local and remote clusters
- **databases**: scripts for MongoDB and Neo4j databases
  - **dataset**: scripts used to generate the dataset
  - **scripts**: scripts to generate indexes in MongoDB and Neo4j databases
- **docs**: documentation files and resources (queries, UML diagrams...)
- **gameflows**: Java source files

## Structure of the repository

```
gameflows-social-network
|
├── cluster
│   ├── local-replicas
│   └── virtual-machines
|
├── databases
│   ├── dataset
|   |   ├── dataset-generation
|   |   └── dataset-to-mongodb
|   └── scripts
|
├── docs
│   ├── resources
│   ├── Manual of usage.pdf
│   └── Documentation.pdf
|
└── gameflows
    └── src
        ├── main
        │   ├── java
        │   │   └── it.unipi.dii.inginf.lsdb.gameflows
        │   │       ├── admin
        │   │       ├── comment
        │   │       ├── gui
        │   │       │   ├── controller
        |   |       |   |   └── listener
        │   │       │   └── model 
        │   │       ├── persistence
        │   │       ├── post
        │   │       ├── user
        │   │       ├── util
        │   │       └── videogamecommunity
        |   |
        │   └── resources    
        │       └── it.unipi.dii.inginf.lsdb.gameflows
        │           └── gui
        │               ├── controller
        │               ├── css
        │               ├── icons
        │               └── logos 
        └── test
            └── java
                └── it.unipi.dii.inginf.lsdb.gameflows
                    ├── admin
                    ├── comment
                    ├── persistence
                    ├── post
                    ├── user
                    ├── util
                    └── videogamecommunity          
```