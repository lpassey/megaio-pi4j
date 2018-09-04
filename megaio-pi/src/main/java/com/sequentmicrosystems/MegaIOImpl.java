package com.sequentmicrosystems;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;

import static com.sequentmicrosystems.Constants.*;

/**
 * Created by lpassey on 7/23/18.
 */
public class MegaIOImpl implements MegaIO
{
  private I2CDevice device = null;

  private GpioWrapper gpioWrapper = new GpioWrapper();

  private MegaIOImpl()  {}

  public MegaIOImpl( int cardNum )
      throws IOException, I2CFactory.UnsupportedBusNumberException
  {
    // get the I2C bus to communicate on
    I2CBus i2c = I2CFactory.getInstance( I2CBus.BUS_1 );

    // create an I2C device object for the MegaIO card, typically 0x31
    device = i2c.getDevice( MEGAIO_HW_I2C_BASE_ADD + cardNum );
  }

  String getHardwareVersion()
      throws IOException
  {
    int dev, hwMajor, hwMinor;

    hwMajor = device.read( REVISION_HW_MAJOR_MEM_ADD );
    hwMinor = device.read( REVISION_HW_MINOR_MEM_ADD );
    return String.format( "%d.%d", hwMajor, hwMinor );
  }

  String getFirmwareVersion()
      throws IOException
  {
    int minor, major;

    major = device.read( REVISION_MAJOR_MEM_ADD );
    minor = device.read( REVISION_MINOR_MEM_ADD );
    return String.format( "%d.%d", major, minor );
  }

  /* ------------- RELAYS  -------------- */
  @Override
  public int readRelays()
      throws IOException
  {
    return device.read( RELAY_MEM_ADD );
  }

  @Override
  public boolean isRelayOn( int relayNumber )
      throws IOException
  {
    return readPin( RELAY_MEM_ADD, relayNumber );
  }

  @Override
  public boolean triggerRelay( int relayNumber, boolean on )
      throws IOException
  {
    int retry = RETRY_TIMES;

    while (on != isRelayOn( relayNumber ) && retry > 0)
    {
      try
      {
        device.write( on ? RELAY_ON_MEM_ADD : RELAY_OFF_MEM_ADD, (byte) (relayNumber & 0xFF) );
      }
      catch( IOException ioex )
      {
        if (1 == retry)
        {
          throw ioex;
        }
      }
      gpioWrapper.delay( 5 );
       --retry;
    }
    return retry > 0;
  }

  /* ------------- ADC/DAC  -------------- */
  private int readShort( int address )
      throws IOException
  {
    byte[] buffer = new byte[2];
    device.read( address, buffer, 0, 2 );
    return (buffer[0] << 8) | (buffer[1] & 0xFF );

  }

  @Override
  public int readAnalog( int channel )
      throws IOException
  {
    int address = ADC_VAL_MEM_ADD + 2 * (channel - 1);
    return readShort( address );
  }

  @Override
  public boolean writeAnalog( int analogValue )
      throws IOException
  {
    byte[] byteStreamFromInteger = new byte[2];
    byteStreamFromInteger[1] = (byte) (analogValue & 0xFF);
    byteStreamFromInteger[0] = (byte) ((analogValue >> 8) & 0xF);

    int retry = RETRY_TIMES;
    while (retry > 0)
    {
      try
      {
        device.write( DAC_VAL_H_MEM_ADD, byteStreamFromInteger );
        if (analogValue == readShort( DAC_VAL_H_MEM_ADD ))
        {
          break;
        }
      }
      catch( IOException ioex )
      {
        if ( 1 == retry)
        {
          // we've tried too many times, give up
          throw ioex;
        }
      }
      retry--;
    }
    return retry != 0;
  }

  private boolean readPin( int memoryAddress, int pinNumber )
      throws IOException
  {
    int bitMask = 0x01 << (pinNumber - 1);    // Isolate the one pin whose status interests us.
    int pinValues = device.read( memoryAddress );
    return 0 != (pinValues & bitMask);
  }

  /* ------------- GPIO pins  -------------- */
  @Override
  public boolean isGpioPinOutput( int gpioPinNumber )
      throws IOException
  {
    return readPin( GPIO_DIR_MEM_ADD, gpioPinNumber );
  }

  @Override
  public boolean setGpioPinDirection( int gpioPinNumber, boolean output )
      throws IOException
  {
    // get direction flags for all pins
    int pinDirections = device.read( GPIO_DIR_MEM_ADD );
    int bitMask = 0x01 << (gpioPinNumber - 1);    // Isolate the one pin whose status interests us.

    if (output)
    {
      pinDirections = pinDirections | bitMask;
    }
    else
    {
      pinDirections = pinDirections & ~bitMask;
    }

    device.write( GPIO_DIR_MEM_ADD, (byte) ( 0xff & pinDirections ));
    return readPin( GPIO_DIR_MEM_ADD, gpioPinNumber );
  }

  @Override
  public boolean setGpioPin( int gpioPinNumber, boolean on )
      throws IOException
  {
    if (on)
    {
      device.write( GPIO_SET_MEM_ADD, (byte) gpioPinNumber );
    }
    else
    {
      device.write( GPIO_CLR_MEM_ADD, (byte) gpioPinNumber );
    }
    return on;
  }

  /*
  * doIoIt
  * Read the pending interrupt(s) on XPIO Pin(s)\n");
  ******************************************************************************************
  *
    if (argc == 3)
    {
      val = readReg8(dev,GPIO_IT_FLAGS_MEM_ADD );
      printf("%d\n", val);
    }
    else
    {
      printf( "Invalid command\n");
      exit(1);
    }
  }
  */

  private IrqState setIrq( int pinNumber, IrqState state, int risingMemAddr, int fallingMemAddr )
      throws IOException
  {
    int bitMask = 0x01 << (pinNumber - 1);    // Isolate the one pin whose status interests us.
    int rValRising = device.read( risingMemAddr );
    gpioWrapper.delay( 10 );
    int rValFalling = device.read( fallingMemAddr );
    switch (state)
    {
      case DISABLED:
        rValRising &= ~(bitMask);
        rValFalling &= ~(bitMask);
        break;

      case RISING:
        rValRising |= bitMask;
        rValFalling &= ~(bitMask);
        break;

      case FALLING:
        rValFalling |= bitMask;
        rValRising &= ~(bitMask);
        break;

      case BOTH:
        rValFalling |= bitMask;
        rValRising |= bitMask;
        break;

      default:
        break;
    }
    gpioWrapper.delay( 10 );

    device.write( risingMemAddr, (byte) (0xff & rValRising) );
    gpioWrapper.delay( 10 );
    device.write( fallingMemAddr, (byte) (0xff & rValFalling) );
    return state;
  }

  @Override
  public IrqState setIoIrq( int gpioPinNumber, IrqState state )
      throws IOException
  {
    return setIrq( gpioPinNumber, state, GPIO_EXT_IT_RISING_MEM_ADD, GPIO_EXT_IT_FALLING_MEM_ADD );
  }

  @Override
  public boolean readGpioPin( int gpioPinNumber )
      throws IOException
  {
    return readPin( GPIO_VAL_MEM_ADD, gpioPinNumber );
  }

  /* ------------- OPTICALLY ISOLATED PINS  -------------- */
  @Override
  public boolean readOptoIn( int optoPinNumber )
      throws IOException
  {
    return readPin( OPTO_IN_MEM_ADD, optoPinNumber );
  }

  /*
  * doOptoInIt
  * Read the rise event by opto in pins..
  ******************************************************************************************

  static void doOptoInIt(int argc)
  {
    int dev, val;

    dev = doBoardInit (gHwAdd);
    if(dev <= 0)
    {
      exit(1);
    }
    if (argc == 3)
    {
      val = readReg8(dev,OPTO_IT_FLAGS_MEM_ADD );
      printf("%d\n", val);
    }
    else
    {
      printf( "Invalid command\n");
      exit(1);
    }
  }
  */

  @Override
  public IrqState setOptoIrq( int gpioPinNumber, IrqState state )
      throws IOException
  {
    return setIrq( gpioPinNumber, state, OPTO_IT_RISING_MEM_ADD, OPTO_IT_FALLING_MEM_ADD );
  }

  @Override
  public boolean readOCPin( int ocPinNumber )
      throws IOException
  {
    return readPin( OC_OUT_VAL_MEM_ADD, ocPinNumber );
  }

  @Override
  public boolean setOCPin( int ocPinNumber, boolean on )
      throws IOException
  {
    int retry = RETRY_TIMES;
    boolean setting;
    while (retry > 0)
    {
      try
      {
        if (on)
        {
          device.write( OC_OUT_SET_MEM_ADD, (byte) (ocPinNumber & 0xff) );
        }
        else
        {
          device.write( OC_OUT_CLR_MEM_ADD, (byte) (ocPinNumber & 0xff) );
        }
        setting = readOCPin( ocPinNumber );
        if (on == setting)
        {
          // We got what we wanted, we're good.
          return setting;
        }
        // else try again;
      }
      catch( IOException ioex )
      {
        if (1 == retry)
        {
          // we've tried too many times, give up
          throw ioex;
        }
      }
      retry--;
    }
    throw new IOException( "Too many attempts" );
  }
}
