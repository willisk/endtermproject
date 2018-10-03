# My CS end-term project
This was my end-term project for the course 'Introduction to Computer Science'.

Our task was to create a game where the player has to navigate through a maze filled with static and dynamic obstacles in search for all hidden keys.
Once aquired the player is able to pass through the secret exit and finish the game.

We were given the code for creating random mazes and had to implement game logic, hit collision, menus, a GUI, etc..

The original requirement for the project was to use Lanterna, a character-by-character based GUI, to display the maze on the screen.
I had used Java's Swing Toolkit before to and decided to instead fare with it for this project.


![Alt text](demo_2.png?raw=true "Game Menu")

The character and map textures were taken from my two favorite childhood games.
The player and mobs were made animated.

In the end, I created three different kind of levels with varying difficulties:
A cave level,

![Alt text](demo_3.png?raw=true "Cave level")

a snow level

![Alt text](demo_4.png?raw=true "Snow level")

and a forest level.

![Alt text](demo_5.png?raw=true "Forest level")

![Alt text](demo_6.png?raw=true "Game Over")

## Installing
To run the game, clone the repository and run the main class in Java:

Clone the repository:
```
git clone https://github.com/williskurt/endtermproject
```
To compile the game use:
```
cd endtermproject/src
javac game/*.java
```
To run the game, be sure to be a folder above the .class files (so either in /bin or /src) and run:
```
java game.Game
```
