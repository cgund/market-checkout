
import java.time.LocalTime;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/*
Allows user to enter the parameters for the simulation and validates input
*/
public class SimSetUpStage
{
    private Stage stage;
    private static final int MAX_NUM_LANES = 10;
    
    int numRows = 0;
    private final TextField tfArrivalRate = new TextField();
    private final TextField tfNumSuperExLanes = new TextField();
    private final TextField tfNumExLanes = new TextField();
    private final TextField tfNumStandardLanes = new TextField();
    private final TextField tfNumSelfLanes = new TextField();
    private final TextField tfSuperExMaxItems = new TextField();
    private final TextField tfExMaxItems = new TextField();
    private final TextField tfSelfMaxItems = new TextField();
    private final TextField tfAvgNumItems = new TextField();
    private final TextField tfItemsPerMinute = new TextField();
    ComboBox<LocalTime> cbStartTime;
    ComboBox<LocalTime> cbEndTime;
    
    public SimSetUpStage()
    {
        stage = new Stage();
    }
    
    public void show()
    {
        BorderPane root = new BorderPane();
        
        /*
        Default parameters.  Set for testing convienence
        */
        tfArrivalRate.setText("7");
        tfNumSuperExLanes.setText("2");
        tfNumExLanes.setText("2");
        tfNumStandardLanes.setText("5");
        tfNumSelfLanes.setText("1");
        tfSuperExMaxItems.setText("10");
        tfExMaxItems.setText("15");
        tfSelfMaxItems.setText("20");
        tfAvgNumItems.setText("24");
        tfItemsPerMinute.setText("12");
       
        root.setTop(createTitlePane());
        root.setCenter(createFieldsPane());
        root.setBottom(createButtonsPane());
        
        Scene scene = new Scene(root, 500, numRows * 45);
        stage.setTitle("Sim Setup");
        stage.getIcons().addAll(Resources.getIcons());
        stage.setScene(scene);
        stage.show();      
    }
    
    public void close()
    {
        stage.close();
    }
    
    private Node createTitlePane()
    {
        HBox hbTitle = new HBox(10);
        hbTitle.setAlignment(Pos.CENTER);
        hbTitle.setPadding(new Insets(10, 10, 10, 10));
        Label lblTitle = new Label("Enter Sim Parameters");
        lblTitle.setFont(Font.font(null, FontWeight.BOLD, 12));
        hbTitle.getChildren().add(lblTitle);
        return hbTitle;
    }
    
    private Node createFieldsPane()
    {
        ObservableList<LocalTime> data = fillObservableList();
        
        cbStartTime = new ComboBox(data); 
        cbStartTime.setValue(LocalTime.of(8, 00));    
        cbEndTime = new ComboBox(data);
        cbEndTime.setValue(LocalTime.of(8, 15));

        GridPane gpFields = new GridPane();
        gpFields.setAlignment(Pos.CENTER);
        gpFields.setPadding(new Insets(10, 10, 10, 10));
        gpFields.setHgap(10);
        gpFields.setVgap(10);
        gpFields.addRow(numRows++, new Label("Customer Arrival Rate (customers/minute): "), tfArrivalRate);
        gpFields.addRow(numRows++, new Label("# of SuperExpress Lanes: "), tfNumSuperExLanes);
        gpFields.addRow(numRows++, new Label("# of Express Lanes: "), tfNumExLanes);
        gpFields.addRow(numRows++, new Label("# of Standard Lanes"), tfNumStandardLanes);
        gpFields.addRow(numRows++, new Label("# of Self Checkout Lanes"), tfNumSelfLanes);
        gpFields.addRow(numRows++, new Label("Max. Items Allowed SuperExpress Lanes: "), tfSuperExMaxItems);
        gpFields.addRow(numRows++, new Label("Max. Items Allowed Express Lanes: "), tfExMaxItems);
        gpFields.addRow(numRows++, new Label("Max. Items Allowed Self Checkout Lanes: "), tfSelfMaxItems);
        gpFields.addRow(numRows++, new Label("Avg Num of Items per Customer: "), tfAvgNumItems);
        gpFields.addRow(numRows++, new Label("Processing Speed (items/minute): "), tfItemsPerMinute);
        gpFields.addRow(numRows++, new Label("Simulation Start Time: "), cbStartTime);
        gpFields.addRow(numRows++, new Label("Simulation End Time: "), cbEndTime);
        
        return gpFields;
    }
    
    private Node createButtonsPane()
    {
        CheckBox cbAnimate = new CheckBox("Animate");
        
        Button btnRun = new Button("Run");
        btnRun.setOnAction(e ->
        {
            try
            {
                int customersPerMinute = Integer.parseInt(tfArrivalRate.getText());
                int numSuper = Integer.parseInt(tfNumSuperExLanes.getText());
                int numEx = Integer.parseInt(tfNumExLanes.getText());
                int numStd = Integer.parseInt(tfNumStandardLanes.getText());
                int numSelf = Integer.parseInt(tfNumSelfLanes.getText());
                int maxSuper = Integer.parseInt(tfSuperExMaxItems.getText());
                int maxEx = Integer.parseInt(tfExMaxItems.getText());
                int maxSelf = Integer.parseInt(tfSelfMaxItems.getText());
                int numAvgItems = Integer.parseInt(tfAvgNumItems.getText());
                double processingSpeed = Double.parseDouble(tfItemsPerMinute.getText());
                LocalTime startTime = cbStartTime.getValue();
                LocalTime endTime = cbEndTime.getValue();
                
                Number[] inputs = {customersPerMinute, numSuper, numEx, numStd, numSelf,
                                   maxSuper, maxEx, numAvgItems, processingSpeed};
                if (inputGreaterThan(inputs))
                {
                    if (numLanesLessThan(numSuper, numEx, numStd, numSelf))
                    {
                        if (startTime.isBefore(endTime))
                        {
                            CheckOutAreaStage manager = new CheckOutAreaStage(customersPerMinute, 
                                    numSuper, numEx, numStd, numSelf, maxSuper, maxEx, maxSelf, 
                                    numAvgItems, processingSpeed, startTime, endTime);
                            manager.show(cbAnimate.isSelected());                              
                        }
                        else
                            displayAlert("Start time must be earlier than end time");
                    }
                    else
                        displayAlert("Max Number of Lanes = 10");
                }
                else
                    displayAlert("Values must be 1 or greater");
                
            }
            catch (NumberFormatException ex)
            {
                displayAlert("Integer values required");
            }
        });

        HBox hbButton = new HBox(10);
        hbButton.setAlignment(Pos.CENTER);
        hbButton.setPadding(new Insets(10, 10, 0, 10));
        hbButton.getChildren().addAll(btnRun, cbAnimate);
        
        Button btnClose = new Button("Close");
        btnClose.setOnAction(e ->
        {
            stage.close();
        });
        
        HBox hbCloseButton = new HBox(10);
        hbCloseButton.setAlignment(Pos.CENTER);
        hbCloseButton.setPadding(new Insets(10, 10, 10, 10));
        hbCloseButton.getChildren().addAll(btnClose);
        
        VBox vbButton = new VBox();
        vbButton.setAlignment(Pos.CENTER);
        vbButton.getChildren().addAll(hbButton, hbCloseButton);
        return vbButton;
    }
    
    private void displayAlert(String message)
    {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Invalid Input");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
     
    /*
    Fills ComboBox with LocalTime values in 15 minute increments
    */
    private ObservableList<LocalTime> fillObservableList()
    {
        ObservableList<LocalTime> data = FXCollections.observableArrayList();
        LocalTime time = LocalTime.of(0, 0);
        for (int i = 0; i < 96; i++)
        {
            time = time.plusMinutes(15);
            data.add(time);
        }
        return data;
    }
    
    /*
    Checks for inputs less than 1
    */
    public boolean inputGreaterThan(Number[] inputs)
    {
        boolean valid = true;
        int index = 0;
        while (valid & index < inputs.length)
        {
            int value = inputs[index].intValue();
            if (value < 1)
            {
                valid = false;
            }
            index++;
        }
        return valid;
    }
    
    /*
    Determines if max num lanes has been exceeded
    */
    private boolean numLanesLessThan(int numSuper, int numEx, int numStd, int numSelf)
    {
        boolean valid = true;
        if (MAX_NUM_LANES < numSuper + numEx + numStd + numSelf)
        {
            valid = false;
        }
        return valid;
    }
}
