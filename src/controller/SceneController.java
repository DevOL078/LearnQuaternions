package controller;

import animation.Action;
import animation.QRotate;
import animation.QTranslate;
import gui.AnimationBox;
import gui.ShapeBox;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.Main;
import model.Point;
import model.Quaternion;
import model.Shape;
import util.LoadController;
import util.Parser;
import util.SaveController;
import util.SceneManager;

import java.io.File;

import java.io.IOException;
import java.util.Optional;
import java.util.Vector;

public class SceneController {

    @FXML
    private ListView<AnimationBox> animationList;

    @FXML
    private Button plusAnimationButton;

    @FXML
    private Button minusAnimationButton;

    @FXML
    private Button optionsAnimationButton;

    @FXML
    private ListView<ShapeBox> shapeList;

    @FXML
    private Pane mainScenePain;

    @FXML
    private Button zoomPlusButton;

    @FXML
    private Button zoomMinusButton;

    @FXML
    private Button minusShapeButton;

    @FXML
    private Button switchAxesButton;

    @FXML
    private Button playButton;

    @FXML
    private Button stopButton;

    private double oldPosX;
    private double oldPosY;
    private double posX;
    private double posY;
    private final double SHIFT_ROTATION_COEFF = 10;

    private static SceneController instance;

    @FXML
    public void initialize(){
        instance = this;
        minusAnimationButton.setDisable(true);
        optionsAnimationButton.setDisable(true);
        minusShapeButton.setDisable(true);
        zoomPlusButton.setDisable(true);
        zoomMinusButton.setDisable(true);
        playButton.setDisable(true);
        stopButton.setDisable(true);
        plusAnimationButton.setDisable(true);
        switchAxesButton.setDisable(true);

        shapeList.setOnMousePressed(e->{
            ShapeBox selected = shapeList.getSelectionModel().getSelectedItem();
            if(selected != null) {
                if (selected.getClicked()) {
                    selected.setClicked(false);
                    selected.switchDrawMode();
                } else {
                    selected.setClicked(true);
                    selected.switchDrawMode();
                }
            }
        });

        SubScene subScene = new SubScene(SceneManager.getInstance().getMainScene(),718,544,true, SceneAntialiasing.DISABLED);
        subScene.setFill(Paint.valueOf("White"));
        mainScenePain.getChildren().add(subScene);
        subScene.setCamera(SceneManager.getInstance().getCamera());

        subScene.setOnScroll(e->{
            if(e.getDeltaY() > 0){
                onZoomMinusButtonClick();
            }
            else{
                onZoomPlusButtonClick();
            }
        });

        subScene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                oldPosX = event.getSceneX();
                oldPosY = event.getSceneY();
                posX = oldPosX;
                posY = oldPosY;
            }
        });

        subScene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                oldPosX = posX;
                oldPosY = posY;
                posX = event.getSceneX();
                posY = event.getSceneY();

                double deltaX = posX - oldPosX;
                double deltaY = posY - oldPosY;

                Quaternion qX;
                Quaternion qY;

                if (event.isShiftDown()) {
                    qX = new Quaternion(new Point(0, 1, 0), deltaX * -0.1 * SHIFT_ROTATION_COEFF);
                    qY = new Quaternion(new Point(1, 0, 0), deltaY * 0.1 * SHIFT_ROTATION_COEFF);
                } else {
                    qX = new Quaternion(new Point(0, 1, 0), deltaX * -0.1);
                    qY = new Quaternion(new Point(1, 0, 0), deltaY * 0.1);
                }

                QRotate.rotateGroup(SceneManager.getInstance().getCameraGroup2(), qX);
                QRotate.rotateGroup(SceneManager.getInstance().getCameraGroup1(), qY);
            }
        });

        Sphere sphere = new Sphere(1);
        SceneManager.getInstance().getMainScene().getChildren().add(sphere);

        Box ox = new Box(120, 0.5, 0.5);
        Box oy = new Box(0.5, 120, 0.5);
        Box oz = new Box(0.5, 0.5, 120);
        PhongMaterial materialX = new PhongMaterial();
        materialX.setDiffuseColor(Color.RED);
        materialX.setSpecularPower(10.0);
        PhongMaterial materialY = new PhongMaterial();
        materialY.setDiffuseColor(Color.GREEN);
        materialY.setSpecularPower(10.0);
        PhongMaterial materialZ = new PhongMaterial();
        materialZ.setDiffuseColor(Color.BLUE);
        materialZ.setSpecularPower(10.0);

        ox.setMaterial(materialX);
        oy.setMaterial(materialY);
        oz.setMaterial(materialZ);

        SceneManager.getInstance().getMainScene().getChildren().addAll(ox, oy, oz);
    }

    public static SceneController getInstance(){
        return instance;
    }

    public void addAnimationBox(AnimationBox box){
        animationList.getItems().add(box);
        minusAnimationButton.setDisable(false);
        optionsAnimationButton.setDisable(false);
        playButton.setDisable(false);
    }

    public void onPlusAnimationButtonClick(){
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/structure/newanimation.fxml"));
            Scene scene = new Scene(root, 320, 240);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("New animation");
            stage.setScene(scene);
            stage.setOnCloseRequest(e->{
                if(SceneManager.getInstance().getAnimationsNumber() > 0){
                    playButton.setDisable(false);
                    minusAnimationButton.setDisable(false);
                    optionsAnimationButton.setDisable(false);
                }
            });
            NewAnimationController.getInstance().setStage(stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onMinusAnimationButtonClick(){
        AnimationBox box = animationList.getSelectionModel().getSelectedItem();
        if(box != null){
            Action action = box.getAction();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete animation");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to delete the animation " + box.getName() + "?");

            Optional<ButtonType> result = alert.showAndWait();
            if(result.get() == ButtonType.OK){
                animationList.getItems().remove(box);
                SceneManager.getInstance().getScenario().deleteAction(action);
                SceneManager.getInstance().deleteAction(action);
            }

            if(animationList.getItems().size() == 0){
                playButton.setDisable(true);
                stopButton.setDisable(true);
                minusAnimationButton.setDisable(true);
                optionsAnimationButton.setDisable(true);
            }
        }
    }

    public void onOptionsAnimationButtonClick(){
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/structure/animationinfo.fxml"));
            AnimationInfoController.getInstance().buildScene(animationList.getSelectionModel().getSelectedItem());
            Scene scene = new Scene(root, 320, 240);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Animation options");
            stage.setScene(scene);
            AnimationInfoController.getInstance().setStage(stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onPlayButtonClick(){
        if(SceneManager.getInstance().getAnimationsNumber() > 0) {
            SceneManager.getInstance().getScenario().play(100);
            playButton.setDisable(true);
            stopButton.setDisable(false);
        }
    }

    public void onStopButtonClick(){
        SceneManager.getInstance().getScenario().stopTimer();
        onStopScenario();
    }

    public void onStopScenario(){
        playButton.setDisable(false);
        stopButton.setDisable(true);
    }

    public void onZoomPlusButtonClick(){
        SceneManager.getInstance().getCamera().setTranslateZ(SceneManager.getInstance().getCamera().getTranslateZ() + 5);
    }

    public void onZoomMinusButtonClick(){
        SceneManager.getInstance().getCamera().setTranslateZ(SceneManager.getInstance().getCamera().getTranslateZ() - 5);
    }

    public void onPlusShapeButtonClick() {
        if(SceneManager.getInstance().getShapeNumber() == 0){
            buildShapeController();
        }
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Meshmixer artifact (*.smesh)", "*.smesh");
        chooser.getExtensionFilters().add(filter);
        File file = chooser.showOpenDialog(Main.getStage());
        if (file != null) {
            Parser parser = new Parser(file.getAbsolutePath());
            Stage stage = new Stage();
            stage.setResizable(false);
            stage.setTitle("New shape");
            AnchorPane root = new AnchorPane();
            Label label = new Label("New shape's name");
            TextField textField = new TextField();
            Button okButton = new Button("OK");
            okButton.setMaxWidth(70);
            okButton.setOnMouseClicked(e -> {
                if (!textField.getText().equals("")) {
                    try {
                        Shape shape = new Shape(textField.getText(), parser.parseTriangleMesh(), parser.getFile());
                        SceneManager.getInstance().addShape(shape);
                        shapeList.getItems().add(new ShapeBox(shape));
                        stage.close();
                        minusShapeButton.setDisable(false);
                        switchAxesButton.setDisable(false);
                        zoomPlusButton.setDisable(false);
                        zoomMinusButton.setDisable(false);
                        plusAnimationButton.setDisable(false);

                    } catch (IOException e1) {
                        System.out.println("Parsing failing");
                    }
                }
            });

            AnchorPane.setLeftAnchor(label, 30.0);
            AnchorPane.setTopAnchor(label, 32.0);
            AnchorPane.setRightAnchor(textField, 20.0);
            AnchorPane.setTopAnchor(textField, 30.0);
            AnchorPane.setLeftAnchor(okButton, 130.0);
            AnchorPane.setBottomAnchor(okButton, 10.0);
            root.getChildren().addAll(label, textField, okButton);
            Scene scene = new Scene(root, 300, 100);
            stage.setScene(scene);
            stage.show();

        }
    }

    public void onMinusShapeButtonClick(){
        ShapeBox box = shapeList.getSelectionModel().getSelectedItem();
        if(box != null){
            Shape shape = box.shape();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete shape");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to delete the shape " + box.shape().getName() + " ?");

            Optional<ButtonType> result = alert.showAndWait();
            if(result.get() == ButtonType.OK) {
                SceneManager.getInstance().deleteShape(shape);
                shapeList.getItems().remove(box);
                Vector<AnimationBox> toDelete = new Vector<>();
                for (AnimationBox b: animationList.getItems()) {
                    if(b.getAction().getShape().equals(shape)){
                        toDelete.add(b);
                    }
                }
                animationList.getItems().removeAll(toDelete);
                if(animationList.getItems().size() == 0){
                    minusAnimationButton.setDisable(true);
                    optionsAnimationButton.setDisable(true);
                }
                if (SceneManager.getInstance().getShapeNumber() == 0) {
                    minusShapeButton.setDisable(true);
                    switchAxesButton.setDisable(true);
                    plusAnimationButton.setDisable(true);
                    playButton.setDisable(true);
                }

            }
        }
    }

    public void onSwitchAxesButtonClick(){
        ShapeBox box = shapeList.getSelectionModel().getSelectedItem();
        if(box != null){
            box.shape().switchAxes();
        }
    }

    private void buildShapeController() {
        Main.getStage().getScene().setOnKeyPressed(e -> {
            ShapeBox box = shapeList.getSelectionModel().getSelectedItem();
            if(box != null) {
                Shape shape = box.shape();
                switch (e.getCode()) {
                    case W: {
                        QTranslate.translateShape(shape, new Point(0,0,-3));
                        break;
                    }
                    case S: {
                        QTranslate.translateShape(shape, new Point(0,0,3));
                        break;
                    }
                    case A: {
                        QTranslate.translateShape(shape, new Point(3,0,0));
                        break;
                    }
                    case D: {
                        QTranslate.translateShape(shape, new Point(-3,0,0));
                        break;
                    }
                    case E: {
                        QTranslate.translateShape(shape, new Point(0,-3,0));
                        break;
                    }

                    case Q: {
                        QTranslate.translateShape(shape, new Point(0,3,0));
                        break;
                    }
                    case I: {
                        QRotate.rotateShape(shape, new Quaternion(new Point(1, 0, 0), 2));
                        break;
                    }
                    case K: {
                        QRotate.rotateShape(shape, new Quaternion(new Point(1, 0, 0), -2));
                        break;
                    }
                    case J: {
                        QRotate.rotateShape(shape, new Quaternion(new Point(0, 1, 0), -2));
                        break;
                    }
                    case L: {
                        QRotate.rotateShape(shape, new Quaternion(new Point(0, 1, 0), 2));
                        break;
                    }
                    case U: {
                        QRotate.rotateShape(shape, new Quaternion(new Point(0, 0, 1), 2));
                        break;
                    }
                    case O: {
                        QRotate.rotateShape(shape, new Quaternion(new Point(0, 0, 1), -2));
                        break;
                    }
                }
            }
        });
    }

    public void onSaveButtonClick(){
        DirectoryChooser chooser = new DirectoryChooser();
        File file = chooser.showDialog(Main.getStage());
        if(file != null && file.isDirectory()){
            new SaveController(file.getAbsolutePath()).save();
        }
    }

    public void onLoadButtonClick(){
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Saved file (*.quat)", "*.quat");
        chooser.getExtensionFilters().add(filter);
        File file = chooser.showOpenDialog(Main.getStage());
        if(file != null && !file.isDirectory()){
            new LoadController(file.getAbsolutePath()).load();
        }

    }

    public void addShape(Shape shape){
        SceneManager.getInstance().addShape(shape);
        shapeList.getItems().add(new ShapeBox(shape));
        minusShapeButton.setDisable(false);
        switchAxesButton.setDisable(false);
        zoomPlusButton.setDisable(false);
        zoomMinusButton.setDisable(false);
        plusAnimationButton.setDisable(false);
    }

    public void addAction(Action action){
        if(action instanceof QTranslate) {
            QTranslate translate = (QTranslate)action;
            SceneManager.getInstance().getScenario().addAction(translate);
            SceneManager.getInstance().addTransition();

            AnimationBox animationBox = new AnimationBox(translate, "Transition" + SceneManager.getInstance().getTransitionNumber());
            addAnimationBox(animationBox);
        }
        else if(action instanceof QRotate){
            QRotate rotation = (QRotate)action;
            AnimationBox animationBox;
            if(rotation.getFullRotation()) {
                SceneManager.getInstance().addFullRotation();
                animationBox = new AnimationBox(rotation, "FullRotation" + SceneManager.getInstance().getFullRotationNumber());
            }
            else{
                SceneManager.getInstance().addRotation();
                animationBox = new AnimationBox(rotation, "Rotation" + SceneManager.getInstance().getRotationNumber());
            }
            SceneManager.getInstance().getScenario().addAction(rotation);
            addAnimationBox(animationBox);
        }
    }

    public void onCalculatorButtonClick(){
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/structure/calculator.fxml"));
            Scene scene = new Scene(root, 600, 335);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Calculator");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onConverterButtonClick(){
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/structure/converter.fxml"));
            Scene scene = new Scene(root, 600, 250);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Converter");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}