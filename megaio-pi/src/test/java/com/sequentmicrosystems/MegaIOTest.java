package com.sequentmicrosystems;

import com.pi4j.io.i2c.I2CDevice;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;

import static com.sequentmicrosystems.Constants.*;
import static com.sequentmicrosystems.Constants.IrqState.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by lpassey on 7/23/18.
 */
public class MegaIOTest
{
  @Mock
  I2CDevice device;

  @Mock
  GpioWrapper gpio;   // Declared so @InjectMocks will inject it

  @InjectMocks
  MegaIOImpl mega;

  @Before
  public void setup()
      throws IOException
  {
    initMocks( this );
    // pin 6 is rising and falling, pin 5 and 3 are just falling, pins 4 and 2 are just rising, pin 1 is disabled.
    when( device.read( GPIO_EXT_IT_RISING_MEM_ADD  )).thenReturn( 0b00101010 );
    when( device.read( GPIO_EXT_IT_FALLING_MEM_ADD )).thenReturn( 0b00110100 );
  }

  @Test
  public void relayStatus()
      throws IOException
  {
    // pretend that relay 2, 4, 5 and 7 are on
    when( device.read( RELAY_MEM_ADD )).thenReturn( 0b01001010 );

    assertTrue( mega.isRelayOn( 4 ));
    assertFalse( mega.isRelayOn( 8 ));
  }

  @Test
  public void triggerRelayOn()
      throws Exception
  {
    when( device.read( RELAY_MEM_ADD )).thenReturn( 0 ).thenReturn( 0x01 );
    assertTrue( mega.triggerRelay( (byte) 1, true ));
  }

  @Test
  public void triggerRelayOffFails()
      throws Exception
  {
    // pretend that all the relays are on.
    when( device.read( RELAY_MEM_ADD )).thenReturn( 0xFF );
    assertFalse( mega.triggerRelay( (byte) 2, false ));
    verify( device, times( RETRY_TIMES) ).write( RELAY_OFF_MEM_ADD, (byte) 2 );
  }

  @Test
  public void readAnalog()
      throws Exception
  {
    int address = ADC_VAL_MEM_ADD + 6;
    byte[] buffer = new byte[2];

    doAnswer( invocationOnMock ->
    {
      byte[] buffer1 = invocationOnMock.getArgument( 1 );
      buffer1[0] = 12;
      buffer1[1] = -86;
      return null;
    } ).when( device ).read( address, buffer, 0, 2 );
    int voltage = mega.readAnalog( 4 );
    assertEquals( 3242, voltage );
  }

  @Test
  public void writeAnalog()
      throws Exception
  {
    byte[] byteStreamFromInteger = new byte[2];
    byteStreamFromInteger[0] = 12;
    byteStreamFromInteger[1] = -86;
    doNothing().when( device ).write( DAC_VAL_H_MEM_ADD, byteStreamFromInteger );

    doAnswer( invocationOnMock ->
    {
      byte[] buffer1 = invocationOnMock.getArgument( 1 );
      buffer1[0] = 12;
      buffer1[1] = -86;
      return null;
    } ).when( device ).read( DAC_VAL_H_MEM_ADD, new byte[2], 0, 2 );

    mega.writeAnalog( 3242 );
    verify( device ).write( DAC_VAL_H_MEM_ADD, byteStreamFromInteger );
  }

  @Test
  public void getPinDirection()
      throws IOException
  {
    when( device.read( GPIO_DIR_MEM_ADD )).thenReturn( 0xF0 ).thenReturn( 0x0F );
    assertTrue( mega.isGpioPinOutput( 5 ));
    assertFalse( mega.isGpioPinOutput( 5 ));
  }

  @Test
  public void setGpioPinOut()
      throws IOException
  {
    when( device.read( GPIO_DIR_MEM_ADD )).thenReturn( 0x0F ).thenReturn( 0x1F );
    assertTrue( mega.setGpioPinDirection( 5, true ));
    verify( device ).write( GPIO_DIR_MEM_ADD, (byte) 0x1F );
  }

  @Test
  public void setGpioPinIn()
      throws IOException
  {
    when( device.read( GPIO_DIR_MEM_ADD )).thenReturn( 0x3F ).thenReturn( 0x2F );
    assertFalse( mega.setGpioPinDirection( 5, false ));
    verify( device ).write( GPIO_DIR_MEM_ADD, (byte) 0x2F );
  }

  @Test
  public void setGpioPinHigh()
      throws IOException
  {
    mega.setGpioPin( 3, true );
    verify( device ).write( GPIO_SET_MEM_ADD, (byte) 3 );
  }

  @Test
  public void setGpioPinLow()
      throws IOException
  {
    mega.setGpioPin( 4, false );
    verify( device ).write( GPIO_CLR_MEM_ADD, (byte) 4 );
  }

  @Test
  public void readHighGpioPin()
      throws IOException
  {
    when( device.read( GPIO_VAL_MEM_ADD )).thenReturn( 0x0F );
    assertTrue( "Pin 3 should be high", mega.readGpioPin( 3 ));
    verify( device ).read( GPIO_VAL_MEM_ADD );
  }

  @Test
  public void readLowGpioPin()
      throws IOException
  {
    // unmocked methods return 0 by default...
    assertFalse( "Pin 4 should be low", mega.readGpioPin( 4 ));
    verify( device ).read( GPIO_VAL_MEM_ADD );
  }

  @Test
  public void readHighOptoPin()
      throws IOException
  {
    when( device.read( OPTO_IN_MEM_ADD )).thenReturn( 0x0F );
    assertTrue( "Pin 3 should be high", mega.readOptoIn( 3 ));
    verify( device ).read( OPTO_IN_MEM_ADD );
  }

  @Test
  public void readLowOptoPin()
      throws IOException
  {
    // unmocked methods return 0 by default...
    assertFalse( "Pin 4 should be low", mega.readOptoIn( 4 ));
    verify( device ).read( OPTO_IN_MEM_ADD );
  }

  @Test
  public void setIoIrqDisabled()
      throws IOException
  {
    // pin 6 is rising and falling, pin 5 and 3 are just falling, pins 4 and 2 are just rising, pin 1 is disabled.
    mega.setIoIrq( 6, DISABLED );
    verify( device ).write( GPIO_EXT_IT_RISING_MEM_ADD,  (byte) 0b00001010 );
    verify( device ).write( GPIO_EXT_IT_FALLING_MEM_ADD, (byte) 0b00010100 );
  }

  @Test
  public void setIoIrqRising()
      throws IOException
  {
    // pin 6 is rising and falling, pin 5 and 3 are just falling, pins 4 and 2 are just rising, pin 1 is disabled.
    mega.setIoIrq( 5, RISING );
    verify( device ).write( GPIO_EXT_IT_RISING_MEM_ADD,  (byte) 0b00111010 );
    verify( device ).write( GPIO_EXT_IT_FALLING_MEM_ADD, (byte) 0b00100100 );
  }

  @Test
  public void setIoIrqFalling()
      throws IOException
  {
    // pin 6 is rising and falling, pin 5 and 3 are just falling, pins 4 and 2 are just rising, pin 1 is disabled.
    mega.setIoIrq( 2, FALLING );
    verify( device ).write( GPIO_EXT_IT_RISING_MEM_ADD,  (byte) 0b00101000 );
    verify( device ).write( GPIO_EXT_IT_FALLING_MEM_ADD, (byte) 0b00110110 );
  }

  @Test
  public void setIoIrqRisingAndFalling()
      throws IOException
  {
    // pin 6 is rising and falling, pin 5 and 3 are just falling, pins 4 and 2 are just rising, pin 1 is disabled.
    mega.setIoIrq( 1, BOTH );
    verify( device ).write( GPIO_EXT_IT_RISING_MEM_ADD,  (byte) 0b00101011 );
    verify( device ).write( GPIO_EXT_IT_FALLING_MEM_ADD, (byte) 0b00110101 );
  }
}