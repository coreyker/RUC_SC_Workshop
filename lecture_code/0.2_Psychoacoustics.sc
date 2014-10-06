/*
 ///////////////////////////////////////
 A Very Short Primer on Psychoacoustics
 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
*/

/* Sound
 `````````
Sound is a sensory/perceptual phenomena experienced by humans in response to air pressure variations in a certain *frequency* range.
*/

/* Frequency
 `````````````
Frequency = # of cycles (repetitions) per second of a periodic waveform (measured in Hertz (Hz))

Period = the duration of one cycle
*/
( // Ex.
	var frequency = #[1,2,4,24];
	{SinOsc.ar(frequency)}.plot(1);
)

/* The audible frequency range (20-20,000Hz)
 ```````````````````````````````````````````
*/
( // Ex.
fork{
	x = { |freq=20|
		var amp = AmpCompA.kr(freq,20).lag(0.25);
		SinOsc.ar(freq.lag(0.25), mul:amp).dup;
	}.play;

	s.sync;

	n = 100;
	n.do{|i|
		var freq = i.linexp(0,n,20,20000).postln;
		x.set(\freq, freq);
		0.5.wait;
	};
	x.free;
}
)

x = {LPF.ar(WhiteNoise.ar, MouseX.kr(20,20000,1)).dup}.play;
x.free;

// Wind?
x = {Splay.ar(RLPF.ar(WhiteNoise.ar(1!12), LFNoise2.kr(0.5!12, 1500, 1600), LFNoise2.kr(0.5!12,0.15,0.2)).mean)}.play;
x.free;

/*
 Perception of frequency as tones vs. beats
 ``````````````````````````````````````````
*/
( // Ex.
fork{
	x = { |freq=1|
		Decay.ar(Blip.ar(freq,(15000/freq).floor),2,0.1).dup;
	}.play;

	s.sync;

	((1..4)++(5..9)++(10,15..20)++(30,50..150)).do{|i|
		var freq = i.postln;
		x.set(\freq, freq);
		4.wait;
	};
	x.free;
}
)

/*
 Exponential perception of frequency
 ````````````````````````````````````
*/
(
// Linear frequency increase
// (start to lose perception of pitch difference  at high frequencies)

fork{
	x = {|freq=0| SinOsc.ar(freq,0,0.1).dup}.play;
	s.sync;

	20.do{|i|
		var freq = i.linlin(0,20, 500, 10000);
		x.set(\freq, freq);
		0.5.wait;
	};
	x.free;
};
)

(
// Exponential frequency increase
//(maintain perception of pitch difference at high frequencies)
fork{
	x = {|freq=0| SinOsc.ar(freq,0,0.1).dup}.play;
	s.sync;

	20.do{|i|
		var freq = i.linexp(0,20, 500, 10000);
		x.set(\freq, freq);
		0.5.wait;
	};
	x.free;
};
)

/*
 Dynamic range
 `````````````
- (Digitally) audio waveforms are represented by numbers between [-1,1]
- This then gets translated to a voltage (by the soundcard), which subsequently controls the displacement of a speaker cone

+1 = maximum postive displacement

     |
      \
       |
      /
     |

0 = no displacement

     |
     |
     |
     |
     |

-1 = maximum negative displacement

     |
    /
   |
    \
     |
 */
