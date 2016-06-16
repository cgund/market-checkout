package market;


import java.sql.SQLException;
import java.time.LocalTime;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

/*
Displays data generated during simulation
*/
public class SimReportStage 
{
    private Map<String, QGroup<CustomerQ>> mapQTypes;
    private final int custPerMin;
    private final LocalTime start;
    private final LocalTime end;
    private final QGroup allQs;
    private final GridPane gpReport;
    private DataBaseBridge db = null;
    private final Insets insets = new Insets(10, 10, 10, 10);
    private final BorderPane root;
    private Scene scene;
    private Stage stage;
    private static final String[] qTypes = {"SuperExpress", "Express", "Standard", "Self"};
    private static final Color Q_TYPE_FILL = Color.DARKBLUE;
    private static final Color TOTAL_FILL = Color.RED;
    private static final Font FONT = Font.font(null, FontWeight.BOLD, 12);
    
    public SimReportStage(Map<String, QGroup<CustomerQ>> mapQTypes, int customersPerMinute, 
            int numQs, int maxItemsSuper, int maxItemsEx, int maxItemsSelf,
            LocalTime start, LocalTime end, long simTime)
    {
        this.mapQTypes = mapQTypes;
        
        this.custPerMin = customersPerMinute;
        this.start = start;
        this.end = end;
        allQs = new QGroup(5, numQs, "Overall", simTime); //represents data on all lanes
        allQs.setTextFill(TOTAL_FILL);
        stage = new Stage();
        
        Label lblTitle = new Label("Simulation Results");
        lblTitle.setFont(Font.font(null, FontWeight.BOLD, 14));
        int row = 0;
        GridPane gpTitle = new GridPane();
        gpTitle.setAlignment(Pos.CENTER);
        gpTitle.setPadding(insets);
        gpTitle.addRow(row++, lblTitle);
        
        gpReport = new GridPane();
        
        Button btnSave = new Button("Save and Close");
        btnSave.setOnAction(e ->
        {
            saveReport();
        });
        
        Button btnCancel = new Button("Cancel");
        btnCancel.setOnAction(e ->
        {
            stage.close();
        });
        HBox hbButtons = new HBox(10);
        hbButtons.setAlignment(Pos.CENTER);
        hbButtons.setPadding(insets);
        hbButtons.getChildren().addAll(btnSave, btnCancel);
        
        root = new BorderPane();
        root.setTop(gpTitle);
        root.setCenter(gpReport);
        root.setBottom(hbButtons);
    }
    
    public void viewReport()
    {
        gpReport.setAlignment(Pos.TOP_CENTER);
        gpReport.setPadding(insets);
        gpReport.setHgap(15);
        gpReport.setVgap(0);
        calculateTotals();
        int numColumns = createColumnHeaders();
        int numRows = createRowHeaders();
        displayData();
        scene = new Scene(root, numColumns * 125, numRows * 75);
        gpReport.setPrefWidth(scene.getWidth() - 20);
        stage.getIcons().addAll(Resources.getIcons());
        stage.setScene(scene);
        stage.setTitle("Results");
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }
    
    public void calculateTotals()
    {
        for (String qType: qTypes)
        {
            QGroup<CustomerQ> qGroup = mapQTypes.get(qType);
            qGroup.setTextFill(Q_TYPE_FILL);
            for (CustomerQ q: qGroup)
            {
                qGroup.setTotalAvgWaitTime(q.getAvgWaitTime());
                qGroup.setTotalAvgIdleTime(q.getAvgIdleTime());
                qGroup.setTotalGlitchTime(q.getTotalGlitchTime());
                qGroup.setTotalAvgQSize(q.getAvgQSize());
                qGroup.setTotalAvgNumCustHour(q.getAvgNumCustHour());
                qGroup.setMaxQSize(q.getMaxQLength());
                qGroup.setTotalNumItems(q.getTotalNumItemsScanned());
                qGroup.setNumAbandonedCarts(q.getTotalNumAbandonedCarts());
                
                qGroup.setNumCustCheckedOut(q.getTotalNumCustCheckedOut());
                
                allQs.setTotalAvgWaitTime(q.getAvgWaitTime());
                allQs.setTotalAvgIdleTime(q.getAvgIdleTime());
                allQs.setTotalGlitchTime(q.getTotalGlitchTime());
                allQs.setTotalAvgQSize(q.getAvgQSize());
                allQs.setTotalAvgNumCustHour(q.getAvgNumCustHour());
                allQs.setMaxQSize(q.getMaxQLength());
                allQs.setTotalNumItems(q.getTotalNumItemsScanned());
                allQs.setNumAbandonedCarts(q.getTotalNumAbandonedCarts());
            }
        }        
    }
    
    public int createColumnHeaders()
    {
        int colNum = 1;
        for (String qType: qTypes)
        {
            QGroup<CustomerQ> group = mapQTypes.get(qType);
            for (CustomerQ q: group)
            {
                Label qHeader = new Label(q.getName() + ": " + q.getID());
                qHeader.setAlignment(Pos.CENTER);
                qHeader.setWrapText(true);
                HBox hbHeader = new HBox(10);
                hbHeader.setAlignment(Pos.CENTER);
                hbHeader.getChildren().add(qHeader);
                gpReport.add(hbHeader, colNum, 0); 
                colNum++;
            }
            Label qTypeHeader = new Label(group.toString());
            qTypeHeader.setAlignment(Pos.CENTER);
            qTypeHeader.setWrapText(true);
            qTypeHeader.setFont(FONT);
            qTypeHeader.setTextFill(Q_TYPE_FILL);
            HBox hbHeader = new HBox(10);
            hbHeader.setAlignment(Pos.CENTER);
            hbHeader.getChildren().add(qTypeHeader);
            gpReport.add(hbHeader, colNum, 0); 
            colNum++;
        }
        Label totalHeader = new Label(allQs.toString());
        totalHeader.setAlignment(Pos.CENTER);
        totalHeader.setWrapText(true);
        totalHeader.setFont(FONT);
        totalHeader.setTextFill(TOTAL_FILL);
        HBox hbHeader = new HBox(10);
        hbHeader.setAlignment(Pos.CENTER);
        hbHeader.getChildren().add(totalHeader);
        gpReport.add(hbHeader, colNum, 0); 
        colNum++;
        
        return colNum;
    }
    
    public int createRowHeaders()
    {
        String[] rowHeaders = {"Avg. Wait Time (min:sec)", "Avg % Idle Time", "Total Glitch Time (min.)",
            "Avg. Length of Line", "Avg. Num. Customers/Hour", "Max Length of Line", "Avg. Num. Items Scanned/Hour",
            "Avg. Num. Abandonded Carts/Hour"};
        
        int rowNum = 1;
        
        for (String header: rowHeaders)
        {
            Label hdr = new Label(header);
            hdr.setWrapText(true);
            hdr.setAlignment(Pos.CENTER);
            hdr.setFont(Font.font(null, FontWeight.BOLD, 12));
            HBox hbHeader = new HBox(10);
            hbHeader.setAlignment(Pos.CENTER_LEFT);
            hbHeader.getChildren().add(hdr);
            gpReport.add(hbHeader, 0, rowNum);
            rowNum++;
        }
        
        for (int i = 0; i < rowNum; i++)
        {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(100 / rowNum);
            gpReport.getRowConstraints().add(row);            
        }
        return rowNum;
    }
        
    public void displayData()
    {
        int colNum = 1;
        for (String qType: qTypes)
        {
            QGroup<CustomerQ> qGroup = mapQTypes.get(qType);
            for (CustomerQ q: qGroup)
            {
                int rowNum = 1;
                gpReport.add(q.getAvgWaitTimeFormatted(), colNum, rowNum++);
                gpReport.add(q.getAvgIdleTimeFormatted(), colNum, rowNum++);
                gpReport.add(q.getTotalGlitchTimeFormatted(), colNum, rowNum++);
                gpReport.add(q.getAvgQSizeFormatted(), colNum, rowNum++);
                gpReport.add(q.getAvgNumCustHourFormatted(), colNum, rowNum++);
                gpReport.add(q.getMaxQLengthFormatted(), colNum, rowNum++);
                gpReport.add(q.getNumItemsPerHourFormatted(), colNum, rowNum++);
                gpReport.add(q.getAvgNumAbanCartsHourFormatted(), colNum, rowNum++);
                colNum++;
            }
            int rowNum = 1;
            gpReport.add(qGroup.getAvgWaitTimeFormatted(), colNum, rowNum++);
            gpReport.add(qGroup.getAvgIdleTimeNodeFormatted(), colNum, rowNum++);
            gpReport.add(qGroup.getAvgGlitchTimeFormatted(), colNum, rowNum++);
            gpReport.add(qGroup.getAvgQSizeFormatted(), colNum, rowNum++);

            gpReport.add(qGroup.getAvgNumCustHourFormatted(), colNum, rowNum++);
            gpReport.add(qGroup.getMaxQSizeFormatted(), colNum, rowNum++);
            gpReport.add(qGroup.getNumItemsPerHourFormatted(), colNum, rowNum++);
            gpReport.add(qGroup.getNumAbandonedCartsFormatted(), colNum, rowNum++);
            colNum++;
        }
        int rowNum = 1;
      
        gpReport.add(allQs.getAvgWaitTimeFormatted(), colNum, rowNum++);
        gpReport.add(allQs.getAvgIdleTimeNodeFormatted(), colNum, rowNum++);
        gpReport.add(allQs.getAvgGlitchTimeFormatted(), colNum, rowNum++);
        gpReport.add(allQs.getAvgQSizeFormatted(), colNum, rowNum++);
        gpReport.add(allQs.getAvgNumCustHourFormatted(), colNum, rowNum++);
        gpReport.add(allQs.getMaxQSizeFormatted(), colNum, rowNum++);
        gpReport.add(allQs.getNumItemsPerHourFormatted(), colNum, rowNum++);
        gpReport.add(allQs.getNumAbandonedCartsFormatted(), colNum, rowNum++);    
    }
    
    private void saveReport()
    {
        db = new DataBaseBridge();
        try
        {
            db.createInsertStatements();
            insertSimData();
            insertQGroupData();
            insertQData();
            alert("Saved", "Results saved to database");            
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            alert("Not saved", ex.getMessage());
        }
    }
    
    private void alert(String title, String content)
    {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
        alert.setOnCloseRequest(e ->
        {
            stage.close();
            CheckOutAreaStage.stage.close();
        });
    }
    
    private void insertSimData() throws SQLException
    {
        Integer[] numQTypes = new Integer[qTypes.length];
        for (int i = 0; i < qTypes.length; i++)
        {
            QGroup<CustomerQ> qGroup = mapQTypes.get(qTypes[i]);
            numQTypes[i] = qGroup.size();
        }

        int numSuperExQs = numQTypes[0];
        int numExQs = numQTypes[1];
        int numStdQs = numQTypes[2];  
        int numSelfQs = numQTypes[3];
        double avgIdle = allQs.getAvgIdleTimeDecimalFormatted();
        double totalGlitch = allQs.getTotalGlitchTime().toMinutes();
        double avgCustHour = allQs.getAvgNumCustHour();
        int maxQLength = allQs.getMaxQSize();
        double avgItemsHour = allQs.getNumItemsPerHour();
        double numCartsAbandonded = allQs.getAvgNumAbanCartsHour();
        long avgWaitTime = allQs.getAvgWaitTime().getSeconds();
        double avgQLength = allQs.getAvgQSize();
        
        db.insertSim(numSuperExQs, numExQs, numStdQs, avgIdle, totalGlitch, 
                avgCustHour, maxQLength, avgItemsHour, numCartsAbandonded, 
                avgWaitTime, avgQLength, numSelfQs, start, end, custPerMin);
    }
    
    private void insertQGroupData() throws SQLException
    {
        for (String qType: qTypes)
        {
            QGroup<CustomerQ> qGroup = mapQTypes.get(qType);
            
            int groupID = qGroup.getID();
            int numQs = qGroup.size();
            double avgIdle = qGroup.getAvgIdleTimeDecimalFormatted();
            double totalGlitch = qGroup.getTotalGlitchTime().toMinutes();
            double avgCustHour = qGroup.getAvgNumCustHour();
            int maxQLength = qGroup.getMaxQSize();
            double avgItemsHour = qGroup.getNumItemsPerHour();
            double numCartsAbandonded = qGroup.getAvgNumAbanCartsHour();
            long avgWaitTime = qGroup.getAvgWaitTime().getSeconds();
            double avgQLength = qGroup.getAvgQSize();
            String type = qGroup.toString();
            
            db.insertQGroup(groupID, numQs, avgIdle, totalGlitch, avgCustHour, maxQLength,
                    avgItemsHour, numCartsAbandonded, avgWaitTime, avgQLength,
                    type);
        }
    }
    
    private void insertQData() throws SQLException
    { 
        for (String qType: qTypes)
        {
            QGroup<CustomerQ> qGroup = mapQTypes.get(qType);
            for (CustomerQ q: qGroup)
            {
                int ID = q.getID();
                int groupID = q.getGroupID();
                double avgIdle = q.getAvgIdleTimeDecimalFormatted();
                double totalGlitch = q.getTotalGlitchTime().toMinutes();
                double avgCustHour = q.getAvgNumCustHour();
                int maxQLength = q.getMaxQLength();
                double itemsHour = q.getNumItemsScannedHour();
                double numCartsAbandonded = q.getTotalNumAbandonedCarts();
                long avgWaitTime = q.getAvgWaitTime().getSeconds();
                double avgQLength  = q.getAvgQSize();
                String type = q.getName();
                int maxItems = q.getMaxItemsAllowed();
                
                db.insertQ(ID, groupID, avgIdle, totalGlitch, avgCustHour, 
                        maxQLength, itemsHour, numCartsAbandonded, 
                        avgWaitTime, avgQLength, type, maxItems);
            }
        }
    }
}
