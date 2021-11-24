import org.ini4j.Ini;
import org.ini4j.Profile.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class Test {

    public static void main(String[] args) throws IOException {
        Ini ini = new Ini(new File("bookmarks.ini"));
        Section section = ini.get("Nidome no Yuusha");
//        LinkedHashMap<String, Integer> bookmarks = new LinkedHashMap<>();
        ArrayList<String> list = new ArrayList<>();
        for (String optionName : section.keySet()) {
//            String optionValue = section.get(optionName);
            list.add(optionName);
        }
        list.remove("latestProgressForSeries");
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }
    }

}
