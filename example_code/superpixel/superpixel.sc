
SynthDef(\pixsynth,{
	arg freq=100, cut=100, amp=0, pan=0;
	Out.ar(0,
		Pan2.ar(
			LPF.ar(LFSaw.ar(freq, 0, amp),cut),
			pan)

	);
}).add;

~synthlist = Array.fill(32, {Synth(\pixsynth)});

~scale = Scale.minor.degrees.asFlatArray;

OSCdef(\superpixel,{
	arg ... x;

	var pan = 0;
	var num = x[0][1];
	var hue = x[0][2];
	var sat = x[0][3];
	var bright = x[0][4];

	var degree = hue.linlin(0,255,0,24).floor;
	var note = 50 + ~scale.wrapAt(degree) + (degree/~scale.size*12);
	var amp = bright.linlin(0,255,0,1);
	var cut = sat.linlin(0,255,0,2000);

	if( num<16,
		{pan=-1},
		{pan=1}
	);


	note.postln;

	~synthlist[ num ].set(\freq, note.midicps, \amp, amp, \cut, cut, \pan, pan);
},\superpixel)
