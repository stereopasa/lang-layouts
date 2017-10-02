import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

public class Startup {

    public static void main(String[] args) throws IOException {
        Startup startup = new Startup();
        Path assetsPath = Paths.get("assets");
        Files.walkFileTree(assetsPath, startup.new Visitor());
    }

    class LayoutVO {
        String layouName;
        String[] languages;
        String type;

        public LayoutVO(String layouName, String[] languages, String type) {
            this.layouName = layouName;
            this.languages = languages;
            this.type = type;
        }

        JSONObject getJSONObject() {
            JSONObject res = new JSONObject();
            res.put("layout", layouName);
            res.put("lang", Arrays.toString(languages));
            res.put("type", type);

            return res;
        }
    }

    class Visitor extends SimpleFileVisitor<Path> {
        private JSONParser parser = new JSONParser();

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (file.getFileName().toString().equals("config.json")) {
                System.out.println(file.toAbsolutePath());
                try (Reader reader = Files.newBufferedReader(file)) {
                    JSONObject data = (JSONObject) parser.parse(reader);
                    System.out.println(data);

                    JSONArray layouts = (JSONArray) data.get("layouts");
                    if (layouts != null) {
                        JSONArray newLayouts = new JSONArray();
                        for (Object layout : layouts) {
                            String layoutName = (String) layout;
                            LayoutVO vo = new LayoutVO(layoutName, new String[]{}, "game");
                            newLayouts.add(vo.getJSONObject());
                        }

                        if (newLayouts != null) {
                            data.replace("layouts", newLayouts);
                            System.out.println(data);
                        }
                    }

                    try (Writer writer = Files.newBufferedWriter(file.getParent().resolve("res.json"))) {
                        writer.write(data.toJSONString());
                    }

                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
                return FileVisitResult.SKIP_SUBTREE;
            }
            return FileVisitResult.CONTINUE;
        }
    }

}