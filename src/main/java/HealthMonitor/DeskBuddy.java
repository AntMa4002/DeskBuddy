package HealthMonitor;
//firmata4j imports
import org.firmata4j.I2CDevice;
import org.firmata4j.IOEvent;
import org.firmata4j.Pin;
import org.firmata4j.PinEventListener;
import org.firmata4j.firmata.FirmataDevice;
import org.firmata4j.ssd1306.SSD1306;

//IOException import
import java.io.IOException;

//ArrayList import
import java.util.ArrayList;

//Timer import
import java.util.Timer;

//StdDraw import
import edu.princeton.cs.introcs.StdDraw;
public class DeskBuddy
{
    //private static statements for the class
    public static void main(String[] args) throws IOException, InterruptedException
    {
        //Firmata object initialization
        var device = new FirmataDevice("COM3");
        try
        {
            //Arduino Startup
            device.start();
            System.out.println("Device started.");
            device.ensureInitializationIsDone();
        }
        catch (Exception ex)
        {
            //error code
            System.out.println("Device did not connect.");
        }
        finally
        {
            //number of sample counters for the two for loops
            int Lsamples = 1;
            int Ssamples = 1;

            //LED Initialization
            var LEDObject = device.getPin(Pins.D4);
            LEDObject.setMode(Pin.Mode.OUTPUT);

            //Potentiometer Initialization
            var PotentioObject = device.getPin(Pins.A0);
            PotentioObject.setMode(Pin.Mode.ANALOG);

            //Light Sensor Initialization
            var LightObject = device.getPin(Pins.A6);
            LightObject.setMode(Pin.Mode.ANALOG);

            //Sound Sensor Initialization
            var SoundObject = device.getPin(Pins.A2);
            SoundObject.setMode(Pin.Mode.ANALOG);

            //Button Initialization
            var button = device.getPin(Pins.D6);
            button.setMode(Pin.Mode.INPUT);

            //OLED screen Initialization
            I2CDevice i2cObject = device.getI2CDevice((byte) 0x3C);
            SSD1306 OledObject = new SSD1306(i2cObject, SSD1306.Size.SSD1306_128_64); // 128x64 OLED SSD1515
            OledObject.init();


            //While loop to run the graph indefinitely
            while (true)
            {
                //ArrayLists for the Light sensor and Sound sensor values
                ArrayList<Integer> LightValues = new ArrayList<>();
                ArrayList<Integer> SoundValues = new ArrayList<>();

                //Window scaling
                StdDraw.setXscale(-20, 140);
                StdDraw.setYscale(-90, 1200);

                //Pen initialization
                StdDraw.setPenRadius(0.005);
                StdDraw.setPenColor(StdDraw.BLACK);

                //X and Y axes
                StdDraw.line(0, 0, 0, 1100);
                StdDraw.line(0, 0, 100, 0);

                //axis labels
                StdDraw.text(50, 1100, "Light and Sound Intensity");
                StdDraw.text(122, -80, "X = Danger lines");
                StdDraw.text(50, -40, "Samples");
                StdDraw.text(-12, 500, "Intensity");
                StdDraw.text(-3, 0, "0");
                StdDraw.text(-7.5, 1100, "1023");
                StdDraw.setPenColor(StdDraw.CYAN);
                StdDraw.line(0, 700, 100, 700);
                StdDraw.text(110, 700, "X Light");
                StdDraw.text(-6, 700, "700");
                StdDraw.setPenColor(StdDraw.MAGENTA);
                StdDraw.line(0, 740, 100, 740);
                StdDraw.text(111, 740, "X Sound");
                StdDraw.text(-6, 740, "740");
                StdDraw.setPenColor(StdDraw.GREEN);
                StdDraw.text(50, 1200, "Desk Buddy Monitoring");

                //Timer code
                Timer timer = new Timer();
                var task = new ScreenTask(OledObject, LightObject, SoundObject, LEDObject, PotentioObject, LightValues, SoundValues);
                timer.schedule(task, 0, 5);

                // sleep method to let the sensors record values into both ArrayLists
                Thread.sleep(20000);
                // cancel the timer for the timer task
                timer.cancel();

                //for loop to iterate through the Light values and graph them
                for (double i : task.getLightValues())
                {
                    if (Lsamples < 100)
                    {
                        StdDraw.setPenColor(StdDraw.CYAN);
                        StdDraw.text(Lsamples, i, "*");
                        Lsamples++;
                        System.out.println("Light: " + Lsamples);
                    }
                }

                //for loop to iterate through the Sound values and graph them
                for (double x : task.getSoundValues())
                {
                    if (Ssamples < 100)
                    {
                        StdDraw.setPenColor(StdDraw.MAGENTA);
                        StdDraw.text(Ssamples, x, "*");
                        Ssamples++;
                        System.out.println("Sound: " + Ssamples);
                    }
                }

                //If statement to check if the samples have gone past the desired amount
                if (Lsamples >= 100 || Ssamples >= 100)
                {
                    //sample numbers are reset and the graph is cleared so that there is space for the next batch of data
                    Lsamples = 0;
                    Ssamples = 0;
                    StdDraw.clear();
                }

                //A pause so that the OLED screen is not overwhelmed
                Thread.sleep(500);
            }
        }

    }
}

