package com.sequentmicrosystems;

import com.pi4j.wiringpi.Gpio;

/**
 * Created by lpassey on 7/23/18.
 */
public class GpioWrapper
{
  public void delay( long millis )
  {
    Gpio.delay( millis );
  }
}
