package market;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

/*
Class to hold application resources
*/
public class Resources 
{
    private static final ObservableList<Image> icons = FXCollections.observableArrayList();
    
    /*
    Called in MainStage class
    */
    public static void buildResources()
    {
        try
        {
            icons.addAll(new Image(Resources.class.getResourceAsStream("resources/icon-small.png")),
                    new Image(Resources.class.getResourceAsStream("resources/icon.png")));
        }
        catch(NullPointerException ex)
        {
            System.err.println(ex.getMessage());
        }
    }
    /*
    Returns an ObservableList of Image.  The ObservableList is passed as a parameter
    to a Stage objects ObservableList of Image to set the Stage's icon
    */
    public static ObservableList<Image> getIcons()
    {
        return icons;
    }
}
