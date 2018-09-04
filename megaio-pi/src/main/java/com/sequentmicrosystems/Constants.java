package com.sequentmicrosystems;

/**
 * Created by lpassey on 7/23/18.
 */
public class Constants
{
 public static final int RETRY_TIMES = 10;

 public static final int RELAY_MEM_ADD = 0x00;
 public static final int RELAY_ON_MEM_ADD = 0x01;
 public static final int RELAY_OFF_MEM_ADD = 0x02;
 public static final byte OPTO_IN_MEM_ADD = 0x03;
 public static final byte OC_OUT_VAL_MEM_ADD = 0x04;
 public static final byte OC_OUT_SET_MEM_ADD = 0x05;
 public static final byte OC_OUT_CLR_MEM_ADD = 0x06;

 public static final byte ADC_VAL_MEM_ADD = 0x07;
 public static final byte DAC_VAL_H_MEM_ADD = 0x17;
 public static final byte DAC_VAL_L_MEM_ADD = 0x18;
 public static final byte GPIO_VAL_MEM_ADD = 0x19;
 public static final byte GPIO_SET_MEM_ADD = 0x1a;
 public static final byte GPIO_CLR_MEM_ADD = 0x1b;
 public static final byte GPIO_DIR_MEM_ADD = 0x1c;
 public static final byte OPTO_IT_RISING_MEM_ADD = 0x1d; // 1B
 public static final byte OPTO_IT_FALLING_MEM_ADD = 0x1e; // 1B
 public static final byte GPIO_EXT_IT_RISING_MEM_ADD = 0x1f; // 1B
 public static final byte GPIO_EXT_IT_FALLING_MEM_ADD = 0x20; // 1B
 public static final byte OPTO_IT_FLAGS_MEM_ADD = 0x21; // 1B
 public static final byte GPIO_IT_FLAGS_MEM_ADD = 0x22; // 1B

 public static final byte REVISION_HW_MAJOR_MEM_ADD = 0x3c;
 public static final byte REVISION_HW_MINOR_MEM_ADD = 0x3d;
 public static final byte REVISION_MAJOR_MEM_ADD = 0x3e;
 public static final byte REVISION_MINOR_MEM_ADD = 0x3f;

 public static final int GPIO_PIN_NUMBER = 6;

 public static final int ERROR = -1;
 public static final int FAIL = 0;
 public static final int OK = 1;

 public static final int CHANNEL_NR_MIN = 1;
 public static final int RELAY_CH_NR_MAX = 8;
 public static final int ADC_CH_NR_MAX = 8;
 public static final int OPTO_CH_NR_MAX = 8;
 public static final int OC_CH_NR_MAX = 4;
 public static final int GPIO_CH_NR_MAX = 6;

 public static final int ANALOG_VAL_MIN = 0;
 public static final int ANALOG_VAL_MAX = 4095;
 public static final int ANALOG_ERR_THRESHOLD = 100;

 public static final int COUNT_KEY = 0;
 public static final int YES = 1;
 public static final int NO = 2;

 public static final byte MEGAIO_HW_I2C_BASE_ADD = 0x31;

 enum IrqState
 {
  DISABLED,
  RISING,
  FALLING,
  BOTH;
 }
}
