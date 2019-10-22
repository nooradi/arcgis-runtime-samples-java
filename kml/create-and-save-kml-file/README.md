# Create and save KML file

Construct a KML document and save it as a KMZ file.

![Create And Save KML File Sample](CreateAndSaveKMLFile.png)

## Use case

KML is a useful format for creating and saving data without using a service or database. A KML document can be
 serialized to a KMZ file for sharing and reading in other applications, such as ArcGIS Earth or Google Earth.

## How to use the sample

Select the geometry type for sketching with the first combo box.  Change the
 style for the KML feature using the second combo box. 

Click anywhere on the map to sketch. Press ENTER to commit the sketch or ESCAPE to discard the sketch. 
 
 When finished, click the 'Save KMZ file' button to save the KML document to a KMZ file. 

## How it works

1. Create a `KmlDocument`
2. Create a `KmlDataset` using the KML document.
3. Create a `KmlLayer` using the KML dataset and add it to the map as an operational layer.
4. Create `Geometry` using `SketchEditor`.
5. Project that geometry to WGS84 using `GeometryEngine.project`.
6. Create a `KmlGeometry` object using the projected geometry.
7. Create a `KmlPlacemark` using the KML geometry.
8. Add the KML placemark to the KML document.
9. Set the `KmlStyle` for the KML placemark.
10. Save the KML document to a file using `kmlDocument.saveAsAsync(Path)`.

## Relevant API

* GeometryEngine.Project
* KmlDataset
* KmlDocument
* KmlGeometry
* KmlLayer
* KmlNode.SaveAsASync
* KmlPlacemark
* KmlStyle
* SketchEditor

## Tags

Keyhole, KML, KMZ, OGC