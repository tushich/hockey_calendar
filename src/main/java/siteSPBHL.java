import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface siteSPBHL {

    static List<Map<String,String>> getMatchTable(String url)
    {
        List<Map<String,String>> matchTable = new ArrayList<>();

        Document doc;

        try
        {
            doc = Jsoup.connect(url).get();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.print("Ошибка чтения страницы HTML");
            return matchTable;
        }

        Elements rows = doc.getElementById("MatchGridView").select("tr");
        String[] colNames = new String[]
                    {   "Турнир",
                        "Тур",
                        "Номер",
                        "startDate",
                        "startTime",
                        "Стадион",
                        "teams",
                        "count",
                        "protokolExist",
                        "linkMatch",
                        "matchID"};


        for (int i = 1; i < rows.size(); i++)
        {
            Map<String,String> rowTable = new HashMap<>();
            // TODO Переделать строку таблицы матчей с мапы на свой отдельный объект Match
            Elements cols = rows.get(i).select((i == 0)? "th": "td");// разбиваем полученную строку по тегу на столбы

            for (int j = 0; j < cols.size(); j++)
            {
                if(j == 6) // колонка с матчем
                {
                    Elements matchElements = cols.get(j).select("a");
                    if(matchElements.size() > 0)
                    {
                        // Вытащим matchID из ссылки. Состоит он из IDТурнира + v + IDМатча
                        String matchID = matchElements.get(0).attributes().toString();

                        // Добавим ссылку на матч
                        rowTable.put("linkMatch", "https://spbhl.ru/" + matchID.replace(" href=\"", "")
                                .replace("\"", "")
                                .replace("&amp;", "&"));

                        matchID = matchID.replace(" href=\"Match.aspx?TournamentID=", "");
                        matchID = matchID.replace("MatchID=", "");
                        matchID = matchID.replace("\"", "");
                        matchID = matchID.replace("&amp;", "v");

                        rowTable.put("matchID", matchID);
                    }
                }
                if(j == 8)
                {
                    rowTable.put("protokolExist", (cols.get(j).select("a").size()) > 0? "Есть" : "Нет");
                }
                else
                {
                    rowTable.put(colNames[j], cols.get(j).text());
                }
            }

            matchTable.add(rowTable);

        }
        return matchTable;
    }
}
