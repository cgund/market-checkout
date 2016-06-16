package market;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;


class QueryResultsStage 
{
    ResultSet resultSet;
    
    public QueryResultsStage(ResultSet resultSet)
    {
        this.resultSet = resultSet;
    }
    public void display()
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
                col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList,String>,ObservableValue<String>>()
                {                    
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) 
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
            
            Stage stage = new Stage();
            
            BorderPane root = new BorderPane();
            root.setPadding(new Insets(20, 20, 20, 20));
            root.setCenter(tableview); 
            
            Button btnClose = new Button("Close");
            btnClose.setOnAction(e ->
            {
                stage.close();
            });
            HBox hb = new HBox(10);
            hb.setAlignment(Pos.CENTER);
            hb.setPadding(new Insets(10, 10, 10, 10));
            hb.getChildren().add(btnClose);
            root.setBottom(hb);
            
            Scene scene = new Scene(root);
            stage.getIcons().addAll(Resources.getIcons());
            stage.setWidth(colCount * 100);
            String tableName = metaData.getTableName(1);
            stage.setTitle("Query Results for " + tableName);

            stage.setScene(scene);
            stage.setResizable(true);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        }
        catch (SQLException ex)
        {
            displayAlert(ex.getMessage());
        }
    }
    
    private static void displayAlert(String message)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}
