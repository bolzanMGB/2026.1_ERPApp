package com.gerenciador.controller;

import com.gerenciador.app.DadosRepositorio;
import com.gerenciador.model.cliente.Cliente;
import com.gerenciador.model.cliente.ClientePF;
import com.gerenciador.model.cliente.ClientePJ;
import com.gerenciador.model.pessoa.Endereco;
import com.gerenciador.model.pessoa.PessoaFisica;
import com.gerenciador.model.pessoa.PessoaJuridica;
import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.stage.Modality;
import javafx.util.Callback;
import javafx.util.Duration;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class ClientesController implements Initializable {
    @FXML private AnchorPane pane;
    @FXML private VBox pj_fields;
    @FXML private VBox pf_fields;
    @FXML private Label tipoFormularioLabel;
    @FXML private Label h1Formulario;
    @FXML private HBox boxCadastrar;
    @FXML private HBox boxAtualizar;
    @FXML private TextField txtNomePF, txtCpf;
    @FXML private TextField txtNomeResponsavel, txtCnpj, txtNomeFantasia, txtRazaoSocial, txtInscricaoEstadual;
    @FXML private TextField txtTelefone, txtTelefone2, txtObservacao;
    @FXML private TextField txtCidade, txtBairro, txtRua, txtNumeroCasa;
    @FXML private ComboBox<String> filtroTabela;
    @FXML private TextField txtPesquisa;
    @FXML private Label erroLabel;
    @FXML private Label sucessoLabel;
    @FXML private TableView<Cliente> tabelaClientes;
    @FXML private TableColumn<Cliente, String> colId, colNome, colTipo, colDocumento, colTelefone, colTelefone2, colCidade;
    @FXML private TableColumn<Cliente, Void> colAcoes;
    private FilteredList<Cliente> listaFiltrada;
    @FXML private Label labelFiltro;
    private Cliente clienteEmEdicao = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarTabela();
        configurarFiltro();
        configurarCss();
        configurarLimitadores();
        configurarAtalhosTeclado();
        javafx.application.Platform.runLater(() -> pane.requestFocus());
    }

    private void configurarAtalhosTeclado() {
        pane.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER: cadastrar(); break;
                case ESCAPE: if (clienteEmEdicao != null) ativarModoCadastro(); break;
                default: break;
            }
        });
    }

    private void configurarLimitadores() {
        limitarCaracteres(txtNomePF, 60);
        limitarCaracteres(txtNomeFantasia, 60);
        limitarCaracteres(txtNomeResponsavel, 60);
        limitarCaracteres(txtRazaoSocial, 60);
        limitarCaracteres(txtCidade, 40);
        limitarCaracteres(txtBairro, 40);
        limitarCaracteres(txtRua, 40);
        limitarCaracteres(txtNumeroCasa, 10);
        limitarCaracteres(txtObservacao, 255);
    }

    private void limitarCaracteres(TextField field, int limite) {
        field.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > limite) {
                field.setText(oldValue);
            }
        });
    }

    public void configurarFiltro() {
        ObservableList<String> opcoes = FXCollections.observableArrayList("Todos", "Pessoa Física", "Pessoa Jurídica");
        filtroTabela.setItems(opcoes);
        filtroTabela.setValue("Todos");
        aplicarMascaraCPF(txtCpf);
        aplicarMascaraCNPJ(txtCnpj);
        aplicarMascaraInscricao(txtInscricaoEstadual);
        aplicarMascaraTelefone(txtTelefone);
        if (txtTelefone2 != null) aplicarMascaraTelefone(txtTelefone2);
        filtroTabela.setOnAction(event -> atualizarFiltros());

        filtroTabela.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) labelFiltro.getStyleClass().add("label-verde");
            else labelFiltro.getStyleClass().remove("label-verde");
        });

        txtPesquisa.textProperty().addListener((observable, oldValue, newValue) -> atualizarFiltros());
    }

    public void atualizarFiltros() {
        String termoBusca = txtPesquisa.getText().toLowerCase().trim();
        String selecaoTipo = filtroTabela.getValue();

        listaFiltrada.setPredicate(cliente -> {
            boolean matchesTipo = true;
            if (selecaoTipo.equals("Pessoa Física")) matchesTipo = (cliente instanceof PessoaFisica);
            else if (selecaoTipo.equals("Pessoa Jurídica")) matchesTipo = (cliente instanceof PessoaJuridica);

            if (!matchesTipo) return false;
            if (termoBusca.isEmpty()) return true;

            return cliente.getNomePrincipal().toLowerCase().contains(termoBusca) ||
                    cliente.getDocumento().toLowerCase().contains(termoBusca) ||
                    cliente.getTelefone().toLowerCase().contains(termoBusca) ||
                    (cliente.getTelefone2() != null && cliente.getTelefone2().toLowerCase().contains(termoBusca)) ||
                    cliente.getEndereco().getCidade().toLowerCase().contains(termoBusca);
        });
    }

    public void configurarTabela() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colTipo.setCellValueFactory(cellData -> {
            Cliente cliente = cellData.getValue();
            if (cliente instanceof PessoaFisica) return new SimpleStringProperty("Física");
            return new SimpleStringProperty("Jurídica");
        });
        colDocumento.setCellValueFactory(new PropertyValueFactory<>("documento"));
        colTelefone.setCellValueFactory(new PropertyValueFactory<>("telefone"));
        colTelefone2.setCellValueFactory(new PropertyValueFactory<>("telefone2"));
        colCidade.setCellValueFactory(new PropertyValueFactory<>("cidade"));
        colAcoes.setCellFactory(criarColunaAcoes());
        listaFiltrada = new FilteredList<>(DadosRepositorio.getCliente(), p -> true);

        SortedList<Cliente> sortedData = new SortedList<>(listaFiltrada);
        sortedData.comparatorProperty().bind(tabelaClientes.comparatorProperty());
        tabelaClientes.setItems(sortedData);
    }

    private Callback<TableColumn<Cliente, Void>, TableCell<Cliente, Void>> criarColunaAcoes() {
        return new Callback<>() {
            @Override
            public TableCell<Cliente, Void> call(TableColumn<Cliente, Void> clienteVoidTableColumn) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button();
                    private final Button btnDelete = new Button();
                    private final HBox container = new HBox(10, btnEdit, btnDelete);

                    {
                        ImageView editIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/gerenciador/icons/edit.png"))));
                        ImageView deleteIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/gerenciador/icons/delete.png"))));
                        editIcon.setFitWidth(15); editIcon.setFitHeight(15);
                        deleteIcon.setFitWidth(15); deleteIcon.setFitHeight(15);

                        btnEdit.setGraphic(editIcon);
                        btnDelete.setGraphic(deleteIcon);
                        container.setAlignment(Pos.CENTER);
                        btnEdit.getStyleClass().add("btn-edit");
                        btnDelete.getStyleClass().add("btn-delete");

                        btnDelete.setOnAction(e -> removerCliente(getTableView().getItems().get(getIndex())));
                        btnEdit.setOnAction(e -> editarCliente(getTableView().getItems().get(getIndex())));
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : container);
                    }
                };
            }
        };
    }

    public void removerCliente(Cliente cliente) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação de Exclusão");
        alert.setHeaderText("Remover Cliente");
        alert.setContentText("Deseja mesmo remover " + cliente.getNomePrincipal() + "?");

        if (pane.getScene() != null) alert.initModality(Modality.APPLICATION_MODAL);

        ButtonType btnSim = new ButtonType("Sim, Remover", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnCancelar, btnSim);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == btnSim) {
            DadosRepositorio.removerCliente(cliente);
            tabelaClientes.getItems().remove(cliente);
            mostrarSucesso("Cliente removido");
        }
    }

    public void editarCliente(Cliente cliente) {
        boolean isPF = cliente instanceof PessoaFisica;
        h1Formulario.setText("Editar Cliente");
        if (isPF && pj_fields.isVisible()) trocarFormulario();
        else if (!isPF && pf_fields.isVisible()) trocarFormulario();

        tipoFormularioLabel.setText("Cliente " + cliente.getId());
        clienteEmEdicao = cliente;

        txtTelefone.setText(cliente.getTelefone());
        txtTelefone2.setText(cliente.getTelefone2() != null ? cliente.getTelefone2() : "");
        txtObservacao.setText(cliente.getObservacao() != null ? cliente.getObservacao() : "");
        txtCidade.setText(cliente.getEndereco().getCidade());
        txtBairro.setText(cliente.getEndereco().getBairro());
        txtRua.setText(cliente.getEndereco().getRua());
        txtNumeroCasa.setText(cliente.getEndereco().getnumeroCasa());

        if (isPF) {
            txtNomePF.setText(cliente.getNomePrincipal());
            txtCpf.setText(cliente.getDocumento());
        } else {
            ClientePJ pj = (ClientePJ) cliente;
            txtNomeFantasia.setText(pj.getNomePrincipal());
            txtCnpj.setText(pj.getDocumento());
            txtNomeResponsavel.setText(pj.getNomeResponsavel());
            txtInscricaoEstadual.setText(pj.getInscricaoEstadual());
            txtRazaoSocial.setText(pj.getRazaoSocial());
        }

        boxCadastrar.setVisible(false); boxCadastrar.setManaged(false);
        boxAtualizar.setVisible(true); boxAtualizar.setManaged(true);
    }

    public void configurarCss() {
        URL cssFC = getClass().getResource("/com/gerenciador/css/formularios.css");
        URL cssTC = getClass().getResource("/com/gerenciador/css/tabelas.css");
        if (cssFC != null) pane.getStylesheets().add(cssFC.toExternalForm());
        if (cssTC != null) pane.getStylesheets().add(cssTC.toExternalForm());
    }

    @FXML
    public void trocarFormulario() {
        boolean pfVisivel = pf_fields.isVisible();
        pf_fields.setVisible(!pfVisivel);
        pf_fields.setManaged(!pfVisivel);
        pj_fields.setVisible(pfVisivel);
        pj_fields.setManaged(pfVisivel);

        tipoFormularioLabel.setText(pfVisivel ? "Pessoa Jurídica" : "Pessoa Física");
        limparBordas();
        limparInputs();
        if (pj_fields.isVisible()) txtNomeFantasia.requestFocus();
        else txtNomePF.requestFocus();
    }

    public void ativarModoCadastro() {
        h1Formulario.setText("Cadastrar Cliente");
        clienteEmEdicao = null;
        boxCadastrar.setVisible(true); boxCadastrar.setManaged(true);
        boxAtualizar.setVisible(false); boxAtualizar.setManaged(false);
        limparBordas();
        limparInputs();

        tipoFormularioLabel.setText(pf_fields.isVisible() ? "Pessoa Física" : "Pessoa Jurídica");
        javafx.application.Platform.runLater(() -> pane.requestFocus());
    }

    @FXML
    public void cadastrar() {
        try {
            limparBordas();
            Endereco endereco = new Endereco(txtCidade.getText(), txtBairro.getText(), txtRua.getText(), txtNumeroCasa.getText());

            if (clienteEmEdicao == null) {
                boolean isPF = tipoFormularioLabel.getText().equals("Pessoa Física");

                if (isPF) {
                    DadosRepositorio.adicionarCliente(new ClientePF(txtNomePF.getText(), txtCpf.getText(), txtTelefone.getText(), txtTelefone2.getText(), txtObservacao.getText(), endereco));
                } else {
                    DadosRepositorio.adicionarCliente(new ClientePJ(txtNomeFantasia.getText(), txtTelefone.getText(), txtTelefone2.getText(), txtObservacao.getText(), endereco, txtCnpj.getText(), txtNomeResponsavel.getText(), txtInscricaoEstadual.getText(), txtRazaoSocial.getText()));
                }
                limparInputs();
                mostrarSucesso("Cadastro realizado");

            } else {
                if (clienteEmEdicao instanceof ClientePF pf) {
                    pf.setTelefone(txtTelefone.getText());
                    pf.setTelefone2(txtTelefone2.getText());
                    pf.setObservacao(txtObservacao.getText());
                    pf.setEndereco(endereco);
                    pf.setNome(txtNomePF.getText());
                    pf.setCpf(txtCpf.getText());
                    mostrarSucesso("Atualização realizada");

                } else if (clienteEmEdicao instanceof ClientePJ pj) {
                    pj.setTelefone(txtTelefone.getText());
                    pj.setTelefone2(txtTelefone2.getText());
                    pj.setObservacao(txtObservacao.getText());
                    pj.setEndereco(endereco);
                    pj.setNome(txtNomeFantasia.getText());
                    pj.setCnpj(txtCnpj.getText());
                    pj.setNomeResponsavel(txtNomeResponsavel.getText());
                    pj.setRazaoSocial(txtRazaoSocial.getText());
                    pj.setInscricaoEstadual(txtInscricaoEstadual.getText());
                    mostrarSucesso("Atualização realizada");
                }

                DadosRepositorio.atualizarCliente(clienteEmEdicao);
                tabelaClientes.refresh();
                ativarModoCadastro();
            }
        } catch (IllegalArgumentException e) {
            lidarComErroValidacao(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro inesperado: " + e.getMessage());
        }
    }

    private void lidarComErroValidacao(String msg) {
        mostrarErro(msg);
        if (msg.contains("Fantasia")) { aplicarBordaErro(txtNomePF); aplicarBordaErro(txtNomeFantasia); }
        else if (msg.contains("CPF")) aplicarBordaErro(txtCpf);
        else if (msg.contains("CNPJ")) aplicarBordaErro(txtCnpj);
        else if (msg.contains("Responsável")) aplicarBordaErro(txtNomeResponsavel);
        else if (msg.contains("Inscrição Estadual")) aplicarBordaErro(txtInscricaoEstadual);
        else if (msg.contains("Razão Social")) aplicarBordaErro(txtRazaoSocial);
        else if (msg.contains("Telefone 2")) aplicarBordaErro(txtTelefone2);
        else if (msg.contains("Telefone")) aplicarBordaErro(txtTelefone);
        else if (msg.contains("Cidade")) aplicarBordaErro(txtCidade);
        else if (msg.contains("Bairro")) aplicarBordaErro(txtBairro);
        else if (msg.contains("Rua")) aplicarBordaErro(txtRua);
        else if (msg.contains("Número")) aplicarBordaErro(txtNumeroCasa);
    }

    private void mostrarErro(String mensagem) { exibirNotificacao(erroLabel, "Erro: " + mensagem); }
    private void mostrarSucesso(String mensagem) { exibirNotificacao(sucessoLabel, mensagem + " com sucesso!"); }

    private void exibirNotificacao(Label label, String texto) {
        erroLabel.setVisible(false); erroLabel.setManaged(false);
        sucessoLabel.setVisible(false); sucessoLabel.setManaged(false);

        label.setText(texto);
        label.setVisible(true); label.setManaged(true); label.setOpacity(1.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), label);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), label);
        slideIn.setFromY(-10); slideIn.setToY(0);

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), label);
            fadeOut.setFromValue(1); fadeOut.setToValue(0);
            fadeOut.setOnFinished(event -> { label.setVisible(false); label.setManaged(false); });
            fadeOut.play();
        });

        fadeIn.play(); slideIn.play(); delay.play();
    }

    private void aplicarBordaErro(TextField campo) {
        campo.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2px; -fx-border-radius: 5px;");
    }

    private void limparBordas() {
        TextField[] fields = {txtNomePF, txtCpf, txtTelefone, txtTelefone2, txtObservacao, txtCidade, txtBairro, txtRua,
                txtNumeroCasa, txtNomeResponsavel, txtNomeFantasia, txtInscricaoEstadual, txtRazaoSocial, txtCnpj};
        for (TextField f : fields) if(f != null) f.setStyle(null);
    }

    private void limparInputs() {
        TextField[] fields = {txtNomePF, txtCpf, txtTelefone, txtTelefone2, txtObservacao, txtCidade, txtBairro, txtRua,
                txtNumeroCasa, txtNomeResponsavel, txtNomeFantasia, txtInscricaoEstadual, txtRazaoSocial, txtCnpj};
        for (TextField f : fields) if(f != null) f.setText("");
    }

    private void aplicarMascaraCPF(TextField field) {
        field.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) return;
            String texto = newValue.replaceAll("[^0-9]", "");
            if (texto.length() > 11) texto = texto.substring(0, 11);
            StringBuilder b = new StringBuilder(texto);
            if (b.length() > 3) b.insert(3, ".");
            if (b.length() > 7) b.insert(7, ".");
            if (b.length() > 11) b.insert(11, "-");
            String resultado = b.toString();
            if (!newValue.equals(resultado)) { field.setText(resultado); javafx.application.Platform.runLater(field::end); }
        });
    }

    private void aplicarMascaraCNPJ(TextField field) {
        field.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) return;
            String texto = newValue.replaceAll("[^0-9]", "");
            if (texto.length() > 14) texto = texto.substring(0, 14);
            StringBuilder b = new StringBuilder(texto);
            if (b.length() > 2) b.insert(2, ".");
            if (b.length() > 6) b.insert(6, ".");
            if (b.length() > 10) b.insert(10, "/");
            if (b.length() > 15) b.insert(15, "-");
            String resultado = b.toString();
            if (!newValue.equals(resultado)) { field.setText(resultado); javafx.application.Platform.runLater(field::end); }
        });
    }

    private void aplicarMascaraInscricao(TextField field) {
        field.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) return;
            String texto = newValue.replaceAll("[^0-9]", "");
            if (texto.length() > 14) texto = texto.substring(0, 14);
            if (!newValue.equals(texto)) { field.setText(texto); javafx.application.Platform.runLater(field::end); }
        });
    }

    private void aplicarMascaraTelefone(TextField field) {
        field.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) return;
            String numeros = newValue.replaceAll("\\D", "");
            if (numeros.length() > 11) numeros = numeros.substring(0, 11);
            StringBuilder formatado = new StringBuilder();
            int len = numeros.length();
            if (len > 0) formatado.append("(");
            if (len <= 2) formatado.append(numeros);
            else {
                formatado.append(numeros.substring(0, 2)).append(")");
                if (len <= 6) formatado.append(numeros.substring(2));
                else if (len <= 10) formatado.append(numeros.substring(2, 6)).append("-").append(numeros.substring(6));
                else formatado.append(numeros.substring(2, 7)).append("-").append(numeros.substring(7));
            }
            String resultado = formatado.toString();
            if (!resultado.equals(newValue)) { field.setText(resultado); javafx.application.Platform.runLater(field::end); }
        });
    }
}