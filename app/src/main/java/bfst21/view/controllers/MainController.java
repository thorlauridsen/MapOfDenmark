package bfst21.view.controllers;

import bfst21.data.BinaryFileManager;
import bfst21.exceptions.MapDataNotLoadedException;
import bfst21.models.*;
import bfst21.osm.Node;
import bfst21.osm.UserNode;
import bfst21.osm.Way;
import bfst21.view.ColorMode;
import bfst21.view.MapCanvas;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class MainController {

    @FXML
    private MapCanvas canvas;
    @FXML
    private StackPane stackPane;
    @FXML
    private VBox debugBox;
    @FXML
    private VBox startBox;
    @FXML
    private Scene scene;
    @FXML
    private VBox loadingText;
    @FXML
    private ProgressBar progressBar;

    @FXML
    private VBox userNodeVBox;
    @FXML
    private ListView<String> userNodeListView;
    @FXML
    private VBox newUserNodeVBox;
    @FXML
    private TextField userNodeNameTextField;
    @FXML
    private TextField userNodeDescriptionTextField;
    @FXML
    private VBox userNodeClickedVBox;
    @FXML
    private Text userNodeClickedCoords;
    @FXML
    private Text userNodeClickedName;
    @FXML
    private Text userNodeClickedDescription;
    @FXML
    private VBox userNodeNewNameVBox;
    @FXML
    private TextField userNodeNewNameTextField;
    @FXML
    private VBox userNodeNewDescriptionVBox;
    @FXML
    private TextField userNodeNewDescriptionTextField;

    @FXML
    private SearchBoxController searchBoxController;
    @FXML
    private NavigationBoxController navigationBoxController;
    @FXML
    private DebugBoxController debugBoxController;
    @FXML
    private StartBoxController startBoxController;

    private boolean resetDijkstra = true;

    private boolean userNodeToggle = false;
    private final ImageCursor userNodeCursorImage = new ImageCursor(new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("cursor_transparent.png"))));
    private UserNode currentUserNode = null;
    private ObservableList<UserNode> userNodeListItems = FXCollections.observableArrayList();
    private HashMap<String, UserNode> userNodesMap = new HashMap<>();

    private Model model;
    private Point2D lastMouse;
    private final DisplayOptions displayOptions = DisplayOptions.getInstance();


    public void updateZoomBox() {
        debugBoxController.setZoomPercent("Zoom percent: " + canvas.getZoomPercent());
        debugBoxController.setZoomText("Zoom level: " + canvas.getZoomLevelText());
        updateAverageRepaintTime();
        updateNodeSkipAmount();
    }

    public void updateNodeSkipAmount() {
        debugBoxController.setNodeSkipAmount("Node skip: " + Way.getNodeSkipAmount(canvas.getZoomLevel()));
    }

    public void updateAverageRepaintTime() {
        debugBoxController.setRepaintTime("Repaint time: " + canvas.getAverageRepaintTime());
    }

    public void updateMouseCoords(Point2D currentMousePos) {
        double currentPosX = canvas.mouseToModelCoords(currentMousePos).getX();
        double currentPosY = canvas.mouseToModelCoords(currentMousePos).getY();

        String xFormatted = String.format(Locale.ENGLISH, "%.7f", currentPosX);
        String yFormatted = String.format(Locale.ENGLISH, "%.7f", currentPosY);

        debugBoxController.setMouseCoords("Mouse coords: " + xFormatted + ", " + yFormatted);

        String yFormattedRealWorld = String.format(Locale.ENGLISH, "%.7f", -1 * currentPosY * 0.56);
        debugBoxController.setMouseCoordsRealWorld("Real coords: " + xFormatted + ", " + yFormattedRealWorld);
    }

    public MapCanvas getCanvas() {
        return canvas;
    }

    public void init(Model model) {
        this.model = model;
        canvas.init(model);

        searchBoxController.setController(this);
        navigationBoxController.setController(this);
        debugBoxController.setController(this);
        startBoxController.setController(this);

        StackPane.setAlignment(debugBox, Pos.TOP_RIGHT);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        updateZoomBox();

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.D && event.isControlDown()) {
                showHideDebug();
            }
        });
        scene.setOnMouseMoved(event -> {
            Point2D currentMousePos = new Point2D(event.getX(), event.getY());
            updateMouseCoords(currentMousePos);
        });

        userNodeListView.setOnMouseClicked(event -> userNodeClickedInListView(userNodeListView.getSelectionModel().getSelectedItem()));
    }

    private void userNodeClickedInListView(String userNodeName) {
        if(userNodeName != null) {
            UserNode clickedUserNode = userNodesMap.get(userNodeName);
            canvas.changeView(clickedUserNode.getX(), clickedUserNode.getY());
            userNodeListView.getSelectionModel().clearSelection();
        }
    }

    @FXML
    private void showHideDebug() {
        debugBox.setVisible(!debugBox.isVisible());
    }

    public void onWindowResize(Stage stage) {
        StackPane.setAlignment(debugBox, Pos.TOP_RIGHT);
        stage.getHeight();
        canvas.repaint();
        searchBoxController.onWindowResize(stage);
        navigationBoxController.onWindowResize(stage);
    }

    @FXML
    private void onScroll(ScrollEvent scrollEvent) {
        double deltaY;

        //Limit delta Y to avoid a rapid zoom update
        if (scrollEvent.getDeltaY() > 0.0D) {
            deltaY = 32.0D;
        } else {
            deltaY = -32.0D;
        }
        double factor = Math.pow(1.01D, deltaY);
        Point2D point = new Point2D(scrollEvent.getX(), scrollEvent.getY());

        canvas.zoom(factor, point, false);
        updateZoomBox();
    }

    @FXML
    public void onMouseReleased() {
        canvas.runRangeSearchTask();

        if (model.getMapData() != null) {
            if (!model.getMapData().getUserNodes().isEmpty()) {

                Point2D point = canvas.mouseToModelCoords(lastMouse);
                Node nodeAtMouse = new Node((float) point.getX(), (float) point.getY());
                UserNode closestNode = null;

                for (UserNode userNode : model.getMapData().getUserNodes()) {
                    //If closestNode is null, any Node could be the closest node
                    if (closestNode == null || nodeAtMouse.distTo(userNode) < nodeAtMouse.distTo(closestNode)) {
                        closestNode = userNode;
                    }
                }
                if (closestNode != null) {
                    // 300 is a number chosen by trial and error. It seems to fit perfectly.
                    if (nodeAtMouse.distTo(closestNode) < 300 * (1 / Math.sqrt(canvas.getTrans().determinant()))) {
                        userNodeClickedVBox.setVisible(true);
                        userNodeClickedName.setText(closestNode.getName());
                        userNodeClickedDescription.setText((closestNode.getDescription().equals("") ? "No description entered" : closestNode.getDescription()));
                        String x = String.format(Locale.ENGLISH, "%.6f", closestNode.getX());
                        String y = String.format(Locale.ENGLISH, "%.6f", (-1 * closestNode.getY() * 0.56));
                        userNodeClickedCoords.setText(x + ", " + y);
                        currentUserNode = closestNode;
                    }
                }
            }
        }
    }

    @FXML
    private void onMouseDragged(MouseEvent mouseEvent) {
        double dx = mouseEvent.getX() - lastMouse.getX();
        double dy = mouseEvent.getY() - lastMouse.getY();

        if (mouseEvent.isPrimaryButtonDown()) {
            canvas.pan(dx, dy);
        }
        onMousePressed(mouseEvent);
    }

    @FXML
    private void onMousePressed(MouseEvent mouseEvent) {
        lastMouse = new Point2D(mouseEvent.getX(), mouseEvent.getY());
        updateAverageRepaintTime();

        if (mouseEvent.isSecondaryButtonDown()) {
            Point2D point = canvas.mouseToModelCoords(lastMouse);
            float[] queryCoords = new float[]{(float) point.getX(), (float) point.getY()};
            canvas.runNearestNeighborTask(queryCoords);
        }

        if (userNodeToggle && mouseEvent.isPrimaryButtonDown()) {
            newUserNodeVBox.setVisible(true);
            userNodeNameTextField.requestFocus();
            scene.setCursor(Cursor.DEFAULT);
        }

        if (mouseEvent.isShiftDown() && mouseEvent.isPrimaryButtonDown()) {
            Point2D point = canvas.mouseToModelCoords(lastMouse);
            float[] queryCoords = new float[]{(float) point.getX(), (float) point.getY()};
            float[] nearestCoords = model.getMapData().kdTreeNearestNeighborSearch(queryCoords);

            if (resetDijkstra) {
                resetDijkstra = false;
                model.getMapData().originCoords = nearestCoords;
                model.getMapData().destinationCoords = null;
            } else {
                model.getMapData().destinationCoords = nearestCoords;
                model.getMapData().runDijkstra();
                resetDijkstra = true;
            }
        }
    }

    @FXML
    public void onMouseEntered(MouseEvent mouseEvent) {
        if(model.getMapData() != null) {
            updateUserNodeList();
        }
    }

    @FXML
    public void loadDefault() {

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                loadingText.setVisible(true);
                canvas.load(true);
                updateZoomBox();
                return null;
            }
        };
        task.setOnSucceeded(e -> finishedLoadingFile());
        task.setOnFailed(e -> task.getException().printStackTrace());
        Thread thread = new Thread(task);
        thread.start();
    }

    @FXML
    public void loadNewFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load new map segment");
        fileChooser.setInitialDirectory(new File("./"));

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("OBJ file, OSM file, ZIP file", "*.obj; *.osm; *.zip")
        );
        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            String filename = file.getAbsolutePath();
            model.setFileName(filename);

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    loadingText.setVisible(true);
                    canvas.load(false);
                    updateZoomBox();

                    return null;
                }
            };
            task.setOnSucceeded(e -> finishedLoadingFile());
            task.setOnFailed(e -> task.getException().printStackTrace());
            Thread thread = new Thread(task);
            thread.start();
        }
    }

    private void finishedLoadingFile() {
        startBox.setVisible(false);
        startBox.setManaged(false);
        loadingText.setVisible(false);
        searchBoxController.setVisible(true);
        canvas.runRangeSearchTask();
    }

    @FXML
    public void zoomButtonClicked(ActionEvent actionEvent) {
        Point2D point = new Point2D(stackPane.getWidth() / 2, stackPane.getHeight() / 2);

        if (actionEvent.toString().toLowerCase().contains("zoomin")) {
            canvas.zoom(2.0D, point, false);
        } else {
            canvas.zoom(0.5D, point, false);
        }
        updateZoomBox();
    }

    @FXML
    public void changeColorMode(ActionEvent actionEvent) {
        String text = actionEvent.toString().toLowerCase();

        for (ColorMode colorMode : ColorMode.values()) {
            String optionText = colorMode.toString().toLowerCase().replaceAll("_", "");

            if (text.contains(optionText)) {
                canvas.setColorMode(colorMode);
                canvas.repaint();
                break;
            }
        }
    }

    @FXML
    public void userNodeButtonClicked() throws MapDataNotLoadedException {
        if (model.getMapData() == null) {
            throw new MapDataNotLoadedException("No MapData has been loaded. MapData is null.");
        }
        if (userNodeToggle) {
            userNodeToggle = false;
            scene.setCursor(Cursor.DEFAULT);
        } else {
            userNodeToggle = true;
            scene.setCursor(userNodeCursorImage);
        }
    }

    @FXML
    public void userNodeTextFieldKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            newUserNodeCheckNameAndSave();
        } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
            newUserNodeVBox.setVisible(false);
            scene.setCursor(userNodeCursorImage);
        }
    }

    @FXML
    public void userNodeSaveClicked() {
        newUserNodeCheckNameAndSave();
    }

    private void newUserNodeCheckNameAndSave() {
        if(userNodeNameTextField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("");
            alert.setContentText("A name is required");
            alert.showAndWait();
        } else if(userNodesMap.containsKey(userNodeNameTextField.getText())) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("");
            alert.setContentText("Point of Interest names must be unique");
            alert.showAndWait();
        } else {
            saveUserNode();
        }
    }

    @FXML
    public void userNodeCancelClicked() {
        newUserNodeVBox.setVisible(false);
        scene.setCursor(userNodeCursorImage);
    }

    private void saveUserNode() {
        Point2D point = canvas.mouseToModelCoords(lastMouse);
        UserNode userNode = new UserNode((float) point.getX(), (float) point.getY(), userNodeNameTextField.getText(), userNodeDescriptionTextField.getText());

        model.getMapData().addUserNode(userNode);
        model.getMapData().updateUserNodesMap();

        scene.setCursor(Cursor.DEFAULT);
        userNodeToggle = false;
        newUserNodeVBox.setVisible(false);
        updateUserNodeList();
        canvas.repaint();
    }

    private void updateUserNodeList() {
        userNodeListItems.setAll(model.getMapData().getUserNodes());
        ObservableList<String> tempList = FXCollections.observableArrayList();

        userNodesMap = model.getMapData().getUserNodesMap();

        for(UserNode userNode : userNodeListItems) {
            tempList.add(userNode.getName());
        }
        userNodeListView.setItems(tempList);
    }

    @FXML
    public void userNodeDeleteClicked() {
        if (currentUserNode == null) {
            throw new NullPointerException("currentUserNode is null");
        } else {
            model.getMapData().getUserNodes().remove(currentUserNode);
            model.getMapData().updateUserNodesMap();
            currentUserNode = null;
            userNodeClickedVBox.setVisible(false);
            updateUserNodeList();
            canvas.repaint();
        }
    }

    @FXML
    public void userNodeCloseClicked() {
        userNodeClickedVBox.setVisible(false);
        currentUserNode = null;
    }

    @FXML
    public void userNodeChangeNameClicked(ActionEvent actionEvent) {
        userNodeNewNameVBox.setVisible(true);
        userNodeNewNameTextField.requestFocus();
    }

    @FXML
    public void userNodeChangeDescriptionClicked() {
        userNodeNewDescriptionVBox.setVisible(true);
        userNodeNewDescriptionTextField.requestFocus();
    }

    @FXML
    public void userNodeNewNameTextFieldKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            userNodeNewNameCheckNameAndSave();
        } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
            userNodeNewNameVBox.setVisible(false);
        }
    }

    @FXML
    public void userNodeNewDescTextFieldKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            currentUserNode.setDescription(userNodeNewDescriptionTextField.getText());
            userNodeNewDescriptionVBox.setVisible(false);
            userNodeNewDescriptionTextField.setText("");
            userNodeClickedDescription.setText(currentUserNode.getDescription());
        } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
            userNodeNewDescriptionVBox.setVisible(false);
        }
    }

    @FXML
    public void userNodeNewNameCancelClicked() {
        userNodeNewNameVBox.setVisible(false);
    }

    @FXML
    public void userNodeNewDescCancelClicked() {
        userNodeNewDescriptionVBox.setVisible(false);
    }

    @FXML
    public void userNodeNewNameSaveClicked() {
        userNodeNewNameCheckNameAndSave();
    }

    private void userNodeNewNameCheckNameAndSave() {
        if(userNodeNewNameTextField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("");
            alert.setContentText("A name is required");
            alert.showAndWait();
        } else if(userNodesMap.containsKey(userNodeNewNameTextField.getText())) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("");
            alert.setContentText("Point of Interest names must be unique");
            alert.showAndWait();
        } else {
            currentUserNode.setName(userNodeNewNameTextField.getText());
            userNodeNewNameVBox.setVisible(false);
            userNodeNewNameTextField.setText("");
            userNodeClickedName.setText(currentUserNode.getName());
            updateUserNodeList();
            model.getMapData().getUserNodesMap().put(currentUserNode.getName(), currentUserNode);
        }
    }

    @FXML
    public void userNodeNewDescSaveClicked() {
        currentUserNode.setDescription(userNodeNewDescriptionTextField.getText());
        userNodeNewDescriptionVBox.setVisible(false);
        userNodeNewDescriptionTextField.setText("");
        userNodeClickedDescription.setText(currentUserNode.getDescription());
    }

    public void setNavigationBoxVisible(boolean visible) {
        navigationBoxController.setVisible(visible);
    }

    public void setSearchBoxVisible(boolean visible) {
        searchBoxController.setVisible(visible);
    }

    public void setNavigationBoxAddressText(String address) {
        navigationBoxController.transferAddressText(address);
    }

    public void setSearchBoxAddressText(String address) {
        searchBoxController.transferAddressText(address);
    }

    @FXML
    public void preSaveObjFile() {
        String fileName = model.getFileName();

        if (fileName.endsWith(".obj")) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("");
            alert.setContentText("You're currently using an OBJ file. Are you sure you want to save another OBJ file?");
            alert.showAndWait();
            if (alert.getResult() == ButtonType.OK) {
                saveObjFile();
            }
        } else {
            saveObjFile();
        }
    }

    private void saveObjFile() {
        FileChooser fileSaver = new FileChooser();
        fileSaver.setTitle("Save to OBJ");
        fileSaver.setInitialDirectory(new File("./"));
        fileSaver.getExtensionFilters().addAll((
                new FileChooser.ExtensionFilter("OBJ file", ".obj")));
        File file = fileSaver.showSaveDialog(new Stage());
        if (file != null) {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws IOException {
                    BinaryFileManager binaryFileManager = new BinaryFileManager();
                    MapData mapData = model.getMapData();

                    long time = -System.nanoTime();
                    binaryFileManager.saveOBJ(file.getAbsolutePath(), mapData);
                    time += System.nanoTime();
                    System.out.println("Saved .obj file in: " + time / 1_000_000 + "ms to " + file.getAbsolutePath());
                    return null;
                }
            };
            task.setOnSucceeded(event -> {
                Alert confirmationPopup = new Alert(Alert.AlertType.INFORMATION);
                confirmationPopup.setContentText("Successfully saved OBJ");
                confirmationPopup.setTitle("Success");
                confirmationPopup.setHeaderText("");
                //confirmationPopup.setGraphic(null);
                confirmationPopup.showAndWait();
            });
            task.setOnFailed(event -> task.getException().printStackTrace());
            Thread thread = new Thread(task);
            thread.start();
        }
    }

    public void checkDebug(ActionEvent actionEvent) {
        String text = actionEvent.toString().toLowerCase();

        for (DisplayOption displayOption : DisplayOption.values()) {
            String optionText = displayOption.toString().toLowerCase().replaceAll("_", " ");

            if (text.contains(optionText)) {
                displayOptions.toggle(displayOption);
                canvas.repaint();
                break;
            }
        }
    }
}