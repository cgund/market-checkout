
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.util.Callback;

public class QueryResultsStage 
{
    private static final String DATABASE_URL = "jdbc:derby://localhost:1527/CheckOut";
    private static final String USER_NAME = "blank";
    private static final String PASSWORD = "blank";
    
    private Connection connection;
    private PreparedStatement psSelectAll;
    private PreparedStatement psSelectMaxId;
    private PreparedStatement psInsertSim;
    private PreparedStatement psInsertQGroup;
    private PreparedStatement psInsertQ;
    
    private Integer simID;
    
    public QueryResultsStage()
    {
        try
        {
            connection = DriverManager.getConnection(DATABASE_URL, USER_NAME, PASSWORD);

            psSelectAll = connection.prepareStatement("SELECT * FROM SIMULATIONS");
            psSelectMaxId = connection.prepareStatement("SELECT MAX(ID) FROM SIMULATIONS");
            psInsertSim = connection.prepareStatement("INSERT INTO SIMULATIONS"
                    + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            psInsertQGroup = connection.prepareStatement("INSERT INTO Q_GROUP"
                    + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            psInsertQ = connection.prepareStatement("INSERT INTO Q"
                    + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    
            simID = selectMaxSimId() + 1;
        }
        catch(SQLException ex)
        {
            displayAlert(ex.getMessage());
        }        
    }
    
    public static void selectDynamicQuery(List<String> colNames, String table, Map<String, Pair<String, Double>> hmFilter)
    {
        StringBuilder sb = new StringBuilder();
        if (colNames.isEmpty())
        {
            sb.append("SELECT * ");
        }
        else
        {
            sb.append("SELECT ");
            for (int i = 0; i < colNames.size(); i++)
            {
                if (i != colNames.size() - 1)
                {
                    sb.append(colNames.get(i)).append(", ");
                }
                else
                {
                    sb.append(colNames.get(i)).append(" ");
                }
            }                
        }

        sb.append("FROM ").append(table);

        Set<Entry<String, Pair<String, Double>>> entrySet = hmFilter.entrySet();
        if (entrySet.size() > 0)
        {
            sb.append(" WHERE");
            Iterator<Entry<String, Pair<String, Double>>> iterator = entrySet.iterator();
            int entrySetSize = entrySet.size();
            int entryNum = 0;
            while (iterator.hasNext())
            {
                Entry<String, Pair<String, Double>> entry = iterator.next();
                String field = entry.getKey();
                Pair<String, Double> opOp = entry.getValue();
                String operator = opOp.getLeft();
                Double operand = opOp.getRight();
                sb.append(" ");
                sb.append(field);
                sb.append(" ");
                sb.append(operator);
                sb.append(" ");
                sb.append(operand);
                if (entryNum != entrySetSize - 1)
                {
                    sb.append(" AND");
                }
                entryNum++;
            }                
        }
        createConnection(sb.toString());          
    }
    
    private static void createConnection(String statement)
    {
        try
        {
        Connection connectionStatic = DriverManager.getConnection(DATABASE_URL, USER_NAME, PASSWORD);
        PreparedStatement psSelectDynamic = connectionStatic.prepareStatement(statement);

        ResultSet resultSet = null;
        resultSet = psSelectDynamic.executeQuery();
        displayQuery(resultSet); 
        }
        catch(SQLException ex)
        {
            displayAlert(ex.getMessage());            
        }
    }
        
    public void selectAllQuery()
    {
        try
        {
            ResultSet resultSet = null;
            resultSet = psSelectAll.executeQuery();
            displayQuery(resultSet);
        }
        catch(SQLException ex)
        {
            displayAlert(ex.getMessage());
        }        
    }
    
    public final Integer selectMaxSimId()
    {
        ResultSet resultSet = null;
        Integer max = 0;        
        try
        {
            resultSet = psSelectMaxId.executeQuery();
            ResultSetMetaData meta = resultSet.getMetaData();
            int numColumns = meta.getColumnCount();
            while (resultSet.next())
            {
                for (int i = 1; i <= numColumns; i++)
                {
                    max = (Integer)resultSet.getObject(i);
                }
            }
        }
        catch(SQLException ex)
        {
            displayAlert(ex.getMessage());
        }
        if (max == null)
        {
            return 0;
        }
        else
        {
            return max;
        }
    }
    
    public void insertSim(int numSuperExQs, int numExQs, int numStdQs,
            double avgIdle, double totalGlitch, double avgCustHour, int maxQLength,
            double avgItemsHour, double numCartsAbandonded, long avgWaitTime,
            double avgQLength, int numSelfQs, LocalTime start, LocalTime end,
            int custPerMin)
    { 
        try
        {
            psInsertSim.setInt(1, simID);
            psInsertSim.setInt(2, numSuperExQs);
            psInsertSim.setInt(3, numExQs);
            psInsertSim.setInt(4, numStdQs);
            psInsertSim.setDouble(5, avgIdle);
            psInsertSim.setDouble(6, totalGlitch);
            psInsertSim.setDouble(7, avgCustHour);
            psInsertSim.setInt(8, maxQLength);
            psInsertSim.setDouble(9, avgItemsHour);
            psInsertSim.setDouble(10, numCartsAbandonded);
            psInsertSim.setLong(11, avgWaitTime);
            psInsertSim.setDouble(12, avgQLength);
            psInsertSim.setInt(13, numSelfQs);
            java.sql.Time startTime = new java.sql.Time(start.getHour(), start.getMinute(), start.getSecond());
            psInsertSim.setTime(14, startTime);
            java.sql.Time endTime = new java.sql.Time(end.getHour(), end.getMinute(), end.getSecond());
            psInsertSim.setTime(15, endTime);
            psInsertSim.setInt(16, custPerMin);
            
            psInsertSim.executeUpdate();
        }
        catch(SQLException ex)
        {
            displayAlert(ex.getMessage());
        }
    }
    
    public void insertQGroup(int groupID, int numQs, double avgIdle, double totalGlitch, 
            double avgCustHour, int maxQLength,double avgItemsHour, double numCartsAbandonded, 
            long avgWaitTime, double avgQLength, String type)
    {
        try
        {
            psInsertQGroup.setInt(1, groupID);
            psInsertQGroup.setInt(2, simID);
            psInsertQGroup.setInt(3, numQs);
            psInsertQGroup.setDouble(4, avgIdle);
            psInsertQGroup.setDouble(5, totalGlitch);
            psInsertQGroup.setDouble(6, avgCustHour);
            psInsertQGroup.setInt(7, maxQLength);
            psInsertQGroup.setDouble(8, avgItemsHour);
            psInsertQGroup.setDouble(9, numCartsAbandonded);
            psInsertQGroup.setLong(10, avgWaitTime);
            psInsertQGroup.setDouble(11, avgQLength);
            psInsertQGroup.setString(12, type);
            
            psInsertQGroup.executeUpdate();
        }
        catch(SQLException ex)
        {
            displayAlert(ex.getMessage());
        }           
    }
    
    public void insertQ(int ID, int groupID, double avgIdle, double totalGlitch, 
            double avgCustHour, int maxQLength,double avgItemsHour, double numCartsAbandonded, 
            long avgWaitTime, double avgQLength, String type, int maxItemsAllowed)
    {
        try
        {
            psInsertQ.setInt(1, ID);
            psInsertQ.setInt(2, groupID);
            psInsertQ.setInt(3, simID);
            psInsertQ.setDouble(4, avgIdle);
            psInsertQ.setDouble(5, totalGlitch);
            psInsertQ.setDouble(6, avgCustHour);
            psInsertQ.setInt(7, maxQLength);
            psInsertQ.setDouble(8, avgItemsHour);
            psInsertQ.setDouble(9, numCartsAbandonded);
            psInsertQ.setLong(10, avgWaitTime);
            psInsertQ.setDouble(11, avgQLength);
            psInsertQ.setString(12, type);
            psInsertQ.setInt(13, maxItemsAllowed);
            
            psInsertQ.executeUpdate();
        }
        catch(SQLException ex)
        {
            displayAlert(ex.getMessage());
        }
    }
    
    private static void displayQuery(ResultSet resultSet)
    {
        try
        {
            TableView tableview = new TableView();
            ObservableList<ObservableList> data = FXCollections.observableArrayList();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int colCount = metaData.getColumnCount();
            for(int i = 0 ; i < colCount; i++)
            {
                final int j = i;                
                TableColumn col = new TableColumn(resultSet.getMetaData().getColumnName(i + 1));
                col.setCellValueFactory(new Callback<CellDataFeatures<ObservableList,String>,ObservableValue<String>>()
                {                    
                    public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) 
                    {                                                                                              
                        return new SimpleStringProperty(param.getValue().get(j).toString());                        
                    }                    
                });
               
                tableview.getColumns().addAll(col); 
            }
            
            while(resultSet.next())
            {
                //Iterate Row
                ObservableList<String> row = FXCollections.observableArrayList();
                for(int i = 1 ; i <= colCount; i++)
                {
                    //Iterate Column
                    row.add(resultSet.getString(i));
                }
                data.add(row);
            }
            tableview.setItems(data);
            
            Scene scene = new Scene(tableview);
            Stage stage = new Stage();
            stage.getIcons().addAll(Resources.getIcons());
            stage.setWidth(colCount * 100);
            String tableName = metaData.getTableName(1);
            stage.setTitle("Query Results for " + tableName);

            stage.setScene(scene);
            stage.show();
        }
        catch (SQLException ex)
        {
            displayAlert(ex.getMessage());
        }
    }
    
    private static void displayAlert(String message)
    {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
        
}
