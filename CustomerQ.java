package market;


import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

/*
Represents a single queue/lane in simulation
*/
public class CustomerQ 
{
    private final Deque<Customer> q; //Holds Customers in queue
    private final List<Integer> qLengths; //Used for determing avg q length
    private final String qName;
    private final int ID;
    private static int qIDCounter = 1;
    private final int groupID;
    private final GridPane gpCheckedOut;
    private final GridPane gpInQ;
    private static int rowInQ = 0;
    private static int rowCheckedOut = 0;
    
    private final long simTimeSecs;
    private int numTotalCustCheckedOut;
    private int numCustInLine;
    
    private Duration totalWaitTime; //Total wait time of all Customers in queue

    private int maxQLength = 0;
    private final int maxItemsAllowed;
    
    private Duration totalIdleTime;
    private Duration totalGlitchTime;
    private int numAbandonedCarts;
    private double numItemsScanned;

    //Constructs queue
    public CustomerQ(String name, int groupID, long simTimeSeconds, int maxItemsAllowed)
    {
        q = new LinkedList();
        qLengths = new ArrayList<>();
        qName = name;
        ID = qIDCounter;
        qIDCounter++;
        this.groupID = groupID;
        gpCheckedOut = new GridPane();
        gpCheckedOut.setPrefHeight(150);
        gpInQ = new GridPane();
        gpInQ.setPrefHeight(150);
        simTimeSecs = simTimeSeconds;
        numTotalCustCheckedOut = 0;
        numCustInLine = 0;
        totalWaitTime = Duration.ofSeconds(0);
        totalIdleTime = Duration.ofSeconds(0);
        totalGlitchTime = Duration.ofSeconds(0);
        numItemsScanned = 0;
        numAbandonedCarts = 0;
        this.maxItemsAllowed = maxItemsAllowed;
    }

    public GridPane getGpCheckedOut() 
    {
        return gpCheckedOut;
    }

    public GridPane getGpInQ() 
    {
        return gpInQ;
    }

    public int getTotalNumCustCheckedOut() 
    {
        return numTotalCustCheckedOut;
    }
    
    public double getAvgNumCustHour()
    {
        double numAvgCustPerSecond = (double)getTotalNumCustCheckedOut() / (double)simTimeSecs;
        double numAvgCustPerHour = numAvgCustPerSecond * 3600;
        return numAvgCustPerHour;
    }
    
    public Node getAvgNumCustHourFormatted()
    {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        HBox hb = new HBox(10);
        hb.setAlignment(Pos.CENTER);
        hb.getChildren().add(new Label(df.format(getAvgNumCustHour())));
        return hb;       
    }
    
    public int getNumCustInLine()
    {
        return numCustInLine;
    }
    
    public void incrementTotalWaitTime(Duration custWaitTime) 
    {
        totalWaitTime = totalWaitTime.plus(custWaitTime);
    }
    
    public Duration getTotalWaitTime() 
    {
        return totalWaitTime;
    }

    /*
    Average wait time of all customers in queue
    */
    public Duration getAvgWaitTime()
    {
        if (numTotalCustCheckedOut > 0)
        {
            return getTotalWaitTime().dividedBy(numTotalCustCheckedOut);
        }
        else
        {
            return Duration.ZERO;
        }
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
            sb.append(String.valueOf(seconds));
        }
        else
        {
            sb.append(String.valueOf(seconds));
        }
        Label lblWaitTime = new Label(String.valueOf(minutes) + ":" + sb.toString());
        HBox hbWaitTime = new HBox(10);
        hbWaitTime.setAlignment(Pos.CENTER);
        hbWaitTime.getChildren().add(lblWaitTime);
        return hbWaitTime;
    }
  
    public int size()
    {
        return q.size();
    }
    
    public void addCustomer(Customer customer)
    {
        q.offerLast(customer);
        qLengths.add(q.size());
        if (q.size() > getMaxQLength()) //Keeps track of max queue length
        {
            maxQLength = q.size();
        }
        addCustomerInQHyperlink(customer);
        numCustInLine++;
    }
    
    private void addCustomerInQHyperlink(Customer customer)
    {
        Hyperlink hlCustomer = new Hyperlink(customer.toString());
        hlCustomer.setOnAction(e -> //displays data on customers still in Q
        {
           CustomerInQStage data = new CustomerInQStage(customer);
           data.show();
        });
        customer.setHyperlink(hlCustomer);
        gpInQ.addRow(rowInQ++, hlCustomer);        
    }
    
    public Customer peekFirstCustomer()
    {
        return q.peekFirst();
    }
    
    public Customer removeFirstCustomer(Customer customer)
    {
        Customer checkedOut = q.pollFirst();
        gpInQ.getChildren().remove(0);
        qLengths.add(q.size());
        addCustomerCheckedOutHyperlink(customer);
        numTotalCustCheckedOut++;
        numCustInLine--;
        numItemsScanned += customer.getNumItems();
        return checkedOut;
    }
    
    private void addCustomerCheckedOutHyperlink(Customer customer)
    {
        Hyperlink hlCustomer = new Hyperlink(customer.toString());
        hlCustomer.setOnAction(e -> //displays data on customers checkedout
        {
            CustomerCheckedOutStage data = new CustomerCheckedOutStage(customer);
            data.show();
        });
        gpCheckedOut.addRow(rowCheckedOut++, hlCustomer);        
    }
    
    public Customer peekLastCustomer()
    {
        return q.peekLast();
    }
    
    public Customer removeLastCustomer(Customer customer)
    {
        gpInQ.getChildren().remove(customer.getHyperlink());
        Customer removed = q.pollLast();
        numCustInLine--;
        qLengths.add(q.size());
        return removed;
    }
    
    public boolean removeCustomer(Customer customer)
    {
        gpInQ.getChildren().remove(customer.getHyperlink());
        boolean customerRemoved = q.removeFirstOccurrence(customer);
        numCustInLine--;
        qLengths.add(q.size());
        return customerRemoved;
    }
    
    public Iterator getIterator()
    {
        return q.iterator();
    }
    
    public int getNumTotalCustomers()
    {
        return numTotalCustCheckedOut + numCustInLine;
    }
    
    /*
    Method that returns the total number of items in all the customers
    carts who are waiting in line
    */
    public int getTotalNumItemsInQ()
    {
        int numTotalItems = 0;
        Iterator<Customer> iterator = q.iterator();
        while (iterator.hasNext())
        {
            Customer customer = iterator.next();
            numTotalItems += numTotalItems + customer.getNumItems();
        }
        return numTotalItems;
    }
    
    public boolean isEmpty()
    {
        return q.isEmpty();
    }
    
    public void incrementNumAbandonedCarts()
    {
        numAbandonedCarts++;
    }
    
    public int  getTotalNumAbandonedCarts()
    {
        return numAbandonedCarts;
    }
    
    public double getAvgNumAbanCartsHour()
    {
        double numAbanCartsSec = getTotalNumAbandonedCarts() / (double)simTimeSecs;
        double numAbanCartsHour = numAbanCartsSec * 3600;
        return numAbanCartsHour;        
    }
    
    public Node getAvgNumAbanCartsHourFormatted()
    {
        HBox hb = new HBox(10);
        hb.setAlignment(Pos.CENTER);
        DecimalFormat df = new DecimalFormat("#.##");
        hb.getChildren().add(new Label(String.valueOf(df.format(getAvgNumAbanCartsHour()))));
        return hb;
    }
          
    public void incrementIdleTime()
    {
        totalIdleTime = totalIdleTime.plusSeconds(60);
    }
    
    public Duration getAvgIdleTime()
    {
        return totalIdleTime.dividedBy(simTimeSecs);
    }
    
    public double getAvgIdleTimeDecimalFormatted()
    {
        long idleNanosecs = getAvgIdleTime().toNanos();
        double propIdle = (double)idleNanosecs / 1000000000d;
        double percentIdle = propIdle * 100;
        return percentIdle;
    }
    
    public Node getAvgIdleTimeFormatted()
    {
        Duration avgIdleTime = getAvgIdleTime();
        long idleNanosecs = avgIdleTime.toNanos();
        double propIdle = (double)idleNanosecs / 1000000000d;
        double percentIdle = propIdle * 100;
        DecimalFormat df = new DecimalFormat("##.##");
        Label lblIdleTime = new Label(String.valueOf(df.format(percentIdle)));
        HBox hbIdleTime = new HBox(10);
        hbIdleTime.setAlignment(Pos.CENTER);
        hbIdleTime.getChildren().add(lblIdleTime);
        return hbIdleTime;
    }
    
    public void incrementGlitchTime()
    {
        totalGlitchTime = totalGlitchTime.plusMinutes(1);
    }
    
    public Duration getTotalGlitchTime()
    {
        return totalGlitchTime;
    }
    
    public Node getTotalGlitchTimeFormatted()
    {
        Label lblGlitch = new Label(String.valueOf(getTotalGlitchTime().toMinutes()));
        HBox hbGlitchTime = new HBox(10);
        hbGlitchTime.setAlignment(Pos.CENTER);
        hbGlitchTime.getChildren().add(lblGlitch);
        return hbGlitchTime;
    }

    public int getMaxQLength() {
        return maxQLength;
    }
    
    public Node getMaxQLengthFormatted()
    {
        HBox hb = new HBox(10);
        hb.setAlignment(Pos.CENTER);
        hb.getChildren().add(new Label(String.valueOf(getMaxQLength())));
        return hb;
    }
    
    public int getAvgQSize()
    {
        if (qLengths.size() > 0)
        {
            int sum = 0;
            for (Integer size: qLengths)
            {
                sum += size;
            }
            return sum / qLengths.size();            
        }
        else
        {
            return 0;
        }
    }
    
    public Node getAvgQSizeFormatted()
    {
        HBox hb = new HBox(10);
        hb.setAlignment(Pos.CENTER);
        hb.getChildren().add(new Label(String.valueOf(getAvgQSize())));
        return hb;
    }

    public double getTotalNumItemsScanned() 
    {
        return numItemsScanned;
    }
    
    public double getNumItemsScannedHour()
    {
        double numItemsPerSecond = getTotalNumItemsScanned() / (double)simTimeSecs;
        double numItemsPerHour = numItemsPerSecond * 3600;
        return numItemsPerHour;
    }
    
    public Node getNumItemsPerHourFormatted()
    {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        HBox hb = new HBox(10);
        hb.setAlignment(Pos.CENTER);
        hb.getChildren().add(new Label(df.format(getNumItemsScannedHour())));
        return hb;         
    }
       
    public String getName()
    {
        return qName;
    }
    
    public int getID()
    {
        return ID;
    }

    public int getMaxItemsAllowed() {
        return maxItemsAllowed;
    }
    
    public static void resetIDCounter()
    {
        qIDCounter = 1;
    }

    public int getGroupID() {
        return groupID;
    }
}
