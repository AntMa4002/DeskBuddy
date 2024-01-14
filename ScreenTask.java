package HealthMonitor;
//Firmata imports
import	org.firmata4j.Pin;
import	org.firmata4j.ssd1306.SSD1306;

//Exception import
import	java.io.IOException;

//LocalTime import
import java.time.LocalTime;
//DateTimerFormat import
import java.time.format.DateTimeFormatter;

//ArrayList import
import java.util.ArrayList;

//TimerTask import
import	java.util.TimerTask;


public class ScreenTask extends TimerTask //This class extends the TimerTask class
{
    //Private variables to be used in the constructor and methods
    private	final SSD1306 display;
    private	final	Pin	Light;
    private final Pin Sound;
    private final Pin LED;
    private final Pin Potentio;
    private LocalTime CurrentTime;
    private ArrayList<Integer> LightVal;
    private ArrayList<Integer> SoundVal;

    public	ScreenTask(SSD1306	display, Pin Light, Pin Sound, Pin LED, Pin Potentio, ArrayList<Integer> LightVal,ArrayList<Integer> SoundVal) //Constructor
    {
        //this statements
        this.display = display;
        this.Light = Light;
        this.Sound = Sound;
        this.LED = LED;
        this.Potentio = Potentio;
        this.LightVal = LightVal;
        this.SoundVal = SoundVal;
    }

    //ArrayList of light values return method
    public ArrayList<Integer> getLightValues()
    {
        return LightVal;
    }

    //ArrayList of sound values return method
    public ArrayList<Integer> getSoundValues()
    {
        return SoundVal;
    }


    @Override
    public	void	run() //run method
    {
        //Current time code
        CurrentTime = LocalTime.now();
        //Proper formatting of the time to mimic a digital clock
        String time = CurrentTime.format(DateTimeFormatter.ofPattern("hh:mm:ss\na"));

        //Light and Sound sensor value check
        long Lval = Light.getValue();
        long Sval = Sound.getValue();

        //Adding both sensor values into their respective ArrayList
        LightVal.add(Integer.valueOf((int)Lval));
        SoundVal.add(Integer.valueOf((int)Sval));

        //Conversion of long to string to be displayed
        String LightValue =	String.valueOf(Lval);
        String SoundValue =	String.valueOf(Sval);

        
        //Potentiometer code to determine if the screen will be turned on or off
        if (Potentio.getValue() == 0)
        {
            display.getCanvas().clear();
            display.display();
        }

        display.getCanvas().clear();
        //Time is displayed with a larger font at 2
        display.getCanvas().setTextsize(2);
        display.getCanvas().drawString(0,0,time);
        //Light and Sound intensity values are displayed with a smaller font of 1
        display.getCanvas().setTextsize(1);
        display.getCanvas().drawString(0,35,"Light Intensity: " + LightValue);
        display.getCanvas().drawString(0,45,"Sound Intensity: " + SoundValue);

        //If and else if statements for the danger levels
        //checks if the volume is too loud
        if (Sval > 740)
        {
            display.getCanvas().clear();
            display.getCanvas().drawString(0,0,"Turn down your volume\nit's too loud!");
            display.display();

            try
            {
                //LED blinks when warnings are initiated
                LED.setValue(1);
                Thread.sleep(1000);
                LED.setValue(0);
            }
            //exceptions
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        //Light and Sound values are connected by if and else if statements so that they do not display on top of each other
        //checks if the brightness is too bright
        else if (Lval > 700)
        {
            display.getCanvas().clear();
            display.getCanvas().drawString(0,0,"Turn down your \nbrightness it's too\nbright!");
            display.display();

            try
            {
                LED.setValue(1);
                Thread.sleep(1000);
                LED.setValue(0);
            }

            //exceptions
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        //To check if lights are turned off
        else if (Lval < 10)
        {
            display.getCanvas().clear();
            display.getCanvas().drawString(0,0,"Turn on your lights\nits too dark!");
            display.display();
            try
            {
                LED.setValue(1);
                Thread.sleep(1000);
                LED.setValue(0);
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        //display is updated
        display.display();
    }
}
