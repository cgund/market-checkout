package market;


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
Displays data on individual customers who are still waiting in the queue
at the end of the simulation runtime
*/
class CustomerInQStage 
{
    private final Stage stage;
    private static final Insets INSETS = new Insets(10, 10, 10, 10); 
    
    public CustomerInQStage(Customer customer)
    {           
        BorderPane root = new BorderPane();
        root.setTop(createTitlePane(customer));
        root.setCenter(createGridPane(customer));
        root.setBottom(createButtonPane());
        
        stage = new Stage();
        stage.getIcons().addAll(Resources.getIcons());
        stage.setScene(new Scene(root, 250, 200));
        stage.setTitle("Customer Data");        
    }
    
    private Node createTitlePane(Customer customer)
    {
        HBox hbTitle = new HBox();
        hbTitle.setAlignment(Pos.CENTER);
        hbTitle.setPadding(INSETS);
        hbTitle.getChildren().add(new Label(customer.toString()));
        return hbTitle;
    }
    
    private Node createGridPane(Customer customer)
    {
        GridPane gpData = new GridPane();
        gpData.setAlignment(Pos.CENTER);
        gpData.setHgap(10);
        gpData.setVgap(10);
        gpData.setPadding(INSETS);
        int row = 0;
        gpData.addRow(row++, new Label("Number of Items: "), 
                new Label(String.valueOf(customer.getNumItems())));
        gpData.addRow(row++, new Label("Arrival Time: "), 
                new Label(String.valueOf(customer.getArrivalTime())));
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
        hbButton.setPadding(INSETS);
        hbButton.getChildren().add(btnClose);
        return hbButton;
    }
    
    public void show()
    {
        stage.show();
    }
}
