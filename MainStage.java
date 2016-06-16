package market;


import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

/*
Portal to application.  User selects between running a new simulation or 
querying previous simulations in the database
*/
public class MainStage extends Application 
{   
    private static final Insets DEFAULT_INSETS = new Insets(10, 10, 10, 10);
    private RadioButton rbRunSim;
    
    @Override
    public void start(Stage primaryStage) 
    {
        Resources.buildResources();
        BorderPane root = new BorderPane();
        
        root.setLeft(createOptionsPane());
        root.setBottom(createButtonPane());
        root.setRight(createLogoPane());
        
        Scene scene = new Scene(root, 250, 150);
        primaryStage.setResizable(false);
        primaryStage.setTitle("Checkout Sim");
        primaryStage.getIcons().addAll(Resources.getIcons());
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        
    }
    
    private Node createTitlePane()
    {
        HBox hbTitle = new HBox(20);
        hbTitle.setAlignment(Pos.CENTER_LEFT);
        hbTitle.setPadding(DEFAULT_INSETS);
        Label lblTitle = new Label("Main Menu");
        lblTitle.setFont(Font.font(null, FontWeight.BOLD, 12));
        Image imgHelp = new Image(getClass().getResourceAsStream("resources/about.png"));
        ImageView imgViewHelp = new ImageView(imgHelp);
        imgViewHelp.setFitWidth(15);
        imgViewHelp.setPreserveRatio(true);
        imgViewHelp.setSmooth(true);
        imgViewHelp.setOnMouseClicked(e ->
        {
            
        });
        hbTitle.getChildren().addAll(lblTitle, imgViewHelp);
        
        return hbTitle;        
    }
    
    private Node createOptionsPane()
    {        
        rbRunSim = new RadioButton("Run new simulation");
        rbRunSim.setPadding(new Insets(0, 0, 5, 0));
        RadioButton rbQuery = new RadioButton("Query database");
        ToggleGroup toggleGroup = new ToggleGroup();
        rbRunSim.setToggleGroup(toggleGroup);
        rbQuery.setToggleGroup(toggleGroup);
        rbRunSim.setSelected(true);
        
        VBox vbOptions = new VBox(10);
        vbOptions.setAlignment(Pos.CENTER_LEFT);
        vbOptions.setPadding(DEFAULT_INSETS);
        vbOptions.getChildren().addAll(createTitlePane(), rbRunSim, rbQuery);
        
        return vbOptions;
    }
    
    private Node createButtonPane()
    {
        Button btnGo = new Button("Go");
        btnGo.setOnMouseClicked(e ->
        {
            if (rbRunSim.isSelected())
            {
                SimSetUpStage sim = new SimSetUpStage();
                sim.show();
            }
            else
            {
                QueryBuilderStage dQ = new QueryBuilderStage();
                dQ.show();
            }
        });
        
        HBox hbButton = new HBox(10);
        hbButton.setAlignment(Pos.CENTER_LEFT);
        hbButton.setPadding(new Insets(10, 0, 10, 50));
        hbButton.getChildren().add(btnGo);
        
        return hbButton;
    }
    
    private Node createLogoPane()
    {
        try
        {
            Image imgCart = new Image(getClass().getResourceAsStream("resources/icon.png"));
            ImageView imgViewCart = new ImageView(imgCart);
            imgViewCart.setFitWidth(60);
            imgViewCart.setPreserveRatio(true);
            imgViewCart.setSmooth(true);
            
            VBox vbImage = new VBox();
            vbImage.setPadding(new Insets(45, 30, 0, 0));
            vbImage.setAlignment(Pos.CENTER);
            vbImage.getChildren().add(imgViewCart);
            return vbImage;
        }
        catch (NullPointerException ex)
        {
            System.err.println(ex.getMessage());
        }
        return null;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
}
