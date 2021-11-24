package Model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.Arrays;

public class SharedModel {

    private ObservableList<File> bookList = FXCollections.observableArrayList(Arrays.asList(new File("books").listFiles()));

    public ObservableList<File> getBookList() {
        return bookList;
    }

    public void addToBookList(String fileName) {
        bookList.add(new File(fileName));
    }
}
