OSCdef(\processing,{
	arg ...x;
	var msg = x[0];
	var x_position = msg[1];
	var y_position = msg[2];

	[x_position, y_position].postln;
}, \brightest_pixel)
