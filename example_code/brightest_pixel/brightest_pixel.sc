OSCdef(\processing,{
	arg ...x;
	var msg = x[0];

	var x_position = msg[1];
	var y_position = msg[2];
	var hue = msg[3];
	var brightness = msg[4];
	var saturation = msg[5];

	[x_position, y_position, hue, brightness, saturation].postln;
}, \brightest_pixel)
