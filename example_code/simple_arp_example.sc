// MIDI = Musical Instrument Digital Interface

Server.default = s = Server.internal;
s.boot;

MIDIIn.connectAll;

/* ---------------------------
    A very simple MIDI synth
   --------------------------- */

MIDIdef.noteOn(\keyPressed,{
	arg ... x;
	var midinote = x[1];
	var velocity = x[0];

	~simpleSynth.value(midinote, velocity).play;
	x.postln;
})


~simpleSynth = {
	arg midinote, velocity;
	{
		var freq, volume, env;
		freq = midinote.midicps;
		volume = velocity/127;

		env = EnvGen.ar(Env.perc(1e-8,0.5), doneAction:2);

		LPF.ar(LFSaw.ar([freq, 1.02*freq], 0, volume * env), 4*freq);
	}
}



/* --------------------------------
   A very simple MIDI Arp(eggiator)
   -------------------------------- */

(
~noteList = Array.fill(100,{0});

MIDIdef.noteOn(\keyPressed,{
	arg ... x;
	var midinote = x[1];
	var velocity = x[0];

	~noteList[ midinote ] = 1;
});

MIDIdef.noteOff(\keyOff,{
	arg ... x;
	var midinote = x[1];
	var velocity = x[0];

	~noteList[ midinote ] = 0;
});
)

(
fork{
	var t = 0.5;
	inf.do({
		var activeNotes = [];

		~noteList.do({
			arg value, index;
			if( value==1,
				{activeNotes = activeNotes ++ index});
		});

		fork{
			activeNotes.do({
				arg midinote;
				~simpleSynth.value(midinote, 100).play;
				(t/3).wait;
			});
		};
		t.wait;
	});

}
)





























