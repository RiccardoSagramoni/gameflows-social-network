# gameflows-social-network
The *Release* page contains both the source code and the dumps of the databases.
*Note:* The Java source code is inside the **gameflows** folder.

## Description
**GameFlows** is a **social networking application** that aims to put together people interested in the same videogames. 

Users registered to the service can discover new videogames and follow the related community. Inside a community a user can interact with the other user by writing posts or reply to other users' posts. 

Moreover, user can read posts written by special users, called *influencers*. Influencers are users who have proven to be engaging inside the community in respect to the other users. The level of "ability to engage" is defined the number of recent likes and comments on its posts. In this perspective, their posts can be filtered out from the others while browsing the post of a videogame community.

## Structure of the repository
- **cluster**: scripts for local and remote clusters
- **databases**: dump and script for MongoDB and Neo4j databases
  - **dataset**: scripts and json files used to generate the final dataset
  - **scripts**: scripts to generate indexes in MongoDB and Neo4j databases
- **docs**: documentation files (queries, UML diagrams...)
- **gameflows**: Java source files
