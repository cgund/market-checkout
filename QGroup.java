
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.ArrayList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/*
Class for holding data on the different types of queues in the simulation
*/
public class QGroup<T> extends ArrayList<T>
{
    private final int ID;
    private final int numLanes;
    private final String laneType;
    private final long simulationTime;
    private final Duration durationSim;
    private Duration totalAvgWaitTime;
    private Duration avgWaitTime;
    private Duration totalAvgIdleTime;
    private Duration avgIdleTime;
    private Duration totalGlitchTime;
    private double totalAvgQSize;
    private double avgQSize;
    private double numCustCheckedOut;
    private double totalAvgNumCustHour;
    private double avgNumCustHour;
    private int maxQSize;
    private double totalNumItems;
    private double avgNumItems;
    private double avgNumItemsHour;
    private int numTotalAbanCartsHour;
    private Color textFill = Color.BLACK;
    private static final Font FONT = Font.font(null, FontWeight.BOLD, 12);
    
    public QGroup(int ID, int numLanes, String laneType, long simulTime)
    {
        this.ID = ID;
        this.numLanes = numLanes;
        this.laneType = laneType;
        this.simulationTime = simulTime;
        durationSim = Duration.ofSeconds(simulationTime);
        totalAvgWaitTime = Duration.ZERO;
        avgWaitTime = Duration.ZERO;
        totalAvgIdleTime = Duration.ZERO;
        avgIdleTime = Duration.ZERO;
        totalGlitchTime = Duration.ZERO;
        totalAvgQSize = 0;
        avgQSize = 0;
        numCustCheckedOut = 0;
        totalAvgNumCustHour = 0;
        avgNumCustHour = 0;
        maxQSize = 0;
        totalNumItems = 0;
        avgNumItems = 0;
        avgNumItemsHour = 0;      
        numTotalAbanCartsHour = 0;
    }
    
    public Duration getSimulationTime()
    {
        return durationSim;
    }
    
    public void setTotalAvgWaitTime(Duration qAvgWaitTime)
    {
        totalAvgWaitTime = totalAvgWaitTime.plus(qAvgWaitTime);
    }
    
    public Duration getAvgWaitTime()
    {
        avgWaitTime = totalAvgWaitTime.dividedBy(numLanes);
        return avgWaitTime;
    }
        
    public Node getAvgWaitTimeFormatted()
    {
        Duration wait = getAvgWaitTime();
        long waitTime = wait.getSeconds();
        long minutes = (long)waitTime / 60;
        long seconds = (long)waitTime % 60;
        StringBuilder sb = new StringBuilder();
        if (seconds < 10)
        {
            sb.append("0");
            sb.append((String.valueOf(seconds)));
        }
        else
        {
            sb.append(String.valueOf(seconds));
        }
        Label lblWaitTime = new Label(String.valueOf(minutes) + ":" + sb.toString());
        lblWaitTime.setTextFill(getTextFill());
        lblWaitTime.setFont(FONT);
        HBox hbWaitTime = new HBox(10);
        hbWaitTime.setAlignment(Pos.CENTER);
        hbWaitTime.getChildren().add(lblWaitTime);
        return hbWaitTime;        
    }

    public void setTotalAvgIdleTime(Duration qAvgIdleTime) 
    {
        totalAvgIdleTime = totalAvgIdleTime.plus(qAvgIdleTime);
    }
    
    public Duration getAvgIdleTime()
    {
        avgIdleTime = totalAvgIdleTime.dividedBy(numLanes);
        return avgIdleTime;
    }
    
    public double getAvgIdleTimeDecimalFormatted()
    {
        long idleNanosecs = getAvgIdleTime().toNanos();
        double propIdle = (double)idleNanosecs / 1000000000d;
        double percentIdle = propIdle * 100;
        return percentIdle;
    }
    
    public Node getAvgIdleTimeNodeFormatted()
    {
        long idleNanosecs = getAvgIdleTime().toNanos();
        double propIdle = (double)idleNanosecs / 1000000000d;
        double percentIdle = propIdle * 100;
        DecimalFormat df = new DecimalFormat("##.##");
        Label lbl = new Label(String.valueOf(df.format(percentIdle)));
        lbl.setTextFill(getTextFill());
        lbl.setFont(FONT);
        HBox hbIdleTime = new HBox(10);
        hbIdleTime.setAlignment(Pos.CENTER);
        hbIdleTime.getChildren().add(lbl);
        return hbIdleTime;
    }
    
    public void setTotalGlitchTime(Duration qTotalGlitchTime)
    {
        totalGlitchTime = totalGlitchTime.plus(qTotalGlitchTime);
    }
    
    public Duration getTotalGlitchTime()
    {
        return totalGlitchTime;
    }
    
    public Node getAvgGlitchTimeFormatted()
    {
        Label lbl = new Label(String.valueOf(getTotalGlitchTime().toMinutes()));
        lbl.setTextFill(getTextFill());
        lbl.setFont(FONT);
        HBox hbGlitchTime = new HBox(10);
        hbGlitchTime.setAlignment(Pos.CENTER);
        hbGlitchTime.getChildren().add(lbl);
        return hbGlitchTime;
    }
    
    public void setTotalAvgQSize(int qAvgSize)
    {
        totalAvgQSize += qAvgSize;
    }
    
    public double getAvgQSize()
    {
        avgQSize = totalAvgQSize / (double)numLanes;
        return avgQSize;
    }
    
    public Node getAvgQSizeFormatted()
    {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        Label lbl = new Label(String.valueOf(df.format(getAvgQSize())));        
        lbl.setTextFill(getTextFill());
        lbl.setFont(FONT);
        HBox hb = new HBox(10);
        hb.setAlignment(Pos.CENTER);
        hb.getChildren().add(lbl);
        return hb;
    }
    
    public void setTotalAvgNumCustHour(double qAvgNumCustHour)
    {
        this.totalAvgNumCustHour += qAvgNumCustHour;
    }
    
    public double getTotalNumAvgCustHour()
    {
        return totalAvgNumCustHour;
    }
    
    public double getAvgNumCustHour()
    {
        avgNumCustHour = getTotalNumAvgCustHour() / (double)numLanes;
        return avgNumCustHour;
    }
    
    public Node getAvgNumCustHourFormatted()
    {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        Label lbl = new Label(df.format(getAvgNumCustHour()));
        lbl.setTextFill(getTextFill());
        lbl.setFont(FONT);
        HBox hb = new HBox(10);
        hb.setAlignment(Pos.CENTER);
        hb.getChildren().add(lbl);
        return hb;        
    }
    
    public void setMaxQSize(int qMaxSize)
    {
        if (qMaxSize > maxQSize)
        {
            maxQSize = qMaxSize;
        }
    }
    
    public int getMaxQSize()
    {
        return maxQSize;
    }
    
    public Node getMaxQSizeFormatted()
    {
        Label lbl = new Label(String.valueOf(getMaxQSize()));      
        lbl.setTextFill(getTextFill());
        lbl.setFont(FONT);
        HBox hb = new HBox(10);
        hb.setAlignment(Pos.CENTER);
        hb.getChildren().add(lbl);
        return hb;        
    }
    
    public void setTotalNumItems(double qNumItems)
    {
        totalNumItems += qNumItems;
    }
    
    public double getTotalNumItems()
    {
        return totalNumItems;
    }
    
    public double getNumItemsPerHour()
    {
        avgNumItems = getTotalNumItems() / (double)numLanes;
        double avgNumItemsSecond = avgNumItems / (double)simulationTime;
        avgNumItemsHour = avgNumItemsSecond * 3600;
        return avgNumItemsHour;
    }
    
    public Node getNumItemsPerHourFormatted()
    {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        Label lbl = new Label(df.format(getNumItemsPerHour()));
        lbl.setFont(FONT);
        lbl.setTextFill(getTextFill());
        HBox hb = new HBox(10);
        hb.setAlignment(Pos.CENTER);
        hb.getChildren().add(lbl);
        return hb;         
    }
    
    public void setNumAbandonedCarts(double numCartsHour)
    {
        numTotalAbanCartsHour += numCartsHour;
    }
    
    public int  getAvgNumAbanCartsHour()
    {
        return numTotalAbanCartsHour / numLanes;
    }
    
    public Node getNumAbandonedCartsFormatted()
    {
        DecimalFormat df = new DecimalFormat("#.##");
        Label lbl = new Label(String.valueOf(df.format(getAvgNumAbanCartsHour())));
        lbl.setTextFill(getTextFill());
        lbl.setFont(FONT);
        HBox hb = new HBox(10);
        hb.setAlignment(Pos.CENTER);
        hb.getChildren().add(lbl);
        return hb;
    }
    
    public Color getTextFill() 
    {
        return textFill;
    }

    public void setTextFill(Color fill) 
    {
        textFill = fill;
    }
    
    @Override
    public String toString()
    {
        return laneType;
    }

    public double getNumCustCheckedOut() 
    {
        return numCustCheckedOut;
    }

    public void setNumCustCheckedOut(double qNumCustCheckedOut) 
    {
        this.numCustCheckedOut = qNumCustCheckedOut;
    }

    public int getID() {
        return ID;
    }
}
