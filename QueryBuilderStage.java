package market;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

/*
Class that creates a form that allows user to query the database of simulation 
results.  The user can select a query by overall simulation results, QGroup results,
or individual queues.  The user can select the columns to display in the results
and filter data from the results
*/
public class QueryBuilderStage 
{
    private final static Insets DEFAULT_INSETS = new Insets(10, 10, 10, 10);
    private final Font DEFAULT_FONT = Font.font(null, FontWeight.BOLD, 12);
    
    private final String SIM = "SIM";
    private final String Q_TYPE = "Q_TYPE";
    private final String Q = "Q";
    
    private RadioButton rbSim;
    private RadioButton rbQType;
    private RadioButton rbQ;
    private GridPane gpTable;
    private GridPane gpColumns;
    private GridPane gpFilters;
    
    private int rowNumTable = 0;
    private int rowNumColumns = 0;
    private int rowNumFilters = 0;
    private int numAllowedFilterRows = 0;
    private int numActualFilterRows = 0;
    
    private final QueryTableSelector tableSelector = new QueryTableSelector();
    private final ScrollPane scrollPaneCol = new ScrollPane();
    
    private final Map<ComboBox<Pair<String, String>>, Pair<ComboBox<String>, TextField>> 
            mapComboBoxFieldControls = new HashMap();
    private final Stage stage = new Stage();
    
    public QueryBuilderStage()
    {
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 900, 675);
        stage.setScene(scene);
        stage.getIcons().addAll(Resources.getIcons());
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Query Builder");
      
        root.setCenter(createGridPanes(scene));
        root.setBottom(createButtonPane());
    }
    
    private Node createButtonPane()
    {    
        Button btnSearch = new Button("Search");
        btnSearch.setOnAction(e ->
        {
            queryDB();
        });
        
        Button btnClose = new Button("Close");
        btnClose.setOnAction(e ->
        {
           stage.close();
        });
        
        HBox hbButtons = new HBox(10);
        hbButtons.setAlignment(Pos.CENTER);
        hbButtons.setPadding(DEFAULT_INSETS);
        hbButtons.getChildren().addAll(btnSearch, btnClose);
        return hbButtons;
    }
    
    private void formatGridPanes(Scene scene)
    {
        gpTable = new GridPane();
        gpColumns = new GridPane();
        gpFilters = new GridPane();
        gpTable.setAlignment(Pos.CENTER_LEFT);
        gpTable.setPadding(DEFAULT_INSETS);
        gpTable.setHgap(20);
        gpTable.setVgap(10);
        gpTable.setMinWidth(scene.getWidth());
        
        gpColumns.setAlignment(Pos.TOP_LEFT);
        gpColumns.setHgap(20);
        gpColumns.setVgap(10);
        
        gpFilters.setAlignment(Pos.TOP_LEFT);
        gpFilters.setPadding(new Insets(0, 10, 0, 20));
        gpFilters.setHgap(20);
        gpFilters.setVgap(10);        
    }
    
    private ScrollPane createGridPanes(Scene scene)
    {
        formatGridPanes(scene);
        
        Label lblTable = new Label("Step 1: Select Table to Search ");
        lblTable.setFont(DEFAULT_FONT);
        gpTable.add(lblTable, 0, rowNumTable++, 4, 1);
        gpTable.add(createRadioButtonsPane(), 0, rowNumTable++, 3, 1);
        
        Label lblColumns = new Label("Step 2: Select Columns to Display ");
        lblColumns.setFont(DEFAULT_FONT);
        gpColumns.add(lblColumns, 0, rowNumColumns++);
        gpColumns.add(scrollPaneCol, 0, rowNumColumns++);
              
        HBox hBox = new HBox(10);
        hBox.setPadding(DEFAULT_INSETS);
        hBox.getChildren().addAll(gpColumns, gpFilters);        
        
        VBox vbGridPanes = new VBox(10);
        vbGridPanes.setAlignment(Pos.CENTER_LEFT);
        vbGridPanes.setPadding(DEFAULT_INSETS);
        vbGridPanes.getChildren().addAll(gpTable, hBox);
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(vbGridPanes);
        scrollPane.setPadding(DEFAULT_INSETS);
        return scrollPane;
    }
    
    private Node createRadioButtonsPane()
    {
        rbSim = new RadioButton("Simulation");
        rbSim.setId("SIMULATIONS");
        rbSim.setTooltip(new Tooltip("Retrieve data on overall simulation"));
        rbSim.setOnAction(e ->
        {
            setUpControls(SIM);
        });
        rbQType = new RadioButton("Queue Type");
        rbQType.setId("Q_GROUP");
        rbQType.setTooltip(new Tooltip("Retrieve data on each type of queue"));
        rbQType.setOnAction(e ->
        {
            setUpControls(Q_TYPE);
        });
        rbQ = new RadioButton("Queue");
        rbQ.setId("Q");
        rbQ.setTooltip(new Tooltip("Retrieve data on each individual queue"));
        rbQ.setOnAction(e ->
        {
            setUpControls(Q);
        });
        setUpControls(SIM);
        
        ToggleGroup tGroup = new ToggleGroup();
        rbSim.setToggleGroup(tGroup);
        rbQType.setToggleGroup(tGroup);
        rbQ.setToggleGroup(tGroup);
        rbSim.setSelected(true);
        
        HBox hbRadioButton = new HBox(30);
        hbRadioButton.setAlignment(Pos.CENTER);
        hbRadioButton.setPadding(DEFAULT_INSETS);
        hbRadioButton.getChildren().addAll(rbSim, rbQType, rbQ);
        return hbRadioButton;
    }
     
    private void setUpControls(String table)
    {
        GridPane gridPane = tableSelector.loadCheckBoxes(table);
        scrollPaneCol.setContent(gridPane);
        
        mapComboBoxFieldControls.clear();
        rowNumFilters = 0;
        gpFilters.getChildren().clear();
        Label lblFilters = new Label("Step 3: Add Filters(optional)");
        lblFilters.setFont(DEFAULT_FONT);
        gpFilters.add(lblFilters, 0, rowNumFilters++);
        
        numAllowedFilterRows = gridPane.getChildren().size();
        numActualFilterRows = 0;
        addFilterRow(table);
    }
    
    private void queryDB()
    {
        try
        {
            //Build SELECT
            List<String> SELECT = new ArrayList<>();
            GridPane fields = (GridPane)scrollPaneCol.getContent();
            ObservableList<Node> ol = fields.getChildren();
            for (Node node: ol)
            {
                CheckBox cb = (CheckBox) node;
                if (cb.isSelected())
                    SELECT.add(cb.getId());
            }

            //Build FROM
            String FROM;
            if (rbSim.isSelected())
                FROM = rbSim.getId();
            else if (rbQType.isSelected())
                FROM = rbQType.getId();
            else
                FROM = rbQ.getId();

            //Build WHERE
            Map<String, Pair<String, Double>> WHERE = new HashMap();
            boolean noDuplicates = true;
            Set<ComboBox<Pair<String, String>>> setComboBoxFields = mapComboBoxFieldControls.keySet();
            Iterator<ComboBox<Pair<String, String>>> iterator = setComboBoxFields.iterator();
            while (noDuplicates && iterator.hasNext())
            {
                ComboBox<Pair<String, String>> cbField = iterator.next();
                Pair<String, String> nameAlias = cbField.getValue();
                if (nameAlias != null)
                {
                    String colName = nameAlias.getLeft();
                    if (!WHERE.containsKey(colName))
                    {
                        Pair<ComboBox<String>, TextField> controls = mapComboBoxFieldControls.get(cbField);
                        ComboBox<String> cbOperator = controls.getLeft();
                        String operator = cbOperator.getValue();
                        TextField tfOperand = controls.getRight();
                        String operand = tfOperand.getText();
                        Double dblOperand = Double.parseDouble(operand);
                        Pair opOp = new Pair(operator, dblOperand);
                        WHERE.put(colName, opOp);
                    }
                    else
                        noDuplicates = false;                        
                }
            }

            if (noDuplicates)
            {
                DataBaseBridge database = new DataBaseBridge();
                database.buildDynamicQuery(SELECT, FROM, WHERE);
            }
            else
                displayAlert("Duplicate Entries", "Duplicate Fields Detected");
        }
        catch(NumberFormatException ex)
        {
            displayAlert("Input Error", "Numeric Value Required");
        }
    }
    
    private void displayAlert(String title, String message)
    {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
    
    private void addFilterRow(String table)
    {
        ComboBox<Pair<String, String>> cbField = new ComboBox();
        cbField.setMinWidth(100);
        cbField.setItems(tableSelector.populateComboBox(table));
        ComboBox cbOperator = new ComboBox();
        cbOperator.getItems().addAll("<","<=","=",">=",">");
        cbOperator.setValue("=");
        TextField tfOperand = new TextField();
        tfOperand.setMinWidth(100);
        Button btnAddFilterRow = new Button("+");
        btnAddFilterRow.setOnAction(e ->
        {
            if (numActualFilterRows < numAllowedFilterRows)
                addFilterRow(table);
        });

        gpFilters.add(cbField, 0, rowNumFilters);
        gpFilters.add(cbOperator, 1, rowNumFilters);
        gpFilters.add(tfOperand, 2, rowNumFilters);
        gpFilters.add(btnAddFilterRow, 3, rowNumFilters);
        rowNumFilters++;
        numActualFilterRows++;
        Pair<ComboBox<String>, TextField> controls = new Pair(cbOperator, tfOperand);
        mapComboBoxFieldControls.put(cbField, controls);        
    }
    
    public void show()
    {
        stage.show();
    }
}
