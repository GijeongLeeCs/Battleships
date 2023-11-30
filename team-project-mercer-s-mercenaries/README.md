# team-project
This is the repo for the CSC335 Final Project\
Contributors:\
Ryan Bullard\
Elliot Seo\
Kartikey Bihani\
Gijeong Lee
# Running the Server
To run multiplayer games, first Networking.Server.Server.java needs to be run. Afterward, the clicking the online button
in BoardGUI.java should not error. Pass the IP of your server to the other people you'd like to play with and they can
enter it on the registration screen If there are no existing hosts, both buttons in the connection panel default to
registering this client as a host in the server. If there are existing hosts, the game will display a list of registered
hosts. Clicking on a host will allow you to join their game.\
There is also a chat window, so you can ~~be toxic to~~ communicate with your opponent. Prior to connecting to a game,
messages will be sent to all users waiting to join a game.
# Known Bugs
Sometimes during gameplay, exceptions are printed to the console with vague messages like "Null-Pointer Exception because
 local<5> is null". These error messages are unhelpful for debugging, and are difficult to track down.\
Explosions start at a random offset in the animation. Since the code uses a single instance of the explosion animations,
 the explosion animation does not fully play each time it is used. The animation then pauses at an arbitrary point and 
resumes the next time it is used. The bug is small enough that the inefficient solution of creating a new image object 
each time is not worth the effort, memory, and speed to fix.\
Explosions reveal the position of enemy ships. This is actually somewhat accurate to the real rules of battleship. Players 
are supposed to announce which ship was hit, and the size. Since we can't reveal this in the game, this bug actually 
reflects this rule. (~~This is not cope 🙄🙄🙄🙄~~)
