package market;


import java.security.SecureRandom;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

/*
Class to represent the check out area at a supermarket.  For conciseness,
the identifier "Q" is used in place of the more typical "line" or "lane"
*/
public class CheckOutAreaStage 
{
    //Reference assigned to Customer seeking a queue to enter
    private Customer customerSeekingQ;
    
    private final int customersPerMinute;
    private final int numSuperExQs;
    private final int numExQs;
    private final int numStandardQs;
    private final int numSelfQs;
    private final int numTotalQs;
    private final int maxSuperExItems;
    private final int maxExItems;
    private final int maxSelfItems;
    private final int avgNumItems;
    private final double itemsPerMinute;
    private LocalTime currentTime;
    private Label lblTime;
    private Label lblCurrentTime;
    private ProgressIndicator pi;
    private double progress = 0;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final long simulationTimeSeconds;
    private final long simulationTimeMinutes;
    private final double incrementRate; //Used for ProgressIndicator
    
    //Event probability constants
    private static final double PROB_SLOWDOWN = .10;
    private static final double PROB_SELF_Q_USER_ERROR = .25;
    private static final double PROB_SELECT_SHORTEST_LANE = .75;
    private static final double PROB_SWITCH_LANE = .05;
    
    private static final  String SUPER_EXPRESS = "SuperExpress";
    private static final String EXPRESS = "Express";
    private static final String STANDARD = "Standard";
    private static final String SELF = "Self";
    
    /*
    Name of queue group mapped to QGroup which contains individuals queues
    */
    private final Map<String, QGroup<CustomerQ>> hmQs;
    
    /*
    Variables to approximate when a customer will abandon cart
    */
    private final Map<Integer, Double> hmTimeProbLeave;
    private int maxWaitTime = 0;
    
    private GridPane gpAbandonedQ; //Holds Customers who abandoned carts
    private int leftQrow = 0;
    
    private static final Insets DEFAULT_INSETS = new Insets(10, 10, 10, 10);
    protected static Stage stage;
    private final BorderPane root;
    private final Scene scene;

    private static final SecureRandom random = new SecureRandom();
    
    /*
    Constructor accepts simulation parameter to initiliaze class fields.
    */
    public CheckOutAreaStage(int customersPerMin, int numSuper, int numEx, int numStd, int numSelf, int maxItemsSuper,
                                int maxItemsEx, int maxItemsSelf, int avgNumItems, double itemsPerMinute, LocalTime start,
                                LocalTime end)
    {        
        customersPerMinute = customersPerMin;
        numSuperExQs = numSuper;
        numExQs = numEx;
        numStandardQs = numStd;
        numSelfQs = numSelf;
        numTotalQs = numSuperExQs + numExQs + numStandardQs + numSelfQs;

        maxSuperExItems = maxItemsSuper;
        maxExItems = maxItemsEx;
        maxSelfItems = maxItemsSelf;
        this.avgNumItems = avgNumItems;
        this.itemsPerMinute = itemsPerMinute;
        
        startTime = start;
        endTime = end.plusMinutes(1);
        simulationTimeSeconds = ChronoUnit.SECONDS.between(startTime, endTime);
        simulationTimeMinutes = ChronoUnit.MINUTES.between(startTime, endTime);
        incrementRate = (100 / (double)simulationTimeMinutes) / 100; 

        hmQs = new HashMap();
        hmQs.put(SUPER_EXPRESS, new QGroup(1, numSuperExQs, SUPER_EXPRESS, simulationTimeSeconds));
        hmQs.put(EXPRESS, new QGroup(2, this.numExQs, EXPRESS, simulationTimeSeconds));
        hmQs.put(STANDARD, new QGroup(3, numStandardQs, STANDARD, simulationTimeSeconds));
        hmQs.put(SELF, new QGroup(4, this.numSelfQs, SELF, simulationTimeSeconds)); 
        
        hmTimeProbLeave = new HashMap();
        fillProbLeaveMap();
        root = new BorderPane();
        root.setPadding(DEFAULT_INSETS);
        scene = new Scene(root, numTotalQs * 150, 400);        
        root.setTop(createTitlePane());
        root.setRight(createRightPane());
        root.setCenter(createCheckOutAreaPane());
        root.setBottom(createButtonsPane());
        root.setLeft(createSideLabelPane());
        
        stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Simulation");
        stage.getIcons().addAll(Resources.getIcons());
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
    }
    
    /*
    Displays stage.  Specified boolean value determines if simulation is animated
    or not
    */
    public void show(boolean animated)
    {
        stage.show();
        CustomerQ.resetIDCounter();
        Customer.resetIDCounter();
        if (animated)  //user selected animation option
        {
            lblTime.setVisible(true);
            lblCurrentTime.setVisible(true);
            pi.setVisible(true);
            currentTime = startTime;
            Timeline animation = new Timeline(new KeyFrame(Duration.millis(500), e -> runSimulAnim()));
            animation.setCycleCount((int)simulationTimeMinutes);
            animation.play();
        }
        else //user did not select animation option
        {
            runSimulation();            
        }
    }
    
    /*
    Iterates from start time to end time.  Increments by one minute. Generates variable
    to represent number of customers arriving in that minute.  For each value, creates a
    customer, assigns customer to Customer variable, and calls method to direct customer.  
    Once that method returns(customer has found a lane, others customers have switched lanes or left
    q) then each lane is proccessed.
    */
    private void runSimulation()
    {   
        for (currentTime = startTime; currentTime.isBefore(endTime); currentTime = currentTime.plusMinutes(1))
        {
            int numCustomers = numCustomersArriving();
            for (int i = 0; i < numCustomers; i++)
            {
                customerSeekingQ = collectCustomerData();
                directCustomer(); 
            }
            processLanes();
        }
    }
    
    /*
    Simulation with animation option
    */
    private void runSimulAnim()
    {
        lblCurrentTime.setText(String.valueOf(currentTime));
        progress++;
        pi.setProgress(progress * incrementRate);
        if (pi.getProgress() >= 1) //Simulation complete
        {
            pi.setVisible(false);
        }
        int numCustomers = numCustomersArriving();
        for (int i = 0; i < numCustomers; i++)
        {
            customerSeekingQ = collectCustomerData();
            directCustomer();
        }
        processLanes();
        currentTime = currentTime.plusMinutes(1);
    }
    
    /*
    Generates a random int to represent the number of customers arriving per
    minute
    */
    private int numCustomersArriving()
    {
        return random.nextInt(customersPerMinute * 2);
    }

    /*
    Constructs and returns Customer
    */
    private Customer collectCustomerData()
    {
        return new Customer(currentTime, avgNumItems, itemsPerMinute);
    }
    
    /*
    Directs customers to a queue.  The customer is directed to a queue based
    on the customers personality type and number of items in cart
    */
    private void directCustomer()
    {
        int numItems = customerSeekingQ.getNumItems();
        if (customerSeekingQ.isDoItYourselfType() && numItems <= maxSelfItems)        
            surveyEligibleQs(SELF);   
        else
            if (numItems <= maxSuperExItems || customerSeekingQ.isPotentialCheater()) //customer qualifies for super_ex or cheats
                if (allLanesOccupied(SUPER_EXPRESS))
                    if (allLanesOccupied(EXPRESS))
                        if(allLanesOccupied(STANDARD))
                            surveyEligibleQs(SUPER_EXPRESS, EXPRESS, STANDARD);
            else if (numItems <= maxExItems) //customer qualifies for express or cheats
                if (allLanesOccupied(EXPRESS))
                    if (allLanesOccupied(STANDARD))
                        surveyEligibleQs(EXPRESS, STANDARD); 
            else //customer is only eligible for standard and does not cheat
                surveyEligibleQs(STANDARD);           
    }
    
    /*
    Iterates through specified QGroup.  If q/lane is unoccupied, customer is 
    added to q/lane and method returns false. Othewise method returns true
    */
    public boolean allLanesOccupied(String qType)
    {
        //Collections of queues within type (e.g, all Standard queues)
        List<CustomerQ> lanesByType = hmQs.get(qType);

        boolean allLanesOccupied = true;
        int index = 0;
        while (allLanesOccupied && index < lanesByType.size())
        {
            CustomerQ q = lanesByType.get(index); //Individual queue
            if (q.size() < 1) //Queue empty
            {
                q.addCustomer(customerSeekingQ);
                determineIfCheated(q);
                allLanesOccupied = false;
            }
            index++;
        }
        return allLanesOccupied;
    }
    
    /*
    Determines q selection criteria.  The customer either selects queue based
    on the number of customers in queue or select queue based on a estimate
    of the total number of items in each customer's cart in the queue
    */
    public void surveyEligibleQs(String... eligibleLanes)
    {
        if (random.nextDouble() < PROB_SELECT_SHORTEST_LANE) //Selects shortest q
        {
            selectShortestQ(eligibleLanes);
        }
        else //Selects queue with fewest items
        {
            selectFewestItemsQ(eligibleLanes);
        }
    }
    
    /*
    Processes customer who selects shortest queue from eligible queues.
    */
    public void selectShortestQ(String... eligibleLanes)
    {
        int shortestQLength;
        QGroup<CustomerQ> lanesByType = hmQs.get(eligibleLanes[0]);
        CustomerQ shortestQ = lanesByType.get(0);
        shortestQLength = shortestQ.size();

        for (String qType: eligibleLanes)
        {
            lanesByType = hmQs.get(qType);
            for (CustomerQ q: lanesByType)
            {
                if (q.size() < shortestQLength)
                {
                    shortestQLength = q.size();
                    shortestQ = q;
                }
            }
        }
        shortestQ.addCustomer(customerSeekingQ);
        determineIfCheated(shortestQ);
    }
        
    /*
    Processes customer who selects queue with fewest items from eligible queues
    */
    public void selectFewestItemsQ(String... eligibleLanes)
    {
        int smallestNumItems;
        QGroup<CustomerQ> lanesByType = hmQs.get(eligibleLanes[0]);
        CustomerQ fewestItemsQ = lanesByType.get(0);
        smallestNumItems = fewestItemsQ.getTotalNumItemsInQ();
        
        for (String qType: eligibleLanes)
        {
            lanesByType = hmQs.get(qType);
            for (CustomerQ q: lanesByType)
            {
                int numItems = q.getTotalNumItemsInQ();
                if (numItems < smallestNumItems)
                {
                    smallestNumItems = numItems;
                    fewestItemsQ = q;
                }
            }
        }
        fewestItemsQ.addCustomer(customerSeekingQ);
        determineIfCheated(fewestItemsQ);
    }
    
    /*
    Sets cheated field to true if customer entered ineligible q
    */
    public void determineIfCheated(CustomerQ q)
    {
        if (customerSeekingQ.getNumItems() > q.getMaxItemsAllowed())
            customerSeekingQ.setCheated(true);       
    }
    
    /*
    Iterates through each q contained in the simulation
    */
    public void processLanes()
    {
        Set<String> qTypes = hmQs.keySet();
        for (String qType: qTypes)
        {
            List<CustomerQ> lanesByType = hmQs.get(qType);
            for (CustomerQ q: lanesByType)
            {
                if (!q.isEmpty()) //q is not empty
                {
                    if (glitch(q)) //slow down occurs
                    {
                        q.incrementIdleTime(); 
                        q.incrementGlitchTime();
                        considerLaneSwitch(q);
                        considerAbandoningCart(q);
                    }
                    else
                    {
                        checkOutCustomer(q); 
                        considerLaneSwitch(q);
                        considerAbandoningCart(q);
                    }                    
                }
                else //q is empty and idle time is increased
                {
                    q.incrementIdleTime();
                }
            }
        }
    }
    
    /*
    Determines if glitch/malfunction occurs at time of checkout
    */
    public boolean glitch(CustomerQ q)
    {
        if (q.getName().equals(SELF))
            return random.nextDouble() < PROB_SELF_Q_USER_ERROR;
        else
            return random.nextDouble() < PROB_SLOWDOWN;
    }
    
    /*
    Determines if customer is eligible for checkout.  Returns true if customer
    is checked out and false otherwise
    */
    public boolean checkOutCustomer(CustomerQ q)
    {
        boolean checkedOut = false;
        Customer customerCheckingOut = q.peekFirstCustomer(); //Gets Customer at head of q
        //Customer eligible for checkout
        if (currentTime.isAfter(customerCheckingOut.getOptimalCheckOutTime()))
        {
            customerCheckingOut.setActualCheckOutTime(currentTime);
            q.incrementTotalWaitTime(customerCheckingOut.getFinalWaitTime());
            q.removeFirstCustomer(customerCheckingOut);
            checkedOut = true;
        }
        return checkedOut;
    }
    
    /*
    Determines if customer decides to switch lanes.  Returns true if customer
    switched queues, otherwise returns false
    */
    @SuppressWarnings("empty-statement")
    public boolean considerLaneSwitch(CustomerQ q)
    {
        boolean switchedLanes = false;
        if (q.size() > 2) //Customers considers switching if more than two customers ahead in queue
        {
            Customer customerInRear = q.peekLastCustomer();
            if (customerInRear.hasSwitchedLanes() == false) //customer has not switched lanes already
            {
                if (random.nextDouble() < PROB_SWITCH_LANE);
                {
                    switchedLanes = true;
                    customerInRear.setScanTime(customerInRear.getScanTime().plusSeconds(30));
                    customerInRear.switchedLanes(true);
                    customerSeekingQ = q.removeLastCustomer(customerInRear);
                    directCustomer(); //Starts process over with customer who switched queues
                }                
            }
        }
        return switchedLanes;
    }
    
    /*
    Determines whether customer is tired of waiting in queue and decides to abandon
    cart.
    */
     public void considerAbandoningCart(CustomerQ q)
    {
        Iterator<Customer> iterator = q.getIterator();
        int custLinePos = 0;
        List<Customer> customersLeaving = new ArrayList();
        while (iterator.hasNext())
        {
            Customer customer = iterator.next();
            if (custLinePos > 1) //customers near cashier decide to stick it out
            {
                LocalTime customerArrivalTime = customer.getArrivalTime();
                int currentWaitTime = (int)ChronoUnit.MINUTES.between(customerArrivalTime, currentTime);
                if (currentWaitTime <= maxWaitTime)
                {
                    if (hmTimeProbLeave.containsKey(currentWaitTime))
                    {
                        if (random.nextDouble() < hmTimeProbLeave.get(currentWaitTime))
                        {
                            customer.setAbandonCartTime(currentTime);
                            q.incrementNumAbandonedCarts();
                            customersLeaving.add(customer);
                        }                        
                    }
                }
                else
                {
                    customer.setAbandonCartTime(currentTime);
                    q.incrementNumAbandonedCarts();
                    customersLeaving.add(customer);
                }
            }
            custLinePos++;
        }
        
        /*
        Avoids concurrent modification exception being thrown when trying to remove
        a customer while iterating through list of customers.  Hyperlink click
        event displays data for customer that left q
        */
        for (Customer customer: customersLeaving)
        {
            Hyperlink hlCustomer = new Hyperlink("Customer " + customer.getCustID());
            hlCustomer.setOnAction(e ->
            {
                CustomerAbandonedQStage data = new CustomerAbandonedQStage(customer, q);
                data.show();
            });
                    
            gpAbandonedQ.addRow(leftQrow++, hlCustomer);
            q.removeCustomer(customer);
        }
    }
    
    /*
    Creates buttons for stage and registers event handlers with buttons
    */
    private Node createButtonsPane()
    {
        Button btnClose = new Button("Close");
        btnClose.setOnAction(e ->
        {
            stage.close();
        });
        
        Button btnReport = new Button("View Report");
        btnReport.setOnAction(e ->
        {
            SimReportStage report = new SimReportStage(hmQs, customersPerMinute, numTotalQs, 
                    maxSuperExItems, maxExItems, maxSelfItems, startTime, endTime, 
                    simulationTimeSeconds);
            report.viewReport();
        });
        
        HBox hbButton = new HBox(10);
        hbButton.setAlignment(Pos.CENTER);
        hbButton.setPadding(DEFAULT_INSETS);
        hbButton.getChildren().addAll(btnReport, btnClose);
        return hbButton;
    }
    
    /*
    Creates title for scene
    */
    private Node createTitlePane()
    { 
        Label lblTitle = new Label("Checkout Area");
        lblTitle.setFont(Font.font(null, FontWeight.BOLD, 12));
        HBox hbTitle = new HBox(10);
        hbTitle.setAlignment(Pos.CENTER);
        hbTitle.getChildren().add(lblTitle);
        return hbTitle;      
    }
    
    /*
    Pane to hold panes that provide information on the progress of the simulation
    and customers who have abandonded their carts
    */
    private Node createRightPane()
    {
        Label lblLeft = new Label("Abandoned Cart");
        lblLeft.setAlignment(Pos.CENTER);
        HBox hbTitleLeft = new HBox();
        hbTitleLeft.setPadding(DEFAULT_INSETS);
        hbTitleLeft.setAlignment(Pos.CENTER);
        hbTitleLeft.getChildren().add(lblLeft);;
        
        GridPane gpContainer = new GridPane();
        gpContainer.setAlignment(Pos.CENTER);
        gpContainer.setPadding(DEFAULT_INSETS);
        int row = 0;
        gpContainer.addRow(row++, createProgressPane());
        gpContainer.addRow(row++, new Label());
        gpContainer.addRow(row++, hbTitleLeft);
        gpContainer.addRow(row++, createAbandondedPane(lblLeft.getWidth()));
        
        return gpContainer;
    }
    
    /*
    Pane to provide information on the customers who have abandonded their carts
    */
    private Node createAbandondedPane(double width)
    {
        gpAbandonedQ = new GridPane();
        gpAbandonedQ.setAlignment(Pos.CENTER);
        gpAbandonedQ.setPadding(DEFAULT_INSETS);
        
        ScrollPane spLeftQ = new ScrollPane(gpAbandonedQ);
        spLeftQ.setPrefHeight(scene.getHeight() * .75);
        spLeftQ.setMinViewportWidth(width * 1.5);
        
        return spLeftQ;
    }
    
    /*
    Panes to hold labels that indicate which customers have been checked out
    and which customers still remain in the queue at the end of the simulation
    */
    private Node createSideLabelPane()
    {
        Label lblDone = new Label("Checked Out");
        lblDone.setRotate(270);
        Label lblInQueue = new Label("Remaining In Queue");
        lblInQueue.setRotate(270);
        VBox vbLabels = new VBox(125);
        vbLabels.setAlignment(Pos.CENTER);
        vbLabels.getChildren().addAll(lblDone, lblInQueue);
        return vbLabels;
    }
    
    /*
    Pane to provide information on the progress of the simulation
    */
    private Node createProgressPane()
    {
        lblTime = new Label("Time: ");
        lblCurrentTime = new Label();
        lblTime.setVisible(false);
        lblCurrentTime.setVisible(false);
        pi = new ProgressBar(0);
        pi.setVisible(false);        
        
        HBox hbTime = new HBox(10);
        hbTime.setAlignment(Pos.CENTER);
        hbTime.setPadding(new Insets(0, 0, 10, 0));
        hbTime.getChildren().addAll(lblTime, lblCurrentTime);
        
        HBox hbProgress = new HBox(10);
        hbProgress.setAlignment(Pos.CENTER);
        hbProgress.setPadding(new Insets(0, 0, 10, 0));
        hbProgress.getChildren().add(pi);
        
        VBox vbTime = new VBox(10);
        vbTime.setAlignment(Pos.CENTER);
        vbTime.getChildren().addAll(hbTime, hbProgress);
        return vbTime;
    }
    
    /*
    Sets up GUI.  Creates each CustomerQ
    */
    private Node createCheckOutAreaPane()
    {
        GridPane gpLanes = new GridPane();
        
        gpLanes.setAlignment(Pos.CENTER);
        gpLanes.setPadding(DEFAULT_INSETS);
        gpLanes.setHgap(25);
        gpLanes.setVgap(10);
        gpLanes.setPrefWidth(scene.getWidth() - 20);
        for (int c = 0; c < numTotalQs; c++)
        {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100 / numTotalQs);
            gpLanes.getColumnConstraints().add(column);            
        }

        int i;
        int column = 0;
        double SCROLL_PANE_WIDTH = scene.getWidth() / numTotalQs;
        for (i = 0; i < numTotalQs; i++)
        {
            CustomerQ q;
            Label lblQ;
            if (i < numSuperExQs)
            {
                q = addQ(SUPER_EXPRESS);
                lblQ = new Label(SUPER_EXPRESS + " " + q.getID());
            }
            else if (i < numExQs + numSuperExQs)
            {
                q = addQ(EXPRESS);
                lblQ = new Label(EXPRESS + " " + q.getID());                
            }
            else if (i < numStandardQs + numExQs + numSuperExQs)
            {
                q = addQ(STANDARD);
                lblQ = new Label(STANDARD + " " + q.getID());                
            }
            else
            {
                q = addQ(SELF);
                lblQ = new Label(SELF + " " + q.getID());
            }
            HBox hb = new HBox(10);
            hb.setAlignment(Pos.CENTER);
            hb.getChildren().add(lblQ);
            gpLanes.add(hb, column, 0);
            
            ScrollPane scrollCheckedOut = new ScrollPane(q.getGpCheckedOut());
            scrollCheckedOut.setPrefWidth(SCROLL_PANE_WIDTH);
            ScrollPane scrollInQ = new ScrollPane(q.getGpInQ());
            scrollInQ.setPrefWidth(SCROLL_PANE_WIDTH);
            gpLanes.add(scrollCheckedOut, column, 1);
            gpLanes.add(scrollInQ, column, 2);
            column++;
        }
        return gpLanes;
    }
    
    //Adds a Queue to its QGroup
    private CustomerQ addQ(String qType)
    {
        QGroup<CustomerQ> qGroup = hmQs.get(qType);
        CustomerQ q = new CustomerQ(qType, qGroup.getID(), simulationTimeSeconds, maxSuperExItems);
        qGroup.add(q);
        return q;
    }
    
    /*
    Fills a Map with wait times mapped to the probability that a given customer
    will abandon his/her cart at those wait times.  The probability of abandoning
    a cart increases geometrically every five minutes
    */
    public final void fillProbLeaveMap()
    {
        double probLeave = .05;
        double multiplier = 1.5;
        int waitTime = 11;  //Minutes at which customer considers leaving cart
        while (probLeave < 1.0)
        {
            hmTimeProbLeave.put(waitTime, probLeave);
            if (waitTime % 5 == 0)
            {
                probLeave += .10 * multiplier;
                multiplier *= multiplier;
            }
            else
            {
                probLeave += .01;            
            }
            maxWaitTime = waitTime;
            waitTime++;
        }
    }
}
