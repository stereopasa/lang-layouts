import com.google.gson.*;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Startup {

    public static void main(String[] args) throws IOException {
        Startup startup = new Startup();
        Path assetsPath = Paths.get("assets");
        Files.walkFileTree(assetsPath, startup.new Visitor());
    }

    class LayoutVO {
        String layoutName;
        String[] languages;
        String type;

        public LayoutVO(String layoutName, String[] languages, String type) {
            this.layoutName = layoutName;
            this.languages = languages;
            this.type = type;
        }

        JsonObject getJSONObject() {
            JsonObject res = new JsonObject();
            res.addProperty("layout", layoutName);
            res.add("lang", new JsonArray());
            res.addProperty("type", type);

            return res;
        }
    }

    class Visitor extends SimpleFileVisitor<Path> {
        private JsonParser parser = new JsonParser();
        private Gson gson = new GsonBuilder().setPrettyPrinting().create();


        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (file.getFileName().toString().equals("config.json")) {
                System.out.println(file.toAbsolutePath());
                String fileString = new String(Files.readAllBytes(file));

                Pattern pattern = Pattern.compile("\\s+\"layouts\": \\[(\\s|.)+?\\],");
                Matcher matcher = pattern.matcher(fileString);
                if (matcher.find()) {
                    int start = matcher.start();
                    int end = matcher.end();
                    String layoutsDataString = matcher.group();
                    String jsonString = "{" + layoutsDataString.substring(0, layoutsDataString.lastIndexOf(',')) + "}";
                    JsonObject jsonObject = parser.parse(jsonString).getAsJsonObject();
                    JsonElement layoutsElement = jsonObject.get("layouts");
                    if (layoutsElement != null) {
                        JsonArray layouts = new JsonArray();
                        for (JsonElement layout : layoutsElement.getAsJsonArray()) {
                            String layoutName = layout.getAsString();
                            LayoutVO vo = new LayoutVO(layoutName, new String[]{}, "game");
                            layouts.add(vo.getJSONObject());
                        }

                        if (layouts != null) {
                            jsonObject.add("layouts", layouts);
                            String prettyJson = gson.toJson(jsonObject);

                            int startPretty = prettyJson.indexOf('"');
                            int endPretty = prettyJson.lastIndexOf(']') + 1;
                            String writeJson = prettyJson.substring(startPretty, endPretty);
                            String resultString = fileString.substring(0, start)
                                    + System.getProperty("line.separator") + ' ' + writeJson + ","
                                    + fileString.substring(end);

                            try (Writer writer = Files.newBufferedWriter(file.getParent().resolve("res.json"))) {
                                writer.write(resultString);
                            }
                        }
                    }
                }
                return FileVisitResult.SKIP_SUBTREE;
            }
            return FileVisitResult.CONTINUE;
        }
    }

}