package com.sequentmicrosystems;

import com.pi4j.io.i2c.I2CDevice;

import java.io.IOException;

import static com.sequentmicrosystems.Constants.*;

/**
 * Created by lpassey on 8/8/18.
 */
public interface MegaIO
{
  I2CDevice device = null;

  /**
   * Reads the status of all relays as a single 8-bit number, where the bit 0 represents the state of relay
   * number 1 and the bit 7 represents the state of relay number 8
   *
   * @return the status of all relays as a single 8-bit number
   * @throws IOException thrown in case byte cannot be read from the i2c device or i2c bus
   */
  int readRelays()
      throws IOException;

  /**
   * Queries a given relay for its state.
   *
   * @param relayNumber the relay number whose state is sought, from 1 to 8
   * @return true if the relay is powered on or false if it is powered off.
   * @throws IOException thrown in case byte cannot be read from the i2c device or i2c bus
   */
  boolean isRelayOn( int relayNumber )
          throws IOException;

  /**
   * Turns a relay on or off. If the relay is already in the desired state, no attempt will be made to change it.
   *
   * @param relayNumber the number of the relay to trigger, between 1 and 8
   * @param on  if true, turn the relay on otherwise turn the relay off
   * @return  true if the relay was successfully triggered, false otherwise
   * @throws IOException  thrown in case byte cannot be written to the i2c device or i2c bus
   */
  boolean triggerRelay( int relayNumber, boolean on )
              throws IOException;

  /**
   * Reads an integer value from the specified analog to digital channel according to the voltage measured,
   * from 0 to 3.3v (which is the maximum allowable voltage on any A2D channel.
   *
   * @param a2dChannel the analog channel to read, from 1 to 8
   * @return a 12-bit integer value from 0 to 4095
   * @throws IOException thrown in case byte cannot be read from the i2c device or i2c bus
   */
  int readAnalog( int a2dChannel )
                  throws IOException;

  /**
   * Writes an integer value, from 0 to 4095, to the digital to analog pin, producing a voltage between from 0 to 3.3v
   *
   * @param analogValue a 12-bit value from 0 to 4095
   * @return true if the value was successfully written
   * @throws IOException thrown in case byte cannot be written to the i2c device or i2c bus
   */
  boolean writeAnalog( int analogValue )
                      throws IOException;

  /**
   * Reads the direction of a GPIO pin.
   *
   * @param gpioPinNumber the pin whose direction to read, from 1 to 6.
   * @return true if the pin is set to produce 3.3v or false if the pin is set to sink that same voltage
   * @throws IOException thrown in case byte cannot be read from the i2c device or i2c bus
   */
  boolean isGpioPinOutput( int gpioPinNumber )
                          throws IOException;

  /**
   * sets a GPIO pin to produce or sink 3.3v
   *
   * @param gpioPinNumber the pin whose direction to set, from 1 to 6.
   * @param output true to set the pin to produce 3.3v or false if the pin is set to sink that same voltage
   * @return the status of the pin after being set
   * @throws IOException  thrown in case byte cannot be written to the i2c device or i2c bus
   */
  boolean setGpioPinDirection( int gpioPinNumber, boolean output )
                              throws IOException;

  /**
   * turns a GPIO pin that has been set to output mode on, or off
   *
   * @param gpioPinNumber the pin to set, from 1 to 6.
   * @param on true if the pin should be set to 3.3v or false if the pin should be 0v.
   * @return the status of the pin, on or off
   * @throws IOException thrown in case byte cannot be written to the i2c device or i2c bus
   */
  boolean setGpioPin( int gpioPinNumber, boolean on )
      throws IOException;

  /**
   *
   * @param gpioPinNumber the GPIO pin to set the state on, from 1 to 6.
   * @param state the desired interrupt request state of the pin: DISABLED, RISING, FALLING, or BOTH
   * @return the new state of the pin
   * @throws IOException thrown in case byte cannot be written to the i2c device or i2c bus
   */
  Constants.IrqState setIoIrq( int gpioPinNumber, IrqState state )
      throws IOException;


  /**
   * reads the value of a GPIO pin that has been set to input mode.
   *
   * @param gpioPinNumber the pin whose value to read, from 1 to 6.
   * @return true if a voltage is present on the specified pin or false if there is no voltage
   */
  boolean readGpioPin(  int gpioPinNumber )
      throws IOException;

  /**
   *
   * @param optoPinNumber  the OptoCoupled pin whose value to read, from 1 to 8.
   * @return true if a voltage is present on the specified pin or false if there is no voltage
   */
  boolean readOptoIn( int optoPinNumber )
      throws IOException;

  /**
   *
   * @param gpioPinNumber
   * @param state
   * @return
   * @throws IOException thrown in case byte cannot be written to the i2c device or i2c bus
   */
  IrqState setOptoIrq( int gpioPinNumber, IrqState state )
      throws IOException;

  /**
   *
   * @param ocPinNumber
   * @return
   * @throws IOException thrown in case byte cannot be read from the i2c device or i2c bus
   */
  boolean readOCPin( int ocPinNumber )
      throws IOException;

  /**
   *
   * @param ocPinNumber
   * @param on
   * @return
   * @throws IOException thrown in case byte cannot be written to the i2c device or i2c bus
   */
  boolean setOCPin( int ocPinNumber, boolean on )
      throws IOException;
}
