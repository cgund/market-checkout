
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;

/*
Class that is used to build a query.  
*/
public class QueryTableSelector 
{
    private final String SIM = "SIM";
    private final String Q_TYPE = "Q_TYPE";
    private final String Q = "Q";
    
    private final String[] Q_COL_ALIASES = {"Id", "Group Id", "Sim Id", "Cashier Idle (%)",
      "Glitch Time (min)", "Num Customer/hour", "Max Q Length", "Items Scanned/hour",
      "Number Abandonded Carts", "Avg. Wait Time (seconds)", "Avg. Length", 
      "Max Items Allowed", "Q Type"};
    
    private final String[] Q_COL_NAMES = {"ID", "ID_GROUP", "ID_SIM", "AVG_PERCENT_IDLE",
      "TOTAL_GLITCH_TIME", "AVG_NUM_CUST_HOUR", "MAX_Q_LENGTH", "AVG_NUM_ITEMS_SCANNED_HOUR",
      "NUM_ABAN_CARTS", "AVG_WAIT_TIME", "AVG_Q_LENGTH", "MAX_ITEMS_ALLOWED", "Q_TYPE"};
    
    private final String[] Q_TYPE_COL_ALIASES = {"Id", "Sim Id", "Number Q's", "Cashier Idle (%)",
      "Glitch Time (min)", "Avg. Num Customer/hour", "Max Q Length", "Avg. Items Scanned/hour",
      "Number Abandonded Carts", "Avg. Wait Time (seconds)", "Avg. Length", "Type"};
    
    private final String[] Q_TYPE_COL_NAMES = {"ID", "ID_SIM", "NUM_QS", "AVG_PERCENT_IDLE",
      "TOTAL_GLITCH_TIME", "AVG_NUM_CUST_HOUR", "MAX_Q_LENGTH", "AVG_NUM_ITEMS_SCANNED_HOUR",
      "NUM_ABAN_CARTS", "AVG_WAIT_TIME", "AVG_Q_LENGTH", "TYPE"};
    
    private final String[] SIM_COL_ALIASES = {"Id", "Num. Super Express Q's", "Num. Express Q's",
      "Num. Standard Q's", "Num. Self Q's", "Cashier Idle (%)", "Glitch Time (min)", 
      "Avg. Num Customer/hour", "Max Q Length", "Avg. Items Scanned/hour",
      "Number Abandonded Carts", "Avg. Wait Time (seconds)", "Avg. Length", "Customer/min",
      "Start Time", "End Time"};
    
    private final String[] SIM_COL_NAMES = {"ID", "NUM_SUPER_EX_QS", "NUM_EX_QS", "NUM_STD_QS",
      "NUM_SELF_QS", "AVG_PERCENT_IDLE", "TOTAL_GLITCH_TIME", "AVG_NUM_CUST_HOUR", "MAX_Q_LENGTH", 
      "AVG_NUM_ITEMS_SCANNED_HOUR", "NUM_ABAN_CARTS", "AVG_WAIT_TIME", "AVG_Q_LENGTH", "CUST_PER_MIN",
      "START_TIME", "END_TIME"};
    
    /*
    Holds references to the CheckBoxes that correspond to each table in database
    */
    private final GridPane gPaneQ = new GridPane();
    private final GridPane gPaneQ_Type = new GridPane();
    private final GridPane gPaneSim = new GridPane();
    
    /*
    Holds references to Pairs that correspond to the fields/columns in each table
    */
    private final ObservableList<Pair<String, String>> olQ = FXCollections.observableArrayList();
    private final ObservableList<Pair<String, String>> olQ_Type = FXCollections.observableArrayList();
    private final ObservableList<Pair<String, String>> olSim = FXCollections.observableArrayList();
    
    /*
    Builds the GridPanes and ObservableLists that are used depending on which table
    the user opts to search in the database
    */
    public QueryTableSelector()
    {
        int length = Q_COL_NAMES.length;
        for (int i = 0; i < length; i++)
        {
            CheckBox checkBox = new CheckBox(Q_COL_ALIASES[i]);
            checkBox.setId(Q_COL_NAMES[i]);
            gPaneQ.addRow(i, checkBox);
            if (i < length - 1)
                olQ.add(new Pair(Q_COL_NAMES[i], Q_COL_ALIASES[i]));            
        }
        
        length = Q_TYPE_COL_NAMES.length;
        for (int i = 0; i < length; i++)
        {
            CheckBox checkBox = new CheckBox(Q_TYPE_COL_ALIASES[i]);
            checkBox.setId(Q_TYPE_COL_NAMES[i]);
            gPaneQ_Type.addRow(i, checkBox);
            if (i < length - 1)
                olQ_Type.add(new Pair(Q_TYPE_COL_NAMES[i],Q_TYPE_COL_ALIASES[i]));
        }
        
        length = SIM_COL_NAMES.length;
        for (int i = 0; i < length; i++)
        {       
            CheckBox checkBox = new CheckBox(SIM_COL_ALIASES[i]);
            checkBox.setId(SIM_COL_NAMES[i]);
            gPaneSim.addRow(i, checkBox);
            if (i < length - 2)
                olSim.add(new Pair(SIM_COL_NAMES[i], SIM_COL_ALIASES[i]));
        }
    }
    
    public GridPane loadCheckBoxes(String searchType)
    {
        GridPane gPane;
        switch (searchType) 
        {
            case Q:
                clearCheckBoxes(gPaneQ);
                gPane = gPaneQ;
                break;
            case Q_TYPE:
                clearCheckBoxes(gPaneQ_Type);
                gPane = gPaneQ_Type;
                break;
            default:
                clearCheckBoxes(gPaneSim);
                gPane = gPaneSim;
                break;
        }  
        gPane.setPadding(new Insets(10, 10, 10, 10));
        gPane.setAlignment(Pos.CENTER);
        gPane.setHgap(10);
        gPane.setVgap(10);
        return gPane;
    }
    
    public void clearCheckBoxes(GridPane gridPane)
    {
        ObservableList<Node> olCheckBoxes = gridPane.getChildren();
        olCheckBoxes.stream().map((node) -> (CheckBox)node).forEach((checkBox) -> 
        {
            checkBox.setSelected(false);
        });
    }
    
    
    public ObservableList<Pair<String, String>> populateComboBox(String searchType)
    {
        ObservableList<Pair<String, String>> ol;
        switch(searchType)
        {
            case Q:
                ol = olQ;
                break;
            case Q_TYPE:
                ol = olQ_Type;
                break;
            default:
                ol = olSim;
                break;
        }
        return ol;
    }
}
