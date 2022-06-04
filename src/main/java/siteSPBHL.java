import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface siteSPBHL {

    static List<Match> getMatchTable(String url)
    {
        List<Match> matchTable = new ArrayList<>();

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

        for (int i = 1; i < rows.size(); i++)
        {
            Match match = new Match();
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
                        match.setLinkMatch("https://spbhl.ru/" + matchID.replace(" href=\"", "")
                                .replace("\"", "")
                                .replace("&amp;", "&"));

                        matchID = matchID.replace(" href=\"Match.aspx?TournamentID=", "");
                        matchID = matchID.replace("MatchID=", "");
                        matchID = matchID.replace("\"", "");
                        matchID = matchID.replace("&amp;", "v");

                        match.setMatchID(matchID);
                    }
                }
                if(j == 3 || j == 4) // дату собираем из 2х строк
                {
                    match.setStartDateTime(cols.get(3).text(), cols.get(4).text());
                }
                else if(j == 8)
                {
                    match.setProtokolExist((cols.get(j).select("a").size()) > 0? "Есть" : "Нет");
                }
                else
                {
                    match.setByColumnId(j, cols.get(j).text());
                }
            }

            matchTable.add(match);

        }
        return matchTable;
    }
}
