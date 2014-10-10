{
	var in, amp, freq, hasFreq;
	in = HPF.ar(LPF.ar(SoundIn.ar(0),500),80);
	amp = LPF.kr(Amplitude.kr(in),2);
	# freq, hasFreq = Pitch.kr(in);
	freq = LPF.kr(freq,2);

	SendTrig.kr(Impulse.kr(60), 0, amp);
	SendTrig.kr(Impulse.kr(60), 1,(amp>1e-2) * freq);

}.play;

OSCdef(\ampTracker, {
	arg ... x;
	var id = x[0][2];
	//x.postln;

	if( id==0, {~amp = x[0][3]});
	if( id==1, {~freq = x[0][3]});

}, \tr, s.addr);


(
w = Window.new(\ampWin, Rect(200,200,500,500));
u = UserView.new(w, Rect(0,0,w.bounds.width,w.bounds.height));
u.background_(Color.red(0.2))
.drawFunc_({
	var diameter = ~amp * 1e3;
	var radius = diameter / 2;
	var x = w.bounds.width/2 - radius;
	var y = w.bounds.height - radius - (500*~freq.linlin(0,500,0,1));

	[~freq, ~amp].postln;

	Pen.width = 2;
	Pen.strokeColor = Color.gray(0.75);
	Pen.strokeOval(Rect(x,y,diameter,diameter));
})
.animate_(true);
w.front;
)


(0,0.1..1).linexp(0,1,0.0001,1).plot