import processing.video.*;
import oscP5.*;
import netP5.*;

OscP5 oscP5;
NetAddress netaddr;
OscMessage msg;

Movie video;

int vidx = 640;
int vidy = 360;

int nrows = 4;
int ncols = 8;

int superwidth = vidx / ncols;
int superheight = vidy / nrows;
  
PImage superpixel;

void setup() {
  
  size(vidx, vidy+vidy);
  
  // set osc send receive ports
  oscP5 = new OscP5(this,12000);
  netaddr = new NetAddress("127.0.0.1",57120);
  
  // load movie
  /*
  video = new Movie(this, "/Users/corey/Downloads/aurora.mov");  
  */
  video = new Movie(this, "/Users/corey/Downloads/fireworks_silent.mov");
  
  video.loop();
  
  superpixel = createImage(superwidth,superheight,RGB);
}

void draw() { 
  
  frameRate(15);
  
  background(0); 
  
  image(video, 0, 0);
  

  // average superpixels
  for (int i=0; i<nrows; i++) {
    for (int j=0; j<ncols; j++) {
      
      int y = i*superheight;
      int x = j*superwidth;
      superpixel.copy(video,x,y,superheight,superwidth,0,0,superheight,superwidth);
      
      color ave = calcAve( superpixel );     
      fill(ave);
      rect(x,y + vidy,superwidth, superheight);
      
      // send osc message
      msg = new OscMessage("/superpixel");
      msg.add(i*ncols+j);       
      msg.add(hue(ave));  
      msg.add(saturation(ave));      
      msg.add(brightness(ave));
      oscP5.send(msg, netaddr);      
    }
  }


}

// Called every time a new frame is available to read
void movieEvent(Movie m) {    
  m.read();
}

// calculate the average color of an image
color calcAve( PImage img ) {
  int npixels = img.width * img.height;
  img.loadPixels();
  
  float rave = 0;
  float gave = 0;
  float bave = 0;    
  
  for (int i=0; i<npixels; i++ ) {
    rave += red(img.pixels[i])/float(npixels);
    gave += green(img.pixels[i])/float(npixels);
    bave += blue(img.pixels[i])/float(npixels);       
  }
  return color(rave, gave, bave);  
}
