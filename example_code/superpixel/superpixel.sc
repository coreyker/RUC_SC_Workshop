s.boot;

(
SynthDef(\pixsynth,{
	arg freq=100, cut=2000, amp=0, pan=0;
	var t=0.01;
	Out.ar(0,
		Pan2.ar(
			LPF.ar(Mix(PinkNoise.ar(0.1) + LFSaw.ar(freq.lag(t)*[1,1.01])), cut.lag(t)) * amp.lag(t),
			pan)

	);
}).add;
)

(
~deg = Scale.major.degrees.asFlatArray;
~scale = ~deg.collect{|i| 12*(..2) + i}.flat;

~nrows = 7;
~ncols = 7;

~synthlist = Array.fill(~ncols*~nrows, {Synth(\pixsynth)});
~bundle = List.new;
OSCdef(\sp,{
	arg ... x;

	var num = x[0][1];
	var hue = x[0][2];
	var sat = x[0][3];
	var bright = x[0][4];
	var row = (num/~ncols).floor;
	var col = num % ~ncols;

	var octave = ~nrows - row - 1;
	var degree = row*5 + col;//hue.linlin(0,255,0,12).floor + num;
	var note = 12*octave + ~scale.wrapAt(degree + hue.linlin(0,255,-5,5));
	//var amp = bright.explin(1e-6,255,1e-6,1);
	//var cut = sat.linlin(0,255,0,2000);
	var cut = bright.linlin(0,255,100,2000);
	//var amp = bright.linlin(0,255,0,0.5);
	var amp = sat.linlin(0,255,0,1);
	//var amp = hue.linlin(0,255,0,1);
	var pan = col.linlin(0,~ncols-1,-1,1);

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

s.record;
s.stopRecording;