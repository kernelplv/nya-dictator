package sample;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

public class AboutController
{
    @FXML private AnchorPane AboutRoot;

    @FXML
    void AboutClose()
    {
        AboutRoot.getScene().getWindow().hide();
    }

}