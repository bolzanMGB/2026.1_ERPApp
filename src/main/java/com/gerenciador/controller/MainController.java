package com.gerenciador.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.event.ActionEvent;
import java.net.URL;
import java.util.Collections;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private StackPane contentArea;
    @FXML
    private Button clientesBtn;
    @FXML
    private Button fornecedoresBtn;
    @FXML
    private Button servicosBtn;
    @FXML
    private Button estoqueBtn;
    @FXML
    private Button comprasBtn;
    @FXML
    private Button vendasBtn;
    @FXML
    private Button relatoriosBtn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setActive (clientesBtn);
        loadView("clientes.fxml");
    }

    private void setActive(Button activeButton) {
        clientesBtn.getStyleClass().remove("active");
        fornecedoresBtn.getStyleClass().remove("active");
        estoqueBtn.getStyleClass().remove("active");
        servicosBtn.getStyleClass().remove("active");
        comprasBtn.getStyleClass().remove("active");
        vendasBtn.getStyleClass().remove("active");
        relatoriosBtn.getStyleClass().remove("active");
        activeButton.getStyleClass().add("active");
    }

    private void loadView (String fxml){
        try{
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/gerenciador/fxml/" + fxml)
            );
            contentArea.getChildren().setAll(Collections.singleton(loader.load()));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    private void onNavigate (ActionEvent event){
        Button clickedBtn =  (Button) event.getSource();
        setActive(clickedBtn);
        String fxml = clickedBtn.getUserData().toString();
        try {
            loadView(fxml);
        } catch (Exception e){
            System.out.println(e);
        }
    }
}
