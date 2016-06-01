
import java.time.LocalTime;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
/*
Displays data on individual customers who have been checked out during the 
simulation runtime
*/
class CustomerCheckedOutStage 
{
    private final Stage stage;
    private final Insets DEFAULT_INSETS = new Insets(10, 10, 10, 10);    
    
    public CustomerCheckedOutStage(Customer customer)
    {                              
        BorderPane root = new BorderPane();
        root.setTop(createTitlePane(customer));
        root.setCenter(createGridPane(customer));
        root.setBottom(createButtonPane());
        
        stage = new Stage();
        stage.getIcons().addAll(Resources.getIcons());
        stage.setScene(new Scene(root, 300, 300));
        stage.setTitle("Customer Data");

    }
    
    private Node createTitlePane(Customer customer)
    {
        HBox hbTitle = new HBox();
        hbTitle.setAlignment(Pos.CENTER);
        hbTitle.setPadding(DEFAULT_INSETS);
        hbTitle.getChildren().add(new Label(customer.toString()));
        return hbTitle;
    }
    
    private Node createGridPane(Customer customer)
    {
        GridPane gpData = new GridPane();
        gpData.setAlignment(Pos.CENTER);
        gpData.setHgap(10);
        gpData.setVgap(10);
        gpData.setPadding(DEFAULT_INSETS);
        int row = 0;
        gpData.addRow(row++, new Label("Number of Items: "), 
                new Label(String.valueOf(customer.getNumItems())));
        gpData.addRow(row++, new Label("Arrival Time: "), 
                new Label(String.valueOf(customer.getArrivalTime())));
        LocalTime optimal = customer.getOptimalCheckOutTime();
        gpData.addRow(row++, new Label("Optimal Checkout Time: "), 
                new Label(optimal.toString()));
        gpData.addRow(row++, new Label("Actual Checkout Time: "), 
                new Label(String.valueOf(customer.getActualCheckOutTime())));
        gpData.addRow(row++, new Label("Total Wait Time: "), 
                new Label(String.valueOf(customer.getFinalWaitTime().toMinutes()) + " minutes"));
        if (customer.hasSwitchedLanes())
            gpData.addRow(row++, new Label("Switched Lanes: "), new Label("yes"));
        else
            gpData.addRow(row++, new Label("Switched Lanes: "), new Label("no"));
        if (customer.getCheated())
            gpData.addRow(row++, new Label("Cheated: "), new Label("yes"));            
        else
            gpData.addRow(row++, new Label("Cheated: "), new Label("no")); 
        
        return gpData;
    }
    
    private Node createButtonPane()
    {
        Button btnClose = new Button("Close");
        btnClose.setOnAction(e ->
        {
            stage.close();
        });
        HBox hbButton = new HBox();
        hbButton.setAlignment(Pos.CENTER);
        hbButton.setPadding(DEFAULT_INSETS);
        hbButton.getChildren().add(btnClose);
        
        return hbButton;
    }
    
    public void show()
    {
        stage.show();
    }
}
