s.boot;

(
SynthDef(\pixsynth,{
	arg freq=100, cut=2000, amp=0, pan=0;
	var t=0.01;
	Out.ar(0,
		Pan2.ar(
			LPF.ar(Mix(LFSaw.ar(freq.lag(t)*[1,1.01], 0, amp.lag(t))), cut.lag(t)),
			pan)

	);
}).add;
)

(
~deg = Scale.minor.degrees.asFlatArray;
~scale = ~deg.collect{|i| 12*(..2) + i}.flat;

~synthlist = Array.fill(32, {Synth(\pixsynth)});
~bundle = List.new;
OSCdef(\sp,{
	arg ... x;

	var num = x[0][1];
	var hue = x[0][2];
	var sat = x[0][3];
	var bright = x[0][4];
	var row = (num/8).floor;
	var col = num % 8;

	var octave = 2+row*12;
	var degree = hue.linlin(0,255,0,12).floor + row + col;
	var note = octave + ~scale.wrapAt(degree);
	//var amp = bright.explin(1e-6,255,1e-6,1);
	//var cut = sat.linlin(0,255,0,2000);
	var cut = bright.linlin(0,255,100,2000);
	var amp = bright.linlin(0,255,0,1); //sat.linlin(0,255,0,1);
	var pan = col.linlin(0,7,-1,1);

	if( ~bundle.size == ~synthlist.size, {
		// bundle full, so send
		s.listSendBundle(s.latency, ~bundle);
		~bundle=List.new;
	},{
		~bundle.add( ~synthlist[ num ].setMsg(\freq, note.midicps, \amp, amp, \cut, cut, \pan, pan) );
	});
	//x.postln;
},\superpixel);
)

~deg

