/*
 Sound Design with SuperCollider Workshop
 Part II: A Tour of UGENS
*/

/* -----------------------------------------------------
SuperCollider is a modular sound processing environment.

It includes many basic modules that can be interconnected in different ways to make sound.

We call these modules UGENs == Unit Generators
------------------------------------------------------- */

Server.default = s = Server.internal;
s.boot;

// UGEN short for Unit Generator

// ///////////////////////////////
// 0. Audio Rate vs. Control Rate
// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
// UGENS can run at 2 different rates: audio rate (ar), and control rate (kr):
{
	[ SinOsc.kr(100), SinOsc.ar(100)]
}.plot

{
	var freq = SinOsc.kr(4);
	SinOsc.ar(100*freq + 200)
}.play;

// Using control rate UGENs reduces the CPU load
// (thus "kr UGENS" should be used instead of "ar UGENS" for parameters that need to be changed slowly)

// /////////////////////////
// A. Sound Producing UGENS
// \\\\\\\\\\\\\\\\\\\\\\\\\
s.freqscope;
s.scope;


/* -----------------------
 The Sinusoidal Oscillator
 ------------------------- */

{SinOsc.ar(400!3, [0, pi/2, pi])}.plot;
{SinOsc.ar(400!3, [0, pi/2, pi])}.scope;
{SinOsc.ar(MouseX.kr(10,5000).poll)}.play;

// **********************************
// Diversion: Multichannel expansion
// **********************************

// this:
{
	Splay.ar([
		SinOsc.ar(100),
		SinOsc.ar(200),
		SinOsc.ar(300),
		SinOsc.ar(400);
	])
}.play;

// is the same as this:
{Splay.ar( SinOsc.ar(100*(1..4)) )}.play; // but this is much shorter to write!


// Multichannel expansion can be very powerful:
{
	var n = 40;
	var freq = Array.series(n,100,100);
	var amp = Array.rand(n,0,1);
	var osc = SinOsc.ar( freq, mul:amp );
	osc.mean.dup;
}.play;

// Let's automate that
(
fork{
	50.do{|i|
		var freq;
		freq = (0..i)+1 * 100;
		x = {Mix(SinOsc.ar(freq, mul:1/n)).dup}.play;
		0.1.wait;
		x.free;
	}
}
)

/* -----------------------
 The Triangle Oscillator
 ------------------------- */
{
	SinOsc.ar(100)!2;
}.play;

{
	var freq = 200;
	[LFTri.ar(freq), SinOsc.ar(freq), Saw.ar(freq)]
}.plot;

/* -----------------------
 The Sawtooth Oscillator
 ------------------------- */

{
	[LFSaw.ar(100), Saw.ar(100)]
}.play;

/* -----------------------
 The Pulse Oscillator
 ------------------------- */
{
	[
	LFPulse.ar(500,0,0.1),
	LFPulse.ar(500,0,0.5),
	LFPulse.ar(500,0,0.9)
	]
}.plot

{
	LFPulse.ar(100,width:MouseX.kr(0.1,0.9).poll).dup;
	// See also "Pulse.ar"
}.play

{
	var width = SinOsc.kr(0.2,0,0.5,0.5);
	LFPulse.ar(100, 0, width).dup;
}.play


/* -----------------------
 The Klang Oscillator(s)
 ------------------------- */
(
	n = 12;
~freqs = {([-1,1].choose * Scale.dorian.degrees.choose + 60).midicps}!n;
~amps = {0.1.rand}!n;
a = {
	Klang.ar(`[~freqs,~amps,nil]).dup
}.play;
)


/* -----------------------
 Buffer playback
 ------------------------- */
~filepath = Platform.resourceDir +/+ "sounds/a11wlk01.wav";
~sndbuf = Buffer.read(s, ~filepath);

// The simple way

(
x = {
	var rate=SinOsc.ar(0.1,0,1);
	PlayBuf.ar(1, ~sndbuf.bufnum, rate, loop:1).dup;
}.play;
)

y = {SinOsc.ar(100)}.play;

x.set(\rate, -1);
x.set(\rate, 1);
x.free;

// The more flexible way
// (use another UGEN to choose sample playback)

// Ex 1: linear playback
// ``````````````````
{Phasor.ar(100, 1, 0, 100)}.plot;
(
x = {
	arg rate=1;
	var phase = Phasor.ar(1, rate, 0, BufFrames.ir(~sndbuf.bufnum));
	BufRd.ar(1, ~sndbuf.bufnum, phase, 1, 4).dup;
}.play;
)

x.set(\rate, 0.5);
x.set(\rate, 2);
x.free;

// Ex 2a: non-linear playback
// ```````````````````````````
(
x = {
	arg rate=1;
	var n_samples = BufFrames.ir(~sndbuf.bufnum);
	var n_dur = BufDur.ir(~sndbuf.bufnum);
	var phase = SinOsc.ar(rate/2 * n_dur.reciprocal, mul:n_samples/2);
	BufRd.ar(1, ~sndbuf.bufnum, phase, 1, 4).dup;
}.play;
)

x.set(\rate, 0.5);
x.set(\rate, 4);
x.free;

// Ex 2b: non-linear playback
// ```````````````````````````
{Phasor.ar(1,1,0,100) + LFNoise0.ar(100,10)}.plot(0.1)

(
x = {
	arg rate=1, jitter=2;
	var n_samples = BufFrames.ir(~sndbuf.bufnum);
	var phase = Phasor.ar(1,rate,0,n_samples-1) + LFNoise0.ar(jitter, n_samples-1) % n_samples;
	BufRd.ar(1, ~sndbuf.bufnum, phase, 1, 4).dup;
}.play;
)

x.set(\rate, 5, \jitter, 10);
x.set(\rate, 1, \jitter, 0);
x.free;


/* --------------------------
  Interconnecting Oscillators
  ---------------------------*/
// We have already seen (to some extent) that UGENs can be chained together
(
x = {
	arg freq1=200, freq2=0.25;
	var llfo = SinOsc.ar(0.5, mul:freq2);
	var lfo = SinOsc.ar(llfo,0,50);
	var osc = SinOsc.ar(freq1 + lfo);
	osc;
}.play;
)

x.set(\freq1, 200, \freq2, 2);
x.set(\freq1, 200, \freq2, 6);
x.set(\freq1, 200, \freq2, 14);
x.set(\freq1, 200, \freq2, 20);
x.set(\freq1, 200, \freq2, 50);
x.set(\freq1, 200, \freq2, 1000);

(
fork{
	inf.do{
	x.set(\freq1, 200, \freq2, 1200);
	(0.125/4).wait;
	x.set(\freq1, 200, \freq2, 1300);
	(0.125/4).wait};
	x.free;};
)


// /////////////////////////
// Noise Producing UGENS
// \\\\\\\\\\\\\\\\\\\\\\\\\

/* ------------------------
   Low frequency (LF) noise:
   ------------------------ */
(
{
	[
		LFNoise0.ar(100),
		LFNoise1.ar(100),
		LFNoise2.ar(100)];
}.plot(0.1)
)

// LFNoise is good for modulating parameters:
(
x = {
	arg rate=2;
	var freq = LFNoise0.ar(rate,500,510);
	var amp = LFNoise2.ar(rate,0.25,0.3);
	SinOsc.ar(freq,0,amp);
}.play;
)
x.set(\rate, 300);
x.free;

/* ------------------------
   White/Pink/Brown noise
   ------------------------ */
{WhiteNoise.ar(0.1).dup}.play
{BrownNoise.ar(0.1).dup}.play
{PinkNoise.ar(0.1).dup}.play

(
{
	SelectX.ar(MouseX.kr(0,3), [
		SinOsc.ar,
		Saw.ar,
		BrownNoise.ar
	]) * 0.1;
}.play;
)

/* ------------------------
   Dust
   ------------------------ */
{Dust.ar(1000)}.plot;
{Impulse.ar(300)}.plot;

{Decay.ar(Impulse.ar(10),0.1)}.plot(1)

(
x = {
	arg rate=2, freq=80;
	Decay.ar(Impulse.ar(rate),1) * SinOsc.ar(freq);
}.play;
)

x.set(\rate, 2);
x.set(\freq, 100);
x.free;

// /////////////////////////
// Envelopes
// \\\\\\\\\\\\\\\\\\\\\\\\\
// Envelopes can be used to control the time of frequency evolution of other UGENs

// Built-in envelope types
(
	[
	Env.sine(1),
	Env.perc(1e-3,0.2),
	Env.adsr(0.1,0.2,0.5,0.5),
	].plot
)

// Custom envelope
(
Env.new([0,1,4,3,0.1,2],[0.1,0.3,0.2,0.05,0.1], [\lin, \exp, \sin, \cub, \sqr]).plot
)

Env.perc(1e-8,0.5,1,2).plot

// Applying an envelop
(
{
	var env_shape = Env.perc(1e-8,3,1,-20);
	var amp_env = EnvGen.ar(env_shape, doneAction:2);
	var freq = EnvGen.ar(Env.new([0,1,0.5,1,0.1,2],[0.1,0.5,0.2,0.5,0.1]))*500 + 50;
	var out = LFSaw.ar(freq*[1,1.01]) * amp_env;
	out.dup;
}.play;
)

/*************************
  Diversion: doneActions
 *************************/
// Most important doneAction -> "2"
// This deletes/frees the synth when the envelope finishes

// See:
x = { EnvGen.ar(Env.sine(1e-6), doneAction:2) }.play;
x.free; //<--- see error message
//(This is because synth was freed by the envelope already)

// If we have hundreds of short duration synths playing we will want them to free themselves automatically using doneActions so that the server does not get overloaded (and possibly crash).

/* ---------------------------
   Other types of envelopes
   --------------------------- */

// Line vs. XLine
(
{
	[
		Line.kr(100,0,1),
		XLine.kr(100,1e-12,1)
	]
}.plot(1);
)

// Line
(
{
	var freq = Line.kr(10000,200,4, doneAction:2);
	SinOsc.ar(freq,0,0.1).dup;
}.play;
)

(
// note: put freqscope in linear scale
fork{
	10.do{
		{
			var freq = Line.kr(10000,200,4, doneAction:2);
			SinOsc.ar(freq,0,0.05).dup;
		}.play;
		0.2.wait;
	};
}
)

// XLine
(
{
	var freq = XLine.kr(10000,200,4, doneAction:2);
	SinOsc.ar(freq,0,0.1).dup;
}.play;
)


(
fork{
	10.do{
		{
			var freq = XLine.kr(10000,200,4, doneAction:2);
			SinOsc.ar(freq,0,0.05).dup;
		}.play;
		0.2.wait;
	};
}
)

// Decay
(
{
	var amp = Decay.ar(Dust.ar(10),0.3,0.2);
	SinOsc.ar({1000.rand}.dup,0,amp);
}.play;
)

// Note that Decay doesn't allow a doneAction
// However, there are a few special UGENs that will let use issue a doneAction

// DetectSilence
(
x = {
	var amp = Decay.ar(Impulse.ar(0), 0.01);
	DetectSilence.ar(amp, 0.01, 0.01, doneAction:2);
	0;
}.play;
)
x.free;

// FreeSelf
(
x = {
	var freq = Phasor.kr(1, 10, 20, inf);
	FreeSelf.kr(freq>4000);
	SinOsc.ar(freq,0,0.1).dup;
}.play;
)

// Lag (smooths out transitions
(
{
	a = LFNoise0.ar(8);
	[
		a,
		a.lag(0.05)
	]
}.plot(1);
)

// Compare:
(
{
	var freq = LFNoise0.ar(4, 500, 600);
	SinOsc.ar(freq, 0, 0.1).dup;
}.play;
)

// with:
(
{
	var freq = LFNoise0.ar(4, 500, 600);
	SinOsc.ar(freq.lag(0.3), 0, 0.1).dup;
}.play;
)

// /////////////////////////
// Filter UGENS
// \\\\\\\\\\\\\\\\\\\\\\\\\

~filepath = Platform.resourceDir +/+ "sounds/a11wlk01.wav";
~sndbuf = Buffer.read(s, ~filepath);

/* -------------------------
   Low pass filter
   ------------------------- */
(
x = {
	var freq = MouseX.kr(100, 20e3, 1);
	LPF.ar(PlayBuf.ar(1,~sndbuf.bufnum,loop:1), freq, 0.75).dup;
}.play;
)
x.free;

/* -------------------------
   High pass filter
   ------------------------- */
(
x = {
	var freq = MouseX.kr(100, 20e3, 1);
	HPF.ar(PlayBuf.ar(1,~sndbuf.bufnum,loop:1), freq, 0.75).dup;
}.play;
)
x.free;

/* -------------------------
   Band pass filter
   ------------------------- */
(
x = {
	var freq = MouseX.kr(100, 20e3, 1);
	var rq = MouseY.kr(1e-4,1);
	BPF.ar(PlayBuf.ar(1,~sndbuf.bufnum,loop:1), freq, rq).dup;
}.play;
)
x.free;

/* -----------------------------
   Resonant filters
   ---------------------------- */
(
x = {
	var src = WhiteNoise.ar(0.01);
	var freq = MouseX.kr(100, 20e3, 1);
	Ringz.ar(src, freq, 0.1).dup;
}.play;
)

// "Playing" noise
(
fork{
	~scale = Scale.major.degrees;
	~octave = [4,5,6];
	inf.do{
		var freq = 12*~octave.choose + ~scale.choose;
		{
			var amp_env = EnvGen.ar(Env.perc(1e-6,1),doneAction:2);
			var noise = WhiteNoise.ar(1e-2) * amp_env;
			var out = Ringz.ar(noise, freq.midicps, 2);
			out.dup;
		}.play;
		0.125.wait;
	};
}
)

// See UGENS > Filters in the help docks for many more including RLPF, RHPF, ...


// /////////////////////////
// Effects UGENS
// \\\\\\\\\\\\\\\\\\\\\\\\\

/* -----------------
   Delay
   ----------------- */

// Feedforwad Delay
(
{
	var trig = Impulse.ar(0);
	var env = Decay.ar(trig);
	var out = SinOsc.ar(300,0,0.1)*env;
	var delay = DelayC.ar(out, 0.5, 0.5, 0.5);
	out = out + delay;
	DetectSilence.ar(out, doneAction:2);
	out.dup;
}.play;
)

// Feedback Delay
(
x = {
	var feeback=0.9;

	var trig = Impulse.ar(0);
	var env = Decay.ar(trig);
	var out = SinOsc.ar(300,0,0.1)*env;

	out = out + LocalIn.ar(1);

	LocalOut.ar(DelayC.ar(out, 0.5, 0.125, feeback));

	DetectSilence.ar(out, doneAction:2);
	out.dup;
}.play;
)

/* ----------------
   Distortion
   ---------------- */

// clipping function
(
var x = (-5,-4.95..5);
	[
		x.tanh,
		x.softclip
	].plot;
)

// Ex1.
(
x = { |dist_amount=0|
	var amp_in  = dist_amount.linlin(0,100,0.5,5);
	var amp_out = (amp_in + 1).reciprocal;

	var out = SinOsc.ar(300!2) * amp_in;
	out.tanh * amp_out;
}.play;
)
x.set(\dist_amount, 100);
x.free;

// Ex2. This time on a buffer
~filepath = Platform.resourceDir +/+ "sounds/a11wlk01.wav";
~sndbuf = Buffer.read(s, ~filepath);

(
x = {
	|dist_amount=0|
	var amp_in  = dist_amount.linlin(0,100,0.5,50);
	var amp_out = (amp_in + 1).reciprocal;

	var out = PlayBuf.ar(1, ~sndbuf.bufnum,1,loop:1).dup * amp_in;
	out.tanh * amp_out;

}.play;
)
x.set(\dist_amount, 50);
x.set(\dist_amount, 100);
x.free;

/* -------------
   Waveshaping
   ------------- */

// folding
(
{
	var a = SinOsc.ar(400);
	[
		a,
		a.fold(-0.9,0.9),
		a.fold(-0.6,0.6)
	]
}.plot;
)

// wrapping
(
{
	var a = SinOsc.ar(400);
	[
		a,
		a.wrap(-0.9,0.9),
		a.wrap(-0.6,0.6)
	]
}.plot;
)

// using one waveform to control another
(
{
	var a = SinOsc.ar(400);
	var b = LFSaw.ar(100);
	[
		a,
		b,
		a>b,
		a.min(b),
		a%b
	]
}.plot;
)

(
{
	var a = SinOsc.ar(300);
	var b = LFSaw.ar(200 + SinOsc.ar(0.125,0,200),1);
	//a>b;
	//a.min(b);
	a.abs>b*a;
}.play;
)

// The Shaper UGEN
b = Buffer.alloc(s, 512, 1, {|buf| buf.chebyMsg([1,0,1,1,0,1])});
b.plot

(
{
    Shaper.ar(
        b,
        SinOsc.ar(300, 0, Line.kr(0,1,6)),
        0.5
    )
}.play;
)

b.free;


// /////////////////////////
// Spatialization
// \\\\\\\\\\\\\\\\\\\\\\\\\

// Stereo Panning
(
{Pan2.ar(SinOsc.ar(400),MouseX.kr(-1,1))}.play;
)

// Mixdown
(
{Mix(SinOsc.ar(400*(1..10)))}.plot
)

(
{(SinOsc.ar(400*(1..10))).mean}.plot
)

// Splay
(
{Splay.ar(SinOsc.ar(400*(1..10)))}.plot
)


// /////////////////////////
// Control/Interaction
// \\\\\\\\\\\\\\\\\\\\\\\\\

MouseX, MouseY // we have seen these now

// Select
(
{
	//var cnt = MouseX.kr(0,4);
	var ctl = LFNoise2.ar(0.5,0.5,0.5)*4;
	SelectX.ar(ctl,
		[
			a = SinOsc.ar(200),
			b = LFSaw.ar(150),
			a>b,
			a%b
	]).dup;
}.play;
)

/* -------------------
   Demand/Duty UGENS
   ------------------- */

// Duty, Dseq, Drand
(
{
	var seq = Dseq(400*[1/2,3/4,2,3/2,3,4/3], inf);
	var dur = Drand(0.125*[1/2,3/4,2,3/2,3,4/3], inf);

	var freq = Duty.ar(dur,0,seq);
	SinOsc.ar(freq,0,0.2).dup;
}.play;
)

// Demand
(
{
	var seq = Dseq(400*[1/2,3/4, Dstutter(4, Drand([1/2,1,2,3,4,5,6],1)), 2,3/2,3,4/3], inf);
	var trig = Impulse.ar(8);//Dust.ar(10);

	var freq = Demand.ar(trig,0,seq);
	SinOsc.ar(freq,0,0.2).dup;
}.play;
)

// For fun...
(
f = { |f0=200, rate=4|
	{
		var seq = Dseq(f0*[1/2,3/4, Dstutter(4, Drand([1/2,1,2,3,4,5,6],1)), 2,3/2,3,4/3], inf);
		var trig = Impulse.ar(rate);
		var freq = Demand.ar(trig,0, seq);
		var env = Decay2.ar(trig,1e-3,1);
		FreeVerb.ar(
			Splay.ar(SinOsc.ar(freq*[0.995, 1, 1.005]).fold(-0.999,0.999)*0.25*env),
			0.5,
			0.5
		);
	}
};

fork{ 5.do{|i|
	fork{
		f.(100,2).play;
		(1/8).wait;
		f.(200,4).play;
		(1/4).wait;
		f.(100*4/3,8).play;
		(1/2).wait;
		f.(400,4).play;
	};
	(0.5/(2**i)).wait;
}};
)

