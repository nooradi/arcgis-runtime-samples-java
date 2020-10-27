/*
 * Copyright 2017 Esri.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.esri.samples.update_graphics;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;

public class UpdateGraphicsSample extends Application {

  private List<SimpleMarkerSymbol> markers;
  private Button updateDescriptionButton;
  private ComboBox<String> symbolBox;

  private ArcGISMap map; // keep loadable in scope to avoid garbage collection
  private MapView mapView;
  private Graphic selectedGraphic;
  private GraphicsOverlay graphicsOverlay;
  private ListenableFuture<IdentifyGraphicsOverlayResult> identifyGraphics;
  private Point2D mapViewPoint;

  // colors for symbols
  private static final int PURPLE = 0xFF800080;
  private static final int BLUE = 0xFF0000FF;
  private static final int RED = 0xFFFF0000;
  private static final int GREEN = 0xFF00FF00;

  @Override
  public void start(Stage stage) {

    try {
      // create stack pane and application scene
      StackPane stackPane = new StackPane();
      Scene scene = new Scene(stackPane);
      scene.getStylesheets().add(getClass().getResource("/update_graphics/style.css").toExternalForm());

      // set title, size, and add scene to stage
      stage.setTitle("Update Graphics Sample");
      stage.setWidth(800);
      stage.setHeight(700);
      stage.setScene(scene);
      stage.show();

      // create a control panel
      VBox controlsVBox = new VBox(6);
      controlsVBox.setBackground(new Background(new BackgroundFill(Paint.valueOf("rgba(0,0,0,0.3)"), CornerRadii.EMPTY,
              Insets.EMPTY)));
      controlsVBox.setPadding(new Insets(10.0));
      controlsVBox.setMaxSize(180, 100);
      controlsVBox.getStyleClass().add("panel-region");

      // create buttons for user interaction
      updateDescriptionButton = new Button("Update Description");
      updateDescriptionButton.setMaxWidth(Double.MAX_VALUE);
      updateDescriptionButton.setDisable(true);

      // create combo box
      Label symbolLabel = new Label("Update Symbol:");
      symbolLabel.getStyleClass().add("panel-label");
      symbolBox = new ComboBox<>();
      symbolBox.getItems().addAll("CIRCLE", "TRIANGLE", "CROSS", "DIAMOND");
      symbolBox.getSelectionModel().selectFirst();
      symbolBox.setMaxWidth(Double.MAX_VALUE);
      symbolBox.setDisable(true);

      // set the symbol of the graphic
      symbolBox.showingProperty().addListener((obs, wasShowing, isShowing) -> {
        if (selectedGraphic.isSelected() && !isShowing) {
          selectedGraphic.setSymbol(markers.get(symbolBox.getSelectionModel().getSelectedIndex()));
        }
      });

      // add label, combo box and buttons to the control panel
      controlsVBox.getChildren().addAll(updateDescriptionButton, symbolLabel, symbolBox);

      // create a ArcGISMap with basemap light gray canvas
      map = new ArcGISMap(Basemap.Type.LIGHT_GRAY_CANVAS, 56.075844, -2.681572, 13);

      // create a map view and set the map to it
      mapView = new MapView();
      mapView.setMap(map);

      // create a graphics overlay
      graphicsOverlay = new GraphicsOverlay();

      // add graphics overlay to the map view
      mapView.getGraphicsOverlays().add(graphicsOverlay);

      // create default graphics for graphics overlay
      createGraphics();

      updateDescriptionButton.setOnAction(e -> {
        if (selectedGraphic.isSelected()) {
          // get attributes from selected graphic
          java.util.Map<String, Object> attributes = selectedGraphic.getAttributes();

          // create input dialog
          TextInputDialog dialog = new TextInputDialog();
          dialog.setTitle(attributes.get("NAME").toString());
          dialog.setGraphic(null);
          dialog.setHeaderText(attributes.get("DESCRIPTION").toString());
          dialog.setContentText("New Description");

          // set the graphic's description is text entered
          Optional<String> result = dialog.showAndWait();
          result.ifPresent(text -> {
            if (!text.isEmpty()) {
              attributes.put("DESCRIPTION", text);
            }
          });
        }
      });

      mapView.setOnMouseClicked(e -> {
        if (e.getButton() == MouseButton.PRIMARY && e.isStillSincePress()) {

          // create a point from location clicked
          mapViewPoint = new Point2D(e.getX(), e.getY());

          // clear any selected graphic
          graphicsOverlay.clearSelection();

          // set the cursor to default
          mapView.setCursor(Cursor.DEFAULT);

          // identify graphics on the graphics overlay
          identifyGraphics = mapView.identifyGraphicsOverlayAsync(graphicsOverlay, mapViewPoint, 10, false);

          identifyGraphics.addDoneListener(() -> {
            try {
              if (!identifyGraphics.get().getGraphics().isEmpty()) {
                // get the first identified graphic
                selectedGraphic = identifyGraphics.get().getGraphics().get(0);
                // select the identified graphic
                selectedGraphic.setSelected(true);
                // update the drop down box with the identified graphic's current symbol
                String style = ((SimpleMarkerSymbol) selectedGraphic.getSymbol()).getStyle().toString();
                symbolBox.getSelectionModel().select(style);
                // show the UI
                disableUI(false);
              } else {
                disableUI(true);
              }
            } catch (Exception x) {
              new Alert(Alert.AlertType.ERROR, "Error identifying clicked graphic").show();
            }
          });
        }
      });

      mapView.setOnMouseDragged(e -> {
        if (selectedGraphic.isSelected()) {
          // set the cursor to the move
          mapView.setCursor(Cursor.MOVE);

          // disable to UI while moving the graphic
          disableUI(true);

          // create a point from the dragged location
          mapViewPoint = new Point2D(e.getX(), e.getY());
          Point mapPoint = mapView.screenToLocation(mapViewPoint);

          // update the location of the graphic to the dragged location
          selectedGraphic.setGeometry(mapPoint);
          mapView.requestFocus();
        }
      });

      mapView.setOnKeyPressed(e -> {
        // if the user presses the Escape key
        if (e.getCode() == KeyCode.ESCAPE) {
          // set the cursor to default
          mapView.setCursor(Cursor.DEFAULT);

          // de-select the graphic to stop moving it
          selectedGraphic.setSelected(false);
          disableUI(true);
        }
      });

      // add the map view and control panel to stack pane
      stackPane.getChildren().addAll(mapView, controlsVBox);
      StackPane.setAlignment(controlsVBox, Pos.TOP_LEFT);
      StackPane.setMargin(controlsVBox, new Insets(10, 0, 0, 10));
    } catch (Exception e) {
      // on any error, display the stack trace
      e.printStackTrace();
    }
  }

  /**
   * Creates four Graphics with a location, a symbol, and two attributes. Then
   * adds those Graphics to the GraphicsOverlay.
   */
  private void createGraphics() {

    Graphic graphic;
    // create spatial reference for the points
    SpatialReference spatialReference = SpatialReferences.getWgs84();

    // create points to place markers
    List<Point> points = new ArrayList<>();
    points.add(new Point(-2.641, 56.077, spatialReference));
    points.add(new Point(-2.669, 56.058, spatialReference));
    points.add(new Point(-2.718, 56.060, spatialReference));
    points.add(new Point(-2.720, 56.073, spatialReference));

    // create simple marker symbols for the points
    markers = new ArrayList<>();
    markers.add(new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, RED, 10));
    markers.add(new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.TRIANGLE, PURPLE, 10));
    markers.add(new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CROSS, GREEN, 10));
    markers.add(new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.DIAMOND, BLUE, 10));

    // create a list of names for graphics
    List<String> names = new ArrayList<>();
    names.add("LAMB");
    names.add("CANTY BAY");
    names.add("NORTH BERWICK");
    names.add("FIDRA");

    // create a list of descriptions for graphics
    List<String> descriptions = new ArrayList<>();
    descriptions.add("Just opposite of Bass Rock.");
    descriptions.add("100m long and 50m wide.");
    descriptions.add("Lighthouse in northern section.");
    descriptions.add("Also known as Barley Farmstead.");

    // create four graphics with attributes and add to graphics overlay
    for (int i = 0; i < 4; i++) {
      graphic = new Graphic(points.get(i), markers.get(i));
      graphic.getAttributes().put("NAME", names.get(i));
      graphic.getAttributes().put("DESCRIPTION", descriptions.get(i));
      graphicsOverlay.getGraphics().add(graphic);
    }
  }

  /**
   * Disables the visibility of the UI controls
   *
   * @param disable visibility of the UI
   */
  private void disableUI(boolean disable) {
    updateDescriptionButton.setDisable(disable);
    symbolBox.setDisable(disable);
  }

  /**
   * Stops and releases all resources used in application.
   */
  @Override
  public void stop() {

    if (mapView != null) {
      mapView.dispose();
    }
  }

  /**
   * Opens and runs application.
   *
   * @param args arguments passed to this application
   */
  public static void main(String[] args) {

    Application.launch(args);
  }
}
