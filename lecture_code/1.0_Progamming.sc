/*
 Sound Design with SuperCollider Workshop
 Part I: Introduction to Supercollider
 */

// //////////////////////////////
// A. SUPERCOLLIDER ARCHITECTURE
// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

// SuperCollider is an environment consisting of 2 applications:
//
//     1. The "language" (also known as the "client")
//
//                AND
//
//     2. The synthesis "server"
//
// The language sends commands to the server in order to control what is output to the soundcard. These two applications communicate using network protocols, so they don't have to run on the same computer.

// The synthesis server is not visible to you; it has no graphical user interface (GUI).

// The language, on the other hand, is manifest in the SuperCollider GUI (i.e., what you are looking at right now)











// /////////////////////////////////
// B. INTERACTING WITH THE LANGUAGE
// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

// Executing code
// ==============
// To execute code highlight it and type "enter"
// (This is "Ctl-return" on Macs)
// Double-clicking just to the right of ( or to the left of ) automatically highlights the enclosed code

(
var message;
message = "Hello world"; /* store the string "Hello world" in the variable message */
message.postln; // evaluate message, send result to the "post window"

Server.default.waitForBoot({
	fork{
		var x;
		x = {SinOsc.ar([440,448])}.play; /* asks the server to play something (we will return to this) */
		2.wait;
		x.free;
	}
});
)

/*
 Notice that each line in the language is terminated with a semi-colon ;
 If we don't do this we will get an error.
*/


// Getting help/answers
// =====================
Array // highlight (or double-click) on the text and type ctl-d to bring up the help page

// Click the "Search" button or "Browse" button to find documentation

// "Browse" > "Tutorials" > "Getting-Started" has lots of useful information








// /////////////////////
// PROGRAMMING CONCEPTS
// \\\\\\\\\\\\\\\\\\\\\

// The SuperCollider language is a powerful object oriented programming language. (Everything is an object, and different objects respond to different messages)

a = 10.5111; // set a new global variable

a.isKindOf(Float); // this variable is a Float(ing) point number

Float.dumpAllMethods; // lists all messages that a Float will respond to (there are many)

a.round; // <--- calls the "round" message on a.


// VARIABLES
// =========
(
var myVarA, myVarB, stringA, stringB; // declare
myVarA = 1; // set
myVarB = 2;

stringA = "myVarA is equal to " ++ myVarA ++ "."; // ++ concatenates strings
stringB = "myVarB is equal to " ++ myVarB ++ ".";

stringA.postln;
stringB.postln;
)





// SCOPE
// =====
stringA.postln; // throws an error

// This is because code written between parentheses () has "local" scope. This means that only other elements inside the brackets have access to these "local" variables.

// We can make variables have "global" scope using tilde ~ or the special predefined global variables a,b,c,..z (we already used "a" above)
(
a = "Global variable a";
~myVar = "Global variable ~myVar";
)

a.postln; // no errors this time
~myVar.postln;




// MATH
// ====
// The order of mathematical operations in SuperCollider is a bit different than you might expect.
// Expressions get evaluated from left to right:

a = 1 + 1 * 5; // first 1 + 1 = 2 is calculated, then 2 * 5 is calculated

a = 1 + (1 * 5); // we can force the multiplication to happen first with ()

a = 10 / 2; // division

a = 13 % 10; // modulo operator (gives remainder)

a = 2**3; // this is 2 raised to the power 3

a = sqrt(4); // square root

a = log(10); // natural logarithm

a = log2(10); // base-2 log

a = log10(10); // base-10 log

a = exp(1); // e^1

a = sin(pi); // sine

a = atan(-1); // arctangent

//... and so on


// FUNCTIONS
// =========
// Functions are declared using {}.
(
~f1 = {arg x; x**2};
~f2 = {|x| x**2}; /* ~f2 and ~f1 are identical (they are just two different ways to write the same thing) */

~f1.value(2); /* evalute function with input 2 */
~f1.(2); /* same as above */
)

( // a function with multiple arguments
~f3 = {
	arg x,y,z;
	var sum;
	sum = x + y + z; /* the last line of a function defines its return value */
};
~f3.value(1,2,3);
)

( // a function with a variable number of arguments
~f4 = {
	arg ... x; // x gets converted to a list of the inputs
	x.sum;
};
~f4.value(1,2,3,4,5,6,7,8,9,10);
)






// ARRAYS
// ======
a = [1,2,3,4,5,6,7,8,9,10];
a[0]; // indexed from 0 to length-1
a[9];
a[10]; // doesn't exist

a[0] = a[9]; // set the first value to the last value
a.plot;



// Array shortcuts
// ````````````````
a = (1..10); // series from 1 to 10
a = (2,4..10); // series from 2,4..10 (stepping by 2)
a = 2!5; // 2 repeated 5 times

a = (1..10).rotate(3); // rotate array
a = (1..10).reverse; // reverse array

a = [1,2,3,4].choose; // return a random entry from the array

// Using the Array class create new arrays
Array.rand(100,-1.0,1.0).plot; // 100 random values in range (-1,1)

Array.series(100,0,5).plot; // 100 values, starting from 0 and incrementing by 5

(
~f = {arg x; sin(0.1*x)}; // sine function

a = Array.fill(100,~f); // call function to fill array

b = ~f!100; // shortcut for the above

[a, b].plot;
)


// CONTROL STRUCTURES
// ```````````````````
(
a = Array.series(100,1,1);

// loop through elements of an array
a.do({ arg value, index;
	[index, value].postln;
});
)

10.do({|i| ("Called: "++i).postln});
10.do{|i| ("Called: "++i).postln}; /* same as above () around "do" can be omitted */

// create a new array by modifying another one
(
b = a.collect({ arg value, index;
	-1*value;
});

[a,b].plot;
)

// conditional execution
(
b = a.collect({ arg value, index;
	if( index > 50, // condition
		{-1*value}, // evaluate this function if condition is TRUE
		{value});   // evaluate this function if condition is FALSE
});

[a,b].plot;
)

// while loop
(
i=0;
while({i<5}, {i.postln; i=i+1});
)

// for loops - they exist, but we don't need them ("do" works fine)

// switch - look-up for yourself (highlight and type ctrl-d)


// //////////////////////
// LET'S MAKE SOME SOUND
// \\\\\\\\\\\\\\\\\\\\\\

// !!!WARNING!!!
// `````````````
// It can be dangerous to work in SuperCollider using headphones. This is because you may make mistakes in your code, or something may not work as you expected, which could result in very loud output. It is best to test things out at low volumes, using speakers before you attempt using headphones.


Server.default = s = Server.local; /* set the default server and save it in the global variable s */

s.boot; // we have to boot the server before we can send it anything

// We can send a function to the server to be played
// (the function must return a unit generator)

x = {SinOsc.ar(440)}.play; // SinOsc is a sinusoidal oscillator. "ar" means "audio rate"

// We can stop a sound at any time using "ctrl-."
// This is crucial to remember



(
fork{
	48.do{ |i|
		t = [0.03125, 0.0625, 0.125, 0.25].choose;

		x = {
			var note, freq, sin, env;
			note = 50 + i;
			freq = note.midicps;

			sin = SinOsc.ar([freq, freq*1.1], mul:0.1);
			env = XLine.ar(1, 1e-5, t*2, doneAction:2);

			sin * env;
		}.play;

		t.wait; //0.125.wait;
	}
}
)



/*
 Homework for Part I:
 - install SuperCollider on your computer
 - look at the built in documentation...
*/

// Up next...
// A tour of UGENS
// SynthDefs, The Server, Groups, Busses
// Patterns
// LiveCoding
// Machine Listening
// OSC communication

// Designing Sounds
//  - critical listening
//  - spectral analysis
//  -

// Projects

