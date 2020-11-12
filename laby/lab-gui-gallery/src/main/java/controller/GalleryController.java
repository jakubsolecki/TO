package controller;


import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import model.Gallery;
import model.Photo;

import javafx.scene.control.TextField;
import util.PhotoDownloader;

public class GalleryController {

    private Gallery galleryModel;

    @FXML
    private TextField imageNameField;

    @FXML
    private ImageView imageView;

    @FXML
    private ListView<Photo> imagesListView;

    @FXML
    private TextField searchTextField;

    public void searchButtonClicked(ActionEvent event) {
        PhotoDownloader photoDownloader = new PhotoDownloader();
        galleryModel.clear();
        photoDownloader.searchForPhotos(searchTextField.getText())
                .subscribeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(galleryModel::addPhoto);
    }

    @FXML
    public void initialize() {
        // TODO additional FX controls initialization
        imagesListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Photo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    ImageView photoIcon = new ImageView(item.getPhotoData());
                    photoIcon.setPreserveRatio(true);
                    photoIcon.setFitHeight(50);
                    setGraphic(photoIcon);
                }
            }
        });
        imagesListView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        bindSelectedPhoto(newValue);
                    }
                    if (oldValue != null) {
                        imageNameField.textProperty().unbindBidirectional(oldValue);
                    }
                });
    }

    public void setModel(Gallery gallery) {
        this.galleryModel = gallery;
        bindSelectedPhoto(gallery.getPhotos().get(0));
        imagesListView.setItems(gallery.getPhotos());
    }

    private void bindSelectedPhoto(Photo selectedPhoto) {
        imagesListView.getSelectionModel().select(selectedPhoto);
        imageView.imageProperty().bind(selectedPhoto.photoDataProperty());
        imageNameField.textProperty().bindBidirectional(selectedPhoto.nameProperty());
    }


}

