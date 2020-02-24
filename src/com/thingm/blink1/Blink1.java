/**
 * ThingM blink(1) Java library
 *
 * Copyright 2008-2020, Tod E. Kurt / @todbot
 *
 * To quickly compile:
 *  javac -cp ../lib/jna-5.5.0.jar:../lib/purejavahidapi.jar:. com/thingm/blink1/Blink1.java* To run:
 *  java -cp ../lib/jna-5.5.0.jar:../lib/purejavahidapi.jar:. com.thingm.blink1.Blink1
 * 
 * Must use Java 13 (openjdk on Mac) because of purejavahidapi
 *
 */

package com.thingm.blink1;

import java.util.*;
import java.awt.Color;

import purejavahidapi.*;

public class Blink1 
{

  // static class variables
  static int vendorId = 0x27B8;
  static int productId = 0x01ed;
  static byte reportId = 0x01;
  
  /**
   * List of all currently connected blink(1) devices.
   * Populated by enumerate()
   */
  static List<HidDeviceInfo> blink1DevList = new ArrayList<HidDeviceInfo>();

  // member variables
  HidDevice dev = null;
  

  public static void usage() { 
    System.out.println("Usage: Blink1 <cmd> [options]");
  }


  //--------------------------------------------------------------------------

  /**
   * Constructor.  Normally not used.  
   * Use static methods Blink1.open...() to get a Blink1 object
   */
  protected Blink1(HidDevice dev) {
    this.dev = dev;
  }

  public boolean error() {
    //System.out.println("errorCode: "+errorCode);
    //return (errorCode < 0);
    return false;
  }

  /**
   * (re)Enumerate the bus and return a count of blink(1) device found.
   * @returns blink1_command response code, -1 == fail 
   */
  public static int enumerate() {
    blink1DevList = new ArrayList<HidDeviceInfo>();
    try {
      List<HidDeviceInfo> devList = PureJavaHidApi.enumerateDevices();
      for (HidDeviceInfo info : devList) {
        if (info.getVendorId() == (short) vendorId &&
            info.getProductId() == (short) productId) {
          blink1DevList.add(info);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    //Collections.sort(blink1DevList, (d1, d2) -> {
    //    return d2.getSerialNumberString() - d1.getSerialNumberString();
    //  });
    
    return getCount();
  }
  
  /**
   * Get a count of blink(1) devices that have been enumerated.
   *
   */
  public static int getCount() {
    return blink1DevList.size();
  }
  
  /**
   * Return the list of blink(1) device paths found by enumerate.
   *
   * @returns array of device paths
   */
  public static List<String> getDevicePaths() {
    List<String> paths = new ArrayList<String>();
    for (HidDeviceInfo dev : blink1DevList) {
      paths.add( dev.getPath() );
    }
    return paths;
  }
  
  /**
   * Return the list of blink(1) device serials found by enumerate.
   *
   * @returns array of device serials
   */
  public static List<String> getDeviceSerials() {
    List<String> serials = new ArrayList<String>();
    for (HidDeviceInfo dev : blink1DevList) {
      serials.add( dev.getSerialNumberString() );
    }
    return serials;
  }

  /**
   * Open blink(1) device by blink(1) numerical id (0-getCount()).
   * Id list is ordered by serial number.
   * @returns Blink1 object or NULL if no device with that id found
   */
  public static Blink1 openById( int id ) {
    Blink1 blink1 = null;
    try { 
      HidDeviceInfo devInfo = blink1DevList.get(id);
      final HidDevice dev = PureJavaHidApi.openDevice(devInfo);
      blink1 = new Blink1(dev);
    } catch(Exception e) {
    }
    return blink1;
  }
  
  /**
   * Open the first (or only) blink(1) device.
   */
  public static Blink1 openFirst() {
    return openById(0);
  }    

  /**
   * Open the first (or only) blink(1) device.  
   * Causes an enumerate to happen.
   * Stores open device id statically in native lib.
   *
   * @returns Blink1 object or null if no blink(1) device
   */
  public static Blink1 open() {
    return openFirst();
  }

  /**
   * Close blink(1) device. 
   */
  public void close() {
    this.dev.close();
  }

  /**
   * Open blink(1) device by USB path, may be different for each insertion.
   *
   * @returns Blink1 object or NULL if no device with that path found
   */
  public static Blink1 openByPath( String devicepath ) {
    
    return null;
  }
  
  /**
   * Open blink(1) device by blink(1) serial number.
   *
   * @returns Blink1 object or NULL if no device with that serial found
   */
  public static Blink1 openBySerial( String serialnumber ) {
    return null;
    
  }

  /**
   * A small abstraction for dev.setFeatureReport()
   * Currently purejavahidapi has inconsistent behavior
   * between Mac & Windows for the newer setFeatureReport(reportId, buffer, length);
   * method.  This method uses the older (but working) deprecated method 
   * with a supresswarning override.
   */
  @SuppressWarnings("deprecation")
  public int setFeatureReport(byte[] buffer, int len) {
    //return this.dev.setFeatureReport(buffer[0],buffer,len);
    return this.dev.setFeatureReport(buffer,len);
  }
  
  /**
   * A small abstraction for dev.getFeatureReport()
   */
  public int getFeatureReport(byte[] buffer, int len) {
    return this.dev.getFeatureReport(buffer, len);
  }
  
  /**
   * Set blink(1) RGB color immediately.
   *
   * @param r red component 0-255
   * @param g green component 0-255
   * @param b blue component 0-255
   * @returns blink1_command response code, -1 == fail 
   */
  public int setRGB(int r, int g, int b) {
    byte[] buff = {reportId, (byte)'n', (byte)r, (byte)g, (byte)b, 0, 0, 0, 0};
    int rc = this.setFeatureReport(buff, buff.length);
    return rc;
  }

  /**
   * Set blink(1) RGB color immediately.
   *
   * @param c Color to set
   * @returns blink1_command response code, -1 == fail 
   */
  public int setRGB(Color c) {
    return setRGB( c.getRed(), c.getGreen(), c.getBlue() );
  }

  /**
   * Fade blink(1) to RGB color over fadeMillis milliseconds.
   *
   * @param fadeMillis milliseconds to take to get to color
   * @param r red component 0-255
   * @param g green component 0-255
   * @param b blue component 0-255
   * @returns blink1_command response code, -1 == fail 
   */
  public int fadeToRGB(int fadeMillis, int r, int g, int b ) {
    return this.fadeToRGB(fadeMillis,r,g,b,0);
  }
  
  /**
   * Fade blink(1) to RGB color over fadeMillis milliseconds.
   *
   * @param fadeMillis milliseconds to take to get to color
   * @param r red component 0-255
   * @param g green component 0-255
   * @param b blue component 0-255
   * @param ledn which LED to address (0=all)
   * @returns blink1_command response code, -1 == fail 
   */
  public int fadeToRGB(int fadeMillis, int r, int g, int b, int ledn) {
    int dms = fadeMillis/10;
    byte th = (byte)(dms >> 8);
    byte tl = (byte)(dms & 0xff);
    byte[] buff = {reportId, (byte)'c', (byte)r, (byte)g, (byte)b, th,tl, (byte)ledn, 0};
    int rc = this.setFeatureReport(buff, buff.length);
    return rc;
  }
  
  /**
   * Fade blink(1) to RGB color over fadeMillis milliseconds.
   *
   * @param fadeMillis milliseconds to take to get to color
   * @param c Color to set
   * @returns blink1_command response code, -1 == fail 
   */
  public int fadeToRGB(int fadeMillis, Color c) {
    return fadeToRGB( fadeMillis, c.getRed(), c.getGreen(), c.getBlue() );
  }

  /**
   * Fade blink(1) to RGB color over fadeMillis milliseconds.
   *
   * @param fadeMillis milliseconds to take to get to color
   * @param c Color to set
   * @param ledn which LED to address (0=all)
   * @returns blink1_command response code, -1 == fail 
   */
  public int fadeToRGB(int fadeMillis, Color c, int ledn) {
    return fadeToRGB( fadeMillis, c.getRed(), c.getGreen(), c.getBlue(), ledn );
  }

  /**
   * Write a blink(1) light pattern entry.
   *
   * @param fadeMillis milliseconds to take to get to color
   * @param r red component 0-255
   * @param g green component 0-255
   * @param b blue component 0-255
   * @param pos entry position 0-patt_max
   * @returns blink1_command response code, -1 == fail 
   */
  public int writePatternLine(int fadeMillis, int r, int g, int b, int pos) {
    return -1;
  }
  
  /**
   * Write a blink(1) light pattern entry.
   *
   * @param fadeMillis milliseconds to take to get to color
   * @param c Color to set
   * @param pos entry position 0-patt_max
   * @returns blink1_command response code, -1 == fail 
   */
  public int writePatternLine(int fadeMillis, Color c, int pos) {
    return writePatternLine(fadeMillis, c.getRed(), c.getGreen(), c.getBlue(), pos);
  }

  /**
   * Play a color pattern.
   *
   * @param play  true to play, false to stop
   * @param pos   starting position to play from, 0 = start
   * @returns blink1_command response code, -1 == fail 
   */
  public int play( boolean play, int pos) {
    return -1;
  }

  /**
   * Enable or disable serverdown / servertickle mode
   * @param on true = turn on serverdown mode, false = turn it off
   * @param millis milliseconds until light pattern plays if not updated 
   * @returns blink1_command response code, -1 == fail 
   */
  public int serverdown( boolean on, int millis) {
    return -1;
    
  }

  /** 
   * Get version of firmware code in blink(1) device.
   * @returns blink1 version number as int (e.g. v1.0 == 100, v2.0 = 200)
   */
  public int getFirmwareVersion() {
    byte [] buff = { 1, 'v', 0,0,0,0,0,0,0 };
    this.setFeatureReport(buff, buff.length);
    this.getFeatureReport(buff, buff.length);
    System.out.println("BLINK1:getVersion:"+Arrays.toString((buff)));
    int vh = Character.getNumericValue(buff[3]);
    int vl = Character.getNumericValue(buff[4]);
    int ver = (vh*100) + vl;
    return ver;
  }

  /**
   * @return serial number string of this blink(1)
   */
  public String getSerial() {
    return this.dev.getHidDeviceInfo().getSerialNumberString();
  }

  //-------------------------------------------------------------------------
  // Utilty Class methods
  //-------------------------------------------------------------------------

  /**
   * one attempt at a degamma curve.
   * //FIXME: this is now in blink1-lib
   */
  public static final int log2lin( int n ) {
    //return  (int)(1.0* (n * 0.707 ));  // 1/sqrt(2)
    return (((1<<(n/32))-1) + ((1<<(n/32))*((n%32)+1)+15)/32);
  }

  /**
   * Utility: A simple delay
   */
  public static final void pause(int millis) {
    try {
        Thread.sleep(millis);
    } catch (Exception e) {
    }
  }



    /**
   * Simple command-line demonstration
   */
  public static void main(String args[]) {
    
    int rc;

    if( args.length == 0 ) {
      //usage();
    }
    
    System.out.println("Looking for blink(1) devices...");
    Blink1.enumerate(); 
    int count = Blink1.getCount();

    System.out.println("found "+count+ ((count==1) ? " device":" devices"));

    if( count == 0 ) {
      System.out.println("no devices found, would normally exit. continuing for error testing");
    }

    System.out.println("id : serial : path:");
    List<String> paths = Blink1.getDevicePaths();
    List<String> serials = Blink1.getDeviceSerials();
    for( int i=0; i<paths.size(); i++ ) { 
      System.out.println( i + " : "+ serials.get(i) + " : " + paths.get(i));
    }
    
    System.out.println("Opening deviceId 0");

    Blink1 blink1 = Blink1.open();

    if( blink1 == null ) { 
      System.out.println("error on open(), no blink(1), next call will return error ");
    }

    System.out.print("fading to 10,20,30 ");
    rc = blink1.fadeToRGB( 100, 10,20,30 );
    System.out.println(" -- rc = "+rc);

    int ver = blink1.getFirmwareVersion();
    System.out.println("firmware version: " + ver);

    blink1.close();

    if( serials.size() >= 2 ) {
      String serialA  = serials.get(0);
      String serialB  = serials.get(1);
      System.out.println("opening two devices: "+serialA+" and "+serialB);
      Blink1 blink1A = Blink1.openBySerial( serialA );
      Blink1 blink1B = Blink1.openBySerial( serialB );
      System.out.println("fading "+ serialA + " to red"); 
      blink1A.fadeToRGB( 100, 255,0,0 );
      Blink1.pause(500);
      System.out.println("fading "+ serialB + " to green");
      blink1B.fadeToRGB( 100, 0,255,0 );
      Blink1.pause(500);
      System.out.println("fading "+ serialA + " to blue"); 
      blink1A.fadeToRGB( 100, 0,0,255 );
      Blink1.pause(500);
      System.out.println("fading "+ serialB + " to purple"); 
      blink1B.fadeToRGB( 100, 255,0,255 );
      Blink1.pause(500);
      blink1A.close();
      blink1B.close();
      Blink1.pause(500);
    }

    Random rand = new Random();
    for( int i=0; i<10; i++ ) {
      int r = rand.nextInt() & 0xFF;
      int g = rand.nextInt() & 0xFF;
      int b = rand.nextInt() & 0xFF;
      
      int id = (count==0) ? 0 : rand.nextInt() & (count-1);
      
      System.out.print("setting device "+id+" to color "+r+","+g+","+b+"   ");

      blink1 = Blink1.openById( id );
      if( blink1.error() ) { 
        System.out.print("couldn't open "+id+" ");
      }
      
      // can do r,g,b ints or a single Color
      //rc = blink1.setRGB( r,g,b );
      Color c = new Color( r,g,b );
      rc = blink1.setRGB( c );
      if( rc == -1 ) 
        System.out.println("error detected");
      else 
        System.out.println();
      
      blink1.close();
      
      Blink1.pause( 300 );
    }

    System.out.println("Turn off all blink(1)s.");
    for(int n=0; n < count; n++){
      blink1 = Blink1.openById(n);
      blink1.setRGB(Color.BLACK);
      blink1.close();
    }

    System.out.println("Done.");
  }

}
