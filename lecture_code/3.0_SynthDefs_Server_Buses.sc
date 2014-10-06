// SynthDefs, The Server, Groups, Busses
Server.default = s = Server.internal;
s.boot;

// Controling synths

/* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
   Up until now we have been using the {}.play shortcut.
   This is fine for protoyping, however, it is usually better
   to use "SynthDefs" which allow us to control how audio signals
   are routed.
   -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= */

// Let's translate:
{SinOsc.ar(400,0,0.1)}.play

// into a synthdef:
SynthDef(\simpleSine, {|freq=440, amp=0.1|
	var sin = SinOsc.ar(freq,0,amp);
	Out.ar(0, sin); //<--- Noitce the "Out" UGEN is new (it lets us send the output to different busses)
}).add;

x = Synth(\simpleSine, [\freq, 220, \amp, 0.25]);
x.set(\freq, 200);
x.free;

// There are 2 types of Busses in SuperCollider (control rate, and audio rate)
// Audio bus 0 corresponds to the first output on your soundcard
// Audio bus 1 corresponds to the second output on your soundcard
// and so on,

// Let's assume "ugen" is some multichannel UGEN and "busnum" is an integer
// Writing Out.ar(busnum, ugen), sends ugen[0] to busnum, ugen[1] to busnum+1, and so on...

// There are __A__ audio busses. The first __B__ are audio outputs. The next __C__ are audio inputs. The remaining __D__ are private audio busses:

(
("A: " ++ s.options.numBuffers).postln;
("B: " ++ s.options.numOutputBusChannels).postln;
("C: " ++ s.options.numInputBusChannels).postln;
("D: " ++ (s.options.numBuffers - s.options.numOutputBusChannels - s.options.numInputBusChannels)).postln;
)

/* ----------------------------
    Control buses
   ---------------------------- */

// Control buses can be used to control SynthDefs
~ctlBus = Bus.control(s,1);
~ctlBus.set(0);

x = Synth(\simpleSine, [\freq, ~ctlBus.asMap]);

~ctlBus.set(400);
~ctlBus.get()
~ctlBus.set(200);
~ctlBus.get()
~ctlBus.set(0);
x.free;


/* ----------------
   Audio buses
   ---------------- */

// Getting input
SynthDef(\input,{
	var in = In.ar(8,1); // busses 0-7 are output, so the 8th is the first input
	//var in = SoundIn.ar(0); // <--- Note!!! this is the same as In.ar(8,1); but easier to write

	var freq = Pitch.kr(in); // frequency tracker
	var amp = Amplitude.kr(in); // volume tracker
	var trig = amp>0.1;

	var out = SinOsc.ar(Gate.kr(freq, trig));

	Out.ar(1, DelayC.ar(out,0.5,0.2,0.05)); // send to right speaker
}).add;

x = Synth(\input); // Will work better with headphones (to avoid feedback)
x.free;

/* ------------------------------
   Routing audio between busses
   ------------------------------ */

~wetBus = Bus.audio(s,2);
~dryBus = Bus.audio(s,2);
~fxBus = Bus.audio(s,2);

// input src
SynthDef(\input, {
	var src = SinOsc.ar(800,pi/2,Decay2.ar(Dust.ar(2),0.01,0.1,0.5))!2;
	Out.ar(~fxBus.index, src);
	Out.ar(~dryBus.index, src);
}).add;

// fx
SynthDef(\fx, {|room=0.5|
	var in = In.ar(~fxBus.index,2);
	8.do{in = AllpassC.ar(LPF.ar(in,1000), 0.2, Rand(0.01,0.2),Rand(-3,3))};
	Out.ar(~wetBus.index, in);
}).add;

// mix
SynthDef(\mix, { |fx=0|
	var wet = In.ar(~wetBus.index,2);
	var dry = In.ar(~dryBus.index,2);
	var out = SelectX.ar(fx, [dry, wet]);
	Out.ar(0, out);
}).add;

// important evaluate c, then b, then a
c = Synth(\mix);
b = Synth(\fx);
a = Synth(\input);

c.set(\fx, 0.8);

// note!!!: if we evaluate the synths in a different order this won't work

/* -------------------------------
             Groups
   ------------------------------- */
// In order to control the order in which busses are evaluated we can use "Groups"

~bus = Bus.audio(s,2);

SynthDef(\input,{
	Out.ar(~bus.index, SinOsc.ar(440,0,0.1).dup)
}).add;

SynthDef(\output, {
	Out.ar(0, In.ar(~bus.index,2))
}).add;


(
~inputGroup = Group.new;
~outputGroup = Group.after(~inputGroup);

// because the group are in the right order (output after input) we can evaluate
// the following two lines in any order and everything still works!
Synth(\input, target:~inputGroup);
Synth(\output, target:~outputGroup);
)


/* ----------------------------------
       An example
   ---------------------------------- */

~outBus = Bus.audio(s,2);
~ctlBus = Bus.control(s);
~ctlBus.set(1);

SynthDef(\sin,{|freq=100,dur=1,amp=0.1|
	var env = EnvGen.ar(Env.sine(dur,amp), doneAction:2);
	var out = SinOsc.ar([freq, 1.01*freq],0,env);
	Out.ar(~outBus.index, out);
}).add;

SynthDef(\out,{|dur=1, feedback=0.5|
	var in = In.ar(~outBus.index,2) + LocalIn.ar(2);
	Out.ar(0, in.softclip);

	LocalOut.ar(DelayC.ar(in,1,dur,feedback));
}).add;

(
~inGrp = Group.new;
~outGrp = Group.after(~inGrp);

x = Synth(\out, target:~outGrp);

fork{
	var degrees = Scale.minor.degrees;
	var octaves = 12*(2..6);

	inf.do{
		Synth(\sin, [\freq, (octaves.choose + degrees.choose).midicps, \dur, ~ctlBus.asMap, \amp, 0.1], target:~inGrp);
		0.5.wait;
	}
}
)

~ctlBus.set(0.25);
x.set(\dur, 0.125/2, \feedback, 0.95);

x.free;
x = Synth(\out, target:~outGrp);