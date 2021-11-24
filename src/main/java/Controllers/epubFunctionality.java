package Controllers;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.epub.EpubReader;
import org.ini4j.Ini;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class epubFunctionality {

    public static Book book;
    public static ArrayList<String> chapters = new ArrayList<>();
    public static ArrayList<String> books = new ArrayList<>();

    public void setBook(String bookDirectory) throws IOException {
        EpubReader epubReader = new EpubReader();
        book = epubReader.readEpub(new FileInputStream(bookDirectory));
    }

    public void getText() {
        System.out.println(book.getTitle());
        Spine spine = new Spine(book.getTableOfContents());
        for (SpineReference bookSection : spine.getSpineReferences()) {
            Resource res = bookSection.getResource();
            StringBuilder chapter = new StringBuilder();
            try {
                InputStream is = res.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                while (reader.ready()) {
                    chapter.append(reader.readLine());
                }
                chapters.add(chapter.toString());
            } catch (IOException e) {

            }
        }
    }

    public void addAllBooksToSeries(String directory) {
        File folder = new File(directory);
        ArrayList<File> files = new ArrayList(Arrays.asList(folder.listFiles()));
        for (File f : files) {
            if (f.getAbsolutePath().contains(".epub")) {
                books.add(f.getAbsolutePath());
            }
        }
        for (String s : books) {
            System.out.println(s);
        }
    }

    public void createBookForSeries() throws IOException {
        for (String s : books) {
            setBook(s);
            getText();
        }
    }

    public void writeToFile(String name) throws IOException {
        File file = new File("books/" + name + ".book");
        FileWriter fw = new FileWriter(file.getAbsolutePath(), true);
        BufferedWriter bw = new BufferedWriter(fw);
        for (int i = 0; i < chapters.size(); i++) {
            bw.append(chapters.get(i));
        }
        bw.close();
        Ini ini = new Ini(new File("bookmarks.ini"));
        ini.put(name, "latestProgressForSeries", 0);
        ini.store();
        chapters.clear();
        books.clear();
    }

}
