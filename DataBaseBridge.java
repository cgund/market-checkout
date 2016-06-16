package market;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.Driver;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import org.apache.derby.drda.NetworkServerControl;
import org.apache.derby.jdbc.EmbeddedDriver;

/*
Class to provide the logic for starting the server, making a connection to the
database, creating and executing prepared statements, and building and executing
dynamic queries
*/
public class DataBaseBridge 
{
    private NetworkServerControl server =  null;
    private Connection connection = null;
    private static final String DATABASE_URL = "jdbc:derby:CheckOut";
    private static final String USER_NAME = "blank";
    private static final String PASSWORD = "blank";

    private PreparedStatement psSelectMaxId = null;
    private PreparedStatement psInsertSim = null;
    private PreparedStatement psInsertQGroup = null;
    private PreparedStatement psInsertQ = null;
    
    private Integer simID;
    
    public DataBaseBridge()
    {
        try
        {
            startServer();
            createConnection();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            displayAlert(ex.getMessage());
        }
    }
    
    private void startServer() throws Exception
    {
        server = new NetworkServerControl(InetAddress.getByName("localhost"), 1527);
        server.start(null);        
    }
    
    private void createConnection() throws SQLException
    {
         Driver derbyEmbeddedDriver = new EmbeddedDriver();
         DriverManager.registerDriver(derbyEmbeddedDriver);
         connection = DriverManager.getConnection
            (DATABASE_URL, USER_NAME, PASSWORD);        
    }
    
    public void createInsertStatements() throws SQLException
    {
        psSelectMaxId = connection.prepareStatement("SELECT MAX(ID) FROM SIMULATIONS");
        psInsertSim = connection.prepareStatement("INSERT INTO SIMULATIONS"
                + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        psInsertQGroup = connection.prepareStatement("INSERT INTO Q_GROUP"
                + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        psInsertQ = connection.prepareStatement("INSERT INTO Q"
                + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");       
    }
    
    public final Integer selectMaxSimId() throws SQLException
    {
        ResultSet resultSet = null;
        Integer max = 0;        
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
            int custPerMin) throws SQLException
    { 

        simID = selectMaxSimId() + 1; 
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
    
    public void insertQGroup(int groupID, int numQs, double avgIdle, double totalGlitch, 
            double avgCustHour, int maxQLength,double avgItemsHour, double numCartsAbandonded, 
            long avgWaitTime, double avgQLength, String type) throws SQLException
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
    
    public void insertQ(int ID, int groupID, double avgIdle, double totalGlitch, 
            double avgCustHour, int maxQLength,double avgItemsHour, double numCartsAbandonded, 
            long avgWaitTime, double avgQLength, String type, int maxItemsAllowed) throws
            SQLException
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

    public void buildDynamicQuery(List<String> colNames, String table, Map<String, Pair<String, Double>> hmFilter)
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
        executeDynamicQuery(sb.toString());          
    }
    
    private void executeDynamicQuery(String statement)
    {
        try
        {
            PreparedStatement psSelectDynamic = connection.prepareStatement(statement);
            ResultSet resultSet = null;
            resultSet = psSelectDynamic.executeQuery();
            displayResultSet(resultSet);
        }
        catch(SQLException ex)
        {
            ex.printStackTrace();
            displayAlert(ex.getMessage());
        }
    }
        
    private void displayResultSet(ResultSet resultSet)
    {
        QueryResultsStage qrs = new QueryResultsStage(resultSet);
        qrs.display();
    }
    
    
    public void cleanUp()
    {
        try{
            if (connection != null){
                connection.close();
            }
            if (server != null){
                server.shutdown();                
            }
            if (psInsertQ != null){
                psInsertQ.close();
            }
            if (psInsertQGroup != null){
                psInsertQGroup.close();
            }
            if (psInsertSim != null){
                psInsertSim.close();
            }
            if (psSelectMaxId != null){
                psSelectMaxId.close();
            }
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
        catch (Exception ex){
            ex.printStackTrace();
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
