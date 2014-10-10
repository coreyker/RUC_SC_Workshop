/* -------------------------
   Brightest pixel detector 
   ------------------------- */
import oscP5.*;
import netP5.*;
import processing.video.*;

OscP5 oscP5;
NetAddress netaddr;
OscMessage msg;

Movie video;

int vidx = 1280;
int vidy = 720;

PVector bright_loc, last;

void setup() {
  
  size(vidx, vidy);
  
  //frameRate(15);
  
  // set osc send receive ports
  oscP5 = new OscP5(this,12000);
  netaddr = new NetAddress("127.0.0.1",57120);
  
  
  video = new Movie(this, "/Users/corey/Downloads/Sunset.mp4"); // !!! CHANGE !!! 
  video.loop();
  video.volume(0);
  
  bright_loc = new PVector(0,0);
}

void draw() {
  
  if( video.available() ) {
    video.read();
  }
      
  // find brightest pixel  
  PVector b =  findBrightestBlobCentroid( video );
  
  if (frameCount==1) {
    bright_loc = b;
  }
  
  // smoothing
  float alpha = 0.95;
  bright_loc.x = alpha * bright_loc.x + (1-alpha) * b.x;  
  bright_loc.y = alpha * bright_loc.y + (1-alpha) * b.y;
  
  
  image(video, 0, 0);    
  noStroke();
  fill(0,100);
  ellipse(bright_loc.x, bright_loc.y, 25, 25);
  
  // send OSC (open sound control) message
  msg = new OscMessage("/brightest_pixel");
  msg.add( bright_loc.x );  
  msg.add( bright_loc.y );        
  oscP5.send(msg, netaddr);      
}

PVector findBrightestBlobCentroid( PImage img ) {
  
  img.loadPixels();
  
  int K = 100;
  int step = 10;
  int pos = 0;
  
  PVector [] locs = new PVector[K];
  float [] levels = new float [K];  
  
  for (int k=0; k<K; k++) {
    locs[k] = new PVector(0,0);
  }      
  
  for (int i=0; i<img.width; i+=step) {
    for (int j=0; j<img.height; j+=step) {
      
      int index = j*width + i;
            
      float b = brightness( img.pixels[index] );
      
      for (int k=0; k<K; k++ ) {
        
        if (b > levels[pos % K]) {
          levels[pos % K] = b;
          locs[pos % K].x = i;
          locs[pos % K].y = j;

          break;
        }      
        pos += 1;
      }
      
    }
  }
  
  float x=0, y=0;
  for (int k=0; k<K; k++) {
    x += locs[k].x;
    y += locs[k].y;
  }
  
  return new PVector( x/K, y/K );
}
