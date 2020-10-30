
game-engine

Project goals:
 * Game engine for open world games
 * Low budget in mind, design features around re-using assets and perhaps in-engine creation of them.
 * Low poly.. lower than is standard nowadays.
 * Requires programming skills, editor is for world editing only. not a game maker studio. 
    * Engine provides the world and ways to load and render, you do the rest.
  


Core:
* Library system
* World is divided into cells/sectors/regions
* Cells contain cellnodes which can be anything that you can place in the game world.
* Cellnode class is meant to be extended only for engine features.
* Component system. For cellnodes is made for defining the node itself. 
* Packing and loading of cells, and cellnodes.
* Terrain nodes, and multitexturing support via blendmaps.

Editor
* Editing terrain
    * Modify, Flatten, Smooth 
* Texturing terrain
    * Up to 4 different textures per terrain node.
    * Edit blend map in Editor using brushes.
* Library management:
    * Creation of materials, lights.
    * Editing of material and light nodes.
    * Deleting, renaming entries.
* Cell hierarchy
    * Delete, or save to library.
    * Edit nodes.
* Placement of nodes
    * 
    

*** TODO ***

Core
* Right now, maybe nothing.

Editor
* Ability to save and load cells properly, saving seems to work.
* Ability to snap camera on to node in hierarchy by double clicking.
* Inspector for editing things.
* Better functioning movement tool.
* Highlight borders of all cells.
* The UI is awful right now, have to re-design it.
* Perhaps camera position isn't the best way to decide which cell is active? Feels awkward at times.
    * have no "active" cells? add some smarts into the texture painting?. edit textures via cell hierarchy->terrain->inspect?
 

This read me may be edited at later date as the project progresses.
