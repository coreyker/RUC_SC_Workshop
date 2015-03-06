/*
Short intro to "Patterns"
*/

// Sound buffers
~sampleFolder = "/Users/corey/Music/Samples/linndrum";
~kick  = Buffer.read(s, ~sampleFolder +/+ "kick.wav");
~snare = Buffer.read(s, ~sampleFolder +/+ "sd.wav");
~hat   = Buffer.read(s, ~sampleFolder +/+ "chhl.wav");

// Sound player
SynthDef(\samplePlayer, {
	arg buf=0, amp=0.5, rate=1;
	var snd = PlayBuf.ar(1, buf, rate, doneAction:2);
	Out.ar(0, amp * snd.dup);
}).add;

// Test
Synth(\samplePlayer, [\buf, ~kick]);
Synth(\samplePlayer, [\buf, ~snare]);
Synth(\samplePlayer, [\buf, ~hat]);


// Play with pattern
TempoClock.default.tempo = 3;
Pdef(\drums).play;
Pdef(\drums).quant = 4;

(
~dur = 1;
~kickPat = Pbind(\instrument, \samplePlayer,
	\buf, ~kick,
	\dur, ~dur,
	\amp, Pseq([2,0,0,0],inf)
);

~snarePat = Pbind(\instrument, \samplePlayer,
	\buf, ~snare,
	\dur, ~dur,
	\amp, Pseq([1, 0, 1, 0],inf)
);

~hatPat = Pbind(\instrument, \samplePlayer,
	\buf, ~hat,
	\dur, 1,
	\rate, Pseq([2,1.5,1,0.75],inf),
	\amp, Pwhite(0.1,0.5)
);

~melody = Pbind(\instrument, \default,
 	\octave, 4,//Prand([4,5],inf),
 	\degree, Pn(Plazy({Pstutter(1,Pshuf(({14.rand}!8).clump(4), 2))}),inf),
 	\sustain, 0.1,
 	\strum, 0.5,
 	\amp, Pwhite(0.01,0.5),
 	\dur, ~dur
 );

//Pdef(\drums, Ppar([~kickPat, ~snarePat, ~hatPat], inf));
Pdef(\drums, Ppar([~kickPat, ~snarePat, ~hatPat, ~melody], inf));
)



Pdef(\drums).clear

~hatFillPat = Pbind(\instrument, \samplePlayer,
	\buf, ~hat,
	\dur, Pseq([Pseq(1!4,1), Pseq([1,1,1,0.33,0.33,0.34],1)],inf),
	\rate, Pseq([2,1.5,1,0.75]++(1!6),inf),
	\amp, Pseq([Prand([0.125!4],1),Pseq([1,0.1,0.5,0.2,0.4,0.2],1)],inf)
);