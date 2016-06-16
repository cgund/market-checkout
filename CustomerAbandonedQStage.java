package market;


import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
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
Displays data on individual customers who abandoned their carts in queue
during simulation runtime because they grew frustrated waiting in line
*/
public class CustomerAbandonedQStage 
{
    private final Stage stage;
    private final Insets DEFAULT_INSETS = new Insets(10, 10, 10, 10);

    public CustomerAbandonedQStage(Customer customer, CustomerQ q)
    {        
        BorderPane root = new BorderPane();
        root.setTop(createTitlePane(customer));
        root.setCenter(createGridPane(customer, q));
        root.setBottom(createButtonPane());
        
        stage = new Stage();
        stage.setScene(new Scene(root, 300, 250));
        stage.getIcons().addAll(Resources.getIcons());
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
    
    private Node createGridPane(Customer customer, CustomerQ q)
    {
        GridPane gpData = new GridPane();
        gpData.setAlignment(Pos.CENTER);
        gpData.setHgap(10);
        gpData.setVgap(10);
        gpData.setPadding(DEFAULT_INSETS);
        LocalTime arrival = customer.getArrivalTime();
        LocalTime departure = customer.getAbandonCartTime();
        
        long minutes = ChronoUnit.MINUTES.between(arrival, departure);
        int row = 0;
        gpData.addRow(row++, new Label("Number of Items: "), 
                new Label(String.valueOf(customer.getNumItems())));
        gpData.addRow(row++, new Label("Lane Type: "), 
                new Label(String.valueOf(q.getName())));
        gpData.addRow(row++, new Label("Lane Number: "),
                new Label(String.valueOf(q.getID())));
        gpData.addRow(row++, new Label("Arrival Time: "), 
                new Label(String.valueOf(arrival)));
        gpData.addRow(row++, new Label("Abandon Cart Time: "),
                new Label(String.valueOf(departure)));
        gpData.addRow(row++, new Label("Time in Line: "),
                new Label(String.valueOf(minutes) + " minutes"));
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
