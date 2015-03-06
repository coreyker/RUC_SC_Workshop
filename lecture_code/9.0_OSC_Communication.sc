/*
Communicating with other software over OSC

OSC => Open Sound Control

See also:

Help > Browse > External Control > OSC > OSC Communication
*/

/* Example: Communicating with Processing

  --> First the oscP5 library must be added to processing
  --> Download it here: "http://www.sojamo.de/libraries/oscP5/"
  --> Copy and paste the following lines into Processing and run the sketch:

// ----------- Processing code start ----------------
import oscP5.*;
import netP5.*;

OscP5 oscP5;
NetAddress netaddr;
OscMessage msg;
OscBundle bundle;

// We can double the port SC is listening to by running the following line:
// "SC is listening for OSC messages on port: " ++ NetAddr.langPort.postln;
int SC_PORT = 57120;

// Port that Processing will listen to:
int PROCESSING_PORT = 12000;

void setup() {
  size(800, 300);

  // setup OSC ports
  oscP5   = new OscP5(this, PROCESSING_PORT);
  netaddr = new NetAddress("127.0.0.1", SC_PORT);
}

void draw() {

  background(0); // black background
  fill(255); // white fill
  ellipse(mouseX, mouseY, 30, 30);

  // add mouse x and y position to message
  msg = new OscMessage("/mouse_pos");
  msg.add(mouseX/float(width));
  msg.add(mouseY/float(height));

  oscP5.send(msg, netaddr);
}
// ----------- Processing code end ----------------

*/

// Check what port SC listens on (57120 by default)
"SC is listening for OSC messages on port: " ++ NetAddr.langPort.postln;

/*
Print out received messages with id: /mouse_pos
*/
OSCFunc({
	arg msg, time, addr, recvPort;
	//[msg, time, addr, recvPort].postln; // print complete argument list
	("mouse x: " ++ msg[1] ++ ", mouse y: " ++ msg[2]).postln;
}, '/mouse_pos');












// Control something in SC
// Simple synth def
SynthDef(\simpSynth, {
	arg freq=100, cutoff=400;
	var src = Saw.ar(freq*[0.95,0.99,1,1.01,1.05]).mean;
	var fil = BLowPass.ar(src, cutoff, 0.125);
	Out.ar(0, fil.dup);
}).add;


(
~synth = Synth(\simpSynth);

OSCFunc({
	arg msg, time, addr, recvPort;
	var mouseX = msg[1];
	var mouseY = msg[2];

	var freq = mouseX.linlin(0, 1, 50,1000);
	var cutoff = mouseY.linlin(0, 1, 12000, 50);

	("mouse x: " ++ mouseX ++ ", mouse y: " ++ mouseY).postln;
	("freq: " ++ freq ++ ", cutoff: " ++ cutoff).postln;

	~synth.set(\freq, freq, \cutoff, cutoff);
}, '/mouse_pos');
)


