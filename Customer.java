
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalTime;
import static java.time.temporal.ChronoUnit.SECONDS;
import javafx.scene.control.Hyperlink;

/*
Encapsulates a customer
*/
public class Customer 
{
    private final LocalTime arrivalTime;
    private final int numItems;
    private Duration scanTime;
    private final LocalTime optimalCheckOutTime;
    private LocalTime actualCheckOutTime;
    private LocalTime abandonCartTime;
    private static final double PROB_DO_IT_YOURSELFER = .25;
    private static final double PROB_CHEATER = .10;
    private final boolean doItYourselfType;
    private final boolean cheater;
    private boolean cheated;
    private boolean switchedLanes;
    private final int ID;
    private static int iDcounter = 1; //static variable to hold number of customers
    private static final SecureRandom random = new SecureRandom();
    private Hyperlink hyperlink;
    
    /*
    Constructs a Customer
    @param arrivalTime The time customer entered queue
    @param avgNumItems The average number items for each customer in simulation
    @param scanRate The average scan rate for each customer in simulation
    */
    public Customer(LocalTime arrivalTime, int avgNumItems, double scanRate)
    {
        this.arrivalTime = arrivalTime;
        numItems = 1 + random.nextInt(avgNumItems * 2); //randomly generates num of items in cart
        scanRate = (scanRate / 2) + random.nextInt((int)(scanRate * 1.5)); //randomly generates scan rate
        double timeScanItems = numItems / scanRate;
        long iPart = (long)timeScanItems;
        double fPart = timeScanItems - iPart;
        Duration minutes = Duration.ofMinutes(iPart);
        Duration seconds = Duration.ofSeconds((long)(fPart * 60));
        doItYourselfType = random.nextDouble() < PROB_DO_IT_YOURSELFER;
        if (doItYourselfType) //assumes self checkout will take longer
        {
            
            minutes = minutes.plus(minutes.dividedBy(2));
            seconds = seconds.plus(seconds.dividedBy(2));
            scanTime = minutes.plus(seconds);
        }
        else
        {
            scanTime = minutes.plus(seconds);
        }
        optimalCheckOutTime = this.arrivalTime.plus(scanTime); //earliest check out time
        cheater = random.nextDouble() < PROB_CHEATER;
        cheated = false;
        switchedLanes = false;
        ID = iDcounter++;
    }
    
    public LocalTime getArrivalTime()
    {
        return arrivalTime;
    }
    
    public int getNumItems()
    {
        return numItems;
    }
    
    public Duration getScanTime()
    {
        return scanTime;
    }
    
    public int getCustID()
    {
        return ID;
    }
    
    @Override
    public String toString()
    {
        return "Customer " +  ID;
    }

    public LocalTime getOptimalCheckOutTime() 
    {
        return optimalCheckOutTime;
    }
    
    public void setActualCheckOutTime(LocalTime checkOutTime)
    {
        actualCheckOutTime = checkOutTime;
    }
    
    public void setAbandonCartTime(LocalTime leaveTime)
    {
        this.abandonCartTime = leaveTime;
    }
    
    public LocalTime getAbandonCartTime()
    {
        if (abandonCartTime != null)
        {
            return abandonCartTime;
        }
        else
        {
            return null;
        }
    }
    
    public LocalTime getActualCheckOutTime()
    {
        return actualCheckOutTime;
    }
    
    public Duration getFinalWaitTime()
    {
        long wait = SECONDS.between(arrivalTime, actualCheckOutTime);
        return Duration.ofSeconds(wait);
    }

    public boolean isDoItYourselfType() 
    {
        return doItYourselfType;
    }
    
    public boolean isPotentialCheater()
    {
        return cheater;
    }
    
    public void setCheated(boolean cheated)
    {
        this.cheated = cheated;
    }
    
    public boolean getCheated()
    {
        return cheated;
    }

    public void setScanTime(Duration duration) 
    {
        scanTime  = duration;
    }
    
    public void switchedLanes(boolean switched)
    {
        switchedLanes = switched;
    }
    
    public boolean hasSwitchedLanes()
    {
        return switchedLanes;
    }
    
    public void setHyperlink(Hyperlink hyperlink)
    {
        this.hyperlink = hyperlink;
    }
    
    /*
    Method used to find a customer who abandons q but is not necessarily at end
    of q*/
    public Hyperlink getHyperlink()
    {
        return hyperlink;
    }
    
    /*
    Invoked before each simulation to reset counter
    */
    public static void resetIDCounter()
    {
        iDcounter = 1;
    }
}
