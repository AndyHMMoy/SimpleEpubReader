import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.ini4j.Ini;
import org.ini4j.Profile.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ClientController {

    @FXML
    public AnchorPane anchorid;
    @FXML
    public ListView bookListView;
    @FXML
    public ListView bookmarkListView;
    @FXML
    public Label addBookLabel;
    @FXML
    public Label seriesLabel;
    @FXML
    public Label directoryTextLabel;
    @FXML
    public Label directoryLabel;
    @FXML
    public TextField seriesNameTextField;
    @FXML
    public TextField bookmarkTextField;
    @FXML
    public Button addDirectoryButton;
    @FXML
    public Button addSeriesButton;
    @FXML
    public WebView webView;

    private String currentBook;

    epubFunctionality epub = new epubFunctionality();

    public void initialize() {

        // Creates a custom listCell in the listview to include a button on the right for the removal of books
        bookListView.setCellFactory(param -> new ListCell<String>() {
            final Label label = new Label();
            final Region region = new Region();
            final Button button = new Button(" - ");
            final HBox hBox = new HBox(label, region, button);

            @Override
            public void updateItem(String title, boolean empty) {
                super.updateItem(title, empty);
                if (empty || title == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    label.setText(title);
                    label.setFont(new Font("System", 16));
                    label.setAlignment(Pos.CENTER_LEFT);
                    button.setOnAction(event -> {
                        removeSeries(title);
                    });
                    button.setStyle("-fx-background-color: #ffffff; -fx-border-color: #454545; -fx-border-radius: 10");
                    HBox.setHgrow(region, Priority.ALWAYS);
                    setGraphic(hBox);
                }
            }
        });
        // Sets the double click action of the cell to open the book selected
        bookListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                try {
                    displaySeries(bookListView.getSelectionModel().getSelectedItem().toString());
                    currentBook = bookListView.getSelectionModel().getSelectedItem().toString();
                    refreshBookmarkList();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        // Sets the double click action to bring the user to the bookmarked section of the book
        bookmarkListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                try {
                    visitBookmarkLocation(bookmarkListView.getSelectionModel().getSelectedItem().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        refreshBookList();
    }

    // Updates the book list on the left listView
    public void refreshBookList() {
        ObservableList<File> bookList = FXCollections.observableArrayList(Arrays.asList(new File("books").listFiles()));
        ObservableList<String> books = FXCollections.observableArrayList();
        for (File f : bookList) {
            String bookName = f.getName().replace("/books", "").replace(".book", "");
            books.add(bookName);
        }
        bookListView.setItems(books);
    }

    // Refreshes the bookmark list on the right listView to match the book selected
    public void refreshBookmarkList() throws IOException {
        Ini ini = new Ini(new File("bookmarks.ini"));
        Section section = ini.get(currentBook);
        ObservableList<String> bookmarks = FXCollections.observableArrayList();
        for (String optionName : section.keySet()) {
            bookmarks.add(optionName);
        }
        bookmarks.remove("latestProgressForSeries");
        bookmarkListView.setItems(bookmarks);
    }

    // Adds the directory to a label when selected the import directory
    @FXML
    public void addDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Stage stage = (Stage) anchorid.getScene().getWindow();
        File selectedDirectory = directoryChooser.showDialog(stage);
        String importPath = selectedDirectory.getAbsolutePath();
        directoryLabel.setText(importPath);
    }

    // Creates the series based on directory selected and series name of choice
    @FXML
    public void addSeries() throws IOException {
        epub.addAllBooksToSeries(directoryLabel.getText());
        epub.createBookForSeries();
        epub.writeToFile(seriesNameTextField.getText());
        refreshBookList();
        seriesNameTextField.clear();
        directoryLabel.setText("");
    }

    // Deletes the '.book' file and updates the book list on the view
    public void removeSeries(String title) {
        File file = new File("books/" + title + ".book");
        file.delete();
        refreshBookList();
    }

    // Renders the book into the webView which parses all the html and formats the book automatically
    public void displaySeries(String title) throws IOException {
        File file = new File("books/" + title + ".book");
        Ini ini = new Ini(new File("bookmarks.ini"));
        String textString = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
        webView.getEngine().loadContent(textString);
    }

    // Adds a bookmark for a series and stores it into the 'bookmarks.ini' file
    public void setBookmark() throws IOException {
        if (currentBook != null) {
            Ini ini = new Ini(new File("bookmarks.ini"));
            Section section = ini.get(currentBook);
            LinkedHashMap<String, Integer> bookmarks = new LinkedHashMap<>();
            String bookmarkName = bookmarkTextField.getText();
            ini.put(currentBook, bookmarkName, getVScrollValue(webView));
            // If there are currently no bookmarks then add a new bookmark
            if (section.size() < 2) {
                ini.put(currentBook, "latestProgressForSeries", getVScrollValue(webView));
            } else {
                // Otherwise add a new bookmark and get the highest value from the bookmarks to set as the 'latestProgressForSeries'
                for (String optionName : section.keySet()) {
                    String optionValue = section.get(optionName);
                    bookmarks.put(optionName, Integer.valueOf(optionValue));
                }
                bookmarks.remove("latestProgressForSeries");
                int largest = bookmarks.get(bookmarks.keySet().toArray()[0]);
                for (int i = 1; i < bookmarks.size(); i++) {
                    int current = bookmarks.get(bookmarks.keySet().toArray()[i]);
                    if (current > largest) {
                        largest = current;
                    }
                }
                ini.put(currentBook, "latestProgressForSeries", largest);
            }
            ini.store();
            refreshBookmarkList();
        }
        bookmarkTextField.clear();
    }

    // Scrolls to the section the bookmark is saved at
    public void visitBookmarkLocation(String bookmarkName) throws IOException {
        Ini ini = new Ini(new File("bookmarks.ini"));
        int value = Integer.valueOf(ini.get(currentBook, bookmarkName));
        scrollTo(webView, 0, value);
    }

    // Helper method to scroll to a section
    public void scrollTo(WebView view, int x, int y) {
        view.getEngine().executeScript("window.scrollTo(" + x + ", " + y + ")");
    }

    // Gets the scroll value at the current section
    public int getVScrollValue(WebView view) {
        return (Integer) view.getEngine().executeScript("document.body.scrollTop");
    }

}
